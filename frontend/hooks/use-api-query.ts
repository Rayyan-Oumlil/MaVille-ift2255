"use client"

import { useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseMutationOptions } from "@tanstack/react-query"
import { toast } from "sonner"
import type * as api from "@/lib/api"

// Helper pour créer des queries avec retry automatique et gestion d'erreur
export function useApiQuery<TData, TError = Error>(
  queryKey: string[],
  queryFn: () => Promise<TData>,
  options?: Omit<UseQueryOptions<TData, TError>, "queryKey" | "queryFn">
) {
  return useQuery({
    queryKey,
    queryFn,
    ...options,
    onError: (error) => {
      const message = error instanceof Error ? error.message : "Une erreur est survenue"
      toast.error(message)
      options?.onError?.(error)
    },
  })
}

// Helper pour créer des mutations avec retry automatique et gestion d'erreur
export function useApiMutation<TData, TVariables, TError = Error>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options?: Omit<UseMutationOptions<TData, TError, TVariables>, "mutationFn">
) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn,
    ...options,
    onSuccess: (data, variables, context) => {
      // Invalider les queries pertinentes après une mutation réussie
      queryClient.invalidateQueries()
      options?.onSuccess?.(data, variables, context)
    },
    onError: (error, variables, context) => {
      const message = error instanceof Error ? error.message : "Une erreur est survenue"
      toast.error(message)
      options?.onError?.(error, variables, context)
    },
  })
}

// Hook pour retry automatique avec exponential backoff
export function useRetryableMutation<TData, TVariables, TError = Error>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  maxRetries: number = 3,
  options?: Omit<UseMutationOptions<TData, TError, TVariables>, "mutationFn">
) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: async (variables: TVariables) => {
      let lastError: TError | null = null
      
      for (let attempt = 0; attempt <= maxRetries; attempt++) {
        try {
          return await mutationFn(variables)
        } catch (error) {
          lastError = error as TError
          
          if (attempt < maxRetries) {
            const delay = Math.min(1000 * Math.pow(2, attempt), 30000)
            await new Promise((resolve) => setTimeout(resolve, delay))
            continue
          }
          
          throw error
        }
      }
      
      throw lastError
    },
    ...options,
    onSuccess: (data, variables, context) => {
      queryClient.invalidateQueries()
      options?.onSuccess?.(data, variables, context)
    },
    onError: (error, variables, context) => {
      const message = error instanceof Error ? error.message : "Une erreur est survenue après plusieurs tentatives"
      toast.error(message, {
        description: "Veuillez réessayer plus tard",
      })
      options?.onError?.(error, variables, context)
    },
  })
}
