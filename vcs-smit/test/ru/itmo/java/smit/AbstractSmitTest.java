package ru.itmo.java.smit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.itmo.java.smit.core.manager.SmitCommandManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractSmitTest {
    protected Path playgroundDir;
    protected Path outputPath;
    protected Path standardPath;
    protected SmitCommandManager scm;

    protected final static String DASHES  = "----------------------------";
    protected final static String STARS   = "****************************";
    protected final static String DOLLARS = "$$$$$$$$$$$$$$$$$$$$$$$$$$$$";

    protected void setTestDirsAndSCM() {
        String[] playgroundPathNames = {System.getProperty("user.dir"), "test", "ru", "itmo", "java", "smit", "playground"};
        playgroundDir = Path.of(String.join(File.separator, playgroundPathNames));

        String[] outputPathNames = {System.getProperty("user.dir"), "testResources", "output"};
        outputPath = Path.of(String.join(File.separator, outputPathNames));

        String[] standardPathNames = {System.getProperty("user.dir"), "testResources", "standard"};
        standardPath = Path.of(String.join(File.separator, standardPathNames));

        scm = new SmitCommandManager(playgroundDir);
    }

    @AfterEach
    protected void cleanPlaygroundAfter() {
        var playground = playgroundDir.toFile();
        var status = playground.mkdirs();
        Collection<File> files = FileUtils.listFilesAndDirs(playground, TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE);
        for (File file : files) {
            if (!file.equals(playground)) {
                FileUtils.deleteQuietly(file);
            }
        }
    }

    protected void check(String fileName) {
        assertEquals(
                getFileContent(standardPath.resolve(fileName)),
                getFileContent(outputPath.resolve(fileName))
        );
    }

    protected void checkWithReplace(String fileName) {
        var replaced = replaceDynamicInfoInCommits(outputPath.resolve(fileName));

        assertEquals(
                getFileContent(standardPath.resolve(fileName)),
                replaced
        );
    }

    protected static void createDirAndFiles(Path dirPath, Path outputPath) throws IOException {
        Files.createDirectories(dirPath);
        Files.createDirectories(outputPath);

        createFirstFile(dirPath);
        createSecondFile(dirPath);
//        createThirdFile(dirPath);

    }

    @BeforeEach
    void doInit() throws Exception {
        setTestDirsAndSCM();
        createDirAndFiles(playgroundDir, outputPath);
        var fileName = "init.txt";

        scm.run("init", new PrintStream(outputPath.resolve(fileName).toFile()));

    }

    protected static void createFirstFile(Path dirPath) throws IOException {
        Path filePath = dirPath.resolve("x.txt");
        FileUtils.touch(filePath.toFile());

        FileWriter fileWriter = new FileWriter(filePath.toString());
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("some content here");
        printWriter.printf("another boring string %s and its price is %d $", "Apples", 1721);
        printWriter.close();
    }

    protected static void createSecondFile(Path dirPath) throws IOException {
        Path filePath = dirPath.resolve("y.py");
        FileUtils.touch(filePath.toFile());

        FileWriter fileWriter = new FileWriter(filePath.toString());
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print("""
                def f():
                    return "yes"
                """);
        printWriter.close();
    }

    protected void createThirdFile(Path dirPath) throws IOException {
        Path filePath = dirPath.resolve("z.cpp");
        FileUtils.touch(filePath.toFile());

        FileWriter fileWriter = new FileWriter(filePath.toString());
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print("""
                #include <iostream>

                int main() {
                    std::cout << "hello there tests" std::enfl;
                }
                """);
        printWriter.close();
    }


    protected @Nullable String getFileContent(@NotNull Path filePath) {
        try {
            return FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset());
        } catch (Exception e) {
            return null;
        }
    }


    protected @Nullable String replaceDynamicInfoInCommits(@NotNull Path filePath) {
        var content = getFileContent(filePath);

        if (content == null) {
            return null;
        }

        content = content.replaceAll("[[a-z]|[0-9]]{30}", "COMMIT_HASH");
        content = content.replaceAll("[[[0-9]|[/]|[;]|[:]]*]{19}", "DATE");

        return content;
    }

    protected void createFile(@NotNull String fileName, @NotNull String content, PrintStream out,
                                  Path projectDir) throws Exception {
        out.println(DASHES);
        out.println("created file '" + fileName + "' with content '" + content + "'");
        File file = new File(projectDir.toFile(), fileName);
        FileUtils.writeStringToFile(file, content, Charset.defaultCharset());
    }

    protected void deleteFile(@NotNull String fileName, PrintStream out, Path projectDir) {
        out.println(DASHES);
        out.println("deleted file " + fileName);
        File file = new File(projectDir.toFile(), fileName);
        FileUtils.deleteQuietly(file);
    }

    protected boolean existsFile(@NotNull String fileName) {
        return Files.exists(playgroundDir.resolve(fileName));
    }

    protected void printFileContent(@NotNull String fileName, PrintStream ps) throws IOException{
        var lines = Files.lines(playgroundDir.resolve(fileName));
        lines.forEach(ps::println);

        ps.println(DASHES);
    }

    protected void appendContentToFile(@NotNull String fileName, @NotNull String content) throws Exception {
        Files.writeString(
                playgroundDir.resolve(fileName),
                content,
                StandardOpenOption.APPEND
                );
    }
}
