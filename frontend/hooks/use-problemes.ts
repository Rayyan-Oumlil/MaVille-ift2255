"use client"

import { useApiQuery, useApiMutation } from "./use-api-query"
import * as api from "@/lib/api"

export function useProblemes(params?: {
  quartier?: string
  type?: string
  page?: number
  size?: number
}) {
  return useApiQuery(
    ["problemes", params],
    () => api.getStpmProblemes(params),
    {
      staleTime: 30 * 1000, // 30 secondes
    }
  )
}

export function useSignalerProbleme() {
  return useApiMutation(
    (data: Parameters<typeof api.signalerProbleme>[0]) => api.signalerProbleme(data),
    {
      onSuccess: () => {
        // Invalider la liste des problèmes après un signalement
      },
    }
  )
}
