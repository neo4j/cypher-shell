package org.neo4j.shell.parser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.exception.IncompleteStatementException;
import org.neo4j.shell.exception.UnconsumedStatementException;
import org.neo4j.shell.parser.StatementParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatementParserTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private StatementParser parser;

    @Before
    public void setup() {
        parser = new StatementParser();
    }

    @Test
    public void emptyWillThrow() throws Exception {
        thrown.expect(IncompleteStatementException.class);
        assertFalse(parser.isStatementComplete());
        parser.consumeStatement();
    }

    @Test
    public void singleCommandParses() throws Exception {
        assertFalse(parser.isStatementComplete());
        parser.parseLine(":help exit\n");
        assertTrue(parser.isStatementComplete());
        assertEquals(":help exit\n", parser.consumeStatement());
        // Repeat to make sure state got reset
        assertFalse(parser.isStatementComplete());
        parser.parseLine(":help bob\n");
        assertTrue(parser.isStatementComplete());
        assertEquals(":help bob\n", parser.consumeStatement());
    }

    @Test
    public void unconsumedStatementThrowsOnParse() throws Exception {
        assertFalse(parser.isStatementComplete());
        parser.parseLine(":help exit\n");
        assertTrue(parser.isStatementComplete());

        thrown.expect(UnconsumedStatementException.class);
        parser.parseLine(":help bob\n");
    }

    @Test
    public void getPromptChangesWithContext() throws Exception {
        assertFalse(parser.isStatementComplete());
        String emptyPrompt = parser.getPrompt().plainString();
        assertEquals("neo4j> ", emptyPrompt);

        parser.parseLine("CREATE\n");

        assertFalse(parser.isStatementComplete());

        String multiPrompt = parser.getPrompt().plainString();

        assertEquals("...", multiPrompt);

        // For alignment, they should match in length
        assertEquals("Expected both prompts to have equal length", emptyPrompt.length(), multiPrompt.length());
    }
}
