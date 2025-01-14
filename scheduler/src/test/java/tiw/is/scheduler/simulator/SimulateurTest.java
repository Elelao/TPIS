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

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class SimulateurTest {

    private EntityManager em;
    private EtagereRepository etagereRepository;
    private Simulateur simulateur;
    private int testId = 0;
    private String idBase;
    private String[] serials;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        etagereRepository = new EtagereRepository(em);
        idBase = "st-" + (++testId);
    }

    @AfterEach
    void tearDown() {
        em.close();
    }

    private void initData() {
        em.getTransaction().begin();
        Collection<Etagere> etageres = etagereRepository.getAll();
        for (var et : etageres) {
            em.remove(et);
        }
        em.getTransaction().commit();
        em.getTransaction().begin();
        int nbEtageres = 3;
        serials = new String[nbEtageres];
        etageres = new ArrayList<>();
        for(int i = 0; i < nbEtageres; i++) {
            serials[i] = "etagere" + i+"-"+idBase;
            Etagere etagere = new Etagere(serials[i]);
            etagere.setPosition(new Coordonnees(i+1,i+1));
            etagere.getService().planDeplacement(new Coordonnees(i+2, i+1));
            etageres.add(etagere);
            em.persist(etagere);
        }
        simulateur = new Simulateur(etageres);
        em.getTransaction().commit();
    }

    @Test
    void testAvance() throws NoSuchEtagereException {
        initData();
        em.getTransaction().begin();
        simulateur.avance();
        em.getTransaction().commit();
        assertEquals(new Coordonnees(2,1), etagereRepository.findById(serials[0]).getPosition());
        assertEquals(new Coordonnees(3,2), etagereRepository.findById(serials[1]).getPosition());
        assertEquals(new Coordonnees(4,3), etagereRepository.findById(serials[2]).getPosition());
        em.getTransaction().begin();
        simulateur.avance();
        em.getTransaction().commit();
        assertEquals(new Coordonnees(2,1), etagereRepository.findById(serials[0]).getPosition());
        assertEquals(new Coordonnees(3,2), etagereRepository.findById(serials[1]).getPosition());
        assertEquals(new Coordonnees(4,3), etagereRepository.findById(serials[2]).getPosition());
    }

    @Test
    void testRun() {
        initData();
        assertDoesNotThrow(() -> {
            simulateur.start();
            Thread.sleep(5000);
            simulateur.stopSimulation();
        });
    }
}