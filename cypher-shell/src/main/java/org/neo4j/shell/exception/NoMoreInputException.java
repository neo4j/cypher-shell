package org.neo4j.shell.exception;

/**
 * Signifies that the user hit CTRL-D, or we simply ran out of file.
 * Should many times exit gracefully.
 */
public class NoMoreInputException extends Exception {
}
