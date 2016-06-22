package org.neo4j.shell.commands;

import org.junit.Test;

import java.util.regex.Matcher;

import static org.junit.Assert.*;


public class ConnectArgPatternTest {

    @Test
    public void testHosts() {
        verifySingleHost("localhost");
        verifySingleHost("127.0.0.1");


        verifySingleHost("   localhost   ");
        verifySingleHost("   127.0.0.1  ");
    }

    @Test
    public void testPorts() {
        verifySinglePort(":1");
        verifySinglePort(":1234");


        verifySinglePort("   :12   ");
        verifySinglePort("   :64321  ");
    }

    @Test
    public void testHostnamePort() {
        verifyHostPort("localhost", "1");
        verifyHostPort("   127.0.0.1", "12345  ");
    }

    @Test
    public void testUserPassHost() {
        verifyUserPassHost("   bob1", "pass", "neo4j.com");
        verifyUserPassHost("   bob1", "h@rdp@ss:w0rd", "neo4j.com");
    }

    @Test
    public void testUserPassPort() {
        verifyUserPassPort("   bob1", "pass", "123");
        verifyUserPassPort("bob1", "h@rdp@ss:w0rd", "1  ");
    }

    @Test
    public void testUserPassHostPort() {
        verifyUserPassHostPort("bob1", "pass", "localhost", "123  ");
        verifyUserPassHostPort("bob1", "h@rdp@ss:w0rd", "99.99.99.99", "1");
    }

    private void verifyUserPassHostPort(String user, String pass, String host, String port) {
        String args = user + ":" + pass + "@" + host + ":" + port;
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(port.trim(), m.group("port"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));
    }

    private void verifyUserPassPort(String user, String pass, String port) {
        String args = user + ":" + pass + "@:" + port;
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(port.trim(), m.group("port"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));

        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifyUserPassHost(String user, String pass, String host) {
        String args = user + ":" + pass + "@" + host;
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(user.trim(), m.group("username"));
        assertEquals(pass.trim(), m.group("password"));

        assertNull("Did not expect match for port group", m.group("port"));
    }

    private void verifyHostPort(String host, String port) {
        String args = host + ":" + port;
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(host.trim(), m.group("host"));
        assertEquals(port.trim(), m.group("port"));

        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
    }

    private void verifySinglePort(String args) {
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(args.trim().substring(1), m.group("port"));

        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for host group", m.group("host"));
    }

    private void verifySingleHost(String args) {
        Matcher m = Connect.argPattern.matcher(args);
        assertTrue("Expected a match: " + args, m.matches());
        assertEquals(args.trim(), m.group("host"));
        assertNull("Did not expect match for password group", m.group("password"));
        assertNull("Did not expect match for username group", m.group("username"));
        assertNull("Did not expect match for port group", m.group("port"));
    }


}
