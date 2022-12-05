package ru.itmo.java.smit.commands;


import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import ru.itmo.java.smit.core.commit.SmitCommit;
import ru.itmo.java.smit.core.manager.SmitBlobManager;
import ru.itmo.java.smit.exception.SmitException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;


@Parameters(commandNames = "init", commandDescription = "initializes project")
public class CommandInit extends AbstractCommand {

    public CommandInit(SmitBlobManager blobManager) {
        smitBlobManager = blobManager;
    }

    @Override
    public void innerRun() throws SmitException {
        try {
            if (Files.exists(smitBlobManager.getSmitDir())) {
                FileUtils.deleteQuietly(smitBlobManager.getSmitDir().toFile());
            }


            smitBlobManager.createDirs(smitBlobManager.getSmitDir());
            smitBlobManager.createDirs(smitBlobManager.getBlobsDir());
            smitBlobManager.createDirs(smitBlobManager.getCommitsDir());

            smitBlobManager.setIndex(new HashMap<>());
            smitBlobManager.setBranches(new HashMap<>());

            var currCommit = new SmitCommit("init commit", COLORIZED);
            var res = currCommit.construct(smitBlobManager, null, true);

        } catch (IOException e) {
            throw new SmitException(e);
        }
    }

    @Override
    public void showOutput() throws SmitException {
        printStream.println("project has just started");
    }
}
