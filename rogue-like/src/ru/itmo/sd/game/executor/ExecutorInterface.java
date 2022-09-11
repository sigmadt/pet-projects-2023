package ru.itmo.sd.game.executor;

import ru.itmo.sd.game.movement.Side;

import java.io.IOException;

public interface ExecutorInterface {
    void run();

    void construct();

    Side updateHeroState();

    void loadMap(String fileName) throws IOException;
}
