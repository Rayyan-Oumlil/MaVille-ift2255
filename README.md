# MaVille - Application de Gestion des Travaux Publics

## Description du projet
**MaVille** est une application développée dans le cadre du cours IFT2255 (Génie Logiciel) qui vise à automatiser et améliorer la gestion des travaux publics à Montréal. L'application facilite la communication entre les résidents, les prestataires de services et le Service des Travaux Publics de Montréal (STPM).

### Architecture REST
Cette version implémente une architecture REST complète avec :
- **API REST** : Spring Boot REST API sur le port 7000
- **Base de données** : PostgreSQL (via Docker) ou H2 (en mémoire pour développement)
- **Frontend** : Next.js avec React
- **Intégration externe** : API de données ouvertes de Montréal
- **Authentification** : Système de connexion avec email/NEQ et mots de passe hashés (BCrypt)

### 🐳 Démarrage rapide avec Docker

Pour lancer l'application avec PostgreSQL via Docker :

**1. Lancer PostgreSQL :**
```powershell
docker run -d --name maville-postgres -e POSTGRES_DB=maville -e POSTGRES_USER=maville_user -e POSTGRES_PASSWORD=maville_password -p 5432:5432 -v postgres_data:/var/lib/postgresql/data postgres:15-alpine
```

**OU avec docker-compose :**
```powershell
docker-compose up -d postgres
```

**2. Lancer le backend :**
```powershell
mvn spring-boot:run
```

**3. Lancer le frontend :**
```powershell
cd frontend
npm install  # ou pnpm install
npm run dev  # ou pnpm dev
```

L'application backend est configurée pour utiliser PostgreSQL automatiquement.

**4. Accéder à l'application :**
- Frontend : `http://localhost:3000`
- Backend API : `http://localhost:7000/api`
- Swagger UI : `http://localhost:7000/swagger-ui.html`
- Page de connexion : `http://localhost:3000/login`

Voir [DOCKER_POSTGRES.md](DOCKER_POSTGRES.md) pour plus de détails sur PostgreSQL.

### Fonctionnalités principales

**🔐 Authentification :**
- ✅ Système de connexion avec email (résidents) ou NEQ (prestataires)
- ✅ Mots de passe hashés avec BCrypt pour la sécurité
- ✅ Menu adaptatif selon le type d'utilisateur (Résident, Prestataire, STPM)
- ✅ Protection des routes avec redirection automatique vers la page de connexion
- ✅ Session persistante dans le navigateur (localStorage)

**Pour les résidents :**
- Se connecter avec son email et mot de passe
- Signaler des problèmes routiers (avec abonnement automatique aux notifications)
- Consulter les travaux en cours ou à venir (3 prochains mois)
- Rechercher des travaux par quartier et/ou type
- Recevoir des notifications personnalisées lors de changements
- S'abonner aux notifications par quartier ou rue
- ✅ **Modifier ses préférences de notification** (page Settings complète)

**Pour les prestataires :**
- Se connecter avec son NEQ et mot de passe
- Consulter les problèmes disponibles avec filtres
- Soumettre des candidatures pour des projets
- Mettre à jour leurs projets en cours
- Voir uniquement leurs propres projets (filtrage par NEQ)
- S'abonner aux notifications de problèmes par quartier ou type

**Pour les agents STPM :**
- Se connecter avec son identifiant (authentification STPM)
- Valider ou refuser les candidatures
- Affecter des priorités aux problèmes
- Création automatique de projets lors de l'acceptation
- Recevoir des notifications pour nouveaux problèmes et projets

### Système de notifications avancé
- Abonnement automatique au quartier lors du signalement
- Notifications pour : nouveau projet, changement de statut, modification des dates
- Gestion des abonnements personnalisés (quartiers/rues)
- Notifications pour affectation de priorité aux problèmes
- Affichage du nombre de notifications dans les menus

## Structure du projet

