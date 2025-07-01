package ca.udem.maville.modele;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProblemeTest {

    @Test
    void testCreationProbleme() {
        Resident resident = new Resident("Tremblay", "Marie", "514-555-0001", 
                                         "marie@email.com", "123 Rue Test");
        Probleme probleme = new Probleme("Rue Test", TypeTravaux.TRAVAUX_ROUTIERS,
                                         "Description test", resident);

        assertEquals("Rue Test", probleme.getLieu());
        assertEquals(TypeTravaux.TRAVAUX_ROUTIERS, probleme.getTypeProbleme());
        assertEquals("Description test", probleme.getDescription());
        assertEquals(resident, probleme.getDeclarant());
        assertFalse(probleme.isResolu());
    }
}
