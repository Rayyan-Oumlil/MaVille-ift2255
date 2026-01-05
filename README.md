# MaVille - Plateforme de Gestion des Travaux Publics

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring)
![Next.js](https://img.shields.io/badge/Next.js-15.5.7-black?style=for-the-badge&logo=next.js)
![React](https://img.shields.io/badge/React-19.2.0-blue?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=for-the-badge&logo=postgresql)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?style=for-the-badge&logo=typescript)

**Application full-stack moderne pour la coordination des travaux publics à Montréal**

[Fonctionnalités](#-fonctionnalités) • [Technologies](#-stack-technologique) • [Installation](#-installation) • [Documentation](#-documentation)

</div>

---

## 📋 Description

**MaVille** est une plateforme web complète développée pour automatiser et améliorer la gestion des travaux publics à Montréal. L'application facilite la communication et la coordination entre trois types d'acteurs :

- **Résidents** : Signalement de problèmes routiers, consultation des travaux, gestion des notifications
- **Prestataires** : Consultation des problèmes, soumission de candidatures, gestion de projets
- **Agents STPM** : Validation des candidatures, gestion des priorités, supervision des projets

### 🎯 Objectifs

- ✅ Automatiser le processus de signalement et de gestion des travaux
- ✅ Améliorer la communication entre tous les acteurs
- ✅ Fournir une interface moderne et intuitive
- ✅ Assurer la traçabilité et la transparence des opérations
- ✅ Optimiser les performances avec des technologies modernes

---

## 🚀 Fonctionnalités

### 🔐 Authentification Multi-Rôles

- **Système sécurisé** : Hashage BCrypt des mots de passe
- **Trois types d'utilisateurs** : Résidents (email), Prestataires (NEQ), Agents STPM
- **Session persistante** : Gestion avec localStorage et Context API
- **Protection des routes** : Redirection automatique vers `/login` si non authentifié
- **Menu adaptatif** : Interface personnalisée selon le rôle

### 👥 Pour les Résidents

- ✅ **Signalement de problèmes** : Formulaire complet avec localisation, type de travaux, description
- ✅ **Consultation des travaux** : Filtres par quartier et type, recherche avancée
- ✅ **Gestion des notifications** : Abonnements personnalisés (quartier, rue, type)
- ✅ **Préférences** : Configuration des notifications (email, quartier, type)
- ✅ **Dashboard** : Vue d'ensemble avec statistiques et graphiques

### 🏢 Pour les Prestataires

- ✅ **Consultation des problèmes** : Liste avec filtres avancés et pagination
- ✅ **Soumission de candidatures** : Formulaire complet avec dates, coût, description
- ✅ **Gestion de projets** : Mise à jour du statut, consultation des projets en cours
- ✅ **Notifications** : Alertes pour nouveaux problèmes correspondant aux critères
- ✅ **Abonnements** : Filtres par quartier et type de travaux

### 🏛️ Pour les Agents STPM

- ✅ **Validation des candidatures** : Acceptation/refus avec création automatique de projets
- ✅ **Gestion des priorités** : Affectation de priorités (FAIBLE, MOYENNE, ÉLEVÉE)
- ✅ **Supervision** : Vue d'ensemble de tous les problèmes, candidatures et projets
- ✅ **Notifications** : Alertes pour nouveaux problèmes et candidatures
- ✅ **Dashboard administratif** : Statistiques complètes et graphiques d'activité

### 🔔 Système de Notifications Temps Réel

- ✅ **WebSocket STOMP** : Notifications instantanées (backend Spring Boot + client frontend)
- ✅ **Abonnements personnalisés** : Par quartier, rue, ou type de travaux
- ✅ **Notifications automatiques** : Création de projets, changements de statut, priorités
- ✅ **Gestion des préférences** : Activation/désactivation par type de notification
- ✅ **Interface utilisateur** : Badge avec nombre de notifications, toasts en temps réel

### 📊 Dashboard et Analytics

- ✅ **Statistiques en temps réel** : Nombre de problèmes, candidatures, projets
- ✅ **Graphiques interactifs** : Activité par période, répartition par type
- ✅ **Filtres avancés** : Recherche, tri, pagination sur toutes les listes
- ✅ **Vues personnalisées** : Adaptées selon le rôle de l'utilisateur

---

## 🛠️ Stack Technologique

### Backend

| Catégorie | Technologie | Version |
|-----------|------------|---------|
| **Langage** | Java | 21 |
| **Framework** | Spring Boot | 3.2.0 |
| **Base de données** | PostgreSQL | 15 (Docker) |
| **ORM** | Spring Data JPA / Hibernate | 3.2.0 |
| **WebSocket** | Spring WebSocket (STOMP) | 3.2.0 |
| **Validation** | Jakarta Validation | 3.0 |
| **Sécurité** | Spring Security Crypto (BCrypt) | 6.2.0 |
| **Logging** | SLF4J + Logback | 1.4.11 |
| **Documentation API** | SpringDoc OpenAPI (Swagger) | 2.0.4 |
| **Tests** | JUnit 5, Mockito, Testcontainers | 5.9.3, 5.18.0, 1.19.3 |
| **Build** | Maven | 3.8+ |
| **HTTP Client** | OkHttp | 4.11.0 |

**Architecture Backend :**
- ✅ REST API avec 14 endpoints synchronisés
- ✅ Controllers avec validation automatique (`@Valid`)
- ✅ Gestion d'erreurs globale (`@ControllerAdvice`)
- ✅ DTOs pour la sérialisation JSON
- ✅ Services métier avec transactions (`@Transactional`)
- ✅ Repositories JPA avec requêtes optimisées (`@EntityGraph`)
- ✅ Cache Spring pour améliorer les performances
- ✅ WebSocket STOMP pour notifications temps réel

### Frontend

| Catégorie | Technologie | Version |
|-----------|------------|---------|
| **Framework** | Next.js | 15.5.7 |
| **Langage** | TypeScript | 5.0 |
| **UI Library** | React | 19.2.0 |
| **Styling** | Tailwind CSS | 4.1.9 |
| **Composants UI** | Radix UI / shadcn/ui | Latest |
| **State Management** | React Query | 5.62.0 |
| **WebSocket Client** | @stomp/stompjs + SockJS | 7.0.0, 1.6.1 |
| **Formulaires** | React Hook Form + Zod | 7.60.0, 3.25.76 |
| **Graphiques** | Recharts | Latest |
| **Toasts** | Sonner | 1.7.4 |
| **Monitoring** | Sentry | 10.29.0 |
| **Tests E2E** | Playwright | Latest |
| **Build** | Next.js (Turbopack) | 15.5.7 |

**Architecture Frontend :**
- ✅ App Router (Next.js 15)
- ✅ Server Components et Client Components
- ✅ React Query pour le cache et la synchronisation
- ✅ WebSocket client avec reconnexion automatique
- ✅ Composants réutilisables (Upload, Commentaires)
- ✅ Optimisations production (code splitting, lazy loading)
- ✅ Tests E2E avec Playwright

### Infrastructure

- **Containerisation** : Docker + Docker Compose
- **Base de données** : PostgreSQL 15 (production) / H2 (tests)
- **CI/CD** : GitHub Actions
- **Versioning** : Git
- **Documentation** : Swagger/OpenAPI, JavaDoc

---

## 📦 Installation

### Prérequis

- **Java** : 21 ou supérieur
- **Node.js** : 18 ou supérieur
- **Maven** : 3.8 ou supérieur
- **Docker** : Pour PostgreSQL (optionnel)
- **Git** : Pour cloner le repository

### 🐳 Démarrage avec Docker (Recommandé)

**1. Cloner le repository :**
```bash
git clone https://github.com/Rayyan-Oumlil/MaVille-ift2255.git
cd MaVille-ift2255
```

**2. Lancer PostgreSQL :**
```bash
# Option 1 : Docker Compose
docker-compose up -d postgres

# Option 2 : Docker run
docker run -d --name maville-postgres \
  -e POSTGRES_DB=maville \
  -e POSTGRES_USER=maville_user \
  -e POSTGRES_PASSWORD=maville_password \
  -p 5432:5432 \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine
```

**3. Lancer le backend :**
```bash
mvn spring-boot:run
```

Le backend sera accessible sur `http://localhost:7000/api`

**4. Lancer le frontend :**
```bash
cd frontend
npm install  # ou pnpm install
npm run dev   # ou pnpm dev
```

Le frontend sera accessible sur `http://localhost:3000`

### 📍 Accès à l'application

- **Frontend** : http://localhost:3000
- **Backend API** : http://localhost:7000/api
- **Swagger UI** : http://localhost:7000/swagger-ui.html
- **Page de connexion** : http://localhost:3000/login

### 🔑 Données de test

**Résident :**
- Email : `marie@test.com`
- Mot de passe : `password123`

**Prestataire :**
- NEQ : `ABC123`
- Mot de passe : `password123`

**Agent STPM :**
- Identifiant : `stpm1`
- Mot de passe : `password123`

---

## 🧪 Tests

### Tests Backend

**Tests E2E (5 scénarios complets) :**
```bash
mvn test -Dtest=WorkflowE2ETest
```

**Tests unitaires :**
```bash
mvn test
```

**Résultats :**
- ✅ 5 tests E2E (workflows complets)
- ✅ 17 tests unitaires (modèles, services, stockage)
- ✅ 31 tests d'intégration (nécessitent Java 21)

### Tests Frontend

**Tests E2E avec Playwright :**
```bash
cd frontend
npm run test:e2e          # Exécuter tous les tests
npm run test:e2e:ui       # Interface UI pour déboguer
npm run test:e2e:headed   # Mode visible (avec navigateur)
```

**Tests disponibles :**
- ✅ Authentification (connexion, erreurs)
- ✅ Dashboard (navigation, affichage)
- ✅ Signalement de problème

---

## 📚 Documentation API

### Endpoints Principaux

**Authentification :**
- `POST /api/auth/login` - Connexion utilisateur

**Résidents :**
- `POST /api/residents/problemes` - Signaler un problème
- `GET /api/residents/travaux` - Consulter les travaux
- `GET /api/residents/{email}/notifications` - Notifications
- `PUT /api/residents/{email}/preferences` - Modifier préférences

**Prestataires :**
- `GET /api/prestataires/problemes` - Consulter problèmes disponibles
- `POST /api/prestataires/candidatures` - Soumettre candidature
- `GET /api/prestataires/{neq}/projets` - Projets du prestataire

**STPM :**
- `GET /api/stpm/candidatures` - Consulter candidatures (pagination)
- `PUT /api/stpm/candidatures/{id}/valider` - Valider/refuser
- `PUT /api/stpm/problemes/{id}/priorite` - Modifier priorité

**Documentation complète :** http://localhost:7000/swagger-ui.html

---

## 🏗️ Architecture

### Structure du Projet

```
MaVille-ift2255/
├── frontend/                 # Application Next.js
│   ├── app/                  # Pages (App Router)
│   ├── components/           # Composants React
│   ├── hooks/                # Hooks personnalisés
│   ├── lib/                  # Utilitaires et API client
│   ├── e2e/                  # Tests E2E Playwright
│   └── package.json
│
├── src/
│   ├── main/java/ca/udem/maville/
│   │   ├── api/
│   │   │   ├── controller/   # Contrôleurs REST
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/     # Gestion d'erreurs
│   │   │   └── service/       # Services API
│   │   ├── config/            # Configuration Spring
│   │   ├── entity/            # Entités JPA
│   │   ├── repository/        # Repositories JPA
│   │   └── service/           # Services métier
│   └── test/                  # Tests
│
├── docs/                      # Documentation
├── docker-compose.yml         # Configuration Docker
└── pom.xml                    # Configuration Maven
```

### Patterns et Bonnes Pratiques

- ✅ **Architecture REST** : Séparation claire des responsabilités
- ✅ **DTOs** : Transfert de données optimisé
- ✅ **Validation** : Jakarta Validation avec messages en français
- ✅ **Gestion d'erreurs** : `@ControllerAdvice` centralisé
- ✅ **Transactions** : `@Transactional` pour l'intégrité des données
- ✅ **Cache** : Spring Cache pour améliorer les performances
- ✅ **Optimisation JPA** : `@EntityGraph` pour éviter N+1 queries
- ✅ **Logging structuré** : SLF4J + Logback avec MDC
- ✅ **Tests** : E2E, unitaires, intégration

---

## 🎯 Améliorations Récentes

### ✅ Backend (100% Complété)

- ✅ **PostgreSQL** : Migration complète avec Docker
- ✅ **Tests E2E** : 5 scénarios de workflow complets
- ✅ **Optimisation Performance** : Cache Spring + `@EntityGraph` JPA
- ✅ **Gestion d'erreurs** : Messages français, stack traces conditionnelles
- ✅ **WebSocket STOMP** : Infrastructure complète pour notifications temps réel
- ✅ **Validation** : DTOs avec `@Valid` et messages personnalisés
- ✅ **Documentation API** : Swagger/OpenAPI complètement activé
- ✅ **Logging** : Structuré avec MDC et support JSON

### ✅ Frontend (100% Complété)

- ✅ **React Query** : Migration complète de tous les composants (14/14)
- ✅ **WebSocket Client** : STOMP avec reconnexion automatique
- ✅ **Tests E2E** : Playwright avec 3 fichiers de tests
- ✅ **Upload Fichiers** : Composant avec drag & drop
- ✅ **Commentaires** : Composant réutilisable
- ✅ **Build Production** : Optimisations complètes
- ✅ **Monitoring** : Sentry configuré (optionnel)

---

## 📈 Statistiques du Projet

- **Backend** : 100% fonctionnel et optimisé
- **Frontend** : 100% complété et optimisé
- **Synchronisation** : 14/14 endpoints synchronisés
- **Tests E2E** : 5 scénarios backend + 3 fichiers frontend
- **Performance** : Cache + optimisations JPA + React Query
- **Documentation** : Swagger + JavaDoc complète

---

## 👥 Équipe

- **Younes Lagha** - Développement et tests
- **Rayyan Oumlil** - Documentation et diagrammes UML
- **Karim Omairi** - Interface et architecture

---

## 📄 Licence

Ce projet a été développé dans le cadre du cours IFT2255 (Génie Logiciel) à l'Université de Montréal.

---

## 🔗 Liens Utiles

- **Swagger UI** : http://localhost:7000/swagger-ui.html
- **API Docs JSON** : http://localhost:7000/v3/api-docs
- **Documentation PostgreSQL** : [DOCKER_POSTGRES.md](DOCKER_POSTGRES.md)
- **Prochaines étapes** : [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md)

---

<div align="center">

**🎉 Projet complet et fonctionnel à 100%**

*Dernière mise à jour : Décembre 2025*

</div>
