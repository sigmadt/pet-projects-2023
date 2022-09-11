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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WcCommand implements Command {

    public Map<String, Integer> getNums(String fileContent) {
        var nums = new HashMap<String, Integer>();

        nums.put("lines", (int) fileContent.lines().count());
        nums.put("words", fileContent.trim().split("\\s+").length);
        nums.put("bytes", fileContent.length());

        return nums;
    }

    public String beautifulPrint(String fileName, Map<String, Integer> nums) {
//        var first = "\t lines \t words \t bytes \t file";

        var res = nums
                .values()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\t"));

        return res.concat(" ").concat(fileName);
    }

    @Override
    public InputStream run(InputStream input, List<String> arguments, EnvManager envManager) throws IOException,
            WrongSyntaxException {
        if (arguments.isEmpty()) {
            var numsForInputStream =
                    getNums(new String(input.readAllBytes(), StandardCharsets.UTF_8));

            var bytesForResult = beautifulPrint("", numsForInputStream)
                    .getBytes(StandardCharsets.UTF_8);

            return new ByteArrayInputStream(bytesForResult);
        } else {


            var fileNums =
                    arguments
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
                            ))
                            .entrySet()
                            .stream()
                            .map(pair -> beautifulPrint(pair.getKey(), getNums(pair.getValue())))
                            .collect(Collectors.joining("\n"));

            var totalNums =
                    arguments
                            .stream()
                            .map(
                                    arg -> {
                                        try {
                                            return getNums(Files.readString(Paths.get(arg)));
                                        } catch (IOException | InvalidPathException e) {
                                            throw new WrongSyntaxException("There is no such file for this path");
                                        }
                                    }
                            )
                            .flatMap(m -> m.entrySet().stream())
                            .collect(
                                    Collectors.toMap(
                                            Map.Entry::getKey,
                                            Map.Entry::getValue,
                                            Integer::sum
                                    )
                            );


            var resultContent = arguments.size() < 2 ? fileNums.concat("\n")
                    : fileNums
                    .concat("\n")
                    .concat(beautifulPrint("total", totalNums))
                    .concat("\n");

            return new ByteArrayInputStream(resultContent.getBytes(StandardCharsets.UTF_8));
        }


    }
}