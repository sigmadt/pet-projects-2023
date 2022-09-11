package ru.itmo.sd.game.executor;


import ru.itmo.sd.game.chest.MedKit;
import ru.itmo.sd.game.hero.Hero;
import ru.itmo.sd.game.hero.HeroMovement;
import ru.itmo.sd.game.map.Grid;
import ru.itmo.sd.game.mercenary.AbstractMerc;
import ru.itmo.sd.game.mercenary.DefendMerc;
import ru.itmo.sd.game.mercenary.Merc;
import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.Side;
import ru.itmo.sd.game.ui.GameWindow;
import ru.itmo.sd.game.units.Default;
import ru.itmo.sd.game.units.Fence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Executor implements ExecutorInterface {
    private GameWindow gameWindow;
    private Grid map;
    private Hero hero;
    private List<AbstractMerc> mercs;


    private int nMercs;
    private int nMedKits;


    private String mapPath = "src/ru/itmo/sd/game/res/large_map.txt";


    public Executor() {
        System.out.println("------Starting the game------");
        run();
    }

    @Override
    public void run() {
        gameWindow = new GameWindow();
        while (true) {
            construct();

            var runner = new Runner(gameWindow, map, hero, mercs);
            runner.run();

            if (runner.isGameWon()) {
                gameWindow.paintVictoryMessage();
            } else {
                gameWindow.paintDefeatMessage();
            }
            gameWindow.getGameKeyListener().waitForStart();

        }


    }

    @Override
    public void construct() {
        mercs = new ArrayList<>();

        try {
            System.out.println("------Initializing  map------");
            loadMap(mapPath);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        var heroMovement = new HeroMovement(this);
        hero = new Hero(heroMovement, map.getRandomDefault());

        map.setUnit(hero.getCoordinate(), hero);

        for (int i = 0; i < nMercs; i++) {
            var merc = new Merc(map.getRandomDefault());
            mercs.add(merc);
            map.setUnit(merc.getCoordinate(), merc);
        }

        for (int j = 0; j < nMedKits; j++) {
            map.setUnit(map.getRandomDefault(), new MedKit());
        }
    }

    @Override
    public Side updateHeroState() {
        while (true) {
            var curr = gameWindow.getGameKeyListener().getLastPressedKey();
            switch (curr) {
                case 'w':
                    return Side.NORTH;
                case 'a':
                    return Side.EAST;
                case 's':
                    return Side.SOUTH;
                case 'd':
                    return Side.WEST;
                default:
                    gameWindow.run(map, hero, mercs);

            }
        }
    }

    @Override
    public void loadMap(String fileName) throws IOException {
        Scanner input = new Scanner(new File(fileName));
        var width = input.nextInt();
        var height = input.nextInt();
        nMercs = input.nextInt();
        nMedKits = input.nextInt();
        input.nextLine();

        map = new Grid(width, height);
        for (int i = 0; i < height; i++) {
            var line = input.nextLine();

            if (line.length() != width) {
                throw new IOException("This file can not be processed");
            }

            for (int j = 0; j < width; j++) {
                var curr = line.charAt(j);
                var coordinate = new Coordinate(i, j);
                switch (curr) {
                    case '*' -> map.setUnit(coordinate, new Fence());
                    case '+' -> map.setUnit(coordinate,
                            new MedKit());
                    case 'M' -> {
                        var merc = new Merc(coordinate);
                        mercs.add(merc);
                        map.setUnit(coordinate, merc);
                    }
                    case 'D' -> {
                        var defendMerc = new DefendMerc(coordinate);
                        mercs.add(defendMerc);
                        map.setUnit(coordinate, defendMerc);
                    }
                    default -> map.setUnit(coordinate, new Default());
                }
            }
        }
    }


}