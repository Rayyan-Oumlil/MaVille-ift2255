"use client"

import { useApiQuery } from "./use-api-query"
import * as api from "@/lib/api"
import { useAuth } from "@/contexts/AuthContext"

export function useNotifications() {
  const { user } = useAuth()

  return useApiQuery(
    ["notifications", user?.email || user?.neq],
    async () => {
      if (!user) throw new Error("Utilisateur non connecté")

      if (user.type === "RESIDENT" && user.email) {
        return api.getResidentNotifications(user.email)
      } else if (user.type === "PRESTATAIRE" && user.neq) {
        return api.getPrestataireNotifications(user.neq)
      } else if (user.type === "STPM") {
        return api.getStpmNotifications()
      }

      throw new Error("Type d'utilisateur non reconnu")
    },
    {
      enabled: !!user,
      refetchInterval: 30000, // Rafraîchir toutes les 30 secondes
    }
  )
}
