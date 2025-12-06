"use client"

import { useMemo } from "react"
import { useApiQuery } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import type { Notification } from "@/lib/api"

const getTypeColor = (type: string): string => {
  if (type.includes("SUCCESS") || type.includes("APPROUVEE") || type.includes("CREATED")) {
    return "bg-blue-500"
  }
  if (type.includes("WARNING") || type.includes("PROBLEME") || type.includes("ALERT")) {
    return "bg-orange-500"
  }
  return "bg-blue-500"
}

const getNotificationTitle = (type: string, message: string): string => {
  if (type.includes("NOUVEAU_PROBLEME")) return "NOUVEAU PROBLÈME"
  if (type.includes("NOUVEAU_PROJET")) return "NOUVEAU PROJET"
  if (type.includes("CANDIDATURE")) return "CANDIDATURE"
  if (type.includes("UPDATE")) return "MISE À JOUR SYSTÈME"
  // Extract first few words from message as title
  const words = message.split(" ").slice(0, 3)
  return words.join(" ").toUpperCase()
}

export default function RecentNotifications() {
  const { data, isLoading: loading } = useApiQuery(
    ["notifications", "stpm", "recent"],
    () => api.getStpmNotifications(),
    {
      staleTime: 30 * 1000,
      refetchInterval: 30000, // Refresh every 30 seconds
    }
  )

  const notifications = useMemo(() => {
    if (!data?.notifications) return []
    return data.notifications
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .slice(0, 5)
  }, [data])

  const unreadCount = useMemo(() => {
    return data?.notifications?.filter((n) => !n.lu).length || 0
  }, [data])

  if (loading) {
    return (
      <div className="bg-card border border-border rounded-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-bold text-foreground uppercase">NOTIFICATIONS</h2>
        </div>
        <div className="text-muted-foreground">Chargement...</div>
      </div>
    )
  }

  return (
    <div className="bg-card border border-border rounded-sm p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-bold text-foreground uppercase">NOTIFICATIONS</h2>
        {unreadCount > 0 && (
          <span className="text-xs bg-blue-500/20 text-blue-400 border border-blue-500/50 px-3 py-1 rounded-full font-bold">
            ({unreadCount})
          </span>
        )}
      </div>

      <div className="space-y-2 mb-4">
        {notifications.length === 0 ? (
          <div className="text-muted-foreground text-sm">Aucune notification</div>
        ) : (
          notifications.map((notif, index) => (
            <div
              key={notif.id || index}
              className="flex items-start gap-3 p-3 bg-secondary/30 border border-border/50 rounded-sm hover:border-primary/50 transition-colors"
            >
              {/* Colored dot indicator */}
              <div
                className={`w-2 h-2 rounded-full flex-shrink-0 mt-1 ${getTypeColor(notif.type)}`}
              ></div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <div className="text-xs font-bold text-foreground uppercase">
                  {getNotificationTitle(notif.type, notif.message)}
                </div>
                <p className="text-xs text-muted-foreground mt-1">{notif.message}</p>
                <span className="text-xs text-muted-foreground/60 mt-2 block">
                  {new Date(notif.date).toLocaleDateString()}
                </span>
              </div>
            </div>
          ))
        )}
      </div>

      {notifications.length > 0 && (
        <button className="w-full py-2 text-xs font-bold text-primary uppercase border border-primary/50 hover:bg-primary/10 transition-colors rounded-sm">
          VOIR TOUT ({notifications.length})
        </button>
      )}
    </div>
  )
}
