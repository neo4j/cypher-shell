package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.state.ParamValue;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ShellParameterMapTest
{
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private ParameterMap parameterMap;

    @Before
    public void setup() {
        parameterMap = new ShellParameterMap();
    }


    @Test
    public void newParamMapShouldBeEmpty() {
        assertTrue(parameterMap.allParameterValues().isEmpty());
    }

    @Test
    public void setParamShouldAddParamWithSpecialCharactersAndValue() throws CommandException {
        Object result = parameterMap.setParameter("`bo``b`", "99");
        assertEquals(99L, result);
        assertEquals(99L, parameterMap.allParameterValues().get("bo`b"));
    }

    @Test
    public void setParamShouldAddParam() throws CommandException {
        Object result = parameterMap.setParameter("`bob`", "99");
        assertEquals(99L, result);
        assertEquals(99L, parameterMap.allParameterValues().get("bob"));
    }

    @Test
    public void getUserInput() throws CommandException {
        parameterMap.setParameter("`bob`", "99");
        assertEquals( new ParamValue( "99", 99L ), parameterMap.getAllAsUserInput().get("bob"));
    }
}
