package ru.itmo.sd.bash.res.commands;


import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface Command {

    InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException,
            WrongSyntaxException;
}