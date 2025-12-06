"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { MessageSquare, Send } from "lucide-react"
import { format } from "date-fns"
import { cn } from "@/lib/utils"

export interface Comment {
  id: number
  message: string
  auteurEmail?: string
  auteurNeq?: string
  auteurNom?: string
  auteurType: "RESIDENT" | "PRESTATAIRE" | "STPM"
  dateCreation: string
}

interface CommentsSectionProps {
  comments: Comment[]
  onAddComment: (message: string) => Promise<void>
  currentUserEmail?: string
  currentUserNeq?: string
  currentUserType?: "RESIDENT" | "PRESTATAIRE" | "STPM"
  className?: string
  disabled?: boolean
}

export function CommentsSection({
  comments,
  onAddComment,
  currentUserEmail,
  currentUserNeq,
  currentUserType,
  className,
  disabled = false,
}: CommentsSectionProps) {
  const [newComment, setNewComment] = useState("")
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newComment.trim() || isSubmitting) return

    setIsSubmitting(true)
    try {
      await onAddComment(newComment.trim())
      setNewComment("")
    } catch (error) {
      console.error("Erreur lors de l'ajout du commentaire:", error)
    } finally {
      setIsSubmitting(false)
    }
  }

  const getAuthorInitials = (comment: Comment): string => {
    if (comment.auteurNom) {
      return comment.auteurNom
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2)
    }
    if (comment.auteurEmail) {
      return comment.auteurEmail[0].toUpperCase()
    }
    if (comment.auteurNeq) {
      return comment.auteurNeq.slice(0, 2).toUpperCase()
    }
    return "?"
  }

  const getAuthorName = (comment: Comment): string => {
    if (comment.auteurNom) return comment.auteurNom
    if (comment.auteurEmail) return comment.auteurEmail
    if (comment.auteurNeq) return `Prestataire ${comment.auteurNeq}`
    return "Utilisateur inconnu"
  }

  const getAuthorBadgeVariant = (type: Comment["auteurType"]) => {
    switch (type) {
      case "RESIDENT":
        return "default"
      case "PRESTATAIRE":
        return "secondary"
      case "STPM":
        return "outline"
      default:
        return "default"
    }
  }

  const getAuthorBadgeLabel = (type: Comment["auteurType"]) => {
    switch (type) {
      case "RESIDENT":
        return "Résident"
      case "PRESTATAIRE":
        return "Prestataire"
      case "STPM":
        return "STPM"
      default:
        return "Utilisateur"
    }
  }

  const canComment =
    !disabled &&
    (currentUserEmail || currentUserNeq) &&
    currentUserType

  return (
    <Card className={cn("w-full", className)}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MessageSquare className="h-5 w-5" />
          Commentaires ({comments.length})
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Formulaire d'ajout de commentaire */}
        {canComment && (
          <form onSubmit={handleSubmit} className="space-y-2">
            <Textarea
              placeholder="Ajouter un commentaire..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              disabled={isSubmitting || disabled}
              rows={3}
              className="resize-none"
            />
            <div className="flex justify-end">
              <Button
                type="submit"
                size="sm"
                disabled={!newComment.trim() || isSubmitting || disabled}
              >
                <Send className="h-4 w-4 mr-2" />
                {isSubmitting ? "Envoi..." : "Envoyer"}
              </Button>
            </div>
          </form>
        )}

        {/* Liste des commentaires */}
        {comments.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <MessageSquare className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>Aucun commentaire pour le moment</p>
            {!canComment && (
              <p className="text-xs mt-2">
                Connectez-vous pour ajouter un commentaire
              </p>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {comments.map((comment) => (
              <div
                key={comment.id}
                className="flex gap-3 p-3 rounded-lg border bg-card"
              >
                <Avatar className="h-8 w-8">
                  <AvatarFallback className="text-xs">
                    {getAuthorInitials(comment)}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">
                      {getAuthorName(comment)}
                    </span>
                    <Badge
                      variant={getAuthorBadgeVariant(comment.auteurType)}
                      className="text-xs"
                    >
                      {getAuthorBadgeLabel(comment.auteurType)}
                    </Badge>
                    <span className="text-xs text-muted-foreground ml-auto">
                      {format(new Date(comment.dateCreation), "PPp")}
                    </span>
                  </div>
                  <p className="text-sm text-foreground whitespace-pre-wrap">
                    {comment.message}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
