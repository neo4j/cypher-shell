package org.neo4j.shell;

import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;


public class CypherShellTest {
    private CypherTestShell shell;

    @Before
    public void setup() {
        shell = new CypherTestShell();
    }

    @Test
    public void commentsShouldNotBeExecuted() throws Exception {
        shell.executeLine("// Hi, I'm a comment!");
        // If no exception was thrown, we have success
    }

    @Test
    public void emptyLinesShouldNotBeExecuted() throws Exception {
        shell.executeLine("");
        // If no exception was thrown, we have success
    }

    class CypherTestShell extends CypherShell {

        CypherTestShell() {
            super("", 1, "", "");
        }

        @Override
        void executeCypher(@Nonnull String line) {
            throw new RuntimeException("Unexpected cypher execution");
        }

        @Override
        public void connect(@Nonnull String host, int port,
                            @Nonnull String username, @Nonnull String password) throws CommandException {
            throw new RuntimeException("Test shell can't connect");
        }

        @Override
        public void disconnect() throws CommandException {
            throw new RuntimeException("Test shell can't disconnect");
        }

        @Override
        boolean isConnected() {
            return true;
        }
    }
}
