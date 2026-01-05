"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import ProcessorIcon from "@/components/icons/proccesor"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

export default function PrestatairesPage() {
  const { data, isLoading: loading } = useApiQuery(
    ["problemes", "prestataires"],
    () => api.getPrestatairesProblemes({ page: 0, size: 20 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const problemes = data?.data || []

  return (
    <ProtectedRoute>
      <DashboardPageLayout
      header={{
        title: "PRESTATAIRES",
        description: "Problèmes disponibles pour les prestataires",
        icon: ProcessorIcon,
      }}
    >
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Problèmes Disponibles</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-sm text-muted-foreground">Chargement...</p>
            ) : problemes.length === 0 ? (
              <p className="text-sm text-muted-foreground">Aucun problème disponible</p>
            ) : (
              <div className="space-y-2">
                {problemes.map((probleme) => (
                  <div key={probleme.id} className="p-3 sm:p-4 border rounded">
                    <div className="flex flex-col sm:flex-row justify-between items-start gap-2">
                      <div className="flex-1 min-w-0">
                        <h3 className="font-medium text-sm sm:text-base break-words">#{probleme.id} - {probleme.lieu}</h3>
                        <p className="text-xs sm:text-sm text-muted-foreground mt-1 break-words">{probleme.description}</p>
                        <div className="flex flex-wrap gap-2 mt-2">
                          <Badge variant="outline" className="text-xs">{probleme.type}</Badge>
                          <Badge variant={probleme.priorite === "Élevée" ? "destructive" : "secondary"} className="text-xs">
                            {probleme.priorite}
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
