"use client"

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
// Removed MonkeyIcon import - using text logo instead
import MobileNotifications from "@/components/dashboard/notifications/mobile-notifications";
import { getStpmNotifications } from "@/lib/api";
import BellIcon from "@/components/icons/bell";

export function MobileHeader() {
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    async function fetchNotifications() {
      try {
        const response = await getStpmNotifications();
        const unread = response.notifications.filter((n) => !n.lu).length;
        setUnreadCount(unread);
      } catch (error) {
        console.error("Erreur lors de la récupération des notifications:", error);
      }
    }
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="lg:hidden h-header-mobile sticky top-0 z-50 bg-background/95 backdrop-blur-sm border-b border-border">
      <div className="flex items-center justify-between px-4 py-3">
        {/* Left: Sidebar Menu */}
        <SidebarTrigger />

        {/* Center: MaVille Logo */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2">
            <div className="h-8 px-3 bg-primary rounded flex items-center justify-center">
              <span className="text-sm font-display text-primary-foreground">MAVILLE</span>
            </div>
          </div>
        </div>

        <Sheet>
          {/* Right: Notifications Menu */}
          <SheetTrigger asChild>
            <Button variant="secondary" size="icon" className="relative">
              {unreadCount > 0 && (
                <Badge className="absolute border-2 border-background -top-1 -left-2 h-5 w-5 text-xs p-0 flex items-center justify-center">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </Badge>
              )}
              <BellIcon className="size-4" />
            </Button>
          </SheetTrigger>

          {/* Notifications Sheet */}
          <SheetContent
            closeButton={false}
            side="right"
            className="w-[80%] max-w-md p-0"
          >
            <MobileNotifications />
          </SheetContent>
        </Sheet>
      </div>
    </div>
  );
}
