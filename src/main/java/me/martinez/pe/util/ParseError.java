package me.martinez.pe.util;

/**
 * Store an error reason and optional exception
 */
public class ParseError {
    private final String reason;
    private final Exception exception;

    public ParseError(String reason, Exception e) {
        this.reason = reason;
        this.exception = e;
    }

    public ParseError(String reason) {
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
