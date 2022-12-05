package ru.itmo.java.smit.core.manager;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.core.blobs.SmitRecord;
import ru.itmo.java.smit.core.commit.SmitCommit;
import ru.itmo.java.smit.core.revision.SmitRevisionOption;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.serialization.BlobHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* This class's purpose is providing API for this implementation of VCS (Version control system).
*
 */
public class SmitBlobManager {
    private final String smitPrefix = ".smit";

    private final Path smitDir;
    private final Path workDir;
    private final Path objectsDir;
    private final Path blobsDir;
    private final Path commitsDir;

    private final Path pathToHead;
    private final Path pathToIndex;
    private final Path pathToBranches;

    public SmitBlobManager(Path givenDir) {
        workDir = givenDir.toAbsolutePath().normalize();

        smitDir = workDir.resolve(smitPrefix);
        objectsDir = smitDir.resolve("objects");


        blobsDir = objectsDir.resolve("blobs");
        commitsDir = objectsDir.resolve("commits");

        pathToHead = smitDir.resolve("HEAD");
        pathToIndex = smitDir.resolve("index");
        pathToBranches = objectsDir.resolve("branches");
    }

    /**
     * Getter for vcs directory path.
     * @return vcs directory path.
     */
    public Path getSmitDir() {
        return smitDir;
    }

    /**
     * Getter for working directory path.
     * @return working directory path.
     */
    public Path getWorkDir() {
        return workDir;
    }

    /**
     * Getter for `blobs` directory path in vcs directory. Versioned files are stored there.
     * @return `blobs` directory path in vcs directory.
     */
    public Path getBlobsDir() {
        return blobsDir;
    }

    /**
     * Getter for `commits` file path in vcs directory. Commits are stored there.
     * @return `commits` directory path in vcs directory.
     */
    public Path getCommitsDir() {
        return commitsDir;
    }

    /**
     * Getter for `HEAD` file path in vcs directory.
     * @return `HEAD` file path in vcs directory.
     */
    public Path getPathToHead() {
        return pathToHead;
    }

    /**
     * Getter for content of `HEAD` file.
     * @return `HEAD` content.
     * @throws SmitException if reading by path is failed.
     */
    public String getHead() throws SmitException {
        return BlobHandler.read(pathToHead);
    }

    /**
     * Setter for content of `HEAD` file.
     * @param givenHead new content for `HEAD` file.
     * @throws SmitException if writing by path is failed.
     */
    public void setHead(String givenHead) throws SmitException {
        BlobHandler.write(givenHead, pathToHead);
    }


    /**
     * @return Map of file paths as keys, file information as values.
     * @throws SmitException if reading by path is failed.
     */
    public Map<String, SmitRecord> getIndex() throws SmitException {
        return BlobHandler.read(pathToIndex);
    }

    /**
     * @param givenIndexContent new Map of file paths as keys, file information as values to replace.
     * @throws SmitException if writing by path is failed.
     */
    public void setIndex(Map<String, SmitRecord> givenIndexContent) throws SmitException {
        BlobHandler.write(givenIndexContent, pathToIndex);
    }

    /**
     * Getter for content of `branches` file.
     * @return `branches` content as map of branch names as keys, most recent commits as values.
     * @throws SmitException if reading by path is failed.
     */
    public Map<String, String> getBranches() throws SmitException {
        return BlobHandler.read(pathToBranches);
    }

    /**
     * @param givenBranches new content for `branches` file.
     * @throws SmitException if writing by path is failed.
     */
    public void setBranches(Map<String, String> givenBranches) throws SmitException {
        BlobHandler.write(givenBranches, pathToBranches);
    }

    /**
     * @return content of `HEAD` if it is stored as a key in branches Map, null otherwise.
     * @throws SmitException if reading `branches` by path is failed.
     */
    public String getBranchHead() throws SmitException {
        var branches = getBranches();
        if (branches.containsKey(getHead())) {
            return getHead();
        }
        return null;
    }

