package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.core.commit.SmitCommit;

import java.io.IOException;

@Parameters(commandNames = {"commit"}, commandDescription = "make a snapshot of the project")
public class CommandCommit extends AbstractCommand {
    @Parameter(names = {"-m", "--message"}, description = "specifies commit message", required = true)
    private String message;

    @Parameter(names = {"-a", "--author"}, description = "specifies author for commit")
    private String author = "USER";

    private boolean canCommit;

    public CommandCommit(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        makeCommit(message, null);
    }

    @Override
    public void showOutput() throws SmitException {
        if (canCommit) {
            printStream.println("commited successfully!");
            return;
        }

        printStream.println("nothing to commit (create/copy files and use \"add\" to track)");
    }

    public void makeCommit(String message, String branch) throws SmitException {
        try {
            var newCommit = new SmitCommit(message, author, COLORIZED);
            canCommit = newCommit.construct(smitBlobManager, branch);
        } catch (IOException e) {
            throw new SmitException(e);
        }
    }
}
