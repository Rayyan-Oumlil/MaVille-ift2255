# 🚀 Guide de Déploiement sur Vercel - Étape par Étape

Ce guide vous explique comment déployer votre application MaVille sur Vercel (frontend) et Railway (backend).

## 📋 Vue d'ensemble

- **Frontend (Next.js)** → Vercel (gratuit)
- **Backend (Spring Boot)** → Railway (gratuit)
- **Base de données (PostgreSQL)** → Railway (gratuit)

---

## Étape 1 : Déployer le Backend sur Railway

### 1.1 Créer un compte Railway

1. Allez sur [railway.app](https://railway.app)
2. Cliquez sur **"Start a New Project"** ou **"Login"**
3. Connectez-vous avec votre compte **GitHub**

### 1.2 Créer un nouveau projet

1. Cliquez sur **"+ New Project"**
2. Sélectionnez **"Deploy from GitHub repo"**
3. Choisissez le repository **`MaVille-ift2255`**
4. Railway va détecter automatiquement le Dockerfile

### 1.3 Ajouter PostgreSQL

1. Dans votre projet Railway, cliquez sur **"+ New"**
2. Sélectionnez **"Database"** > **"Add PostgreSQL"**
3. Railway créera automatiquement une base de données PostgreSQL
4. **Notez** les informations de connexion (vous en aurez besoin)

### 1.4 Configurer les variables d'environnement

1. Cliquez sur votre service backend (celui avec le Dockerfile)
2. Allez dans l'onglet **"Variables"**
3. Ajoutez ces variables d'environnement :

```
SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
SPRING_DATASOURCE_USERNAME=${{Postgres.USERNAME}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.PASSWORD}}
SERVER_PORT=7000
SPRING_PROFILES_ACTIVE=production
```

**Note :** `${{Postgres.DATABASE_URL}}` fait référence automatiquement à la base de données PostgreSQL que vous venez de créer.

### 1.5 Obtenir l'URL du backend

1. Une fois le déploiement terminé, Railway vous donnera une URL
2. Cliquez sur votre service backend
3. Allez dans l'onglet **"Settings"**
4. Sous **"Networking"**, vous verrez une URL comme : `https://votre-app.railway.app`
5. **Copiez cette URL** - vous en aurez besoin pour Vercel

---

## Étape 2 : Déployer le Frontend sur Vercel

### 2.1 Créer un compte Vercel

1. Allez sur [vercel.com](https://vercel.com)
2. Cliquez sur **"Sign Up"**
3. Connectez-vous avec votre compte **GitHub**

### 2.2 Importer le projet

1. Cliquez sur **"Add New..."** > **"Project"**
2. Sélectionnez le repository **`MaVille-ift2255`**
3. Vercel détectera automatiquement que c'est un projet Next.js

### 2.3 Configurer le projet

Dans la page de configuration :

1. **Framework Preset** : Next.js (détecté automatiquement)
2. **Root Directory** : Cliquez sur **"Edit"** et entrez : `frontend`
3. **Build Command** : `npm run build` (ou laissé par défaut)
4. **Output Directory** : `.next` (ou laissé par défaut)
5. **Install Command** : `npm install` (ou laissé par défaut)

### 2.4 Configurer les variables d'environnement

**IMPORTANT :** Avant de déployer, ajoutez les variables d'environnement :

1. Dans la section **"Environment Variables"**, cliquez sur **"Add"**
2. Ajoutez ces variables :

```
NEXT_PUBLIC_API_URL=https://VOTRE-URL-RAILWAY.railway.app/api
NEXT_PUBLIC_WS_URL=wss://VOTRE-URL-RAILWAY.railway.app/ws
```

**Remplacez `VOTRE-URL-RAILWAY`** par l'URL que vous avez copiée depuis Railway à l'étape 1.5.

**Exemple :**
```
NEXT_PUBLIC_API_URL=https://maville-backend-production.up.railway.app/api
NEXT_PUBLIC_WS_URL=wss://maville-backend-production.up.railway.app/ws
```

### 2.5 Déployer

1. Cliquez sur **"Deploy"**
2. Vercel va :
   - Installer les dépendances
   - Builder l'application
   - Déployer sur leur CDN
3. Attendez 2-3 minutes pour que le déploiement se termine

### 2.6 Obtenir l'URL de votre application

1. Une fois le déploiement terminé, Vercel vous donnera une URL
2. L'URL sera quelque chose comme : `https://maville-ift2255.vercel.app`
3. **C'est l'URL de votre application publique !** 🎉

---

## Étape 3 : Vérifier que tout fonctionne

### 3.1 Tester le backend

1. Ouvrez votre navigateur
2. Allez sur : `https://VOTRE-URL-RAILWAY.railway.app/api/health`
3. Vous devriez voir une réponse JSON confirmant que l'API fonctionne

### 3.2 Tester le frontend

1. Ouvrez votre navigateur
2. Allez sur l'URL Vercel (ex: `https://maville-ift2255.vercel.app`)
3. Vous devriez voir la page de connexion
4. Testez avec les comptes :
   - Résident : `marie@test.com` / `password123`
   - Prestataire : `ABC123` / `password123`

---

## 🔧 Configuration CORS (si nécessaire)

Si vous avez des erreurs CORS, vous devrez modifier le backend. Mais normalement, la configuration actuelle devrait fonctionner.

---

## 📝 Notes importantes

1. **Railway** : Le plan gratuit inclut 500 heures de runtime par mois
2. **Vercel** : Le plan gratuit est généreux pour Next.js
3. **Base de données** : Railway PostgreSQL est gratuit jusqu'à 1GB
4. **WebSockets** : Utilisez `wss://` (WebSocket Secure) en production

---

## 🐛 Dépannage

### Le backend ne démarre pas sur Railway

- Vérifiez les logs dans Railway : Cliquez sur votre service > "Deployments" > "View Logs"
- Vérifiez que les variables d'environnement sont correctes
- Vérifiez que PostgreSQL est bien connecté

### Le frontend ne se connecte pas au backend

- Vérifiez que `NEXT_PUBLIC_API_URL` est correct dans Vercel
- Vérifiez que l'URL du backend est accessible (testez dans le navigateur)
- Vérifiez les logs Vercel : Allez dans votre projet > "Deployments" > Cliquez sur un déploiement > "View Function Logs"

### Erreurs 404 ou 500

- Vérifiez que le backend est bien démarré sur Railway
- Vérifiez que la base de données est initialisée (les données de test sont créées au premier démarrage)

---

## 🎉 C'est fait !

Votre application est maintenant accessible publiquement ! Partagez l'URL Vercel avec vos utilisateurs.

**URLs importantes :**
- Frontend : `https://votre-app.vercel.app`
- Backend API : `https://votre-backend.railway.app/api`
- Swagger UI : `https://votre-backend.railway.app/swagger-ui.html`

---

## 🔄 Mises à jour futures

Chaque fois que vous poussez du code sur GitHub :
- **Vercel** déploiera automatiquement le frontend
- **Railway** déploiera automatiquement le backend

Pas besoin de faire quoi que ce soit ! 🚀

