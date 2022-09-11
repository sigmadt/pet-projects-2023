package ru.itmo.sd.game.movement;

import ru.itmo.sd.game.hero.Hero;
import ru.itmo.sd.game.map.Grid;

import java.util.List;

public class DefendMercMovement implements Movement {
    private List<Side> sideList = List.of(Side.values());

    @Override
    public Side nextMove(Grid map, Coordinate coordinate) {

        for (var currSide : sideList) {
            if (isHeroNearby(map, coordinate, currSide)) {
                return currSide;
            }
        }

        return Side.DEFAULT;

    }

    private boolean isHeroNearby(Grid map, Coordinate coordinate, Side side) {
        var currPos = new Coordinate(coordinate, side);
        return map.getUnit(currPos) instanceof Hero;
    }

}
