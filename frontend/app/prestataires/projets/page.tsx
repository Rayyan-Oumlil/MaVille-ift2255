"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import { useAuth } from "@/contexts/AuthContext"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import ProcessorIcon from "@/components/icons/proccesor"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

export default function PrestatairesProjetsPage() {
  const { user } = useAuth()

  const { data, isLoading: loading } = useApiQuery(
    ["projets", "prestataire", user?.neq],
    () => {
      if (!user?.neq) throw new Error("NEQ requis")
      return api.getPrestataireProjets(user.neq)
    },
    {
      enabled: !!user?.neq,
      staleTime: 30 * 1000,
    }
  )

  const projets = data?.projets || []

  return (
    <ProtectedRoute>
      <DashboardPageLayout
        header={{
          title: "MES PROJETS",
          description: "Liste de vos projets en cours et terminés",
          icon: ProcessorIcon,
        }}
      >
        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Mes Projets</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-sm text-muted-foreground">Chargement...</p>
              ) : !user?.neq ? (
                <p className="text-sm text-muted-foreground">
                  Vous devez être connecté en tant que prestataire pour voir vos projets.
                </p>
              ) : projets.length === 0 ? (
                <p className="text-sm text-muted-foreground">Aucun projet trouvé</p>
              ) : (
                <div className="space-y-2">
                  {projets.map((projet) => (
                    <div key={projet.id} className="p-3 border rounded">
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <h3 className="font-medium">Projet #{projet.id}</h3>
                          <p className="text-sm text-muted-foreground mt-1">
                            {projet.description || "Aucune description"}
                          </p>
                          <p className="text-xs text-muted-foreground mt-1">
                            Localisation: {projet.localisation || "Non spécifiée"}
                          </p>
                          <div className="flex gap-2 mt-2">
                            <Badge variant={projet.statut === "En cours" ? "default" : "secondary"}>
                              {projet.statut}
                            </Badge>
                            {projet.cout && (
                              <Badge variant="outline">
                                Coût: {projet.cout.toLocaleString("fr-FR", { style: "currency", currency: "CAD" })}
                              </Badge>
                            )}
                          </div>
                          {projet.dateDebut && (
                            <p className="text-xs text-muted-foreground mt-1">
                              Début prévu: {new Date(projet.dateDebut).toLocaleDateString("fr-FR")}
                            </p>
                          )}
                          {projet.dateFin && (
                            <p className="text-xs text-muted-foreground">
                              Fin prévue: {new Date(projet.dateFin).toLocaleDateString("fr-FR")}
                            </p>
                          )}
                        </div>
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
