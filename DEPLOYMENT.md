# Guide de Déploiement - MaVille

Ce guide explique comment déployer l'application MaVille pour qu'elle soit accessible publiquement en tant que démo.

## 🚀 Options de Déploiement

### Option 1 : Déploiement avec Docker Compose (Recommandé pour démo locale)

Déploie tous les services (PostgreSQL, Backend, Frontend) en une seule commande.

#### Prérequis
- Docker et Docker Compose installés
- Ports 3000, 7000, 5432 disponibles

#### Étapes

1. **Cloner le repository**
```bash
git clone https://github.com/Rayyan-Oumlil/MaVille-ift2255.git
cd MaVille-ift2255
```

2. **Créer le fichier .env** (optionnel, les valeurs par défaut fonctionnent)
```bash
cp env.example .env
# Modifiez les valeurs si nécessaire
```

3. **Lancer tous les services**
```bash
docker-compose up -d
```

4. **Vérifier que tout fonctionne**
```bash
docker-compose ps
```

5. **Accéder à l'application**
- Frontend : http://localhost:3000
- Backend API : http://localhost:7000/api
- Swagger UI : http://localhost:7000/swagger-ui.html

#### Arrêter l'application
```bash
docker-compose down
```

#### Voir les logs
```bash
docker-compose logs -f
```

---

### Option 2 : Déploiement sur Vercel (Frontend) + Railway/Render (Backend)

#### Frontend sur Vercel

1. **Préparer le projet**
```bash
cd frontend
npm install
npm run build
```

