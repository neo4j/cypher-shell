package org.neo4j.shell.state;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.*;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.TriFunction;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.test.bolt.FakeDriver;
import org.neo4j.shell.test.bolt.FakeSession;
import org.neo4j.shell.test.bolt.FakeTransaction;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void resetSessionOnReset() throws Exception {
        // given
        Session sessionMock = mock(Session.class);
        StatementResult resultMock = mock(StatementResult.class);
        Driver driverMock = mock(Driver.class);
        Transaction transactionMock = mock(Transaction.class);

        when(driverMock.session()).thenReturn(sessionMock);
        when(sessionMock.run("RETURN 1")).thenReturn(resultMock);
        when(sessionMock.isOpen()).thenReturn(true);
        when(sessionMock.beginTransaction()).thenReturn(transactionMock);

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(driverMock);

        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        // when
        boltStateHandler.reset();

        // then
        verify(sessionMock).reset();
        verify(transactionMock).failure();
        verify(transactionMock).close();
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

    @Test
    public void turnOffEncryptionIfRequested() throws CommandException {
        RecordingDriverProvider provider = new RecordingDriverProvider();
        BoltStateHandler handler = new BoltStateHandler(provider);
        ConnectionConfig config = new ConnectionConfig("", -1, "", "", false);
        handler.connect(config);
        assertEquals(Config.EncryptionLevel.NONE, provider.config.encryptionLevel());
    }

    @Test
    public void turnOnEncryptionIfRequested() throws CommandException {
        RecordingDriverProvider provider = new RecordingDriverProvider();
        BoltStateHandler handler = new BoltStateHandler(provider);
        ConnectionConfig config = new ConnectionConfig("", -1, "", "", true);
        handler.connect(config);
        assertEquals(Config.EncryptionLevel.REQUIRED, provider.config.encryptionLevel());
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
            connect(new ConnectionConfig("", 1, "", "", false));
        }
    }

    private class RecordingDriverProvider implements TriFunction<String, AuthToken, Config, Driver> {
        public Config config;

        @Override
        public Driver apply(String uri, AuthToken authToken, Config config) {
            this.config = config;
            return new FakeDriver();
        }
    }
}
