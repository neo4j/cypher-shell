package org.neo4j.shell;

import jline.console.ConsoleReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class CommandReaderTest {
    @Test
    public void readCommandReadsFromTheConsole() throws Exception {
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

    @Test
    public void readCommandReturnsNullForEOF() throws Exception {
        StreamShell streamShell = new StreamShell("");
        ConsoleReader reader = new ConsoleReader(streamShell.getInputStream(),
                streamShell.getOutputStream());
        // given
        CommandReader commandReader = new CommandReader(reader, streamShell);

        // then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandReturnsEmptyStringForNewLine() throws Exception {
        StreamShell streamShell = new StreamShell("\n");
        ConsoleReader reader = new ConsoleReader(streamShell.getInputStream(),
                streamShell.getOutputStream());
        // given
        CommandReader commandReader = new CommandReader(reader, streamShell);

        // then
        assertThat(commandReader.readCommand(), is(""));
    }

    @Test
    public void readCommandAcceptsMultilineInputs() throws Exception {
        StreamShell streamShell = new StreamShell("CREATE (n:Person) \\\n" +
                "RETURN n\n");
        ConsoleReader reader = new ConsoleReader(streamShell.getInputStream(),
                streamShell.getOutputStream());
        // given
        CommandReader commandReader = new CommandReader(reader, streamShell);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) RETURN n"));
    }

    @Test
    public void readCommandAcceptsMultilineInputsWithWhiteSpace() throws Exception {
        StreamShell streamShell = new StreamShell("CREATE (n: Person{name :\"John \\ Smith\"}) \\ \n" +
                " RETURN n\n");
        ConsoleReader reader = new ConsoleReader(streamShell.getInputStream(),
                streamShell.getOutputStream());
        // given
        CommandReader commandReader = new CommandReader(reader, streamShell);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n: Person{name :\"John \\ Smith\"})  RETURN n"));
    }
}
