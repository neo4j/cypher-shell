package org.neo4j.shell.prettyprint;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CypherVariablesFormatterTest {

    private final CypherVariablesFormatter formatter = new CypherVariablesFormatter();

    @Test
    public void formatNonAlphanumericStrings() throws Exception {
        assertThat(formatter.escape("abc12_A"), is("abc12_A"));
        assertThat(formatter.escape("\0"), is("`\0`"));
        assertThat(formatter.escape("\n"), is("`\n`"));
        assertThat(formatter.escape("comma, separated"), is("`comma, separated`"));
        assertThat(formatter.escape("escaped content `back ticks #"), is("`escaped content ``back ticks #`"));
        assertThat(formatter.escape("escaped content two `back `ticks"),
                is("`escaped content two ``back ``ticks`"));
    }
}
