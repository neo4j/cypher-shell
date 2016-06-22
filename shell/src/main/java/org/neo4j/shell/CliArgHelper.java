package org.neo4j.shell;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command line argument parsing and related stuff
 */
public class CliArgHelper {
    public static final Pattern addressArgPattern =
            Pattern.compile("\\s*((?<username>\\w+):(?<password>[^\\s]+)@)?(?<host>[\\d\\.\\w]+)?(:(?<port>\\d+))?\\s*");

    @Nonnull
    public static CliArgs parse(@Nonnull final String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("neo4j-shell")
                                               .defaultHelp(true)
                                               .description("A command line shell where you can execute Cypher against an instance of Neo4j");

        parser.addArgument("-a", "--address")
                .help("Address and port to connect to")
                .setDefault("localhost:7687");
        parser.addArgument("-u", "--username")
              .setDefault("")
                .help("Username to connect as");
        parser.addArgument("-p", "--password")
              .setDefault("")
                .help("Password to connect with");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        CliArgs cliArgs = new CliArgs();

        // Parse address string
        String address = ns.getString("address");
        Matcher m = addressArgPattern.matcher(address);
        if (!m.matches()) {
            parser.printUsage();
            System.err.println("neo4j-shell: error: Failed to parse address: '" + address + "'");
            System.err.println("\n  Address should be of the form: [username:password@][host][:port]");
            System.exit(1);
        }

        cliArgs.host(m.group("host"), "localhost");
        // Safe, regex only matches integers
        String portString = m.group("port");
        cliArgs.port(portString == null ? 7687 : Integer.parseInt(portString));
        // Also parse username and password from address if available
        cliArgs.username(m.group("username"), "");
        cliArgs.password(m.group("password"), "");

        // Only overwrite user/pass from address string if the arguments were specified
        String user = ns.getString("username");
        if (!user.isEmpty()) {
            cliArgs.username(user, cliArgs.username);
        }
        String pass = ns.getString("password");
        if (!pass.isEmpty()) {
            cliArgs.password(pass, cliArgs.password);
        }

        return cliArgs;
    }


    public static class CliArgs {
        private boolean suppressColor = false;
        private String host = "localhost";
        private int port = 7687;
        private String username = "";
        private String password = "";

        public boolean suppressColor() {
            return suppressColor;
        }

        /**
         * Set the host to the primary value, or if null, the fallback value.
         */
        void host(@Nullable String primary, @Nonnull String fallback) {
            host = primary == null ? fallback : primary;
        }

        /**
         * Set the port to the value.
         */
        void port(int port) {
            this.port = port;
        }

        /**
         * Set the username to the primary value, or if null, the fallback value.
         */
        void username(@Nullable String primary, @Nonnull String fallback) {
            username = primary == null ? fallback : primary;
        }

        /**
         * Set the password to the primary value, or if null, the fallback value.
         */
        void password(@Nullable String primary, @Nonnull String fallback) {
            password = primary == null ? fallback : primary;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }
    }

    public static class CliArgParsingException extends Exception {
        public CliArgParsingException(String msg) {
            super(msg);
        }
    }
}
