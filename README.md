# MaVille - Application de Gestion des Travaux Publics

## Description du projet
**MaVille** est une application développée dans le cadre du cours IFT2255 (Génie Logiciel) qui vise à automatiser et améliorer la gestion des travaux publics à Montréal. L'application facilite la communication entre les résidents, les prestataires de services et le Service des Travaux Publics de Montréal (STPM).

### Architecture REST
Cette version implémente une architecture REST complète avec :
- **API REST** : Serveur Javalin sur le port 7000
- **Client HTTP** : Interface console qui communique avec l'API
- **Persistance JSON** : Stockage des données dans des fichiers JSON
- **Intégration externe** : API de données ouvertes de Montréal

### Fonctionnalités principales

**Pour les résidents :**
- Signaler des problèmes routiers (avec abonnement automatique aux notifications)
- Consulter les travaux en cours ou à venir (3 prochains mois)
- Rechercher des travaux par quartier et/ou type
- Recevoir des notifications personnalisées lors de changements
- S'abonner aux notifications par quartier ou rue

**Pour les prestataires :**
- Consulter les problèmes disponibles avec filtres
- Soumettre des candidatures pour des projets
- Mettre à jour leurs projets en cours
- Voir uniquement leurs propres projets (filtrage par NEQ)

**Pour les agents STPM :**
- Valider ou refuser les candidatures
- Affecter des priorités aux problèmes
- Création automatique de projets lors de l'acceptation

### Système de notifications
- Abonnement automatique au quartier lors du signalement
- Notifications pour : nouveau projet, changement de statut, modification des dates
- Gestion des abonnements personnalisés (quartiers/rues)

## Organisation du répertoire
```
maville/
├── README.md                   # Ce fichier
├── pom.xml                     # Configuration Maven
├── data/                       # Données persistantes (créé automatiquement)
│   ├── problemes.json
│   ├── projets.json
│   ├── candidatures.json
│   ├── residents.json
│   ├── prestataires.json
│   ├── notifications.json
│   └── abonnements.json
│
├── rapport/                    # Documentation du projet
│   ├── index.html              # Rapport principal
│   ├── style.css               # Styles CSS
│   └── images/                 # Diagrammes UML
│       ├── diagramme_activites.jpg
│       ├── Diagramme_classe_conceptuel.jpg
│       ├── Diagramme_classe_CO.jpg
│       ├── Diagramme_activite.jpg
│       ├── diagramme_classes.jpg
│       ├── Diagramme_CU.jpg
│       ├── Diagramme_CU2.jpg
│       ├── Module_arch.jpeg
│       ├── Sequence_Diagram1.jpg
│       └── udem_logo.png
│
├── src/
│   ├── main/java/ca/udem/maville/
│   │   ├── Main.java                    # Point d'entrée
│   │   ├
│   │   ├── api/                         # Architecture REST
│   │   │   ├── ApiServer.java          # Serveur API Javalin
│   │   │   ├── MontrealApiService.java # Intégration API Montréal
│   │   ├── modele/                      # Entités du domaine
│   │   │   ├── Abonnement.java         # Gestion des abonnements
│   │   │   ├── Candidature.java        # Candidatures des prestataires
│   │   │   ├── Notification.java       # Système de notifications
│   │   │   ├── Prestataire.java        # Entreprises prestataires
│   │   │   ├── Priorite.java           # Enum des priorités
│   │   │   ├── Probleme.java           # Problèmes signalés
│   │   │   ├── Projet.java             # Projets de travaux
│   │   │   ├── Resident.java           # Résidents
│   │   │   ├── StatutCandidature.java  # Enum statuts candidature
│   │   │   ├── StatutProjet.java       # Enum statuts projet
│   │   │   └── TypeTravaux.java        # Enum types de travaux
│   │   ├── service/                     # Logique métier
│   │   │   ├── GestionnaireProblemes.java
│   │   │   └── GestionnaireProjets.java
│   │   ├── storage/                     # Persistance JSON
│   │   │   └── JsonStorage.java
│   │   └── ui/                          # Interface utilisateur
│   │       ├── AffichageConsole.java   # Affichage formaté
│   │       ├── MenuPrestataire.java    # Menu prestataires
│   │       ├── MenuPrincipal.java      # Menu principal
│   │       ├── MenuResident.java       # Menu résidents
│   │       ├── MenuStpm.java           # Menu agents STPM
│   │       ├── SaisieConsole.java      # Gestion des entrées
│   │       └── client/
│   │           └── HttpClient.java     # Client HTTP pour l'API
│   │
│   └── test/java/ca/udem/maville/       # Tests unitaires
│       ├── modele/
│       │   ├── CandidatureTest.java
│       │   ├── ProblemeTest.java
│       │   └── ResidentTest.java
│
├── visual_paradigm/                     # Fichiers Visual Paradigm
└── .gitignore                          # Fichiers ignorés par Git
```

