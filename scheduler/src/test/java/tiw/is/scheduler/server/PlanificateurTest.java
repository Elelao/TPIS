package tiw.is.scheduler.server;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tiw.is.scheduler.model.*;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.repositories.NoSuchEtagereException;
import tiw.is.scheduler.repositories.ProduitRepository;
import tiw.is.scheduler.services.NoSpaceAvailableException;
import tiw.is.scheduler.services.ProduitOutOfStockException;
import tiw.is.scheduler.utils.MustBeInTransactionException;
import tiw.is.scheduler.utils.TestPersistenceManager;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PlanificateurTest {
    private EntityManager em;
    private EtagereRepository etagereRepository;
    private ProduitRepository produitRepository;
    private Planificateur planificateur;
    private int testId = 2;
    private String idBase;
    private String[] serials;
    private Produit[] produits;

    @BeforeEach
    void setUp() {
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        em = emf.createEntityManager();
        etagereRepository = new EtagereRepository(em);
        produitRepository = new ProduitRepository(em);
        planificateur = new Planificateur(em, etagereRepository);
        idBase = "pt-" + (++testId);
    }

    @AfterEach
    void tearDown() {
        em.getTransaction().begin();
        Collection<Etagere> etageres = etagereRepository.getAll();
        for (var et : etageres) {
            em.remove(et);
        }
        Collection<Produit> prods = produitRepository.getAll();
        for (var p : prods) {
            em.remove(p);
        }
        em.getTransaction().commit();
        em.close();
    }

    private void initData() {
        em.getTransaction().begin();
        Collection<Etagere> etageres = etagereRepository.getAll();
        for (var et : etageres) {
            em.remove(et);
        }
        Collection<Produit> prods = produitRepository.getAll();
        for (var p : prods) {
            em.remove(p);
        }

        int nbEtageres = 3;
        serials = new String[nbEtageres];
        for(int i = 0; i < nbEtageres; i++) {
            serials[i] = "etagere" + i+"-"+idBase;
            Etagere etagere = new Etagere(serials[i]);
            etagere.setPosition(new Coordonnees(i+1,i+1));
            etagere.getService().planDeplacement(new Coordonnees(i+2, i+1));
            em.persist(etagere);
        }

        int nbProduits = 3;
        produits = new Produit[nbProduits];
        for (int i = 0; i < nbProduits; i++) {
            produits[i] = new Produit("p-p-" + i, "Test planificateur - produit " + i);
            em.persist(produits[i]);
        }
        em.getTransaction().commit();
    }

    @Test
    void failsOutsideTransation() {
        initData();
        assertThrows(MustBeInTransactionException.class,
                () -> planificateur.chargerProduits(null, 0));
        assertThrows(MustBeInTransactionException.class,
                () -> planificateur.livrerProduits(null, 0, null));
    }

    @Test
    void initEtageres() throws NoSuchEtagereException {
        PointOfInterest poi = new PointOfInterest(PoiType.PARKING, new Coordonnees(0, 4));
        assertDoesNotThrow(() -> {
            planificateur.initEtageres();
        });
        assertEquals(poi.coordonnees(), etagereRepository.findById("e-P0").getPosition());
    }

    @Test
    void chargerProduits() throws NoSuchEtagereException {
        initData();
        planificateur.initEtageres();
        planificateur.startSimulateur();
        em.getTransaction().begin();
        assertDoesNotThrow(() -> planificateur.chargerProduits(produits[0], 50));
        assertThrows(NoSpaceAvailableException.class, () -> planificateur.chargerProduits(produits[1], 101));
        assertEquals(50, etagereRepository.findById(serials[0]).getQuantiteProduit(produits[0]));
        planificateur.stopSimulateur();
        em.getTransaction().commit();
    }

    @Test
    void livrerProduits() throws NoSpaceAvailableException {
        initData();
        planificateur.initEtageres();
        planificateur.startSimulateur();
        em.getTransaction().begin();
        planificateur.chargerProduits(produits[0], 50);
        assertDoesNotThrow(() -> planificateur.livrerProduits(produits[0], 10, "L0"));
        assertThrows(ProduitOutOfStockException.class, () -> planificateur.livrerProduits(produits[1], 1, "L1"));
        planificateur.stopSimulateur();
        em.getTransaction().commit();
    }
}