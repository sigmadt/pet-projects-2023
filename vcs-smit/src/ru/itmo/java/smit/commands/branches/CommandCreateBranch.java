package ru.itmo.java.smit.commands.branches;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.commands.AbstractCommand;
import ru.itmo.java.smit.commands.CommandCheckout;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;

import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "branch-create", commandDescription = "create new branch and switch on it")
public class CommandCreateBranch extends AbstractCommand {
    @Parameter(description = "name of the branch", arity = 1)
    private List<String> args = new ArrayList<>();

    private String branchName;
    boolean noArgsGiven = false;

    public CommandCreateBranch(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        if (args.isEmpty()) {
            noArgsGiven = true;
            return;
        }

        var branches = smitBlobManager.getBranches();
        var headCommitHash = smitBlobManager.getHeadCommit().getHash();

        // 1. check if branch in branches
        branchName = args.get(0);
        if (branches.containsKey(branchName)) {
            throw new SmitException("given branch already exist, try to use diffrenet name");
        }

        // 2. put branch in branches
        branches.put(branchName, headCommitHash);
        smitBlobManager.setBranches(branches);

        // 2. checkout to the new branch
        var cmdCheckout = new CommandCheckout(smitBlobManager);
        cmdCheckout.checkoutHelperForBranch(branchName);

    }

    @Override
    public void showOutput() throws SmitException {
        if (noArgsGiven) {
            return;
        }
        printStream.printf("now current branch is: <%s>%n", branchName);
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }
}
