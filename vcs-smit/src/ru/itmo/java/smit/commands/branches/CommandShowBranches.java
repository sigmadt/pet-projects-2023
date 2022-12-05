package ru.itmo.java.smit.commands.branches;

import com.beust.jcommander.Parameters;
import ru.itmo.java.smit.commands.AbstractCommand;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.utils.SmitColor;

@Parameters(commandNames = "show-branches", commandDescription = "shows available branches")
public class CommandShowBranches extends AbstractCommand {

    public CommandShowBranches(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void showOutput() throws SmitException {
        printStream.println("available branches:");

        var headBranch = smitBlobManager.getBranchHead();
        var displayHeadBranch = COLORIZED ?
                SmitColor.makeGreen("\tHEAD -> " + headBranch) :
                "\tHEAD -> " + headBranch;

        printStream.println(displayHeadBranch);

        smitBlobManager
                .getBranches()
                .forEach(
                        (branch, branchHash) ->
                        {
                            if (!branch.equals(headBranch)) {

                                var displayBranch = COLORIZED ?
                                        SmitColor.makeBlue("\t" + branch) :
                                        "\t" + branch;

                                        printStream.println(displayBranch);
                            }
                        }
                );
    }
}
