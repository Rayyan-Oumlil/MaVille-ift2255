/**
 * WebSocket Client for MaVille
 * Gère les connexions WebSocket pour les notifications en temps réel
 */

export type WebSocketMessage = {
  type: string
  payload: any
  timestamp: string
}

export type NotificationMessage = {
  type: "notification"
  payload: {
    id: string
    message: string
    type: string
    date: string
    projetId?: number
  }
}

export type WebSocketStatus = "connecting" | "connected" | "disconnected" | "error"

export class WebSocketClient {
  private ws: WebSocket | null = null
  private url: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 1000
  private listeners: Map<string, Set<(data: any) => void>> = new Map()
  private statusListeners: Set<(status: WebSocketStatus) => void> = new Set()
  private status: WebSocketStatus = "disconnected"
  private shouldReconnect = true

  constructor(url: string) {
    this.url = url
  }

  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      return
    }

    this.setStatus("connecting")

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        this.setStatus("connected")
        this.reconnectAttempts = 0
        console.log("WebSocket connected")
      }

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data)
          this.handleMessage(message)
        } catch (error) {
          console.error("Error parsing WebSocket message:", error)
        }
      }

      this.ws.onerror = (error) => {
        console.error("WebSocket error:", error)
        this.setStatus("error")
      }

      this.ws.onclose = () => {
        this.setStatus("disconnected")
        console.log("WebSocket disconnected")

        if (this.shouldReconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1)
          console.log(
            `Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`
          )
          setTimeout(() => this.connect(), delay)
        }
      }
    } catch (error) {
      console.error("Error creating WebSocket:", error)
      this.setStatus("error")
    }
  }

  disconnect(): void {
    this.shouldReconnect = false
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  send(message: any): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn("WebSocket is not connected. Message not sent:", message)
    }
  }

  on(event: string, callback: (data: any) => void): () => void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event)!.add(callback)

    // Retourner une fonction pour se désabonner
    return () => {
      this.off(event, callback)
    }
  }

  off(event: string, callback: (data: any) => void): void {
    const eventListeners = this.listeners.get(event)
    if (eventListeners) {
      eventListeners.delete(callback)
    }
  }

  onStatusChange(callback: (status: WebSocketStatus) => void): () => void {
    this.statusListeners.add(callback)
    // Appeler immédiatement avec le statut actuel
    callback(this.status)

    // Retourner une fonction pour se désabonner
    return () => {
      this.statusListeners.delete(callback)
    }
  }

  getStatus(): WebSocketStatus {
    return this.status
  }

  private setStatus(status: WebSocketStatus): void {
    this.status = status
    this.statusListeners.forEach((callback) => callback(status))
  }

  private handleMessage(message: WebSocketMessage): void {
    const eventListeners = this.listeners.get(message.type)
    if (eventListeners) {
      eventListeners.forEach((callback) => {
        try {
          callback(message.payload)
        } catch (error) {
          console.error(`Error in WebSocket listener for ${message.type}:`, error)
        }
      })
    }

    // Écouter aussi tous les messages
    const allListeners = this.listeners.get("*")
    if (allListeners) {
      allListeners.forEach((callback) => {
        try {
          callback(message)
        } catch (error) {
          console.error("Error in WebSocket * listener:", error)
        }
      })
    }
  }
}

// Instance singleton pour l'application
let wsClientInstance: WebSocketClient | null = null

export function getWebSocketClient(): WebSocketClient {
  if (!wsClientInstance) {
    // Utiliser SockJS pour compatibilité avec Spring WebSocket
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7000/api'
    const baseUrl = apiUrl.replace('/api', '').replace('http://', 'ws://').replace('https://', 'wss://')
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || `${baseUrl}/ws`
    wsClientInstance = new WebSocketClient(wsUrl)
  }
  return wsClientInstance
}
