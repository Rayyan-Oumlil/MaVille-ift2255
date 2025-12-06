"use client"

import { useToastNotifications } from "@/hooks/use-toast-notifications"
import { useAuth } from "@/contexts/AuthContext"

export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  // Activer les notifications seulement si l'utilisateur est connecté
  useToastNotifications(!!user)

  return <>{children}</>
}
