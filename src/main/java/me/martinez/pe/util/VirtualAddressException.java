package me.martinez.pe.util;

/**
 * Exception with a virtual address that can be retrieved.
 * Useful when a virtual address causes (or could cause) a violation.
 */
public class VirtualAddressException extends Exception {
    private final long vAddr;

    public VirtualAddressException(String message, long vAddr) {
        super(message);
        this.vAddr = vAddr;
    }

    public VirtualAddressException(Throwable cause, long vAddr) {
        super(cause);
        this.vAddr = vAddr;
    }

    public VirtualAddressException(String message, Throwable cause, long vAddr) {
        super(message, cause);
        this.vAddr = vAddr;
    }

    public long getVirtualAddress() {
        return vAddr;
    }

    @Override
    public String toString() {
        return String.format("%s (at 0x%s)", super.toString(), HexOutput.qwordToString(getVirtualAddress()));
    }
}
