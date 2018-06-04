import java.util.HashMap;

public class Geisha {
    GeishasName name;
    HashMap<Colors, Integer> abilities;
    int numberEffect;

    Geisha(GeishasName name, HashMap<Colors, Integer> abilities, int numberEffect){
        this.name = name;
        this.abilities = abilities;
        this.numberEffect = numberEffect;
    }

    public void isApplicaableEffect(Action currentAction){

    }

    public void NatsumiEffect(State state, Card blueCard){
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.guests.add(blueCard);
        turnPlayer.hand.remove(blueCard);
        turnPlayer.score = turnPlayer.score + blueCard.guestReward;
        //Apply card effect or not
    }

}
