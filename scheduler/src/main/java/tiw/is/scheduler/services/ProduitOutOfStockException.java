package tiw.is.scheduler.services;

public class ProduitOutOfStockException extends RuntimeException {
    public ProduitOutOfStockException(String message) {
        super(message);
    }
}
