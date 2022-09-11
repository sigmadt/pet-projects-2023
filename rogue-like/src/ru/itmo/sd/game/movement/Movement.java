package ru.itmo.sd.game.movement;

import ru.itmo.sd.game.map.Grid;

public interface Movement {

    Side nextMove(Grid map, Coordinate coordinate);

}