package org.neo4j.shell.exception;

import org.neo4j.cypher.internal.ast.factory.ASTExceptionFactory;

public class ParameterException extends IllegalArgumentException {
    public ParameterException(String msg) {
        super(msg);
    }

    public static final ASTExceptionFactory FACTORY = new ASTExceptionFactory() {
        @Override
        public Exception syntaxException(Exception e) {
            return new ParameterException(e.getMessage());
        }

        @Override
        public Exception invalidUnicodeLiteral(String s) {
            return new ParameterException(s);
        }
    };
}
