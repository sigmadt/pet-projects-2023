package ru.itmo.java.smit.utils;


import org.jetbrains.annotations.NotNull;

public enum SmitColor {
    BLUE("blue", "\u001B[34m"),
    GREEN("green", "\u001B[32m"),
    PURPLE("purple", "\u001B[35m"),
    RED("red", "\u001B[31m"),
    RESET("reset", "\u001B[0m"),
    YELLOW("yellow", "\u001B[33m");

    private final String name;
    private final String code;

    SmitColor(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static @NotNull String makeBlue(String string) {
        return BLUE.getCode() + string + RESET.getCode();
    }

    public static @NotNull String makeGreen(String string) {
        return GREEN.getCode() + string + RESET.getCode();
    }

    public static @NotNull String makePurple(String string) {
        return PURPLE.getCode() + string + RESET.getCode();
    }

    public static @NotNull String makeRed(String string) {
        return RED.getCode() + string + RESET.getCode();
    }

    public static @NotNull String makeYellow(String string) {
        return YELLOW.getCode() + string + RESET.getCode();
    }
}
