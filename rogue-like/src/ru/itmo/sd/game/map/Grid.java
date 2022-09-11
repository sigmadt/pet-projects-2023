package ru.itmo.sd.game.map;


import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.units.Default;
import ru.itmo.sd.game.units.Unit;

import java.util.Random;

public class Grid {
    private int WIDTH;
    private int HEIGHT;
    private Unit[][] storage;


    public Grid(int w, int h) {
        WIDTH = w;
        HEIGHT = h;

        storage = new Unit[HEIGHT][WIDTH];

        for (int x = 0; x < HEIGHT; x++) {
            for (int y = 0; y < WIDTH; y++) {
                storage[x][y] = new Default();
            }
        }
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public Unit getUnit(Coordinate c) {
        return storage[c.getRow()][c.getCol()];
    }

    public void setUnit(Coordinate c, Unit unit) {
        storage[c.getRow()][c.getCol()] = unit;
    }

    public Coordinate getRandomDefault() {
        var r = new Random();

        int x; int y;
        do {
            x = r.nextInt(HEIGHT);
            y = r.nextInt(WIDTH);
        } while (!(storage[x][y] instanceof Default));

        return new Coordinate(x, y);

    }
}