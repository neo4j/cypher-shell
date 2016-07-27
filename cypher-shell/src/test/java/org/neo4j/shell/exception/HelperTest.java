package org.neo4j.shell.exception;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;

import static org.junit.Assert.assertEquals;
import static org.neo4j.shell.exception.Helper.getFormattedMessage;

public class HelperTest {
    @Test
    public void testSimple() {
        assertEquals("@|RED yahoo|@", getFormattedMessage(new NullPointerException("yahoo")));
    }

    @Test
    public void testNested() {
        assertEquals("@|RED nested|@", getFormattedMessage(new ClientException("outer",
                new CommandException("nested"))));
    }

    @Test
    public void testNestedDeep() {
        assertEquals("@|RED nested deep|@", getFormattedMessage(
                new ClientException("outer",
                        new ClientException("nested",
                                new ClientException("nested deep")))));
    }

    @Test
    public void testNullMessage() {
        assertEquals("@|RED ClientException|@", getFormattedMessage(new ClientException(null)));
        assertEquals("@|RED NullPointerException|@",
                getFormattedMessage(new ClientException("outer", new NullPointerException(null))));
    }
}
