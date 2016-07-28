package org.neo4j.shell.cli;

import org.neo4j.shell.exception.JLineException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * This PrintStream will throw an exception when invoked. This is so that JLine gives us the error, instead of
 * printing directly to STDERR.
 */
class ErrorPassingPrintStream extends PrintStream {

    ErrorPassingPrintStream() {
        super(System.err);
    }

    @Override
    public void write(int b) {
        throw new JLineException();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        throw new JLineException();
    }

    @Override
    public void print(boolean b) {
        throw new JLineException();
    }

    @Override
    public void print(char c) {
        throw new JLineException();
    }

    @Override
    public void print(int i) {
        throw new JLineException();
    }

    @Override
    public void print(long l) {
        throw new JLineException();
    }

    @Override
    public void print(float f) {
        throw new JLineException();
    }

    @Override
    public void print(double d) {
        throw new JLineException();
    }

    @Override
    public void print(char[] s) {
        throw new JLineException();
    }

    @Override
    public void print(String s) {
        throw new JLineException();
    }

    @Override
    public void print(Object obj) {
        // will receive a string with message, before actual exception is printed. waiting for exception, so ignore
    }

    @Override
    public void println() {
        // Newlines are printed before stacktraces by JLine, so just ignore them
    }

    @Override
    public void println(boolean x) {
        throw new JLineException();
    }

    @Override
    public void println(char x) {
        throw new JLineException();
    }

    @Override
    public void println(int x) {
        throw new JLineException();
    }

    @Override
    public void println(long x) {
        throw new JLineException();
    }

    @Override
    public void println(float x) {
        throw new JLineException();
    }

    @Override
    public void println(double x) {
        throw new JLineException();
    }

    @Override
    public void println(char[] x) {
        throw new JLineException();
    }

    @Override
    public void println(String x) {
        throw new JLineException();
    }

    @Override
    public void println(Object x) {
        // exceptions, when printing stacktraces, will first print themselves
        if (x instanceof Throwable) {
            throw new JLineException(((Throwable) x).getMessage());
        }
        throw new JLineException(); //java.lang.IllegalArgumentException: !bang": event not found
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return format(format, args);
    }

    @Override
    public PrintStream format(String format, Object... args) {
        // JLine will print a line such as "[%s]", "ERROR". Exception will be printed later, wait for that.
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return format(format, args);
    }

    @Override
    public PrintStream append(CharSequence csq) {
        throw new JLineException();
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        throw new JLineException();
    }

    @Override
    public PrintStream append(char c) {
        throw new JLineException();
    }

    @Override
    public void write(byte[] b) throws IOException {
        throw new JLineException();
    }
}
