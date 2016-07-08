package org.neo4j.shell;

import jline.console.ConsoleReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CommandReaderTest {
    @Test
    public void readLineReadsFromTheConsole() throws Exception {
        StreamShell streamShell = new StreamShell("CREATE (n:Person) RETURN n\n");
        ConsoleReader reader = new ConsoleReader(streamShell.getInputStream(),
                streamShell.getOutputStream());
        // given
        CommandReader commandReader = new CommandReader(reader, streamShell);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) RETURN n"));
    }
}