2. **Déployer sur Vercel**
   - Allez sur [vercel.com](https://vercel.com)
   - Connectez votre compte GitHub
   - Importez le repository `MaVille-ift2255`
   - Configurez :
     - **Root Directory** : `frontend`
     - **Build Command** : `npm run build`
     - **Output Directory** : `.next`
     - **Install Command** : `npm install`

3. **Variables d'environnement sur Vercel**
   - Allez dans Settings > Environment Variables
   - Ajoutez :
     ```
     NEXT_PUBLIC_API_URL=https://votre-backend.railway.app/api
     NEXT_PUBLIC_WS_URL=wss://votre-backend.railway.app/ws
     ```

4. **Déployer**
   - Vercel déploie automatiquement à chaque push sur `main`

#### Backend sur Railway

1. **Créer un compte Railway**
   - Allez sur [railway.app](https://railway.app)
   - Connectez avec GitHub

2. **Créer un nouveau projet**
   - Cliquez sur "New Project"
   - Sélectionnez "Deploy from GitHub repo"
   - Choisissez `MaVille-ift2255`

3. **Ajouter PostgreSQL**
   - Cliquez sur "+ New" > "Database" > "PostgreSQL"
   - Railway créera automatiquement une base de données

4. **Déployer le Backend**
   - Cliquez sur "+ New" > "GitHub Repo"
   - Sélectionnez `MaVille-ift2255`
   - Railway détectera automatiquement le Dockerfile
   - Configurez les variables d'environnement :
     ```
     SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
     SPRING_DATASOURCE_USERNAME=${{Postgres.USERNAME}}
     SPRING_DATASOURCE_PASSWORD=${{Postgres.PASSWORD}}
     SERVER_PORT=7000
     SPRING_PROFILES_ACTIVE=production
     ```

5. **Obtenir l'URL du backend**
   - Une fois déployé, Railway vous donnera une URL comme `https://votre-app.railway.app`
   - Utilisez cette URL dans les variables d'environnement Vercel

#### Alternative : Backend sur Render

1. **Créer un compte Render**
   - Allez sur [render.com](https://render.com)
   - Connectez avec GitHub

2. **Créer un nouveau Web Service**
   - Cliquez sur "New" > "Web Service"
   - Connectez votre repository GitHub
   - Configurez :
     - **Name** : `maville-backend`
     - **Environment** : `Docker`
     - **Dockerfile Path** : `Dockerfile.backend`
     - **Build Command** : (laissé vide, Docker gère)
     - **Start Command** : (laissé vide, Docker gère)

3. **Ajouter PostgreSQL**
   - Cliquez sur "New" > "PostgreSQL"
   - Créez une nouvelle base de données

4. **Variables d'environnement**
   ```
   SPRING_DATASOURCE_URL=<URL de votre base PostgreSQL Render>
   SPRING_DATASOURCE_USERNAME=<username>
   SPRING_DATASOURCE_PASSWORD=<password>
   SERVER_PORT=7000
   SPRING_PROFILES_ACTIVE=production
   ```

5. **Déployer**
   - Render déploiera automatiquement

---

### Option 3 : Déploiement complet sur un VPS (DigitalOcean, AWS EC2, etc.)

#### Prérequis
- Serveur Ubuntu/Debian
- Docker et Docker Compose installés
- Domaine (optionnel mais recommandé)

#### Étapes

1. **Se connecter au serveur**
```bash
ssh user@your-server-ip
```

2. **Installer Docker et Docker Compose**
```bash
# Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

3. **Cloner le repository**
```bash
git clone https://github.com/Rayyan-Oumlil/MaVille-ift2255.git
cd MaVille-ift2255
```

4. **Configurer les variables d'environnement**
```bash
cp env.example .env
nano .env  # Modifiez les valeurs
```

5. **Lancer l'application**
```bash
docker-compose up -d
```

6. **Configurer Nginx (optionnel, pour HTTPS)**
```nginx
server {
    listen 80;
    server_name votre-domaine.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    location /api {
        proxy_pass http://localhost:7000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

7. **Configurer SSL avec Let's Encrypt**
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d votre-domaine.com
```

---

## 🔧 Configuration des Variables d'Environnement

### Backend
- `SPRING_DATASOURCE_URL` : URL de connexion PostgreSQL
- `SPRING_DATASOURCE_USERNAME` : Username PostgreSQL
- `SPRING_DATASOURCE_PASSWORD` : Password PostgreSQL
- `SERVER_PORT` : Port du serveur (défaut: 7000)
- `SPRING_PROFILES_ACTIVE` : Profile Spring (production/development)

### Frontend
- `NEXT_PUBLIC_API_URL` : URL de l'API backend (ex: `https://api.maville.com/api`)
- `NEXT_PUBLIC_WS_URL` : URL WebSocket (ex: `wss://api.maville.com/ws`)

---

## 📝 Notes Importantes

1. **Base de données** : Assurez-vous que PostgreSQL est accessible depuis le backend
2. **CORS** : Le backend est configuré pour accepter toutes les origines en développement. Pour la production, modifiez `application.properties`
3. **WebSocket** : Pour les WebSockets en production, utilisez `wss://` (WebSocket Secure)
4. **HTTPS** : Recommandé pour la production, surtout pour les WebSockets

---

## 🐛 Dépannage

### Le backend ne démarre pas
- Vérifiez que PostgreSQL est accessible
- Vérifiez les logs : `docker-compose logs backend`
- Vérifiez les variables d'environnement

### Le frontend ne se connecte pas au backend
- Vérifiez `NEXT_PUBLIC_API_URL` dans les variables d'environnement
- Vérifiez que le backend est accessible depuis l'extérieur
- Vérifiez les logs : `docker-compose logs frontend`

### Erreurs de connexion à la base de données
- Vérifiez que PostgreSQL est démarré : `docker-compose ps`
- Vérifiez les credentials dans `.env`
- Vérifiez les logs PostgreSQL : `docker-compose logs postgres`

---

## 🎉 Une fois déployé

Votre application sera accessible publiquement ! Partagez l'URL avec vos utilisateurs pour qu'ils puissent tester l'application.

**Comptes de test :**
- Résident : `marie@test.com` / `password123`
- Prestataire : `ABC123` / `password123`

---

## 📚 Ressources

- [Documentation Docker](https://docs.docker.com/)
- [Documentation Vercel](https://vercel.com/docs)
- [Documentation Railway](https://docs.railway.app/)
- [Documentation Render](https://render.com/docs)
