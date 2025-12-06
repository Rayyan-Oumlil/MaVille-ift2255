"use client"

import { useState } from "react"
import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery, useApiMutation } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import { toast } from "sonner"
import CuteRobotIcon from "@/components/icons/cute-robot"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"

export default function StpmCandidaturesPage() {
  const [processing, setProcessing] = useState<number | null>(null)

  const { data, isLoading: loading, refetch } = useApiQuery(
    ["candidatures", "stpm", "all"],
    () => api.getStpmCandidatures({ page: 0, size: 50 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const candidatures = data?.data || []

  const mutation = useApiMutation(
    ({ id, accepter }: { id: number; accepter: boolean }) =>
      api.validerCandidature(id, accepter),
    {
      onSuccess: () => {
        toast.success("Candidature traitée avec succès")
        refetch() // Recharger la liste après validation
      },
    }
  )

  const handleValider = async (id: number, accepter: boolean) => {
    try {
      setProcessing(id)
      await mutation.mutateAsync({ id, accepter })
    } catch (error) {
      // Error already handled by useApiMutation (toast)
    } finally {
      setProcessing(null)
    }
  }

  return (
    <ProtectedRoute>
      <DashboardPageLayout
        header={{
          title: "CANDIDATURES",
          description: "Gestion des candidatures des prestataires",
          icon: CuteRobotIcon,
        }}
      >
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Candidatures en attente</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-sm text-muted-foreground">Chargement...</p>
              ) : candidatures.length === 0 ? (
                <p className="text-sm text-muted-foreground">Aucune candidature</p>
              ) : (
                <div className="space-y-2">
                  {candidatures.map((candidature) => (
                    <div key={candidature.id} className="p-3 border rounded">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <h3 className="font-medium">Candidature #{candidature.id}</h3>
                          <p className="text-sm text-muted-foreground mt-1">
                            Prestataire: {candidature.prestataire || "Non spécifié"}
                          </p>
                          <p className="text-sm text-muted-foreground mt-1">
                            {candidature.description || "Aucune description"}
                          </p>
                          <div className="flex gap-2 mt-2">
                            <Badge variant={candidature.statut === "Soumise" ? "default" : "secondary"}>
                              {candidature.statut}
                            </Badge>
                            {candidature.cout && (
                              <Badge variant="outline">
                                Coût: {candidature.cout.toLocaleString("fr-FR", { style: "currency", currency: "CAD" })}
                              </Badge>
                            )}
                          </div>
                          {candidature.dateDebut && (
                            <p className="text-xs text-muted-foreground mt-1">
                              Début: {new Date(candidature.dateDebut).toLocaleDateString("fr-FR")}
                            </p>
                          )}
                          {candidature.dateFin && (
                            <p className="text-xs text-muted-foreground">
                              Fin: {new Date(candidature.dateFin).toLocaleDateString("fr-FR")}
                            </p>
                          )}
                        </div>
                        {candidature.statut === "Soumise" && (
                          <div className="flex gap-2 ml-4">
                            <Button
                              size="sm"
                              variant="default"
                              onClick={() => handleValider(candidature.id, true)}
                              disabled={processing === candidature.id}
                            >
                              {processing === candidature.id ? "Traitement..." : "Accepter"}
                            </Button>
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => handleValider(candidature.id, false)}
                              disabled={processing === candidature.id}
                            >
                              {processing === candidature.id ? "Traitement..." : "Refuser"}
                            </Button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </DashboardPageLayout>
    </ProtectedRoute>
  )
}
