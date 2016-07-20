package org.neo4j.shell.state;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.TestTransaction;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class BoltStateHandlerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    Logger logger = mock(Logger.class);
    OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler();

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void closeTransactionAfterRollback() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        assertNotNull("Expected an open transaction", boltStateHandler.getCurrentTransaction());

        TestTransaction tx = (TestTransaction) boltStateHandler.getCurrentTransaction();

        boltStateHandler.rollbackTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
        assertNull("Expected tx to be gone", boltStateHandler.getCurrentTransaction());
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();
        TestTransaction tx = (TestTransaction) boltStateHandler.getCurrentTransaction();

        assertNotNull("Expected an open transaction", tx);

        boltStateHandler.commitTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertTrue("Transaction should be successful", tx.isSuccess());
        assertNull("Expected tx to be gone", boltStateHandler.getCurrentTransaction());
    }

    @Test
    public void beginNeedsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        assertFalse(boltStateHandler.isConnected());

        boltStateHandler.beginTransaction();
    }

    @Test
    public void commitNeedsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        assertFalse(boltStateHandler.isConnected());

        boltStateHandler.commitTransaction();
    }

    @Test
    public void rollbackNeedsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        assertFalse(boltStateHandler.isConnected());

        boltStateHandler.rollbackTransaction();
    }

    @Test
    public void shouldExecuteInTransactionIfOpen() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        Transaction tx = boltStateHandler.getCurrentTransaction();
        assertNotNull("Expected a transaction", tx);

        assertEquals("Transaction should be the runner", tx, boltStateHandler.getStatementRunner());
    }

    @Test
    public void shouldExecuteInSessionByDefault() throws CommandException {
        Transaction tx = boltStateHandler.getCurrentTransaction();
        assertNull("Did not expect a transaction", tx);

        assertEquals("Transaction should be the runner",
                boltStateHandler.getCurrentSession(), boltStateHandler.getStatementRunner());
    }
}