    /**
     * Getter for commit with given hash.
     * @param hash id of commit.
     * @return `SmitCommit` object with corresponding properties.
     * @throws SmitException if reading by path is failed.
     */
    public SmitCommit readCommitByHash(String hash) throws SmitException {
        if (hash == null) {
            return null;
        }
        if (!Files.exists(commitsDir.resolve(hash))) {
            return null;
        }
        return BlobHandler.read(commitsDir.resolve(hash));
    }


    /**
     * Setter for commit with given hash, stores commit as files with the filename as given `hash`.
     * @param hash filename for commit.
     * @param commit `SmitCommit` object with corresponding properties.
     * @throws SmitException if writing by path is failed.
     */
    public void writeCommitByHash(String hash, SmitCommit commit) throws SmitException {
        BlobHandler.write(commit, commitsDir.resolve(hash));
    }


    /**
     * @return most recent commit.
     * @throws SmitException if reading `commits` by path is failed.
     */
    public SmitCommit getHeadCommit() throws SmitException {
        var currHead = getHead();
        var currBranches = getBranches();

        var currHash = currBranches.getOrDefault(currHead, currHead);

        return readCommitByHash(currHash);
    }

    /**
     * @param option revision hash, branch name or HEAD~N.
     * @return enum value of corresponding revision option.
     * @throws SmitException if invalid option were given.
     */
    public SmitRevisionOption defineOption(@NotNull String option) throws SmitException {
        var headPrefix = "HEAD~";

        if (option.startsWith(headPrefix)) {
            var sNum = option.substring(headPrefix.length());
            var number = Integer.parseInt(sNum);

            if (number < 0) {
                throw new SmitException("invalid N parameter for HEAD~ option");
            }

            var headOption = SmitRevisionOption.HEAD_N;
            var info = getNthRevision(number);
            headOption.setOptionInfo(info);

            return headOption;
        } else {
            var branches = getBranches();

            if (Files.exists(commitsDir.resolve(option))) {
                var commitOption = SmitRevisionOption.COMMIT_HASH;
                commitOption.setOptionInfo(option);
                return commitOption;
            }

            if (branches.containsKey(option)) {
                var branchOption = SmitRevisionOption.BRANCH;
                branchOption.setOptionInfo(option);
                return branchOption;
            }

            throw new SmitException("Invalid option given");
        }
    }

    /**
     * @param number nuber of commit starting with current one.
     * @return corresponding commit hash.
     * @throws SmitException if invalid number were given.
     */
    public String getNthRevision(int number) throws SmitException {
        var currCommit = getHeadCommit();

        while (number > 0) {
            if (currCommit == null) {
                throw new SmitException("invalid N");
            }

            currCommit = readCommitByHash(currCommit.getPreviousCommit());
            number--;
        }

        if (currCommit == null) {
            throw new SmitException("invalid N");
        }

        return currCommit.getHash();
    }

    /**
     * @param givenOption revision hash, branch name or HEAD~N.
     * @return corresponding hash for option.
     * @throws SmitException if invalid option were given.
     */
    public String getRevisionOptions(String givenOption) throws SmitException {
        var revisionOption = defineOption(givenOption);
        return revisionOption.getOptionInfo();
    }

    /**
     * @param revisionOption enum value of revision hash, branch name or HEAD~N.
     * @return corresponding hash for option.
     * @throws SmitException if invalid option were given.
     */
    public String getRevisionOptions(@NotNull SmitRevisionOption revisionOption) throws SmitException {
        switch (revisionOption) {
            case HEAD_N ->
                    {
                        var headPrefix = "HEAD~";
                        var sNum = revisionOption.getOptionInfo().substring(headPrefix.length());
                        var number = Integer.parseInt(sNum);

                        if (number < 0) {
                            throw new SmitException("invalid N parameter for HEAD~ option");
                        }

                        var info = getNthRevision(number);
                        revisionOption.setOptionInfo(info);
                        return revisionOption.getOptionInfo();
                    }
            case COMMIT_HASH, BRANCH -> { return revisionOption.getOptionInfo(); }
        }

        return "";
    }

