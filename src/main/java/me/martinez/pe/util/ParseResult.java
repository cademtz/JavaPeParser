package me.martinez.pe.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Store a {@link ParseError} or an "ok" object
 * @param <TOk> Object to store when the result is "ok"
 */
public class ParseResult<TOk> {
    private final Object value;
    private final boolean isOk;

    private ParseResult(Object value, boolean isOk) {
        this.value = value;
        this.isOk = isOk;
    }

    public static <TOk> ParseResult<TOk> ok(TOk ok) {
        return new ParseResult<>(ok, true);
    }

    public static <TOk> ParseResult<TOk> err(ParseError err) {
        return new ParseResult<>(err, false);
    }

    public static <TOk> ParseResult<TOk> err(String reason, Exception e) {
        return new ParseResult<>(new ParseError(reason, e), false);
    }

    public static <TOk> ParseResult<TOk> err(String reason) {
        return ParseResult.err(reason, null);
    }

    public boolean isOk() {
        return isOk;
    }

    public boolean isErr() {
        return !isOk;
    }

    public TOk getOk() {
        if (!isOk())
            throw new NullPointerException("No ok value");
        return (TOk) value;
    }

    public ParseError getErr() {
        if (!isErr())
            throw new NullPointerException("No err value");
        return (ParseError) value;
    }

    public ParseResult<TOk> ifOk(Consumer<TOk> ifOkCode) {
        if (isOk())
            ifOkCode.accept(getOk());
        return this;
    }

    public <T> T ifOkOrDefault(T default_, Function<TOk, T> ifOkCode) {
        if (isOk())
            return ifOkCode.apply(getOk());
        return default_;
    }

    public ParseResult<TOk> ifErr(Consumer<ParseError> ifErrCode) {
        if (isErr())
            ifErrCode.accept(getErr());
        return this;
    }

    public <T> T ifErrOrDefault(T default_, Function<ParseError, T> ifOkCode) {
        if (isErr())
            return ifOkCode.apply(getErr());
        return default_;
    }

    public TOk getOkOrDefault(TOk default_) {
        return isOk() ? getOk() : default_;
    }

    public ParseError getErrOrDefault(ParseError default_) {
        return isErr() ? getErr() : default_;
    }

    public TOk getOrElseDefault(TOk default_, Consumer<ParseError> elseCode) {
        if (isOk())
            return getOk();
        elseCode.accept(getErr());
        return default_;
    }

    public TOk getOrElse(Function<ParseError, TOk> elseCode) {
        return isOk() ? getOk() : elseCode.apply(getErr());
    }
}
