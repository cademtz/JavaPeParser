package me.martinez.pe.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class ParseResult<TOk> {
    private final Object value;
    private final boolean isOk;

    protected ParseResult(Object value, boolean isOk) {
        this.value = value;
        this.isOk = isOk;
    }

    public static <TOk> ParseResult<TOk> ok(TOk ok) {
        return new ParseResult<>(ok, true);
    }

    public static <TOk> ParseResult<TOk> err(GenericError err) {
        return new ParseResult<>(err, false);
    }

    public static <TOk> ParseResult<TOk> err(String reason, Exception e) {
        return new ParseResult<>(new GenericError(reason, e), false);
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

    public GenericError getErr() {
        if (!isErr())
            throw new NullPointerException("No err value");
        return (GenericError) value;
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

    public ParseResult<TOk> ifErr(Consumer<GenericError> ifErrCode) {
        if (isErr())
            ifErrCode.accept(getErr());
        return this;
    }

    public <T> T ifErrOrDefault(T default_, Function<GenericError, T> ifOkCode) {
        if (isErr())
            return ifOkCode.apply(getErr());
        return default_;
    }

    public TOk getOkOrDefault(TOk default_) {
        return isOk() ? getOk() : default_;
    }

    public GenericError getErrOrDefault(GenericError default_) {
        return isErr() ? getErr() : default_;
    }

    public TOk getOrElseDefault(TOk default_, Consumer<GenericError> elseCode) {
        if (isOk())
            return getOk();
        elseCode.accept(getErr());
        return default_;
    }

    public TOk getOrElse(Function<GenericError, TOk> elseCode) {
        return isOk() ? getOk() : elseCode.apply(getErr());
    }
}
