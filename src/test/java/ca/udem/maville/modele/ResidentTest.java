package ca.udem.maville.modele;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResidentTest {

    @Test
    void testCreationResident() {
        Resident r = new Resident("Dubois", "Anne", "514-123-4567", "anne@ex.com", "789 Rue X");
        assertEquals("Dubois", r.getNom());
        assertEquals("Anne", r.getPrenom());
        assertEquals("514-123-4567", r.getTelephone());
        assertEquals("anne@ex.com", r.getEmail());
        assertEquals("789 Rue X", r.getAdresse());
    }

    @Test
    void testModifTelephone() {
        Resident r = new Resident("Dubois", "Anne", "514-123-4567", "anne@ex.com", "789 Rue X");
        r.setTelephone("438-999-8888");
        assertEquals("438-999-8888", r.getTelephone());
    }
}
