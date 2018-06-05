import java.util.ArrayList;

public class Action {
    ActionsNames name;
    Card firstCard;
    Card secondCard;
    boolean usedEffect;
    boolean played;

    Action(Card firstCard, boolean usedEffect) {
        this.name = ActionsNames.Guest;
        this.firstCard = firstCard;
        this.secondCard = null;
        this.usedEffect = usedEffect;
        this.played = false;
    }

    Action(Card firstCard) {
        this.name = ActionsNames.Advertiser;
        this.firstCard = firstCard;
        this.secondCard = null;
        this.usedEffect = false;
        this.played = false;
    }

    Action(ActionsNames name, Card firstCard, Card secondCard) {
        this.name = name;
        this.firstCard = firstCard;
        this.secondCard = secondCard;
        this.usedEffect = false;
        this.played = false;
    }

    Action() {
        this.name = ActionsNames.Search;
        this.firstCard = null;
        this.secondCard = null;
        this.usedEffect = false;
        this.played = false;
    }

    public ArrayList<State> applyEffect(State currentState) {
        ArrayList<State> states = new ArrayList<>();
        if (this.name == ActionsNames.Guest && this.played && usedEffect) {
            State state = new State(currentState);
            states = firstCard.applyEffect(state);
        }
        return states;
    }

    public State applyAction(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);

        switch (this.name) {
            case Guest:
                this.applyGuest(turnPlayer);
            case Advertiser:
                this.applyAdvertiser(turnPlayer);
            case Exchange:
                this.applyExchange(turnPlayer);
            case Introduce:
                this.applyIntroduce(state, turnPlayer);
            case Search:
                this.applySearch(state, turnPlayer);
            default:
                System.out.println("Error in applyAction: there is no such name of action");

        }

        state.parent = currentState;
        state.children = null;
        state.turnPlayerIndex = state.getNextPlayer();
        state.appliedAction = this;
        state.drawDeck = state.cards.size();
        currentState.children.add(state);

        return state;
    }

    private void applyGuest(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.guests.add(firstCard);
        turnPlayer.score += firstCard.guestReward;
    }

    private void applyAdvertiser(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.advertisers.add(firstCard);

        int n = turnPlayer.geisha.abilities.get(Colors.Red);
        turnPlayer.geisha.abilities.put(Colors.Red, firstCard.advReward.get(Colors.Red) + n);
        n = turnPlayer.geisha.abilities.get(Colors.Blue);
        turnPlayer.geisha.abilities.put(Colors.Blue, firstCard.advReward.get(Colors.Blue) + n);
        n = turnPlayer.geisha.abilities.get(Colors.Green);
        turnPlayer.geisha.abilities.put(Colors.Green, firstCard.advReward.get(Colors.Green) + n);
    }

    private void applyExchange(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.advertisers.remove(secondCard);
        turnPlayer.hand.add(secondCard);
        turnPlayer.advertisers.add(firstCard);
    }

    private void applyIntroduce(State state, Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.hand.add(state.getRandomCard());
        if (turnPlayer.hand.size() > 1) {
            turnPlayer.hand.remove(secondCard);
            turnPlayer.hand.add(state.getRandomCard());
        }
    }

    private void applySearch(State state, Player turnPlayer) {
        turnPlayer.hand.add(state.getRandomCard());
    }

    public boolean isApplicableAction(State currentState) {
        Player turnPlayer = currentState.players.get(currentState.turnPlayerIndex);
        switch (this.name) {
            case Guest:
                if (firstCard.color != Colors.Black && firstCard.requirement <= turnPlayer.geisha.abilities.get(firstCard.color)) {
                    return true;
                } else {
                    if (firstCard.color == Colors.Black) {
                        for (int value : turnPlayer.geisha.abilities.values()) {
                            if (value >= firstCard.requirement) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            case Advertiser:
                return true;
            case Exchange:
                return !turnPlayer.advertisers.isEmpty();
            case Introduce:
                return true;
            case Search:
                return true;
            default:
                return false;
        }
    }

}
