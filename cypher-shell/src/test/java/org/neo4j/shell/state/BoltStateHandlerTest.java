package org.neo4j.shell.state;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.test.bolt.FakeSession;
import org.neo4j.shell.test.bolt.FakeTransaction;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BoltStateHandlerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private final Driver mockDriver = mock(Driver.class);
    private OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(mockDriver);

    @Before
    public void setup() {
        when(mockDriver.session()).thenReturn(new FakeSession());
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void closeTransactionAfterRollback() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        assertNotNull("Expected an open transaction", boltStateHandler.getCurrentTransaction());

        FakeTransaction tx = (FakeTransaction) boltStateHandler.getCurrentTransaction();

        boltStateHandler.rollbackTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
        assertNull("Expected tx to be gone", boltStateHandler.getCurrentTransaction());
    }

    @Test
    public void shouldHandleSilentDisconnectExceptions() throws CommandException {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("original exception");

        Driver mockedDriver = mock(Driver.class);
        Session session = mock(Session.class);
        StatementResult resultMock = mock(StatementResult.class);

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(mockedDriver);

        when(mockedDriver.session()).thenReturn(session);
        when(session.run("RETURN 1")).thenReturn(resultMock);
        when(resultMock.consume()).thenThrow(new RuntimeException("original exception"));
        doThrow(new RuntimeException("INIT method message")).when(session).close();

        boltStateHandler.connect();
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();
        FakeTransaction tx = (FakeTransaction) boltStateHandler.getCurrentTransaction();

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
    }

    @Test
    public void shouldRunCypherQuery() throws CommandException {
        boltStateHandler.connect();

        assertEquals("999", boltStateHandler.runCypher("RETURN 999",
                new HashMap<>()).get().getRecords().get(0).get(0).toString());
    }

    @Test
    public void shouldExecuteInSessionByDefault() throws CommandException {
        boltStateHandler.connect();

        Transaction tx = boltStateHandler.getCurrentTransaction();

        assertNull("Did not expect a transaction", tx);
    }

    @Test
    public void onlyUserWillThrow() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Specified username but no password");

        boltStateHandler.connect(new ConnectionConfig("localhost", 1, "user", ""));
    }

    @Test
    public void onlyPassWillThrow() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Specified password but no username");

        boltStateHandler.connect(new ConnectionConfig("localhost", 1, "", "pass"));
    }

    @Test
    public void canOnlyConnectOnce() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Already connected");

        try {
            boltStateHandler.connect();
        } catch (Throwable e) {
            fail("Should not throw here: " + e);
        }

        boltStateHandler.connect();
    }

    @Test
    public void resetClearsTransactionState() throws Exception {
        // given
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        Transaction tx = boltStateHandler.getCurrentTransaction();

        assertNotNull("Expected a transaction", tx);

        // when
        boltStateHandler.reset();

        // then
        assertNull("Transaction state should be reset", boltStateHandler.getCurrentTransaction());
        assertFalse(boltStateHandler.isTransactionOpen());
    }

    @Test
    public void silentDisconnectCleansUp() throws Exception {
        // given
        boltStateHandler.connect();

        Session session = boltStateHandler.session;
        assertNotNull(session);
        assertNotNull(boltStateHandler.driver);

        assertTrue(boltStateHandler.session.isOpen());

        // when
        boltStateHandler.silentDisconnect();

        // then
        assertFalse(session.isOpen());
    }

    /**
     * Bolt state with faked bolt interactions
     */
    private static class OfflineBoltStateHandler extends BoltStateHandler {
        public OfflineBoltStateHandler(Driver driver) {
            super((uri, authToken, config) -> driver);
        }

        public Transaction getCurrentTransaction() {
            return tx;
        }

        public void connect() throws CommandException {
            connect(new ConnectionConfig("", 1, "", ""));
        }
    }
}
