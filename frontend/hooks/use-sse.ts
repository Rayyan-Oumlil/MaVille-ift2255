"use client"

import { useEffect, useState, useRef } from "react"
import type { WebSocketMessage } from "@/lib/websocket"

export type SSEStatus = "connecting" | "connected" | "disconnected" | "error"

/**
 * Hook pour utiliser Server-Sent Events (SSE) pour les notifications
 * SSE permet scale-to-zero sur Cloud Run car ce sont des connexions HTTP standard
 */
export function useSSE(
  userIdentifier: string | null,
  onMessage?: (message: WebSocketMessage) => void,
  autoConnect: boolean = true
) {
  const [status, setStatus] = useState<SSEStatus>("disconnected")
  const eventSourceRef = useRef<EventSource | null>(null)

  useEffect(() => {
    if (typeof window === "undefined" || !userIdentifier || !autoConnect) {
      return
    }

    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7000/api"
    const sseUrl = `${apiUrl}/notifications/stream/${encodeURIComponent(userIdentifier)}`

    setStatus("connecting")

    try {
      const eventSource = new EventSource(sseUrl)
      eventSourceRef.current = eventSource

      eventSource.onopen = () => {
        setStatus("connected")
        console.log("SSE connected for user:", userIdentifier)
      }

      eventSource.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          
          // Gérer les différents types d'événements SSE
          if (event.type === "notification" || data.type === "notification") {
            const message: WebSocketMessage = {
              type: data.type || "notification",
              payload: data.payload || data,
              timestamp: data.timestamp || new Date().toISOString(),
            }
            onMessage?.(message)
          } else if (event.type === "connected" || data.status === "connected") {
            console.log("SSE connection confirmed")
          }
        } catch (error) {
          console.error("Error parsing SSE message:", error)
        }
      }

      // Écouter les événements nommés
      eventSource.addEventListener("notification", (event) => {
        try {
          const data = JSON.parse(event.data)
          const message: WebSocketMessage = {
            type: "notification",
            payload: data.payload || data,
            timestamp: data.timestamp || new Date().toISOString(),
          }
          onMessage?.(message)
        } catch (error) {
          console.error("Error parsing SSE notification event:", error)
        }
      })

      eventSource.onerror = (error) => {
        console.error("SSE error:", error)
        setStatus("error")
        
        // Fermer et nettoyer
        eventSource.close()
        eventSourceRef.current = null
        setStatus("disconnected")
      }

      return () => {
        eventSource.close()
        eventSourceRef.current = null
        setStatus("disconnected")
      }
    } catch (error) {
      console.error("Error creating SSE connection:", error)
      setStatus("error")
    }
  }, [userIdentifier, autoConnect, onMessage])

  const connect = () => {
    // La connexion est gérée automatiquement par useEffect
    // Cette méthode peut être utilisée pour forcer une reconnexion
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
    }
    // Le useEffect se déclenchera automatiquement
  }

  const disconnect = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close()
      eventSourceRef.current = null
      setStatus("disconnected")
    }
  }

  return {
    status,
    connect,
    disconnect,
  }
}

