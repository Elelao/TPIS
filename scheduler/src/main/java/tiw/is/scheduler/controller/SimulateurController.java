package tiw.is.scheduler.controller;

import tiw.is.scheduler.simulator.Simulateur;

public class SimulateurController {
    private Simulateur simulateur;

    public void setSimulateur(Simulateur simulateur) {
        this.simulateur = simulateur;
    }

    /**
     * Démarre le simulateur (dans une autre thread).
     */
    public void startSimulateur() {
        simulateur.start();
    }

    /**
     * Arrête du simulateur (asynchrone)
     */
    public void stopSimulateur() {
        simulateur.stopSimulation();
    }
}
