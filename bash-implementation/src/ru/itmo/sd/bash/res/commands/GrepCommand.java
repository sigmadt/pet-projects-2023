package ru.itmo.sd.bash.res.commands;


import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.exceptions.GrepFlagAException;
import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GrepCommand implements Command {
    private JCommander jCom;

    @Parameter(names = "-w", description = "to find whole words only")
    private boolean wFlag = false;

    @Parameter(names = "-i", description = "to ignore lower and upper case")
    private boolean iFlag = false;

    @Parameter(names = "-l", description = "to list names of matching files")
    private boolean lFlag = false;

    @Parameter(names = "-c", description = "to count the number of matched strings")
    private boolean cFlag = false;

    @Parameter(
            names = "-A",
            description = "to print the number of strings after matched string",
            validateWith = GrepFlagAException.class)
    private int aCapitalFlag = 0;

    @Parameter(description = "feed arguments to grep command")
    private LinkedList<String> givenArguments = new LinkedList<>();

    @Override
    public InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException, WrongSyntaxException {
        jCom = new JCommander(this);

        try {
            jCom.parse(arguments.toArray(new String[0]));
        } catch (ParameterException e) {
            throw new WrongSyntaxException(e.getMessage());

        }

        if (givenArguments.isEmpty()) {
            throw new WrongSyntaxException(wrongUsageMessage());
        }


        var regExpression = givenArguments.removeFirst();

        regExpression = wFlag ? wholeWordWrapper(regExpression) : regExpression;

        var searchedPattern = iFlag ?
                Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE) :
                Pattern.compile(regExpression);

        var output = new StringJoiner("\n");

        if (givenArguments.isEmpty()) {
            var matchedLines = getMatchedLines(
                    new String(input.readAllBytes(), StandardCharsets.UTF_8),
                    searchedPattern);

            for (var line : matchedLines) {
                output.add(line);
            }
            output.add("\n");
            return new ByteArrayInputStream(output.toString().getBytes(StandardCharsets.UTF_8));
        }


        var pathsToContentMap =
                givenArguments
                        .stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                arg -> {
                                    try {
                                        return Files.readString(Paths.get(arg));
                                    } catch (IOException | InvalidPathException e) {
                                        throw new WrongSyntaxException("There is no such file for this path");
                                    }
                                }
                        ));

        var matchedPathsToContent = pathsToContentMap
                .entrySet()
                .stream()
                .map(pair -> Map.entry(pair.getKey(), getMatchedLines(pair.getValue(), searchedPattern)))
                .collect(Collectors.toList());

        boolean flags = false;
        if (lFlag) {
            flags = true;
            var matchedFiles = matchedPathsToContent
                    .stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining("\n"));

            output.add(matchedFiles);
        }
        if (cFlag) {
            flags = true;
            if (matchedPathsToContent.size() == 1) {
                output.add(String.valueOf(matchedPathsToContent.get(0).getValue().size()));
            } else {
                var countMatchedStrings = matchedPathsToContent
                        .stream()
                        .map(
                                el -> String.format("%s:%s", el.getKey(), el.getValue().size())
                        )
                        .collect(Collectors.joining("\n"));

                output.add(countMatchedStrings);
            }

        }

        if (!flags) {
            var defaultOutput =
                    matchedPathsToContent
                            .stream()
                            .map(Map.Entry::getValue)
                            .map(el -> String.join("\n", el))
                            .collect(Collectors.joining("\n~~~~~~~~~~\n"));

            output.add(defaultOutput);
        }


        return new ByteArrayInputStream(output.toString().concat("\n").getBytes(StandardCharsets.UTF_8));

    }


    private String wholeWordWrapper(String reg) {
        return String.format("\\b%s\\b", reg);
    }


    private String wrongUsageMessage() {
        StringBuilder sb = new StringBuilder();
        var jComFormatter = jCom.getUsageFormatter();
        jComFormatter.usage(sb);

        return String.format("Wrong syntax for the grep command. Check usage: %s", sb);
    }


    public static class afterLinesPredicate<T> implements Predicate<T> {
        int curr = 0;
        int nLinesAfter = 0;
        Predicate<T> test;

        afterLinesPredicate(Predicate<T> test, int n) {
            this.test = test;
            nLinesAfter = n;
        }

        public boolean test(T t) {
            if (test.test(t)) {
                curr = nLinesAfter;
                return true;
            }
            if (curr > 0) {
                curr--;
                return true;
            }
            return false;
        }
    }


    private List<String> getMatchedLines(String input, Pattern pattern) {
        var newLineSplitter = "\\r?\\n";
        var lines = input.split(newLineSplitter);

        var predicate = new afterLinesPredicate<String>(s -> pattern.matcher(s).find(), aCapitalFlag);

        return Arrays.stream(lines)
                .filter(predicate)
                .collect(Collectors.toList());

    }


}