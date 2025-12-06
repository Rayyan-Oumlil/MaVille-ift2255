"use client"

import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import Notifications from "@/components/dashboard/notifications"
import EmailIcon from "@/components/icons/email"

export default function NotificationsPage() {
  return (
    <ProtectedRoute>
      <DashboardPageLayout
      header={{
        title: "NOTIFICATIONS",
        description: "Toutes vos notifications",
        icon: EmailIcon,
      }}
    >
      <Notifications />
    </DashboardPageLayout>
    </ProtectedRoute>
  )
}
