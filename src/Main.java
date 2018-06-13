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
        Geisha g_2 = new Geisha(GeishasName.Momiji, a_2, 1);
        Geisha g_3 = new Geisha(GeishasName.Harukaze, a_3, 1);


        ArrayList<Card> cards = new ArrayList<>(); // TODO: fill the array with cards (somehow)
        ArrayList<Player> players = new ArrayList<>();

        Player AI_ISMCTS = new Player("ISMCTS", cards, g_1); players.add(AI_ISMCTS);
        Player AI_Random = new Player("RANDOM", cards, g_2); players.add(AI_Random);
        Player human = new Player("HUMAN", cards, g_3); players.add(human);

        /* Initial state with some players and cards
           and without parent and appliedAction. */
        State initial_state = new State(players, cards, 0);

        action = ISMCTS(initial_state, n);

        System.out.println(action);

    }

    /** AI that makes random turns */
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
            if (s0.turnPlayerIndex == 1) {
                selected = randomAI(s0).applyAction(s0);
            } else {
                for (State s : s0.children) {
                    double value = uct(s);
                    if (bestValue < value) {
                        bestValue = value;
                        selected = s;
                    }
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
        ArrayList<State> s_list = new ArrayList<>();
        ArrayList<State> a_list = new ArrayList<>();

        /* Suzune */
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                    s,
                    null,
                    true
            )) s_list.add(
                    s.players.get(s.turnPlayerIndex).geisha.applyGeisha(
                            s,
                            s.players.get(s.turnPlayerIndex).hand.get(i),
                            null,
                            false,
                            null,
                            true,
                            -1
                    )
            );
        }
        for (State state : s_list) {
            list.addAll(c(state));
        }

        /* Akenohoshi */
        if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                s,
                null,
                true
        )) {
            a_list.add(
                    s.players.get(s.turnPlayerIndex).geisha.applyGeisha(
                            s,
                            null,
                            null,
                            false,
                            Colors.Blue,
                            true,
                            -1
                    )
            );
            a_list.add(
                    s.players.get(s.turnPlayerIndex).geisha.applyGeisha(
                            s,
                            null,
                            null,
                            false,
                            Colors.Red,
                            true,
                            -1
                    )
            );
            a_list.add(
                    s.players.get(s.turnPlayerIndex).geisha.applyGeisha(
                            s,
                            null,
                            null,
                            false,
                            Colors.Green,
                            true,
                            -1
                    )
            );
        }
        for (State state : a_list) {
            list.addAll(c(state));
        }

        // Guest actions without effects
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
            if (guest.isApplicableAction(s)) {
                list.add(guest.applyAction(s));

                int last_pi = s.turnPlayerIndex;
                State sa_guest = list.get(list.size() - 1);

                /* Natsumi */
                if (sa_guest.players.get(last_pi).geisha.isApplicableEffect(
                        sa_guest,
                        null,
                        false
                ))  {
                    // Play one guest without effect
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue)) {
                            list.add(
                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                            sa_guest,
                                            sa_guest.players.get(last_pi).hand.get(j),
                                            null,
                                            false,
                                            null,
                                            false,
                                            -1
                                    )
                            );
                        }
                    }
                    // Play one guest with effect
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        for (int k = 0; k < sa_guest.players.size(); ++k) {
                            if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue)) {
                                list.add(
                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                sa_guest,
                                                sa_guest.players.get(last_pi).hand.get(j),
                                                null,
                                                true,
                                                null,
                                                false,
                                                k
                                        )
                                );
                            }
                        }
                    }
                    // Play two guests without effects
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        for (int k = j + 1; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue) &&
                                    sa_guest.players.get(last_pi).hand.get(k).color.equals(Colors.Blue)) {
                                list.add(
                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest,
                                                        sa_guest.players.get(last_pi).hand.get(j),
                                                        null,
                                                        false,
                                                        null,
                                                        false,
                                                        -1
                                                ),
                                                sa_guest.players.get(last_pi).hand.get(k),
                                                null,
                                                false,
                                                null,
                                                false,
                                                -1
                                        )
                                );
                            }
                        }
                    }
                    // Play one guest without and another guest with effect
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        for (int k = j + 1; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = 0; l < sa_guest.players.size(); ++l) {
                                if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue) &&
                                        sa_guest.players.get(last_pi).hand.get(k).color.equals(Colors.Blue)) {
                                    list.add(
                                            sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            sa_guest,
                                                            sa_guest.players.get(last_pi).hand.get(j),
                                                            null,
                                                            false,
                                                            null,
                                                            false,
                                                            -1
                                                    ),
                                                    sa_guest.players.get(last_pi).hand.get(k),
                                                    null,
                                                    true,
                                                    null,
                                                    false,
                                                    l
                                            )
                                    );
                                }
                            }
                        }
                    }
                    // Play one guest with effect and another without
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        for (int k = j + 1; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = 0; l < sa_guest.players.size(); ++l) {
                                if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue) &&
                                        sa_guest.players.get(last_pi).hand.get(k).color.equals(Colors.Blue)) {
                                    list.add(
                                            sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            sa_guest,
                                                            sa_guest.players.get(last_pi).hand.get(j),
                                                            null,
                                                            true,
                                                            null,
                                                            false,
                                                            l
                                                    ),
                                                    sa_guest.players.get(last_pi).hand.get(k),
                                                    null,
                                                    false,
                                                    null,
                                                    false,
                                                    -1
                                            )
                                    );
                                }
                            }
                        }
                    }
                    // Play two guests with effect
                    for (int j = 0; j < sa_guest.players.get(last_pi).hand.size(); ++j) {
                        for (int k = j + 1; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = 0; l < sa_guest.players.size(); ++l) {
                                for (int m = 0; m < sa_guest.players.size(); ++m) {
                                    if (sa_guest.players.get(last_pi).hand.get(j).color.equals(Colors.Blue) &&
                                            sa_guest.players.get(last_pi).hand.get(k).color.equals(Colors.Blue)) {
                                        list.add(
                                                sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                sa_guest,
                                                                sa_guest.players.get(last_pi).hand.get(j),
                                                                null,
                                                                true,
                                                                null,
                                                                false,
                                                                l
                                                        ),
                                                        sa_guest.players.get(last_pi).hand.get(k),
                                                        null,
                                                        true,
                                                        null,
                                                        false,
                                                        m
                                                )
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Guest actions with effects
        for (int i = 0; i < s.players.size(); ++i) {
            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {

                int last_pi = s.turnPlayerIndex;
                State sa_guest;

                Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(j), true);
                if (guest.isApplicableAction(s)) {
                    list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                    sa_guest = list.get(list.size() - 1);

                    /* Momiji */
                    for (int k = 0; k < s.players.size(); ++k) {
                        if (s.players.get(last_pi).geisha.isApplicableEffect(
                                list.get(list.size() - 1),
                                s.players.get(last_pi).hand.get(j),
                                false
                        )) {
                            list.add(s.players.get(last_pi).geisha.applyGeisha(
                                    list.get(list.size() - 1),
                                    null,
                                    null,
                                    true,
                                    null,
                                    false,
                                    k
                            ));
                        }
                    }

                    /* Natsumi */
                    if (sa_guest.players.get(last_pi).geisha.isApplicableEffect(
                            sa_guest,
                            null,
                            false
                    ))  {
                        // Play one guest without effect
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            if (sa_guest.players.get(last_pi).hand.get(k).color.equals(Colors.Blue)) {
                                list.add(
                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                sa_guest,
                                                sa_guest.players.get(last_pi).hand.get(k),
                                                null,
                                                false,
                                                null,
                                                false,
                                                -1
                                        )
                                );
                            }
                        }
                        // Play one guest with effect
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = 0; l < sa_guest.players.size(); ++l) {
                                if (sa_guest.players.get(last_pi).hand.get(l).color.equals(Colors.Blue)) {
                                    list.add(
                                            sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                    sa_guest,
                                                    sa_guest.players.get(last_pi).hand.get(l),
                                                    null,
                                                    true,
                                                    null,
                                                    false,
                                                    l
                                            )
                                    );
                                }
                            }
                        }
                        // Play two guests without effects
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = k + 1; l < sa_guest.players.get(last_pi).hand.size(); ++l) {
                                if (sa_guest.players.get(last_pi).hand.get(l).color.equals(Colors.Blue) &&
                                        sa_guest.players.get(last_pi).hand.get(l).color.equals(Colors.Blue)) {
                                    list.add(
                                            sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            sa_guest,
                                                            sa_guest.players.get(last_pi).hand.get(l),
                                                            null,
                                                            false,
                                                            null,
                                                            false,
                                                            -1
                                                    ),
                                                    sa_guest.players.get(last_pi).hand.get(l),
                                                    null,
                                                    false,
                                                    null,
                                                    false,
                                                    -1
                                            )
                                    );
                                }
                            }
                        }
                        // Play one guest without and another guest with effect
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = k + 1; l < sa_guest.players.get(last_pi).hand.size(); ++l) {
                                for (int m = 0; m < sa_guest.players.size(); ++m) {
                                    if (sa_guest.players.get(last_pi).hand.get(m).color.equals(Colors.Blue) &&
                                            sa_guest.players.get(last_pi).hand.get(m).color.equals(Colors.Blue)) {
                                        list.add(
                                                sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                sa_guest,
                                                                sa_guest.players.get(last_pi).hand.get(m),
                                                                null,
                                                                false,
                                                                null,
                                                                false,
                                                                -1
                                                        ),
                                                        sa_guest.players.get(last_pi).hand.get(m),
                                                        null,
                                                        true,
                                                        null,
                                                        false,
                                                        m
                                                )
                                        );
                                    }
                                }
                            }
                        }
                        // Play one guest with effect and another without
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = k + 1; l < sa_guest.players.get(last_pi).hand.size(); ++l) {
                                for (int m = 0; m < sa_guest.players.size(); ++m) {
                                    if (sa_guest.players.get(last_pi).hand.get(m).color.equals(Colors.Blue) &&
                                            sa_guest.players.get(last_pi).hand.get(m).color.equals(Colors.Blue)) {
                                        list.add(
                                                sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                sa_guest,
                                                                sa_guest.players.get(last_pi).hand.get(m),
                                                                null,
                                                                true,
                                                                null,
                                                                false,
                                                                m
                                                        ),
                                                        sa_guest.players.get(last_pi).hand.get(m),
                                                        null,
                                                        false,
                                                        null,
                                                        false,
                                                        -1
                                                )
                                        );
                                    }
                                }
                            }
                        }
                        // Play two guests with effect
                        for (int k = 0; k < sa_guest.players.get(last_pi).hand.size(); ++k) {
                            for (int l = k + 1; l < sa_guest.players.get(last_pi).hand.size(); ++l) {
                                for (int m = 0; m < sa_guest.players.size(); ++m) {
                                    for (int n = 0; n < sa_guest.players.size(); ++n) {
                                        if (sa_guest.players.get(last_pi).hand.get(n).color.equals(Colors.Blue) &&
                                                sa_guest.players.get(last_pi).hand.get(n).color.equals(Colors.Blue)) {
                                            list.add(
                                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                    sa_guest,
                                                                    sa_guest.players.get(last_pi).hand.get(n),
                                                                    null,
                                                                    true,
                                                                    null,
                                                                    false,
                                                                    n
                                                            ),
                                                            sa_guest.players.get(last_pi).hand.get(n),
                                                            null,
                                                            true,
                                                            null,
                                                            false,
                                                            n
                                                    )
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Advertiser actions
        for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
            Action advertiser = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
            if (advertiser.isApplicableAction(s)) {
                list.add(advertiser.applyAction(s));

                /* Harukaze */
                State last_state = list.get(list.size() - 1);
                if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                        last_state,
                        null,
                        false
                        )) {
                    for (int j = 0; j < last_state.players.get(last_state.turnPlayerIndex).hand.size(); ++j) {
                        for (int k = j + 1; k < last_state.players.get(last_state.turnPlayerIndex).hand.size(); ++k) {
                            list.add(last_state.players.get(last_state.turnPlayerIndex).geisha.applyGeisha(
                                    last_state,
                                    last_state.players.get(last_state.turnPlayerIndex).hand.get(j),
                                    last_state.players.get(last_state.turnPlayerIndex).hand.get(k),
                                    false,
                                    null,
                                    false,
                                    -1
                            ));
                        }
                    }
                }
            }
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

        /* Suzune */
        for (int i = 0; i < list.size(); ++i) {
            if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                    list.get(i),
                    null,
                    false
            )) {
                for (int j = 0; j < list.get(i).players.get(s.turnPlayerIndex).hand.size(); ++j) {
                    list.add(list.get(i).players.get(s.turnPlayerIndex).geisha.applyGeisha(
                            list.get(i),
                            list.get(i).players.get(s.turnPlayerIndex).hand.get(j),
                            null,
                            false,
                            null,
                            false,
                            -1
                    ));
                }
            }
        }

        return list;
    }

}
