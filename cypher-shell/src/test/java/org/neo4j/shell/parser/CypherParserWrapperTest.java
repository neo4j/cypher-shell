package org.neo4j.shell.parser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.exception.IncompleteCypherError;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherParserWrapperTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
    }

    @Test
    public void testSingleStatements() throws Exception {
        // given
        final String cypher = "CREATE ()";

        // when
        List<String> statements = CypherParserWrapper.parse(cypher);

        // then
        assertEquals(1, statements.size());
        assertEquals(cypher, statements.get(0));
    }

    @Test
    public void testMultipleStatements() throws Exception {
        // given
        final String cypher = "CREATE ()";
        final String allCyphers = String.join(";", cypher, cypher, cypher);

        // when
        List<String> statements = CypherParserWrapper.parse(allCyphers);

        // then
        assertEquals(3, statements.size());
        assertEquals(cypher + ";", statements.get(0));
    }

    @Test
    public void testMultipleTricky() throws Exception {
        // given
        final String cypher =
                "MATCH (m:BOB) RETURN m MATCH (n:NOB) RETURN n";

        // when
        List<String> statements = CypherParserWrapper.parse(cypher);

        // then
        assertEquals(2, statements.size());
        assertEquals("MATCH (m:BOB) RETURN m ", statements.get(0));
        assertEquals("MATCH (n:NOB) RETURN n", statements.get(1));
    }

    @Test
    public void testMultipleReallyTricky() throws Exception {
        // given
        final String cypher =
                "RETURN 1 RETURN 2 RETURN 3";

        // when
        List<String> statements = CypherParserWrapper.parse(cypher);

        // then
        assertEquals(3, statements.size());
        assertEquals("RETURN 1 ", statements.get(0));
        assertEquals("RETURN 2 ", statements.get(1));
        assertEquals("RETURN 3", statements.get(2));
    }

    @Test
    public void testIncompleteSimple() throws Exception {
        // then
        thrown.expect(IncompleteCypherError.class);

        // given
        final String cypher = "RETURN";

        // when
        CypherParserWrapper.parse(cypher);
    }

    @Test
    public void testComment() throws Exception {
        // given
        final String cypher = "// Ignore this line \n" +
                "RETURN 1";

        // when
        List<String> statements = CypherParserWrapper.parse(cypher);

        // then
        assertEquals(1, statements.size());
        assertEquals("// Ignore this line \n" +
                "RETURN 1", statements.get(0));
    }

    @Test
    public void testBlockComment() throws Exception {
        // given
        final String cypher = "/* Ignore this part */ RETURN 1";

        // when
        List<String> statements = CypherParserWrapper.parse(cypher);

        // then
        assertEquals(1, statements.size());
        assertEquals("/* Ignore this part */ RETURN 1", statements.get(0));
    }

    @Test
    public void parseSmallTest() throws Exception {
        // given
        List<String> lines =
                Files.readAllLines(Paths.get(CypherParserWrapperTest.class.getResource("small-test.cypher").toURI()));

        // when
        List<String> statements = CypherParserWrapper.parse(String.join("\n", lines));

        // then
        assertEquals(11, statements.size());
    }

    @Test
    public void parseTortureTest() throws Exception {
        // given
        List<String> lines =
                Files.readAllLines(Paths.get(CypherParserWrapperTest.class.getResource("torture-test.cypher").toURI()));

        // when
        List<String> statements = CypherParserWrapper.parse(String.join("\n", lines));

        // then
        assertEquals(8, statements.size());
    }

    @Test
    public void parseGraphGems() throws Exception {
        // given
        List<String> lines =
                Files.readAllLines(Paths.get(CypherParserWrapperTest.class.getResource("graphgems.cypher").toURI()));

        // when
        List<String> statements = CypherParserWrapper.parse(String.join("\n", lines));

        // then
        assertEquals(8, statements.size());
    }
}
