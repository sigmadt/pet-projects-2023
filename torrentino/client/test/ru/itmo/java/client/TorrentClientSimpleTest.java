package ru.itmo.java.client;

import org.junit.jupiter.api.Test;
import ru.itmo.java.utils.FileConstant;
import ru.itmo.java.utils.Utils;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TorrentClientSimpleTest {
    @Test
    public void testCalculateNParts() {
        var sizes =
                Stream.of(100, 1000, 999, 1243, 77, 18, 4, 3001)
                        .map(s -> ((long) s) * FileConstant.MB)
                        .toList();

        var expected = Arrays.asList(10, 100, 100, 125, 8, 2, 1, 301);
        System.out.println("YOO " + 10 * (1 << 20));

        assertIterableEquals(expected, sizes.stream().map(x -> (int) Utils.calculateNParts(x)).toList());
    }

    @Test
    public void testCalculateBlockSize() {
        var sizes =
                Stream.of(100, 1000, 999, 1243, 77, 18, 4, 3001)
                        .map(s -> ((long) s) * FileConstant.MB)
                        .toList();
        var expected = Arrays.asList(10, 100, 100, 125, 8, 2, 1, 301);
        var expectedRemains =
                Arrays.asList(FileConstant.BLOCK, FileConstant.BLOCK, 9437184, 3145728, 7340032, 8388608, 4194304, FileConstant.MB);

        int pos = 0;
        for (var size : sizes) {
            int till = expected.get(pos) - 1;
            for (int partId = 0; partId < till; partId++) {
                assertEquals(FileConstant.BLOCK, Utils.getBlockSize(partId, size));
            }
            assertEquals(expectedRemains.get(pos), Utils.getBlockSize(till, size));
            pos++;
        }
    }

    // OLD
    @Test
    public void testCalculateNPartsOld() {
        var sizes =
                Stream.of(100, 1000, 999, 1243, 77, 18, 4, 3001)
                        .map(s -> ((long) s) * FileConstant.MB)
                        .toList();

        var expected = Arrays.asList(10, 100, 100, 125, 8, 2, 1, 301);

        assertIterableEquals(expected, sizes.stream().map(x -> (int) OldUtils.calculateNParts(x)).toList());
    }

    // FAILED :(
    @Test
    public void testCalculateBlockSizeOld() {
        var sizes =
                Stream.of(100, 1000, 999, 1243, 77, 18, 4, 3001)
                        .map(s -> ((long) s) * FileConstant.MB)
                        .toList();
        var expected = Arrays.asList(10, 100, 100, 125, 8, 2, 1, 301);
        var expectedRemains =
                Arrays.asList(FileConstant.BLOCK, FileConstant.BLOCK, 9437184, 3145728, 7340032, 8388608, 4194304, FileConstant.MB);

        int pos = 0;
        for (var size : sizes) {
            int till = expected.get(pos) - 1;
            for (int partId = 0; partId < till; partId++) {
                assertEquals(FileConstant.BLOCK, OldUtils.getBlockSize(partId, size));
            }
            assertEquals(expectedRemains.get(pos), OldUtils.getBlockSize(till, size));
            pos++;
        }
    }

    private static class OldUtils {
        public static long calculateNParts(long fileSize) {
            return (fileSize - 1 + FileConstant.BLOCK) / FileConstant.BLOCK;
        }

        public static int getBlockSize(int partId, long fileSize) {
            var nParts = calculateNParts(fileSize);
            int block =
                    partId == nParts - 1 ?
                            (int) (fileSize % FileConstant.BLOCK) :
                            FileConstant.BLOCK;

            return block;
        }
    }
}
