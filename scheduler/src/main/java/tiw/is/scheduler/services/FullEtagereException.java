package tiw.is.scheduler.services;

public class FullEtagereException extends Exception {
    public FullEtagereException(String etagere) {
        super("L'étagère " + etagere + " ne peut pas contenir autant de produits.");
    }
}
