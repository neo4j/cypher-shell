package org.neo4j.shell;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserMessagesHandlerTest {
    private final ConnectionConfig connectionConfig = mock(ConnectionConfig.class);

    @Test
    public void welcomeMessageTest() {
        when(connectionConfig.username()).thenReturn("bob");
        when(connectionConfig.driverUrl()).thenReturn("bolt://some.place.com:99");

        UserMessagesHandler userMessagesHandler = new UserMessagesHandler(connectionConfig, "3.1.0-Beta99");
        assertEquals("Connected to Neo4j 3.1.0-Beta99 at @|BOLD bolt://some.place.com:99|@ as user @|BOLD bob|@.\n" +
                        "Type @|BOLD :help|@ for a list of available commands or @|BOLD :exit|@ to exit the shell.\n" +
                        "Note that Cypher queries must end with a @|BOLD semicolon.|@",
                userMessagesHandler.getWelcomeMessage());
    }

    @Test
    public void exitMessageTest() {
        when(connectionConfig.username()).thenReturn("bob");
        when(connectionConfig.driverUrl()).thenReturn("bolt://some.place.com:99");

        UserMessagesHandler userMessagesHandler = new UserMessagesHandler(connectionConfig, "3.1.0-Beta99");
        assertEquals("\nBye!", userMessagesHandler.getExitMessage());
    }
}
