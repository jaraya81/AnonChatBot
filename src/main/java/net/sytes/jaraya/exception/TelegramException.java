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

  }
