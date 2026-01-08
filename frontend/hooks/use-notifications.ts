"use client"

import { useEffect, useState } from "react"
import { useSSE, type SSEStatus } from "./use-sse"
import { useWebSocket, type WebSocketStatus } from "./use-websocket"
import type { WebSocketMessage } from "@/lib/websocket"

export type NotificationMethod = "websocket" | "sse" | "both" | "polling"

export type NotificationStatus = WebSocketStatus | SSEStatus

/**
 * Hook unifié pour les notifications
 * Permet de choisir entre WebSocket, SSE, les deux, ou polling
 * 
 * @param userIdentifier Email (résident), NEQ (prestataire), ou "stpm"
 * @param onMessage Callback appelé quand une notification arrive
 * @param method Méthode de notification: "websocket", "sse", "both", ou "polling"
 * @param autoConnect Si true, se connecte automatiquement
 */
export function useNotifications(
  userIdentifier: string | null,
  onMessage?: (message: WebSocketMessage) => void,
  method: NotificationMethod = "sse", // SSE par défaut pour scale-to-zero
  autoConnect: boolean = true
) {
  const [status, setStatus] = useState<NotificationStatus>("disconnected")
  const [pollingInterval, setPollingInterval] = useState<NodeJS.Timeout | null>(null)

  // WebSocket
  const { status: wsStatus } = useWebSocket(
    method === "websocket" || method === "both" ? onMessage : undefined,
    autoConnect && (method === "websocket" || method === "both"),
    "/topic/notifications"
  )

  // SSE
  const { status: sseStatus } = useSSE(
    method === "sse" || method === "both" ? userIdentifier : null,
    method === "sse" || method === "both" ? onMessage : undefined,
    autoConnect && (method === "sse" || method === "both")
  )

  // Polling (fallback)
  useEffect(() => {
    if (method !== "polling" || !userIdentifier || !autoConnect) {
      if (pollingInterval) {
        clearInterval(pollingInterval)
        setPollingInterval(null)
      }
      return
    }

    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7000/api"
    
    // Fonction pour récupérer les notifications
    const fetchNotifications = async () => {
      try {
        const response = await fetch(`${apiUrl}/notifications/unread/${encodeURIComponent(userIdentifier)}`)
        if (response.ok) {
          const notifications = await response.json()
          notifications.forEach((notification: any) => {
            onMessage?.({
              type: "notification",
              payload: notification,
              timestamp: notification.dateCreation || new Date().toISOString(),
            })
          })
        }
      } catch (error) {
        console.error("Error fetching notifications:", error)
      }
    }

    // Récupérer immédiatement
    fetchNotifications()

    // Puis toutes les 30 secondes
    const interval = setInterval(fetchNotifications, 30000)
    setPollingInterval(interval)

    return () => {
      clearInterval(interval)
      setPollingInterval(null)
    }
  }, [method, userIdentifier, autoConnect, onMessage])

  // Mettre à jour le statut combiné
  useEffect(() => {
    if (method === "websocket") {
      setStatus(wsStatus)
    } else if (method === "sse") {
      setStatus(sseStatus)
    } else if (method === "both") {
      // Si l'un des deux est connecté, on est connecté
      if (wsStatus === "connected" || sseStatus === "connected") {
        setStatus("connected")
      } else if (wsStatus === "connecting" || sseStatus === "connecting") {
        setStatus("connecting")
      } else if (wsStatus === "error" && sseStatus === "error") {
        setStatus("error")
      } else {
        setStatus("disconnected")
      }
    } else if (method === "polling") {
      setStatus(pollingInterval ? "connected" : "disconnected")
    }
  }, [method, wsStatus, sseStatus, pollingInterval])

  return {
    status,
    method,
  }
}
