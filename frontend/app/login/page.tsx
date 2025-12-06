"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/contexts/AuthContext"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"

export default function LoginPage() {
  const [identifier, setIdentifier] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)
  const { login, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()

  // Rediriger vers le dashboard si déjà authentifié
  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.push("/")
    }
  }, [isAuthenticated, isLoading, router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setLoading(true)
    
    try {
      const success = await login(identifier, password)
      if (success) {
        router.push("/")
      } else {
        setError("Identifiants invalides")
      }
    } catch (err) {
      setError("Erreur lors de la connexion. Veuillez réessayer.")
    } finally {
      setLoading(false)
    }
  }

  // Afficher un loader pendant le chargement initial
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <p className="text-muted-foreground">Chargement...</p>
      </div>
    )
  }

  // Ne rien afficher si déjà authentifié (redirection en cours)
  if (isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background p-4">
        <p className="text-muted-foreground">Redirection vers le tableau de bord...</p>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">Connexion à MaVille</CardTitle>
          <CardDescription className="text-center">
            Connectez-vous avec votre email (résident) ou NEQ (prestataire)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="identifier">Email ou NEQ</Label>
              <Input
                id="identifier"
                type="text"
                placeholder="email@exemple.com ou ABC123"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
                disabled={loading}
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="password">Mot de passe</Label>
              <Input
                id="password"
                type="password"
                placeholder="Votre mot de passe"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={loading}
              />
            </div>
            
            {error && (
              <div className="p-3 text-sm text-red-500 bg-red-500/10 border border-red-500/20 rounded">
                {error}
              </div>
            )}
            
            <Button 
              type="submit" 
              className="w-full" 
              disabled={loading}
            >
              {loading ? "Connexion..." : "Se connecter"}
            </Button>
            
            <div className="text-xs text-muted-foreground text-center space-y-1 pt-4 border-t">
              <p className="font-semibold">Comptes de test :</p>
              <p>Résident : marie@test.com / password123</p>
              <p>Prestataire : ABC123 / password123</p>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
