package tiw.is.scheduler.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.model.Produit;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.repositories.NoSuchEtagereException;
import tiw.is.scheduler.repositories.ProduitRepository;
import tiw.is.scheduler.utils.TestPersistenceManager;

import static org.junit.jupiter.api.Assertions.*;
import static tiw.is.scheduler.model.Etagere.MAX_CAPACITY;

class EtagereServiceTest {
    private EntityManager em;
    private EtagereRepository etagereRepository;
    private ProduitRepository produitRepository;
    private int testId = 0;
    private String idBase;
    private String serial1;
    private String code1;
    private String code2;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        etagereRepository = new EtagereRepository(em);
        produitRepository = new ProduitRepository(em);
        idBase = "pt-" + (++testId);
    }

    @AfterEach
    void tearDown() {
        em.close();
        etagereRepository = null;
    }

    private void initData() {
        Etagere etagere1 = new Etagere("etagere-" + idBase + "-1");
        Etagere etagere2 = new Etagere("etagere-" + idBase + "-2");
        Produit produit1 = new Produit("produit " + idBase + "-1", "produit 1");
        Produit produit2 = new Produit("produit " + idBase + "-2", "produit 2");
        em.getTransaction().begin();
        etagere1.setPosition(new Coordonnees(10, 10));
        etagere2.setPosition(new Coordonnees(2, 2));
        em.persist(etagere1);
        em.persist(etagere2);
        em.persist(produit1);
        em.persist(produit2);
        em.getTransaction().commit();
        serial1 = etagere1.getSerialNumber();
        code1 = produit1.code();
        code2 = produit2.code();
    }

    @Test
    void testNoSuchEtagereException() {
        initData();
        String nonexistant = "doesn't exists";
        assertDoesNotThrow(() -> etagereRepository.findServiceById(serial1));
        assertThrows(NoSuchEtagereException.class,
                () -> etagereRepository.findServiceById(nonexistant));
    }

    @Test
    void move() throws NoSuchEtagereException {
        initData();
        var etagere = etagereRepository.findById(serial1);
        var etagereService1 = etagereRepository.findServiceById(serial1);

        em.getTransaction().begin();
        etagereService1.planDeplacement(new Coordonnees(5, 12));
        etagereService1.move();
        em.getTransaction().commit();

        assertEquals(9, etagere.getPosition().x());
        assertEquals(11, etagere.getPosition().y());
    }

    @Test
    void orderDeplacement() throws NoSuchEtagereException {
        initData();
        var etagereService1 = etagereRepository.findServiceById(serial1);
        em.getTransaction().begin();
        etagereService1.orderDeplacement(new Coordonnees(5, 2));
        em.getTransaction().commit();
        Etagere etagere1 = etagereRepository.findById(serial1);
        assertNotNull(etagere1);
        assertEquals(1, etagere1.getDestinations().size());
    }

    @Test
    void planDeplacement() throws NoSuchEtagereException {
        initData();
        var etagereService1 = etagereRepository.findServiceById(serial1);
        em.getTransaction().begin();
        etagereService1.planDeplacement(new Coordonnees(5, 2));
        em.getTransaction().commit();
        Etagere etagere1 = etagereRepository.findById(serial1);
        assertNotNull(etagere1);
        assertEquals(1, etagere1.getDestinations().size());
    }

    @Test
    void load() throws NoSuchEtagereException {
        initData();
        var etagereService1 = etagereRepository.findServiceById(serial1);
        var produit1 = produitRepository.findById(code1);
        var produit2 = produitRepository.findById(code2);
        assertDoesNotThrow(() -> etagereService1.load(produit1, MAX_CAPACITY));
        assertThrows(FullEtagereException.class, () -> etagereService1.load(produit2, 1));
    }

    @Test
    void deliver() throws NoSuchEtagereException, FullEtagereException {
        initData();
        var etagereService1 = etagereRepository.findServiceById(serial1);
        var produit1 = produitRepository.findById(code1);
        var produit2 = produitRepository.findById(code2);
        etagereService1.load(produit1, 1);
        assertDoesNotThrow(() -> {
            etagereService1.deliver(produit2, 1);
        });
        assertThrows(MissingProduitException.class, () -> etagereService1.deliver(produit2, 1));
    }
}
