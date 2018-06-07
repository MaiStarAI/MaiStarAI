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

    public boolean isApplicableEffect(State state, Card card, int targetPlayer) {
        if ((this.name == CardsNames.Yakuza || this.name == CardsNames.Emissary) &&
                !state.players.get(targetPlayer).advertisers.isEmpty()) {
            return true;
        } else {
            if ((this.name == CardsNames.Thief || this.name == CardsNames.Samurai) &&
                    !state.players.get(targetPlayer).guests.isEmpty()) {
                return true;
            } else {
                if (this.name == CardsNames.Courtier && card.color == this.color &&
                        card.requirement <= state.players.get(state.turnPlayerIndex).geisha.abilities.get(card.color)) {
                    return true;
                } else {
                    return this.name == CardsNames.District_Kanryou || this.name == CardsNames.Ronin
                            || this.name == CardsNames.Daimyo || this.name == CardsNames.Shogun
                            || this.name == CardsNames.Doctor || this.name == CardsNames.Monk
                            || this.name == CardsNames.Okaasan || this.name == CardsNames.Sumo_Wrestler
                            || this.name == CardsNames.Merchant || this.name == CardsNames.Scholar;
                }
            }
        }

    }

    public State applyEffect(State state, Card card, int targetPlayer, boolean withEffect) {
        switch (this.name) {
            case Monk:
                return this.MonkEffect(state);
            case Doctor:
                return this.DoctorEffect(state);
            case Shogun:
                return this.ShogunEffect(state);
            case Okaasan:
                return this.OkaasanEffect(state, card);
            case Sumo_Wrestler:
                return this.SumoWrestlerEffect(state, targetPlayer, card);
            case Emissary:
                return this.EmissaryEffect(state, targetPlayer);
            case Samurai:
                return this.SamuraiEffect(state, targetPlayer);
            case Daimyo:
                return this.DaimyoEffect(state);
            case Ronin:
                return this.RoninEffect(state);
            case District_Kanryou:
                return this.DistrictKanryouEffect(state);
            case Thief:
                return this.ThiefEffect(state, targetPlayer);
            case Yakuza:
                return this.YakuzaEffect(state, targetPlayer);
            case Courtier:
                return this.CourtierEffect(state, card, targetPlayer, withEffect);
            case Merchant:
                return this.MerchantEffect(state, targetPlayer);
            case Scholar:
                return this.ScholarEffect(state, targetPlayer);
            default:
                System.out.println("Error: no such name of card with effect");
                return null;
        }
    }

    private State MonkEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.hand.clear();
        return state;
    }

    private State DoctorEffect(State currentState) {
        State state = new State(currentState);
        state.turnPlayerIndex = state.getNextPlayer();
        return state;
    }

    private State ShogunEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            turnPlayer.guests.add(turnPlayer.hand.get(i));
            turnPlayer.score += turnPlayer.hand.get(i).guestReward;
        }
        turnPlayer.hand.clear();
        return state;
    }

    private State OkaasanEffect(State state, Card card) {
        Action action = new Action(card);
        State newState = action.applyAction(state);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    private State SumoWrestlerEffect(State state, int targetPlayer, Card card) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.remove(card);

        return newState;
    }

    private State EmissaryEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);
        Action action = new Action(removed);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;


        return newState;
    }

    private State SamuraiEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);
        Action action = new Action(removed, false);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    private State DaimyoEffect(State currentState) {
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

    private State RoninEffect(State currentState) {
        currentState.players.get(currentState.turnPlayerIndex).specialEffects.add(CardsNames.Ronin);
        return currentState;
    }

    private State DistrictKanryouEffect(State currentState) {
        currentState.players.get(currentState.turnPlayerIndex).specialEffects.add(CardsNames.District_Kanryou);
        return currentState;
    }

    private State ThiefEffect(State state, int targetPlayer) {
        State newState = new State(state);
        state.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);

        return newState;
    }

    private State YakuzaEffect(State state, int targetPlayer) {
        State newState = new State(state);
        state.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);

        return newState;
    }

    private State CourtierEffect(State state, Card card, int targetPlayer, boolean withEffect) {
        State newState = null;
        Action action = new Action(card);
        if (card.color == this.color && action.isApplicableAction(state)) {
            newState = action.applyAction(state);
            if (withEffect) {
                newState = action.applyEffect(newState, card, targetPlayer, true);
            }
        }
        return newState;
    }

    private State MerchantEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

    private State ScholarEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

}
