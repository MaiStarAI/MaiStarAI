import java.util.ArrayList;
import java.util.HashMap;

public class Geisha {
    GeishasName name;
    HashMap<Colors, Integer> abilities;
    int numberEffect;

    Geisha(GeishasName name, HashMap<Colors, Integer> abilities, int numberEffect) {
        this.name = name;
        this.abilities = abilities;
        this.numberEffect = numberEffect;
    }

    Geisha(Geisha anotherGeisha) {
        this.name = anotherGeisha.name;
        this.abilities = anotherGeisha.abilities;
        this.numberEffect = anotherGeisha.numberEffect;
    }

    public boolean isApplicableEffect(State state, Card first, Card second,
                                      boolean withEffect, Colors ability, boolean firstGeishaEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);

        switch (this.name) {
            case Natsumi:
                return state.parent.turnPlayerIndex == state.turnPlayerIndex
                        && state.appliedAction.firstCard.color == Colors.Blue &&
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

    public State applyGeisha(State state, Card first, Card second, boolean withEffect,
                             Colors ability, boolean firstGeishaEffect, int targetPlayer) {
        switch (this.name) {
            case Natsumi:
                return NatsumiEffect(state, first, withEffect);
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

    private State NatsumiEffect(State state, Card card, boolean withEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.geishaEffect -= 1;

        Action action = new Action(card, withEffect);
        State newState = action.applyAction(state);

        if (withEffect) {
            newState = action.applyEffect(newState, card, -1, withEffect);
        }

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;


        return newState;
    }

    private State SuzuneEffect(State state, Card card, boolean firstGeishaEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.geishaEffect -= 1;

        Action action = new Action(card);
        State newState = action.applyAction(state);

        if (!firstGeishaEffect) {
            newState.parent = state.parent;
            state.children.remove(newState);
            newState.turnPlayerIndex = state.turnPlayerIndex;
        }

        return newState;
    }

    private State MomijiEffect(State state, int targetPlayer, boolean withEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.geishaEffect -= 1;

        State newState = state.appliedAction.applyEffect(state, state.appliedAction.firstCard, targetPlayer, withEffect);

        newState.parent = state.parent;
        state.children.remove(newState);
        newState.turnPlayerIndex = state.turnPlayerIndex;

        return newState;
    }

    private State AkenohoshiEffect(State state, Colors ability, boolean firstGeishaEffect) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.geishaEffect -= 1;

        State newState = new State(state);
        turnPlayer = newState.players.get(newState.turnPlayerIndex);
        int oldValue = turnPlayer.geisha.abilities.get(ability);
        turnPlayer.geisha.abilities.put(ability, oldValue + 3);

        if (!firstGeishaEffect) {
            newState.parent = state.parent;
            state.children.remove(newState);
            newState.turnPlayerIndex = state.turnPlayerIndex;
        }

        return newState;
    }

    private State HarukazeEffect(State state, Card firstCard, Card secondCard) {
        Player turnPlayer = state.players.get(state.turnPlayerIndex);
        turnPlayer.geishaEffect -= 1;

        State newState = new State(state);
        turnPlayer = newState.players.get(newState.turnPlayerIndex);
        turnPlayer.hand.remove(firstCard);
        turnPlayer.hand.remove(secondCard);

        return newState;
    }

    public static void OboroEffect(State state, Player player) {
        if (state.parent == null) {
            player.hand.add(state.getRandomCard());
            player.hand.add(state.getRandomCard());
        }
    }

}
