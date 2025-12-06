# 🔍 Guide d'Intégration Sentry - Monitoring d'Erreurs

## 📋 Vue d'ensemble

Ce guide explique comment intégrer **Sentry** dans l'application MaVille pour le monitoring d'erreurs en production. 

**⚠️ Important :** Sentry est **100% optionnel**. L'application fonctionne parfaitement sans Sentry. Ce guide est destiné aux développeurs qui souhaitent activer le monitoring d'erreurs.

---

## 🎯 Qu'est-ce que Sentry ?

Sentry est un service de monitoring d'erreurs qui permet de :
- ✅ Capturer automatiquement les erreurs JavaScript
- ✅ Recevoir des alertes en temps réel
- ✅ Voir les stack traces complètes
- ✅ Analyser les erreurs par utilisateur, navigateur, etc.
- ✅ Améliorer la qualité de l'application

---

## 🚀 Installation et Configuration

### Étape 1 : Créer un compte Sentry (Gratuit)

1. **Visitez** [https://sentry.io](https://sentry.io)
2. **Cliquez** sur "Sign Up" pour créer un compte gratuit
3. **Vérifiez** votre email si nécessaire

> 💡 **Note :** Le plan gratuit de Sentry offre 5,000 événements/mois, ce qui est largement suffisant pour un projet académique ou personnel.

---

### Étape 2 : Créer un projet Sentry

1. **Connectez-vous** à votre compte Sentry
2. **Cliquez** sur "Create Project" ou "New Project"
3. **Sélectionnez** "Next.js" comme plateforme
4. **Donnez** un nom à votre projet (ex: "maville-frontend")
5. **Cliquez** sur "Create Project"

---

### Étape 3 : Récupérer le DSN (Data Source Name)

Après avoir créé le projet, Sentry vous affichera une page de configuration avec votre **DSN**.

Le DSN ressemble à ceci :
```
https://xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@xxxxx.ingest.sentry.io/xxxxx
```

**⚠️ Important :** Copiez ce DSN, vous en aurez besoin à l'étape suivante.

---

### Étape 4 : Configurer dans l'application

#### Option A : Fichier `.env.local` (Recommandé pour le développement)

1. **Créez** un fichier `.env.local` à la racine du dossier `frontend/` (s'il n'existe pas déjà)
2. **Ajoutez** la ligne suivante :

```env
NEXT_PUBLIC_SENTRY_DSN=https://votre-dsn-ici@xxxxx.ingest.sentry.io/xxxxx
```

**Exemple complet :**
```env
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:7000/api
NEXT_PUBLIC_WS_URL=ws://localhost:7000/ws

# Sentry Configuration
NEXT_PUBLIC_SENTRY_DSN=https://abc123@o123456.ingest.sentry.io/123456
```

#### Option B : Variables d'environnement système (Recommandé pour la production)

Sur votre serveur de production, configurez la variable d'environnement :

```bash
export NEXT_PUBLIC_SENTRY_DSN=https://votre-dsn-ici@xxxxx.ingest.sentry.io/xxxxx
```

Ou dans votre plateforme de déploiement (Vercel, Netlify, etc.), ajoutez la variable dans les paramètres du projet.

---

### Étape 5 : Vérifier l'installation

1. **Redémarrez** le serveur de développement :
   ```bash
   npm run dev
   ```

2. **Ouvrez** l'application dans votre navigateur

3. **Vérifiez** la console du navigateur - vous devriez voir :
   ```
   Sentry initialized
   ```

4. **Testez** une erreur (optionnel) :
   - Ouvrez la console du navigateur
   - Tapez : `throw new Error("Test Sentry")`
   - Allez sur votre dashboard Sentry
   - Vous devriez voir l'erreur apparaître dans quelques secondes

---

## ✅ Vérification de l'Intégration

### Comment savoir si Sentry fonctionne ?

1. **Dashboard Sentry** : Connectez-vous à [sentry.io](https://sentry.io) et allez dans votre projet
2. **Erreurs capturées** : Les erreurs apparaîtront automatiquement dans le dashboard
3. **Console du navigateur** : En développement, vous verrez des logs de Sentry

### Test rapide

Pour tester rapidement si Sentry fonctionne, ajoutez temporairement ce code dans un composant :

```tsx
// Test Sentry (à retirer après vérification)
useEffect(() => {
  if (process.env.NEXT_PUBLIC_SENTRY_DSN) {
    console.log("✅ Sentry est configuré et actif")
  } else {
    console.log("ℹ️ Sentry n'est pas configuré (optionnel)")
  }
}, [])
```

---

## 🔧 Configuration Avancée (Optionnel)

### Personnaliser la configuration Sentry

Les fichiers de configuration Sentry se trouvent à la racine du projet `frontend/` :

- `sentry.client.config.ts` - Configuration côté client (navigateur)
- `sentry.server.config.ts` - Configuration côté serveur (Next.js)
- `sentry.edge.config.ts` - Configuration pour Edge Runtime

### Exemple de personnalisation

Pour modifier le taux d'échantillonnage ou ajouter des tags personnalisés, modifiez `sentry.client.config.ts` :

```typescript
Sentry.init({
  dsn: process.env.NEXT_PUBLIC_SENTRY_DSN,
  
  // Réduire le taux d'échantillonnage en production (10% des erreurs)
  tracesSampleRate: process.env.NODE_ENV === "production" ? 0.1 : 1.0,
  
  // Ajouter des tags personnalisés
  environment: process.env.NODE_ENV,
  
  // Filtrer certaines erreurs
  beforeSend(event, hint) {
    // Ne pas envoyer les erreurs de développement
    if (process.env.NODE_ENV === "development") {
      return null
    }
    return event
  },
})
```

---

## 🚫 Désactiver Sentry

Si vous ne voulez **pas** utiliser Sentry :

1. **Ne configurez pas** la variable `NEXT_PUBLIC_SENTRY_DSN`
2. **Ou supprimez-la** de votre fichier `.env.local`
3. **Redémarrez** l'application

L'application fonctionnera normalement, les erreurs seront simplement loggées dans la console du navigateur.

---

## 📊 Utilisation de Sentry

### Dashboard Sentry

Une fois configuré, vous pouvez :

1. **Voir les erreurs** en temps réel sur [sentry.io](https://sentry.io)
2. **Recevoir des alertes** par email (configurable)
3. **Analyser les erreurs** par :
   - Navigateur
   - Version de l'application
   - Utilisateur
   - Date/heure
   - Stack trace complète

### Exemples d'erreurs capturées automatiquement

- ❌ Erreurs JavaScript non gérées
- ❌ Erreurs d'API (si configuré)
- ❌ Erreurs de rendu React
- ❌ Erreurs de navigation Next.js

---

## 💰 Coûts

### Plan Gratuit

- ✅ **5,000 événements/mois** gratuits
- ✅ **1 projet** gratuit
- ✅ **7 jours** de rétention des données
- ✅ **Support communautaire**

### Pour un projet académique

Le plan gratuit est **largement suffisant** pour :
- Développement
- Tests
- Petites applications en production

---

## 🆘 Dépannage

### Sentry ne capture pas les erreurs

1. **Vérifiez** que `NEXT_PUBLIC_SENTRY_DSN` est bien configuré
2. **Vérifiez** que le DSN est correct (pas d'espaces, URL complète)
3. **Redémarrez** le serveur après avoir ajouté la variable
4. **Vérifiez** la console du navigateur pour des erreurs de connexion

### Erreurs dans la console

Si vous voyez des erreurs liées à Sentry dans la console :

1. **Vérifiez** que le package `@sentry/nextjs` est installé :
   ```bash
   npm install @sentry/nextjs
   ```

2. **Vérifiez** que le DSN est valide sur [sentry.io](https://sentry.io)

### Sentry ralentit l'application

En développement, Sentry peut sembler ralentir l'application. C'est normal. En production, l'impact est minimal grâce à l'échantillonnage.

---

## 📚 Ressources

- **Documentation Sentry** : [https://docs.sentry.io/platforms/javascript/guides/nextjs/](https://docs.sentry.io/platforms/javascript/guides/nextjs/)
- **Dashboard Sentry** : [https://sentry.io](https://sentry.io)
- **Support** : [https://forum.sentry.io](https://forum.sentry.io)

---

## ✅ Checklist d'Intégration

- [ ] Compte Sentry créé
- [ ] Projet Sentry créé (plateforme Next.js)
- [ ] DSN copié
- [ ] Variable `NEXT_PUBLIC_SENTRY_DSN` ajoutée dans `.env.local`
- [ ] Serveur redémarré
- [ ] Test d'erreur effectué
- [ ] Erreur visible dans le dashboard Sentry

---

## 📝 Notes Finales

- **Sentry est optionnel** : L'application fonctionne parfaitement sans
- **Gratuit pour les petits projets** : Le plan gratuit est suffisant
- **Facile à désactiver** : Supprimez simplement la variable d'environnement
- **Utile en production** : Aide à identifier et corriger les erreurs rapidement

**Besoin d'aide ?** Consultez la documentation officielle de Sentry ou créez une issue sur le dépôt du projet.
