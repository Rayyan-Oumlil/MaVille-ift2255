"use client";

import React, { useMemo } from "react";
import { Badge } from "@/components/ui/badge";
import { Bullet } from "@/components/ui/bullet";
import { AnimatePresence, motion, PanInfo } from "motion/react";
import NotificationItem from "./notification-item";
import type { Notification } from "@/types/dashboard";
import { SheetClose, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { useIsV0 } from "@/lib/v0-context";
import { useApiQuery } from "@/hooks/use-api-query";
import * as api from "@/lib/api";
import type { Notification as ApiNotification } from "@/lib/api";

// Convert API notification to dashboard notification format
const convertNotification = (apiNotif: ApiNotification): Notification => {
  const getType = (type: string): "info" | "warning" | "success" | "error" => {
    if (type.includes("SUCCESS") || type.includes("APPROUVEE") || type.includes("CREATED")) {
      return "success";
    }
    if (type.includes("WARNING") || type.includes("PROBLEME") || type.includes("ALERT")) {
      return "warning";
    }
    if (type.includes("ERROR") || type.includes("REJETEE")) {
      return "error";
    }
    return "info";
  };

  const getNotificationTitle = (type: string, message: string): string => {
    if (type.includes("NOUVEAU_PROBLEME")) return "NOUVEAU PROBLÈME";
    if (type.includes("NOUVEAU_PROJET")) return "NOUVEAU PROJET";
    if (type.includes("CANDIDATURE")) return "CANDIDATURE";
    if (type.includes("UPDATE")) return "MISE À JOUR SYSTÈME";
    const words = message.split(" ").slice(0, 3);
    return words.join(" ").toUpperCase();
  };

  return {
    id: apiNotif.id || `notif-${Date.now()}`,
    title: getNotificationTitle(apiNotif.type, apiNotif.message),
    message: apiNotif.message,
    timestamp: apiNotif.date,
    type: getType(apiNotif.type),
    read: apiNotif.lu,
    priority: "medium" as const,
  };
};

interface SwipeableWrapperProps {
  children: React.ReactNode;
  onDelete: () => void;
}

function SwipeableWrapper({ children, onDelete }: SwipeableWrapperProps) {
  const handleDragEnd = (event: Event, info: PanInfo) => {
    // Delete if swiped left more than 120px OR with sufficient velocity
    const shouldDelete =
      info.offset.x < -120 || (info.offset.x < -50 && info.velocity.x < -500);

    if (shouldDelete) {
      // Immediate deletion - let parent handle exit animation
      onDelete();
    }
  };

  return (
    <motion.div
      drag="x"
      dragConstraints={{ left: -200, right: 0 }}
      dragSnapToOrigin
      dragElastic={0.2}
      onDragEnd={handleDragEnd}
      whileDrag={{
        scale: 0.98,
        boxShadow: "0 5px 15px rgba(0,0,0,0.1)",
      }}
      className="relative cursor-grab active:cursor-grabbing"
      style={{ touchAction: "pan-y" }} // Allow vertical scrolling, horizontal dragging
    >
      {children}
    </motion.div>
  );
}

export default function MobileNotifications() {
  const isV0 = useIsV0();

  const { data, isLoading: loading } = useApiQuery(
    ["notifications", "stpm", "mobile"],
    () => api.getStpmNotifications(),
    {
      staleTime: 30 * 1000,
      refetchInterval: 30000, // Refresh every 30 seconds
    }
  );

  const notifications = useMemo(() => {
    if (!data?.notifications) return [];
    return data.notifications
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .map(convertNotification);
  }, [data]);

  const unreadCount = useMemo(() => {
    return notifications.filter((n) => !n.read).length;
  }, [notifications]);

  const markAsRead = (id: string) => {
    // Note: Cette fonction modifie localement l'état pour l'UI
    // En production, on devrait appeler une API pour marquer comme lu
  };

  const deleteNotification = (id: string) => {
    // Note: Cette fonction supprime localement pour l'UI
    // En production, on devrait appeler une API pour supprimer
  };

  return (
    <div className="h-full flex flex-col">
      {/* Accessibility Title */}
      <SheetHeader className="sr-only">
        <SheetTitle>Notifications</SheetTitle>
      </SheetHeader>

      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-border">
        <div className="flex items-center gap-2.5">
          {unreadCount > 0 ? <Badge>{unreadCount}</Badge> : <Bullet />}
          <h2 className="text-sm font-medium uppercase">Notifications</h2>
        </div>
        <SheetClose>
          <Badge
            variant="secondary"
            className="uppercase text-muted-foreground"
          >
            Fermer
          </Badge>
        </SheetClose>
      </div>

      {/* Notifications List */}
      <div className="flex-1 overflow-y-auto overflow-x-clip p-2 space-y-2 bg-muted">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <p className="text-sm text-muted-foreground">Chargement des notifications...</p>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex items-center justify-center h-32">
            <p className="text-sm text-muted-foreground">Aucune notification</p>
          </div>
        ) : (
          <AnimatePresence mode={isV0 ? "wait" : "popLayout"}>
            {notifications.map((notification) => (
              <motion.div
                key={notification.id}
                layout
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.8, x: -300 }}
                transition={{
                  layout: { duration: 0.3, ease: "easeOut" },
                  opacity: { duration: 0.2 },
                  scale: { duration: 0.2 },
                  x: { duration: 0.2 },
                }}
              >
                <SwipeableWrapper
                  onDelete={() => deleteNotification(notification.id)}
                >
                  <NotificationItem
                    notification={notification}
                    onMarkAsRead={markAsRead}
                    onDelete={deleteNotification}
                  />
                </SwipeableWrapper>
              </motion.div>
            ))}
          </AnimatePresence>
        )}
      </div>

      {/* Swipe Hint */}
      {notifications.length > 0 && (
        <div className="p-4 border-t border-border">
          <p className="text-xs text-muted-foreground text-center">
            Glisser vers la gauche pour supprimer les notifications
          </p>
        </div>
      )}
    </div>
  );
}
