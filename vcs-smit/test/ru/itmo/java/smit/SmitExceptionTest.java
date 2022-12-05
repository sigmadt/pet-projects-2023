package ru.itmo.java.smit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.java.smit.exception.SmitException;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class SmitExceptionTest extends AbstractSmitTest {

    @AfterEach
    @BeforeEach
    void displayBeforeTest() {
        System.out.println(STARS);
    }


    @Test
    void testAddThrowsInvalidPath() {
        assertThrows(SmitException.class, () -> scm.throwableRun("add no_such_file.txt"));
    }

    @Test
    void testRemoveThrowsInvalidPath() {
        scm.run("add x.txt y.py");
        scm.run("commit -m \"1\"");

        scm.run("rm y.py");
        scm.run("status");

        assertThrows(SmitException.class, () -> scm.throwableRun("rm no_such_file.txt"));
    }

    @Test
    void testCheckoutThrowsInvalidRevisionOption() {
        scm.run("add x.txt");
        scm.run("commit -m \"1\"");

        scm.run("checkout master");
        scm.run("status");

        assertThrows(SmitException.class, () -> scm.throwableRun("checkout norevision"));
    }

    @Test
    void testLogThrowsInvalidRevisionOption() {
        scm.run("add y.py");
        scm.run("commit -m \"1\"");

        scm.run("log HEAD~1");

        assertThrows(SmitException.class, () -> scm.throwableRun("log norevision"));
    }

    @Test
    void testLogThrowsInvalidNInRevisionOption() {
        scm.run("add y.py");
        scm.run("commit -m \"1\"");

        scm.run("log HEAD~1");

        assertThrows(SmitException.class, () -> scm.throwableRun("log HEAD~5"));
    }

    @Test
    void testResetThrowsInvalidRevisionOption() {
        scm.run("add y.py");
        scm.run("commit -m \"1\"");

        scm.run("add x.txt");
        scm.run("commit -m \"2\"");

        scm.run("log");

        scm.run("reset HEAD~2");
        scm.run("status");
        assertThrows(SmitException.class, () -> scm.throwableRun("reset norevision"));
    }

    @Test
    void testCreateBranchThrowsAlreadyExisted() {
        scm.run("add y.py");
        scm.run("commit -m \"1\"");

        scm.run("branch-create branchX");

        assertThrows(SmitException.class, () -> scm.throwableRun("branch-create branchX"));
    }

    @Test
    void testRemoveBranchThrowsNoSuchBranch() {
        scm.run("add x.txt");
        scm.run("commit -m \"1\"");

        scm.run("branch-create branchX");
        scm.run("branch-remove branchX");

        assertThrows(SmitException.class, () -> scm.throwableRun("branch-remove noBranch"));
    }

    @Test
    void testMergeWithBranchThrowsDetachedHeadState() {
        scm.run("add x.txt");
        scm.run("commit -m \"1\"");

        scm.run("branch-create branchX");

        scm.run("checkout HEAD~1");
        scm.run("status");

        assertThrows(SmitException.class, () -> scm.throwableRun("merge master"));
    }

}
