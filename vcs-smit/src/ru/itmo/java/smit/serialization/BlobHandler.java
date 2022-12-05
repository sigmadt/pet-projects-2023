package ru.itmo.java.smit.serialization;

import org.jetbrains.annotations.NotNull;
import ru.itmo.java.smit.exception.SmitException;
import ru.itmo.java.smit.utils.Hasher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlobHandler {
    private BlobHandler() {}

    public static void write(Object object, @NotNull Path path) throws SmitException {
        //noinspection EmptyTryBlock
        try (PrintWriter ignored = new PrintWriter(path.toFile())) {
        } catch (FileNotFoundException e) {
            throw new SmitException(e);
        }

        try
        (
            FileOutputStream f = new FileOutputStream(path.toFile()); ObjectOutputStream o = new ObjectOutputStream(f)
        ) {
            o.writeObject(object);
        } catch (IOException e) {
            throw new SmitException(String.format("writing failed for given path: %s", path));
        }
    }


    @SuppressWarnings("unchecked")
    public static <S> S read(Path path) throws SmitException {
        try
        (
            FileInputStream f = new FileInputStream(path.toFile()); ObjectInputStream i = new ObjectInputStream(f)
        ) {
            var result = (S) i.readObject();
            return result;
        } catch (ClassNotFoundException | IOException e) {
            throw new SmitException(String.format("reading failed for given path: %s", path));
        }
    }


    public static @NotNull String computeHash(Path path) throws SmitException {
        try (InputStream i = Files.newInputStream(path))
        {
            return Hasher.computeHashOfInputStream(i);
        } catch (IOException e) {
            throw new SmitException(String.format("failed to compute hash of file for given path: %s", path));
        }
    }

}
