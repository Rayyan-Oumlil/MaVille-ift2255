"use client"

import { useEffect, useState } from "react"
import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import StatCard from "@/components/dashboard/stat-card"
import ActivityChart from "@/components/dashboard/activity-chart"
import RecentProblems from "@/components/dashboard/recent-problems"
import PendingApplications from "@/components/dashboard/pending-applications"
import ActiveProjects from "@/components/dashboard/active-projects"
import RecentNotifications from "@/components/dashboard/recent-notifications"
import BracketsIcon from "@/components/icons/brackets"
import { getStpmProblemes, getStpmCandidatures, getResidentsTravaux, getStpmNotifications } from "@/lib/api"

export default function DashboardOverview() {
  const [stats, setStats] = useState({
    problems: 0,
    projects: 0,
    applications: 0,
    completed: 0,
  })
  const [loading, setLoading] = useState(true)
  const [lastUpdated, setLastUpdated] = useState(new Date())

  useEffect(() => {
    async function fetchStats() {
      try {
        setLoading(true)
        
        // Fetch all data in parallel with better error handling
        const [problemsRes, applicationsRes, projectsRes, notificationsRes] = await Promise.all([
          getStpmProblemes({ page: 0, size: 1000 }).catch((err) => {
            console.warn("Erreur lors de la récupération des problèmes:", err)
            return { data: [], totalItems: 0, total: 0, pageSize: 0, size: 0 }
          }),
          getStpmCandidatures({ page: 0, size: 1000 }).catch((err) => {
            console.warn("Erreur lors de la récupération des candidatures:", err)
            return { data: [], totalItems: 0, total: 0, pageSize: 0, size: 0 }
          }),
          getResidentsTravaux({ page: 0, size: 1000 }).catch((err) => {
            console.warn("Erreur lors de la récupération des travaux:", err)
            return { data: [], totalItems: 0, total: 0, pageSize: 0, size: 0 }
          }),
          getStpmNotifications().catch((err) => {
            console.warn("Erreur lors de la récupération des notifications:", err)
            return { notifications: [], total: 0 }
          }),
        ])

        // Count problems from this month
        const now = new Date()
        const thisMonthStart = new Date(now.getFullYear(), now.getMonth(), 1)
        const problemsThisMonth = (problemsRes.data || []).filter((p) => {
          if (!p.date) return false
          const problemDate = new Date(p.date)
          return problemDate >= thisMonthStart
        })

        // Count pending applications
        const pendingApplications = (applicationsRes.data || []).filter(
          (app) => app.statut === "Soumise"
        )

        // Count active projects
        const activeProjects = (projectsRes.data || []).filter(
          (p) => p.statut === "En cours" || p.statut === "Approuvé" || p.statut === "En attente"
        )

        // Count completed projects this month
        const completedThisMonth = projectsRes.data.filter((p) => {
          if (p.statut !== "Terminé") return false
          if (!p.date_fin) return false
          const endDate = new Date(p.date_fin)
          return endDate >= thisMonthStart
        })

        setStats({
          problems: problemsThisMonth.length,
          projects: activeProjects.length,
          applications: pendingApplications.length,
          completed: completedThisMonth.length,
        })

        setLastUpdated(new Date())
      } catch (error) {
        console.error("Error fetching stats:", error)
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
    // Refresh every 60 seconds
    const interval = setInterval(fetchStats, 60000)
    return () => clearInterval(interval)
  }, [])

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString("fr-FR", {
      hour: "numeric",
      minute: "2-digit",
      hour12: false,
    })
  }

  return (
    <ProtectedRoute>
    <DashboardPageLayout
      header={{
        title: "VUE D'ENSEMBLE",
        description: `Dernière mise à jour ${formatTime(lastUpdated)}`,
        icon: BracketsIcon,
      }}
    >
      {/* Statistics Cards - 4 columns */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard
          label="PROBLÈMES SIGNALÉS"
          value={loading ? "..." : stats.problems.toString()}
          description="CE MOIS"
          trend="up"
          icon="📊"
        />
        <StatCard
          label="PROJETS EN COURS"
          value={loading ? "..." : stats.projects.toString()}
          description="ACTIFS"
          badge={stats.projects > 0 ? `${stats.projects} ACTIFS` : undefined}
          badgeType="info"
        />
        <StatCard
          label="CANDIDATURES EN ATTENTE"
          value={loading ? "..." : stats.applications.toString()}
          description="EN RÉVISION"
          badge={stats.applications > 0 ? `${stats.applications} NOUVELLES` : undefined}
          badgeType="warning"
        />
        <StatCard
          label="TRAVAUX TERMINÉS"
          value={loading ? "..." : stats.completed.toString()}
          description="CE MOIS"
          badge={stats.completed > 0 ? "CE MOIS" : undefined}
          badgeType="success"
        />
      </div>

      {/* Activity Chart */}
      <div className="mb-6">
        <ActivityChart />
      </div>

      {/* Bottom grid - Recent Problems and Pending Applications */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <RecentProblems />
        <PendingApplications />
      </div>

      {/* Bottom grid - Active Projects and Recent Notifications */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ActiveProjects />
        <RecentNotifications />
      </div>
    </DashboardPageLayout>
    </ProtectedRoute>
  )
}
