package ru.itmo.java.smit.core.commit;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.blobs.SmitStagedStatus;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.utils.Hasher;
import ru.itmo.java.smit.utils.SmitColor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class SmitCommit implements Serializable {
    private final String author;
    private final String date;

    private final String message;

    private final String hash;

    private String previousCommit;
    private String previousMergeBranch;

    private boolean COLORIZED = false;

    // could be replaced with System.getProperty("user.name");
    private static final String defualtUserName = "USER";


    private final HashMap<String, SmitRecord> blobs = new HashMap<>();

    public SmitCommit(String givenMessage) {
        this(givenMessage, defualtUserName, false);
    }

    public SmitCommit(String givenMessage, boolean COLORIZED) {
        this(givenMessage, defualtUserName, COLORIZED);
    }

    public SmitCommit(String givenMessage, String givenAuthor, boolean COLORIZED) {
        message = givenMessage;
        date = getCurrentTime();
        author = givenAuthor;


        previousCommit = null;

        hash = Hasher.constructRandomString();
        this.COLORIZED = COLORIZED;

    }

    public Map<String, SmitRecord> getBlobs() {
        return blobs;
    }

    public String getPreviousCommit() {
        return previousCommit;
    }

    public String getHash() {
        return hash;
    }

    private @NotNull String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd;HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void pickBranch(@NotNull SmitBlobManager smitBlobManager, String branchToMerge) throws SmitException, IOException {
        var branches = smitBlobManager.getBranches();
        var pathToHead = smitBlobManager.getPathToHead();

        String currBranch = null;

        if (Files.exists(pathToHead)) {
            var head = smitBlobManager.getHead();
            previousCommit = branches.getOrDefault(head, head);

            if (branches.containsKey(head)) {
                currBranch = head;
            }

            var prevSmitCommit = smitBlobManager.readCommitByHash(previousCommit);

            var prevBlobs = prevSmitCommit.getBlobs();
            blobs.putAll(prevBlobs);
            previousMergeBranch = branches.get(branchToMerge);

        }
        else {
            FileUtils.touch(pathToHead.toFile());
            currBranch = "master";
        }

        if (currBranch == null) {
            smitBlobManager.setHead(hash);
        } else {
            smitBlobManager.setHead(currBranch);

            branches.put(currBranch, hash);
            smitBlobManager.setBranches(branches);
        }
    }

    private void fixIndexState(@NotNull SmitBlobManager smitBlobManager) throws SmitException {
        var index = smitBlobManager.getIndex();

        // manage blobs in index based on staged status
        for (var entry : index.entrySet()) {
            var path = entry.getKey();
            var record = entry.getValue();
            if (record.getStatus().equals(SmitStagedStatus.DELETED)) {
                blobs.remove(path);
            } else {
                blobs.put(path, record);
            }
        }

        smitBlobManager.setIndex(new HashMap<>());
    }

    private void fixCommitsState(@NotNull SmitBlobManager smitBlobManager) throws SmitException {
        smitBlobManager.writeCommitByHash(hash, this);
    }

    public boolean construct(@NotNull SmitBlobManager smitBlobManager,
                             String branchToMerge,
                             boolean initCommit) throws IOException, SmitException {
        var index = smitBlobManager.getIndex();
        // 1. init commit or files in index
        if (initCommit || !index.isEmpty()) {
            pickBranch(smitBlobManager, branchToMerge);
            fixIndexState(smitBlobManager);
            fixCommitsState(smitBlobManager);
            return true;
        }

        // 2. nothing to commit
        return false;
    }


    public boolean construct(SmitBlobManager smitBlobManager, String branchToMerge) throws IOException, SmitException {
        return construct(smitBlobManager, branchToMerge, false);
    }

    public String prettyPrint() {
        var sj = new StringJoiner(System.lineSeparator());


        var out = COLORIZED ?
                sj
                    .add(SmitColor.makeYellow("commit " + hash))
                    .add("author: " + author)
                    .add("date: " + date)
                    .add("message: " + message)
                :
                sj
                    .add("commit " + hash)
                    .add("author: " + author)
                    .add("date: " + date)
                    .add("message: " + message);

        return out.toString();
    }
}
