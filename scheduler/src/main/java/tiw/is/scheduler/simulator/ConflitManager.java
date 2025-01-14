package tiw.is.scheduler.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;

import java.util.Collection;
import java.util.Random;

import static tiw.is.scheduler.model.Grille.GRILLE_X;
import static tiw.is.scheduler.model.Grille.GRILLE_Y;

/**
 * Classe responsable de la gestion d'un chargement ou d'une livraison.
 * Reçoit des commandes du simulateur, trouve une étagère capable de la réaliser, et planifie l'opération dessus.
 */
public class ConflitManager {
    private static final Logger log = LoggerFactory.getLogger(ConflitManager.class);

    private static final Random random = new Random();

    /**
     * Met à jour les étagères en conflit avec une étagère donnée,
     * en fonction de leur position courante et de leur prochaine destination.
     * @param etagere1 L'étagère avec laquelle on recherche les conflits
     * @param etageres La collection d'étagères dans laquelle rechercher (sans les étagères sur lesquelles on a déjà itéré)
     */
    public void updateConflits(Etagere etagere1, Collection<Etagere> etageres)  {
        for (Etagere etagere2 : etageres) {
            if (etagere1.hasConflitWith(etagere2)) {
                if (!detectFurtherConflit(etagere1, etagere2)) {
                    etagere1.removeConflit(etagere2);
                    etagere2.removeConflit(etagere1);
                    log.info("Conflit résolu entre {}, et {}", etagere1.getSerialNumber(), etagere2.getSerialNumber());
                } else {
                    // On force la symétrie en représentant deux fois le conflit, une dans chaque sens
                    // Pas très élégant au niveau relationnel (duplication d'information), mais simplifie les requêtes.
                    etagere1.addConflit(etagere2);
                    etagere2.addConflit(etagere1);
                    log.info("Conflit detecté entre {}, et {}", etagere1.getSerialNumber(), etagere2.getSerialNumber());

//                    resolveConflit(etagere1, etagere2);
                }
            }
        }
    }

    /**
     * Change la destination d'une étagère en cas de conflit entre 2 étagères.
     * @param etagere1 L'une des étagères en conflit
     * @param etagere2 L'autre étagère en conflit
     */
    public void resolveConflit(Etagere etagere1, Etagere etagere2) {
        if (etagere1.getNextDestination() == null) {
            // L'étagère 1 n'a pas de nouvelle destination
            etagere1.getService().planDeplacement(randomDestination());
        } else {
            // On change arbitrairement la prochaine destination de l'étagère 2
            etagere2.getService().orderDeplacement(randomDestination());
        }
    }

    /**
     * Recherche un potentiel conflit entre 2 étagères au prochain mouvement.
     * @param etagere1 L'une des étagères entre lesquelles rechercher un conflit
     * @param etagere2 L'autre des étagères entre lesquelles rechercher un conflit
     * @return Un booléen qui indique si un conflit serait présent au prochain mouvement
     */
    boolean detectFurtherConflit(Etagere etagere1, Etagere etagere2) {
        Coordonnees mv1 = etagere1.getService().getNextMove();
        Coordonnees mv2 = etagere2.getService().getNextMove();
        return mv1.equals(mv2) || etagere1.getPosition().equals(mv2) || etagere2.getPosition().equals(mv1);
    }

    /**
     * Génère une position aléatoire.
     * @return Un objet Coordonnees positionné aléatoirement dans les limites de la grille
     */
    private Coordonnees randomDestination() {
        int x = random.nextInt(GRILLE_X);
        int y = random.nextInt(GRILLE_Y);
        return new Coordonnees(x, y);
    }
}
