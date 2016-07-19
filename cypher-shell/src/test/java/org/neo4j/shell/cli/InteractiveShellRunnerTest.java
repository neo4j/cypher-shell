package org.neo4j.shell.cli;

import org.junit.Test;
import org.neo4j.shell.StreamShell;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InteractiveShellRunnerTest {
    @Test
    public void testSimple() throws Exception {
        StreamShell shell = new StreamShell("good1\n" +
                "good2\n");
        InteractiveShellRunner runner = new InteractiveShellRunner(shell);
        runner.runUntilEnd();

        assertThat(shell.getErrLog(), is(""));
    }
}
