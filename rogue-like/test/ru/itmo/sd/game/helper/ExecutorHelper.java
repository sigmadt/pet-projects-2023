package ru.itmo.sd.game.helper;

import ru.itmo.sd.game.chest.MedKit;
import ru.itmo.sd.game.executor.ExecutorInterface;
import ru.itmo.sd.game.hero.Hero;
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
import java.util.List;
import java.util.Scanner;

public class ExecutorHelper implements ExecutorInterface {
    private Grid map;
    private Hero hero;
    private List<AbstractMerc> mercs;


    private int nMercs;
    private int nMedKits;

    @Override
    public void run() {

    }

    @Override
    public void construct() {

    }

    @Override
    public Side updateHeroState() {
        return null;
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
