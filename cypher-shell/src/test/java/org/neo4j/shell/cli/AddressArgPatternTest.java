package org.neo4j.shell.cli;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.*;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class AddressArgPatternTest {

    @Test
    public void testUserPassProtocolHostPort() {
        Matcher m = CliArgHelper.addressArgPattern.matcher("   bolt://bob1:pass@localhost:123");
        assertTrue("Expected a match: " + "   bolt://bob1:pass@:123", m.matches());
        assertEquals("123", m.group("port"));
        assertEquals("bob1", m.group("username"));
        assertEquals("pass", m.group("password"));
        assertEquals("bolt://", m.group("protocol"));
        assertEquals("localhost", m.group("host"));

        Matcher m1 = CliArgHelper.addressArgPattern.matcher("bolt://bob1:h@rdp@ss:w0rd@99.99.99.99:1  ");
        assertTrue("Expected a match: " + "bolt://bob1h@rdp@ss:w0rd1  ", m1.matches());
        assertEquals("1", m1.group("port"));
        assertEquals("bob1", m1.group("username"));
        assertEquals("h@rdp@ss:w0rd", m1.group("password"));
        assertEquals("bolt://", m1.group("protocol"));
        assertEquals("99.99.99.99", m1.group("host"));

        Matcher m2 = CliArgHelper.addressArgPattern.matcher("bolt://bob1:h@rdp@ss:w0rd@99.99.99.99:1");
        assertTrue("Expected a match: " + "bolt://bob1h@rdp@ss:w0rd1", m2.matches());
        assertEquals("1", m2.group("port"));
        assertEquals("bob1", m2.group("username"));
        assertEquals("h@rdp@ss:w0rd", m2.group("password"));
        assertEquals("bolt://", m2.group("protocol"));
        assertEquals("99.99.99.99", m2.group("host"));

    }

    @Test
    public void testHosts() {
        verifySingleHost("localhost");
        verifySingleHost("127.0.0.1");

        verifySingleHost("   localhost   ");
        verifySingleHost("   127.0.0.1  ");
    }

    @Test
    public void testProtocolHosts() {
        verifyProtocolHost("bolt://", "localhost");
        verifyProtocolHost("bolt://", "127.0.0.1");

        verifyProtocolHost("     bolt://", "localhost    ");
        verifyProtocolHost("    bolt://", "127.0.0.1    ");
    }

    @Test
    public void testPorts() {
        verifySinglePort(":1");
        verifySinglePort(":1234");


        verifySinglePort("   :12   ");
        verifySinglePort("   :64321  ");
    }

    @Test
    public void testProtocolPorts() {
        verifyProtocolPort("bolt://", "1");
        verifyProtocolPort("bolt://", "1234");

        verifyProtocolPort("   bolt://", "12   ");
        verifyProtocolPort("   bolt://", "64321  ");
    }

    @Test
    public void testHostnamePort() {
        verifyHostPort("localhost", "1");
        verifyHostPort("   127.0.0.1", "12345  ");
        verifyHostPort("   localhost", "1");
        verifyHostPort("127.0.0.1", "12345  ");
    }

    @Test
    public void testProtocolHostnamePort() {
        verifyProtocolHostPort("bolt://", "localhost", "1");
        verifyProtocolHostPort("   bolt://", "127.0.0.1", "12345  ");
        verifyProtocolHostPort("   bolt://", "localhost", "1");
        verifyProtocolHostPort("bolt://", "127.0.0.1", "12345  ");
    }

    @Test
    public void testUserPassHost() {
        verifyUserPassHost("   bob1", "pass", "neo4j.com");
        verifyUserPassHost("   bob1", "h@rdp@ss:w0rd", "neo4j.com");
        verifyUserPassHost("   bob1", "h@rdp@ss:w0rd", "neo4j.com");
    }

    @Test
    public void testUserPassProtocolHost() {
        verifyUserPassProtocolHost("    bolt://", "bob1", "pass", "neo4j.com");
        verifyUserPassProtocolHost("   bolt://", "bob1", "h@rdp@ss:w0rd", "neo4j.com");
        verifyUserPassProtocolHost("   bolt://", "bob1", "h@rdp@ss:w0rd", "neo4j.com");
    }

    @Test
    public void testUserPassPort() {
        verifyUserPassPort("   bob1", "pass", "123");
        verifyUserPassPort("bob1", "h@rdp@ss:w0rd", "1  ");
    }

    @Test
    public void testUserPassProtocolPort() {
        verifyUserPassProtocolPort("   bolt://", "bob1", "pass", "123");
        verifyUserPassProtocolPort("bolt://", "bob1", "h@rdp@ss:w0rd", "1  ");
    }

    @Test
    public void testUserPassHostPort() {
        verifyUserPassHostPort("bob1", "pass", "localhost", "123  ");
        verifyUserPassHostPort("bob1", "h@rdp@ss:w0rd", "99.99.99.99", "1");
        verifyUserPassHostPort("bob1", "h@rdp@ss:w0rd", "99.99.99.99", "1");
    }

    private void verifyUserPassHostPort(String user, String pass, String host, String port) {
        String args = user + ":" + pass + "@" + host + ":" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(port.trim(), m.group("port"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));
        assertNull("Did not expect match for protocol group", m.group("protocol"));
    }

    private void verifyUserPassPort(String user, String pass, String port) {
        String args = user + ":" + pass + "@:" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(port.trim(), m.group("port"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));

        assertNull("Did not expect match for protocol group", m.group("protocol"));
        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifyUserPassProtocolPort(String protocol, String user, String pass, String port) {
        String args = protocol + user + ":" + pass + "@" + ":" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(port.trim(), m.group("port"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));
        assertEquals(protocol.trim(), m.group("protocol"));

        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifyUserPassProtocolHost(String protocol, String user, String pass, String host) {
        String args = protocol + user + ":" + pass + "@" + host;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));
        assertEquals(protocol.trim(), m.group("protocol"));

        assertNull("Did not expect match for port group", m.group("port"));
    }

    private void verifyUserPassHost(String user, String pass, String host) {
        String args = user + ":" + pass + "@" + host;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));

        assertNull("Did not expect match for protocol group", m.group("protocol"));
        assertNull("Did not expect match for port group", m.group("port"));
    }

    private void verifyProtocolHostPort(String protocol, String host, String port) {
        String args = protocol + host + ":" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(port.trim(), m.group("port"));
        assertEquals(protocol.trim(), m.group("protocol"));

        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
    }

    private void verifyHostPort(String host, String port) {
        String args = host + ":" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(port.trim(), m.group("port"));

        assertNull("Did not expect match for protocol group", m.group("protocol"));
        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
    }

    private void verifyProtocolPort(String protocol, String port) {
        String args = protocol + ":" + port;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(port.trim(), m.group("port"));
        assertEquals(protocol.trim(), m.group("protocol"));

        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifySinglePort(String args) {
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(args.trim().substring(1), m.group("port"));

        assertNull("Did not expect match for protocol group", m.group("protocol"));
        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifySingleHost(String args) {
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(args.trim(), m.group("host"));
        assertNull("Did not expect match for protocol group", m.group("protocol"));
        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for port group", m.group("port"));
    }

    private void verifyProtocolHost(String protocol, String host) {
        String args = protocol + host;
        Matcher m = CliArgHelper.addressArgPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(protocol.trim(), m.group("protocol"));
        assertEquals(host.trim(), m.group("host"));
        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for port group", m.group("port"));
    }
}
