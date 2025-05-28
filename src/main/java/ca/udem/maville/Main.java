package ca.udem.maville;

import ca.udem.maville.service.*;
import ca.udem.maville.ui.MenuPrincipal;

public class Main {
    public static void main(String[] args) {
        // Initialisation simple des services
        GestionnaireProblemes gestionnaireProblemes = new GestionnaireProblemes();
        GestionnaireProjets gestionnaireProjets = new GestionnaireProjets();
        
        // Lancement de l'application
        MenuPrincipal menuPrincipal = new MenuPrincipal(gestionnaireProblemes, gestionnaireProjets);
        menuPrincipal.demarrer();
    }
}