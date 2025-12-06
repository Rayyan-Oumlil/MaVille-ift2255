"use client"

import { useState, useMemo } from "react"
import { useApiQuery, useApiMutation } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import { toast } from "sonner"
import type { Candidature } from "@/lib/api"

export default function PendingApplications() {
  const [processing, setProcessing] = useState<number | null>(null)

  const { data, isLoading: loading } = useApiQuery(
    ["candidatures", "pending"],
    () => api.getStpmCandidatures({ page: 0, size: 10 }),
    {
      staleTime: 30 * 1000,
      refetchInterval: 30000, // Refresh every 30 seconds
    }
  )

  const applications = useMemo(() => {
    return (data?.data || []).filter((app) => app.statut === "Soumise")
  }, [data])

  const mutation = useApiMutation(
    ({ id, accepter }: { id: number; accepter: boolean }) =>
      api.validerCandidature(id, accepter),
    {
      onSuccess: () => {
        toast.success("Candidature traitée avec succès")
      },
    }
  )

  const handleAction = async (id: number, accepter: boolean) => {
    try {
      setProcessing(id)
      await mutation.mutateAsync({ id, accepter })
    } catch (error) {
      // Error already handled by useApiMutation (toast)
    } finally {
      setProcessing(null)
    }
  }

  if (loading) {
    return (
      <div className="bg-card border border-border rounded-sm p-6">
        <h2 className="text-lg font-bold text-foreground uppercase mb-4">CANDIDATURES EN ATTENTE</h2>
        <div className="text-muted-foreground">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="bg-card border border-border rounded-sm p-6">
      <h2 className="text-lg font-bold text-foreground uppercase mb-4">CANDIDATURES EN ATTENTE</h2>

      <div className="space-y-3">
        {applications.length === 0 ? (
          <div className="text-muted-foreground text-sm">Aucune candidature en attente</div>
        ) : (
          applications.map((app) => (
            <div
              key={app.id}
              className="flex items-center justify-between p-3 bg-secondary/30 border border-border/50 rounded-sm hover:border-primary/50 transition-colors"
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-sm font-bold text-primary">#{app.id}</span>
                  <span className="text-sm text-foreground truncate">{app.prestataire}</span>
                </div>
                {app.cout && (
                  <span className="text-xs text-muted-foreground">
                    Coût : ${app.cout.toLocaleString()}
                  </span>
                )}
              </div>

              <div className="flex items-center gap-3">
                {app.cout && (
                  <span className="text-sm font-bold text-blue-400">${app.cout.toLocaleString()}</span>
                )}
                <div className="flex gap-2">
                  <button
                    onClick={() => handleAction(app.id, true)}
                    disabled={processing === app.id}
                    className="px-3 py-1 text-xs font-bold text-white bg-blue-500/20 border border-blue-500/50 hover:bg-blue-500/30 transition-colors rounded-sm uppercase disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {processing === app.id ? "..." : "Accepter"}
                  </button>
                  <button
                    onClick={() => handleAction(app.id, false)}
                    disabled={processing === app.id}
                    className="px-3 py-1 text-xs font-bold text-white bg-red-500/20 border border-red-500/50 hover:bg-red-500/30 transition-colors rounded-sm uppercase disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {processing === app.id ? "..." : "Refuser"}
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
