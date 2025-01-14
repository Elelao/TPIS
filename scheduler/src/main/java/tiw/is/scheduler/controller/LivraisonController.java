package tiw.is.scheduler.controller;

import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.services.PlanificateurService;
import tiw.is.scheduler.services.ProduitOutOfStockException;

public class LivraisonController {
    private final PlanificateurService planificateurService;

    public LivraisonController(PlanificateurService planificateurService) {
        this.planificateurService = planificateurService;
    }

    public void livrerProduits(Produit produit, int quantite, String livraisonPoi) throws ProduitOutOfStockException {
        planificateurService.planLivraison(produit, quantite, livraisonPoi);
    }
}
