package ru.itmo.java;


import ru.itmo.java.server.TorrentTracker;
import ru.itmo.java.utils.Utils;

import java.util.Arrays;
import java.util.Scanner;

public class Application {
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private static final String LIST = "list";

    private static final String USERS = "users";

    private TorrentTracker tracker;
    private boolean isAlreadyClosed = false;


    public static void main(String[] args) {
        new Application(args);
    }

    public Application(String[] args) {
        exec();
    }

    private boolean initTorrentTracker() {
        tracker = new TorrentTracker();


        try {
            if (tracker.canLoad()) {
                System.out.println("Found tracker files from previous session");
                tracker.load();
            } else {
                System.out.println("Initializing new session...");
            }
            tracker.run();
            return true;
        } catch (Exception e) {
            System.out.println("Can't initialize and run torrent tracker");
        }
        return false;
    }

    private void exec() {
        if (!initTorrentTracker()) {
            return;
        }

        translate();
    }

    private void translate() {
        var server = Utils.getUserName();
        var currPath = Utils.getUserDirAbsPath();

        keyboardExit();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.printf("@%s | %s$ ", server, currPath.getFileName());

                var line = scanner.nextLine();
                var splitInput = Arrays.asList(line.split("\\s"));

                var cmd = splitInput.get(0);
                var shouldExit = false;

                switch (cmd) {
                    case LIST -> handleListQuery();
                    case USERS -> handleUsersQuery();
                    case EXIT -> {
                        tryClose();
                        shouldExit = true;
                        System.exit(0);
                    }
                    case HELP -> usageMessage();
                    default -> System.out.println("Invalid command. Use 'help' to see usage.");
                }

                if (shouldExit) {
                    break;
                }
            }
        }
    }

    private void tryClose() {
        try {
            if (isAlreadyClosed) {
                return;
            }
            tracker.close();
            isAlreadyClosed = true;
            System.out.println("Tracker is closed");
        } catch (Exception ignored) {
        }
    }

    private void keyboardExit() {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::tryClose));
    }

    private void handleListQuery() {
        var files = tracker.getFiles();

        var fmt = Utils.getFormatForListQuery();
        System.out.printf("%8s \t %15s \t %s\n", "ID", "Name", "Size");
        files
                .forEach(
                        fileData ->
                                System.out.printf(fmt,
                                        fileData.getId(),
                                        fileData.getName(),
                                        fileData.getSize())
                );
    }

    private void handleUsersQuery() {
        var clients = tracker.getClients().keySet();
        clients
                .forEach(
                        clientData -> System.out.printf(
                                "ip[%s] | port[%d] %s %n",
                                Utils.prettyPrintIpAddress(clientData.getIp().toByteArray()),
                                clientData.getPort(),
                                tracker.getClients().get(clientData).toString()
                                )
                );
    }

    private void usageMessage() {
        System.out.println("****************************************************************");
        System.out.println("list                        – list of available files");
        System.out.println("users                       – list of active users");
        System.out.println("exit                        – close torrent server");
        System.out.println("****************************************************************");
    }
}