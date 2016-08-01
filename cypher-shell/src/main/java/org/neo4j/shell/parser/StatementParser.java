package org.neo4j.shell.parser;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An object capable of parsing a piece of text and returning a List statements contained within.
 */
public interface StatementParser {

    /**
     * Parse the next line of text
     *
     * @param line to parse
     */
    void parseMoreText(@Nonnull String line);

    /**
     * @return true if any statements have been parsed yet, false otherwise
     */
    boolean hasStatements();

    /**
     * Once this method has been called, the method will return the empty list (unless more text is parsed).
     * If nothing has been parsed yet, then the empty list is returned.
     *
     * @return statements which have been parsed so far and remove them from internal state
     */
    @Nonnull
    List<String> consumeStatements();

    /**
     * @return false if no text (except whitespace) has been seen since last parsed statement, true otherwise.
     */
    boolean containsText();
}
