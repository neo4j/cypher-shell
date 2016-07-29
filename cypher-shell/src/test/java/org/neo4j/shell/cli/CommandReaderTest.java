package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.getProperty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandReaderTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    Logger logger = mock(Logger.class);
    InputStream mockedInput = mock(InputStream.class);

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
    public void historyIsRecorded() throws Exception {
        // given
        File historyFile = temp.newFile();

        String cmd1 = ":set var \"3\"";
        String cmd2 = ":help exit";
        String inputString = cmd1 + "\n" + cmd2 + "\n";

        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger, historyFile);

        // when
        assertEquals(cmd1 + "\n", commandReader.readCommand());
        assertEquals(cmd2 + "\n", commandReader.readCommand());

        commandReader.flushHistory();

        // then
        List<String> history = Files.readAllLines(historyFile.toPath());

        assertEquals(2, history.size());
        assertEquals(cmd1, history.get(0));
        assertEquals(cmd2, history.get(1));
    }

    @Test
    public void badHistoryFileFallsBackToMemoryFile() throws Exception {
        new CommandReader(mockedInput, logger,
                new File("/temp/aasbzs/asfaz/asdfasvzx/asfdasdf/asdfasd"));
        verify(logger).printError("Could not load history file. Falling back to session-based history.\n" +
                "Failed to create directory for history: /temp/aasbzs/asfaz/asdfasvzx/asfdasdf");

    }

    @Test
    public void defaultHistoryFile() throws Exception {
        Path expectedPath = Paths.get(getProperty("user.home"), ".neo4j", ".neo4j_history");

        File history = CommandReader.getDefaultHistoryFile();
        assertEquals(expectedPath.toString(), history.getPath());
    }

    @Test
    public void unescapedBangWorks() throws Exception {
        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine by default, just like in bash, but we have disabled that
        String inputString = ":set var \"String with !bang\"\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertEquals(inputString, commandReader.readCommand());
    }

    @Test
    public void escapedBangWorks() throws Exception {
        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine, just like in bash
        String inputString = ":set var \"String with \\!bang\"\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        CommandReader commandReader = new CommandReader(inputStream, logger);

        // when then
        assertEquals(inputString, commandReader.readCommand());
    }
}
