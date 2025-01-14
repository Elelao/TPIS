package tiw.is.scheduler.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.utils.MustBeInTransactionException;
import tiw.is.scheduler.utils.TestPersistenceManager;

import static org.junit.jupiter.api.Assertions.*;

class ProduitRepositoryTest {
    private EntityManager em;
    private ProduitRepository repo;
    private int idTest = 0;
    private String baseId;
    private Produit produit = null;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        repo = new ProduitRepository(em);
        baseId = "prt-" + (++idTest);
    }

    @AfterEach
    void tearDown() {
        em.close();
    }

    private void createProduit() {
        produit = new Produit("produit-" + baseId, "description: " + baseId);
        em.getTransaction().begin();
        em.persist(produit);
        em.getTransaction().commit();
    }

    @Test
    void testSave() {
        Produit p = new Produit("produit-" + baseId, "description: " + baseId);
        em.getTransaction().begin();
        repo.save(p);
        em.getTransaction().commit();
        assertNotNull(em.find(Produit.class, p.code()));
    }

    @Test
    void testFindById() {
        createProduit();
        assertEquals(produit, repo.findById(produit.code()));
    }

    @Test
    void failOutsideTransaction() {
        Produit p = new Produit("produit-" + baseId, "description: " + baseId);
        assertThrows(MustBeInTransactionException.class, () -> repo.save(p));
    }
}
