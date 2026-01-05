"use client"

import { usePathname } from "next/navigation"
import { SidebarProvider } from "@/components/ui/sidebar"
import { MobileHeader } from "@/components/dashboard/mobile-header"
import { DashboardSidebar } from "@/components/dashboard/sidebar"
import Widget from "@/components/dashboard/widget"
import Notifications from "@/components/dashboard/notifications"

export function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()
  const isLoginPage = pathname === "/login"

  // Sur la page de login, afficher seulement le contenu
  if (isLoginPage) {
    return <>{children}</>
  }

  // Sur les autres pages, afficher le layout complet avec sidebar
  return (
    <SidebarProvider>
      {/* Mobile Header - only visible on mobile */}
      <MobileHeader />

      {/* Desktop Layout */}
      <div className="w-full grid grid-cols-1 lg:grid-cols-12 gap-gap lg:px-sides">
        <div className="hidden lg:block col-span-2 top-0 relative">
          <DashboardSidebar />
        </div>
        <div className="col-span-1 lg:col-span-7">{children}</div>
        <div className="col-span-3 hidden lg:block">
          <div className="space-y-gap py-sides min-h-screen max-h-screen sticky top-0 overflow-clip">
            <Widget />
            <Notifications />
          </div>
        </div>
      </div>
    </SidebarProvider>
  )
}
