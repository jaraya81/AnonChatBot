package net.sytes.jaraya.exception;

public class CoreException extends Exception {

    public CoreException(String message) {
        super(message);
    }

    public CoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public CoreException(Throwable cause) {
        super(cause);
    }

    public CoreException() {
        super();
    }

}
