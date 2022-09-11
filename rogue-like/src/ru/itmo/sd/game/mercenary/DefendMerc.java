package ru.itmo.sd.game.mercenary;


import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.DefendMercMovement;
import ru.itmo.sd.game.utils.Utils;

public class DefendMerc extends AbstractMerc {
    public DefendMerc(Coordinate coordinate) {
        super(
                new DefendMercMovement(),
                new Properties(30),
                coordinate);

    }


    @Override
    public String view() {
        return Utils.getViewUnitCodes().get("def");
    }

}