package ru.itmo.sd.game.chest;

import ru.itmo.sd.game.units.Unit;
import ru.itmo.sd.game.utils.Utils;

public class MedKit implements Unit {
    public int insideHP = 20;


    @Override
    public String view() {
        return Utils.getViewUnitCodes().get("med");
    }
}
