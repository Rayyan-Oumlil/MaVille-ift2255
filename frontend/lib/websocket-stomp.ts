/**
 * WebSocket Client avec STOMP pour MaVille
 * Compatible avec Spring WebSocket (SockJS + STOMP)
 */

import SockJS from 'sockjs-client'
import { Client, IMessage } from '@stomp/stompjs'

export type WebSocketStatus = "connecting" | "connected" | "disconnected" | "error"

export class WebSocketStompClient {
  private client: Client | null = null
  private url: string
  private listeners: Map<string, Set<(data: any) => void>> = new Map()
  private statusListeners: Set<(status: WebSocketStatus) => void> = new Set()
  private status: WebSocketStatus = "disconnected"
  private shouldReconnect = true
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5

  constructor(url: string) {
    this.url = url
  }

  connect(): void {
    if (this.client?.connected) {
      return
    }

    this.setStatus("connecting")

    try {
      // Utiliser SockJS pour compatibilité avec Spring
      const socket = new SockJS(this.url)
      
      this.client = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          this.setStatus("connected")
          this.reconnectAttempts = 0
          console.log("WebSocket STOMP connected")
        },
        onStompError: (frame) => {
          console.error("STOMP error:", frame)
          this.setStatus("error")
        },
        onDisconnect: () => {
          this.setStatus("disconnected")
          console.log("WebSocket STOMP disconnected")
          
          if (this.shouldReconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            setTimeout(() => this.connect(), 5000 * this.reconnectAttempts)
          }
        },
      })

      this.client.activate()
    } catch (error) {
      console.error("Error creating WebSocket STOMP:", error)
      this.setStatus("error")
    }
  }

  disconnect(): void {
    this.shouldReconnect = false
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
  }

  subscribe(destination: string, callback: (data: any) => void): () => void {
    if (!this.client?.connected) {
      console.warn("WebSocket not connected. Subscription queued.")
      // Attendre la connexion
      const checkConnection = setInterval(() => {
        if (this.client?.connected) {
          clearInterval(checkConnection)
          this.doSubscribe(destination, callback)
        }
      }, 100)
      return () => clearInterval(checkConnection)
    }

    return this.doSubscribe(destination, callback)
  }

  private doSubscribe(destination: string, callback: (data: any) => void): () => void {
    if (!this.client?.connected) {
      return () => {}
    }

    const subscription = this.client.subscribe(destination, (message: IMessage) => {
      try {
        const data = JSON.parse(message.body)
        callback(data)
      } catch (error) {
        console.error("Error parsing WebSocket message:", error)
        callback(message.body)
      }
    })

    // Stocker pour pouvoir se désabonner
    if (!this.listeners.has(destination)) {
      this.listeners.set(destination, new Set())
    }
    this.listeners.get(destination)!.add(callback)

    return () => {
      subscription.unsubscribe()
      this.listeners.get(destination)?.delete(callback)
    }
  }

  send(destination: string, body: any): void {
    if (this.client?.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      })
    } else {
      console.warn("WebSocket not connected. Message not sent:", body)
    }
  }

  onStatusChange(callback: (status: WebSocketStatus) => void): () => void {
    this.statusListeners.add(callback)
    callback(this.status)
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
}

// Instance singleton
let wsStompClientInstance: WebSocketStompClient | null = null

export function getWebSocketStompClient(): WebSocketStompClient {
  if (!wsStompClientInstance) {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7000/api'
    const baseUrl = apiUrl.replace('/api', '')
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || `${baseUrl}/ws`
    wsStompClientInstance = new WebSocketStompClient(wsUrl)
  }
  return wsStompClientInstance
}
