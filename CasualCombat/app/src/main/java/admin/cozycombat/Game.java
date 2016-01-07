package admin.cozycombat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Game {

    ArrayList<Combatant> combatants;

    private PlayerCharacter playerCharacter;
    private ArrayList<Foe> foes;
    private LinkedList<String> log;

    private int playerTarget;
    private Move currentMove;

    Game(PlayerCharacter pc){

        playerCharacter = pc;

        // TODO base foes on pc level
        foes = new ArrayList<>();
        foes.add(new Foe(Foe.GOBLIN));
        foes.add(new Foe(Foe.GOBLIN));

        log = new LinkedList<>();
    }

    //
    void advance(){

        // TODO make way of checking if can advance prettier
        if (playerCharacter.getMove() == null) return;

        // get moves for all foes
        for (int i = 0; i < foes.size(); i++){
            if (foes.get(i).isDead()) continue;
            // TODO get move from ai
            foes.get(i).setMove(new Move(Move.BASIC_ATTACK));
            foes.get(i).getMove().setTarget(-1);
        }
        // sort combatants by speed
        ArrayList<Combatant> combatants = new ArrayList<>();
        combatants.add(playerCharacter);
        for (Foe foe : foes) combatants.add(foe);
        System.out.println(combatants);
        Collections.sort(combatants);
        System.out.println(combatants);

        // performs moves
        for (int i = 0; i < combatants.size(); i++){

            // TODO if already dead cancel
            if (combatants.get(i).isDead()) continue;

            Move move = combatants.get(i).getMove();
            if (combatants.get(i).isFoe()) {
                if (move.getDamage() > 0){
                    playerCharacter.lowerHealth(move.getDamage());
                    log.add("" + combatants.get(i).getName() + " hits " + playerCharacter.getName() + " for " + move.getDamage() + "!");
                }
            }
            if (!combatants.get(i).isFoe()) {
                if (move.getDamage() > 0){
                    // TODO if range == 0 then self
                    // TODO if range > 1 then multiple targets
                    Combatant target = foes.get(move.getTarget());
                    target.lowerHealth(move.getDamage());
                    log.add("" + combatants.get(i).getName() + " hits " + target.getName() + " for " + move.getDamage() + "!");

                    if (target.isDead()){
                        log.add("" + target.getName() + " is defeated!");
                    }
                }
            }

            combatants.get(i).setMove(null);
        }
    }

    void handleMove(){

    }

    //
    String pop() {
        System.err.println(log);
        if (!log.isEmpty()) {
            return log.removeFirst();
        } else return "";
    }

    //
    void setPlayerTarget(int target){
        playerTarget = target;
        log.add("target " + playerTarget);
    }

    //
    PlayerCharacter getPlayerCharacter(){
        return playerCharacter;
    }

    ArrayList<Foe> getFoes(){
        return foes;
    }
}
