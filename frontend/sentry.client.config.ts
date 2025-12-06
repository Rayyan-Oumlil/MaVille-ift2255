// Sentry est optionnel - ne s'active que si NEXT_PUBLIC_SENTRY_DSN est configuré
// Pour utiliser Sentry:
// 1. Créez un compte gratuit sur https://sentry.io
// 2. Créez un projet pour votre application
// 3. Copiez le DSN et ajoutez-le dans .env.local: NEXT_PUBLIC_SENTRY_DSN=votre-dsn

if (process.env.NEXT_PUBLIC_SENTRY_DSN) {
  try {
    // @ts-ignore - Import dynamique pour éviter les erreurs si le package n'est pas installé
    const Sentry = require("@sentry/nextjs")
    
    Sentry.init({
      dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
      tracesSampleRate: process.env.NODE_ENV === "production" ? 0.1 : 1.0,
      debug: false,
      replaysOnErrorSampleRate: 1.0,
      replaysSessionSampleRate: 0.1,
      integrations: [
        Sentry.replayIntegration({
          maskAllText: true,
          blockAllMedia: true,
        }),
      ],
    })
  } catch (error) {
    // Sentry n'est pas installé ou configuré - ce n'est pas grave
    console.log("Sentry non configuré - monitoring désactivé")
  }
}
