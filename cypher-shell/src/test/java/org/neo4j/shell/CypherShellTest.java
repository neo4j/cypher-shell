package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.StatementRunner;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.commands.CommandExecutable;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.PrettyPrinter;
import org.neo4j.shell.state.BoltStateHandler;
import org.neo4j.shell.state.OfflineBoltStateHandler;
import org.neo4j.shell.test.OfflineTestShell;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class CypherShellTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private BoltStateHandler mockedBoltStateHandler = mock(BoltStateHandler.class);
    private final PrettyPrinter mockedPrettyPrinter = new PrettyPrinter(Format.VERBOSE);
    private Logger logger = mock(Logger.class);
    private OfflineTestShell offlineTestShell;

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
        offlineTestShell = new OfflineTestShell(logger);

        CommandHelper commandHelper = new CommandHelper(logger, Historian.empty, offlineTestShell);

        offlineTestShell.setCommandHelper(commandHelper);
    }

    @Test
    public void verifyDelegationOfConnectionMethods() throws CommandException {
        ConnectionConfig cc = new ConnectionConfig("", 1, "", "");
        CypherShell shell = new CypherShell(logger, mockedBoltStateHandler, mockedPrettyPrinter);

        shell.connect(cc);
        verify(mockedBoltStateHandler).connect(cc);

        shell.isConnected();
        verify(mockedBoltStateHandler).isConnected();
    }

    @Test
    public void verifyDelegationOfResetMethod() throws CommandException {
        CypherShell shell = new CypherShell(logger, mockedBoltStateHandler, mockedPrettyPrinter);

        shell.reset();
        verify(mockedBoltStateHandler).reset();
    }

    @Test
    public void verifyDelegationOfTransactionMethods() throws CommandException {
        CypherShell shell = new CypherShell(logger, mockedBoltStateHandler, mockedPrettyPrinter);

        shell.beginTransaction();
        verify(mockedBoltStateHandler).beginTransaction();

        shell.commitTransaction();
        verify(mockedBoltStateHandler).commitTransaction();

        shell.rollbackTransaction();
        verify(mockedBoltStateHandler).rollbackTransaction();
    }

    @Test
    public void setWhenDisconnectedShouldThrow() throws CommandException {
        CypherShell shell = new OfflineTestShell(logger);

        assertFalse(shell.isConnected());

        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        shell.set("bob", "99");
        assertEquals("99", shell.getAll().get("bob"));
    }

    @Test
    public void verifyVariableMethods() throws CommandException {
        ConnectionConfig cc = new ConnectionConfig("", 1, "", "");
        OfflineTestShell shell = new OfflineTestShell(logger);
        shell.connect(cc);

        assertTrue(shell.isConnected());

        assertTrue(shell.getAll().isEmpty());

        Optional result = shell.set("bob", "99");
        assertTrue(result.isPresent());
        assertEquals("99", result.get());
        assertEquals("99", shell.getAll().get("bob"));

        shell.remove("bob");
        assertTrue(shell.getAll().isEmpty());
    }

    @Test
    public void executeOfflineThrows() throws CommandException {
        OfflineTestShell shell = new OfflineTestShell(logger);

        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        shell.execute("RETURN 999");
    }

    @Test
    public void executeShouldPrintResult() throws CommandException {
        Driver mockedDriver = mock(Driver.class);
        Session session = mock(Session.class);
        StatementRunner statementRunner = mock(StatementRunner.class);
        StatementResult resultMock = mock(StatementResult.class);

        BoltStateHandler boltStateHandler = mock(BoltStateHandler.class);
        PrettyPrinter prettyPrinter = mock(PrettyPrinter.class);

        when(boltStateHandler.isConnected()).thenReturn(true);
        when(boltStateHandler.getStatementRunner()).thenReturn(statementRunner);
        when(statementRunner.run(anyString(), anyMap())).thenReturn(resultMock);
        when(prettyPrinter.format(resultMock)).thenReturn("999");
        when(mockedDriver.session()).thenReturn(session);

        OfflineTestShell shell = new OfflineTestShell(logger, boltStateHandler, prettyPrinter);
        shell.execute("RETURN 999");
        verify(logger).printOut(contains("999"));
    }

    @Test
    public void commandNameShouldBeParsed() {

        Optional<CommandExecutable> exe = offlineTestShell.getCommandExecutable("   :help    ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandNameShouldBeParsedWithNewline() {

        Optional<CommandExecutable> exe = offlineTestShell.getCommandExecutable("   :help    \n");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandWithArgsShouldBeParsed() throws CommandException {

        Optional<CommandExecutable> exe = offlineTestShell.getCommandExecutable("   :help   arg1 arg2 ");

        assertTrue(exe.isPresent());

        thrown.expect(CommandException.class);
        thrown.expectMessage("Incorrect number of arguments");

        offlineTestShell.executeCmd(exe.get());
    }

    @Test
    public void commandWithArgsShouldBeParsedAndExecuted() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Incorrect number of arguments");

        offlineTestShell.execute("   :help   arg1 arg2 ");
    }

    @Test
    public void shouldReturnNothingOnStrangeCommand() {
        Optional<CommandExecutable> exe = offlineTestShell.getCommandExecutable("   :aklxjde   arg1 arg2 ");

        assertFalse(exe.isPresent());
    }

    @Test
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgs cliArgs = CliArgHelper.parse("MATCH (n) RETURN n");

        ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, offlineTestShell, logger);

        if (!(shellRunner instanceof StringShellRunner)) {
            fail("Expected a different runner than: " + shellRunner.getClass().getSimpleName());
        }
    }

    @Test
    public void shouldParseCommandsAndArgs() {
        assertTrue(offlineTestShell.getCommandExecutable(":help").isPresent());
        assertTrue(offlineTestShell.getCommandExecutable(":help :set").isPresent());
        assertTrue(offlineTestShell.getCommandExecutable(":set \"A piece of string\"").isPresent());
    }

    @Test
    public void unsetAlreadyClearedValue() throws CommandException {
        // when
        // then
        assertFalse("Expected param to be unset", offlineTestShell.remove("unknown var").isPresent());
    }

    @Test
    public void setWithSomeBoltError() throws CommandException {
        // then
        thrown.expect(CommandException.class);
        thrown.expectMessage("Failed to set value of parameter");

        // given
        StatementRunner runner = mock(StatementRunner.class);
        when(runner.run(anyString(), anyMapOf(String.class, Object.class))).thenReturn(null);
        BoltStateHandler bh = mockedBoltStateHandler;
        doReturn(runner).when(bh).getStatementRunner();

        CypherShell shell = new CypherShell(logger, bh, mockedPrettyPrinter);

        // when
        shell.set("bob", "99");
    }
}
