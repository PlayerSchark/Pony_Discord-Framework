package io.schark.pony.exception;

public class PonyRuntimeException extends RuntimeException {

    public PonyRuntimeException() {
        super();
    }

    public PonyRuntimeException(String message) {
        super(message);
    }

    public PonyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PonyRuntimeException(Throwable cause) {
        super(cause);
    }

    protected PonyRuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
