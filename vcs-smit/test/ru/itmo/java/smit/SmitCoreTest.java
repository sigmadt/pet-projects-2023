package ru.itmo.java.smit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.PrintStream;

public class SmitCoreTest extends AbstractSmitTest {

    @Test
    void testAdd() throws Exception {
        var fileName = "add.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt y.py", out);
        scm.run("status", out);

        check(fileName);
    }

    @Test
    void testAddAndCommit() throws Exception {
        var fileName = "addAndCommit.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt y.py", out);
        scm.run("status", out);

        scm.run("commit -m \"1\"", out);

        scm.run("status", out);

        check(fileName);
    }

    @Test
    void testAddAndRemove() throws Exception {
        var fileName = "addAndRemove.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt y.py", out);
        scm.run("status", out);

        scm.run("rm y.py", out);

        scm.run("status", out);

        check(fileName);
    }

    @Test
    void testCommitAndRemove() throws Exception {
        var fileName = "commitAndRemove.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt y.py", out);
        scm.run("status", out);

        scm.run("commit -m \"1\"", out);

        scm.run("rm x.txt", out);
        scm.run("status", out);

        check(fileName);
    }

    @Test
    void testDoubleCommitAndLog() throws Exception {
        var fileName = "commitAndLog.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("log", out);

        checkWithReplace(fileName);
    }


    @Test
    void testCommitAndCheckoutWithHEAD() throws Exception {
        var fileName = "commitAndCheckout.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("log", out);

        scm.run("checkout HEAD~1", out);
        scm.run("status", out);


        checkWithReplace(fileName);
    }

    @Test
    void testCommitAndCheckoutWithBranch() throws Exception {
        var fileName = "commitAndCheckoutBranch.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("log", out);

        scm.run("checkout HEAD~1", out);
        scm.run("status", out);
        scm.run("log", out);

        scm.run("checkout master", out);
        scm.run("status", out);
        scm.run("log", out);


        checkWithReplace(fileName);
    }

    @Test
    void testCheckoutRemovesUnknownFiles() throws Exception {
        var fileName = "checkoutRemovesUnknownFiles.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("checkout HEAD~1", out);

        scm.run("status", out);

        scm.run("log", out);

        checkWithReplace(fileName);

        assertTrue(existsFile("x.txt"));
        assertFalse(existsFile("y.py"));
    }

    @Test
    void testCommitAndReset() throws Exception {
        var fileName = "commitAndReset.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("log", out);

        scm.run("reset HEAD~1", out);
        scm.run("log", out);

        createFile("z.cpp", "#include <iostream>", out, playgroundDir);
        scm.run("add z.cpp", out);
        scm.run("commit -m \"3\"", out);

        scm.run("log", out);

        checkWithReplace(fileName);
    }

    @Test
    void testCheckoutDropChanges() throws Exception {
        var fileName = "checkoutDropChanges.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        printFileContent("x.txt", out);

        scm.run("add y.py", out);
        printFileContent("y.py", out);

        scm.run("commit -m \"1\"", out);

        appendContentToFile("x.txt", "random string");
        printFileContent("x.txt", out);

        appendContentToFile("y.py", "\t\tsome random content\n");
        printFileContent("y.py", out);

        scm.run("checkout -- x.txt y.py");

        printFileContent("x.txt", out);
        printFileContent("y.py", out);

        check(fileName);
    }

    @Test
    void testResetRemovesFiles() throws Exception {
        var fileName = "resetRemovesFiles.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("reset HEAD~1", out);

        scm.run("log", out);

        checkWithReplace(fileName);

        assertTrue(existsFile("x.txt"));
        assertFalse(existsFile("y.py"));
    }

    @Test
    void testcreateBranchAndStatus() throws Exception {
        var fileName = "createBranchAndStatus.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add y.py", out);
        scm.run("commit -m \"1\"", out);

        scm.run("status", out);

        scm.run("branch-create new", out);
        scm.run("add x.txt", out);
        scm.run("status", out);

        checkWithReplace(fileName);
    }

    @Test
    void testCreateBranchAndCheckout() throws Exception {
        var fileName = "createBranchAndCheckout.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add y.py", out);
        scm.run("commit -m \"1\"", out);

        scm.run("log", out);

        scm.run("branch-create new", out);
        scm.run("add x.txt", out);
        scm.run("commit -m \"new2\"", out);
        scm.run("log", out);

        scm.run("checkout master", out);
        scm.run("log", out);

        checkWithReplace(fileName);
    }

    @Test
    void testShowBranches() throws Exception {
        var fileName = "showBranches.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("branch-create branchX", out);
        scm.run("branch-create branchY", out);
        scm.run("branch-create branchZ", out);

        scm.run("show-branches", out);

        check(fileName);
    }

    @Test
    void testCreateAndRemoveBranches() throws Exception {
        var fileName = "createAndRemoveBranches.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("branch-create branchX", out);
        scm.run("branch-create branchY", out);

        scm.run("branch-remove branchX", out);
        scm.run("show-branches", out);

        scm.run("branch-remove noSuchBranch", out);

        check(fileName);
    }


    @Test
    void testMergeWithNewBranch() throws Exception {
        var fileName = "mergeWithNewBranch.txt";
        var out = new PrintStream(outputPath.resolve(fileName).toFile());

        scm.run("add x.txt", out);
        scm.run("commit -m \"1\"", out);

        scm.run("branch-create branchX", out);

        scm.run("add y.py", out);
        scm.run("commit -m \"2\"", out);

        scm.run("log", out);

        scm.run("checkout master", out);
        scm.run("log", out);

        scm.run("merge branchX", out);
        scm.run("log", out);
        scm.run("status", out);


        checkWithReplace(fileName);
    }
}
