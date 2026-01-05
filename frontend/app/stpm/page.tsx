"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import CuteRobotIcon from "@/components/icons/cute-robot"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

export default function StpmPage() {
  const { data: problemesData, isLoading: problemesLoading } = useApiQuery(
    ["problemes", "stpm"],
    () => api.getStpmProblemes({ page: 0, size: 20 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const { data: candidaturesData, isLoading: candidaturesLoading } = useApiQuery(
    ["candidatures", "stpm"],
    () => api.getStpmCandidatures({ page: 0, size: 20 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const problemes = problemesData?.data || []
  const candidatures = candidaturesData?.data || []
  const loading = problemesLoading || candidaturesLoading

  return (
    <ProtectedRoute>
      <DashboardPageLayout
      header={{
        title: "STPM",
        description: "Gestion des problèmes et candidatures",
        icon: CuteRobotIcon,
      }}
    >
      <Tabs defaultValue="problemes" className="space-y-4">
        <TabsList>
          <TabsTrigger value="problemes">Problèmes</TabsTrigger>
          <TabsTrigger value="candidatures">Candidatures</TabsTrigger>
        </TabsList>

        <TabsContent value="problemes">
          <Card>
            <CardHeader>
              <CardTitle>Problèmes Signalés</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-sm text-muted-foreground">Chargement...</p>
              ) : problemes.length === 0 ? (
                <p className="text-sm text-muted-foreground">Aucun problème</p>
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
        </TabsContent>

        <TabsContent value="candidatures">
          <Card>
            <CardHeader>
              <CardTitle>Candidatures</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-sm text-muted-foreground">Chargement...</p>
              ) : candidatures.length === 0 ? (
                <p className="text-sm text-muted-foreground">Aucune candidature</p>
              ) : (
                <div className="space-y-2">
                  {candidatures.map((candidature) => (
                    <div key={candidature.id} className="p-3 sm:p-4 border rounded">
                      <div className="flex flex-col sm:flex-row justify-between items-start gap-2">
                        <div className="flex-1 min-w-0">
                          <h3 className="font-medium text-sm sm:text-base break-words">#{candidature.id} - {candidature.prestataire}</h3>
                          <p className="text-xs sm:text-sm text-muted-foreground mt-1 break-words">{candidature.description}</p>
                          <div className="flex flex-wrap gap-2 mt-2">
                            <Badge variant={candidature.statut === "Soumise" ? "warning" : "secondary"} className="text-xs">
                              {candidature.statut}
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
        </TabsContent>
      </Tabs>
    </DashboardPageLayout>
    </ProtectedRoute>
  )
}
