package org.neo4j.shell;

import org.junit.Test;
import org.neo4j.driver.Config;
import org.neo4j.shell.log.Logger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.neo4j.driver.internal.messaging.request.MultiDatabaseUtil.ABSENT_DB_NAME;

public class ConnectionConfigTest {
    private Logger logger = mock(Logger.class);
    private ConnectionConfig config = new ConnectionConfig("bolt://", "localhost", 1, "bob",
            "pass", false, ABSENT_DB_NAME);

    @Test
    public void scheme() throws Exception {
        assertEquals("bolt://", config.scheme());
    }

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
    public void driverUrlDefaultScheme() throws Exception {
        assertEquals("bolt://localhost:1", config.driverUrl());
    }

    @Test
    public void encryption() {
        assertEquals(Config.EncryptionLevel.REQUIRED,
                new ConnectionConfig("bolt://", "", -1, "", "", true, ABSENT_DB_NAME).encryption());
        assertEquals(Config.EncryptionLevel.NONE,
                new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME).encryption());
    }
}
