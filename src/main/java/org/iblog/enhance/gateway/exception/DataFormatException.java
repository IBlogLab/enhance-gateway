package org.iblog.enhance.gateway.exception;

public class DataFormatException extends RuntimeException {
    public DataFormatException(String m) {
        super(m);
    }

    public DataFormatException(Throwable t) {
        super(t);
    }

    public DataFormatException(String m, Throwable t) {
        super(m, t);
    }
}
