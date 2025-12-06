"use client"

import { createContext, useContext, useState, useEffect } from "react"

export type UserType = "RESIDENT" | "PRESTATAIRE" | "STPM" | null

export interface User {
  id: number
  email?: string
  neq?: string
  nom: string
}

interface AuthContextType {
  user: User | null
  userType: UserType
  isAuthenticated: boolean
  isLoading: boolean
  login: (identifier: string, password: string) => Promise<boolean>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [userType, setUserType] = useState<UserType>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Charger depuis localStorage au démarrage
    if (typeof window !== "undefined") {
      const saved = localStorage.getItem("auth")
      if (saved) {
        try {
          const { user, type } = JSON.parse(saved)
          setUser(user)
          setUserType(type)
        } catch (e) {
          // Ignorer les erreurs de parsing
          localStorage.removeItem("auth")
        }
      }
      // Marquer le chargement comme terminé
      setIsLoading(false)
    } else {
      setIsLoading(false)
    }
  }, [])

  const login = async (identifier: string, password: string) => {
    try {
      const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:7000/api';
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      })
      
      const data = await response.json()
      if (data.success) {
        setUser(data.user)
        setUserType(data.type)
        if (typeof window !== "undefined") {
          localStorage.setItem("auth", JSON.stringify({ user: data.user, type: data.type }))
        }
        return true
      }
      return false
    } catch (error) {
      console.error("Erreur lors de la connexion:", error)
      return false
    }
  }

  const logout = () => {
    // Nettoyer immédiatement l'état et le localStorage
    if (typeof window !== "undefined") {
      localStorage.removeItem("auth")
    }
    // Mettre à jour l'état de manière synchrone
    setUser(null)
    setUserType(null)
  }

  return (
    <AuthContext.Provider value={{
      user,
      userType,
      isAuthenticated: !!user,
      isLoading,
      login,
      logout,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth must be used within AuthProvider")
  return context
}
