package ru.itmo.java.smit.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Hasher {
    private static final int BYTE_MASK = 0xFF;
    private static final int BASE = 16;
    private static final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom rnd = new SecureRandom();
    private static final int LEN = 30;


    public static @NotNull String constructRandomString(){
        StringBuilder sb = new StringBuilder(LEN);
        for(int i = 0; i < LEN; i++)
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        return sb.toString();
    }

    public static @NotNull String computeHashOfInputStream(InputStream inputStream) throws IOException {
        byte[] b = mdFiveAsBytes(inputStream);
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result
                    .append(
                            Integer.toString((value & BYTE_MASK) + BYTE_MASK + 1, BASE)
                            .substring(1)
                    );
        }
        return result.toString();
    }


    private static byte[] mdFiveAsBytes(InputStream fis) throws IOException {
        MessageDigest complete;
        try {
            complete = computeMdFiveMessageDigest(fis);
        } catch (NoSuchAlgorithmException ex) {
            throw new UnsupportedOperationException(ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                return new byte[0];
            }
        }
        return complete.digest();
    }

    private static MessageDigest computeMdFiveMessageDigest(@NotNull InputStream fis) throws IOException,
            NoSuchAlgorithmException {
        int numRead;
        int chunk = 1024;
        MessageDigest complete = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[chunk];
        do {
            numRead = fis.read(buffer, 0, chunk);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        return complete;
    }
}
