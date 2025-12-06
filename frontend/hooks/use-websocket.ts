"use client"

import { useEffect, useState, useRef } from "react"
import { getWebSocketStompClient, type WebSocketStatus } from "@/lib/websocket-stomp"
import type { WebSocketMessage } from "@/lib/websocket"

export function useWebSocket(
  onMessage?: (message: WebSocketMessage) => void,
  autoConnect: boolean = true,
  destination: string = "/topic/notifications"
) {
  const [status, setStatus] = useState<WebSocketStatus>("disconnected")
  const wsClientRef = useRef<ReturnType<typeof getWebSocketStompClient> | null>(null)

  useEffect(() => {
    if (typeof window === "undefined") return

    const client = getWebSocketStompClient()
    wsClientRef.current = client

    // Écouter les changements de statut
    const unsubscribeStatus = client.onStatusChange((newStatus) => {
      setStatus(newStatus)
    })

    // S'abonner aux messages
    let unsubscribeMessage: (() => void) | null = null
    if (onMessage) {
      unsubscribeMessage = client.subscribe(destination, (data) => {
        onMessage({
          type: data.type || "notification",
          payload: data.payload || data,
          timestamp: data.timestamp || new Date().toISOString(),
        })
      })
    }

    // Se connecter si autoConnect est activé
    if (autoConnect) {
      client.connect()
    }

    return () => {
      unsubscribeStatus()
      if (unsubscribeMessage) {
        unsubscribeMessage()
      }
      // Ne pas déconnecter ici car d'autres composants pourraient utiliser la même instance
    }
  }, [onMessage, autoConnect, destination])

  const send = (destination: string, message: any) => {
    if (wsClientRef.current) {
      wsClientRef.current.send(destination, message)
    }
  }

  const connect = () => {
    if (wsClientRef.current) {
      wsClientRef.current.connect()
    }
  }

  const disconnect = () => {
    if (wsClientRef.current) {
      wsClientRef.current.disconnect()
    }
  }

  return {
    status,
    send,
    connect,
    disconnect,
  }
}
