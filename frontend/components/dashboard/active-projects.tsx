"use client"

import { useMemo } from "react"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import type { Travail } from "@/lib/api"

const statusColors: Record<string, string> = {
  "En cours": "bg-blue-500/20 text-blue-400 border-blue-500/50",
  "Approuvé": "bg-orange-500/20 text-orange-400 border-orange-500/50",
  "En attente": "bg-yellow-500/20 text-yellow-400 border-yellow-500/50",
  "Terminé": "bg-blue-500/20 text-blue-400 border-blue-500/50",
  "Suspendu": "bg-gray-500/20 text-gray-400 border-gray-500/50",
  "Annulé": "bg-red-500/20 text-red-400 border-red-500/50",
}

export default function ActiveProjects() {
  const { data, isLoading: loading } = useApiQuery(
    ["travaux", "active"],
    () => api.getResidentsTravaux({ page: 0, size: 5 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const projects = useMemo(() => {
    return (data?.data || []).filter(
      (p) => p.statut === "En cours" || p.statut === "Approuvé" || p.statut === "En attente"
    )
  }, [data])

  const formatDates = (dateDebut?: string, dateFin?: string) => {
    if (!dateDebut && !dateFin) return "N/D"
    const start = dateDebut ? new Date(dateDebut).toLocaleDateString("fr-FR", { month: "2-digit", day: "2-digit" }) : ""
    const end = dateFin ? new Date(dateFin).toLocaleDateString("fr-FR", { month: "2-digit", day: "2-digit" }) : ""
    return start && end ? `${start} - ${end}` : start || end || "N/D"
  }

  if (loading) {
    return (
      <div className="bg-card border border-border rounded-sm p-6">
        <h2 className="text-lg font-bold text-foreground uppercase mb-4">PROJETS ACTIFS</h2>
        <div className="text-muted-foreground">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="bg-card border border-border rounded-sm p-6">
      <h2 className="text-lg font-bold text-foreground uppercase mb-4">PROJETS ACTIFS</h2>

      <div className="space-y-3">
        {projects.length === 0 ? (
          <div className="text-muted-foreground text-sm">Aucun projet actif</div>
        ) : (
          projects.map((project) => (
            <div
              key={project.id}
              className="p-3 bg-secondary/30 border border-border/50 rounded-sm hover:border-primary/50 transition-colors"
            >
              <div className="flex items-start justify-between mb-2">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-primary">#{project.id.replace("MAVILLE-", "")}</span>
                    <span className="text-sm text-foreground">{project.titre || project.description || "Projet"}</span>
                  </div>
                </div>
                <div
                  className={`text-xs font-bold px-3 py-1 rounded-full border ${
                    statusColors[project.statut] || statusColors["En attente"]
                  }`}
                >
                  {project.statut}
                </div>
              </div>
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>{project.quartier || project.lieu}</span>
                <span>{formatDates(project.date_debut, project.date_fin)}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
