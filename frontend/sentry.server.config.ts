// Sentry est optionnel - ne s'active que si NEXT_PUBLIC_SENTRY_DSN est configuré
if (process.env.NEXT_PUBLIC_SENTRY_DSN) {
  try {
    const Sentry = require("@sentry/nextjs")
    Sentry.init({
      dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
      tracesSampleRate: process.env.NODE_ENV === "production" ? 0.1 : 1.0,
      debug: false,
    })
  } catch (error) {
    console.log("Sentry non configuré - monitoring désactivé")
  }
}
