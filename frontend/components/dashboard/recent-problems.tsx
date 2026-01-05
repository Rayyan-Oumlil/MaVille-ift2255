"use client"

import { useMemo, useState } from "react"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import type { Probleme } from "@/lib/api"

const priorityColors = {
  "Urgente": "bg-red-500/20 text-red-400 border-red-500/50",
  "Haute": "bg-orange-500/20 text-orange-400 border-orange-500/50",
  "Moyenne": "bg-yellow-500/20 text-yellow-400 border-yellow-500/50",
  "Basse": "bg-blue-500/20 text-blue-400 border-blue-500/50",
  "Élevée": "bg-red-500/20 text-red-400 border-red-500/50",
  "Faible": "bg-blue-500/20 text-blue-400 border-blue-500/50",
}

type SortField = "date" | "priorite" | "lieu"
type SortOrder = "asc" | "desc"

export default function RecentProblems() {
  const [searchQuery, setSearchQuery] = useState("")
  const [priorityFilter, setPriorityFilter] = useState<string>("all")
  const [sortField, setSortField] = useState<SortField>("date")
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc")

  const { data, isLoading: loading, error } = useApiQuery(
    ["problemes", "recent"],
    () => api.getStpmProblemes({ page: 0, size: 100 }),
    {
      staleTime: 30 * 1000, // 30 secondes
      refetchInterval: 60000, // Rafraîchir toutes les 60 secondes
      retry: 2, // Retry 2 fois en cas d'erreur
    }
  )

  const allProblems = data?.data || []

  // Count problems from last 24 hours as "new"
  const newCount = useMemo(() => {
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)
    return allProblems.filter((p) => {
      if (!p.date) return false
      const problemDate = new Date(p.date)
      return problemDate > yesterday
    }).length
  }, [allProblems])

  // Filter and sort problems
  const filteredProblems = useMemo(() => {
    let filtered = [...allProblems]

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      filtered = filtered.filter(
        (p) =>
          p.lieu.toLowerCase().includes(query) ||
          p.description.toLowerCase().includes(query) ||
          p.type.toLowerCase().includes(query)
      )
    }

    // Priority filter
    if (priorityFilter !== "all") {
      filtered = filtered.filter((p) => p.priorite === priorityFilter)
    }

    // Sort
    filtered.sort((a, b) => {
      let comparison = 0
      switch (sortField) {
        case "date":
          comparison = new Date(a.date).getTime() - new Date(b.date).getTime()
          break
        case "priorite":
          const priorityOrder = ["Urgente", "Haute", "Moyenne", "Basse", "Élevée", "Faible"]
          comparison =
            priorityOrder.indexOf(a.priorite) - priorityOrder.indexOf(b.priorite)
          break
        case "lieu":
          comparison = a.lieu.localeCompare(b.lieu)
          break
      }
      return sortOrder === "asc" ? comparison : -comparison
    })

    return filtered.slice(0, 10) // Show top 10
  }, [allProblems, searchQuery, priorityFilter, sortField, sortOrder])

  const uniquePriorities = useMemo(() => {
    return Array.from(new Set(allProblems.map((p) => p.priorite))).sort()
  }, [allProblems])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc")
    } else {
      setSortField(field)
      setSortOrder("desc")
    }
  }

  if (loading) {
    return (
      <div className="bg-card border border-border rounded-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold text-foreground uppercase">PROBLÈMES RÉCENTS</h2>
        </div>
        <div className="text-muted-foreground">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="bg-card border border-border rounded-sm p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-bold text-foreground uppercase">PROBLÈMES RÉCENTS</h2>
        {newCount > 0 && (
          <span className="text-xs bg-orange-500/20 text-orange-400 border border-orange-500/50 px-3 py-1 rounded-full font-bold">
            {newCount} NOUVEAUX
          </span>
        )}
      </div>

      {/* Search and Filters */}
      <div className="mb-4 space-y-3">
        {/* Search */}
        <input
          type="text"
          placeholder="Rechercher par lieu, description, type..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full px-3 py-2 bg-secondary/30 border border-border rounded-sm text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
        />

        {/* Filters Row */}
        <div className="flex flex-wrap gap-2 items-center">
          {/* Priority Filter */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="px-3 py-1.5 bg-secondary/30 border border-border rounded-sm text-xs text-foreground focus:outline-none focus:border-primary/50"
          >
            <option value="all">Toutes les priorités</option>
            {uniquePriorities.map((priority) => (
              <option key={priority} value={priority}>
                {priority}
              </option>
            ))}
          </select>

          {/* Sort Buttons */}
          <div className="flex flex-wrap gap-1 sm:ml-auto">
            <button
              onClick={() => handleSort("date")}
              className={`px-2 py-1 text-xs font-bold uppercase border rounded-sm transition-colors ${
                sortField === "date"
                  ? "bg-primary/20 text-primary border-primary/50"
                  : "bg-secondary/30 text-muted-foreground border-border hover:border-primary/30"
              }`}
            >
              Date {sortField === "date" && (sortOrder === "asc" ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSort("priorite")}
              className={`px-2 py-1 text-xs font-bold uppercase border rounded-sm transition-colors ${
                sortField === "priorite"
                  ? "bg-primary/20 text-primary border-primary/50"
                  : "bg-secondary/30 text-muted-foreground border-border hover:border-primary/30"
              }`}
            >
              Priorité {sortField === "priorite" && (sortOrder === "asc" ? "↑" : "↓")}
            </button>
            <button
              onClick={() => handleSort("lieu")}
              className={`px-2 py-1 text-xs font-bold uppercase border rounded-sm transition-colors ${
                sortField === "lieu"
                  ? "bg-primary/20 text-primary border-primary/50"
                  : "bg-secondary/30 text-muted-foreground border-border hover:border-primary/30"
              }`}
            >
              Lieu {sortField === "lieu" && (sortOrder === "asc" ? "↑" : "↓")}
            </button>
          </div>
        </div>
      </div>

      {/* Results count */}
      {filteredProblems.length !== allProblems.length && (
        <div className="mb-3 text-xs text-muted-foreground">
          {filteredProblems.length} résultat{filteredProblems.length > 1 ? "s" : ""} sur {allProblems.length}
        </div>
      )}

      {/* Problems List */}
      <div className="space-y-3 max-h-[300px] sm:max-h-[400px] overflow-y-auto">
        {error ? (
          <div className="text-red-400 text-sm text-center py-4">
            Erreur lors du chargement des problèmes. Veuillez réessayer.
          </div>
        ) : filteredProblems.length === 0 && !loading ? (
          <div className="text-muted-foreground text-sm text-center py-4">
            {allProblems.length === 0 
              ? "Aucun problème disponible pour le moment"
              : "Aucun problème trouvé avec les filtres sélectionnés"}
          </div>
        ) : loading ? (
          <div className="text-muted-foreground text-sm text-center py-4">
            Chargement des problèmes...
          </div>
        ) : (
          filteredProblems.map((problem) => (
            <div
              key={problem.id}
              className="flex items-center gap-4 p-3 bg-secondary/30 border border-border/50 rounded-sm hover:border-primary/50 transition-colors"
            >
              {/* Orange indicator bar */}
              <div className="w-1 h-8 bg-orange-400"></div>

              {/* Problem info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-sm font-bold text-primary">#{problem.id}</span>
                  <span className="text-sm text-foreground truncate">{problem.lieu}</span>
                </div>
                <span className="text-xs text-muted-foreground">
                  {new Date(problem.date).toLocaleDateString("fr-FR", {
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric",
                  })}
                </span>
              </div>

              {/* Priority badge */}
              <div
                className={`text-xs font-bold px-3 py-1 rounded-full border whitespace-nowrap ${
                  priorityColors[problem.priorite as keyof typeof priorityColors] || priorityColors["Moyenne"]
                }`}
              >
                {problem.priorite}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
