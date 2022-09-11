package ru.itmo.sd.game.mercenary;


public class Properties {
    private int HP;

    public Properties(int HP) {
        this.HP = HP;
    }

    public int getHP() {
        return HP;
    }

    public void seHP(int newHP) {
        this.HP = newHP;
    }

}