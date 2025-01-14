package tiw.is.scheduler.services;

public class MissingProduitException extends Exception {
    public MissingProduitException(String etagere, String produit) {
        super("L'étagère " + etagere + " ne contient pas assez de produit " + produit + ".");
    }
}
