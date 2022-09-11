package ru.itmo.sd.game.mercenary;

import ru.itmo.sd.game.chest.MedKit;
import ru.itmo.sd.game.hero.Hero;
import ru.itmo.sd.game.map.Grid;
import ru.itmo.sd.game.movement.Coordinate;
import ru.itmo.sd.game.movement.Movement;
import ru.itmo.sd.game.movement.Side;
import ru.itmo.sd.game.units.Default;
import ru.itmo.sd.game.units.Fence;
import ru.itmo.sd.game.units.Unit;

public abstract class AbstractMerc implements Unit {
    private Movement movement;
    private Properties props;
    private Coordinate coordinate;


    public Properties getProps() {
        return props;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }


    public AbstractMerc(Movement movement, Properties props, Coordinate coordinate) {
        this.movement = movement;
        this.props = props;
        this.coordinate = coordinate;
    }

    public void move(Grid map) {
        var side = movement.nextMove(map, coordinate);

        if (side == Side.DEFAULT) {
            return;
        }

        var pos = new Coordinate(coordinate, side);
        if (pos.getCol() < 0 || pos.getRow() < 0
                || pos.getRow() >= map.getHEIGHT() || pos.getCol() >= map.getWIDTH()) {
            return;
        }

        var currUnit = map.getUnit(pos);

        if (currUnit instanceof Default) {
            moveToCoordinate(pos, map);
            return;
        }
        if (currUnit instanceof Fence) {
            return;
        }
        if (currUnit instanceof Hero hero) {
            hero.hurt(10);
            return;
        }

        if (currUnit instanceof Merc merc) {
            merc.hurt(5);

            if (merc.isKilled()) {
                map.setUnit(pos, new Default());
            }
            return;
        }
        if (currUnit instanceof DefendMerc defendMerc) {
            defendMerc.hurt(5);

            if (defendMerc.isKilled()) {
                map.setUnit(pos, new Default());
            }
            return;
        }
        if (currUnit instanceof MedKit medKit) {
            heal(medKit.insideHP);
            map.setUnit(pos, new Default());

        }


    }

    public void moveToCoordinate(Coordinate toCoordinate, Grid map) {
        map.setUnit(toCoordinate, this);
        map.setUnit(coordinate, new Default());

        coordinate = toCoordinate;
    }

    public boolean isKilled() {
        return props.getHP() <= 0;
    }

    public void hurt(int minusHP) {
        props.seHP(props.getHP() - minusHP);
    }

    public void heal(int plusHP) {
        props.seHP(props.getHP() + plusHP);
    }


    @Override
    public String view() {
        return "";
    }
}
