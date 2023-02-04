package ru.itmo.java.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class Server {
    protected String name;

    private final int port;

    private ExecutorService pool;

    private boolean isRunning = true;

    @NotNull
    private ServerSocket serverSocket;

    @NotNull
    private Function<Socket, Runnable> handler;

    public Server(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public void run() throws IOException {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.printf("can't construct server socket with port[%d] %n", port);
            System.out.println(e.getMessage());
            return;
        }

        // System.out.printf("... %s server started on port %s...\n", name, serverSocket.getLocalPort());
        pool = Executors.newCachedThreadPool();

        pool.execute(() -> {
            while (isRunning) {
                try {
                    var socket = serverSocket.accept();
                    pool.execute(handler.apply(socket));
                } catch (IOException ignored) {
                }
            }
        });

    }

    public synchronized void shutdown() throws IOException {
        isRunning = false;
        System.out.printf("...shutting down pool %s... %n", name);
        pool.shutdownNow();
        System.out.println("pool is shut");
        System.out.println("... closing server socket ...");
        serverSocket.close();
        System.out.println("socket closed!");
    }


    public void setHandler(Function<Socket, Runnable> handler) {
        this.handler = handler;
    }

    public synchronized int getPort() {
        if (!isRunning) {
            return 0;
        }
        return serverSocket.getLocalPort();
    }
}
