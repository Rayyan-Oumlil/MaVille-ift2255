"use client"

import { useEffect } from "react"
import { toast } from "sonner"
import { useWebSocket } from "./use-websocket"
import type { WebSocketMessage } from "@/lib/websocket"
import { useAuth } from "@/contexts/AuthContext"

export function useToastNotifications(enabled: boolean = true) {
  const { user } = useAuth()
  
  // Déterminer la destination selon le type d'utilisateur
  const getDestination = () => {
    if (!user) return "/topic/notifications"
    if (user.type === "RESIDENT" && user.email) {
      return `/topic/notifications/${user.email}`
    }
    if (user.type === "PRESTATAIRE" && user.neq) {
      return `/topic/notifications/${user.neq}`
    }
    if (user.type === "STPM") {
      return "/topic/notifications/stpm"
    }
    return "/topic/notifications"
  }

  const handleMessage = (message: WebSocketMessage) => {
    if (!enabled) return

    if (message.type === "notification") {
      const notification = message.payload
      
      toast(notification.message || notification.payload?.message || "Nouvelle notification", {
        description: notification.type || notification.payload?.type,
        action: notification.projetId || notification.payload?.projetId
          ? {
              label: "Voir",
              onClick: () => {
                window.location.href = `/prestataires/projets`
              },
            }
          : undefined,
      })
    } else if (message.type === "error") {
      toast.error(message.payload?.message || "Une erreur est survenue")
    } else if (message.type === "success") {
      toast.success(message.payload?.message || "Opération réussie")
    }
  }

  const destination = getDestination()
  const { status } = useWebSocket(handleMessage, enabled && !!user, destination)

  useEffect(() => {
    if (enabled && status === "connected") {
      // Ne pas afficher de toast de succès automatiquement pour éviter le spam
      console.log("Notifications WebSocket activées")
    } else if (enabled && status === "error") {
      // Ne pas afficher d'erreur automatiquement, seulement logger
      console.warn("Impossible de se connecter aux notifications en temps réel")
    }
  }, [status, enabled])

  return { status }
}
