package ru.itmo.java.smit.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.blobs.SmitStagedStatus;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.serialization.BlobHandler;
import ru.itmo.java.smit.core.manager.SmitBlobManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Parameters(commandNames = {"add"}, commandDescription = "add files to index")
public class CommandAdd extends AbstractCommand {
    @Parameter(description = "given path to files")
    private List<String> paths = new ArrayList<>();

    public CommandAdd(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        try {
            List<Path> blobPaths = smitBlobManager.getRelWorkDirPaths(paths);
            addBlobPaths(blobPaths);
        } catch (RuntimeException e) {
            throw new SmitException(e.getMessage());
        }
    }

    @Override
    public void showOutput() throws SmitException {
        printStream.println("given files were added to index");
    }

    @Override
    public void afterRun() throws SmitException {
        paths.clear();
    }

    public void addBlobPaths(@NotNull List<Path> givenPaths) throws SmitException {
        var index = smitBlobManager.getIndex();

        for (var blobPath : givenPaths) {
            actualizeIndex(blobPath, index);
        }

        smitBlobManager.setIndex(index);
    }

    private void actualizeIndex(Path blobPath, @NotNull Map<String, SmitRecord> index) throws SmitException {
        var headCommit = smitBlobManager.getHeadCommit();

        var blobName = blobPath.toString();
        var absBlobPath = smitBlobManager.absWorkDirPath(blobPath);
        String commitBlobHash = null;
        String indexBlobHash = null;
        String currentBlobHash = BlobHandler.computeHash(absBlobPath);

        if (index.containsKey(blobName)) {
            indexBlobHash = index.get(blobName).getHash();
        }

        // 1. add is ambiguous because hashes are equal
        if (currentBlobHash.equals(indexBlobHash)) {
            return;
        }

        // 2. get blob hash stored in smit repo
        if (headCommit != null) {
            var blobsInHeadCommit = headCommit.getBlobs();
            if (blobsInHeadCommit.containsKey(blobName)) {
                commitBlobHash = blobsInHeadCommit.get(blobName).getHash();
            }
        }

        // 3. add is ambiguous because hashes are equal
        if (Objects.equals(currentBlobHash, commitBlobHash)) {
            return;
        }

        var newPath = smitBlobManager
                .getBlobsDir()
                .resolve(blobName)
                .resolve(currentBlobHash);

        try {
            smitBlobManager.copyBlobByPath(absBlobPath, newPath);
        } catch (IOException e) {
            throw new SmitException(String.format("add for file %s failed", blobPath));
        }

        var blobStatus =
                commitBlobHash == null ?
                SmitStagedStatus.ADDED :
                SmitStagedStatus.MODIFIED;

        var relNewPath = smitBlobManager.relWorkDirPath(newPath);
        var blobRecord = new SmitRecord(currentBlobHash, blobStatus, relNewPath.toString());

        index.put(blobName, blobRecord);
    }
}
