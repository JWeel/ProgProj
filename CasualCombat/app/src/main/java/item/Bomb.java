package item;

import move.Move;

public class Bomb extends UsableItem {

    public Bomb(){
        this.id = Item.BOMB;
        this.itemMove = Move.findMoveByID(Move.ITEM_BOMB);
        this.name = "Bomb";
        this.info = "A small bomb that explodes on impact";
    }

}
