package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HelpTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Logger logger = mock(Logger.class);
    private CommandHelper cmdHelper = mock(CommandHelper.class);

    private Command cmd;

    @Before
    public void setup() {

        this.cmd = new Help(logger, cmdHelper);
    }

    @Test
    public void shouldAcceptNoArgs() throws CommandException {
        cmd.execute("");
        // Should not throw
    }

    @Test
    public void shouldNotAcceptTooManyArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob alice");
        fail("Should not accept too many args");
    }

    @Test
    public void helpListing() throws CommandException {
        // given
        List<Command> commandList = new ArrayList<>();

        commandList.add(new FakeCommand("bob"));
        commandList.add(new FakeCommand("bobby"));

        doReturn(commandList).when(cmdHelper).getAllCommands();

        // when
        cmd.execute("");

        // then
        verify(logger).printOut("\nAvailable commands:");
        verify(logger).printOut("  @|BOLD bob  |@ description for bob");
        verify(logger).printOut("  @|BOLD bobby|@ description for bobby");
        verify(logger).printOut("\nFor help on a specific command type:");
        verify(logger).printOut("    :help@|BOLD  command|@\n");
    }

    @Test
    public void helpForCommand() throws CommandException {
        // given
        doReturn(new FakeCommand("bob")).when(cmdHelper).getCommand(eq("bob"));

        // when
        cmd.execute("bob");

        // then
        verify(logger).printOut("\nusage: @|BOLD bob|@usage for bob\n"
                               + "\nhelp for bob\n");
    }

    @Test
    public void helpForNonExistingCommandThrows() throws CommandException {
        // then
        thrown.expect(CommandException.class);
        thrown.expectMessage("No such command: notacommandname");

        // when
        cmd.execute("notacommandname");
    }

    @Test
    public void helpForCommandHasOptionalColon() throws CommandException {
        // given
        doReturn(new FakeCommand(":bob")).when(cmdHelper).getCommand(eq(":bob"));

        // when
        cmd.execute("bob");

        // then
        verify(logger).printOut("\nusage: @|BOLD :bob|@usage for :bob\n"
                + "\nhelp for :bob\n");
    }

    private class FakeCommand implements Command {
        private final String name;

        FakeCommand(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return name;
        }

        @Nonnull
        @Override
        public String getDescription() {
            return "description for " + name;
        }

        @Nonnull
        @Override
        public String getUsage() {
            return "usage for " + name;
        }

        @Nonnull
        @Override
        public String getHelp() {
            return "help for " + name;
        }

        @Nonnull
        @Override
        public List<String> getAliases() {
            return new ArrayList<>();
        }

        @Override
        public void execute(@Nonnull String args) throws ExitException, CommandException {

        }
    }
}
