package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.exception.JLineException;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommandReaderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
    public void noHistoryFileGivesMemoryHistory() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("yo\n".getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger, false);

        assertTrue(commandReader.getHistory().isEmpty());

        assertEquals("yo\n", commandReader.readCommand());

        assertEquals(1, commandReader.getHistory().size());
        assertEquals("yo", commandReader.getHistory().get(0));
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

    @Test
    public void unescapedBangThrowsException() throws Exception {
        thrown.expect(JLineException.class);
        thrown.expectMessage("!bang\": event not found");

        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine, just like in bash
        String inputString = ":set var \"String with !bang\"\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when
        commandReader.readCommand();
    }

    @Test
    public void escapedBangWorks() throws Exception {
        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine, just like in bash
        String escapedString = ":set var \"String with \\!bang\"\n";
        InputStream escapedStream = new ByteArrayInputStream(escapedString.getBytes());
        CommandReader commandReader = new CommandReader(escapedStream, logger);

        // when
        String actual = commandReader.readCommand();

        // then
        assertEquals(":set var \"String with !bang\"\n", actual);
    }
}
