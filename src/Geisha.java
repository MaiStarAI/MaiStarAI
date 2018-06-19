import java.util.HashMap;

/**
 * Class card contain information about current geisha
 */

public class Geisha {
    GeishasName name;
    HashMap<Colors, Integer> abilities;
    int numberEffect;

    /**
     * Main constructor to create geisha
     * @param name: name of a geisha
     * @param abilities: numbers which determine geisha's abilities to apply cars as a guest
     * @param numberEffect: how much we can apply geisha's effect
     */

    Geisha(GeishasName name, HashMap<Colors, Integer> abilities, int numberEffect) {
        this.name = name;
        this.abilities = abilities;
        this.numberEffect = numberEffect;
    }

    /**
     * Constructor to create copies of geisha
     * @param anotherGeisha: geisha which new object need to create
     */

    Geisha(Geisha anotherGeisha) {
        this.name = anotherGeisha.name;
        this.abilities = anotherGeisha.abilities;
        this.numberEffect = anotherGeisha.numberEffect;
    }

    /**
     * Method to check applicability of geisha's effect
     * @param state: game's state for which we apply effect
     * @param first: card which we apply to get game's state for which we apply effect
     * @param firstGeishaEffect: true if we apply effect of geisha first
     * @return true if we can apply geisha's effect
     */

    public boolean isApplicableEffect(State state, Card first, boolean firstGeishaEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);

