package org.neo4j.shell.exception;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;

import static org.junit.Assert.assertEquals;
import static org.neo4j.shell.exception.Helper.getSensibleMsg;

public class HelperTest {
    @Test
    public void testSimple() {
        assertEquals("yahoo", getSensibleMsg(new NullPointerException("yahoo")));
    }

    @Test
    public void testNested() {
        assertEquals("nested", getSensibleMsg(new ClientException("outer", new CommandException("nested"))));
    }

    @Test
    public void testNestedDeep() {
        assertEquals("nested deep", getSensibleMsg(
                new ClientException("outer",
                        new ClientException("nested",
                                new ClientException("nested deep")))));
    }

    @Test
    public void testNullMessage() {
        assertEquals("ClientException", getSensibleMsg(new ClientException(null)));
        assertEquals("NullPointerException",
                getSensibleMsg(new ClientException("outer", new NullPointerException(null))));
    }
}
