"use client"

import { useState } from "react"
import { ProtectedRoute } from "@/components/ProtectedRoute"
import DashboardPageLayout from "@/components/dashboard/layout"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Switch } from "@/components/ui/switch"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import GearIcon from "@/components/icons/gear"
import { useApiQuery, useApiMutation } from "@/hooks/use-api-query"
import * as api from "@/lib/api"
import type { Preferences } from "@/lib/api"
import { toast } from "sonner"
import { useAuth } from "@/contexts/AuthContext"

// Types de notifications disponibles
const NOTIFICATION_TYPES = [
  { id: "nouveau_probleme", label: "Nouveau problème signalé" },
  { id: "candidature_acceptee", label: "Candidature acceptée" },
  { id: "candidature_refusee", label: "Candidature refusée" },
  { id: "projet_approuve", label: "Projet approuvé" },
  { id: "projet_termine", label: "Projet terminé" },
  { id: "priorite_modifiee", label: "Priorité modifiée" },
]

export default function SettingsPage() {
  const [preferences, setPreferences] = useState<Preferences>({
    notificationsEmail: true,
    notificationsQuartier: true,
    notificationsType: [],
  })
  const { user, userType } = useAuth()

  // Récupérer l'email depuis le contexte d'authentification
  const userEmail = user?.email || (userType === "PRESTATAIRE" ? user?.neq : null)

  const { data, isLoading: loading } = useApiQuery(
    ["preferences", userEmail],
    () => {
      if (!userEmail) throw new Error("Email requis")
      return api.getResidentPreferences(userEmail)
    },
    {
      enabled: !!userEmail,
      onSuccess: (response) => {
        if (response.success) {
          setPreferences(response.preferences)
        }
      },
    }
  )

  const mutation = useApiMutation(
    (prefs: Preferences) => {
      if (!userEmail) throw new Error("Email requis")
      return api.updateResidentPreferences(userEmail, prefs)
    },
    {
      onSuccess: () => {
        toast.success("Préférences mises à jour avec succès")
      },
    }
  )

  const handleSave = async () => {
    if (!userEmail) {
      toast.error("Vous devez être connecté pour modifier vos préférences")
      return
    }

    try {
      await mutation.mutateAsync(preferences)
    } catch (error) {
      // Error already handled by useApiMutation (toast)
    }
  }

  const saving = mutation.isPending

  const handleToggleEmail = (checked: boolean) => {
    setPreferences((prev) => ({ ...prev, notificationsEmail: checked }))
  }

  const handleToggleQuartier = (checked: boolean) => {
    setPreferences((prev) => ({ ...prev, notificationsQuartier: checked }))
  }

  const handleToggleNotificationType = (typeId: string, checked: boolean) => {
    setPreferences((prev) => {
      const types = prev.notificationsType || []
      if (checked) {
        return { ...prev, notificationsType: [...types, typeId] }
      } else {
        return { ...prev, notificationsType: types.filter((t) => t !== typeId) }
      }
    })
  }

  return (
    <ProtectedRoute>
      <DashboardPageLayout
        header={{
          title: "PARAMÈTRES",
          description: "Gérez vos préférences de notification",
          icon: GearIcon,
        }}
      >
        <div className="space-y-6">
          {/* Préférences de Notification */}
          <Card>
            <CardHeader>
              <CardTitle>Préférences de Notification</CardTitle>
              <CardDescription>
                Configurez comment et quand vous souhaitez recevoir des notifications
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {!userEmail ? (
                <div className="text-sm text-muted-foreground text-center py-4">
                  Vous devez être connecté pour accéder aux préférences
                </div>
              ) : loading ? (
                <div className="text-sm text-muted-foreground">Chargement...</div>
              ) : (
                <>
                  {/* Notifications par Email */}
                  <div className="flex items-center justify-between">
                    <div className="space-y-0.5">
                      <Label htmlFor="email-notifications" className="text-base font-semibold">
                        Notifications par Email
                      </Label>
                      <p className="text-sm text-muted-foreground">
                        Recevoir des notifications par courriel
                      </p>
                    </div>
                    <Switch
                      id="email-notifications"
                      checked={preferences.notificationsEmail}
                      onCheckedChange={handleToggleEmail}
                    />
                  </div>

                  <div className="border-t border-border" />

                  {/* Notifications par Quartier */}
                  <div className="flex items-center justify-between">
                    <div className="space-y-0.5">
                      <Label htmlFor="quartier-notifications" className="text-base font-semibold">
                        Notifications par Quartier
                      </Label>
                      <p className="text-sm text-muted-foreground">
                        Recevoir des notifications pour les problèmes dans votre quartier
                      </p>
                    </div>
                    <Switch
                      id="quartier-notifications"
                      checked={preferences.notificationsQuartier}
                      onCheckedChange={handleToggleQuartier}
                    />
                  </div>

                  <div className="border-t border-border" />

                  {/* Types de Notifications */}
                  <div className="space-y-4">
                    <div className="space-y-0.5">
                      <Label className="text-base font-semibold">
                        Types de Notifications
                      </Label>
                      <p className="text-sm text-muted-foreground">
                        Sélectionnez les types de notifications que vous souhaitez recevoir
                      </p>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {NOTIFICATION_TYPES.map((type) => (
                        <div
                          key={type.id}
                          className="flex items-center space-x-2 p-3 border border-border rounded-sm hover:bg-secondary/30 transition-colors"
                        >
                          <Checkbox
                            id={type.id}
                            checked={preferences.notificationsType?.includes(type.id) || false}
                            onCheckedChange={(checked) =>
                              handleToggleNotificationType(type.id, checked as boolean)
                            }
                          />
                          <Label
                            htmlFor={type.id}
                            className="text-sm font-normal cursor-pointer flex-1"
                          >
                            {type.label}
                          </Label>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Bouton de sauvegarde */}
                  <div className="flex justify-end pt-4 border-t border-border">
                    <Button onClick={handleSave} disabled={saving}>
                      {saving ? "Enregistrement..." : "Enregistrer les préférences"}
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </DashboardPageLayout>
    </ProtectedRoute>
  )
}
