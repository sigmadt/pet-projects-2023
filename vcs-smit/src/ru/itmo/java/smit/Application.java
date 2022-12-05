package ru.itmo.java.smit;

import ru.itmo.java.smit.core.manager.SmitCommandManager;

import java.io.File;
import java.nio.file.Path;


public class Application {
    public static void main(String[] args) {
        var userDir = System.getProperty("user.dir");
        var pathToCurrUserDir = Path.of(userDir);

        var scm = new SmitCommandManager(pathToCurrUserDir);
        scm.run(args);
    }
}
