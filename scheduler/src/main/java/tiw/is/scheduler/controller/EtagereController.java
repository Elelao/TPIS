package tiw.is.scheduler.controller;

import jakarta.persistence.EntityManager;
import tiw.is.scheduler.model.Coordonnees;
import tiw.is.scheduler.model.Etagere;
import tiw.is.scheduler.repositories.EtagereRepository;
import tiw.is.scheduler.repositories.NoSuchEtagereException;

public class EtagereController {
    private final EntityManager em;
    private final EtagereRepository etagereRepository;

    public EtagereController(EntityManager em, EtagereRepository etagereRepository) {
        this.em = em;
        this.etagereRepository = etagereRepository;
    }

    public Etagere getEtagere(String serial) throws NoSuchEtagereException {
        return etagereRepository.findById(serial);
    }

    public void createEtagere(String serial, Coordonnees coordonnees) {
        em.getTransaction().begin();
        Etagere e = new Etagere("e-" + serial);
        e.setPosition(coordonnees);
        etagereRepository.save(e);
        em.getTransaction().commit();
    }
}
