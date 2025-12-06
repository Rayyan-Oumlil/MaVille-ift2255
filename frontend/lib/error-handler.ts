/**
 * Gestion centralisée des erreurs avec Sentry
 */

export function reportError(error: Error, context?: Record<string, any>) {
  // En production, envoyer à Sentry
  if (typeof window !== "undefined" && process.env.NEXT_PUBLIC_SENTRY_DSN) {
    // @ts-ignore - Sentry sera importé dynamiquement si disponible
    if (window.Sentry) {
      window.Sentry.captureException(error, {
        contexts: {
          custom: context || {},
        },
      })
    }
  }

  // Toujours logger en console pour le développement
  console.error("Error reported:", error, context)
}

export function reportMessage(message: string, level: "info" | "warning" | "error" = "info", context?: Record<string, any>) {
  if (typeof window !== "undefined" && process.env.NEXT_PUBLIC_SENTRY_DSN) {
    // @ts-ignore
    if (window.Sentry) {
      window.Sentry.captureMessage(message, {
        level,
        contexts: {
          custom: context || {},
        },
      })
    }
  }

  console[level]("Message reported:", message, context)
}
