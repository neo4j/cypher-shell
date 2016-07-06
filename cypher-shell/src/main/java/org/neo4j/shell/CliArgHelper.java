package org.neo4j.shell;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.action.StoreConstArgumentAction;
import net.sourceforge.argparse4j.inf.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.neo4j.shell.CliArgHelper.FailBehavior.FAIL_AT_END;
import static org.neo4j.shell.CliArgHelper.FailBehavior.FAIL_FAST;

/**
 * Command line argument parsing and related stuff
 */
public class CliArgHelper {

    public enum FailBehavior {
        FAIL_FAST,
        FAIL_AT_END
    }


    public static final Pattern addressArgPattern =
            Pattern.compile("\\s*((?<username>\\w+):(?<password>[^\\s]+)@)?(?<host>[\\d\\.\\w]+)?(:(?<port>\\d+))?\\s*");

    @Nonnull
    public static CliArgs parse(@Nonnull final String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("neo4j-shell")
                .defaultHelp(true)
                .description("A command line shell where you can execute Cypher against an instance of Neo4j");

        ArgumentGroup connGroup = parser.addArgumentGroup("connection arguments");
        connGroup.addArgument("-a", "--address")
                .help("address and port to connect to")
                .setDefault("localhost:7687");
        connGroup.addArgument("-u", "--username")
                .setDefault("")
                .help("username to connect as");
        connGroup.addArgument("-p", "--password")
                .setDefault("")
                .help("password to connect with");

        MutuallyExclusiveGroup failGroup = parser.addMutuallyExclusiveGroup();
        failGroup.addArgument("-ff", "--fail-fast")
                .help("exit and report failure on first error when reading from file")
                .dest("fail-behavior")
                .setConst(FAIL_FAST)
                .action(new StoreConstArgumentAction());
        failGroup.addArgument("-fae", "--fail-at-end")
                .help("exit and report failures at end of input when reading from file")
                .dest("fail-behavior")
                .setConst(FAIL_AT_END)
                .action(new StoreConstArgumentAction());
        parser.setDefault("fail-behavior", FAIL_FAST);

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

        cliArgs.setHost(m.group("host"), "localhost");
        // Safe, regex only matches integers
        String portString = m.group("port");
        cliArgs.setPort(portString == null ? 7687 : Integer.parseInt(portString));
        // Also parse username and password from address if available
        cliArgs.setUsername(m.group("username"), "");
        cliArgs.setPassword(m.group("password"), "");

        // Only overwrite user/pass from address string if the arguments were specified
        String user = ns.getString("username");
        if (!user.isEmpty()) {
            cliArgs.setUsername(user, cliArgs.username);
        }
        String pass = ns.getString("password");
        if (!pass.isEmpty()) {
            cliArgs.setPassword(pass, cliArgs.password);
        }

        // Other arguments
        // Fail behavior as sensible default and returns a proper type
        cliArgs.setFailBehavior(ns.get("fail-behavior"));

        return cliArgs;
    }


    public static class CliArgs {
        private boolean suppressColor = false;
        private String host = "localhost";
        private int port = 7687;
        private String username = "";
        private String password = "";
        private FailBehavior failBehavior = FailBehavior.FAIL_FAST;

        public boolean getSuppressColor() {
            return suppressColor;
        }

        /**
         * Set the host to the primary value, or if null, the fallback value.
         */
        void setHost(@Nullable String primary, @Nonnull String fallback) {
            host = primary == null ? fallback : primary;
        }

        /**
         * Set the port to the value.
         */
        void setPort(int port) {
            this.port = port;
        }

        /**
         * Set the username to the primary value, or if null, the fallback value.
         */
        void setUsername(@Nullable String primary, @Nonnull String fallback) {
            username = primary == null ? fallback : primary;
        }

        /**
         * Set the password to the primary value, or if null, the fallback value.
         */
        void setPassword(@Nullable String primary, @Nonnull String fallback) {
            password = primary == null ? fallback : primary;
        }

        void setFailBehavior(FailBehavior failBehavior) {
            this.failBehavior = failBehavior;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public FailBehavior getFailBehavior() {
            return failBehavior;
        }
    }

    public static class CliArgParsingException extends Exception {
        public CliArgParsingException(String msg) {
            super(msg);
        }
    }
}
