package ru.itmo.java.client;

import com.google.protobuf.ByteString;
import org.apache.commons.io.FileUtils;
import ru.itmo.java.message.tracker.ClientData;
import ru.itmo.java.message.tracker.FileData;
import ru.itmo.java.utils.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {
    private static final InetAddress ip = Utils.getIpAddress("localhost");
    public static final List<Integer> fileIds = Arrays.asList(17, 88, 9999);


    public static Path createSmallFile(Path dirPath) throws IOException {
        var filePath = dirPath.resolve("x.txt");
        FileUtils.touch(filePath.toFile());

        FileWriter fileWriter = new FileWriter(filePath.toString());
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("Some golden quote from Dominik TorreNto: \n");
        printWriter.println("\"I don't have friends, I have family.\"");
        printWriter.close();

        return filePath;
    }


    public static Path createLargeFile(Path dirPath, int sizeInMB) throws IOException {
        long sizeInBytes = (long) sizeInMB * 1024 * 1024;
        long size = sizeInBytes / 8;

        var filePath = dirPath.resolve("big.txt");
        FileUtils.touch(filePath.toFile());

        var ss =
                Stream.iterate(111111, (Integer n) -> n + 1)
                        .map(Object::toString)
                        .limit(size);
        System.out.printf("-- Creating large file with size %dMb --%n", sizeInMB);
        Files.write(filePath, (Iterable<String>) ss::iterator);
        System.out.println("-- File is ready --");

        return filePath;
    }

    public static ClientData buildClient(int port) {
        return ClientData.newBuilder()
                .setIp(ByteString.copyFrom(ip.getAddress()))
                .setPort(port)
                .build();
    }

    public static FileData buildFile(Path filePath) {
        var file = filePath.toFile();

        return FileData.newBuilder()
                .setName(filePath.getFileName().toString())
                .setSize(file.length())
                .setId(fileIds.get(0))
                .build();
    }
}
