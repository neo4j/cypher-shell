package org.neo4j.shell;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CliArgHelperTest {

    @Test
    public void testFailFastIsDefault() {
        assertEquals("Unexpected fail-behavior", CliArgHelper.FailBehavior.FAIL_FAST,
                CliArgHelper.parse(args()).getFailBehavior());
    }

    @Test
    public void testFailFastIsParsed() {
        assertEquals("Unexpected fail-behavior", CliArgHelper.FailBehavior.FAIL_FAST,
                CliArgHelper.parse(args("-ff")).getFailBehavior());
        assertEquals("Unexpected fail-behavior", CliArgHelper.FailBehavior.FAIL_FAST,
                CliArgHelper.parse(args("--fail-fast")).getFailBehavior());
    }

    @Test
    public void testFailAtEndIsParsed() {
        assertEquals("Unexpected fail-behavior", CliArgHelper.FailBehavior.FAIL_AT_END,
                CliArgHelper.parse(args("-fae")).getFailBehavior());
        assertEquals("Unexpected fail-behavior", CliArgHelper.FailBehavior.FAIL_AT_END,
                CliArgHelper.parse(args("--fail-at-end")).getFailBehavior());
    }

    private String[] args(String... arguments) {
        return arguments;
    }
}
