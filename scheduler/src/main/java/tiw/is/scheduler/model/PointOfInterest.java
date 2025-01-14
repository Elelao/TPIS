package tiw.is.scheduler.model;

import jakarta.persistence.Embeddable;

/**
 * Représentation d'un point important sur la carte de l'entrepôt.
 * <ul>
 *     <li>Point de chargement des étagères (on admet qu'il n'y en a qu'un)</li>
 *     <li>Points de livraison des produits par les étagères</li>
 *     <li>"Parkings" des étagères au repos (pour ne pas encombrer les points de livraison)</li>
 * </ul>
 * @param type Le type de point d'intérêt
 * @param coordonnees Les coordonnées de ce point
 */
@Embeddable
public record PointOfInterest(PoiType type, Coordonnees coordonnees) {
}
