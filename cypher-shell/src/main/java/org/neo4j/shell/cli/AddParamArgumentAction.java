package org.neo4j.shell.cli;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.util.Map;

import org.neo4j.shell.ParameterMap;
import org.neo4j.shell.commands.Param;
import org.neo4j.shell.exception.CommandException;

/**
 * Action that adds arguments to a ParameterMap.
 * This action always consumes an argument.
 */
public class AddParamArgumentAction implements ArgumentAction
{
    private final Param paramCommand;

    /**
     * @param parameterMap the ParameterMap to add parameters to,
     */
    AddParamArgumentAction( ParameterMap parameterMap )
    {
        paramCommand = new Param( parameterMap, false );
    }

    @Override
    public void run( ArgumentParser parser, Argument arg, Map<String,Object> attrs, String flag, Object value ) throws ArgumentParserException
    {
        try
        {
            paramCommand.execute( value.toString() );
        }
        catch ( CommandException e )
        {
            throw new ArgumentParserException(e.getMessage(), e, parser);
        }
    }

    @Override
    public void onAttach( Argument arg )
    {

    }

    @Override
    public boolean consumeArgument()
    {
        return true;
    }
}
