package org.neo4j.shell.test;

public class Util {
    public static String[] asArray(String... arguments) {
        return arguments;
    }

    public static class NotImplementedYetException extends RuntimeException {
        public NotImplementedYetException(String message) {
            super(message);
        }
    }

    /**
     * Generate the control code for the specified character. For example, give this method 'C', and it will return
     * the code for `Ctrl-C`, which you can append to an inputbuffer for example, in order to simulate the  user
     * pressing Ctrl-C.
     *
     * @param let character to generate code for, must be between A and Z
     * @return control code for given character
     */
    public static char ctrl(final char let) {
        if (let < 'A' || let > 'Z') {
            throw new IllegalArgumentException("Cannot generate CTRL code for "
                    + "char '" + let + "' (" + ((int) let) + ")");
        }

        int result = ((int) let) - 'A' + 1;
        return (char) result;
    }
}
