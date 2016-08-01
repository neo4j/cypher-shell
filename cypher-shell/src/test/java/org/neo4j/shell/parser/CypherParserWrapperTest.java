package org.neo4j.shell.parser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherParserWrapperTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private CypherParserWrapper parser;

    @Before
    public void setup() {
        parser = new CypherParserWrapper();
    }

    @Test
    public void testSingleStatements() throws Exception {
        // given
        final String cypher = "CREATE ()";

        // when
        List<String> statements = parser.parse(cypher);

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
        List<String> statements = parser.parse(allCyphers);

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
        List<String> statements = parser.parse(cypher);

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
        List<String> statements = parser.parse(cypher);

        // then
        assertEquals(3, statements.size());
        assertEquals("RETURN 1 ", statements.get(0));
        assertEquals("RETURN 2 ", statements.get(1));
        assertEquals("RETURN 3", statements.get(2));
    }

    @Test
    public void testIncompleteSimple() throws Exception {
        // then
        thrown.expect(CypherParserWrapper.IncompleteCypherError.class);

        // given
        final String cypher = "RETURN";

        // when
        List<String> statements = parser.parse(cypher);
    }
}
