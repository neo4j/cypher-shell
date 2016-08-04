package org.neo4j.shell.parser;

import org.neo4j.shell.exception.CypherSyntaxError;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An object capable of parsing a piece of text and returning a List statements contained within.
 */
public interface StatementParser {
    /**
     * Parse the text and return a list of statements
     * @param text to parse
     * @return a List of statements
     * @throws CypherSyntaxError if a syntax error is detected
     */
    @Nonnull
    List<String> parse(@Nonnull String text) throws CypherSyntaxError;
}
