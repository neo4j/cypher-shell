package org.neo4j.shell;

public class Util {
    public static String[] asArray(String... arguments) {
        return arguments;
    }
    static Object[] asObjectArray(Object... arguments) {
        return arguments;
    }

    public static class NotImplementedYetException extends RuntimeException {
        public NotImplementedYetException(String message) {
            super(message);
        }
    }
}
