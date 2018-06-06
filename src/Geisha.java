import java.util.ArrayList;
import java.util.HashMap;

public class Geisha {
    GeishasName name;
    HashMap<Colors, Integer> abilities;

    Geisha(GeishasName name, HashMap<Colors, Integer> abilities) {
        this.name = name;
        this.abilities = abilities;
    }

    Geisha(Geisha anotherGeisha) {
        this.name = anotherGeisha.name;
        this.abilities = anotherGeisha.abilities;
    }

    public void isApplicaableEffect(Action currentAction) {

    }

    public ArrayList<State> NatsumiEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            if (turnPlayer.hand.get(i).color == Colors.Blue) {

                //Apply effect one time without card effect
                Action action = new Action(turnPlayer.hand.get(i), true);
                State newState = action.applyAction(state);
                states.add(newState);

                //Apply effect one time with card effect
                ArrayList<State> effects = action.applyEffect(newState);
                for (int j = 0; j < effects.size(); j++) {
                    states.add(effects.get(j));
                }
                
            }
        }
        for (int i = 0; i < ; i++) {
            
        }
        return states;
    }

}
