package org.neo4j.shell.state;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.TriFunction;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.test.bolt.FakeDriver;
import org.neo4j.shell.test.bolt.FakeSession;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.shell.DatabaseManager.ABSENT_DB_NAME;
import static org.neo4j.shell.DatabaseManager.DEFAULT_DEFAULT_DB_NAME;

public class BoltStateHandlerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private final Driver mockDriver = mock(Driver.class);
    private OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(mockDriver);

    @Before
    public void setup() {
        when(mockDriver.session(any())).thenReturn(new FakeSession());
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void versionIsEmptyBeforeConnect() throws CommandException {
        assertFalse(boltStateHandler.isConnected());
        assertEquals("", boltStateHandler.getServerVersion());
    }

    @Test
    public void versionIsEmptyIfDriverReturnsNull() throws CommandException {
        RecordingDriverProvider provider = new RecordingDriverProvider() {
            @Override
            public Driver apply(String uri, AuthToken authToken, Config config) {
                super.apply(uri, authToken, config);
                return new FakeDriver();
            }
        };
        BoltStateHandler handler = new BoltStateHandler(provider, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME);
        handler.connect(config);

        assertEquals("", handler.getServerVersion());
    }

    @Test
    public void versionIsNotEmptyAfterConnect() throws CommandException {
        Driver driverMock = stubResultSummaryInAnOpenSession(mock(Result.class), mock(Session.class), "Neo4j/9.4.1-ALPHA");

        BoltStateHandler handler = new BoltStateHandler((s, authToken, config) -> driverMock, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME);
        handler.connect(config);

        assertEquals("9.4.1-ALPHA", handler.getServerVersion());
    }

    @Test
    public void actualDatabaseNameIsNotEmptyAfterConnect() throws CommandException {
        Driver driverMock =
                stubResultSummaryInAnOpenSession(mock(Result.class), mock(Session.class), "Neo4j/9.4.1-ALPHA", "my_default_db");

        BoltStateHandler handler = new BoltStateHandler((s, authToken, config) -> driverMock, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME);
        handler.connect(config);

        assertEquals("my_default_db", handler.getActualDatabaseAsReportedByServer());
    }

    @Test
    public void exceptionFromRunQueryDoesNotResetActualDatabaseNameToUnresolved() throws CommandException {
        Session sessionMock = mock(Session.class);
        Result resultMock = mock(Result.class);
        Driver driverMock =
                stubResultSummaryInAnOpenSession(resultMock, sessionMock, "Neo4j/9.4.1-ALPHA", "my_default_db");

        ClientException databaseNotFound = new ClientException("Neo.ClientError.Database.DatabaseNotFound", "blah");

        when(sessionMock.run(any(Query.class)))
                .thenThrow(databaseNotFound)
                .thenReturn(resultMock);

        BoltStateHandler handler = new BoltStateHandler((s, authToken, config) -> driverMock, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME);
        handler.connect(config);

        try {
            handler.runCypher("RETURN \"hello\"", Collections.emptyMap());
            fail("should fail on runCypher");
        } catch (Exception e) {
            assertThat(e, is(databaseNotFound));
            assertEquals("my_default_db", handler.getActualDatabaseAsReportedByServer());
        }
    }

    @Test
    public void closeTransactionAfterRollback() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        assertTrue(boltStateHandler.isTransactionOpen());

        boltStateHandler.rollbackTransaction();

        assertFalse(boltStateHandler.isTransactionOpen());
    }

    @Test
    public void exceptionsFromSilentDisconnectAreSuppressedToReportOriginalErrors() throws CommandException {
        Session session = mock(Session.class);
        Result resultMock = mock(Result.class);

        RuntimeException originalException = new RuntimeException("original exception");
        RuntimeException thrownFromSilentDisconnect = new RuntimeException("exception from silent disconnect");

        Driver mockedDriver = stubResultSummaryInAnOpenSession(resultMock, session, "neo4j-version");
        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(mockedDriver);

        when(resultMock.consume()).thenThrow(originalException);
        doThrow(thrownFromSilentDisconnect).when(session).close();

        try {
            boltStateHandler.connect();
            fail("should fail on silent disconnect");
        } catch (Exception e) {
            assertThat(e.getSuppressed()[0], is(thrownFromSilentDisconnect));
            assertThat(e, is(originalException));
        }
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();
        assertTrue(boltStateHandler.isTransactionOpen());

        boltStateHandler.commitTransaction();

        assertFalse(boltStateHandler.isTransactionOpen());
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
    public void beginNeedsToInitialiseTransactionStatements() throws CommandException {
        boltStateHandler.connect();

        boltStateHandler.beginTransaction();
        assertTrue(boltStateHandler.isTransactionOpen());
    }

    @Test
    public void whenInTransactionHandlerLetsTransactionDoTheWork() throws CommandException {
        Transaction transactionMock = mock(Transaction.class);
        Session sessionMock = mock(Session.class);
        when(sessionMock.beginTransaction()).thenReturn(transactionMock);
        Driver driverMock = stubResultSummaryInAnOpenSession(mock(Result.class), sessionMock, "neo4j-version");

        Result result = mock(Result.class);

        when(transactionMock.run((Query)anyObject())).thenReturn(result);

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(driverMock);
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();
        BoltResult boltResult = boltStateHandler.runCypher("UNWIND [1,2] as num RETURN *", Collections.emptyMap()).get();
        assertEquals(result, boltResult.iterate());

        boltStateHandler.commitTransaction();

        assertFalse(boltStateHandler.isTransactionOpen());

    }

    @Test
    public void rollbackNeedsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        assertFalse(boltStateHandler.isConnected());

        boltStateHandler.rollbackTransaction();
    }

    @Test
    public void executeNeedsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        boltStateHandler.runCypher("", Collections.emptyMap());
    }

    @Test
    public void shouldExecuteInTransactionIfOpen() throws CommandException {
        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        assertTrue("Expected a transaction", boltStateHandler.isTransactionOpen());
    }

    @Test
    public void shouldRunCypherQuery() throws CommandException {
        Session sessionMock = mock(Session.class);
        Result resultMock = mock(Result.class);
        Record recordMock = mock(Record.class);
        Value valueMock = mock(Value.class);

        Driver driverMock = stubResultSummaryInAnOpenSession(resultMock, sessionMock, "neo4j-version");

        when(resultMock.list()).thenReturn(asList(recordMock));

        when(valueMock.toString()).thenReturn("999");
        when(recordMock.get(0)).thenReturn(valueMock);
        when(sessionMock.run(any(Query.class))).thenReturn(resultMock);

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(driverMock);

        boltStateHandler.connect();

        BoltResult boltResult = boltStateHandler.runCypher("RETURN 999",
                new HashMap<>()).get();
        verify(sessionMock).run(any(Query.class));

        assertEquals("999", boltResult.getRecords().get(0).get(0).toString());
    }

    @Test
    public void triesAgainOnSessionExpired() throws Exception {
        Session sessionMock = mock(Session.class);
        Result resultMock = mock(Result.class);
        Record recordMock = mock(Record.class);
        Value valueMock = mock(Value.class);

        Driver driverMock = stubResultSummaryInAnOpenSession(resultMock, sessionMock, "neo4j-version");

        when(resultMock.list()).thenReturn(asList(recordMock));

        when(valueMock.toString()).thenReturn("999");
        when(recordMock.get(0)).thenReturn(valueMock);
        when(sessionMock.run(any(Query.class)))
                .thenThrow(new SessionExpiredException("leaderswitch"))
                .thenReturn(resultMock);

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(driverMock);

        boltStateHandler.connect();
        BoltResult boltResult = boltStateHandler.runCypher("RETURN 999",
                new HashMap<>()).get();

        verify(driverMock, times(2)).session(any());
        verify(sessionMock, times(2)).run(any(Query.class));

        assertEquals("999", boltResult.getRecords().get(0).get(0).toString());
    }

    @Test
    public void shouldExecuteInSessionByDefault() throws CommandException {
        boltStateHandler.connect();

        assertFalse("Did not expect a transaction", boltStateHandler.isTransactionOpen());
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
        Driver driverMock = stubResultSummaryInAnOpenSession(mock(Result.class), sessionMock, "neo4j-version");

        OfflineBoltStateHandler boltStateHandler = new OfflineBoltStateHandler(driverMock);

        boltStateHandler.connect();
        boltStateHandler.beginTransaction();

        // when
        boltStateHandler.reset();

        // then
        verify(sessionMock).reset();
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
        BoltStateHandler handler = new BoltStateHandler(provider, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", false, ABSENT_DB_NAME);
        handler.connect(config);
        assertFalse(provider.config.encrypted());
    }

    @Test
    public void turnOnEncryptionIfRequested() throws CommandException {
        RecordingDriverProvider provider = new RecordingDriverProvider();
        BoltStateHandler handler = new BoltStateHandler(provider, false);
        ConnectionConfig config = new ConnectionConfig("bolt://", "", -1, "", "", true, ABSENT_DB_NAME);
        handler.connect(config);
        assertTrue(provider.config.encrypted());
    }

    private Driver stubResultSummaryInAnOpenSession(Result resultMock, Session sessionMock, String version) {
        return stubResultSummaryInAnOpenSession(resultMock, sessionMock, version, DEFAULT_DEFAULT_DB_NAME);
    }

    private Driver stubResultSummaryInAnOpenSession(Result resultMock, Session sessionMock, String version, String databaseName) {
        Driver driverMock = mock(Driver.class);
        ResultSummary resultSummary = mock(ResultSummary.class);
        ServerInfo serverInfo = mock(ServerInfo.class);
        DatabaseInfo databaseInfo = mock(DatabaseInfo.class);

        when(resultSummary.server()).thenReturn(serverInfo);
        when(serverInfo.version()).thenReturn(version);
        when(resultMock.consume()).thenReturn(resultSummary);
        when(resultSummary.database()).thenReturn(databaseInfo);
        when(databaseInfo.name()).thenReturn(databaseName);

        when(sessionMock.isOpen()).thenReturn(true);
        when(sessionMock.run("RETURN 1")).thenReturn(resultMock);
        when(driverMock.session(any())).thenReturn(sessionMock);

        return driverMock;
    }

    /**
     * Bolt state with faked bolt interactions
     */
    private static class OfflineBoltStateHandler extends BoltStateHandler {

        public OfflineBoltStateHandler(Driver driver) {
            super((uri, authToken, config) -> driver, false);
        }

        public void connect() throws CommandException {
            connect(new ConnectionConfig("bolt://", "", 1, "", "", false, ABSENT_DB_NAME));
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
