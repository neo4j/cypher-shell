package org.neo4j.shell.exception;

public class ExitException extends Error {
    private final int code;

    public ExitException(int code) {
        super();
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
