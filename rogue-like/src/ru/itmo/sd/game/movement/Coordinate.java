package ru.itmo.sd.game.movement;


public class Coordinate {
    private int row;
    private int col;


    public Coordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Coordinate(Coordinate prev, Side side) {
        switch (side) {
            case DEFAULT -> {
                row = prev.getRow();
                col = prev.getCol();
            }
            case NORTH -> {
                row = prev.getRow() - 1;
                col = prev.getCol();
            }
            case SOUTH -> {
                row = prev.getRow() + 1;
                col = prev.getCol();
            }
            case EAST -> {
                row = prev.getRow();
                col = prev.getCol() - 1;
            }
            case WEST -> {
                row = prev.getRow();
                col = prev.getCol() + 1;
            }
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


}