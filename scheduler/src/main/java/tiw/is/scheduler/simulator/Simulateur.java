package tiw.is.scheduler.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Simulateur extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Simulateur.class);

    private final ConflitManager conflitManager = new ConflitManager();
    private final Collection<Etagere> etageres;

    private volatile boolean continuation = true;

    public Simulateur(Collection<Etagere> etageres) {
        this.etageres = etageres;
    }

    /**
     * Démarrage du simulateur.
     * Doit être appelée avec <code>start()</code>.
     */
    public void run() {
        while (continuation) {
            avance();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Arrêt (à la fin d'une itération) du simulateur.
     */
    public void stopSimulation() {
        continuation = false;
        log.info("Le simulateur va s'arrêter...");
    }

    /**
     * Réalise une étape de la simulation.
     */
    void avance() {
        log.info("Simulateur avance");
        synchronized (etageres) {
            // On duplique la collection pour éviter une ConcurrentModificationException
            List<Etagere> etageres2 = new ArrayList<>(etageres);
            for (Etagere etagere : etageres) {

                // On supprime l'étagère courante pour éviter les doublons de recherches ou avec l'étagère elle-même
                etageres2.remove(etagere);
                conflitManager.updateConflits(etagere, etageres2);
                if (etagere.hasConflit()) {
                    etagere.getConflits().forEach(etagere2 -> {
                        conflitManager.resolveConflit(etagere, etagere2);
                        conflitManager.updateConflits(etagere, etageres2);
                    });
                } else if (etagere.getNextDestination() != null) {
                    Coordonnees nm = etagere.getService().move();
                    if (nm.equals(etagere.getNextDestination())) {
                        etagere.getService().removeDeplacement();
                        log.info("Etagere " + etagere.getSerialNumber() + " arrivée à destination.");
                    }
                }
            }
        }
    }
}