```
MaVille-ift2255/
├── README.md                      # Documentation principale
├── pom.xml                        # Configuration Maven
│
├── docs/                          # 📚 Documentation organisée
│   ├── dev1/                      # Documentation Devoir 1
│   ├── dev2/                      # Documentation Devoir 2
│   ├── dev3/                      # Documentation Devoir 3
│   └── BACKEND_IMPROVEMENTS.md   # Améliorations backend
│
├── screenshots/                   # 🖼️ Captures d'écran
│
├── visual_paradigm/              # 📊 Diagrammes Visual Paradigm
│   ├── Dev2.vpp
│   └── SignalementProbleme.vpp
│
├── rapport/                       # 📄 Rapport HTML
│   ├── index.html
│   ├── images/                    # Diagrammes UML
│   └── jacoco/                    # Rapport de couverture
│
├── data/                          # 💾 Données persistantes (JSON)
│   ├── problemes.json
│   ├── projets.json
│   ├── candidatures.json
│   ├── residents.json
│   ├── prestataires.json
│   ├── notifications.json
│   └── abonnements.json
│
├── src/
│   ├── main/
│   │   ├── java/ca/udem/maville/
│   │   │   ├── Main.java                    # Point d'entrée
│   │   │   │
│   │   │   ├── api/                         # Architecture REST
│   │   │   │   ├── ApiServer.java          # Serveur API Javalin
│   │   │   │   ├── MontrealApiService.java # Intégration API Montréal
│   │   │   │   ├── dto/                    # Data Transfer Objects
│   │   │   │   │   ├── ErrorResponse.java
│   │   │   │   │   └── PaginatedResponse.java
│   │   │   │   └── util/                   # Utilitaires
│   │   │   │       └── ValidationUtil.java
│   │   │   │
│   │   │   ├── config/                     # Configuration
│   │   │   │   └── AppConfig.java
│   │   │   │
│   │   │   ├── modele/                     # Entités du domaine
│   │   │   │   ├── Abonnement.java
│   │   │   │   ├── Candidature.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── Prestataire.java
│   │   │   │   ├── Priorite.java
│   │   │   │   ├── Probleme.java
│   │   │   │   ├── Projet.java
│   │   │   │   ├── Resident.java
│   │   │   │   └── TypeTravaux.java
│   │   │   │
│   │   │   ├── service/                    # Logique métier
│   │   │   │   ├── GestionnaireProblemes.java
│   │   │   │   └── GestionnaireProjets.java
│   │   │   │
│   │   │   ├── storage/                    # Persistance JSON
│   │   │   │   └── JsonStorage.java
│   │   │   │
│   │   │   └── ui/                         # Interface console
│   │   │       ├── MenuPrincipal.java
│   │   │       ├── MenuResident.java
│   │   │       ├── MenuPrestataire.java
│   │   │       ├── MenuStpm.java
│   │   │       └── client/
│   │   │           └── HttpClient.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Configuration
│   │       ├── logback.xml                 # Configuration logging
│   │       └── public/                     # Interface web
│   │           ├── index.html
│   │           ├── resident.html
│   │           ├── prestataire.html
│   │           ├── stpm.html
│   │           ├── css/
│   │           └── js/
│   │
│   └── test/java/ca/udem/maville/         # Tests unitaires
│       ├── api/
│       ├── modele/
│       ├── service/
│       └── storage/
│
└── target/                         # Fichiers compilés (généré par Maven)
```

## Installation et exécution

### Prérequis
- **Java** : Version 17 ou supérieure
- **Maven** : Version 3.8 ou supérieure
- **Git** : Pour cloner le repository

### Option 1 : Utiliser le JAR précompilé
```bash
# Télécharger le JAR depuis la section Releases
# Puis exécuter :
java -jar target/maville-1.0-SNAPSHOT.jar
```

### Option 2 : Compiler depuis les sources
```bash
# Cloner le repository
git clone https://github.com/IFT-2255/ift2255-ma-ville-team9.git
cd ift2255-ma-ville-team9

# Compiler et créer le JAR
mvn clean package

# Exécuter
java -jar target/maville-1.0-SNAPSHOT.jar
```

### Démarrage de l'application
L'application démarre automatiquement :
1. **Backend** : Le serveur API sur http://localhost:7000/api
2. **Frontend** : L'interface web sur http://localhost:3000 (après `npm run dev` dans le dossier `frontend`)
3. **Page de connexion** : http://localhost:3000/login (redirection automatique si non authentifié)
4. **Swagger UI** : http://localhost:7000/swagger-ui.html (documentation interactive de l'API)

