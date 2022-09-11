package ru.itmo.sd.bash.res.commands;

import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class ExternalCommand implements Command {

    @Override
    public InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException, WrongSyntaxException {
        Process process;

        try {
           var processBuilder = new ProcessBuilder(arguments);
           processBuilder.directory(Path.of(envManager.get("PWD")).toFile());
           if (input == System.in) {
               processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
           }

           process = processBuilder.start();

           if (input != System.in) {
               input.transferTo(process.getOutputStream());
               process.getOutputStream().close();
           }

           process.waitFor();

        }
        catch (IOException exception) {
           throw new WrongSyntaxException("This command was not found even in external call!");
        }
        catch (InterruptedException e) {
            throw new WrongSyntaxException("Interrupted exception happened");
        }


        return process.getInputStream();
    }
}
