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
- Modifier ses préférences de notification

**Pour les prestataires :**
- Consulter les problèmes disponibles avec filtres
- Soumettre des candidatures pour des projets
- Mettre à jour leurs projets en cours
- Voir uniquement leurs propres projets (filtrage par NEQ)
- S'abonner aux notifications de problèmes par quartier ou type

**Pour les agents STPM :**
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
1. Le serveur API sur http://localhost:7000/api
2. L'interface console pour interagir

## Navigation dans l'application

### Menu principal
1. **Résident** - Signaler problèmes, consulter travaux, gérer notifications
2. **Prestataire** - Consulter problèmes, soumettre candidatures, gérer projets
3. **Agent STPM** - Valider candidatures, gérer priorités

### Données de test (DM3)
L'application est initialisée avec :
- **5 résidents** dont deux dans le même quartier, avec des préférences différentes
- **5 prestataires** avec des préférences différentes
- **5 problèmes routiers** déclarés dont au moins 2 avec une priorité affectée
- **5 projets de travaux**, avec au moins un accepté et en cours, et un terminé
- **Notifications déjà consultables** pour tous les profils

### Exemples d'utilisation

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

**STPM valide :**
```
Accepter candidature #4
→ Projet créé automatiquement
→ Notifications envoyées aux résidents du quartier
→ Notifications envoyées aux prestataires abonnés
```

**Modification de préférences de notification :**
```
Résident : S'abonner aux notifications pour le quartier "Villeray"
Prestataire : S'abonner aux notifications de problèmes "Travaux routiers"
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
- `GET /api/notifications/{profil}` - Consulter notifications
- `POST /api/abonnements` - Gérer abonnements

### Persistance
- Données sauvegardées automatiquement dans `data/`
- Format JSON pour tous les objets
- Synchronisation des IDs au démarrage
- Données préservées au-delà des sessions

### Tests
```bash
# Exécuter tous les tests
mvn test

# Tests avec rapport détaillé
mvn test -X

# Générer rapport de couverture JaCoCo
mvn clean test jacoco:report
```

### Documentation JavaDoc
```bash
# Générer la documentation JavaDoc
mvn javadoc:javadoc

# La documentation sera générée dans target/site/apidocs/
```

## Technologies utilisées
- **Langage :** Java 17
- **Architecture :** REST (Client-Serveur)
- **Framework API :** Javalin 5.6.1
- **Client HTTP :** OkHttp 4.11
- **Sérialisation :** Jackson 2.15.2
- **Tests :** JUnit 5.9.3, JaCoCo
- **Build :** Maven 3.8+
- **Interface :** Console interactive

## Améliorations par rapport au DM2
- ✅ **Système de notifications avancé** : Affichage du nombre, gestion des abonnements personnalisés
- ✅ **Données enrichies** : 5 résidents, 5 prestataires, 5 problèmes, 5 projets comme requis
- ✅ **Validation robuste** : Toutes les entrées sont validées, l'application ne plante pas
- ✅ **Persistance complète** : Données préservées au-delà des sessions
- ✅ **Tests unitaires étendus** : 6 fonctionnalités testées avec JUnit
- ✅ **Documentation JavaDoc complète** : Toutes les classes documentées
- ✅ **Rapport de couverture JaCoCo** : Mesure de la qualité des tests
- ✅ **GitHub Actions** : Intégration continue (CI/CD)
- ✅ **Manuel d'utilisation complet** : Instructions détaillées

## Équipe
- **Younes Lagha** - Développement et tests
- **Rayyan Oumlil** - Documentation et diagrammes UML
- **Karim Omairi** - Interface et architecture

---

**Note :** Cette version finale du DM3 implémente toutes les fonctionnalités demandées avec une architecture robuste et une documentation complète. Le système est prêt pour une utilisation en production.
