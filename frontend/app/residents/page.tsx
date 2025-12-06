"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import AtomIcon from "@/components/icons/atom"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

export default function ResidentsPage() {
  const { data, isLoading: loading } = useApiQuery(
    ["travaux", "residents"],
    () => api.getResidentsTravaux({ page: 0, size: 20 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const travaux = data?.data || []

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
            <CardTitle>Travaux en Cours</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-muted-foreground">Chargement...</p>
            ) : travaux.length === 0 ? (
              <p className="text-sm text-muted-foreground">Aucun travail en cours</p>
            ) : (
              <div className="space-y-2">
                {travaux.map((travail) => (
                  <div key={travail.id} className="p-3 border rounded">
                    <div className="flex justify-between items-start">
                      <div>
                        <h3 className="font-medium">{travail.titre || travail.id}</h3>
                        <p className="text-sm text-muted-foreground mt-1">{travail.description || "Aucune description"}</p>
                        <div className="flex gap-2 mt-2">
                          <Badge variant="outline">{travail.source}</Badge>
                          <Badge variant="secondary">{travail.quartier}</Badge>
                          <Badge variant={travail.statut === "En cours" ? "default" : "secondary"}>
                            {travail.statut}
                          </Badge>
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
