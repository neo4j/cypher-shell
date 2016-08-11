package org.neo4j.shell.cli;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.neo4j.shell.test.Util.asArray;

public class CliArgHelperTest {

    @Test
    public void testFailFastIsDefault() {
        assertEquals("Unexpected fail-behavior", FailBehavior.FAIL_FAST,
                CliArgHelper.parse(asArray()).getFailBehavior());
    }

    @Test
    public void testFailFastIsParsed() {
        assertEquals("Unexpected fail-behavior", FailBehavior.FAIL_FAST,
                CliArgHelper.parse(asArray("--fail-fast")).getFailBehavior());
    }

    @Test
    public void testFailAtEndIsParsed() {
        assertEquals("Unexpected fail-behavior", FailBehavior.FAIL_AT_END,
                CliArgHelper.parse(asArray("--fail-at-end")).getFailBehavior());
    }

    @Test
    public void singlePositionalArgumentIsFine() {
        String text = "Single string";
        assertEquals("Did not parse cypher string", text,
                CliArgHelper.parse(asArray(text)).getCypher().get());
    }

    @Test
    public void parseArgumentsAndQuery() {
        String query = "\"match (n) return n\"";
        ArrayList<String> strings = new ArrayList<>();
        strings.addAll(asList("-a 192.168.1.1 -p 123 --format plain".split(" ")));
        strings.add(query);
        assertEquals(Optional.of(query),
                CliArgHelper.parse(strings.toArray(new String[strings.size()])).getCypher());
    }
}
