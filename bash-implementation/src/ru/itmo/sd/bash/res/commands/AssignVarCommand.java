package ru.itmo.sd.bash.res.commands;


import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.Utils;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AssignVarCommand implements Command {
    @Override
    public InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException, WrongSyntaxException {
        if (arguments.size() == 2) {
            var variable = arguments.get(0);
            var elem = arguments.get(1);
            envManager.set(variable, elem);

            return Utils.emptyInputStream();
        }
        else {
            throw new WrongSyntaxException("Can not assign, number of arguments should be exactly 2");
        }

    }
}