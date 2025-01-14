package tiw.is.scheduler.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.model.Produit;

/**
 * Réalise les opérations métier sur une étagère.
 * Ne devrait être accédée qu'à travers la méthode Etagere.getService().
 */
public class EtagereService {
    private static final Logger log = LoggerFactory.getLogger(EtagereService.class);

    private final Etagere etagere;

    public EtagereService(Etagere etagere) {
        this.etagere = etagere;
    }

    /**
     * Commande le déplacement de l'étagère au prochain mouvement.
     * @param coordonnees Nouvelles coordonnées vers lesquelles se déplacer
     */
    public void orderDeplacement(Coordonnees coordonnees) {
        etagere.getDestinations().addFirst(coordonnees);
        log.info("Etagere " + etagere.getSerialNumber() + " : changement de destination (" + coordonnees.x() + ", " + coordonnees.y() + ")");
    }

    /**
     * Planifie un déplacement de l'étagère (quand elle aura fait les précédents).
     * @param coordonnees Nouvelles coordonnées vers lesquelles se déplacer
     */
    public void planDeplacement(Coordonnees coordonnees) {
        etagere.getDestinations().add(coordonnees);
        log.info("Etagere " + etagere.getSerialNumber() + " : nouvelle destination (" + coordonnees.x() + ", " + coordonnees.y() + ")");
    }

    /**
     * Supprime le premier déplacement planifié.
     * À utiliser quand l'étagère est arrivée à destination.
     */
    public void removeDeplacement() {
        etagere.getDestinations().removeFirst();
    }

    /**
     * Réalise un déplacement de l'étagère vers sa prochaine destination.
     * @return Les coordonnées de l'étagère après le déplacement
     */
    public Coordonnees move() {
        etagere.setPosition(getNextMove());
        return etagere.getPosition();
    }

    /**
     * Calcule les coordonnées de la prochaine case de la grille où l'étagère devrait se rendre.
     * L'étagère peut avancer d'une case au maximum autour d'elle ;
     * elle partira donc "en direction" de sa prochaine destination.
     * @return Les coordonnées de la case autour d'elle en direction de la prochaine destination
     * ou sa position si elle n'a pas de destination en cours
     */
    public Coordonnees getNextMove() {
        if (etagere.getDestinations().isEmpty()) {
            return etagere.getPosition();
        }
        return new Coordonnees(
                etagere.getPosition().x() + Integer.signum(etagere.getNextDestination().x() - etagere.getPosition().x()),
                etagere.getPosition().y() + Integer.signum(etagere.getNextDestination().y() - etagere.getPosition().y()));
    }

    /**
     * Charge un lot de produits identiques sur l'étagère, qu'elle pourra ensuite livrer pour une commande client.
     * Ne doit être appelée que lorsqu'elle est à un point de chargement.
     * @param produit Le produit chargé sur l'étagère
     * @param quantite La quantité de produits à charger
     */
    public void load(Produit produit, int quantite) throws FullEtagereException {
        if (quantite < 1) {
            throw new IllegalArgumentException("Quantite must be greater than 0");
        }
        etagere.addQuantiteProduit(produit, quantite);
        log.info("Etagere " + etagere.getSerialNumber() + " - chargement terminé : nouvelle quantité de produit " + produit.code() + " = " + etagere.getQuantiteProduit(produit));
    }

    /**
     * Livre un lot de produits identiques afin de compléter une commande client.
     * Ne doit être appelée que lorsqu'elle est à un point de livraison.
     * @param produit Le produit demandé
     * @param quantite La quantité de produits à livrer
     */
    public void deliver(Produit produit, int quantite) throws MissingProduitException {
        if (quantite < 1) {
            throw new IllegalArgumentException("Quantite must be greater than 0");
        }
        etagere.removeQuantiteProduit(produit, quantite);
        log.info("Etagere " + etagere.getSerialNumber() + " - livraison terminée : nouvelle quantité de produit " + produit.code() + " = " + etagere.getQuantiteProduit(produit));
    }
}
