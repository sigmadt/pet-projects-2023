package ru.itmo.java.serialization;

import org.jetbrains.annotations.NotNull;
import ru.itmo.java.exception.TorrentSerializationException;

import java.io.*;
import java.nio.file.Path;

public class FileHandler {
    private FileHandler() {
    }

    public static void write(Object object, @NotNull Path path) throws TorrentSerializationException {
        //noinspection EmptyTryBlock
        try (PrintWriter ignored = new PrintWriter(path.toFile())) {
        } catch (FileNotFoundException e) {
            throw new TorrentSerializationException(e);
        }

        try
                (
                        FileOutputStream f = new FileOutputStream(path.toFile()); ObjectOutputStream o = new ObjectOutputStream(f)
                ) {
            o.writeObject(object);
        } catch (IOException e) {
            throw new TorrentSerializationException(String.format("writing failed for given path: %s", path));
        }
    }


    @SuppressWarnings("unchecked")
    public static <S> S read(Path path) throws TorrentSerializationException {
        try
                (
                        FileInputStream f = new FileInputStream(path.toFile()); ObjectInputStream i = new ObjectInputStream(f)
                ) {
            return (S) i.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new TorrentSerializationException(String.format("reading failed for given path: %s", path));
        }
    }

    public static void writeInt(Integer num, @NotNull Path path) throws TorrentSerializationException {
        //noinspection EmptyTryBlock
        try (PrintWriter ignored = new PrintWriter(path.toFile())) {
        } catch (FileNotFoundException e) {
            throw new TorrentSerializationException(e);
        }

        try
                (
                        FileOutputStream f = new FileOutputStream(path.toFile()); ObjectOutputStream o = new ObjectOutputStream(f)
                ) {
            o.writeInt(num);
        } catch (IOException e) {
            throw new TorrentSerializationException(String.format("writing failed for given path: %s", path));
        }
    }

    public static int readInt(Path path) throws TorrentSerializationException {
        try (FileInputStream f = new FileInputStream(path.toFile()); ObjectInputStream i = new ObjectInputStream(f)) {
            return i.readInt();
        } catch (IOException e) {
            throw new TorrentSerializationException(String.format("reading failed for given path: %s", path));
        }
    }
}
