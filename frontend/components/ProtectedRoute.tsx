"use client"

import { useAuth } from "@/contexts/AuthContext"
import { useRouter } from "next/navigation"
import { useEffect, useRef } from "react"

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth()
  const router = useRouter()
  const redirectTimeoutRef = useRef<NodeJS.Timeout | null>(null)

  useEffect(() => {
    // Attendre que le chargement initial soit terminé avant de vérifier l'authentification
    if (!isLoading && !isAuthenticated) {
      // Redirection immédiate avec un petit délai pour éviter les boucles
      if (redirectTimeoutRef.current) {
        clearTimeout(redirectTimeoutRef.current)
      }
      redirectTimeoutRef.current = setTimeout(() => {
        window.location.href = "/login"
      }, 0)
    }

    return () => {
      if (redirectTimeoutRef.current) {
        clearTimeout(redirectTimeoutRef.current)
      }
    }
  }, [isAuthenticated, isLoading])

  // Afficher un loader pendant le chargement initial
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-muted-foreground">Chargement...</p>
      </div>
    )
  }

  // Rediriger immédiatement si non authentifié (après le chargement)
  if (!isAuthenticated) {
    // Redirection immédiate sans délai visible
    if (typeof window !== "undefined") {
      window.location.href = "/login"
    }
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-muted-foreground">Redirection vers la page de connexion...</p>
      </div>
    )
  }

  return <>{children}</>
}
