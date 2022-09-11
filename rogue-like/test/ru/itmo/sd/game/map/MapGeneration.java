package ru.itmo.sd.game.map;

import org.junit.jupiter.api.Test;
import ru.itmo.sd.game.executor.ExecutorInterface;
import ru.itmo.sd.game.helper.ExecutorHelper;

import java.io.IOException;
import java.util.InputMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapGeneration {
    ExecutorInterface executor = new ExecutorHelper();


    @Test()
    void matNotLoaded() {
        // wrong path
        Exception thrownExc =
                assertThrows(IOException.class,
                () -> executor.loadMap("test/ru/itmo/sd/game/res/nofile.txt"));

        var expectedMessage = "test/ru/itmo/sd/game/res/nofile.txt (No such file or directory)";

        assertEquals(expectedMessage, thrownExc.getMessage());

    }

    @Test()
    void contentOfMapFileIsInvalid() {
        // no ints in header
        assertThrows(IOException.class,
                () -> executor.loadMap("test/ru/itmo/sd/game/res/invalid_map.txt"));

    }

    @Test()
    void contentOfMapFileIsInvalidMismatch() {
        // wrong symbols for map
        assertThrows(InputMismatchException.class,
                () -> executor.loadMap("test/ru/itmo/sd/game/res/mismatch_map.txt"));

    }


}
