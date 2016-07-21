package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CommandReaderTest {

    Logger logger = mock(Logger.class);

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void readCommandReadsFromTheConsole() throws Exception {
        // given
        String inputString = "CREATE (n:Person) RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) RETURN n\n"));
    }

    @Test
    public void readCommandDoesNotFiltersSingleLineCommentsFromTheConsole() throws Exception {
        // given
        String inputString = "CREATE (n:Person) \\\n" +
                "//We are returning all People \\\n" +
                "RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) \n"));
    }

    @Test
    public void readCommandIgnoresComment() throws Exception {
        // given
        String inputString = "// Hi, I'm a comment!\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandIgnoresEmptyLines() throws Exception {
        // given
        String inputString = "\n\n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandIgnoresWhitespacedLines() throws Exception {
        // given
        String inputString = "     \n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandIgnoresEmptyMultiLines() throws Exception {
        // given
        String inputString = "     \\\n" +
                "// Second line comment, first line escapes newline";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandDoesNotFiltersSingleLineCommentsFromTheConsoleScenario2() throws Exception {
        // given
        String inputString = "CREATE (n:Person) //We are returning all People RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) \n"));
    }

    @Test
    public void readCommandDoesNotReadMultiLineCommentsFromTheConsole() throws Exception {
        // given
        String inputString = "CREATE (n:Person) \\\n" +
                "//We are returning \n" +
                "// all People \\\n" +
                "RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) \n"));
    }

    @Test
    public void readCommandReturnsNullForEOF() throws Exception {
        // given
        String inputString = "";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandReturnsNullForNewLine() throws Exception {
        // given
        String inputString = "\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // then
        assertNull(commandReader.readCommand());
    }

    @Test
    public void readCommandAcceptsMultilineInputs() throws Exception {
        // given
        String inputString = "CREATE (n:Person) \\\n" +
                "RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n:Person) \nRETURN n\n"));
    }

    @Test
    public void readCommandAcceptsMultilineInputsWithWhiteSpace() throws Exception {
        // given
        String inputString = "CREATE (n: Person{name :\"John \\ Smith\"}) \\ \n" +
                " RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertThat(actual, is("CREATE (n: Person{name :\"John \\ Smith\"}) \n RETURN n\n"));
    }
}
