package org.neo4j.shell.cli;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
@RunWith(Parameterized.class)
public class AddressArgPatternTest {

    private final String scheme;
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final String connString;

    public AddressArgPatternTest(String scheme, String host, String port,
                                 String username, String password) {

        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        StringBuilder connString = new StringBuilder().append(scheme);
        // Only expect username/pass in case host is present
        if (!host.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            connString.append(username).append(":").append(password).append("@");
        }
        if (!host.isEmpty()) {
            connString.append(host);
        }
        if (!port.isEmpty()) {
            connString.append(":").append(port);
        }
        this.connString = connString.toString();
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        Collection<Object[]> data = new ArrayList<>();
        for (final String scheme : getSchemes()) {
            for (final String username : getUsernames()) {
                for (final String password : getPasswords()) {
                    for (final String host : getHosts()) {
                        for (final String port : getPorts()) {
                            data.add(new String[]{scheme, host, port, username, password});
                        }
                    }
                }
            }
        }
        return data;
    }

    private static String[] getPorts() {
        return new String[]{"", "1", "23642"};
    }

    private static String[] getHosts() {
        return new String[]{"", "localhost", "127.0.0.1", "ec2-54-73-70-121.eu-west-1.compute.amazonaws.com"};
    }

    private static String[] getPasswords() {
        return new String[]{"pass1", "p@assw0rd"};
    }

    private static String[] getUsernames() {
        return new String[]{"", "bob1"};
    }

    private static String[] getSchemes() {
        return new String[]{"", "bolt://", "bolt+routing://", "w31rd.-+://"};
    }

    @Test
    public void testUserPassschemeHostPort() {
        Matcher m = CliArgHelper.ADDRESS_ARG_PATTERN.matcher("   " + connString + "  ");
        assertTrue("Expected a match: " + connString, m.matches());
        if (host.isEmpty()) {
            assertNull("Host should have been null", m.group("host"));
        } else {
            assertEquals("Mismatched host", host, m.group("host"));
        }
        if (port.isEmpty()) {
            assertNull("Port should have been null", m.group("port"));
        } else {
            assertEquals("Mismatched port", port, m.group("port"));
        }
        if (host.isEmpty() || username.isEmpty() || password.isEmpty()) {
            assertNull("Username should have been null", m.group("username"));
            assertNull("Password should have been null", m.group("password"));
        } else {
            assertEquals("Mismatched username", username, m.group("username"));
            assertEquals("Mismatched password", password, m.group("password"));
        }
        if (scheme.isEmpty()) {
            assertNull("scheme should have been null", m.group("scheme"));
        } else {
            assertEquals("Mismatched scheme", scheme, m.group("scheme"));
        }
    }
}
