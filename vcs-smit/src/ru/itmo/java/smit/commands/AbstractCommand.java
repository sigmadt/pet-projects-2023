package ru.itmo.java.smit.commands;

import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;

import java.io.PrintStream;


/**
 * This class's purpose is to reduce the amount of boilerplate code for creating commands.
 * Set `COLORIZED = true` and output in terminal will be highlighted. Useful for status and log.
 *
 */
public abstract class AbstractCommand {
    protected PrintStream printStream;

    protected SmitBlobManager smitBlobManager;

    protected boolean COLORIZED = true;

    public AbstractCommand() {}


    public void run() throws SmitException {
        innerRun();
        showOutput();
        afterRun();
    }
    public void innerRun() throws SmitException {}

    public void afterRun() throws SmitException {}

    public void showOutput() throws SmitException {}

    public void setPrintStream(PrintStream givenPrintStream) {
        printStream = givenPrintStream;
    }
}
