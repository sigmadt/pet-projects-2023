package ru.itmo.java.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ru.itmo.java.utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Application {
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private static final String LIST = "list";

    private static final String DOWNLOAD = "download";
    private static final String UPLOAD = "upload";

    private final JCommander jCom;

    @Parameter(names = {"--help"}, help = true)
    private boolean help;

    @Parameter(names = {"-p", "--port"})
    private int clientPort = 0;

    @Parameter(names = {"-h", "--host"})
    private String host = "localhost";

    private InetAddress hostAddress;

    private TorrentClient torrentClient;

    private boolean isAlreadyClosed = false;

    public static void main(String[] args) throws IOException {
        new Application(args);
    }

    private Application(String[] args) {
        jCom = new JCommander(this);

        try {
            jCom.parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            jCom.usage();
            System.exit(1);
        }

        if (help) {
            jCom.usage();
        } else {
            exec();
        }
    }

    private boolean initTorrentClient() {
        hostAddress = Utils.getIpAddress(host);
        torrentClient = TorrentClient.start(clientPort);

        return torrentClient != null;
    }

    private void exec() {
        if (!initTorrentClient()) {
            return;
        }

        translate();
    }

    private void translate() {
        var user = Utils.getUserName();
        var currPath = Utils.getUserDirAbsPath();

        keyboardExit();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.printf("@%s | %s$ ", user, currPath.getFileName());

                var line = scanner.nextLine();
                var splitInput = Arrays.asList(line.split("\\s"));

                var cmd = splitInput.get(0);

                var shouldExit = false;

                switch (cmd) {
                    case LIST -> handleListQuery();
                    case UPLOAD -> handleUploadQuery(splitInput);
                    case DOWNLOAD -> handleDownloadQuery(splitInput);
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
            torrentClient.close();
            isAlreadyClosed = true;
            System.out.println("goodbye!");
        } catch (Exception ignored) {
        }
    }

    private void keyboardExit() {
        Runtime.getRuntime()
                .addShutdownHook(new Thread(this::tryClose));
    }

    private void handleListQuery() {
        try {
            tryRunClient();

            var files = torrentClient.getFiles();

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
        } catch (Exception e) {
            System.out.println("Error happened during handling list query");
        }
    }

    private void handleUploadQuery(List<String> splitInput) {
        if (splitInput.size() != 2) {
            System.out.println("Invalid number of arguments. Use 'help' to see usage.");
            return;
        }

        try {
            var filePath = splitInput.get(1);
            tryRunClient();

            torrentClient.upload(filePath);

        } catch (Exception e) {
            System.out.println("Error happened during handling upload query");
        }
    }

    private void handleDownloadQuery(List<String> splitInput) {
        if (splitInput.size() < 2) {
            System.out.println("Invalid number of arguments. Use 'help' to see usage.");
            return;
        }

        try {
            var fileId = Integer.parseInt(splitInput.get(1));

            var destinationDir =
                    splitInput.size() == 2 ?
                            Paths.get(".") :
                            Paths.get(splitInput.get(2));

            tryRunClient();

            torrentClient.download(fileId, destinationDir);
        } catch (Exception e) {
            System.out.println("Error happened during handling download query");
        }

    }

    private void usageMessage() {
        System.out.println("****************************************************************");
        System.out.println("upload <source>             – upload file from local machine");
        System.out.println("list                        – list of available files ");
        System.out.println("download <id> [destination] – download file with id from torrent, destination is optional");
        System.out.println("exit                        – quit connection to torrent");
        System.out.println("****************************************************************");
    }

    private void tryRunClient() {
        try {
            torrentClient.run(hostAddress);
        } catch (IOException e) {
            System.out.printf("cannot run client for %s \n", hostAddress.getCanonicalHostName());
            System.out.println(Utils.getCheckMessage());
        }
    }
}
