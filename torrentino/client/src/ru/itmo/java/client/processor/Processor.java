package ru.itmo.java.client.processor;

import ru.itmo.java.task.IOTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Processor implements AutoCloseable {
    protected Socket socket;

    protected final ExecutorService readPool = Executors.newSingleThreadExecutor();
    protected final ExecutorService writePool = Executors.newSingleThreadExecutor();


    public void connect(InetAddress ip, int port) throws IOException {
        socket = new Socket(ip, port);
        // System.out.println("***Socket created: " + socket);
    }


    protected void executeWriteTask(IOTask task) {
        writePool.submit(() -> {
            try {
                task.run();
            } catch (IOException ignored) {
            }
        });
    }


    @Override
    public void close() throws IOException {
        if (socket == null) {
            return;
        }
        socket.close();
        readPool.shutdownNow();
        writePool.shutdownNow();
    }

    public void tryCloseSocket() throws IOException {
        if (socket.isClosed()) {
            return;
        }
        socket.close();
    }
}
