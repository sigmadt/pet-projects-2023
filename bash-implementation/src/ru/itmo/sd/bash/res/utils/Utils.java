package ru.itmo.sd.bash.res.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Utils {

    private Utils() {
    }

    public static Path getCurrentDir() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static String getTestFilesDir() {
        return "test/ru/itmo/sd/bash/temp";
    }

    public static Map<String, String> getTestFilesPaths() {
        var testFolder = "test/ru/itmo/sd/bash/temp";
        return Map.of(
                "test", testFolder + "/test.txt",
                "forth", testFolder + "/forth.fs",
                "zmej", testFolder + "/zmej.py"
        );
    }

    public static InputStream emptyInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };
    }
}