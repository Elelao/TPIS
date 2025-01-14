package tiw.is.scheduler.services;

public class NoSpaceAvailableException extends Exception {
    public NoSpaceAvailableException(String message) {
        super(message);
    }
}
