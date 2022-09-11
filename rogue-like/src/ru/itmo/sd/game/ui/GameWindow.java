package ru.itmo.sd.game.ui;


import ru.itmo.sd.game.hero.Hero;
import ru.itmo.sd.game.map.Grid;
import ru.itmo.sd.game.mercenary.AbstractMerc;
import ru.itmo.sd.game.movement.Coordinate;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GameWindow {
    private static final int HEIGHT = 900;
    private static final int WIDTH = 900;

    private JFrame box;
    private JPanel panelBox;
    private JTextArea textBox;

    private Clip clip = null;



    private Color textColor = new Color(255, 255, 255);
    private Color backgroundColor = new Color(135, 100, 173);


    private Font customFont;
    private final String fontPath = "src/ru/itmo/sd/game/ui/fonts/VT323-Regular.ttf";
    private final String musicPath = "src/ru/itmo/sd/game/res/purple_haze.wav";

    private GameKeyListener gameKeyListener = new GameKeyListener();


    public GameWindow() {
        box = new JFrame();

        textBox = new JTextArea(40, 40);
        textBox.setSize(WIDTH, HEIGHT);
        textBox.setEditable(false);
        textBox.setOpaque(false);
        setFontProps();
        textBox.setFont(customFont);
        textBox.setForeground(textColor);

        panelBox = new JPanel();

        panelBox.setBackground(backgroundColor);
        panelBox.add(textBox, BorderLayout.CENTER);
        box.add(panelBox, BorderLayout.CENTER);


        textBox.addKeyListener(gameKeyListener);

        box.setVisible(true);
        box.setSize(WIDTH, HEIGHT);
        box.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            play(musicPath, 10);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }


    private void setFontProps() {
        try {
            customFont = Font
                    .createFont(
                            Font.TRUETYPE_FONT,
                            new File(fontPath))
                    .deriveFont(20f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    // paint methods
    public void paintVictoryMessage() {
        var message = "Victory! Is this game was too easy for you? Press <R> to fight again";
        textBox.setText(message);
    }

    public void paintDefeatMessage() {
        var message = "Defeat! Don't be sad, just press <R> to fight again";
        textBox.setText(message);
    }

    public String getGameTitle() {
        return "PURPLE HAZE\n\n";
    }

    public String getGameDescription() {
        return
                "Basic movement: use <w>, <a>, <s>, <d> keys.\n\n" +
                        "You are legendary Jimmy Hendrix surrounded with purple haze.\n" +
                        "Try to kill all haters <H> and doubters <D>.\n" +
                        "If you want to heal yourself just take a pill <+>.\n\n";
    }


    // main run method
    public void run(Grid map, Hero hero, java.util.List<AbstractMerc> mercs) {
        var content = new StringBuilder();
        content.append(getGameTitle());
        content.append(getGameDescription());
        var h = map.getHEIGHT();
        var w = map.getWIDTH();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                content.append(map.getUnit(new Coordinate(i, j)).view());
            }
            content.append("\n");
        }

        content.append("\n");

        var props = hero.getProps();

        content.append("HP").append(props.getHP()).append("\n");
        content.append("Enemies left: ").append(mercs.size()).append("\n");
        textBox.setText(content.toString());

    }

    // key listener
    public GameKeyListener getGameKeyListener() {
        return gameKeyListener;
    }

    // music
    public void play(String path, int numberOfLoops) throws UnsupportedAudioFileException,
            LineUnavailableException, IOException {
        File file = new File(path);
        if (file.exists()) {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(file));
            clip.start();
            clip.loop(numberOfLoops);
        }
    }
}




