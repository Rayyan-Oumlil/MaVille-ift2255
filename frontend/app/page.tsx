"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import StatCard from "@/components/dashboard/stat-card"
import ActivityChart from "@/components/dashboard/activity-chart"
import RecentProblems from "@/components/dashboard/recent-problems"
import PendingApplications from "@/components/dashboard/pending-applications"
import ActiveProjects from "@/components/dashboard/active-projects"
import RecentNotifications from "@/components/dashboard/recent-notifications"
import BracketsIcon from "@/components/icons/brackets"
import { useDashboardStats } from "@/hooks/use-dashboard-stats"

export default function DashboardOverview() {
  const { stats, loading, lastUpdated } = useDashboardStats()

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
          icon="chart"
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
