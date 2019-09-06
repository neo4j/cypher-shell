package org.neo4j.shell.commands;

import org.neo4j.cypher.internal.evaluator.EvaluationException;
import org.neo4j.shell.ParameterMap;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.util.ParameterSetter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This command sets a variable to a name, for use as query parameter.
 */
public class Param extends ParameterSetter<CommandException> implements Command {
    private final static String COMMAND_NAME = ":param";

    /**
     * @param parameterMap the map to set parameters in
     */
    public Param(@Nonnull final ParameterMap parameterMap) {
        super(parameterMap);
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Set the value of a query parameter";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "name => value" ;
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Set the specified query parameter to the value given";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    protected void onWrongUsage() throws CommandException
    {
        throw new CommandException(AnsiFormattedText.from("Incorrect usage.\nusage: ")
                                                    .bold().append( COMMAND_NAME ).boldOff().append( " ").append( getUsage()));
    }

    @Override
    protected void onWrongNumberOfArguments() throws CommandException
    {
        throw new CommandException(AnsiFormattedText.from("Incorrect number of arguments.\nusage: ")
                                                    .bold().append( COMMAND_NAME ).boldOff().append( " ").append( getUsage()));
    }

    @Override
    protected void onEvaluationException( EvaluationException e ) throws CommandException
    {
        throw new CommandException( e.getMessage(), e );
    }
}