## Installation et exécution


### Option 1 : Utiliser le JAR précompilé
```bash
# Télécharger le JAR depuis la section Releases
# Puis exécuter :
java -jar MaVille.jar
```

### Option 2 : Compiler depuis les sources
```bash
# Cloner le repository
git clone 
cd maville

# Compiler et créer le JAR
mvn clean package

# Exécuter
java -jar target/maville-1.0-SNAPSHOT.jar
```

### Démarrage de l'application
L'application démarre automatiquement :
1. Le serveur API sur http://localhost:7000/api
2. L'interface console pour interagir

## Navigation dans l'application

### Menu principal
1. **Résident** - Signaler problèmes, consulter travaux, gérer notifications
2. **Prestataire** - Consulter problèmes, soumettre candidatures, gérer projets
3. **Agent STPM** - Valider candidatures, gérer priorités

### Données de test
L'application est initialisée avec :
- 4 résidents
- 4 prestataires  
- 4 problèmes dans différents quartiers
- 3 candidatures en attente

### Exemples d'utilisation

**Résident signale un problème :**
```
Email : marie@test.com
Rue : Saint-Denis
Quartier : Plateau
Description : Trou dangereux
→ Abonnement automatique au Plateau
```

**Prestataire soumet une candidature :**
```
NEQ : ABC123
Type : Entretien urbain
Dates : 2025-07-15 à 2025-07-20
Coût : 5000$
```

**STPM valide :**
```
Accepter candidature #4
→ Projet créé
→ Notifications envoyées aux résidents du quartier
```

## Documentation technique

### Endpoints API REST
- `GET /api/health` - Vérification de l'API
- `POST /api/residents/problemes` - Signaler un problème
- `GET /api/residents/travaux` - Consulter les travaux
- `GET /api/prestataires/problemes` - Problèmes disponibles
- `POST /api/prestataires/candidatures` - Soumettre candidature
- `PUT /api/stpm/candidatures/{id}/valider` - Valider candidature
- `GET /api/montreal/travaux` - API externe Montréal

### Persistance
- Données sauvegardées automatiquement dans `data/`
- Format JSON pour tous les objets
- Synchronisation des IDs au démarrage

### Tests
```bash
# Exécuter tous les tests
mvn test

# Tests avec rapport détaillé
mvn test -X
```

## Technologies utilisées
- **Langage :** Java 17
- **Architecture :** REST (Client-Serveur)
- **Framework API :** Javalin 5.6.1
- **Client HTTP :** OkHttp 4.11
- **Sérialisation :** Jackson 2.15.2
- **Tests :** JUnit 5.9.3
- **Build :** Maven 3.8+
- **Interface :** Console interactive

## Améliorations par rapport au DM1
- ✅ Architecture REST complète avec séparation claire client/serveur (Javalin + client HTTP)

- ✅ Persistance des données dans des fichiers JSON avec synchronisation des IDs

- ✅ Prototype interactif console avec navigation par profils (résident, prestataire, agent STPM)

- ✅ Système de notifications temps réel basé sur l’abonnement automatique ou manuel (quartier/rue)

- ✅ Filtrage avancé des problèmes et projets par quartier, type, et statut

- ✅ Gestion complète des projets : soumission, validation, mise à jour de statut (en cours, terminé, suspendu, annulé)

- ✅ Validation STPM avec création automatique d’un projet lors de l’acceptation d’une candidature

- ✅ Gestion des priorités par les agents STPM à partir des signalements

- ✅ Détection de doublons (plusieurs signalements possibles pour un même problème)

- ✅ Gestion des candidatures avec édition et annulation possibles tant qu’elles ne sont pas validées

- ✅ Gestion des statuts de projets et candidatures (avec enums dédiées)

- ✅ Initialisation avec données de test (résidents, prestataires, problèmes, candidatures)

- ✅ Intégration de l’API externe de Montréal pour les travaux publics officiels

- ✅ Menus hiérarchiques imbriqués (3 niveaux max) avec retour vers le menu principal

- ✅ Séparation claire du code par couches : modele, service, api, ui, storage

- ✅ README complet avec instructions, documentation API, organisation du projet et démonstration

- ✅ Version exécutable (.jar) générée via Maven et prête pour la release GitHub

- ✅ Tests unitaires sur les classes principales (Probleme, Candidature, Resident)

- ✅ Diagrammes UML à jour (classes, activités, cas d’utilisation, séquence)

## Équipe
- Younes Lagha
- Rayyan oumlil
- Karim Omairi

---

**Note :** Cette version finale implémente toutes les fonctionnalités demandées plus le bonus REST (+10%). Le système est prêt pour une utilisation en production avec quelques ajustements (authentification, base de données, interface web).