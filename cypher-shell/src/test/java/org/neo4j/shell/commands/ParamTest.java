package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.cypher.internal.evaluator.EvaluationException;
import org.neo4j.shell.ParameterMap;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParamTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private ParameterMap mockShell = mock( ParameterMap.class);
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new Param(mockShell);
    }

    @Test
    public void shouldFailIfNoArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("");
    }

    @Test
    public void shouldFailIfOneArg() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
    }

    @Test
    public void setParam() throws EvaluationException, CommandException {
        cmd.execute("bob   9");

        verify(mockShell).setParameter("bob", "9");
    }

    @Test
    public void setLambdasAsParam() throws EvaluationException, CommandException {
        cmd.execute("bob => 9");

        verify(mockShell).setParameter("bob", "9");
    }

    @Test
    public void setLambdasAsParamWithBackticks() throws EvaluationException, CommandException {
        cmd.execute("`bob` => 9");

        verify(mockShell).setParameter("`bob`", "9");
    }

    @Test
    public void setSpecialCharacterParameter() throws EvaluationException, CommandException {
        cmd.execute("bØb   9");

        verify(mockShell).setParameter("bØb", "9");
    }

    @Test
    public void setSpecialCharacterParameterForLambdaExpressions() throws EvaluationException, CommandException {
        cmd.execute("`first=>Name` => \"Bruce\"");

        verify(mockShell).setParameter("`first=>Name`", "\"Bruce\"");
    }

    @Test
    public void setParamWithSpecialCharacters() throws EvaluationException, CommandException {
        cmd.execute("`bob#`   9");

        verify(mockShell).setParameter("`bob#`", "9");
    }

    @Test
    public void setParamWithOddNoOfBackTicks() throws EvaluationException, CommandException {
        cmd.execute(" `bo `` sömething ```   9");

        verify(mockShell).setParameter("`bo `` sömething ```", "9");
    }

    @Test
    public void shouldFailForVariablesWithoutEscaping() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob#   9");

        fail("Expected error");
    }

    @Test
    public void shouldFailForVariablesMixingMapStyleAssignmentAndLambdas() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect usage"));

        cmd.execute("bob: => 9");

        fail("Expected error");
    }

    @Test
    public void shouldFailForEmptyVariables() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("``   9");

        fail("Expected error");
    }

    @Test
    public void shouldFailForInvalidVariables() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("`   9");

        fail("Expected error");
    }

    @Test
    public void shouldFailForVariablesWithoutText() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("```   9");

        fail("Expected error");
    }

    @Test
    public void shouldNotSplitOnSpace() throws EvaluationException, CommandException {
        cmd.execute("bob 'one two'");
        verify(mockShell).setParameter("bob", "'one two'");
    }

    @Test
    public void shouldAcceptUnicodeAlphaNumeric() throws EvaluationException, CommandException {
        cmd.execute("böb 'one two'");
        verify(mockShell).setParameter("böb", "'one two'");
    }

    @Test
    public void shouldAcceptColonFormOfParams() throws EvaluationException, CommandException {
        cmd.execute("bob: one");
        verify(mockShell).setParameter("bob", "one");
    }

    @Test
    public void shouldAcceptForTwoColonsFormOfParams() throws EvaluationException, CommandException {
        cmd.execute("`bob:`: one");
        verify(mockShell).setParameter("`bob:`", "one");

        cmd.execute("`t:om` two");
        verify(mockShell).setParameter("`t:om`", "two");
    }

    @Test
    public void shouldNotExecuteEscapedCypher() throws EvaluationException, CommandException {
        cmd.execute("bob \"RETURN 5 as bob\"");
        verify(mockShell).setParameter("bob", "\"RETURN 5 as bob\"");
    }

    @Test
    public void printUsage() {
        String usage = cmd.getUsage();
        assertEquals(usage, "name => value");
    }
}
