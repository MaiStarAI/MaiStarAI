import java.util.HashMap;

public class Card {
    CardsNames name;
    Colors color;
    int requirement;
    int guestReward;
    HashMap<Colors, Integer> advReward;

    Card(CardsNames cardName, Colors color, int requirement, int guestReward, HashMap<Colors,Integer> advReward){
        this.name = cardName;
        this.color = color;
        this.requirement = requirement;
        this.guestReward = guestReward;
        this.advReward = advReward;
    }

    public void applyEffect(State state, Action action){
        
    }

    public void MonkEffect(State state){
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.hand.clear();
    }

    public void DoctorEffect(State state, Action action){
        state.turnPlayerIndex = state.getNextPlayer();
    }

}
