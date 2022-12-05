package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.core.commit.SmitCommit;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "log", commandDescription = "displays log info")
public class CommandLog extends AbstractCommand {
    @Parameter(description = "from revision")
    List<String> args = new ArrayList<>();

    private SmitCommit currCommit;
    private final String newLine = System.lineSeparator();

    public CommandLog(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        // 1. from revision
        if (args.size() == 1) {
            var revision = args.get(0);
            var revisionHash = smitBlobManager.getRevisionOptions(revision);

            currCommit = smitBlobManager.readCommitByHash(revisionHash);
            return;
        }


        // 2. invalid amount of arguments
        if (args.size() > 1) {
            printStream.println("specify only 1 revision hash");
            return;
        }

        // 3. empty query
        currCommit = smitBlobManager.getHeadCommit();

    }

    @Override
    public void showOutput() throws SmitException {
        var sb = new StringBuilder();
        var printCommit = currCommit;

        while (printCommit != null) {
            sb.append(printCommit.prettyPrint());
            sb.append(newLine);

            printCommit = smitBlobManager.readCommitByHash(printCommit.getPreviousCommit());

            sb.append(newLine);
        }

        var logOutput = sb.toString();
        printStream.println(logOutput);
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }
}
