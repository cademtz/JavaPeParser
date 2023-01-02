package me.martinez.pe.util;

public class GenericError {
    private final String reason;
    private final Exception exception;

    public GenericError(String reason, Exception e) {
        this.reason = reason;
        this.exception = e;
    }

    public GenericError(String reason) {
        this(reason, null);
    }

    public boolean hasException() {
        return exception != null;
    }

    public String getReason() {
        return reason;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        String str = reason;
        if (hasException())
            str += ": " + exception.toString();
        return str;
    }
}
