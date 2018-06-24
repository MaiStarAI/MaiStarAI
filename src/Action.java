import java.util.ArrayList;

/**
 * Class action contain information about current action
 */

public class Action {
    ActionsNames name;
    Card firstCard;
    Card secondCard;

    boolean usedEffect;
    int targetPlayer; //Targeted player by effect
    Card effectCard; //Card which used for effect

    boolean usedGeisha; //True if used effect of geisha
    Card geishaCard1; //Card which used for geisha effect
    Card geishaCard2; //Card which used for geisha effect
    int geishaTargetPlayer; //Targeted player by geisha effect
    Colors geishaAbility; //Ability which increased by geisha effect


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
        targetPlayer = -1;
        effectCard = null;
        usedGeisha = false;
        geishaCard1 = null;
        geishaCard2 = null;
        geishaTargetPlayer = -1;
        geishaAbility = null;
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
        targetPlayer = -1;
        effectCard = null;
        usedGeisha = false;
        geishaCard1 = null;
        geishaCard2 = null;
        geishaTargetPlayer = -1;
        geishaAbility = null;
    }

    /**
     * Constructor to create action Exchange or Introduce
     * @param name: name of action (Exchange or Introduce)
     * @param firstCard: in case of Exchange: card from player's hand which we want to add as a new advertiser
     *                   in case of Introduce: card from player's hand which we want to remove
     * @param secondCard: in case of Exchange: card from player's advertisers which we want to exchange
     *                    in case of Introduce: card from player's hand which we want to remove
     */

    Action(ActionsNames name, Card firstCard, Card secondCard) {
        this.name = name;
        this.firstCard = firstCard;
        this.secondCard = secondCard;
        this.usedEffect = false;
        targetPlayer = -1;
        effectCard = null;
        usedGeisha = false;
        geishaCard1 = null;
        geishaCard2 = null;
        geishaTargetPlayer = -1;
        geishaAbility = null;
    }

    /**
     * Constructor to create action Search
     */

    Action() {
        this.name = ActionsNames.Search;
        this.firstCard = null;
        this.secondCard = null;
        this.usedEffect = false;
        targetPlayer = -1;
        effectCard = null;
        usedGeisha = false;
        geishaCard1 = null;
        geishaCard2 = null;
        geishaTargetPlayer = -1;
        geishaAbility = null;
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
        this.targetPlayer = targetPlayer;
        this.effectCard = card;

        if (this.name == ActionsNames.Guest && !usedEffect
                && firstCard.isApplicableEffect(state, card, targetPlayer)) {
            state = firstCard.applyEffect(state, card, targetPlayer, withEffect);
            usedEffect = true;
            this.targetPlayer = targetPlayer;
            effectCard = card;
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
        Player turnPlayer = new Player(state.players.get(state.turnPlayerIndex));

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
        if (currentState.children == null) {
            currentState.children = new ArrayList<>();
            currentState.children.add(state);
        } else {
            currentState.children.add(state);
        }

        return state;
    }

    /**
     * Apply action guest
     * @param turnPlayer: player who make this action
     */

    private void applyGuest(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.guests.add(firstCard);
        turnPlayer.score += firstCard.guestReward;
        turnPlayer.cardsNumber --;
    }

    /**
     * Apply action advertiser
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who make this action
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

        if (state.drawDeck > 0) turnPlayer.hand.add(state.getRandomCard());

        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            if (turnPlayer.hand.get(i).name == CardsNames.District_Kanryou) {
                turnPlayer.specialEffects.add(CardsNames.District_Kanryou);
            }
        }
    }

    /**
     * Apply action exchange
     * @param turnPlayer: player who make this action
     */

    private void applyExchange(Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        turnPlayer.advertisers.remove(secondCard);
        turnPlayer.hand.add(secondCard);
        turnPlayer.advertisers.add(firstCard);

        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            if (turnPlayer.hand.get(i).name == CardsNames.District_Kanryou) {
                turnPlayer.specialEffects.add(CardsNames.District_Kanryou);
            }
        }
    }

    /**
     * Apply action introduce
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who make this action
     */

    private void applyIntroduce(State state, Player turnPlayer) {
        turnPlayer.hand.remove(firstCard);
        state.discardedCards.add(firstCard);
        turnPlayer.hand.add(state.getRandomCard());
        if (turnPlayer.hand.size() > 1) {
            turnPlayer.hand.remove(secondCard);
            state.discardedCards.add(secondCard);
            turnPlayer.hand.add(state.getRandomCard());
        }

        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            if (turnPlayer.hand.get(i).name == CardsNames.District_Kanryou) {
                turnPlayer.specialEffects.add(CardsNames.District_Kanryou);
            }
        }
    }

    /**
     * Apply action search
     * @param state: game's state for which we apply action
     * @param turnPlayer: player who make this action
     */

    private void applySearch(State state, Player turnPlayer) {
        turnPlayer.hand.add(state.getRandomCard());
        turnPlayer.cardsNumber ++;

        for (int i = 0; i < turnPlayer.hand.size(); i++) {
            if (turnPlayer.hand.get(i).name == CardsNames.District_Kanryou) {
                turnPlayer.specialEffects.add(CardsNames.District_Kanryou);
            }
        }
    }

    /**
     * Method to check applicability current action to state (according to rules of the game)
     * @param currentState: game's state for which we apply action
     * @return true if we can apply such action
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
                return currentState.drawDeck > 1;
            case Search:
                return currentState.drawDeck > 0;
            default:
                return false;
        }
    }

    /**
     * Method to get main information about action
     * @return string with main information
     */

    public String toString() {
        String info = "";
        info += this.name.toString() + "\n";
        if(this.name != ActionsNames.Search) {
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
        }

        info += "Used effect: " + this.usedEffect + "\n";
        if(targetPlayer != -1) {
            info += "Target player: " + this.targetPlayer + "\n";
        }

        if(effectCard != null){
            info += "Card used for effect: " + this.effectCard.name + "\n";
        }

        info += "Used geisha: " + this.usedGeisha + "\n";

        if(this.geishaCard1 != null){
            info += "Card used for geisha effect: " + this.geishaCard1.name;
        }

        if(geishaCard2 != null){
            info += ", " + this.geishaCard2.name + "\n";
        }

        if(geishaTargetPlayer != -1){
            info += "\n" + "Player targeted by geisha effect: " + this.geishaTargetPlayer;
        }

        if(geishaAbility != null){
            info += "\n" + "Ability increased by geisha effect: " + this.geishaAbility;
        }

        return info;
    }

}
