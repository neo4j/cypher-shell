package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.Historian;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.CypherSyntaxError;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.exception.NoMoreInputException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.ShellStatementParser;
import org.neo4j.shell.parser.StatementParser;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.neo4j.shell.test.Util.ctrl;

public class InteractiveShellRunnerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Logger logger;
    private StatementExecuter cmdExecuter;
    private File historyFile;
    private StatementParser statementParser = new StatementParser() {
        @Nonnull
        @Override
        public List<String> parse(@Nonnull String text) throws CypherSyntaxError {
            // Strips ending newline
            return Arrays.asList(text.replaceAll("\n", ""));
        }
    };

    @Before
    public void setup() throws Exception {
        logger = mock(Logger.class);
        cmdExecuter = mock(StatementExecuter.class);
        historyFile = temp.newFile();
        doThrow(new ClientException("Found a bad line")).when(cmdExecuter).execute(contains("bad"));
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void testSimple() throws Exception {
        String input = "good1\n" +
                "good2\n";
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser,
                new ByteArrayInputStream(input.getBytes()), historyFile);
        runner.runUntilEnd(cmdExecuter);

        verify(cmdExecuter).execute("good1");
        verify(cmdExecuter).execute("good2");
        verifyNoMoreInteractions(cmdExecuter);
    }

    @Test
    public void runUntilEndShouldKeepGoingOnErrors() throws IOException, CommandException {
        String input = "good1\n" +
                "bad1\n" +
                "good2\n" +
                "bad2\n" +
                "good3\n";
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, new ByteArrayInputStream(input.getBytes()),
                historyFile);

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 0, code);

        verify(cmdExecuter).execute("good1");
        verify(cmdExecuter).execute("bad1");
        verify(cmdExecuter).execute("good2");
        verify(cmdExecuter).execute("bad2");
        verify(cmdExecuter).execute("good3");
        verifyNoMoreInteractions(cmdExecuter);

        verify(logger, times(2)).printError("@|RED Found a bad line|@");
    }

    @Test
    public void runUntilEndShouldStopOnExitExceptionAndReturnCode() throws IOException, CommandException {
        String input = "good1\n" +
                "bad1\n" +
                "good2\n" +
                "exit\n" +
                "bad2\n" +
                "good3\n";
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, new ByteArrayInputStream(input.getBytes()),
                historyFile);

        doThrow(new ExitException(1234)).when(cmdExecuter).execute(contains("exit"));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 1234, code);

        verify(cmdExecuter).execute("good1");
        verify(cmdExecuter).execute("bad1");
        verify(cmdExecuter).execute("good2");
        verify(cmdExecuter).execute("exit");
        verifyNoMoreInteractions(cmdExecuter);

        verify(logger).printError("@|RED Found a bad line|@");
    }

    @Test
    public void ctrlCDoesNotKillInteractiveShell() throws Exception {
        String input = "good1\n" +
                "good2\n" +
                ctrl('C') +
                "good3\n";
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, new ByteArrayInputStream(input.getBytes()),
                historyFile);

        doThrow(new ExitException(1234)).when(cmdExecuter).execute(contains("exit"));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 0, code);

        verify(logger).printError("@|RED KeyboardInterrupt|@");

        verify(cmdExecuter).execute("good1");
        verify(cmdExecuter).execute("good2");
        verify(cmdExecuter).execute("good3");
        verifyNoMoreInteractions(cmdExecuter);

    }

    @Test
    public void historyIsRecorded() throws Exception {
        // given

        String cmd1 = ":set var \"3\"";
        String cmd2 = ":help exit";
        String input = cmd1 + "\n" + cmd2 + "\n";

        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, new ByteArrayInputStream(input.getBytes()),
                historyFile);

        // when
        runner.runUntilEnd(cmdExecuter);

        // then
        Historian historian = runner.getHistorian();
        historian.flushHistory();

        List<String> history = Files.readAllLines(historyFile.toPath());

        assertEquals(2, history.size());
        assertEquals(cmd1, history.get(0));
        assertEquals(cmd2, history.get(1));
    }

    @Test
    public void unescapedBangWorks() throws Exception {
        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine by default, just like in bash, but we have disabled that
        InputStream inputStream = new ByteArrayInputStream(":set var \"String with !bang\"\n".getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, inputStream, historyFile);

        // when
        List<String> statements = runner.readUntilStatement();
        // then
        assertEquals(":set var \"String with !bang\"", statements.get(0));
    }

    @Test
    public void escapedBangWorks() throws Exception {
        // given
        PrintStream mockedErr = mock(PrintStream.class);
        when(logger.getErrorStream()).thenReturn(mockedErr);

        // Bangs need escaping in JLine by default, just like in bash, but we have disabled that
        InputStream inputStream = new ByteArrayInputStream(":set var \"String with \\!bang\"\n".getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, statementParser, inputStream, historyFile);

        // when
        List<String> statements = runner.readUntilStatement();
        // then
        assertEquals(":set var \"String with \\!bang\"", statements.get(0));
    }

    @Test
    public void impliedMultiline() throws Exception {
        // given
        String inputString = "CREATE\n(n:Person) RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, new ShellStatementParser(), inputStream, historyFile);

        // when
        List<String> statements = runner.readUntilStatement();

        // then
        assertEquals(1, statements.size());
        assertThat(statements.get(0), is("CREATE\n(n:Person) RETURN n\n"));
    }

    @Test
    public void justNewLineThrowsNoMoreInput() throws Exception {
        // then
        thrown.expect(NoMoreInputException.class);

        // given
        String inputString = "\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, new ShellStatementParser(), inputStream, historyFile);

        // when
        runner.readUntilStatement();
    }

    @Test
    public void emptyStringThrowsNoMoreInput() throws Exception {
        // then
        thrown.expect(NoMoreInputException.class);

        // given
        String inputString = "";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, new ShellStatementParser(), inputStream, historyFile);

        // when
        runner.readUntilStatement();
    }

    @Test
    public void emptyLineIsIgnored() throws Exception {
        // given
        String inputString = "     \nCREATE (n:Person) RETURN n\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());
        InteractiveShellRunner runner = new InteractiveShellRunner(logger, new ShellStatementParser(), inputStream, historyFile);

        // when
        List<String> statements = runner.readUntilStatement();

        // then
        assertEquals(1, statements.size());
        assertThat(statements.get(0), is("CREATE (n:Person) RETURN n\n"));
    }
}
