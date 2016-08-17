package org.neo4j.shell.parser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

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
    public void parseEmptyLineDoesNothing() throws Exception {
        // when
        parser.parseMoreText("\n");

        // then
        assertFalse(parser.containsText());
        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseAShellCommand() throws Exception {
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
    public void parseAShellCommandWithNewLine() throws Exception {
        // when
        parser.parseMoreText(":help exit bob snob\n");

        // then
        assertFalse(parser.containsText());
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(":help exit bob snob\n", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseIncompleteCypher() throws Exception {
        // when
        parser.parseMoreText("CREATE ()\n");

        // then
        assertTrue(parser.containsText());
        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseCompleteCypher() throws Exception {
        // when
        parser.parseMoreText("CREATE (n)\n");
        assertTrue(parser.containsText());
        parser.parseMoreText("CREATE ();");
        assertFalse(parser.containsText());

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("CREATE (n)\nCREATE ();", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void parseMultipleCypherSingleLine() throws Exception {
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
    public void parseMultipleCypherMultipleLine() throws Exception {
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
    public void singleQuotedSemicolon() throws Exception {
        // when
        parser.parseMoreText("hello '\n");
        parser.parseMoreText(";\n");
        parser.parseMoreText("'\n");
        parser.parseMoreText(";\n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("hello '\n;\n'\n;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void backtickQuotedSemicolon() throws Exception {
        // when
        parser.parseMoreText("hello `\n");
        parser.parseMoreText(";\n");
        parser.parseMoreText("`\n");
        parser.parseMoreText(";  \n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("hello `\n;\n`\n;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void doubleQuotedSemicolon() throws Exception {
        // when
        parser.parseMoreText("hello \"\n");
        parser.parseMoreText(";\n");
        parser.parseMoreText("\"\n");
        parser.parseMoreText(";   \n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("hello \"\n;\n\"\n;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void escapedChars() throws Exception {
        // when
        parser.parseMoreText("one \\;\n");
        parser.parseMoreText("\"two \\\"\n");
        parser.parseMoreText(";\n");
        parser.parseMoreText("\";\n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("one \\;\n\"two \\\"\n;\n\";", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void nestedQuoting() throws Exception {
        // when
        parser.parseMoreText("go `tick;'single;\"double;\n");
        parser.parseMoreText("end`;\n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("go `tick;'single;\"double;\nend`;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void mixCommandAndCypherWithSpacingsAdded() throws Exception {
        // when
        parser.parseMoreText(" :help me \n");
        parser.parseMoreText(" cypher me up \n");
        parser.parseMoreText(" :scotty \n");
        parser.parseMoreText(" ; \n");
        parser.parseMoreText(" :do it now! \n");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(3, statements.size());
        assertEquals(" :help me \n", statements.get(0));
        assertEquals(" cypher me up \n :scotty \n ;", statements.get(1));
        assertEquals(" :do it now! \n", statements.get(2));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void commentHandlingIfSemicolon() throws Exception {
        // when
        parser.parseMoreText(" first // ;\n");
        parser.parseMoreText("// /* ;\n");
        parser.parseMoreText(" third ; // actually a semicolon here\n");

        // then
        assertTrue(parser.hasStatements());
        assertTrue(parser.containsText());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals(" first // ;\n// /* ;\n third ;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
    }

    @Test
    public void backslashDeadInBlockQuote() throws Exception {
        // when
        parser.parseMoreText("/* block \\*/\nCREATE ();");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("/* block \\*/\nCREATE ();", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void commentInQuote() throws Exception {
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
    public void blockCommentInQuote() throws Exception {
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
    public void quoteInComment() throws Exception {
        // when
        parser.parseMoreText("// `;\n;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("// `;\n;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void quoteInBlockomment() throws Exception {
        // when
        parser.parseMoreText("/* `;\n;*/\n;");

        // then
        assertTrue(parser.hasStatements());

        List<String> statements = parser.consumeStatements();

        assertEquals(1, statements.size());
        assertEquals("/* `;\n;*/\n;", statements.get(0));

        assertFalse(parser.hasStatements());
        assertEquals(0, parser.consumeStatements().size());
        assertFalse(parser.containsText());
    }

    @Test
    public void testReset() throws Exception {
        // given
        parser.parseMoreText("/* `;\n;*/\n;");
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
