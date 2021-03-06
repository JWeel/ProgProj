// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package foe;

import java.util.HashSet;

import move.Move;

// second boss, appears later as random foe
public class Dragon extends Foe {

    public Dragon(){
        this.maxHealth = 30;
        this.currentHealth = maxHealth;
        this.maxMagic = 14;
        this.currentMagic = maxMagic;
        this.strength = 8;
        this.willpower = 7;
        this.defense = 5;
        this.resistance = 5;
        this.speed = 6;
        this.name = "Dragon";
        this.money = 10;
        this.spells = new HashSet<>();
        spells.add(Move.FIREBALL);

        this.willDefend = false;
        this.color = "#AA6600";
        this.id = Foe.DRAGON;
    }
}
