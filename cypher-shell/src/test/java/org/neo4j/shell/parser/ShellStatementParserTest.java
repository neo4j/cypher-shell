package org.neo4j.shell.parser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellStatementParserTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private ShellStatementParser parser;

    @Before
    public void setup() {
        parser = new ShellStatementParser();
    }

    @Test
    public void parseEmptyLineDoesNothing() {
        // when
        parser.parseMoreText(lineSeparator());

        // then
        assertFalse(parser.containsText());
        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseAShellCommand() {
        // when
        parser.parseMoreText("  :help exit bob snob  ");

        // then
        assertFalse(parser.containsText());
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("  :help exit bob snob  ", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseAShellCommandWithNewLine() {
        // when
        parser.parseMoreText(format(":help exit bob snob%n"));

        // then
        assertFalse(parser.containsText());
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format(":help exit bob snob%n"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseIncompleteCypher() {
        // when
        parser.parseMoreText(format("CREATE ()%n"));

        // then
        assertTrue(parser.containsText());
        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseCompleteCypher() {
        // when
        parser.parseMoreText(format("CREATE (n)%n"));
        assertTrue(parser.containsText());
        parser.parseMoreText("CREATE ();");
        assertFalse(parser.containsText());

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("CREATE (n)%nCREATE ();"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseMultipleCypherSingleLine() {
        // when
        parser.parseMoreText("RETURN 1;RETURN 2;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(2, statements.size());
        assertEquals("RETURN 1;", statements.get(0));
        assertEquals("RETURN 2;", statements.get(1));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void parseMultipleCypherMultipleLine() {
        // when
        parser.parseMoreText("RETURN 1;");
        parser.parseMoreText("RETURN 2;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(2, statements.size());
        assertEquals("RETURN 1;", statements.get(0));
        assertEquals("RETURN 2;", statements.get(1));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void singleQuotedSemicolon() {
        // when
        parser.parseMoreText(format("hello '%n"));
        parser.parseMoreText(format(";%n"));
        parser.parseMoreText(format("'%n"));
        parser.parseMoreText(format(";%n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("hello '%n;%n'%n;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void backtickQuotedSemicolon() {
        // when
        parser.parseMoreText(format("hello `%n"));
        parser.parseMoreText(format(";%n"));
        parser.parseMoreText(format("`%n"));
        parser.parseMoreText(format(";  %n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("hello `%n;%n`%n;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void doubleQuotedSemicolon() {
        // when
        parser.parseMoreText(format("hello \"%n"));
        parser.parseMoreText(format(";%n"));
        parser.parseMoreText(format("\"%n"));
        parser.parseMoreText(format(";   %n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("hello \"%n;%n\"%n;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void escapedChars() {
        // when
        parser.parseMoreText(format("one \\;%n"));
        parser.parseMoreText(format("\"two \\\"%n"));
        parser.parseMoreText(format(";%n"));
        parser.parseMoreText(format("\";%n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("one \\;%n\"two \\\"%n;%n\";"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void nestedQuoting() {
        // when
        parser.parseMoreText(format("go `tick;'single;\"double;%n"));
        parser.parseMoreText(format("end`;%n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("go `tick;'single;\"double;%nend`;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void mixCommandAndCypherWithSpacingsAdded() {
        // when
        parser.parseMoreText(format(" :help me %n"));
        parser.parseMoreText(format(" cypher me up %n"));
        parser.parseMoreText(format(" :scotty %n"));
        parser.parseMoreText(format(" ; %n"));
        parser.parseMoreText(format(" :do it now! %n"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(3, statements.size());
        assertEquals(format(" :help me %n"), statements.get(0));
        assertEquals(format(" cypher me up %n :scotty %n ;"), statements.get(1));
        assertEquals(format(" :do it now! %n"), statements.get(2));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void commentHandlingIfSemicolon() {
        // when
        parser.parseMoreText(format(" first // ;%n"));
        parser.parseMoreText(format("// /* ;%n"));
        parser.parseMoreText(format(" third ; // actually a semicolon here%n"));

        // then
        assertTrue(parser.hasStatements());
        assertTrue(parser.containsText());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format(" first // ;%n// /* ;%n third ;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void backslashDeadInBlockQuote() {
        // when
        parser.parseMoreText(format("/* block \\*/%nCREATE ();"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("/* block \\*/%nCREATE ();"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void commentInQuote() {
        // when
        parser.parseMoreText("` here // comment `;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("` here // comment `;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void blockCommentInQuote() {
        // when
        parser.parseMoreText("` here /* comment `;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("` here /* comment `;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void quoteInComment() {
        // when
        parser.parseMoreText(format("// `;%n;"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("// `;%n;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void quoteInBlockomment() {
        // when
        parser.parseMoreText(format("/* `;%n;*/%n;"));

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(format("/* `;%n;*/%n;"), statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void testReset() {
        // given
        parser.parseMoreText(format("/* `;%n;*/%n;"));
        parser.parseMoreText("bob");
        assertTrue(parser.hasStatements());
        assertTrue(parser.containsText());

        // when
        parser.reset();
        
        // then
        assertFalse(parser.hasStatements());
        assertFalse(parser.containsText());
    }
}
