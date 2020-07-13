package org.neo4j.shell.state;

import org.neo4j.driver.exceptions.Neo4jException;

/**
 * An exception throws if an error occurs while a transaction is open.
 */
public class ErrorWhileInTransactionException extends Neo4jException
{

    public ErrorWhileInTransactionException( String message )
    {
        super( message );
    }

    public ErrorWhileInTransactionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ErrorWhileInTransactionException( String code, String message )
    {
        super( code, message );
    }

    public ErrorWhileInTransactionException( String code, String message, Throwable cause )
    {
        super( code, message, cause );
    }
}
