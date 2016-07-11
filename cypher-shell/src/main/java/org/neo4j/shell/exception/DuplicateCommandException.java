package org.neo4j.shell.exception;

public class DuplicateCommandException extends RuntimeException {
    public DuplicateCommandException(String s) {
        super(s);
    }
}
