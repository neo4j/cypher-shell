package org.neo4j.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.neo4j.cypher.internal.evaluator.EvaluationException;
import org.neo4j.cypher.internal.evaluator.Evaluator;
import org.neo4j.cypher.internal.evaluator.ExpressionEvaluator;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.prettyprint.CypherVariablesFormatter;
import org.neo4j.shell.state.ParamValue;

/**
 * An object which keeps named parameters and allows them them to be set/unset.
 */
public class ShellParameterMap implements ParameterMap
{
    private final Map<String, ParamValue> queryParams = new HashMap<>();
    private ExpressionEvaluator evaluator = Evaluator.expressionEvaluator();

    @Override
    public Object setParameter( @Nonnull String name, @Nonnull String valueString ) throws CommandException
    {
        try {
            String parameterName = CypherVariablesFormatter.unescapedCypherVariable( name);
            Object value = evaluator.evaluate(valueString, Object.class);
            queryParams.put(parameterName, new ParamValue(valueString, value));
            return value;
        } catch ( EvaluationException e ) {
            throw new CommandException(e.getMessage(), e);
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
