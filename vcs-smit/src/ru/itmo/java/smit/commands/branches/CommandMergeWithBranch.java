package ru.itmo.java.smit.commands.branches;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.commands.AbstractCommand;
import ru.itmo.java.smit.commands.CommandAdd;
import ru.itmo.java.smit.commands.CommandCommit;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.blobs.SmitStagedStatus;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * There a lot of strategies to resolve merge conflicts.
 * For this implementation I decided to choose "-Xtheirs", for more information visit:
 * https://git-scm.com/docs/merge-strategies
 *
 * */
@Parameters(commandNames = "merge", commandDescription = "merge given brunch with current")
public class CommandMergeWithBranch extends AbstractCommand {
    @Parameter(description = "name of the branch", arity = 1)
    private List<String> args = new ArrayList<>();

    private String mergeBranchName;
    boolean noArgsGiven = false;

    public CommandMergeWithBranch(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        if (args.isEmpty()) {
            noArgsGiven = true;
            return;
        }
        mergeBranchName = args.get(0);
        mergeWithBranch(mergeBranchName);

    }

    @Override
    public void showOutput() throws SmitException {
        if (noArgsGiven) {
            return;
        }

        printStream.println("merge is done");
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }

    private void mergeWithBranch(String givenBranch) throws SmitException {
        // 1. check if given branch is valid
        var branches = smitBlobManager.getBranches();
        if (!branches.containsKey(givenBranch)) {
            throw new SmitException("invalid branch name given, use \"show-branches\" to display available branches");
        }

        // 2. check if detached head state
        var headBranch = smitBlobManager.getBranchHead();
        if (headBranch == null) {
            throw new SmitException("detached HEAD state");
        }

        // 3. merge!
        var hashCommit = branches.get(givenBranch);
        var commit = smitBlobManager.readCommitByHash(hashCommit);

        var blobs = commit.getBlobs();
        var currBranchBlobs = smitBlobManager.getHeadCommit().getBlobs();


        // 3.1 collect blob paths
        List<Path> newBlobPaths = collectBlobPaths(blobs);

        // 3.2 add blobs
        var cmdAdd = new CommandAdd(smitBlobManager);
        cmdAdd.addBlobPaths(newBlobPaths);

        // 4. auto-commit changes!
        var cmdCommit = new CommandCommit(smitBlobManager);

        var mergeMessage =
                String.format(
                        "merged <%s> with <%s> successfully",
                        headBranch,
                        givenBranch);

        cmdCommit.makeCommit(mergeMessage, givenBranch);

    }

    private List<Path> collectBlobPaths(Map<String, SmitRecord> blobs) throws SmitException {
        List<Path> newBlobsPaths = new ArrayList<>();

        try {
            for (var entry : blobs.entrySet()) {
                var blobName = entry.getKey();
                var record = entry.getValue();

                if (checkIfBlobValid(blobName, record, blobs)) {
                    // 1. move blobs
                    var pathInRecord = smitBlobManager.absWorkDirPath(Path.of(record.getPath()));
                    smitBlobManager.moveBlobsToWorkDir(blobName, pathInRecord);

                    // 2. add path to result
                    var actualPath = smitBlobManager.relWorkDirPath(blobName);
                    newBlobsPaths.add(actualPath);
                }

            }
        } catch (IOException e) {
            throw new SmitException(e);
        }

        return newBlobsPaths;
    }

    private boolean checkIfBlobValid(String blobName,
                                     SmitRecord record,
                                     Map<String, SmitRecord> blobs) {
        return !record.getStatus().equals(SmitStagedStatus.DELETED);
    }

}
