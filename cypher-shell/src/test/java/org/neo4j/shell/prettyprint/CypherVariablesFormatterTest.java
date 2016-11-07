package org.neo4j.shell.prettyprint;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CypherVariablesFormatterTest {

    private final CypherVariablesFormatter formatter = new CypherVariablesFormatter();

    @Test
    public void escapeNonAlphanumericStrings() throws Exception {
        assertThat(formatter.escape("abc12_A"), is("abc12_A"));
        assertThat(formatter.escape("Åbc12_A"), is("Åbc12_A"));
        assertThat(formatter.escape("\0"), is("`\0`"));
        assertThat(formatter.escape("\n"), is("`\n`"));
        assertThat(formatter.escape("comma, separated"), is("`comma, separated`"));
        assertThat(formatter.escape("escaped content `back ticks #"), is("`escaped content ``back ticks #`"));
        assertThat(formatter.escape("escaped content two `back `ticks"),
                is("`escaped content two ``back ``ticks`"));
    }

    @Test
    public void reEscapeNonAlphanumericStrings() throws Exception {
        assertThat(formatter.unescapedCypherVariable("abc12_A"), is("abc12_A"));
        assertThat(formatter.unescapedCypherVariable("Åbc12_A"), is("Åbc12_A"));
        assertThat(formatter.unescapedCypherVariable("`\0`"), is("\0"));
        assertThat(formatter.unescapedCypherVariable("`\n`"), is("\n"));
        assertThat(formatter.unescapedCypherVariable("`comma, separated`"), is("comma, separated"));
        assertThat(formatter.unescapedCypherVariable("`escaped content ``back ticks #`"),
                is("escaped content `back ticks #"));
        assertThat(formatter.unescapedCypherVariable("`escaped content two ``back ``ticks`"),
                is("escaped content two `back `ticks"));
    }
}
