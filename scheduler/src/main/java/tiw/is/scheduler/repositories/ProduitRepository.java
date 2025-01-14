package tiw.is.scheduler.repositories;

import jakarta.persistence.EntityManager;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.utils.MustBeInTransactionException;

import java.util.Collection;

public class ProduitRepository {
    private final EntityManager em;

    public ProduitRepository(EntityManager em) {
        this.em = em;
    }

    /** Cherche un produit par son code */
    public Produit findById(String code) {
        return em.find(Produit.class, code);
    }

    public Collection<Produit> getAll() {
        return em.createQuery("SELECT p FROM Produit p", Produit.class).getResultList();
    }

    /** Sauvegarde un nouveau produit */
    public Produit save(Produit produit) {
        if (! em.getTransaction().isActive()) {
            throw new MustBeInTransactionException();
        }
        if (em.contains(produit)) {
            return em.merge(produit);
        } else {
            em.persist(produit);
            return produit;
        }
    }
}
