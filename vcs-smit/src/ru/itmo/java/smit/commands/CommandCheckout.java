package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.core.revision.SmitRevisionOption;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Parameters(commandNames = "checkout", commandDescription = "checkouts to given revision", separators = "--")
public class CommandCheckout extends AbstractCommand {
    @Parameter(description = "commit hash, branch name or head and number")
    private List<String> args = new ArrayList<>();

    @Parameter(names = {"--"}, description = "reset changes for given files")
    private List<String> resetFiles = new ArrayList<>();

    private boolean noArgsGiven = false;

    public CommandCheckout(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        // 1. no args
        if (args.isEmpty()) {
            noArgsGiven = true;
            return;
        }
        // 2. -- option
        if (args.get(0).equals("") && args.get(1).equals("-")) {
            resetFiles.addAll(args.subList(2, args.size()));
            checkoutHelperForUndo(resetFiles);
            return;
        }

        var checkoutOption = args.get(0);
        var revisionOption = smitBlobManager.defineOption(checkoutOption);


        switch (revisionOption) {
            case HEAD_N -> checkoutHelperForHeadN(revisionOption);
            case COMMIT_HASH -> checkoutHelperForCommitHash(revisionOption);
            case BRANCH -> checkoutHelperForBranch(revisionOption);
        }
    }

    @Override
    public void showOutput() throws SmitException {
        if (noArgsGiven) {
            return;
        }

        printStream.println("checkout done");
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }

    public void checkoutHelperForBranch(@NotNull SmitRevisionOption revisionOption) throws SmitException {
        var branchName = revisionOption.getOptionInfo();
        checkoutHelperForBranch(branchName);
    }

    public void checkoutHelperForBranch(String branchName) throws SmitException {
        var headCommit = smitBlobManager.getHeadCommit();
        var headCommitHash = headCommit.getHash();

        smitBlobManager.setHead(headCommitHash);

        // 1. smit reset --hard to revision branch head
        var branchHash = smitBlobManager.getBranches().get(branchName);
        var cmdReset = new CommandReset(smitBlobManager);

        cmdReset.resetByHash(branchHash);

        // 2. update head with given branch
        smitBlobManager.setHead(branchName);
    }

    private void checkoutHelperForCommitHash(@NotNull SmitRevisionOption revisionOption) throws SmitException {
        var commitHash = revisionOption.getOptionInfo();
        checkoutHelperForCommitHash(commitHash);
    }

    private void checkoutHelperForCommitHash(String commitHash) throws SmitException {
        var index = smitBlobManager.getIndex();
        var headCommitHash = smitBlobManager.getHeadCommit().getHash();

        if (!index.isEmpty()) {
            throw new SmitException("index is not empty");
        }

        // 1. turn into detach head state
        smitBlobManager.setHead(headCommitHash);
        printStream.println("You are in 'detached HEAD' state.");

        // 2. reset to commit
        var cmdReset = new CommandReset(smitBlobManager);
        cmdReset.resetByHash(commitHash);
    }

    private void checkoutHelperForHeadN(@NotNull SmitRevisionOption revisionOption) throws SmitException {
        var commitHash = revisionOption.getOptionInfo();
        checkoutHelperForCommitHash(commitHash);
    }

    private void checkoutHelperForUndo(@NotNull List<String> args) throws SmitException {
        var pathArgs = smitBlobManager.getAbsWorkDirPaths(args);

        for (var blobPath : pathArgs) {
            rollBackBlobByPath(blobPath);
        }

    }

    private void rollBackBlobByPath(Path blobPath) throws SmitException {
        // 1. get relative blob path
        var blobName = smitBlobManager.relWorkDirPath(blobPath).toString();

        // 2. blob is not in staging area
        var headCommit = smitBlobManager.getHeadCommit();
        var blobs = headCommit.getBlobs();
        if (!blobs.containsKey(blobName)) {
            return;
        }

        //3. s'all good. now remove blob from index and revert
        var index = smitBlobManager.getIndex();
        index.remove(blobName);

        var absOldPath = smitBlobManager.absWorkDirPath(blobs.get(blobName).getPath());
        var absBlobPath = smitBlobManager.absWorkDirPath(blobPath);
        try {
            smitBlobManager.copyBlobByPath(absOldPath, absBlobPath);
        } catch (IOException e) {
            throw new SmitException(e);
        }
    }

}
