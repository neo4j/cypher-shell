package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CliArgsTest {
    private CliArgs cliArgs;

    @Before
    public void setup() {
        cliArgs = new CliArgs();
    }

    @Test
    public void setHost() throws Exception {
        cliArgs.setHost("foo", "bar");
        assertEquals("foo", cliArgs.getHost());

        cliArgs.setHost(null, "bar");
        assertEquals("bar", cliArgs.getHost());
    }

    @Test
    public void setPort() throws Exception {
        cliArgs.setPort(999);
        assertEquals(999, cliArgs.getPort());
    }

    @Test
    public void setUsername() throws Exception {
        cliArgs.setUsername("foo", "bar");
        assertEquals("foo", cliArgs.getUsername());

        cliArgs.setUsername(null, "bar");
        assertEquals("bar", cliArgs.getUsername());
    }

    @Test
    public void setPassword() throws Exception {
        cliArgs.setPassword("foo", "bar");
        assertEquals("foo", cliArgs.getPassword());

        cliArgs.setPassword(null, "bar");
        assertEquals("bar", cliArgs.getPassword());
    }

    @Test
    public void setFailBehavior() throws Exception {
        // default
        assertEquals(FailBehavior.FAIL_FAST, cliArgs.getFailBehavior());

        cliArgs.setFailBehavior(FailBehavior.FAIL_AT_END);
        assertEquals(FailBehavior.FAIL_AT_END, cliArgs.getFailBehavior());
    }

    @Test
    public void setFormat() throws Exception {
        // default
        assertEquals(Format.VERBOSE, cliArgs.getFormat());

        cliArgs.setFormat(Format.PLAIN);
        assertEquals(Format.PLAIN, cliArgs.getFormat());
    }

    @Test
    public void setCypher() throws Exception {
        // default
        assertFalse(cliArgs.getCypher().isPresent());

        cliArgs.setCypher("foo");
        assertTrue(cliArgs.getCypher().isPresent());
        //noinspection OptionalGetWithoutIsPresent
        assertEquals("foo", cliArgs.getCypher().get());

        cliArgs.setCypher(null);
        assertFalse(cliArgs.getCypher().isPresent());
    }
}
