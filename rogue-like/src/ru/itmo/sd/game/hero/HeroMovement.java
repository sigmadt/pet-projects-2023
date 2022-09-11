package ru.itmo.sd.game.hero;

import ru.itmo.sd.game.executor.Executor;
import ru.itmo.sd.game.map.Grid;
import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.Movement;
import ru.itmo.sd.game.movement.Side;

import java.util.List;

public class HeroMovement implements Movement {
    private Executor executor;

    public HeroMovement(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Side nextMove(Grid map, Coordinate coordinate) {
        return executor.updateHeroState();

    }

}