        switch (this.name) {
            case Natsumi:
                return state.appliedAction.firstCard.color == Colors.Blue &&
                        state.appliedAction.name == ActionsNames.Guest && turnPlayer.geishaEffect > 0
                        && !firstGeishaEffect;
            case Suzune:
                return turnPlayer.geishaEffect > 0;
            case Momiji:
                return first.color == Colors.Red && turnPlayer.geishaEffect > 0 && !firstGeishaEffect;
            case Akenohoshi:
                return turnPlayer.geishaEffect > 0;
            case Harukaze:
                return state.appliedAction.name == ActionsNames.Advertiser
                        && turnPlayer.geishaEffect > 0 && !firstGeishaEffect;
            default:
                System.out.println("Error: there is no ability for such geisha or no such geisha");
                return false;
        }
    }

    /**
     * Method to apply geisha's effect
     * @param state: game's state for which we apply effect
     * @param first: card which we use for this effect
     * @param second: card which we use for this effect
     * @param withEffect: true if we use effect of card which we use for this effect
     * @param ability: ability (color) which we want to increase (Akenohoshi's effect)
     * @param firstGeishaEffect: true if we apply effect of geisha first
     * @param targetPlayer: player on which we apply effect
     * @return new state after applying effect
     */

    public State applyGeisha(State state, Card first, Card second, boolean withEffect,
                             Colors ability, boolean firstGeishaEffect, int targetPlayer) {
        state.appliedAction.usedGeisha = true;
        state.appliedAction.geishaCard1 = first;
        state.appliedAction.geishaCard2 = second;
        state.appliedAction.geishaAbility = ability;
        state.appliedAction.geishaTargetPlayer = targetPlayer;

        switch (this.name) {
            case Natsumi:
                return NatsumiEffect(state, first, withEffect, targetPlayer);
            case Suzune:
                return SuzuneEffect(state, first, firstGeishaEffect);
            case Momiji:
                return MomijiEffect(state, targetPlayer, withEffect);
            case Akenohoshi:
                return AkenohoshiEffect(state, ability, firstGeishaEffect);
            case Harukaze:
                return HarukazeEffect(state, first, second);
            default:
                System.out.println("Error: there is no ability for such geisha or no such geisha");
                return null;
        }
    }

    /**
     * Apply effect of geisha Natsumi
     * @param state: game's state for which we apply effect
     * @param card: card which we use for this effect (play as a blue guest)
     * @param withEffect: true if we use effect of card which we use for this effect (play as a blue guest)
     * @return new state after applying effect
     */

    private State NatsumiEffect(State state, Card card, boolean withEffect, int targetPlayer) {
        Action action = new Action(card, withEffect);
        if (action.isApplicableAction(state)) {
            State newState = action.applyAction(state);

            if (withEffect) {
                newState = action.applyEffect(newState, card, targetPlayer, true);
            }
            if (newState != null) {
                newState.parent = state.parent;
                state.children.remove(newState);
                newState.turnPlayerIndex = state.turnPlayerIndex;
                newState.players.get(newState.turnPlayerIndex).geishaEffect -= 1;
            }

            return newState;
        }
        return null;
    }

    /**
     * Apply effect of geisha Suzune
     * @param state: game's state for which we apply effect
     * @param card: card which we use for this effect (play as an advertiser)
     * @param firstGeishaEffect: true if we play geisha's effect first
     * @return new state after applying effect
     */

    private State SuzuneEffect(State state, Card card, boolean firstGeishaEffect) {
        Action action = new Action(card);
        State newState = action.applyAction(state);

        if (!firstGeishaEffect) {
            newState.parent = state.parent;
            state.children.remove(newState);
            newState.turnPlayerIndex = state.turnPlayerIndex;
        }

        newState.players.get(state.turnPlayerIndex).geishaEffect -= 1;

        return newState;
    }

    /**
     * Apply effect of geisha Momiji
     * @param state: game's state for which we apply effect
     * @param targetPlayer: player on which we apply effect
     * @param withEffect: true if we apply effect of this card
     * @return new state after applying effect
     */

    private State MomijiEffect(State state, int targetPlayer, boolean withEffect) {
        State newState = state.appliedAction.applyEffect(state, state.appliedAction.firstCard, targetPlayer, withEffect);
        if (newState != null) {
            newState.parent = state.parent;
            //state.children.remove(newState);
            newState.turnPlayerIndex = state.turnPlayerIndex;

            newState.players.get(state.turnPlayerIndex).geishaEffect -= 1;
        }
        return newState;
    }

    /**
     * Apply effect of geisha Akenohoshi
     * @param state: game's state for which we apply effect
     * @param ability: ability (color) which we want to increase
     * @param firstGeishaEffect: true if we apply effect of this card
     * @return new state after applying effect
     */

    private State AkenohoshiEffect(State state, Colors ability, boolean firstGeishaEffect) {
        State newState = new State(state);
        Player turnPlayer = newState.players.get(newState.turnPlayerIndex);
        int oldValue = turnPlayer.geisha.abilities.get(ability);
        turnPlayer.geisha.abilities.put(ability, oldValue + 3);

        if (!firstGeishaEffect) {
            newState.parent = state.parent;
            state.children.remove(newState);
            newState.turnPlayerIndex = state.turnPlayerIndex;
        }

        newState.players.get(state.turnPlayerIndex).geishaEffect -= 1;

        return newState;
    }

    /**
     * Apply effect of geisha Harukaze
     * @param state: game's state for which we apply effect
     * @param firstCard: card which we want to remove from hand
     * @param secondCard: card which we want to remove from hand
     * @return new state after applying effect
     */

    private State HarukazeEffect(State state, Card firstCard, Card secondCard) {
        State newState = new State(state);
        Player turnPlayer = newState.players.get(newState.turnPlayerIndex);
        turnPlayer.hand.remove(firstCard);
        turnPlayer.hand.remove(secondCard);
        firstCard.known = true;
        secondCard.known = true;
        newState.cards.add(firstCard);
        newState.cards.add(secondCard);
        newState.drawDeck += 2;

        newState.players.get(state.turnPlayerIndex).geishaEffect -= 1;

        return newState;
    }

    /**
     * Apply effect of geisha Oboro
     * @param state: game's state for which we apply effect
     * @param player: player on which we apply effect (can be only a player who owns this geisha)
     */

    public static void OboroEffect(State state, Player player) {
        if (state.parent == null) {
            player.hand.add(state.getRandomCard());
            player.hand.add(state.getRandomCard());
            player.cardsNumber += 2;
        }
    }

}