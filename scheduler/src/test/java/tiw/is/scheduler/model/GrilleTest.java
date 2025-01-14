package tiw.is.scheduler.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrilleTest {
    private Grille grille;

    @BeforeEach
    void setUp() {
        grille = new Grille();
    }

    @Test
    void addPointToGrille() {
        assertDoesNotThrow(() -> {
            grille.addPointToGrille("test", new PointOfInterest(PoiType.LIVRAISON, new Coordonnees(2, 0)));
        });
    }

    @Test
    void getChargementPoiId() {
        assertEquals("C", grille.getChargementPoiId());
    }

    @Test
    void getLivraisonPoiIds() {
        assertEquals(2, grille.getLivraisonPoiIds().size());
    }

    @Test
    void getParkingPoiIds() {
        assertEquals(3, grille.getParkingPoiIds().size());
    }

    @Test
    void getPointOfInterestCoordonnees() {
        assertEquals(new Coordonnees(10, 3), grille.getPointOfInterestCoordonnees("C"));
    }
}