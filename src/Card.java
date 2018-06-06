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

    public State applyEffect(State state) {
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

    public State OkaasanEffect(State state, Card card) {
        Action action = new Action(card);
        State newState = action.applyAction(state);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    public State SumoWrestlerEffect(State state, int targetPlayer, Card card) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.remove(card);

        return newState;
    }

    public State EmissaryEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);
        Action action = new Action(removed);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;


        return newState;
    }

    public State SamuraiEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);
        Action action = new Action(removed, false);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
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

    public State ThiefEffect(State state, int targetPlayer) {
        State newState = new State(state);
        state.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);

        return newState;
    }

    public State YakuzaEffect(State state, int targetPlayer) {
        State newState = new State(state);
        state.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);

        return newState;
    }

    public State CourtierEffect(State state, Card card, boolean withEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        State newState = null;
        Action action = new Action(card);
        if (card.color == this.color && action.isApplicableAction(state)) {
            newState = action.applyAction(state);
            if (withEffect) {
                newState = action.applyEffect(newState);
            }
        }
        return newState;
    }

    public State MerchantEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

    public State ScholarEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

}
