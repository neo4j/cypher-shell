package org.neo4j.shell.prettyprint;

import org.junit.Test;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.escape;
import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.unescapedCypherVariable;

public class CypherVariablesFormatterTest {



    @Test
    public void escapeNonAlphanumericStrings() throws Exception {
        assertThat(escape("abc12_A"), is("abc12_A"));
        assertThat(escape("Åbc12_A"), is("Åbc12_A"));
        assertThat(escape("\0"), is("`\0`"));
        assertThat(escape(lineSeparator()), is(format("`%n`")));
        assertThat(escape("comma, separated"), is("`comma, separated`"));
        assertThat(escape("escaped content `back ticks #"), is("`escaped content ``back ticks #`"));
        assertThat(escape("escaped content two `back `ticks"),
                is("`escaped content two ``back ``ticks`"));
    }

    @Test
    public void reEscapeNonAlphanumericStrings() throws Exception {
        assertThat(unescapedCypherVariable("abc12_A"), is("abc12_A"));
        assertThat(unescapedCypherVariable("Åbc12_A"), is("Åbc12_A"));
        assertThat(unescapedCypherVariable("`\0`"), is("\0"));
        assertThat(unescapedCypherVariable(format("`%n`")), is(lineSeparator()));
        assertThat(unescapedCypherVariable("`comma, separated`"), is("comma, separated"));
        assertThat(unescapedCypherVariable("`escaped content ``back ticks #`"),
                is("escaped content `back ticks #"));
        assertThat(unescapedCypherVariable("`escaped content two ``back ``ticks`"),
                is("escaped content two `back `ticks"));
    }
}
