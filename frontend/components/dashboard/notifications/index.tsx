"use client";

import React, { useState, useMemo } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Bullet } from "@/components/ui/bullet";
import NotificationItem from "./notification-item";
import type { Notification as DashboardNotification } from "@/types/dashboard";
import { useApiQuery, useApiMutation } from "@/hooks/use-api-query";
import { useAuth } from "@/contexts/AuthContext";
import * as api from "@/lib/api";
import type { Notification as ApiNotification } from "@/lib/api";
import { AnimatePresence, motion } from "framer-motion";
import { useQueryClient } from "@tanstack/react-query";

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
  const { user, userType } = useAuth();
  const queryClient = useQueryClient();

  // Determine which API to use based on user type
  const getNotificationsFn = useMemo(() => {
    if (userType === "RESIDENT" && user?.email) {
      return () => api.getResidentNotifications(user.email!);
    } else if (userType === "PRESTATAIRE" && user?.neq) {
      return () => api.getPrestataireNotifications(user.neq!);
    } else {
      return () => api.getStpmNotifications();
    }
  }, [userType, user]);

  const { data, isLoading: loading } = useApiQuery(
    ["notifications", userType, user?.email || user?.neq || "stpm"],
    getNotificationsFn,
    {
      enabled: initialNotifications.length === 0 && !!user,
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

  // Mutation to clear all notifications
  const clearAllMutation = useApiMutation(
    async () => {
      if (userType === "RESIDENT" && user?.email) {
        return api.clearAllResidentNotifications(user.email);
      } else if (userType === "PRESTATAIRE" && user?.neq) {
        return api.clearAllPrestataireNotifications(user.neq);
      } else {
        return api.clearAllStpmNotifications();
      }
    },
    {
      onSuccess: () => {
        // Invalidate and refetch notifications
        queryClient.invalidateQueries({ queryKey: ["notifications"] });
      },
    }
  );

  const markAsRead = (id: string) => {
    // TODO: Implémenter l'appel API pour marquer une notification individuelle comme lue
    // Pour l'instant, cette fonction est vide car l'endpoint backend existe mais doit être appelé
  };

  const deleteNotification = (id: string) => {
    // TODO: Implémenter l'appel API pour supprimer une notification individuelle
    // Pour l'instant, cette fonction est vide car l'endpoint backend existe mais doit être appelé
  };

  const clearAll = () => {
    clearAllMutation.mutate();
  };

  return (
    <Card className="h-full">
      <CardHeader className="flex items-center justify-between pl-3 pr-1">
        <CardTitle className="flex items-center gap-2.5 text-sm font-medium uppercase text-white">
          {unreadCount > 0 ? <Badge className="text-white bg-white/20">{unreadCount}</Badge> : <Bullet />}
          Notifications
        </CardTitle>
        {notifications.length > 0 && (
          <Button
            className="opacity-50 hover:opacity-100 uppercase text-white"
            size="sm"
            variant="ghost"
            onClick={clearAll}
          >
            Tout effacer
          </Button>
        )}
      </CardHeader>

      <CardContent className="!bg-accent p-1.5 overflow-hidden [&_*]:text-white">
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
                <p className="text-sm text-white/70">
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
                  className="w-full text-white"
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
