package ru.itmo.sd.game.ui;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameKeyListener implements KeyListener {
    private static final Object Window = new Object();
    private boolean restart;
    private char last;


    public GameKeyListener() {
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (Window) {
            var currKey = e.getKeyChar();

            switch (currKey) {
                case 'r' -> {
                    restart = true;
                    Window.notify();
                }
                case 'w', 'a', 's', 'd' -> {
                    last = currKey;
                    Window.notify();
                }
            }

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public boolean isRestart() {
        return restart;
    }

    public char getLastPressedKey() {
        synchronized (Window) {
            last = 0;
            while (last == 0) {
                try {
                    Window.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            return last;
        }
    }

    public void waitForStart() {
        restart = false;
        synchronized (Window) {
            while (!restart) {
                try {
                    Window.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}