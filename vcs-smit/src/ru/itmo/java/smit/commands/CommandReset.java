package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.commit.SmitCommit;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Parameters(commandNames = "reset", commandDescription = "resets to versioned repository state")
public class CommandReset extends AbstractCommand {
    @Parameter(required = true, description = "revision hash", arity = 1)
    private List<String> args = new ArrayList<>();

    boolean noArgsGiven = false;

    public CommandReset(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        if (args.isEmpty()) {
            noArgsGiven = true;
            return;
        }

        var revision = args.get(0);
        var revisionHash = smitBlobManager.getRevisionOptions(revision);

        resetByHash(revisionHash);
    }

    @Override
    public void showOutput() throws SmitException {
        if (noArgsGiven) {
            return;
        }
        printStream.println("reset done");
    }

    @Override
    public void afterRun() throws SmitException {
        args.clear();
    }

    public void resetByHash(String hash) throws SmitException {
        var currCommit = smitBlobManager.readCommitByHash(hash);
        var headCommit = smitBlobManager.getHeadCommit();
        var blobs = currCommit.getBlobs();

        // 1. restore all blobs
        restoreBlobs(blobs, headCommit);
        // 2. clean out index
        clearIndex();
        // 3. actualize branch state
        actualizeBranchState(hash);
    }

    private void restoreBlobs(Map<String, SmitRecord> blobs, SmitCommit headCommit) throws SmitException {
        try {
            // delete blobs
            smitBlobManager
                    .walkInWorkDir()
                    .filter(this::validFilePath)
                    .map(path -> {
                        try {
                            return smitBlobManager.relWorkDirPath(path).toString();
                        } catch (SmitException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(blobName -> blobs.containsKey(blobName) ||
                            (headCommitBlobsContains(headCommit, blobName)))
                    .filter(blobName -> headCommitBlobsContains(headCommit, blobName))
                    .forEach(blobName -> deleteBlobIfNotContains(blobs, blobName));


            blobs
                 .forEach(
                         (name, record) ->
                         {
                            try {
                                var givenPath = smitBlobManager.absWorkDirPath(blobs.get(name).getPath());
                                smitBlobManager.moveBlobsToWorkDir(name, givenPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                         }
                 );

        } catch (IOException | RuntimeException e) {
            throw new SmitException(e);
        }

        
    }

    private boolean validFilePath(@NotNull Path path) {
        return !path.startsWith(smitBlobManager.getSmitDir()) &&
               !path.equals(smitBlobManager.getWorkDir());
    }

    private boolean headCommitBlobsContains(SmitCommit headCommit, String blobName) {
        if (headCommit == null) {
            return false;
        }
        return headCommit.getBlobs().containsKey(blobName);
    }

    private void deleteBlobIfNotContains(@NotNull Map<String, SmitRecord> blobs, String blobName) {
        try {
            if (!blobs.containsKey(blobName)) {
                var actualPath = smitBlobManager.getWorkDir().resolve(blobName);
                smitBlobManager.deleteBlob(actualPath);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void clearIndex() throws SmitException {
        var emptyMap = new HashMap<String, SmitRecord>();
        smitBlobManager.setIndex(emptyMap);
    }

    private void actualizeBranchState(String hash) throws SmitException {
        var currBranchHead = smitBlobManager.getBranchHead();
        if (currBranchHead == null) {
            smitBlobManager.setHead(hash);
            return;
        }

        var branches = smitBlobManager.getBranches();
        branches.put(currBranchHead, hash);
        smitBlobManager.setBranches(branches);
    }
}
