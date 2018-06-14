/**
 * Class action contain information about current action
 */

public class Action {
    ActionsNames name;
    Card firstCard;
    Card secondCard;
    boolean played; //true if action is played and we can apply effect in case of Guest action

    boolean usedEffect;

    /**
     * Constructor to create action Guest
     * @param firstCard: card which we want to play as a guest
     * @param usedEffect: true if we want and can use effect of this card
     */

    Action(Card firstCard, boolean usedEffect) {
        this.name = ActionsNames.Guest;
        this.firstCard = firstCard;
        this.secondCard = null;
        this.usedEffect = usedEffect;
        this.played = false;
    }

    /**
     * Constructor to create action Advertiser
     * @param firstCard: card which we want to play as an advertiser
     */

    Action(Card firstCard) {
        this.name = ActionsNames.Advertiser;
        this.firstCard = firstCard;
        this.secondCard = null;
        this.usedEffect = false;
        this.played = false;
    }

    /**
     * Constructor to create action Exchange or Introduce
     * @param name: name of action (Exchange or Introduce)
     * @param firstCard: in case of Exchange: card from player's advertisers which we want to exchange
     *                   in case of Introduce: card from player's hand which we want to remove
     * @param secondCard: in case of Exchange: card from player's hand which we want to add as a new advertiser
     *                    in case of Introduce: card from player's hand which we want to remove
     */

    Action(ActionsNames name, Card firstCard, Card secondCard) {
        this.name = name;
        this.firstCard = firstCard;
        this.secondCard = secondCard;
        this.usedEffect = false;
        this.played = false;
    }

    /**
     * Constructor to create action Search
     */

    Action() {
        this.name = ActionsNames.Search;
        this.firstCard = null;
        this.secondCard = null;
        this.usedEffect = false;
        this.played = false;
    }

    /**
     * Apply effect of card in case of Guest card
     * @param currentState: game's state for which we apply effect
     * @param card: card which we use for effect
     * @param targetPlayer: player on which we apply effect
     * @param withEffect: apply effect of card which we use for effect
     * @return new state after applying effect
     */

    public State applyEffect(State currentState, Card card, int targetPlayer, boolean withEffect) {
        State state = new State(currentState);
        if (this.name == ActionsNames.Guest && this.played && usedEffect
                && firstCard.isApplicableEffect(state, card, targetPlayer)) {
            state = firstCard.applyEffect(state, card, targetPlayer, withEffect);
            return state;
        } else {
            return null;
        }
    }

    /**
     * Apply action
     * @param currentState: game's state for which we apply action
     * @return new state after applying action
     */

    public State applyAction(State currentState) {
        State state = new State(currentState);
        Player turnPlayer = state.players.get(state.turnPlayerIndex);

        switch (this.name) {
            case Guest:
                this.applyGuest(turnPlayer);
                break;
            case Advertiser:
                this.applyAdvertiser(state, turnPlayer);
                break;
            case Exchange:
                this.applyExchange(turnPlayer);
                break;
            case Introduce:
                this.applyIntroduce(state, turnPlayer);
                break;
            case Search:
                this.applySearch(state, turnPlayer);
                break;
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

    /**
     * Apply action Guest
     * @param turnPlayer: player who play this card as a guest
     */

    private void applyGuest(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.guests.add(firstCard);
        turnPlayer.score += firstCard.guestReward;
        turnPlayer.cardsNumber --;
    }

    /**
     * Apply action Advertiser
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who play this card as an advertiser
     */

    private void applyAdvertiser(State state, Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.advertisers.add(firstCard);

        int n = turnPlayer.geisha.abilities.get(Colors.Red);
        turnPlayer.geisha.abilities.put(Colors.Red, firstCard.advReward.get(Colors.Red) + n);
        n = turnPlayer.geisha.abilities.get(Colors.Blue);
        turnPlayer.geisha.abilities.put(Colors.Blue, firstCard.advReward.get(Colors.Blue) + n);
        n = turnPlayer.geisha.abilities.get(Colors.Green);
        turnPlayer.geisha.abilities.put(Colors.Green, firstCard.advReward.get(Colors.Green) + n);

        turnPlayer.hand.add(state.getRandomCard());
    }

    /**
     * Apply action Exchange
     * @param turnPlayer: player who play this action
     */

    private void applyExchange(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.advertisers.remove(secondCard);
        turnPlayer.hand.add(secondCard);
        turnPlayer.advertisers.add(firstCard);
    }

    /**
     * Apply action Introduce
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who play this action
     */

    private void applyIntroduce(State state, Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.hand.add(state.getRandomCard());
        if (turnPlayer.hand.size() > 1) {
            turnPlayer.hand.remove(secondCard);
            turnPlayer.hand.add(state.getRandomCard());
        }
    }

    /**
     * Apply action Search
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who play this action
     */

    private void applySearch(State state, Player turnPlayer) {
        turnPlayer.hand.add(state.getRandomCard());
        turnPlayer.cardsNumber ++;
    }

    /**
     * Method to check applicability of action
     * @param currentState: game's state for which we apply action
     * @return true if we can apply this action
     */

    public boolean isApplicableAction(State currentState) {
        Player turnPlayer = currentState.players.get(currentState.turnPlayerIndex);
        switch (this.name) {
            case Guest:
                if (firstCard.color != Colors.Black && firstCard.requirement <= turnPlayer.geisha.abilities.get(firstCard.color)) {
                    return true;
                } else {
                    if (firstCard.color == Colors.Black) {
                        if (firstCard.name != CardsNames.District_Kanryou) {
                            for (int value : turnPlayer.geisha.abilities.values()) {
                                if (value >= firstCard.requirement) {
                                    return true;
                                }
                            }
                        }
                        return false;
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

    /**
     * Get information about action
     * @return string with all information about current action
     */

    public String toString() {
        String info = "";
        info += this.name.toString() + "\n";
        info += "Name of card: " + firstCard.name + "\n" + "Color: " + firstCard.color + "\n" +
                "Requirement: " + firstCard.requirement + "\n" + "Guest reward: " + firstCard.guestReward +
                "\n" + "Advertiser reward: \n" + "Red: " + firstCard.advReward.get(Colors.Red) + "\n" + "Blue: " +
                firstCard.advReward.get(Colors.Blue) + "\n" + "Green: " + firstCard.advReward.get(Colors.Green) + "\n";
        if (this.name == ActionsNames.Introduce || this.name == ActionsNames.Exchange) {
            info += "Name of card: " + secondCard.name + "\n" + "Color: " + secondCard.color + "\n" +
                    "Requirement: " + secondCard.requirement + "\n" + "Guest reward: " + secondCard.guestReward +
                    "\n" + "Advertiser reward: \n" + "Red: " + secondCard.advReward.get(Colors.Red) + "\n" + "Blue: " +
                    secondCard.advReward.get(Colors.Blue) + "\n" + "Green: " + secondCard.advReward.get(Colors.Green) + "\n";
        }

        info += "Played: " + this.played + "\n" + "Used effect: " + this.usedEffect + "\n";

        return info;
    }

}
