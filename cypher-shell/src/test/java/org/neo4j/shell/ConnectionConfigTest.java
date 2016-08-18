package org.neo4j.shell;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionConfigTest {
    ConnectionConfig config = new ConnectionConfig("localhost", 1, "bob", "pass");

    @Test
    public void host() throws Exception {
        assertEquals("localhost", config.host());
    }

    @Test
    public void port() throws Exception {
        assertEquals(1, config.port());
    }

    @Test
    public void username() throws Exception {
        assertEquals("bob", config.username());
    }

    @Test
    public void password() throws Exception {
        assertEquals("pass", config.password());
    }

    @Test
    public void driverUrl() throws Exception {
        assertEquals("bolt://localhost:1", config.driverUrl());
    }

}
