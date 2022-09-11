package ru.itmo.sd.game.executor;

import ru.itmo.sd.game.hero.Hero;
import ru.itmo.sd.game.map.Grid;
import ru.itmo.sd.game.mercenary.AbstractMerc;
import ru.itmo.sd.game.ui.GameWindow;

import java.util.List;
import java.util.stream.Collectors;

public class Runner {
    private GameWindow gameWindow;
    private Grid map;
    private Hero hero;

    private List<AbstractMerc> mercs;

    private boolean gameWon = false;

    public Runner(GameWindow gameWindow, Grid map, Hero hero, List<AbstractMerc> mercs) {
        this.gameWindow = gameWindow;
        this.map = map;
        this.hero = hero;
        this.mercs = mercs;
    }

    public void run() {
        while (true) {
            gameWindow.run(map, hero, mercs);
            hero.move(map);

            mercs = mercs
                    .stream()
                    .filter(x -> !x.isKilled())
                    .collect(Collectors.toList());

            for (var m : mercs) {
                m.move(map);
            }

            if (mercs.isEmpty()) {
                gameWon = true;
                break;
            }
            if (hero.isKilled()) {
                break;
            }

        }
    }

    public boolean isGameWon() {
        return gameWon;
    }
}