**Note :** L'interface console (MenuPrincipal.java) est disponible pour les tests, mais l'interface web est recommandée pour une meilleure expérience utilisateur.

## Navigation dans l'application

### Menu principal
1. **Résident** - Signaler problèmes, consulter travaux, gérer notifications
2. **Prestataire** - Consulter problèmes, soumettre candidatures, gérer projets
3. **Agent STPM** - Valider candidatures, gérer priorités, modifier priorités des problèmes

### 🔐 Authentification

L'application utilise un système d'authentification simple mais sécurisé :

- **Connexion** : Page `/login` accessible à tous
- **Mots de passe** : Hashés avec BCrypt côté backend
- **Session** : Stockée dans `localStorage` côté frontend
- **Protection des routes** : Toutes les pages (sauf `/login`) sont protégées
- **Menu adaptatif** : Le menu s'adapte automatiquement selon le type d'utilisateur connecté

**Types d'utilisateurs :**
- **RESIDENT** : Connexion avec email
- **PRESTATAIRE** : Connexion avec NEQ (Numéro d'Entreprise du Québec)
- **STPM** : Connexion avec identifiant STPM (à configurer)

**Données de test :**
- Résident : `marie@test.com` / `password123`
- Prestataire : `ABC123` / `password123`
- (Les mots de passe sont hashés lors de l'initialisation des données)

#### Utilisation pour les Agents STPM
Les agents STPM utilisent l'application pour :
- **Consulter les problèmes** signalés par les résidents (`/stpm`)
  - Voir tous les problèmes avec leurs détails (lieu, description, type, priorité)
  - Modifier la priorité d'un problème (FAIBLE, MOYENNE, ELEVEE) via l'API
- **Gérer les candidatures** (`/stpm/candidatures`)
  - Consulter toutes les candidatures soumises par les prestataires
  - **Accepter** une candidature → Crée automatiquement un projet et envoie des notifications
  - **Refuser** une candidature → Notifie le prestataire
- **Consulter les notifications** (`/notifications`)
  - Recevoir des notifications pour : nouveaux problèmes, nouvelles candidatures, changements de statut
- **Dashboard** (`/`)
  - Vue d'ensemble avec statistiques (problèmes, candidatures, projets)
  - Graphiques d'activité
  - Problèmes récents et candidatures en attente

### Données de test (DM3)
L'application est initialisée avec :
- **5 résidents** dont deux dans le même quartier, avec des préférences différentes
- **5 prestataires** avec des préférences différentes
- **5 problèmes routiers** déclarés dont au moins 2 avec une priorité affectée
- **5 projets de travaux**, avec au moins un accepté et en cours, et un terminé
- **Notifications déjà consultables** pour tous les profils

### Exemples d'utilisation

**Connexion d'un résident :**
```
1. Aller sur /login
2. Entrer email : marie@test.com
3. Entrer mot de passe : password123
→ Connexion réussie
→ Redirection vers le dashboard
→ Menu adapté aux résidents (Mes travaux, Signaler un problème)
```

**Connexion d'un prestataire :**
```
1. Aller sur /login
2. Entrer NEQ : ABC123
3. Entrer mot de passe : password123
→ Connexion réussie
→ Redirection vers le dashboard
→ Menu adapté aux prestataires (Problèmes disponibles, Mes projets)
```

**Résident signale un problème :**
```
Email : marie@test.com
Rue : Saint-Denis
Quartier : Plateau
Description : Trou dangereux
→ Abonnement automatique au Plateau
→ Notification envoyée au STPM
```

**Prestataire soumet une candidature :**
```
NEQ : ABC123
Type : Entretien urbain
Dates : 2025-07-15 à 2025-07-20
Coût : 5000$
→ Notification envoyée au STPM
```

**STPM valide une candidature :**
```
1. Agent STPM consulte /stpm/candidatures
2. Clique sur "Accepter" pour la candidature #4
→ Statut changé à "Approuvée"
→ Projet créé automatiquement
→ Notifications envoyées aux résidents du quartier
→ Notifications envoyées aux prestataires abonnés
```

**STPM modifie la priorité d'un problème :**
```
1. Agent STPM consulte /stpm (onglet Problèmes)
2. Utilise l'API PUT /api/stpm/problemes/{id}/priorite
   avec { "priorite": "ELEVEE" }
→ Priorité modifiée
→ Notifications envoyées aux résidents concernés
```

**Modification de préférences de notification :**
```
Résident : S'abonner aux notifications pour le quartier "Villeray"
Prestataire : S'abonner aux notifications de problèmes "Travaux routiers"
```

## Documentation technique

### Endpoints API REST

**Base URL :** `http://localhost:7000/api`

#### Santé
- `GET /api/health` - Vérification de l'API

#### Authentification
- `POST /api/auth/login` - Connexion d'un utilisateur
  - Body: `{ "identifier": "email@test.com" | "NEQ123", "password": "motdepasse" }`
  - Réponse: `{ "success": true, "type": "RESIDENT" | "PRESTATAIRE", "user": { "id": 1, "email": "...", "nom": "..." } }`
  - Les mots de passe sont hashés avec BCrypt
  - La session est stockée côté frontend dans localStorage

#### Résidents
- `POST /api/residents/problemes` - Signaler un problème
- `GET /api/residents/travaux` - Consulter les travaux (filtres: quartier, type)
- `GET /api/residents/{email}/notifications` - Consulter notifications
- `POST /api/residents/{email}/abonnements` - Créer abonnement
- `GET /api/residents/{email}/abonnements` - Consulter abonnements
- `PUT /api/residents/{email}/notifications/marquer-lu` - Marquer notifications lues
- `GET /api/residents/{email}/preferences` - Récupérer préférences
- `PUT /api/residents/{email}/preferences` - Modifier préférences

#### Prestataires
- `GET /api/prestataires/problemes` - Consulter problèmes disponibles (filtres: quartier, type)
- `POST /api/prestataires/candidatures` - Soumettre candidature
- `GET /api/prestataires/{neq}/projets` - Consulter projets du prestataire
- `PUT /api/prestataires/projets/{id}` - Mettre à jour projet
- `GET /api/prestataires/{neq}/notifications` - Consulter notifications
- `POST /api/prestataires/{neq}/abonnements` - Créer abonnement
- `GET /api/prestataires/{neq}/preferences` - Consulter préférences

#### STPM
- `GET /api/stpm/candidatures?page=0&size=20` - Consulter toutes les candidatures (pagination)
- `PUT /api/stpm/candidatures/{id}/valider` - Valider/refuser candidature
  - Body: `{ "accepter": true }` ou `{ "accepter": false }`
  - Si accepté : crée automatiquement un projet et envoie des notifications
- `GET /api/stpm/problemes?page=0&size=20` - Consulter tous les problèmes (pagination)
- `PUT /api/stpm/problemes/{id}/priorite` - Modifier priorité d'un problème
  - Body: `{ "priorite": "FAIBLE" | "MOYENNE" | "ELEVEE" }`
- `GET /api/stpm/notifications` - Consulter les notifications STPM

#### API Externe
- `GET /api/montreal/travaux` - Travaux officiels de Montréal

### Persistance
- **PostgreSQL** : Base de données principale (via Docker)
- Données persistantes dans un volume Docker
- Tables créées automatiquement au démarrage
- Données de test initialisées automatiquement
- Migration depuis H2 vers PostgreSQL complétée

### Tests

#### Exécuter les tests unitaires (fonctionne avec Java 25)
```bash
# Tests unitaires de base (modèles, services, stockage)
mvn test -Dtest="GestionnaireProjetTest,JsonStorageTest,ProblemeTest,ProjetTest,ResidentTest,CandidatureTest,NotificationTest"

# Ou utiliser le script fourni
.\run-tests.bat
```

**Résultats :** ✅ 17 tests réussis (modèles, services, stockage)

#### Exécuter TOUS les tests (nécessite Java 21)
```bash
# Configurer Java 21
set JAVA_HOME=C:\path\to\java21

# Exécuter tous les tests
mvn clean test

# Ou utiliser le script fourni
.\run-all-tests-java21.bat
```

**Résultats attendus :** 48 tests au total (17 unitaires + 31 d'intégration)

#### Autres commandes de test
```bash
# Tests avec rapport détaillé
mvn test -X

# Générer rapport de couverture JaCoCo (nécessite Java 21)
mvn clean test jacoco:report
```

**⚠️ Note importante concernant Java 25 :**
- ✅ **17 tests unitaires** fonctionnent parfaitement avec Java 25
- ⚠️ **31 tests d'intégration** nécessitent Java 21 (incompatibilité Mockito avec Java 25)
- 📊 Voir `test-results-summary.md` pour le détail complet

### Documentation JavaDoc
```bash
# Générer la documentation JavaDoc
mvn javadoc:javadoc

# La documentation sera générée dans target/site/apidocs/
```

## Technologies utilisées

### Backend
- **Langage :** Java 21
- **Framework :** Spring Boot 3.2.0
- **Base de données :** PostgreSQL 15 (via Docker) ou H2 (développement)
- **ORM :** Spring Data JPA / Hibernate
- **Architecture :** REST API
- **Sécurité :** BCrypt pour le hashage des mots de passe (Spring Security Crypto)
- **Logging :** SLF4J + Logback
- **Tests :** JUnit 5.9.3, Mockito 5.14.2, JaCoCo
- **Build :** Maven 3.8+
- **Documentation API :** Swagger/OpenAPI

### Frontend
- **Framework :** Next.js 15.5.7
- **Langage :** TypeScript
- **UI :** React 19.2.0
- **Styling :** Tailwind CSS
- **Graphiques :** Recharts
- **Authentification :** Context API (React) avec localStorage pour la session
- **Composants UI :** Radix UI / shadcn/ui

## Améliorations récentes (Post-DM3)

### Backend
- ✅ **PostgreSQL avec Docker** : Migration de H2 vers PostgreSQL, base de données persistante
- ✅ **Gestion des préférences utilisateur** : Entité `PreferenceEntity`, Repository et endpoints GET/PUT complets
- ✅ **Logging professionnel** : SLF4J + Logback avec rotation automatique
- ✅ **Configuration externalisée** : Fichier `application.properties` avec PostgreSQL par défaut
- ✅ **Gestion d'erreurs améliorée** : DTOs standardisés, codes HTTP appropriés
- ✅ **Validation des données** : Validation d'email, NEQ, sanitization XSS

### Frontend
- ✅ **Dashboard amélioré** : Filtres avancés, recherche en temps réel, tri par colonnes (date, priorité, lieu)
- ✅ **Page Settings** : Interface complète pour gérer les préférences de notification avec toggles et sélection multiple
- ✅ **Responsive design** : Optimisé pour mobile et desktop
- ✅ **Interface moderne** : Next.js 15.5.7 avec React 19.2.0 et composants UI modernes

Voir `docs/NEXT_STEPS.md` pour les détails complets et `DOCKER_POSTGRES.md` pour la configuration PostgreSQL.

## Améliorations par rapport au DM2
- ✅ **Système de notifications avancé** : Affichage du nombre, gestion des abonnements personnalisés
- ✅ **Données enrichies** : 5 résidents, 5 prestataires, 5 problèmes, 5 projets comme requis
- ✅ **Validation robuste** : Toutes les entrées sont validées, l'application ne plante pas
- ✅ **Persistance complète** : Données préservées dans PostgreSQL
- ✅ **Tests unitaires étendus** : 6 fonctionnalités testées avec JUnit
- ✅ **Documentation JavaDoc complète** : Toutes les classes documentées
- ✅ **Rapport de couverture JaCoCo** : Mesure de la qualité des tests

## Équipe
- **Younes Lagha** - Développement et tests
- **Rayyan Oumlil** - Documentation et diagrammes UML
- **Karim Omairi** - Interface et architecture

---

**Note :** Cette version finale du DM3 implémente toutes les fonctionnalités demandées avec une architecture robuste et une documentation complète. Le système est prêt pour une utilisation en production.
