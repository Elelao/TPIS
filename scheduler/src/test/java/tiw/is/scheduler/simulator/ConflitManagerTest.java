package tiw.is.scheduler.simulator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.repositories.NoSuchEtagereException;
import tiw.is.scheduler.utils.TestPersistenceManager;

import static org.junit.jupiter.api.Assertions.*;

class ConflitManagerTest {
    private ConflitManager conflitManager;
    private EtagereRepository etagereRepository;
    private EntityManager em;
    private int testId = 0;
    private String idBase;
    private String serial1;
    private String serial2;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        etagereRepository = new EtagereRepository(em);
        conflitManager = new ConflitManager();
        idBase = "pt-" + (++testId);
    }

    @AfterEach
    void tearDown() {
        em.close();
        etagereRepository = null;
        conflitManager = null;
    }

    private void initData() {
        Etagere etagere1 = new Etagere("etagere-" + idBase + "-1");
        Etagere etagere2 = new Etagere("etagere-" + idBase + "-2");
        em.getTransaction().begin();
        etagere1.setPosition(new Coordonnees(1, 1));
        etagere2.setPosition(new Coordonnees(2, 2));
        em.persist(etagere1);
        em.persist(etagere2);
        em.getTransaction().commit();
        serial1 = etagere1.getSerialNumber();
        serial2 = etagere2.getSerialNumber();
    }

    @Test
    void testNoSuchElementExceptionOccur() {
        initData();
        String nonexistant = "doesn't exists";
        assertThrows(NoSuchEtagereException.class,
                () -> etagereRepository.findServiceById(nonexistant));
    }

    @Test
    void testResolveConflit() throws NoSuchEtagereException {
        initData();
        em.getTransaction().begin();
        Etagere etagere1 = etagereRepository.findById(serial1);
        Etagere etagere2 = etagereRepository.findById(serial2);
        etagere1.setPosition(new Coordonnees(1, 2));
        etagere1.getDestinations().addFirst(etagere2.getPosition());
        em.getTransaction().commit();
        em.getTransaction().begin();
        etagere2 = etagereRepository.findById(serial2);
        conflitManager.resolveConflit(etagere1, etagere2);
        em.getTransaction().commit();
        assertNotNull(etagere2);
        assertEquals(1, etagere2.getDestinations().size());
    }

    @Test
    void detectFurtherConflit() throws NoSuchEtagereException {
        initData();
        var e1 = etagereRepository.findById(serial1);
        var e2 = etagereRepository.findById(serial2);
        e1.setPosition(new Coordonnees(1,1));
        e2.setPosition(new Coordonnees(2,1));
        assertFalse(conflitManager.detectFurtherConflit(e1, e2));
        e1.getDestinations().add(new Coordonnees(2,1));
        assertTrue(conflitManager.detectFurtherConflit(e1, e2));
        assertTrue(conflitManager.detectFurtherConflit(e1, e2));
        e2.setPosition(new Coordonnees(2,2));
        assertFalse(e1.hasConflitWith(e2));
        e2.getDestinations().add(new Coordonnees(2,1));
        assertTrue(conflitManager.detectFurtherConflit(e1, e2));
        assertTrue(conflitManager.detectFurtherConflit(e1, e2));
    }
}
