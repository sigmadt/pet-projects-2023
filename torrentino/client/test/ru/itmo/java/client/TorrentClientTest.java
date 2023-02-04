package ru.itmo.java.client;

import com.google.protobuf.ByteString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itmo.java.message.tracker.*;
import ru.itmo.java.server.Server;
import ru.itmo.java.utils.Constant;
import ru.itmo.java.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentClientTest {
    private static final InetAddress ip = Utils.getIpAddress("localhost");
    private static final int CLIENT1_PORT = 62980;
    private static final int CLIENT2_PORT = 62981;
    private static final int CLIENT3_PORT = 62982;

    private static final Path TEST_PATH = Paths.get("test", "ru", "itmo", "java", "client", "tmp");
    private static final Path CLIENT1_TEST_PATH = TEST_PATH.resolve("cl1");
    private static final Path CLIENT2_TEST_PATH = TEST_PATH.resolve("cl2");
    private static final Path CLIENT3_TEST_PATH = TEST_PATH.resolve("cl3");

    private MockTrackerServer tracker;

    @BeforeEach
    public void init() throws IOException {
        tracker = new MockTrackerServer("MockTracker");
        Files.createDirectories(CLIENT1_TEST_PATH);
        Files.createDirectories(CLIENT2_TEST_PATH);
        Files.createDirectories(CLIENT3_TEST_PATH);

        tracker.run();
    }

    @AfterEach
    public void shutdownAndCleanDir() throws IOException {
        tracker.shutdown();

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
    public void testLaunchTorrent() throws IOException {
        var cl1 = TorrentClient.start(CLIENT1_PORT, CLIENT1_TEST_PATH);
        assertNotNull(cl1);
        cl1.run(ip);
        cl1.close();
    }

    @Test
    public void testUpdate() throws Exception {
        var cl1 = TorrentClient.start(CLIENT1_PORT, CLIENT1_TEST_PATH);
        assertNotNull(cl1);
        cl1.run(ip);

        Thread.sleep(1000);

        assertTrue(tracker.clients.contains(TestUtils.buildClient(CLIENT1_PORT)));
        cl1.close();
    }

    @Test
    public void testUpload() throws Exception {
        var cl1 = TorrentClient.start(CLIENT1_PORT, CLIENT1_TEST_PATH);
        assertNotNull(cl1);
        cl1.run(ip);
        Thread.sleep(1000);

        var filePath = TestUtils.createSmallFile(CLIENT1_TEST_PATH);
        var fileData = TestUtils.buildFile(filePath);
        cl1.upload(filePath.toString());

        Thread.sleep(1000);

        assertTrue(tracker.files.contains(fileData));

        cl1.close();
    }


    @Test
    public void testList() throws Exception {
        var cl1 = TorrentClient.start(CLIENT1_PORT, CLIENT1_TEST_PATH);
        var cl2 = TorrentClient.start(CLIENT2_PORT, CLIENT2_TEST_PATH);
        assertNotNull(cl1);
        assertNotNull(cl2);

        cl1.run(ip);
        cl2.run(ip);

        var smallFilePath = TestUtils.createSmallFile(CLIENT1_TEST_PATH);
        var largeFilePath = TestUtils.createLargeFile(CLIENT2_TEST_PATH, 200);
        Thread.sleep(5000);

        cl1.upload(smallFilePath.toString());
        Thread.sleep(1000);

        cl2.upload(largeFilePath.toString());
        Thread.sleep(1000);

        assertEquals(2, tracker.files.size());

        var cl1RequestedList = cl1.getFiles();
        Thread.sleep(1000);

        var cl2RequestedList = cl2.getFiles();
        Thread.sleep(1000);

        assertIterableEquals(cl1RequestedList, cl2RequestedList);

        cl1.close();
        cl2.close();
    }


    @Test
    public void testDownloadForSmallFile() throws Exception {
        var cl1 = TorrentClient.start(CLIENT1_PORT, CLIENT1_TEST_PATH);
        var cl3 = TorrentClient.start(CLIENT3_PORT, CLIENT3_TEST_PATH);
        assertNotNull(cl1);
        assertNotNull(cl3);

        cl1.run(ip);
        cl3.run(ip);
        Thread.sleep(1000);

        var smallFilePathClient2 = TestUtils.createSmallFile(CLIENT1_TEST_PATH);

        cl1.upload(smallFilePathClient2.toString());
        Thread.sleep(1000);

        cl3.download(17, CLIENT3_TEST_PATH);
        Thread.sleep(2000);

        var smallFilePathClient3 = CLIENT3_TEST_PATH.resolve(smallFilePathClient2.getFileName());

        // 1. check exists
        assertTrue(Files.exists(smallFilePathClient3));

        // 2. check if lengths are equal
        assertEquals(smallFilePathClient2.toFile().length(), smallFilePathClient3.toFile().length());

        // 3. compare contents
        assertTrue(IOUtils.contentEquals(
                new FileInputStream(smallFilePathClient2.toFile()),
                new FileInputStream(smallFilePathClient3.toFile())
        ));

        cl1.close();
        cl3.close();
    }

    @Test
    public void testDownloadForLargeFile() throws Exception {
        var cl2 = TorrentClient.start(CLIENT1_PORT, CLIENT2_TEST_PATH);
        var cl3 = TorrentClient.start(CLIENT3_PORT, CLIENT3_TEST_PATH);
        assertNotNull(cl2);
        assertNotNull(cl3);

        cl2.run(ip);
        cl3.run(ip);

        var largeFilePathClient3 = TestUtils.createLargeFile(CLIENT3_TEST_PATH, 800);
        Thread.sleep(5000);

        cl3.upload(largeFilePathClient3.toString());
        Thread.sleep(1000);

        cl2.download(17, CLIENT2_TEST_PATH);
        Thread.sleep(5000);

        var largeFilePathClient2 = CLIENT3_TEST_PATH.resolve(largeFilePathClient3.getFileName());

        // 1. check exists
        assertTrue(Files.exists(largeFilePathClient2));

        // 2. check if lengths are equal
        assertEquals(largeFilePathClient2.toFile().length(), largeFilePathClient3.toFile().length());

        // 3. compare contents
        assertTrue(IOUtils.contentEquals(
                new FileInputStream(largeFilePathClient2.toFile()),
                new FileInputStream(largeFilePathClient3.toFile())
        ));

        cl2.close();
        cl3.close();
    }


    public static class MockTrackerServer extends Server {
        private final List<FileData> files = new ArrayList<>(2);
        private final List<ClientData> clients = new ArrayList<>(2);
        private final AtomicInteger currIdIndex = new AtomicInteger();

        public MockTrackerServer(String name) {
            super(Constant.TRACKER_PORT, name);

            setHandler((socket) -> new MockTrackerHandler(socket, files, clients, currIdIndex));
        }


    }

    public static class MockTrackerHandler implements Runnable {
        private boolean running = true;

        private final List<FileData> files;

        private final List<ClientData> clients;

        private final Socket socket;

        private final AtomicInteger currIdIndex;


        public MockTrackerHandler(Socket socket, List<FileData> files, List<ClientData> clients, AtomicInteger currIdIndex) {
            this.files = files;
            this.socket = socket;
            this.clients = clients;
            this.currIdIndex = currIdIndex;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    var trackerRequest = TrackerRequest.parseDelimitedFrom(socket.getInputStream());
                    if (trackerRequest == null) {
                        break;
                    }
                    if (trackerRequest.hasListRequest()) {
                        handleListRequest(trackerRequest.getListRequest());
                    } else if (trackerRequest.hasUploadRequest()) {
                        handleUploadRequest(trackerRequest.getUploadRequest());
                    } else if (trackerRequest.hasSourceRequest()) {
                        handleSourceRequest(trackerRequest.getSourceRequest());
                    } else if (trackerRequest.hasUpdateRequest()) {
                        handleUpdateRequest(trackerRequest.getUpdateRequest());
                    }
                }
            } catch (IOException ignored) {
            }
        }

        private void handleListRequest(ListRequest listRequest) throws IOException {
            var listResponse = ListResponse.newBuilder()
                    .addAllFile(files)
                    .build();

            TrackerResponse.newBuilder()
                    .setListResponse(listResponse)
                    .build()
                    .writeDelimitedTo(socket.getOutputStream());
        }


        private void handleUploadRequest(UploadRequest uploadRequest) throws IOException {
            var givenFileData = uploadRequest.getFile();
            var fileId = TestUtils.fileIds.get(currIdIndex.getAndIncrement());

            var fileData = FileData.newBuilder()
                    .setName(givenFileData.getName())
                    .setSize(givenFileData.getSize())
                    .setId(fileId)
                    .build();

            files.add(fileData);

            var uploadResponse = UploadResponse.newBuilder()
                    .setId(fileId).build();

            TrackerResponse.newBuilder()
                    .setUploadResponse(uploadResponse)
                    .build()
                    .writeDelimitedTo(socket.getOutputStream());
        }

        private void handleSourceRequest(SourceRequest sourceRequest) throws IOException {
            var sourceResponse = SourceResponse.newBuilder()
                    .setCount(clients.size())
                    .addAllClient(clients)
                    .build();

            TrackerResponse.newBuilder()
                    .setSourceResponse(sourceResponse)
                    .build()
                    .writeDelimitedTo(socket.getOutputStream());
        }


        private void handleUpdateRequest(UpdateRequest updateRequest) throws IOException {
            var status = true;

            var clientPort = updateRequest.getPort();
            var fileIds = updateRequest.getIdList();
            var ip = socket.getInetAddress().getAddress();

            var client = ClientData.newBuilder()
                    .setIp(ByteString.copyFrom(ip))
                    .setPort(clientPort)
                    .build();

            clients.add(client);


            var updateResponse = UpdateResponse.newBuilder()
                    .setStatus(status)
                    .build();


            TrackerResponse.newBuilder()
                    .setUpdateResponse(updateResponse)
                    .build()
                    .writeDelimitedTo(socket.getOutputStream());
        }
    }
}