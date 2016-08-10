package org.neo4j.shell;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.CliArgs;

public class MainIntegrationTest {
    @Test
    @Ignore
    public void testName() throws Exception {
        // given
        Main main = new Main();

        CliArgs cliArgs = new CliArgs();
        cliArgs.setCypher("match (n) return n");
        //Set Output format
        main.startShell(cliArgs);
        // when

        // then
    }
}
