package ru.itmo.sd.bash.res.commands;


import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CatCommand implements Command {


    @Override
    public InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException, WrongSyntaxException {
        if (arguments.isEmpty()) {
            return input;
        }

        var concatContentBytes=
                arguments
                .stream()
                .map(
                arg -> {
                    try {
                    return Files.readAllLines(Paths.get(arg).toAbsolutePath(), StandardCharsets.UTF_8);
                    }
                    catch (IOException | InvalidPathException e) {
                        throw new WrongSyntaxException("There is no such file for this path");
                    }
                }
                )
                .map(lines -> String.join("\n", lines))
                .collect(Collectors.joining())
                .concat("\n")
                .getBytes(StandardCharsets.UTF_8);

        return new ByteArrayInputStream(concatContentBytes);
    }
}