package org.neo4j.shell.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.parser.StatementParser;

import static java.lang.String.format;
import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * This command reads a cypher file frome the filesystem and executes the statements therein.
 */
public class Source implements Command {
    private static final String COMMAND_NAME = ":source";
    private final CypherShell cypherShell;
    private final StatementParser statementParser;

    public Source( CypherShell cypherShell, StatementParser statementParser ) {
        this.cypherShell = cypherShell;
        this.statementParser = statementParser;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Interactively executes cypher statements from a file";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "[filename]";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Executes Cypher statements from a file";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        String filename = simpleArgParse(argString, 1, 1, COMMAND_NAME, getUsage())[0];

        try ( BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream( new File(filename) )))) {
            bufferedReader.lines()
                    .forEach(line -> statementParser.parseMoreText(line + "\n"));
            List<String> statements = statementParser.consumeStatements();
            for ( String statement : statements )
            {
                cypherShell.execute( statement );
            }
        }
        catch ( IOException e )
        {
            throw new CommandException( format("Cannot find file: '%s'", filename), e);
        }
    }
}
