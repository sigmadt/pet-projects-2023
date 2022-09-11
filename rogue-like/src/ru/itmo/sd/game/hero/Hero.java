package ru.itmo.sd.game.hero;

import ru.itmo.sd.game.mercenary.AbstractMerc;
import ru.itmo.sd.game.mercenary.Properties;
import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.Movement;
import ru.itmo.sd.game.utils.Utils;

public class Hero extends AbstractMerc {
    public Hero(Movement movement, Coordinate coordinate) {
        super(
                movement,
                new Properties(100),
                coordinate);

    }

    @Override
    public String view() {
        return Utils.getViewUnitCodes().get("hero");
    }

}