import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        int n = 5000; // Number of iterations
        Action action;
        ArrayList<Player> players = new ArrayList<>(); // TODO: fill the array
        ArrayList<Card> cards = new ArrayList<>(); // TODO: fill the array

        /* Initial state with some players and cards
           and without parent and appliedAction. */
        State initial_state = new State(players, cards, null, null);

        action = ISMCTS(initial_state, n);

        System.out.println(action); // TODO: override 'toString()' method

    }

    private static Action ISMCTS (State s, int n) {
        for (int i = 0; i < n; ++i) {
            /* s.fillDeterminization() */ // TODO: determinization filling in
            State selected = select(s);
            if (u(selected).size() != 0) {
                selected = expand(selected);
            }
            int reward = simulate(selected);
            backpropagate(selected, reward);
        }
        /* Choose the most visited node. */
        int max_visits = Integer.MIN_VALUE;
        Action a = s.children.get(0).appliedAction;
        for (int i = 0; i < s.children.size(); ++i) {
            if (s.children.get(i).visits > max_visits) {
                max_visits = s.children.get(i).visits;
                a = s.children.get(i).appliedAction;
            }
        }
        return a;
    }

    /** Bandit algorithm */
    private static double uct (State s) {
        double epsilon = 1e-6;
        return s.visits == 0 ? Double.MAX_VALUE :
                (s.victories / (s.visits + epsilon)) +
                        (Math.sqrt( (Math.log(s.availability)) / (s.visits + epsilon) ));
    }

    /** Selection stage */
    private static State select (State s0) {
        State selected = s0;
        double bestValue = Double.MIN_VALUE;
        while (u(selected).size() == 0 /* && !selected.isTerminal() */) { // TODO: implement 'isTerminal()' function
            for (State s : s0.children) {
                double value = uct(s);
                if (bestValue < value) {
                    bestValue = value;
                    selected = s;
                }
            }
        }
        return selected;
    }

    /** Expansion stage */
    private static State expand (State s) {
        Random rand = new Random();
        s.children.add( u(s).get( rand.nextInt( u(s).size() ) ) );
        return s;
    }

    /** Simulation stage */
    private static int simulate (State s) {
        return 0;
    }

    /** Backpropagation stage */
    private static void backpropagate (State s, int reward) {

    }

    /** Returns all possible actions of state 's'
     *  that are not in the tree yet. */
    private static ArrayList<State> u (State s) {
        ArrayList<State> allStates = c(s);
        ArrayList<State> statesToAdd = new ArrayList<>();
        for (State state : allStates) {
            if (!s.children.contains(state)) {
                statesToAdd.add(state);
            }
        }
        return statesToAdd;
    }

    /** Returns all possible actions of state 's'. */
    private static ArrayList<State> c (State s) {
        ArrayList<State> list = new ArrayList<>();

        // Guest actions without effects
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
            if (guest.isApplicableAction(s)) list.add(guest.applyAction(s));
        }

        // TODO: guest actions with effects

        // Advertiser actions
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            Action advertiser = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
            if (advertiser.isApplicableAction(s)) list.add(advertiser.applyAction(s));
        }

        // Exchange action
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            for (int j = 0; j < s.players.get(s.turnPlayerIndex).advertisers.size(); ++j) {
                Action exchange = new Action(ActionsNames.Exchange,
                        s.players.get(s.turnPlayerIndex).hand.get(i),
                        s.players.get(s.turnPlayerIndex).advertisers.get(j));
                if (exchange.isApplicableAction(s)) list.add(exchange.applyAction(s));
            }
        }

        // Introduce action
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            for (int j = i + 1; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                Action introduce = new Action(ActionsNames.Introduce,
                        s.players.get(s.turnPlayerIndex).hand.get(i),
                        s.players.get(s.turnPlayerIndex).hand.get(j));
                if (introduce.isApplicableAction(s)) list.add(introduce.applyAction(s));
            }
        }

        // Search action
        Action search = new Action();
        if (search.isApplicableAction(s)) list.add(search.applyAction(s));

        // TODO: geisha's effects

        return list;
    }

}