    /**
     * @param dir path for creating a directory.
     * @throws IOException if creation failed.
     */
    public void createDirs(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    /**
     * @param path path for deleting a file.
     * @throws IOException if deletion failed.
     */
    public void deleteBlob(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    /**
     * @return stream of paths in working directory.
     * @throws IOException if collecting paths failed.
     */
    public Stream<Path> walkInWorkDir() throws IOException {
        return Files.walk(workDir);
    }

    /**
     * @param path given path.
     * @return normilized path if absolute path provided, normilized path from working directory otherwise.
     */
    public Path absWorkDirPath(Path path) {
        return path.isAbsolute() ?
                path.normalize() :
                workDir.resolve(path).normalize();
    }

    /**
     * @param strPath path given as a string in whatever form.
     * @return normilized path if absolute path provided, normilized path from working directory otherwise.
     */
    public Path absWorkDirPath(String strPath) {
        return absWorkDirPath(Path.of(strPath));
    }

    /**
     * @param actualPath given path.
     * @return normilized relative to working directory path.
     * @throws SmitException if working directory is not a subpath of a given path.
     */
    public Path relWorkDirPath(Path actualPath) throws SmitException {
        var strPath = actualPath.toString();
        if (actualPath.isAbsolute()) {
            actualPath = actualPath.normalize();

            if (!actualPath.startsWith(workDir)) {
                throw new SmitException(
                        String.format("impossible to get relative path from working directory for %s", strPath)
                );
            }
            return actualPath.subpath(workDir.getNameCount(), actualPath.getNameCount());
        }

        if (!Files.exists(workDir.resolve(strPath).normalize())) {
            throw new SmitException(
                    String.format("given path %s is not relative to working directory", strPath)
            );
        }

        return Path.of(strPath).normalize();
    }

    /**
     * @param strPath given path as a string.
     * @return normilized relative to working directory path.
     * @throws SmitException if working directory is not a subpath of a given path.
     */
    public Path relWorkDirPath(String strPath) throws SmitException {
        var actualPath = Path.of(strPath);
        return relWorkDirPath(actualPath);
    }

    /**
     * @param blobPath file path.
     * @return filename that stores in given path
     */
    public String nameOfBlob(@NotNull Path blobPath) {
        var strPath = blobPath.toAbsolutePath().normalize().toString();
        var workDirStrPath = workDir.toString();

        if (strPath.startsWith(workDirStrPath)) {
            return strPath.substring(workDirStrPath.length() + 1);
        }
        return null;
    }

    /**
     * @param blobStringPaths list of paths as strings in whatever form.
     * @return list of normalized absolute paths for existing files.
     */
    public List<Path> getAbsWorkDirPaths(@NotNull List<String> blobStringPaths) {
        return blobStringPaths
                    .stream()
                    .map(this::absWorkDirPath)
                    .filter(x -> {
                        if (Files.exists(x)) {
                            return true;
                        } else {
                            throw new RuntimeException(x + " : no such file");
                        }
                    })
                    .collect(Collectors.toList());
    }

    /**
     * @param blobStringPaths list of paths as strings in whatever form.
     * @return list of normalized relative to working directory paths.
     */
    public List<Path> getRelWorkDirPaths(@NotNull List<String> blobStringPaths) {
        return blobStringPaths
                .stream()
                .map(path -> {
                    try {
                        return relWorkDirPath(path);
                    } catch (SmitException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * @param blobPath file path for copying from.
     * @param newPath file path for copying into.
     * @throws IOException if copying failed.
     */
    public void copyBlobByPath(Path blobPath, @NotNull Path newPath) throws IOException {
        FileUtils.touch(newPath.toFile());
        Files.copy(blobPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @param blobName file name.
     * @param givenPath path for copying from.
     * @throws IOException if copying failed.
     */
    public void moveBlobsToWorkDir(String blobName, Path givenPath) throws IOException {
        var actualPath = workDir.resolve(blobName);
        copyBlobByPath(givenPath, actualPath);
    }
}
