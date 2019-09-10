package org.neo4j.shell.cli;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.util.Map;

import org.neo4j.cypher.internal.evaluator.EvaluationException;
import org.neo4j.shell.ParameterMap;
import org.neo4j.shell.util.ParameterSetter;

/**
 * Action that adds arguments to a ParameterMap.
 * This action always consumes an argument.
 */
public class AddParamArgumentAction extends ParameterSetter<ArgumentParserException> implements ArgumentAction
{
    /**
     * @param parameterMap the ParameterMap to add parameters to.
     */
    AddParamArgumentAction( ParameterMap parameterMap )
    {
        super(parameterMap);
    }

    @Override
    public void run( ArgumentParser parser, Argument arg, Map<String,Object> attrs, String flag, Object value ) throws ArgumentParserException
    {
        try
        {
            execute( value.toString() );
        }
        catch ( Exception e )
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

    @Override
    protected void onWrongUsage()
    {
        throw new IllegalArgumentException("Incorrect usage.\nusage: --param  \"name => value\"");
    }

    @Override
    protected void onWrongNumberOfArguments()
    {
        throw new IllegalArgumentException("Incorrect number of arguments.\nusage: --param  \"name => value\"");
    }

    @Override
    protected void onEvaluationException( EvaluationException e )
    {
        throw new RuntimeException( e.getMessage(), e );
    }
}
