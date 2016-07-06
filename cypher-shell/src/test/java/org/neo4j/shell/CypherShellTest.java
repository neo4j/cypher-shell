package org.neo4j.shell;

import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

import java.io.IOException;

import static junit.framework.TestCase.fail;


public class CypherShellTest {
    private ThrowingShell shell;

    @Before
    public void setup() {
        shell = new ThrowingShell();
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

    @Test
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse("--cypher", "MATCH (n) RETURN n");

        ShellRunner shellRunner = shell.getShellRunner(cliArgs);

        if (!(shellRunner instanceof StringShellRunner)) {
            fail("Expected a different runner than: " + shellRunner.getClass().getSimpleName());
        }
    }

    class ThrowingShell extends TestShell {
        @Override
        void executeCypher(@Nonnull String line) {
            throw new RuntimeException("Unexpected cypher execution");
        }
    }
}
