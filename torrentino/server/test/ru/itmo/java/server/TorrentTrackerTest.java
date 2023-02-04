package ru.itmo.java.server;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.java.message.tracker.*;
import ru.itmo.java.utils.Constant;
import ru.itmo.java.utils.TimeConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentTrackerTest {
    private static final int CLIENT1_PORT = 62980;
    private static final int CLIENT2_PORT = 62981;
    private static final int CLIENT3_PORT = 62982;
    private static final int CLIENT4_PORT = 62983;

    private static final Path TEST_PATH = Paths.get("test", "ru", "itmo", "java", "server", "tmp");
    private static final Path TRACKER_TEST_PATH = TEST_PATH.resolve("tracker");

    private static List<FileData> files = new ArrayList<>();

    private TorrentTracker tracker;

    @BeforeEach
    public void initTracker() throws IOException {
        Files.createDirectories(TRACKER_TEST_PATH);
        tracker = new TorrentTracker(Constant.TRACKER_PORT, TRACKER_TEST_PATH);
        tracker.run();

        createFilesData();
    }

    public void createFilesData() {
        var fileData1 = FileData.newBuilder()
                .setName("x.txt")
                .setSize(20)
                .build();

        var fileData2 = FileData.newBuilder()
                .setName("y.txt")
                .setSize(500)
                .build();

        var fileData3 = FileData.newBuilder()
                .setName("z.txt")
                .setSize(3000)
                .build();

        var fileData4 = FileData.newBuilder()
                .setName("big.txt")
                .setSize(100000000)
                .build();

        files = new ArrayList<>(List.of(fileData1, fileData2, fileData3, fileData4));
    }

    @AfterEach
    public void shutdownTrackerAndCleanDir() throws IOException {
        tracker.close();

        var testDir = TEST_PATH.toFile();
        Collection<File> files =
                FileUtils.listFilesAndDirs(
                        testDir,
                        TrueFileFilter.INSTANCE,
                        TrueFileFilter.INSTANCE);
        for (File file : files) {
            if (!file.equals(testDir)) {
                FileUtils.deleteQuietly(file);
            }
        }
    }

    @Test
    public void testUploadQuery() {
        var uniqueIds =
                files
                        .stream()
                        .map(f -> {
                            try {
                                return MockClient.processUploadQuery(f.getName(), f.getSize());
                            } catch (IOException ignored) {
                            }
                            return -1;
                        })
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(4, uniqueIds.size());

        for (var id : uniqueIds) {
            assertTrue(id >= 0);
        }
    }

    @Test
    public void testListQuery() throws IOException {
        var fileIds =
                files
                        .stream()
                        .map(f -> {
                            try {
                                return MockClient.processUploadQuery(f.getName(), f.getSize());
                            } catch (IOException ignored) {
                            }
                            return -1;
                        })
                        .toList();

        var torrentFiles =
                MockClient.processListQuery()
                        .stream()
                        .map(FileData::getId)
                        .toList();

        assertIterableEquals(fileIds, torrentFiles);
    }

    @Test
    public void testSourcesQuery() throws IOException {
        var fileIds =
                files
                        .stream()
                        .map(f -> {
                            try {
                                return MockClient.processUploadQuery(f.getName(), f.getSize());
                            } catch (IOException ignored) {
                            }
                            return -1;
                        })
                        .toList();

        MockClient.processUpdateQuery(CLIENT1_PORT, 4, fileIds);
        MockClient.processUpdateQuery(CLIENT2_PORT, 2, fileIds.subList(1, 3));
        MockClient.processUpdateQuery(CLIENT3_PORT, 3, List.of(fileIds.get(0), fileIds.get(1), fileIds.get(3)));
        MockClient.processUpdateQuery(CLIENT4_PORT, 1, List.of(fileIds.get(2)));


        var clientPortsWithFile1 =
                MockClient.processSourceQuery(0)
                        .stream()
                        .map(ClientData::getPort)
                        .toList();
        assertEquals(2, clientPortsWithFile1.size());
        assertTrue(clientPortsWithFile1.contains(CLIENT1_PORT));
        assertTrue(clientPortsWithFile1.contains(CLIENT3_PORT));


        var clientPortsWithFile2 =
                MockClient.processSourceQuery(1)
                        .stream()
                        .map(ClientData::getPort)
                        .toList();
        assertEquals(3, clientPortsWithFile2.size());
        assertTrue(clientPortsWithFile2.contains(CLIENT1_PORT));
        assertTrue(clientPortsWithFile2.contains(CLIENT2_PORT));
        assertTrue(clientPortsWithFile2.contains(CLIENT3_PORT));


        var clientPortsWithFile3 =
                MockClient.processSourceQuery(2)
                        .stream()
                        .map(ClientData::getPort)
                        .toList();
        assertEquals(3, clientPortsWithFile3.size());
        assertTrue(clientPortsWithFile3.contains(CLIENT1_PORT));
        assertTrue(clientPortsWithFile3.contains(CLIENT2_PORT));
        assertTrue(clientPortsWithFile3.contains(CLIENT4_PORT));


        var clientPortsWithFile4 =
                MockClient.processSourceQuery(3)
                        .stream()
                        .map(ClientData::getPort)
                        .toList();
        assertEquals(2, clientPortsWithFile4.size());
        assertTrue(clientPortsWithFile4.contains(CLIENT1_PORT));
        assertTrue(clientPortsWithFile4.contains(CLIENT3_PORT));
    }

    @Test
    public void testRemoveIfNoUpdates() throws IOException, InterruptedException {
        var fileIds =
                files
                        .stream()
                        .map(f -> {
                            try {
                                return MockClient.processUploadQuery(f.getName(), f.getSize());
                            } catch (IOException ignored) {
                            }
                            return -1;
                        })
                        .toList();

        System.out.println("-- 4 clients sent updates --");
        MockClient.processUpdateQuery(CLIENT1_PORT, 4, fileIds);
        MockClient.processUpdateQuery(CLIENT2_PORT, 2, fileIds.subList(1, 3));
        MockClient.processUpdateQuery(CLIENT3_PORT, 3, List.of(fileIds.get(0), fileIds.get(1), fileIds.get(3)));
        MockClient.processUpdateQuery(CLIENT4_PORT, 1, List.of(fileIds.get(2)));

        var waitSecs = TimeConstant.TIME_OUT_UPDATE / 2;
        System.out.printf("...waiting %d seconds...%n", waitSecs / 1000);
        Thread.sleep(waitSecs);

        System.out.println("-- 2th and 4th clients resent updates --");
        MockClient.processUpdateQuery(CLIENT1_PORT, 4, fileIds);
        MockClient.processUpdateQuery(CLIENT2_PORT, 2, fileIds.subList(1, 3));

        System.out.printf("...waiting %d seconds...%n", TimeConstant.TIME_OUT_UPDATE / 1000);
        Thread.sleep(TimeConstant.TIME_OUT_UPDATE);

        System.out.println("-- 1st and 3rd should be deleted --");
        assertEquals(2, tracker.getClients().size());
    }


    private static class MockClient {
        private static Socket socket;
        private static InputStream inputStream;
        private static OutputStream outputStream;

        public static void start() throws IOException {
            socket = new Socket("localhost", Constant.TRACKER_PORT);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        }

        private static void close() throws IOException {
            inputStream.close();
            outputStream.close();
            socket.close();
        }


        public static List<FileData> processListQuery() throws IOException {
            start();
            var listRequest = ListRequest.newBuilder().build();
            var trackerRequest = TrackerRequest.newBuilder()
                    .setListRequest(listRequest)
                    .build();

            trackerRequest.writeDelimitedTo(outputStream);

            var trackerResponse = TrackerResponse.parseDelimitedFrom(inputStream);
            var res = trackerResponse.getListResponse().getFileList();

            close();
            return res;
        }


        public static int processUploadQuery(String fileName, long fileSize) throws IOException {
            start();
            var fileData = FileData.newBuilder()
                    .setName(fileName)
                    .setSize(fileSize)
                    .build();

            var uploadRequest = UploadRequest.newBuilder()
                    .setFile(fileData)
                    .build();

            var trackerRequest = TrackerRequest.newBuilder()
                    .setUploadRequest(uploadRequest)
                    .build();

            trackerRequest.writeDelimitedTo(outputStream);

            var trackerResponse = TrackerResponse.parseDelimitedFrom(inputStream);
            var res = trackerResponse.getUploadResponse().getId();

            close();
            return res;
        }

        public static List<ClientData> processSourceQuery(int id) throws IOException {
            start();

            var sourceRequest = SourceRequest.newBuilder()
                    .setId(id).build();

            var trackerRequest = TrackerRequest.newBuilder()
                    .setSourceRequest(sourceRequest)
                    .build();

            trackerRequest.writeDelimitedTo(outputStream);

            var trackerResponse = TrackerResponse.parseDelimitedFrom(inputStream);
            var res = trackerResponse.getSourceResponse().getClientList();

            close();
            return res;
        }

        public static boolean processUpdateQuery(int clientPort, int count, List<Integer> fileIds) throws IOException {
            start();

            var updateRequest = UpdateRequest.newBuilder()
                    .setPort(clientPort)
                    .setCount(count)
                    .addAllId(fileIds)
                    .build();

            var trackerRequest = TrackerRequest.newBuilder()
                    .setUpdateRequest(updateRequest)
                    .build();

            trackerRequest.writeDelimitedTo(outputStream);

            var trackerResponse = TrackerResponse.parseDelimitedFrom(inputStream);
            var res = trackerResponse.getUpdateResponse().getStatus();

            close();
            return res;
        }
    }
}
