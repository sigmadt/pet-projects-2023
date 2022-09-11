package ru.itmo.sd.bash;


import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.Executor;
import ru.itmo.sd.bash.res.utils.Parser;
import ru.itmo.sd.bash.res.utils.Replacer;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class BashTranslateHelper {
    private static final List<String> stopWords = Arrays.asList("quit", ":q", "exit");

    private static final EnvManager env = new EnvManager();
    private static final Parser parser = new Parser();
    private static final Replacer replacer = new Replacer();

    public String run(String input) {
        if (stopWords.contains(input)) {
            return "Bash shutdown";
        }

        var result = new StringBuilder();
        try {
            var tokens = parser.run(input);
            var tokensReplaced = replacer.apply(tokens, env);

            Executor executor = new Executor();
            var inpStream = executor.run(tokensReplaced, env);

            result.append(new String(inpStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (WrongSyntaxException e) {
            result.append(e.getMessage());
        } catch (IOException e) {
            result.append("IOException happened!");
        }

        return result.toString();
    }
}