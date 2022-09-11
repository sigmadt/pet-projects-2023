package ru.itmo.sd.game.mercenary;

import org.junit.jupiter.api.Test;
import ru.itmo.sd.game.movement.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

public class MercTest {
    Coordinate coordinate = new Coordinate(0, 0);

    @Test
    void simpleMercTest() {
        var merc = new Merc(coordinate);

        assertEquals(20, merc.getProps().getHP());
        assertEquals(0, merc.getCoordinate().getCol());
        assertEquals(0, merc.getCoordinate().getRow());

        assertEquals("h", merc.view());
    }

    @Test
    void simpleDefendMercTest() {
        var merc = new DefendMerc(coordinate);

        assertEquals(30, merc.getProps().getHP());
        assertEquals(0, merc.getCoordinate().getCol());
        assertEquals(0, merc.getCoordinate().getRow());

        assertEquals("d", merc.view());
    }


    @Test
    void killMercTest() {
        var merc = new Merc(coordinate);

        assertFalse(merc.isKilled());

        merc.hurt(100);
        assertTrue(merc.isKilled());
    }
}
