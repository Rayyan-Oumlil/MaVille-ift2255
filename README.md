# MaVille - Application de Gestion des Travaux Publics

## Description du projet

**MaVille** est une application développée dans le cadre du cours IFT2255 (Génie Logiciel) qui vise à automatiser et améliorer la gestion des travaux publics à Montréal. L'application facilite la communication entre les résidents, les prestataires de services et le Service des Travaux Publics de Montréal (STPM).

### Fonctionnalités principales

   **Pour les résidents :**
  - Signaler des problèmes  (nids-de-poule, fissures, etc.)
  - Consulter ses signalements passés
  - Voir les travaux en cours dans son arrondissement

  **Pour les prestataires :**
  - Consulter les problèmes signalés en attente
  - Soumettre des candidatures pour des projets
  - Gérer ses candidatures (modifier, annuler, consulter)

### Contexte académique

Ce projet est réalisé en 3 étapes :
- **Devoir 1** (actuel) : Modélisation du domaine et prototype fonctionnel
- **Devoir 2** : Conception détaillée
- **Devoir 3** : Implémentation complète

## Organisation du répertoire

```
maville/
├── README.md                   # Ce fichier
├── rapport/                    # Documentation du projet
│   ├── index.html              # Rapport principal en HTML
│   ├── style.css               # Styles CSS
│   └── images/                 # Images et diagrammes
│       ├── udem_logo.png       
│       ├── diagramme_activites.png  # Diagramme d'activités
│       └── diagramme_classes.png    # Diagramme de classes
│       └──diagramme_CU.png          # Diagramme de cas d'utilisation
│
├── src/                        
│   └── ca/
│       └── udem/
│           └── maville/
│               ├── Main.java           # Demarrage de l'app
│               ├── modele/            # Entités du domaine
│               │   ├── Resident.java
│               │   ├── Prestataire.java
│               │   ├── Probleme.java
│               │   ├── Candidature.java
│               │   ├── Projet.java
│               │   ├── TypeTravaux.java
│               │   ├── Priorite.java
│               │   ├── StatutCandidature.java
│               │   └── StatutProjet.java
│               ├── service/           # Logique métier
│               │   ├── GestionnaireProblemes.java
│               │   └── GestionnaireProjets.java
│               └── ui/                # Interface utilisateur
│                   ├── MenuPrincipal.java
│                   ├── MenuResident.java
│                   ├── MenuPrestataire.java
│                   ├── AffichageConsole.java
│                   └── SaisieConsole.java
│
```

### Exécution
```

1. Téléchargez le fichier maVille-1.0-SNAPSHOT.jar depuis la section "Releases" du projet GitHub.
2. Ouvrez un terminal et placez-vous dans le dossier où se trouve le fichier JAR.
3. Exécutez la commande suivante :
                                java -jar maVille-1.0-SNAPSHOT.jar
```

### Navigation dans l'application
1. Choisir votre profil (Résident ou Prestataire)
2. Naviguer avec les numéros des menus
3. Utiliser `0` pour revenir au menu précédent
4. Suivre les instructions à l'écran
5. Choisir "Quitter" pour terminer

## Documentation

- **Rapport complet :** Ouvrir `rapport/index.html` dans un navigateur
- **Modélisation :** Diagrammes UML dans le dossier `rapport/images/`
- **Code source :** Commenté en français, organisé en couches

## Technologies utilisées

- **Langage :** Java
- **Architecture :** MVC (Modèle-Vue-Contrôleur)
- **Interface :** Console (ligne de commande)
- **Documentation :** HTML/CSS 

---

**Note :** Ce prototype démontre la faisabilité de l'application MaVille et constitue une base solide pour le développement d'une solution complète qui automatisera la gestion des travaux publics à Montréal.