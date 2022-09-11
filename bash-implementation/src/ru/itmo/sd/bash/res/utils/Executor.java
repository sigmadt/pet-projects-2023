package ru.itmo.sd.bash.res.utils;

import ru.itmo.sd.bash.res.commands.*;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Executor {
    private final HashMap<String, Command> cmdStorage;

    public Executor() {
        cmdStorage = new HashMap<>();

        cmdStorage.put("cat", new CatCommand());
        cmdStorage.put("echo", new EchoCommand());
        cmdStorage.put("pwd", new PwdCommand());
        cmdStorage.put("wc", new WcCommand());
        cmdStorage.put("grep", new GrepCommand());
    }

    public List<String> getCommandNames() {
        return new ArrayList<>(cmdStorage.keySet());
    }


    public InputStream run(List<Token> givenTokens, EnvManager envManager) {
        var inputStream = System.in;

        var pipedTokens = givenTokens.stream().collect(new PipeCollector());


        for (var tokenList : pipedTokens) {
            if (tokenList.isEmpty()) {
                throw new WrongSyntaxException("Given pipe into the void");
            }

            var maybeCmd = tokenList.get(0);

            if (maybeCmd.getType() == Token.Type.ASSIGN) {
                inputStream = assignmentCase(maybeCmd, inputStream, envManager);
                continue;
            }

            if (cmdStorage.containsKey(maybeCmd.getInside())) {
                inputStream = defaultCase(
                        cmdStorage.get(maybeCmd.getInside()),
                        tokenList.subList(1, tokenList.size()),
                        inputStream,
                        envManager);

            } else {
                inputStream = defaultCase(
                        new ExternalCommand(),
                        tokenList,
                        inputStream,
                        envManager
                );
            }
        }

        return inputStream;
    }

    private InputStream assignmentCase(Token assignToken, InputStream inputStream, EnvManager envManager) {
        try {
            var parts = assignToken.getInside().split("=");

            var cmd = new AssignVarCommand();
            return cmd.run(inputStream, List.of(parts), envManager);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Utils.emptyInputStream();
    }


    private InputStream defaultCase(Command actualCmd,
                                    List<Token> restTokens,
                                    InputStream inputStream,
                                    EnvManager envManager) {
        try {
            var actualArgs =
                    restTokens
                            .stream()
                            .map(Token::getInside)
                            .collect(Collectors.toList());

            return actualCmd.run(inputStream, actualArgs, envManager);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Utils.emptyInputStream();
    }


}