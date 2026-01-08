"use client"

import { useEffect } from "react"
import { toast } from "sonner"
import { useNotifications, type NotificationMethod } from "./use-notifications"
import type { WebSocketMessage } from "@/lib/websocket"
import { useAuth } from "@/contexts/AuthContext"

/**
 * Configuration de la méthode de notification
 * Peut être définie via variable d'environnement NEXT_PUBLIC_NOTIFICATION_METHOD
 * Options: "websocket", "sse", "both", "polling"
 * Par défaut: "sse" (permet scale-to-zero sur Cloud Run)
 */
const getNotificationMethod = (): NotificationMethod => {
  if (typeof window === "undefined") return "sse"
  const method = process.env.NEXT_PUBLIC_NOTIFICATION_METHOD || "sse"
  return (["websocket", "sse", "both", "polling"].includes(method) 
    ? method 
    : "sse") as NotificationMethod
}

export function useToastNotifications(enabled: boolean = true) {
  const { user } = useAuth()
  const method = getNotificationMethod()
  
  // Déterminer l'identifiant utilisateur selon le type
  const getUserIdentifier = () => {
    if (!user) return null
    if (user.type === "RESIDENT" && user.email) {
      return user.email
    }
    if (user.type === "PRESTATAIRE" && user.neq) {
      return user.neq
    }
    if (user.type === "STPM") {
      return "stpm"
    }
    return null
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

  const userIdentifier = getUserIdentifier()
  const { status } = useNotifications(userIdentifier, handleMessage, method, enabled && !!user)

  useEffect(() => {
    if (enabled && status === "connected") {
      console.log(`Notifications ${method} activées`)
    } else if (enabled && status === "error") {
      console.warn(`Impossible de se connecter aux notifications (méthode: ${method})`)
    }
  }, [status, enabled, method])

  return { status, method }
}
