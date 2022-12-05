package ru.itmo.java.smit.core.manager;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.commands.*;
import ru.itmo.java.smit.commands.branches.CommandCreateBranch;
import ru.itmo.java.smit.commands.branches.CommandMergeWithBranch;
import ru.itmo.java.smit.commands.branches.CommandRemoveBranch;
import ru.itmo.java.smit.commands.branches.CommandShowBranches;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SmitCommandManager {
    @Parameter(names = {"-h", "--help"}, description = "print usage of the app", help = true)
    private boolean helpFlag;

    private final Map<String, AbstractCommand> commands = new HashMap<>();
    public final SmitBlobManager smitBlobManager;
    private final JCommander jCommander;

    private final static String DASHES = "----------------------------";

    public SmitCommandManager(Path directory) {
        smitBlobManager = new SmitBlobManager(directory);

        commands.put("init", new CommandInit(smitBlobManager));
        commands.put("add", new CommandAdd(smitBlobManager));
        commands.put("rm", new CommandRemove(smitBlobManager));
        commands.put("status", new CommandStatus(smitBlobManager));
        commands.put("commit", new CommandCommit(smitBlobManager));
        commands.put("log", new CommandLog(smitBlobManager));
        commands.put("reset", new CommandReset(smitBlobManager));
        commands.put("checkout", new CommandCheckout(smitBlobManager));
        // branches
        commands.put("branch-create", new CommandCreateBranch(smitBlobManager));
        commands.put("branch-remove", new CommandRemoveBranch(smitBlobManager));
        commands.put("show-branches", new CommandShowBranches(smitBlobManager));
        commands.put("merge", new CommandMergeWithBranch(smitBlobManager));

        jCommander = new JCommander(this);
        jCommander.setAllowParameterOverwriting(true);

        for (var cmd : commands.values()) {
            jCommander.addCommand(cmd);
        }
    }

    public Map<String, AbstractCommand> getCommands() {
        return commands;
    }

    /**
     * Parses given command and arguments.
     * @param args vcs command and its arguments.
     * @return true if parsing succeeded, false otherwise.
     */
    private boolean parseCommandAndArgs(String[] args) {
        try {
            jCommander.parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            jCommander.usage();
            System.exit(1);
        }


        if (jCommander.getParsedCommand() == null || helpFlag) {
            jCommander.usage();
            return false;
        }

        return true;
    }


    /**
     * @param ps print stream to write into.
     * @throws SmitException if parsed command throws SmitException.
     */
    public void execute(PrintStream ps) throws SmitException {
        var currCommandName = jCommander.getParsedCommand();

        var currCommandInstance = commands.get(currCommandName);
        currCommandInstance.setPrintStream(ps);

        currCommandInstance.run();
    }


    /**
     * Run for array arguments using given print stream.
     * @param args vcs command and its arguments.
     * @param printStream print stream to write into.
     */
    public void run(String[] args, PrintStream printStream) {
        if (parseCommandAndArgs(args)) {
            try {
                execute(printStream);
            } catch (SmitException e) {
                printStream.println(e.getMessage());
            }
        }
    }

    /**
     * @param args vcs command and its arguments.
     */
    public void run(String[] args) {
        run(args, System.out);
    }

    /**
     * @param concatArgs vcs command and its arguments as single line.
     * @param ps print stream to write into.
     */
    public void run(@NotNull String concatArgs, PrintStream ps) {
        run(concatArgs.split(" "), ps);
        ps.println(DASHES);
    }

    /**
     * Run, default print stream in console.
     * @param concatArgs vcs command and its arguments as single line.
     */
    public void run(String concatArgs) {
        run(concatArgs, System.out);
    }

    /**
     * Same as `run` but exceptions are possible
     * @param args vcs command and its arguments.
     * @param printStream print stream to write into.
     * @throws SmitException if `execute` throws SmitException.
     */
    public void throwableRun(String[] args, PrintStream printStream) throws SmitException {
        if (parseCommandAndArgs(args)) {
            execute(printStream);
        }
    }

    /**
     * @param concatArgs vcs command and its arguments as single line.
     * @param ps print stream to write into.
     * @throws SmitException if `execute` throws SmitException.
     */
    public void throwableRun(@NotNull String concatArgs, PrintStream ps) throws SmitException {
        throwableRun(concatArgs.split(" "), ps);
        ps.println(DASHES);
    }

    /**
     * Same as `run` but exceptions are possible, default prinstream in console.
     * @param concatArgs vcs command and its arguments as single line.
     * @throws SmitException if `execute` throws SmitException.
     */
    public void throwableRun(String concatArgs) throws SmitException {
        throwableRun(concatArgs, System.out);
    }
}
