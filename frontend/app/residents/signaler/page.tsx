"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { ProtectedRoute } from "@/components/ProtectedRoute"
import { useAuth } from "@/contexts/AuthContext"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiMutation } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import { toast } from "sonner"
import PlusIcon from "@/components/icons/plus"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { FileUpload, type UploadedFile } from "@/components/ui/file-upload"

export default function SignalerProblemePage() {
  const [lieu, setLieu] = useState("")
  const [description, setDescription] = useState("")
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([])
  const [success, setSuccess] = useState(false)
  const { user } = useAuth()
  const router = useRouter()

  const mutation = useApiMutation(
    (data: Parameters<typeof api.signalerProbleme>[0]) => api.signalerProbleme(data),
    {
      onSuccess: (result) => {
        if (result.success) {
          toast.success("Problème signalé avec succès !")
          setSuccess(true)
          setLieu("")
          setDescription("")
          setUploadedFiles([])
          // Rediriger vers la page des travaux après 2 secondes
          setTimeout(() => {
            router.push("/residents")
          }, 2000)
        }
      },
    }
  )

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!user?.email) {
      toast.error("Vous devez être connecté pour signaler un problème")
      return
    }

    try {
      await mutation.mutateAsync({
        residentId: user.email,
        lieu: lieu.trim(),
        description: description.trim(),
        type: "ENTRETIEN_URBAIN", // Type par défaut
      })
    } catch (error) {
      // Error already handled by useApiMutation (toast)
    }
  }

  const loading = mutation.isPending
  const error = mutation.error instanceof Error ? mutation.error.message : ""

  return (
    <ProtectedRoute>
      <DashboardPageLayout
        header={{
          title: "SIGNALER UN PROBLÈME",
          description: "Signalez un problème routier ou urbain",
          icon: PlusIcon,
        }}
      >
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Formulaire de signalement</CardTitle>
              <CardDescription>
                Remplissez ce formulaire pour signaler un problème dans votre quartier
              </CardDescription>
            </CardHeader>
            <CardContent>
              {success ? (
                <div className="p-4 text-sm text-green-500 bg-green-500/10 border border-green-500/20 rounded">
                  <p className="font-semibold">Problème signalé avec succès !</p>
                  <p className="mt-1">Redirection vers la page des travaux...</p>
                </div>
              ) : (
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="lieu">Lieu du problème *</Label>
                    <Input
                      id="lieu"
                      type="text"
                      placeholder="Ex: 123 Rue Saint-Denis, Plateau-Mont-Royal"
                      value={lieu}
                      onChange={(e) => setLieu(e.target.value)}
                      required
                      disabled={loading}
                      minLength={3}
                    />
                    <p className="text-xs text-muted-foreground">
                      Indiquez l'adresse ou la localisation précise du problème
                    </p>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="description">Description du problème *</Label>
                    <Textarea
                      id="description"
                      placeholder="Décrivez en détail le problème observé..."
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      required
                      disabled={loading}
                      minLength={10}
                      rows={5}
                    />
                    <p className="text-xs text-muted-foreground">
                      Minimum 10 caractères. Soyez aussi précis que possible.
                    </p>
                  </div>

                  <div className="space-y-2">
                    <Label>Photos du problème (optionnel)</Label>
                    <FileUpload
                      files={uploadedFiles}
                      onFilesChange={setUploadedFiles}
                      accept="image/*"
                      maxFiles={5}
                      maxSizeMB={5}
                      disabled={loading}
                    />
                    <p className="text-xs text-muted-foreground">
                      Ajoutez des photos pour mieux documenter le problème
                    </p>
                  </div>

                  {error && (
                    <div className="p-3 text-sm text-red-500 bg-red-500/10 border border-red-500/20 rounded">
                      {error}
                    </div>
                  )}

                  <div className="flex gap-2">
                    <Button
                      type="submit"
                      disabled={loading || !lieu.trim() || !description.trim()}
                      className="flex-1"
                    >
                      {loading ? "Envoi en cours..." : "Signaler le problème"}
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => router.push("/residents")}
                      disabled={loading}
                    >
                      Annuler
                    </Button>
                  </div>
                </form>
              )}
            </CardContent>
          </Card>
        </div>
      </DashboardPageLayout>
    </ProtectedRoute>
  )
}
