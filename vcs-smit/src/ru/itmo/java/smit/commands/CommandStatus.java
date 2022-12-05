package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameters;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.blobs.SmitStatusState;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.serialization.BlobHandler;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.utils.SmitColor;
import ru.itmo.java.smit.core.commit.SmitCommit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Parameters(commandNames = "status", commandDescription = "displays current smit status")
public class CommandStatus extends AbstractCommand {
    private SmitStatusState statusState;
    private String currBranch;

    public CommandStatus(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        statusState = new SmitStatusState();
        statusState.init();

        var index = smitBlobManager.getIndex();
        var headCommit = smitBlobManager.getHeadCommit();
        currBranch = smitBlobManager.getBranchHead();

        try {
            // 1. files in index
            index
                    .forEach((blobName, currRecord) -> {
                        if (!Files.exists(smitBlobManager.absWorkDirPath(blobName))) {
                            statusState.addUntrackedDeleted(blobName);
                            return;
                        }

                        var blobStagedStatus = currRecord.getStatus();

                        // 1.1 added
                        // 1.2 deleted
                        // 1.3 modified
                        switch (blobStagedStatus) {
                            case ADDED -> statusState.addStagedAdded(blobName);
                            case DELETED -> statusState.addStagedDeleted(blobName);
                            case MODIFIED -> statusState.addStagedModified(blobName);
                        }
                    });


            smitBlobManager
                    .walkInWorkDir()
                    .filter(this::checkIfValid)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return smitBlobManager.relWorkDirPath(path).toString();
                        } catch (SmitException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    )
                    .filter(blobName -> checkIfNotInBlobs(blobName, headCommit))
                    .forEach(
                            blobName ->
                            {
                                // 2. untracked new
                                if (checkIfUntrackedNew(blobName, headCommit, index)) {
                                    statusState.addUntrackedNew(blobName);
                                }
                            }
                    );

            // 3. untracked files
            if (headCommit == null) {
                return;
            }
            var blobs = headCommit.getBlobs();

            for (var entry : blobs.entrySet()) {
                var blobName = entry.getKey();
                var absPath = smitBlobManager.absWorkDirPath(blobName);
                // 3.1 untracked deleted
                if (!Files.exists(absPath)) {
                    statusState.addUntrackedDeleted(blobName);
                    continue;
                }

                var record = entry.getValue();
                var blobHashInIndex = record.getHash();
                var blobHashByPath = BlobHandler.computeHash(absPath);

                // 3.2 untracked modified
                if (!blobHashInIndex.equals(blobHashByPath)) {
                    statusState.addUntrackedModified(blobName);
                }

            }

        } catch (IOException e) {
            throw new SmitException(e);
        }
    }

    @Override
    public void showOutput() throws SmitException {
        showCurrBranch();
        showCommittableFiles();
    }

    private boolean checkIfValid(@NotNull Path givenPath) {
        if (givenPath.equals(smitBlobManager.getWorkDir())) {
            return false;
        }
        if (givenPath.startsWith(smitBlobManager.getSmitDir())) {
            return false;
        }
        return true;
    }

    private boolean checkIfNotInBlobs(
            String blobName,
            SmitCommit headCommit) {
        if (headCommit == null) {
            return true;
        }
        var blobs = headCommit.getBlobs();
        return !blobs.containsKey(blobName);
    }

    private boolean checkIfUntrackedNew(String blobName,
                                        SmitCommit headCommit,
                                        Map<String, SmitRecord> index) {

        if (checkIfNotInBlobs(blobName, headCommit)) {
            return !index.containsKey(blobName);
        }
        return false;
    }

    private boolean checkIfEmptyUntracked() {
        return statusState.getUntrackedNew().getBlobs().isEmpty() &&
                statusState.getUntrackedModified().getBlobs().isEmpty() &&
                statusState.getUntrackedDeleted().getBlobs().isEmpty();
    }

    private boolean checkIfEmptyStaged() {
        return statusState.getStagedAdded().getBlobs().isEmpty() &&
               statusState.getStagedDeleted().getBlobs().isEmpty() &&
               statusState.getStagedModified().getBlobs().isEmpty();
    }

    private void showCurrBranch() throws SmitException {
        var headDetachedMessage = COLORIZED ?
                SmitColor.makeRed("HEAD detached at ") :
                "HEAD detached at ";

        var message = currBranch == null ?
                headDetachedMessage + smitBlobManager.getHead() :
                String.format("On branch <%s>", currBranch);

        printStream.println(message);
    }

    private void showCommittableFiles() {
        if (checkIfEmptyUntracked() && checkIfEmptyStaged()) {
            printStream.println("Everything is up to date!");
            return;
        }

        if (!checkIfEmptyStaged()) {
            printStream.println("Changes to be committed:");
            showStatusAndFiles(
                    "new:     ",
                    SmitColor.GREEN,
                    statusState.getStagedAdded().getBlobs());
            showStatusAndFiles(
                    "deleted: ",
                    SmitColor.RED,
                    statusState.getStagedDeleted().getBlobs());
            showStatusAndFiles(
                    "modified:",
                    SmitColor.BLUE,
                    statusState.getStagedModified().getBlobs());
        }

        if (!checkIfEmptyUntracked()) {
            printStream.println("Untracked files:");
            showStatusAndFiles(
                    "new:     ",
                    SmitColor.PURPLE,
                    statusState.getUntrackedNew().getBlobs());
            showStatusAndFiles(
                    "deleted: ",
                    SmitColor.PURPLE,
                    statusState.getUntrackedDeleted().getBlobs());
            showStatusAndFiles(
                    "modified:",
                    SmitColor.PURPLE,
                    statusState.getUntrackedModified().getBlobs());

        }
    }

    private void showStatusAndFiles(String status, SmitColor color, @NotNull Set<String> fileNames) {
        if (fileNames.isEmpty()) {
            return;
        }

        for (var name : fileNames) {
            var ln =
                    COLORIZED ?
                    String.format("\t%s%s\t%s%s",
                            color.getCode(),
                            status, name,
                            SmitColor.RESET.getCode())
                    :
                    String.format("\t%s\t%s", status, name);

            printStream.println(ln);
        }

    }
}
