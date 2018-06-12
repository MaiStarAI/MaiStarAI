import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Main {

    public static void main(String[] args) {

        int n = 5000; // Number of iterations
        Action action;

        // Abilities
        HashMap<Colors, Integer> a_1 = new HashMap<>();
        a_1.put(Colors.Red, 5);
        a_1.put(Colors.Blue, 5);
        a_1.put(Colors.Green, 5);

        HashMap<Colors, Integer> a_2 = new HashMap<>();
        a_2.put(Colors.Red, 1);
        a_2.put(Colors.Blue, 1);
        a_2.put(Colors.Green, 1);

        HashMap<Colors, Integer> a_3 = new HashMap<>();
        a_2.put(Colors.Red, 1);
        a_2.put(Colors.Blue, 5);
        a_2.put(Colors.Green, 3);

        // Geisha
        Geisha g_1 = new Geisha(GeishasName.Oboro, a_1, 0);
        Geisha g_2 = new Geisha(GeishasName.Momiji, a_2, 0);
        Geisha g_3 = new Geisha(GeishasName.Harukaze, a_3, 1);


        ArrayList<Card> cards = new ArrayList<>(); // TODO: fill the array with cards (somehow)
        ArrayList<Player> players = new ArrayList<>();

        Player AI_ISMCTS = new Player("ISMCTS", cards, g_1); players.add(AI_ISMCTS);
        Player AI_Random = new Player("RANDOM", cards, g_2); players.add(AI_Random);
        Player human = new Player("HUMAN", cards, g_3); players.add(human);

        /* Initial state with some players and cards
           and without parent and appliedAction. */
        State initial_state = new State(players, cards, null, null, 0);

        action = ISMCTS(initial_state, n);

        System.out.println(action);

    }

    private static Action randomAI (State s) {
        Random rand = new Random();
        ArrayList<State> children = c(s);
        return children.get( rand.nextInt( children.size() ) ).appliedAction;
    }

    private static Action ISMCTS (State s, int n) {
        for (int i = 0; i < n; ++i) {
            s.getDeterminization();
            State selected = select(s);
            if (u(selected).size() != 0) {
                selected = expand(selected);
            }
            int reward = simulate(selected);
            backpropagate(selected, reward);
        }
        /* Choose the most visited node. */
        int max_visits = Integer.MIN_VALUE;
        State s_c = s.children.get(0);
        for (int i = 0; i < s.children.size(); ++i) {
            if (s.children.get(i).visits > max_visits) {
                max_visits = s.children.get(i).visits;
                s_c = s.children.get(i);
            }
        }
        return s_c.appliedAction;
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
        while (u(selected).size() == 0 && !selected.isTerminal()) {
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
        ArrayList<State> u_children = u(s);
        s.children.add( u_children.get( rand.nextInt( u_children.size() ) ) );
        return s;
    }

    /** Simulation stage */
    private static int simulate (State s) {
        Random rand = new Random();
        State s_copy = new State(s);
        while (!s.isTerminal()) {
            ArrayList<State> children = c(s_copy);
            s_copy = children.get( rand.nextInt( children.size() ) );
        }
        return s_copy.isVictory() ? 1 : 0;
    }

    /** Backpropagation stage */
    private static void backpropagate (State s, int reward) {
        while (s.parent != null) {
            s.visits++;
            s.victories += reward;
            for (int i = 0; i < s.parent.children.size(); ++i) s.parent.children.get(i).availability++;
            s = s.parent;
        }
    }

    /** Returns all possible actions of state 's'
     *  that are not in the tree yet. */
    private static ArrayList<State> u (State s) {
        ArrayList<State> p_children = c(s);
        ArrayList<Action> p_actions = new ArrayList<>();
        for (State state : p_children) p_actions.add(state.appliedAction);

        ArrayList<State> a_children = s.children;
        ArrayList<Action> a_actions = new ArrayList<>();
        for (State state : a_children) a_actions.add(state.appliedAction);

        ArrayList<State> children_to_add = new ArrayList<>();
        for (Action action : p_actions) {
            if (!a_actions.contains(action)) {
                children_to_add.add(action.applyAction(s));
            }
        }
        return children_to_add;
    }

    /** Returns all possible actions of state 's'. */
    private static ArrayList<State> c (State s) {
        ArrayList<State> list = new ArrayList<>();

        // Guest actions without effects
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
            if (guest.isApplicableAction(s)) list.add(guest.applyAction(s));
        }

        // Guest actions with effects
        for (int i = 0; i < s.players.size(); ++i) {
            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(j), true);
                if (guest.isApplicableAction(s))
                    list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
            }
        }

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
