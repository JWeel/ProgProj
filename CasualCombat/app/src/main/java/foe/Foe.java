package foe;

import admin.cozycombat.Combatant;

public abstract class Foe extends Combatant {

    protected String color; // TODO color of button

    public static final int GOBLIN = 0;

    protected int id;

    public static Foe findFoeByID(int id){
        switch(id){
            case GOBLIN: return new Goblin();
            default: return null;
        }
    }
    public String getColor(){ return this.color; }

    public int getId(){ return this.id; }

    @Override
    public boolean isFoe(){ return true; }
}
