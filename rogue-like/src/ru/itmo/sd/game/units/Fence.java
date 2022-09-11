package ru.itmo.sd.game.units;

import ru.itmo.sd.game.utils.Utils;

public class Fence implements Unit {
    @Override
    public String view() {
        return Utils.getViewUnitCodes().get("fence");
    }
}
