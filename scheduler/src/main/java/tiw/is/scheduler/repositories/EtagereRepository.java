package tiw.is.scheduler.repositories;

import jakarta.persistence.EntityManager;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.services.EtagereService;
import tiw.is.scheduler.utils.MustBeInTransactionException;

import java.util.Collection;

/**
 * Composant encapsulant les aspects de persistance des étageres
 */
public class EtagereRepository {

    private final EntityManager em;

    public EtagereRepository(EntityManager em) {
        this.em = em;
    }

    /**
     * Renvoie une étagère à partir de son ID.
     * @param serial Le numéro de série de l'étagère recherchée
     * @return L'instance d'étagère correspondant au numéro de série
     * @throws NoSuchEtagereException Si aucune étagère ne correspond au numéro
     */
    public Etagere findById(String serial) throws NoSuchEtagereException {
        Etagere et = em.find(Etagere.class, serial);
        if (et == null) {
            throw new NoSuchEtagereException(serial);
        } else {
            // Obligé car on est en multi-thread
            em.refresh(et);
            return et;
        }
    }

    /**
     * Renvoie le service lié à une étagère à partir de son ID.
     * @param serial Le numéro de série de l'étagère recherchée
     * @return L'instance de EtagèreService correspondant au numéro de série
     * @throws NoSuchEtagereException Si aucune étagère ne correspond au numéro
     */
    //TODO supprimer cette méthode asap et injecter la dépendance dans le constructeur
    public EtagereService findServiceById(String serial) throws NoSuchEtagereException {
        return findById(serial).getService();
    }

    /** Sauvegarde une [nouvelle] étagère */
    public Etagere save(Etagere etagere) {
        if (! em.getTransaction().isActive()) {
            throw new MustBeInTransactionException();
        }
        if (em.contains(etagere)) {
            return em.merge(etagere);
        } else {
            em.persist(etagere);
            return etagere;
        }
    }

    public Etagere getByPosition(Coordonnees position) throws
            NoSuchEtagereException {
        return em.createQuery("SELECT e FROM Etagere e WHERE e.position = :pos", Etagere.class)
                .setParameter("pos", position)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new NoSuchEtagereException(position));
    }

    public Collection<Etagere> getAll() {
        return em.createQuery("SELECT e FROM Etagere e", Etagere.class).getResultList();
    }

    public Collection<String> getAllIds() {
        return em.createQuery("SELECT e.id FROM Etagere e", String.class).getResultList();
    }
}
