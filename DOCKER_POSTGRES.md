# Configuration PostgreSQL avec Docker

## 🎯 Ce qui a été fait

L'application MaVille utilise maintenant **PostgreSQL** comme base de données via Docker, au lieu de H2 en mémoire.

### Modifications apportées

1. **Configuration Docker** : `docker-compose.yml` créé pour lancer PostgreSQL
2. **Configuration Spring Boot** : `application.properties` configuré pour PostgreSQL par défaut
3. **Entité Preferences** : Ajoutée pour gérer les préférences utilisateur
4. **Page Settings** : Interface complète pour modifier les préférences de notification

## 🚀 Démarrage rapide

### 1. Lancer PostgreSQL

```powershell
docker run -d --name maville-postgres -e POSTGRES_DB=maville -e POSTGRES_USER=maville_user -e POSTGRES_PASSWORD=maville_password -p 5432:5432 -v postgres_data:/var/lib/postgresql/data postgres:15-alpine
```

**OU avec docker-compose :**
```powershell
docker-compose up -d postgres
```

### 2. Vérifier que PostgreSQL est actif

```powershell
docker ps
```

Vous devriez voir `maville-postgres` avec le statut "Up".

### 3. Lancer l'application

```powershell
mvn spring-boot:run
```

L'application se connectera automatiquement à PostgreSQL.

## 📋 Configuration

### Identifiants par défaut
- **Base de données** : `maville`
- **Utilisateur** : `maville_user`
- **Mot de passe** : `maville_password`
- **Port** : `5432`

### Fichiers de configuration

- `docker-compose.yml` : Configuration Docker pour PostgreSQL
- `src/main/resources/application.properties` : Configuration Spring Boot (PostgreSQL activé)
- `src/main/resources/application-docker.properties` : Profil alternatif (non utilisé actuellement)

## 🔧 Commandes utiles

### Vérifier le statut
```powershell
docker ps
```

### Voir les logs PostgreSQL
```powershell
docker logs maville-postgres
```

### Arrêter PostgreSQL
```powershell
docker stop maville-postgres
```

### Redémarrer PostgreSQL
```powershell
docker start maville-postgres
```

### Accéder à PostgreSQL en ligne de commande
```powershell
docker exec -it maville-postgres psql -U maville_user -d maville
```

### Supprimer complètement (données incluses)
```powershell
docker stop maville-postgres
docker rm maville-postgres
docker volume rm postgres_data
```

## ⚠️ Important

**Le conteneur Docker PostgreSQL doit rester en cours d'exécution** pendant que l'application Spring Boot tourne. Si vous arrêtez le conteneur, l'application ne pourra plus se connecter à la base de données.

## 🔄 Retour à H2 (si nécessaire)

Si vous voulez revenir à H2 en mémoire, modifiez `src/main/resources/application.properties` :
- Commentez les lignes PostgreSQL
- Décommentez les lignes H2

## 📊 Données

Les données sont stockées dans un volume Docker nommé `postgres_data`. Elles sont **persistantes** même si vous arrêtez le conteneur.

Pour repartir de zéro (supprimer toutes les données) :
```powershell
docker volume rm postgres_data
```
