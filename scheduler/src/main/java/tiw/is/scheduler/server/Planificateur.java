package tiw.is.scheduler.server;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.controller.ChargementController;
import tiw.is.scheduler.controller.EtagereController;
import tiw.is.scheduler.controller.LivraisonController;
import tiw.is.scheduler.controller.SimulateurController;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.services.NoSpaceAvailableException;
import tiw.is.scheduler.services.PlanificateurService;
import tiw.is.scheduler.services.ProduitOutOfStockException;
import tiw.is.scheduler.model.Grille;
import tiw.is.scheduler.simulator.Simulateur;

/**
 * Classe principale de l'application.
 */
public class Planificateur {
    private static final Logger log = LoggerFactory.getLogger(Planificateur.class);

    private final EntityManager em;
    private final EtagereRepository etagereRepository;

    private final Grille grille = new Grille();
    private final EtagereController etagereController;
    private final SimulateurController simulateurController = new SimulateurController();
    private final ChargementController chargementController;
    private final LivraisonController livraisonController;

    public Planificateur(EntityManager em, EtagereRepository etagereRepository) {
        this.em = em;
        this.etagereRepository = etagereRepository;
        this.etagereController = new EtagereController(em, etagereRepository);
        PlanificateurService planificateurService = new PlanificateurService(em, etagereRepository);
        this.chargementController = new ChargementController(planificateurService);
        this.livraisonController = new LivraisonController(planificateurService);
    }

    /**
     * Crée et positionne les étagères en fonction de la grille.
     */
    public void initEtageres() {
        grille.getParkingPoiIds().forEach(key -> {
            etagereController.createEtagere(key, grille.getPointOfInterestCoordonnees(key));
        });
        // On ne peut créer le simulateur qu'après les étagères.
        Simulateur simulateur = new Simulateur(etagereRepository.getAll());
        simulateurController.setSimulateur(simulateur);
    }

    /**
     * Démarre le simulateur (dans une autre thread).
     */
    public void startSimulateur() {
        simulateurController.startSimulateur();
    }

    /**
     * Arrête du simulateur (asynchrone)
     */
    public void stopSimulateur() {
        simulateurController.stopSimulateur();
    }

    /**
     * Planifie et exécute le chargement d'une certaine quantité de produit.
     * @param produit Le produit à charger (doit exister dans la base)
     * @param quantite La quantité de produit à charger
     * @throws NoSpaceAvailableException Si aucune étagère ne dispose d'une quantité de places vides suffisante
     */
    public void chargerProduits(Produit produit, int quantite) throws NoSpaceAvailableException {
        chargementController.chargerProduits(produit, quantite);
    }

    /**
     * Planifie et exécute la livraison d'une certaine quantité de produit.
     * @param produit Le produit à livrer
     * @param quantite La quantité de produit à livrer
     * @param livraisonPoi Le point de livraison du produit sur la grille
     * @throws ProduitOutOfStockException Si aucune étagère ne possède une quantité suffisante de ce produit
     */
    public void livrerProduits(Produit produit, int quantite, String livraisonPoi) throws ProduitOutOfStockException {
        livraisonController.livrerProduits(produit, quantite, livraisonPoi);
    }
}
