"use client"

import { useApiQuery } from "./use-api-query"
import * as api from "@/lib/api"
import { useMemo } from "react"

export function useDashboardStats() {
  // Fetch all data in parallel
  const { data: problemsData, isLoading: problemsLoading } = useApiQuery(
    ["problemes", "all"],
    () => api.getStpmProblemes({ page: 0, size: 1000 }),
    {
      staleTime: 30 * 1000, // 30 secondes
    }
  )

  const { data: applicationsData, isLoading: applicationsLoading } = useApiQuery(
    ["candidatures", "all"],
    () => api.getStpmCandidatures({ page: 0, size: 1000 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const { data: travauxData, isLoading: travauxLoading } = useApiQuery(
    ["travaux", "all"],
    () => api.getResidentsTravaux({ page: 0, size: 1000 }),
    {
      staleTime: 30 * 1000,
    }
  )

  const { data: notificationsData, isLoading: notificationsLoading } = useApiQuery(
    ["notifications", "stpm"],
    () => api.getStpmNotifications(),
    {
      staleTime: 30 * 1000,
    }
  )

  const stats = useMemo(() => {
    const now = new Date()
    const thisMonthStart = new Date(now.getFullYear(), now.getMonth(), 1)

    const problems = problemsData?.data || []
    const applications = applicationsData?.data || []
    const travaux = travauxData?.data || []

    // Count problems from this month
    const problemsThisMonth = problems.filter((p) => {
      if (!p.date) return false
      const problemDate = new Date(p.date)
      return problemDate >= thisMonthStart
    })

    // Count pending applications
    const pendingApplications = applications.filter(
      (app) => app.statut === "Soumise"
    )

    // Count active projects
    const activeProjects = travaux.filter(
      (p) => p.statut === "En cours" || p.statut === "Approuvé" || p.statut === "En attente"
    )

    // Count completed projects this month
    const completedThisMonth = travaux.filter((p) => {
      if (p.statut !== "Terminé") return false
      if (!p.date_fin) return false
      const endDate = new Date(p.date_fin)
      return endDate >= thisMonthStart
    })

    return {
      problems: problemsThisMonth.length,
      projects: activeProjects.length,
      applications: pendingApplications.length,
      completed: completedThisMonth.length,
    }
  }, [problemsData, applicationsData, travauxData])

  const loading = problemsLoading || applicationsLoading || travauxLoading || notificationsLoading

  return {
    stats,
    loading,
    lastUpdated: new Date(),
  }
}
