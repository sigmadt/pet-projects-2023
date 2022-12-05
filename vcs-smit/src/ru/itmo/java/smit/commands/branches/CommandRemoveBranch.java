package ru.itmo.java.smit.commands.branches;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.commands.AbstractCommand;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "branch-remove", commandDescription = "remove existing branch")
public class CommandRemoveBranch extends AbstractCommand {
    @Parameter(description = "name of the branch", arity = 1)
    private List<String> args = new ArrayList<>();

    private String removedBranchName;
    boolean noArgsGiven = false;

    public CommandRemoveBranch(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        if (args.isEmpty()) {
            noArgsGiven = true;
            return;
        }

        // 1. check if branch is valid
        removedBranchName = args.get(0);
        var branches = smitBlobManager.getBranches();
        if (!branches.containsKey(removedBranchName)) {
            throw new SmitException("invalid branch name given, use \"show-branches\" to display available branches");
        }

        // 2. remove given branch and update branches state
        branches.remove(removedBranchName);
        smitBlobManager.setBranches(branches);
    }

    @Override
    public void showOutput() throws SmitException {
        if (noArgsGiven) {
            return;
        }
        printStream.printf("branch <%s> was removed %n", removedBranchName);
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }
}
