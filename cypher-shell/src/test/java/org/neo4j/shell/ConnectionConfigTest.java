package org.neo4j.shell;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.shell.log.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.neo4j.shell.DatabaseManager.ABSENT_DB_NAME;

import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ConnectionConfigTest {

    @Rule
    public final EnvironmentVariables environmentVariables
            = new EnvironmentVariables();

    private Logger logger = mock(Logger.class);
    private ConnectionConfig config = new ConnectionConfig("bolt://", "localhost", 1, "bob",
            "pass", false, "db");


    @Test
    public void scheme() {
        assertEquals("bolt://", config.scheme());
    }

    @Test
    public void host() {
        assertEquals("localhost", config.host());
    }

    @Test
    public void port() {
        assertEquals(1, config.port());
    }

    @Test
    public void username() {
        assertEquals("bob", config.username());
    }

    @Test
    public void usernameDefaultsToEnvironmentVar() {
        environmentVariables.set(ConnectionConfig.USERNAME_ENV_VAR, "alice");
        ConnectionConfig configWithEmptyParams = new ConnectionConfig("bolt://", "localhost", 1, "",
                                                                      "", false, ABSENT_DB_NAME);
        assertEquals("alice", configWithEmptyParams.username());
    }

    @Test
    public void password() {
        assertEquals("pass", config.password());
    }

    @Test
    public void passwordDefaultsToEnvironmentVar() {
        environmentVariables.set(ConnectionConfig.PASSWORD_ENV_VAR, "ssap");
        ConnectionConfig configWithEmptyParams = new ConnectionConfig("bolt://", "localhost", 1, "",
                                                                      "", false, ABSENT_DB_NAME);
        assertEquals("ssap", configWithEmptyParams.password());
    }

    @Test
    public void database() {
        assertEquals("db", config.database());
    }

    @Test
    public void databaseDefaultsToEnvironmentVar() {
        environmentVariables.set(ConnectionConfig.DATABASE_ENV_VAR, "funnyDB");
        ConnectionConfig configWithEmptyParams = new ConnectionConfig("bolt://", "localhost", 1, "",
                                                                      "", false, ABSENT_DB_NAME);
        assertEquals("funnyDB", configWithEmptyParams.database());
    }
    @Test
    public void driverUrlDefaultScheme() {
        assertEquals("bolt://localhost:1", config.driverUrl());
    }

    @Test
    public void encryption() {
        assertTrue(new ConnectionConfig("bolt://", "", -1, "", "", true, ABSENT_DB_NAME).encryption());
        assertFalse(new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME).encryption());
    }
}
