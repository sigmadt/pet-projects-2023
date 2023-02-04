package ru.itmo.java.utils;

import ru.itmo.java.message.tracker.ClientData;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    private static final Random GEN = new Random();
    private Utils() {
    }

    public static String getUserName() {
        return System.getProperty("user.name");
    }

    public static Path getUserDirAbsPath() {
        return Paths.get(System.getProperty("user.dir"));
    }

    public static Path getStorePath() {
        return getUserDirAbsPath().resolve(".session");
    }

    public static Path resolveStorePathForTracker(Path workDir) {
        return workDir.resolve(".session");
    }

    public static Path resolveStorePathForClient(Path workDir) {
        return workDir.resolve(".data");
    }

    public static Path getStorePathForClient() {
        return getUserDirAbsPath().resolve(".data");
    }

    public static byte[] getIpAddressInBytes(String ip) {
        try {
            return InetAddress.getByName(ip).getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintIpAddress(byte[] ipBytes) {
        return IntStream.range(0, ipBytes.length)
                .mapToObj(b -> Byte.toString(ipBytes[b]))
                .collect(Collectors.joining("."));
    }

    public static String prettyPrintClientData(ClientData clientData) {
        return String.format(
                "client with ip[%s] ; port[%d] %n",
                prettyPrintIpAddress(clientData.getIp().toByteArray()),
                clientData.getPort());
    }

    public static InetAddress getIpAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static long calculateNParts(long fileSize) {
        if (fileSize % FileConstant.BLOCK == 0) {
            return fileSize / FileConstant.BLOCK;
        }
        return fileSize / FileConstant.BLOCK + 1;
    }

    public static String getFormatForListQuery() {
        return "%8d \t %15s \t %d\n";
    }

    public static String getCheckMessage() {
        return
                """
                        If you got this error check these things:
                        – Torrent Tracker is running
                        – Client port is not used
                        """;
    }

    public static boolean isFileValid(Path filePath) {
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            return false;
        }
        return true;
    }


    public static <S> S getRandomElement(List<S> collection) {
        var pos = GEN.nextInt(collection.size());
        return collection.get(pos);
    }

    public static int getBlockSize(int partId, long fileSize) {
        if (fileSize % FileConstant.BLOCK == 0) {
            return FileConstant.BLOCK;
        }

        var nParts = calculateNParts(fileSize);

        if (partId + 1 < nParts) {
            return FileConstant.BLOCK;
        }

        return (int) (fileSize % FileConstant.BLOCK);
    }

    public static String drawProgressBar(int currentValue, int maxValue) {
        int progressBarLength = 33;

        if (progressBarLength < 9 || progressBarLength % 2 == 0) {
            throw new ArithmeticException("error in progress bar");
        }

        int currentProgressBarIndex = (int) Math.ceil(((double) progressBarLength / maxValue) * currentValue);
        var formattedPercent = String.format(" %5.1f %% ", (100 * currentProgressBarIndex) / (double) progressBarLength);
        int percentStartIndex = ((progressBarLength - formattedPercent.length()) / 2);

        var sb = new StringBuilder();
        sb.append("||");
        for (int progressBarIndex = 0; progressBarIndex < progressBarLength; progressBarIndex++) {
            if (progressBarIndex <= percentStartIndex - 1 || progressBarIndex >= percentStartIndex + formattedPercent.length()) {
                sb.append(currentProgressBarIndex <= progressBarIndex ? " " : "=");
            } else if (progressBarIndex == percentStartIndex) {
                sb.append(formattedPercent);
            }
        }
        sb.append("||");
        return sb.toString();
    }
}
