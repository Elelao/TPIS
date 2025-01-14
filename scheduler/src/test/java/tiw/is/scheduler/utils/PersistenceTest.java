package tiw.is.scheduler.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Testing the setup of an entitymanager */
class PersistenceTest {
    @Test
    void testSetupEntityManager() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
        EntityManagerFactory emf = new TestPersistenceManager().createEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        assertNotNull(em);
        em.close();
    }
}
