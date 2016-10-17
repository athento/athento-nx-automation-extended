package org.athento.nuxeo.operations.security;

/**
 * Created by victorsanchez on 18/10/16.
 */
public class RestrictionException extends Exception {

    public RestrictionException() {
    }

    public RestrictionException(String message) {
        super(message);
    }

    public RestrictionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestrictionException(Throwable cause) {
        super(cause);
    }

    public RestrictionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
