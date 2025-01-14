package tiw.is.scheduler.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe de configuration de la grille de l'entrepôt.
 * La grille va ressembler à ça :
 * -----------------------------------------
 * |   |   |L0 |   |   |   |   |L1 |   |   |
 * |   |   |   |   |   |   |   |   |   |   |
 * |   |   |   |   |   |   |   |   |   | C |
 * |   |   |   |   |   |   |   |   |   |   |
 * |P0 |   |   |   |P1 |   |   |   |P2 |   |
 * -----------------------------------------
 *
 * C : point de chargement
 * Lx : points de livraison
 * Px : parkings attribués aux étagères non utilisées
 */
public class Grille {

    /**
     * Taille de la grille
     */
    public static final int GRILLE_X = 10;
    public static final int GRILLE_Y = 5;

    /**
     * Points d'intérêt de la grille.
     */
    private final Map<String, PointOfInterest> configGrille = new HashMap<>();

    public Grille() {
        addPointToGrille("C", new PointOfInterest(PoiType.CHARGEMENT, new Coordonnees(10, 3)));
        addPointToGrille("L0", new PointOfInterest(PoiType.LIVRAISON, new Coordonnees(2, 0)));
        addPointToGrille("L1", new PointOfInterest(PoiType.LIVRAISON, new Coordonnees(7, 0)));
        addPointToGrille("P0", new PointOfInterest(PoiType.PARKING, new Coordonnees(0, 4)));
        addPointToGrille("P1", new PointOfInterest(PoiType.PARKING, new Coordonnees(4, 4)));
        addPointToGrille("P2", new PointOfInterest(PoiType.PARKING, new Coordonnees(8, 4)));
    }

    void addPointToGrille(String name, PointOfInterest pointOfInterest) {
        configGrille.put(name, pointOfInterest);
    }

    public String getChargementPoiId() {
        return configGrille.keySet().stream().filter(key -> configGrille.get(key).type() == PoiType.CHARGEMENT).findFirst().orElse(null);
    }

    public List<String> getLivraisonPoiIds() {
        return configGrille.keySet().stream().filter(key -> configGrille.get(key).type() == PoiType.LIVRAISON).toList();
    }

    public List<String> getParkingPoiIds() {
        return configGrille.keySet().stream().filter(key -> configGrille.get(key).type() == PoiType.PARKING).toList();
    }

    public Coordonnees getPointOfInterestCoordonnees(String poiId) {
        return configGrille.get(poiId).coordonnees();
    }
}
