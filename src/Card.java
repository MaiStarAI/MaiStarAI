import java.util.ArrayList;
import java.util.HashMap;

public class Card {
    CardsNames name;
    Colors color;
    int requirement;
    int guestReward;
    HashMap<Colors, Integer> advReward;

    Card(CardsNames cardName, Colors color, int requirement, int guestReward, HashMap<Colors, Integer> advReward) {
        this.name = cardName;
        this.color = color;
        this.requirement = requirement;
        this.guestReward = guestReward;
        this.advReward = advReward;
    }

    Card(Card anotherCard) {
        this.name = anotherCard.name;
        this.color = anotherCard.color;
        this.requirement = anotherCard.requirement;
        this.guestReward = anotherCard.guestReward;
        this.advReward = anotherCard.advReward;
    }

    public ArrayList<State> applyEffect(State state) {
        return null;
    }

    public State MonkEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.hand.clear();
        return state;
    }

    public State DoctorEffect(State currentState) {
        State state = new State(currentState);
        state.turnPlayerIndex = state.getNextPlayer();
        return state;
    }

    public State ShogunEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            turnPlayer.guests.add(turnPlayer.hand.get(i));
            turnPlayer.score += turnPlayer.hand.get(i).guestReward;
        }
        turnPlayer.hand.clear();
        return state;
    }

    public ArrayList<State> OkaasanEffect(State state) {
        ArrayList<State> states = new ArrayList();
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            Action action = new Action(turnPlayer.hand.get(i));
            State newState = action.applyAction(state);
            states.add(newState);
        }
        return states;
    }

    public ArrayList<State> SumoWrestlerEffect(State state) {
        ArrayList<State> states = new ArrayList();
        for (int i = 0; i < state.players.size(); i++) {
            for (int j = 0; j < state.players.get(i).hand.size(); j++) {
                State newState = new State(state);
                newState.players.get(i).hand.remove(j);
                states.add(newState);
            }
        }
        return states;
    }

    public ArrayList<State> EmissaryEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            if (!state.players.get(i).advertisers.isEmpty()) {
                State newState = new State(state);
                Card removed = newState.players.get(i).advertisers.remove(newState.players.get(i).advertisers.size() - 1);
                Action action = new Action(removed);
                newState = action.applyAction(newState);
                states.add(newState);
            }
        }
        return states;
    }

    public ArrayList<State> SamuraiEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            if (!state.players.get(i).guests.isEmpty()) {
                State newState = new State(state);
                Card removed = newState.players.get(i).guests.remove(newState.players.get(i).guests.size() - 1);
                Action action = new Action(removed, false);
                newState = action.applyAction(newState);
                states.add(newState);
            }
        }
        return states;
    }

    public State DaimyoEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.advertisers.size(); i++) {
            if (turnPlayer.advertisers.get(i).color == this.color) {
                Card removed = turnPlayer.advertisers.remove(i);
                turnPlayer.guests.add(removed);
                turnPlayer.score += removed.guestReward;
            }
        }
        return state;
    }

    public State RoninEffect(State currentState) {
        currentState.players.get(currentState.turnPlayerIndex).specialEffects.add(CardsNames.Ronin);
        return currentState;
    }

    public State DistrictKanryouEffect(State currentState) {
        currentState.players.get(currentState.turnPlayerIndex).specialEffects.add(CardsNames.District_Kanryou);
        return currentState;
    }

    public ArrayList<State> ThiefEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            if (!state.players.get(i).guests.isEmpty()) {
                State newState = new State(state);
                state.players.get(i).guests.remove(newState.players.get(i).guests.size() - 1);
                states.add(newState);
            }
        }
        return states;
    }

    public ArrayList<State> YakuzaEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            if (!state.players.get(i).advertisers.isEmpty()) {
                State newState = new State(state);
                state.players.get(i).advertisers.remove(newState.players.get(i).advertisers.size() - 1);
            }
        }
        return states;
    }

    public ArrayList<State> CourtierEffect(State state) {
        ArrayList<State> states = new ArrayList<>();
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            Action action = new Action(turnPlayer.hand.get(i));
            if (turnPlayer.hand.get(i).color == this.color && action.isApplicableAction(state)) {
                State newState = action.applyAction(state);
                states.add(newState);
                ArrayList<State> effects = action.applyEffect(newState);
                for (int j = 0; j < effects.size(); j++) {
                    states.add(effects.get(j));
                }
            }
        }
        return states;
    }

    public ArrayList<State> MerchantEffect(State state){
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            State newState = new State(state);
            newState.players.get(i).hand.add(newState.getRandomCard());
            newState.players.get(i).hand.add(newState.getRandomCard());
            states.add(newState);
        }
        return states;
    }

    public ArrayList<State> ScholarEffect(State state){
        ArrayList<State> states = new ArrayList<>();
        for (int i = 0; i < state.players.size(); i++) {
            State newState = new State(state);
            newState.players.get(i).hand.add(newState.getRandomCard());
            states.add(newState);
        }
        return states;
    }

}
