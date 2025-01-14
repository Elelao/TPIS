package tiw.is.scheduler.services;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.server.Planificateur;
import tiw.is.scheduler.model.Grille;
import tiw.is.scheduler.utils.MustBeInTransactionException;

import java.util.Collection;

import static tiw.is.scheduler.model.Etagere.MAX_CAPACITY;

public class PlanificateurService {
    private static final Logger log = LoggerFactory.getLogger(Planificateur.class);

    private final EntityManager em;
    private final EtagereRepository etagereRepository;

    private final Grille grille = new Grille();

    public PlanificateurService(EntityManager em, EtagereRepository etagereRepository) {
        this.em = em;
        this.etagereRepository = etagereRepository;
    }

    /**
     * Planifie et exécute le chargement d'une certaine quantité de produit.
     * @param produit Le produit à charger (doit exister dans la base)
     * @param quantite La quantité de produit à charger
     * @throws NoSpaceAvailableException Si aucune étagère ne dispose d'une quantité de places vides suffisante
     */
    public void planChargement(Produit produit, int quantite) throws NoSpaceAvailableException {
        if (!em.getTransaction().isActive()) {
            throw new MustBeInTransactionException();
        }

        Coordonnees chargementCoordonnees = grille.getPointOfInterestCoordonnees(grille.getChargementPoiId());
        // On cherche sur quelle étagère faire le chargement
        Etagere etagereChoisie = null;
        Collection<Etagere> etageres = etagereRepository.getAll();
        for (Etagere etagere : etageres) {
            if (MAX_CAPACITY - etagere.getOverallQuantite() >= quantite) {
                etagereChoisie = etagere;
                break;
            }
        }
        if (etagereChoisie == null) {
            throw new NoSpaceAvailableException("Aucune étagère disponible pour recevoir cette livraison.");
        }

        // On planifie le déplacement
        etagereChoisie.getService().planDeplacement(chargementCoordonnees);

        // On attend (un certain temps) pour que l'étagère se positionne
        try {
            int timeout = 30;
            do {
                if (timeout-- < 0) {
                    return;
                }
                Thread.sleep(1000);
            } while (!etagereChoisie.getPosition().equals(chargementCoordonnees));

            etagereChoisie.getService().load(produit, quantite);
        } catch (InterruptedException | FullEtagereException ignored) {
        }
    }

    /**
     * Planifie et exécute la livraison d'une certaine quantité de produit.
     * @param produit Le produit à livrer
     * @param quantite La quantité de produit à livrer
     * @param livraisonPoi Le point de livraison du produit sur la grille
     * @throws ProduitOutOfStockException Si aucune étagère ne possède une quantité suffisante de ce produit
     */
    public void planLivraison(Produit produit, int quantite, String livraisonPoi) throws ProduitOutOfStockException {
        if (!em.getTransaction().isActive()) {
            throw new MustBeInTransactionException();
        }

        Coordonnees livraisonCoordonnees = grille.getPointOfInterestCoordonnees(livraisonPoi);
        Etagere etagereChoisie = null;
        Collection<Etagere> etageres = etagereRepository.getAll();
        for (Etagere etagere : etageres) {
            if (etagere.getQuantiteProduit(produit) >= quantite) {
                etagereChoisie = etagere;
                break;
            }
        }
        if (etagereChoisie == null) {
            throw new ProduitOutOfStockException("L'entrepôt ne contient pas assez de produit " + produit.code() + " pour pouvoir livrer cette commande.");
        }
        etagereChoisie.getService().planDeplacement(livraisonCoordonnees);

        try {
            // On attend qu'elle arrive pour réaliser l'opération...
            int timeout = 30;
            do {
                if (timeout-- < 0) {
                    return;
                }
                Thread.sleep(1000);
            } while (!etagereChoisie.getPosition().equals(livraisonCoordonnees));
            etagereChoisie.getService().deliver(produit, quantite);
        } catch (InterruptedException | MissingProduitException ignored) {
        }
    }
}
