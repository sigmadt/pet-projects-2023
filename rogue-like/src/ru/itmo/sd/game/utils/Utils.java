package ru.itmo.sd.game.utils;


import java.util.Map;

public class Utils {
    private Utils() {
    }


    public static Map<String, String> getViewUnitCodes() {
        Map<String, String> codes =
                Map.of(
                        "default", "_",
                        "fence", "#",
                        "med", "+",
                        "hero", "J",
                        "merc", "h",
                        "def", "d"

                );

        return codes;
    }

}