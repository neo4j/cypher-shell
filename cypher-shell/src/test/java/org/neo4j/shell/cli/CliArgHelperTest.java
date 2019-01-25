package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.neo4j.shell.test.Util.asArray;

public class CliArgHelperTest {

    private PrintStream mockedStdErr;

    @Before
    public void setup() {
        mockedStdErr = mock(PrintStream.class);
    }

    @Test
    public void testForceNonInteractiveIsNotDefault() {
        assertFalse("Force non-interactive should not be the default mode",
                CliArgHelper.parse(asArray()).getNonInteractive());
    }

    @Test
    public void testForceNonInteractiveIsParsed() {
        assertTrue("Force non-interactive should have been parsed to true",
                CliArgHelper.parse(asArray("--non-interactive")).getNonInteractive());
    }

    @Test
    public void testDebugIsNotDefault() {
        assertFalse("Debug should not be the default mode",
                CliArgHelper.parse(asArray()).getDebugMode());
    }

    @Test
    public void testDebugIsParsed() {
        assertTrue("Debug should have been parsed to true",
                CliArgHelper.parse(asArray("--debug")).getDebugMode());
    }

    @Test
    public void testVersionIsParsed() {
        assertTrue("Version should have been parsed to true",
                CliArgHelper.parse(asArray("--version")).getVersion());
    }

    @Test
    public void testDriverVersionIsParsed() {
        assertTrue("Driver version should have been parsed to true",
                CliArgHelper.parse(asArray("--driver-version")).getDriverVersion());
    }

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
        ArrayList<String> strings = new ArrayList<>( asList( "-a 192.168.1.1 -p 123 --format plain".split( " " ) ) );
        strings.add(query);
        assertEquals(Optional.of(query),
                CliArgHelper.parse(strings.toArray( new String[0] )).getCypher());
    }

    @Test
    public void parseFormat() throws Exception {
        assertEquals(Format.PLAIN, CliArgHelper.parse("--format", "plain").getFormat());
        assertEquals(Format.VERBOSE, CliArgHelper.parse("--format", "verbose").getFormat());
    }

    @Test
    public void parsePassword() throws Exception {
        assertEquals("foo", CliArgHelper.parse("--password", "foo").getPassword());
    }

    @Test
    public void parseUserName() throws Exception {
        assertEquals("foo", CliArgHelper.parse("--username", "foo").getUsername());
    }

    @Test
    public void parseFullAddress() throws Exception {
        CliArgs cliArgs = CliArgHelper.parse("--address", "bolt+routing://alice:foo@bar:69");
        assertNotNull(cliArgs);
        assertEquals("alice", cliArgs.getUsername());
        assertEquals("foo", cliArgs.getPassword());
        assertEquals("bar", cliArgs.getHost());
        assertEquals(69, cliArgs.getPort());
    }

    @Test
    public void nonsenseArgsGiveError() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(bout));

        CliArgs cliargs = CliArgHelper.parse("-notreally");

        assertNull(cliargs);

        assertTrue(bout.toString().contains("cypher-shell [-h]"));
        assertTrue(bout.toString().contains("cypher-shell: error: unrecognized arguments: '-notreally'"));
    }

    @Test
    public void nonsenseUrlGivesError() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setErr(new PrintStream(bout));

        CliArgs cliargs = CliArgHelper.parse("--address", "host;port");

        assertNull("should have failed", cliargs);

        assertTrue("expected usage: " + bout.toString(),
                bout.toString().contains("cypher-shell [-h]"));
        assertTrue("expected error: " + bout.toString(),
                bout.toString().contains("cypher-shell: error: Failed to parse address"));
        assertTrue("expected error detail: " + bout.toString(),
                bout.toString().contains(format("%n  Address should be of the form:")));
    }

    @Test
    public void defaultsEncryptionToTrue() {
        assertEquals(true, CliArgHelper.parse().getEncryption());
    }

    @Test
    public void allowsEncryptionToBeTurnedOnOrOff() {
        assertEquals(true, CliArgHelper.parse("--encryption", "true").getEncryption());
        assertEquals(false, CliArgHelper.parse("--encryption", "false").getEncryption());
    }
}
