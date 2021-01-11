package net.sytes.jaraya.exception;

public class TelegramException extends CoreException {

    public TelegramException() {
    }

    public TelegramException(String message) {
        super(message);
    }

    public TelegramException(String message, Throwable cause) {
        super(message, cause);
    }

    public TelegramException(Throwable cause) {
        super(cause);
    }

    public static void throwIt(String cause) throws TelegramException {
        throw new TelegramException(cause);
    }

    public static void throwIt(Throwable e) throws TelegramException {
        throw new TelegramException(e);
    }
}
