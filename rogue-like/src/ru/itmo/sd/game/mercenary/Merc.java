package ru.itmo.sd.game.mercenary;


import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.MercMovement;
import ru.itmo.sd.game.utils.Utils;

public class Merc extends AbstractMerc {
    public Merc(Coordinate coordinate) {
        super(
                new MercMovement(),
                new Properties(20),
                coordinate);

    }


    @Override
    public String view() {
        return Utils.getViewUnitCodes().get("merc");
    }

}