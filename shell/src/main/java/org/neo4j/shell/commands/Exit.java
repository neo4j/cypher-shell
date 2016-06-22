package org.neo4j.shell.commands;

import org.neo4j.shell.Command;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Command to exit the shell. Equivalent to hitting Ctrl-D.
 */
public class Exit implements Command {
    public static final String COMMAND_NAME = ":exit";

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHelp() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList(":quit");
    }

    @Override
    public Object execute(@Nonnull List<String> args) throws ExitException {
        System.out.println("Exiting. Bye bye.");

        throw new ExitException(0);
    }

    public class ExitException extends Error {
        private final int code;

        public ExitException(int code) {
            super();
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
