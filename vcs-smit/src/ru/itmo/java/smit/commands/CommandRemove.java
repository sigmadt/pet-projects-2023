package ru.itmo.java.smit.commands;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.blobs.SmitStagedStatus;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Parameters(commandNames = "rm", commandDescription = "removes files from index")
public class CommandRemove extends AbstractCommand {

    @Parameter(description = "given path to files")
    private List<String> paths = new ArrayList<>();

    private List<Path> blobPaths;

    public CommandRemove(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        try {
            blobPaths = smitBlobManager.getRelWorkDirPaths(paths);

            var index = smitBlobManager.getIndex();

            for (var blobPath : blobPaths) {
                actualizeIndex(blobPath, index);
            }
        } catch (RuntimeException e) {
            throw new SmitException(e.getMessage());
        }

    }

    @Override
    public void showOutput() throws SmitException {
        printStream.println("files were removed");
    }

    @Override
    public void afterRun() throws SmitException {
        paths.clear();
    }

    private void actualizeIndex(Path blobPath, Map<String, SmitRecord> index) throws SmitException {
        var headCommit = smitBlobManager.getHeadCommit();

        var blobName = blobPath.toString();

        if (headCommit != null) {
            var blobsInHeadCommit = headCommit.getBlobs();
            if (blobsInHeadCommit.containsKey(blobName)) {
                var status = SmitStagedStatus.DELETED;
                var removedBlobRecord = new SmitRecord(null, status, null);
                index.put(blobName, removedBlobRecord);
            } else {
                index.remove(blobName);
            }
            smitBlobManager.setIndex(index);
        }

    }
}



