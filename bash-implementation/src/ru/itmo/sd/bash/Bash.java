package ru.itmo.sd.bash;


import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.Executor;
import ru.itmo.sd.bash.res.utils.Parser;
import ru.itmo.sd.bash.res.utils.Replacer;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class Bash {
    private static final String bashSymbol = ">> ";
    private static final String bashWelcome =
            "Hello! You are currently using bash imitation created by @sigmadt, "
                    .concat("consider following. ")
                    .concat("\n")
                    .concat("Version : 1.0.2")
                    .concat("\n")
                    .concat("type something below ↓");
            
    private static final List<String> stopWords = Arrays.asList("quit", ":q", "exit");

    private static final EnvManager env = new EnvManager();
    private static final Parser parser = new Parser();
    private static final Replacer replacer = new Replacer();


    public static void main(String[] args) {
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
        var bash = new Bash();
        System.out.println(bashWelcome);
        while (true) {
            System.out.print(bashSymbol);
            try {
                String userInput = bufReader.readLine();
                if (stopWords.contains(userInput)) {
                    System.out.println("----exiting bash----");
                    break;
                }
                try {
                    bash.translate(userInput);
                } catch (WrongSyntaxException exception) {
                    System.out.println(exception.getMessage());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }


    public void translate(String input) throws WrongSyntaxException {
        var tokens = parser.run(input);
        var tokensReplaced = replacer.apply(tokens, env);

        Executor executor = new Executor();
        var inpStream = executor.run(tokensReplaced, env);
        PrintStream outStream = System.out;
        try {
            outStream.print(new String(inpStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}