package org.neo4j.shell;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.neo4j.cypher.internal.ast.factory.LiteralInterpreter;
import org.neo4j.cypher.internal.parser.javacc.Cypher;
import org.neo4j.cypher.internal.parser.javacc.ParseException;
import org.neo4j.shell.exception.ParameterException;
import org.neo4j.shell.prettyprint.CypherVariablesFormatter;
import org.neo4j.shell.state.ParamValue;

/**
 * An object which keeps named parameters and allows them them to be set/unset.
 */
public class ShellParameterMap implements ParameterMap
{
    private final Map<String, ParamValue> queryParams = new HashMap<>();
    private LiteralInterpreter interpreter = new LiteralInterpreter();

    @Override
    public Object setParameter( @Nonnull String name, @Nonnull String valueString ) throws ParameterException
    {
        String parameterName = CypherVariablesFormatter.unescapedCypherVariable( name );
        try {
            Object value = new Cypher<>( interpreter,
                                         ParameterException.FACTORY,
                                         new StringReader( valueString ) ).Literal();
            queryParams.put( parameterName, new ParamValue( valueString, value ) );
            return value;
        } catch (ParseException e) {
            throw new ParameterException(e.getMessage());
        }
    }

    @Nonnull
    @Override
    public Map<String,Object> allParameterValues()
    {
        return queryParams.entrySet()
                          .stream()
                          .collect( Collectors.toMap(
                                  Map.Entry::getKey,
                                  value -> value.getValue().getValue()));
    }

    @Nonnull
    @Override
    public Map<String,ParamValue> getAllAsUserInput()
    {
        return queryParams;
    }
}
