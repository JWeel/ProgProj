// UNIVERSITEIT VAN AMSTERDAM - MINOR PROGRAMMEREN - PROGRAMMEERPROJECT
// CasualCombat - created by Joseph Weel, 10321624, josefweel@gmail.com

package foe;

import java.util.HashSet;

import move.Move;

// higher tier spell-using foe
public class DarkMage extends Foe {

    public DarkMage(){
        this.maxHealth = 6;
        this.currentHealth = maxHealth;
        this.maxMagic = 30;
        this.currentMagic = maxMagic;
        this.strength = 2;
        this.willpower = 5;
        this.defense = 2;
        this.resistance = 4;
        this.speed = 5;
        this.name = "Dark Mage";
        this.money = 1;
        this.spells = new HashSet<>();
        spells.add(Move.FIREBALL);
        spells.add(Move.TORNADO);
        spells.add(Move.SHOCK);
        spells.add(Move.HEAL);

        this.willDefend = false;
        this.color = "#332255";
        this.id = Foe.DARK_MAGE;
    }
}
