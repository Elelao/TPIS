package tiw.is.scheduler.repositories;

import tiw.is.scheduler.model.Coordonnees;

public class NoSuchEtagereException extends Exception {
    public NoSuchEtagereException(String serial) {
        super("Pas d'étagère '" + serial + "'");
    }
    public NoSuchEtagereException(Coordonnees position) {
        super("Pas d'étagère à la position '" + position + "'");
    }
}
