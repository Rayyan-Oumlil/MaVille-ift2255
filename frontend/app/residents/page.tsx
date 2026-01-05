"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import AtomIcon from "@/components/icons/atom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { useAuth } from "@/contexts/AuthContext"

export default function ResidentsPage() {
  const { user } = useAuth()
  const { data, isLoading: loading } = useApiQuery(
    ["travaux", "residents"],
    () => api.getResidentsTravaux({ page: 0, size: 20 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const travaux = data?.data || []

  // Filtrer les travaux pour ne garder que ceux qui ont une description valide
  const travauxValides = travaux.filter(
    (travail) => 
      travail.titre && 
      travail.titre !== travail.id && 
      travail.description && 
      travail.description !== "Aucune description" &&
      !travail.id.match(/^[a-f0-9]{24}$/i) // Exclure les IDs MongoDB longs
  )

  return (
    <ProtectedRoute>
      <DashboardPageLayout
      header={{
        title: "RÉSIDENTS",
        description: "Travaux en cours pour les résidents",
        icon: AtomIcon,
      }}
    >
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Mes Travaux</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-muted-foreground">Chargement...</p>
            ) : travauxValides.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Aucun travail en cours pour le moment
              </p>
            ) : (
              <div className="space-y-2">
                {travauxValides.map((travail) => (
                  <div key={travail.id} className="p-3 sm:p-4 border rounded">
                    <div className="flex flex-col sm:flex-row justify-between items-start gap-2">
                      <div className="flex-1 min-w-0">
                        <h3 className="font-medium text-sm sm:text-base break-words">{travail.titre}</h3>
                        {travail.description && (
                          <p className="text-xs sm:text-sm text-muted-foreground mt-1 break-words">
                            {travail.description}
                          </p>
                        )}
                        <div className="flex flex-wrap gap-2 mt-2">
                          {travail.source && (
                            <Badge variant="outline" className="text-xs">{travail.source}</Badge>
                          )}
                          {travail.quartier && (
                            <Badge variant="secondary" className="text-xs">{travail.quartier}</Badge>
                          )}
                          {travail.statut && (
                            <Badge variant={travail.statut === "En cours" ? "default" : "secondary"} className="text-xs">
                              {travail.statut}
                            </Badge>
                          )}
                        </div>
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

