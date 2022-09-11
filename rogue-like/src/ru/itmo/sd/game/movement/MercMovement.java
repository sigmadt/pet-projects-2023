package ru.itmo.sd.game.movement;

import ru.itmo.sd.game.map.Grid;

import java.util.List;
import java.util.Random;

public class MercMovement implements Movement {
    private List<Side> sideList = List.of(Side.values());

    @Override
    public Side nextMove(Grid map, Coordinate coordinate) {
        var r = new Random();
        var randomIndex = r.nextInt(sideList.size() - 1);

        return sideList.get(randomIndex);

    }
}
