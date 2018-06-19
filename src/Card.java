import java.util.HashMap;

/**
 * Class card contain information about current card
 */

public class Card {
    CardsNames name;
    Colors color;
    int requirement;
    int guestReward;
    HashMap<Colors, Integer> advReward;
    boolean known;

    /**
     * Main constructor for card
     * @param cardName: name of a card
     * @param color: color of a card
     * @param requirement: number which determine requirement which need to play this card
     * @param guestReward: number which added to the player's score when he plays it as a guest
     * @param advReward: array with numbers which are added  to geisha's abilities when this card played as an advertiser
     */

    Card(CardsNames cardName, Colors color, int requirement, int guestReward, HashMap<Colors, Integer> advReward) {
        this.name = cardName;
        this.color = color;
        this.requirement = requirement;
        this.guestReward = guestReward;
        this.advReward = advReward;
        this.known = false;
    }

    /**
     * Constructor to crate copies of cards
     * @param anotherCard: card which new object need to create
     */

    Card(Card anotherCard) {
        this.name = anotherCard.name;
        this.color = anotherCard.color;
        this.requirement = anotherCard.requirement;
        this.guestReward = anotherCard.guestReward;
        this.advReward = new HashMap<>(anotherCard.advReward);
        this.known = anotherCard.known;
    }

    /**
     * Method to check applicability of this card's effect
     * @param state: game's state for which we apply effect
     * @param card: card which we use for effect
     * @param targetPlayer: player on which we apply effect
     * @return true if we can apply such effect
     */

    public boolean isApplicableEffect(State state, Card card, int targetPlayer) {
        switch (name) {
            case Ronin: { return true; }
            case Courtier: { return card.color == this.color &&
                    card.requirement <= state.players.get(state.turnPlayerIndex).geisha.abilities.get(card.color); }
            case Doctor: { return true; }
            case Emissary: { return !state.players.get(targetPlayer).advertisers.isEmpty(); }
            case Merchant: { return state.drawDeck > 1; }
            case Yakuza: { return !state.players.get(targetPlayer).advertisers.isEmpty(); }
            case Okaasan: { return true; }
            case Scholar: { return state.drawDeck > 0; }
            case Sumo_Wrestler: { return true; }
            case Thief: { return !state.players.get(targetPlayer).guests.isEmpty(); }
            case Actor: { return true; }
            case Daimyo: { return true; }
            case Samurai: { return !state.players.get(targetPlayer).guests.isEmpty(); }
            case Monk: { return true; }
            case Shogun: { return true; }
            default: { return false; }
        }
    }

    /**
     * Method to apply effect
     * @param state: game's state for which we apply effect
     * @param card: card which we use for effect
     * @param targetPlayer: player on which we apply effect
     * @param withEffect: apply effect of card which we use for effect
     * @return new state after applying effect
     */

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
                //System.out.println("Error: no such name of card with effect");
                return null;
        }
    }

    /**
     * Apply effect of card Monk
     * @param currentState: game's state for which we apply effect
     * @return new state after applying effect
     */

    private State MonkEffect(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.hand.clear();
        return state;
    }

    /**
     * Apply effect of card Doctor
     * @param currentState: game's state for which we apply effect
     * @return new state after applying effect
     */

    private State DoctorEffect(State currentState) {
        State state = new State(currentState);
        state.turnPlayerIndex = state.getNextPlayer();
        return state;
    }

    /**
     * Apply effect of card Shogun
     * @param currentState: game's state for which we apply effect
     * @return new state after applying effect
     */


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

    /**
     * Apply effect of card Okaasan
     * @param state: game's state for which we apply effect
     * @param card: card which we use for this effect (as an advertiser)
     * @return new state after applying effect
     */

    private State OkaasanEffect(State state, Card card) {
        Action action = new Action(card);
        State newState = action.applyAction(state);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    /**
     * Apply effect of card Sumo-Wrestler
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @param card: card which we remove
     * @return new state after applying effect
     */

    private State SumoWrestlerEffect(State state, int targetPlayer, Card card) {
        State newState = new State(state);
        for (int i = 0; i < newState.players.get(targetPlayer).hand.size(); i++) {
            newState.players.get(targetPlayer).hand.get(i).known = true;
        }
        newState.players.get(targetPlayer).hand.remove(card);

        return newState;
    }

    /**
     * Apply effect of card Emissary
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State EmissaryEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);

        int red = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Red);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Red, red - removed.advReward.get(Colors.Red));
        int blue = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Blue);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Blue, blue - removed.advReward.get(Colors.Blue));
        int green = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Green);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Green, green - removed.advReward.get(Colors.Green));

        Action action = new Action(removed);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;


        return newState;
    }

    /**
     * Apply effect of card Samurai
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State SamuraiEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = newState.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);
        newState.players.get(targetPlayer).score -= removed.guestReward;

        Action action = new Action(removed, false);
        newState = action.applyAction(newState);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    /**
     * Apply effect of card Daimyo
     * @param currentState: game's state for which we apply effect
     * @return new state after applying effect
     */

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

    /**
     * Apply effect of card Ronin
     * @param currentState: game's state for which we apply effect
     * @return new state after applying effect
     */

    private State RoninEffect(State currentState) {
        currentState.players.get(currentState.turnPlayerIndex).specialEffects.add(CardsNames.Ronin);
        return currentState;
    }

    /**
     * Apply effect of card Thief
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State ThiefEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = state.players.get(targetPlayer).guests.remove(newState.players.get(targetPlayer).guests.size() - 1);
        newState.players.get(targetPlayer).score -= removed.guestReward;

        return newState;
    }

    /**
     * Apply effect of card Yakuza
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State YakuzaEffect(State state, int targetPlayer) {
        State newState = new State(state);
        Card removed = state.players.get(targetPlayer).advertisers.remove(newState.players.get(targetPlayer).advertisers.size() - 1);

        int red = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Red);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Red, red - removed.advReward.get(Colors.Red));
        int blue = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Blue);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Blue, blue - removed.advReward.get(Colors.Blue));
        int green = newState.players.get(targetPlayer).geisha.abilities.get(Colors.Green);
        newState.players.get(targetPlayer).geisha.abilities.put(Colors.Green, green - removed.advReward.get(Colors.Green));

        return newState;
    }

    /**
     * Apply effect of card Courtier
     * @param state: game's state for which we apply effect
     * @param card: card which we use for this effect (as a new guest)
     * @param targetPlayer: player on which we apply effect of card which we use for this effect (as a new guest)
     * @param withEffect: apply effect of card which we use for effect (as a new guest)
     * @return new state after applying effect
     */

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

    /**
     * Apply effect of card Merchant
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State MerchantEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

    /**
     * Apply effect of card Scholar
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply this effect
     * @return new state after applying effect
     */

    private State ScholarEffect(State state, int targetPlayer) {
        State newState = new State(state);
        newState.players.get(targetPlayer).hand.add(newState.getRandomCard());

        return newState;
    }

}
