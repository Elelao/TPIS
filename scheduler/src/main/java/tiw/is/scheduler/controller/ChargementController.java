package tiw.is.scheduler.controller;

import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.services.NoSpaceAvailableException;
import tiw.is.scheduler.services.PlanificateurService;

public class ChargementController {
    private final PlanificateurService planificateurService;

    public ChargementController(PlanificateurService planificateurService) {
        this.planificateurService = planificateurService;
    }

    public void chargerProduits(Produit produit, int quantite) throws NoSpaceAvailableException {
        planificateurService.planChargement(produit, quantite);
    }

}
