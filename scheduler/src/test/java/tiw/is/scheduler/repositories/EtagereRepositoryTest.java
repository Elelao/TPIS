package tiw.is.scheduler.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.utils.MustBeInTransactionException;
import tiw.is.scheduler.utils.TestPersistenceManager;

import static org.junit.jupiter.api.Assertions.*;

class EtagereRepositoryTest {

    private EntityManager em;
    private EtagereRepository repo;
    private int idTest = 0;
    private String baseId;
    private Etagere etagere;
    private Produit p1;
    private Produit p2;


    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        repo = new EtagereRepository(em);
        idTest++;
        baseId = "ert-" + idTest;
    }

    @AfterEach
    void tearDown() {
        repo = null;
        em.close();
    }

    /* Créée une étagère avec deux produits. Utilisé pour tester avec des données existantes */
    private void setupEtagereProduits() {
        String serial = "etagere-" + baseId;
        etagere = new Etagere(serial);
        p1 = new Produit(serial + "-1", "desc1");
        p2 = new Produit(serial + "-2", "desc2");
        try {
            etagere.setQuantiteProduit(p1, 3);
            etagere.setQuantiteProduit(p2, 2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        em.getTransaction().begin();
        em.persist(etagere);
        em.persist(p1);
        em.persist(p2);
        em.getTransaction().commit();
    }

    @Test
    void testCreate() {
        var serial = "etagere-" + baseId;
        Etagere e = new Etagere(serial);
        em.getTransaction().begin();
        repo.save(e);
        em.getTransaction().commit();
        assertNotNull(em.find(Etagere.class, e.getSerialNumber()));
    }

    @Test
    void testFind() {
        setupEtagereProduits();
        assertDoesNotThrow(() -> repo.findById(etagere.getSerialNumber()));
    }

    @Test
    void testAddQuantites() throws NoSuchEtagereException {
        setupEtagereProduits();
        em.detach(etagere);
        var et2 = repo.findById(etagere.getSerialNumber());
        assertEquals(2, et2.getProduitList().size());
        assertEquals(3, et2.getQuantiteProduit(p1));
    }

    @Test
    void testRemoveQuantites() throws NoSuchEtagereException {
        setupEtagereProduits();
        var et2 = repo.findById(etagere.getSerialNumber());
        em.getTransaction().begin();
        try {
            et2.setQuantiteProduit(p1, 5);
            et2.setQuantiteProduit(p2, 0);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        em.getTransaction().commit();
        em.detach(et2);
        var et3 = repo.findById(etagere.getSerialNumber());
        assertEquals(1, et3.getProduitList().size());
        assertEquals(5, et3.getQuantiteProduit(p1));
        assertEquals(0, et3.getQuantiteProduit(p2));
    }

    @Test
    void testMoveAndGetByPosition() throws NoSuchEtagereException {
        setupEtagereProduits();
        em.getTransaction().begin();
        etagere.setPosition(new Coordonnees(3, 4));
        em.getTransaction().commit();
        assertEquals(etagere, repo.getByPosition(new Coordonnees(3, 4)));
        assertThrows(NoSuchEtagereException.class,
                () -> repo.getByPosition(new Coordonnees(5, 10)));
    }

    @Test
    void testTransactionErrors() {
        assertThrows(MustBeInTransactionException.class,
                () -> repo.save(etagere));
    }

    @Test
    void testGetAll() {
        setupEtagereProduits();
        em.getTransaction().begin();
        Etagere etagere2 = new Etagere("etagere2-" + baseId);
        em.persist(etagere2);
        em.getTransaction().commit();
        var all = repo.getAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(etagere));
        assertTrue(all.contains(etagere2));
    }
}
