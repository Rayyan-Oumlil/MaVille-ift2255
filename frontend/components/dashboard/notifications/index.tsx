"use client";

import React, { useState, useMemo } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Bullet } from "@/components/ui/bullet";
import NotificationItem from "./notification-item";
import type { Notification as DashboardNotification } from "@/types/dashboard";
import { useApiQuery } from "@/hooks/use-api-query";
import * as api from "@/lib/api";
import type { Notification as ApiNotification } from "@/lib/api";
import { AnimatePresence, motion } from "framer-motion";

interface NotificationsProps {
  initialNotifications?: DashboardNotification[];
}

// Convert API notification to dashboard notification format
const convertNotification = (apiNotif: ApiNotification): DashboardNotification => {
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

const getNotificationTitle = (type: string, message: string): string => {
  if (type.includes("NOUVEAU_PROBLEME")) return "NOUVEAU PROBLÈME";
  if (type.includes("NOUVEAU_PROJET")) return "NOUVEAU PROJET";
  if (type.includes("CANDIDATURE")) return "CANDIDATURE";
  if (type.includes("UPDATE")) return "MISE À JOUR SYSTÈME";
  const words = message.split(" ").slice(0, 3);
  return words.join(" ").toUpperCase();
};

export default function Notifications({
  initialNotifications = [],
}: NotificationsProps) {
  const [showAll, setShowAll] = useState(false);

  const { data, isLoading: loading } = useApiQuery(
    ["notifications", "stpm", "all"],
    () => api.getStpmNotifications(),
    {
      enabled: initialNotifications.length === 0,
      staleTime: 30 * 1000,
      refetchInterval: 30000, // Refresh every 30 seconds
    }
  );

  const notifications = useMemo(() => {
    if (initialNotifications.length > 0) {
      return initialNotifications;
    }
    if (!data?.notifications) return [];
    return data.notifications
      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
      .map(convertNotification);
  }, [data, initialNotifications]);

  const unreadCount = notifications.filter((n) => !n.read).length;
  const displayedNotifications = showAll
    ? notifications
    : notifications.slice(0, 3);

  const markAsRead = (id: string) => {
    setNotifications((prev) =>
      prev.map((notif) => (notif.id === id ? { ...notif, read: true } : notif))
    );
  };

  const deleteNotification = (id: string) => {
    setNotifications((prev) => prev.filter((notif) => notif.id !== id));
  };

  const clearAll = () => {
    setNotifications([]);
  };

  return (
    <Card className="h-full">
      <CardHeader className="flex items-center justify-between pl-3 pr-1">
        <CardTitle className="flex items-center gap-2.5 text-sm font-medium uppercase text-black">
          {unreadCount > 0 ? <Badge className="text-black">{unreadCount}</Badge> : <Bullet />}
          Notifications
        </CardTitle>
        {notifications.length > 0 && (
          <Button
            className="opacity-50 hover:opacity-100 uppercase"
            size="sm"
            variant="ghost"
            onClick={clearAll}
          >
            Tout effacer
          </Button>
        )}
      </CardHeader>

      <CardContent className="!bg-accent p-1.5 overflow-hidden [&_*]:text-black">
        <div className="space-y-2">
          <AnimatePresence initial={false} mode="popLayout">
            {displayedNotifications.map((notification) => (
              <motion.div
                layout
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
                key={notification.id}
              >
                <NotificationItem
                  notification={notification}
                  onMarkAsRead={markAsRead}
                  onDelete={deleteNotification}
                />
              </motion.div>
            ))}

            {notifications.length === 0 && (
              <div className="text-center py-8">
                <p className="text-sm text-black/70">
                  Aucune notification
                </p>
              </div>
            )}

            {notifications.length > 3 && (
              <motion.div
                layout
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 20 }}
                transition={{ duration: 0.3, ease: "easeOut" }}
                className="w-full"
              >
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setShowAll(!showAll)}
                  className="w-full"
                >
                  {showAll ? "Voir moins" : `Voir tout (${notifications.length})`}
                </Button>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </CardContent>
    </Card>
  );
}
