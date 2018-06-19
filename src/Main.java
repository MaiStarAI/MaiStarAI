import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Main {

    private static int c_calls = 0;
    private static long time_to_compute = 40;
    private static long end_time;
    private enum Mode { ITERATIONS, TIME }

    public static void main(String[] args) {

        Action action;

        // Abilities
        HashMap<Colors, Integer> a_1 = new HashMap<>();
        a_1.put(Colors.Red, 5);
        a_1.put(Colors.Blue, 1);
        a_1.put(Colors.Green, 3);

        HashMap<Colors, Integer> a_2 = new HashMap<>();
        a_2.put(Colors.Red, 1);
        a_2.put(Colors.Blue, 3);
        a_2.put(Colors.Green, 5);

        HashMap<Colors, Integer> a_3 = new HashMap<>();
        a_3.put(Colors.Red, 2);
        a_3.put(Colors.Blue, 2);
        a_3.put(Colors.Green, 2);

        // Geisha
        Geisha g_1 = new Geisha(GeishasName.Momiji, a_1, 1);
        Geisha g_2 = new Geisha(GeishasName.Harukaze, a_2, 1);
        Geisha g_3 = new Geisha(GeishasName.Suzune, a_3, 1);

        Random rand = new Random();
        ArrayList<Card> deck = deckFill();

        ArrayList<Card> ai_cards = new ArrayList<>();
        ArrayList<Card> p2_cards = new ArrayList<>();
        ArrayList<Card> p3_cards = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            ai_cards.add(deck.remove(rand.nextInt(deck.size())));
            p2_cards.add(deck.remove(rand.nextInt(deck.size())));
            p3_cards.add(deck.remove(rand.nextInt(deck.size())));
        }
        ArrayList<Player> players = new ArrayList<>();

        Player AI_ISMCTS = new Player("ISMCTS", ai_cards, g_1); players.add(AI_ISMCTS);
        Player AI_Random = new Player("RANDOM", p2_cards, g_2); players.add(AI_Random);
        Player human = new Player("HUMAN", p3_cards, g_3); players.add(human);

        /* Initial state with some players and cards
           and without parent and appliedAction. */
        State initial_state = new State(players, deck, 0);

        Mode mode = Mode.TIME;
        long start_time;

        switch (mode) {
            case ITERATIONS: {
                start_time = System.currentTimeMillis();
                action = ISMCTS_iter(initial_state, 1000);
                end_time = System.currentTimeMillis();

                System.out.println();
                System.out.println("Elapsed time: " + (end_time - start_time)/1000 + " seconds");
                System.out.println();
                System.out.println(action);
                break;
            }
            case TIME: {
                end_time = System.currentTimeMillis() + time_to_compute * 1000;
                action = ISMCTS_time (initial_state);

                System.out.println();
                System.out.println("Elapsed time: " + time_to_compute + " seconds");
                System.out.println();
                System.out.println(action);
                break;
            }
        }

    }

    /** AI that makes random turns */
    private static Action randomAI (State s) {
        Random rand = new Random();
        ArrayList<State> children = c(s);
        for (int i = children.size() - 1; i >= 0; --i) if (children.get(i) == null) children.remove(i);
        int rand_i = rand.nextInt(children.size());
        return children.get(rand_i).appliedAction;
    }

    private static Action ISMCTS_iter (State s, int n) {
        for (int i = 0; i < n; ++i) {
            System.out.println(((i*1f / n*1f) * 100f) + " %");

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

    private static Action ISMCTS_time(State s) {
        while (System.currentTimeMillis() <= end_time) {
            System.out.println((100f - (float)(end_time - System.currentTimeMillis())/(time_to_compute * 1000)*100) + " %");

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
            if (selected.turnPlayerIndex == 1) {
                selected = randomAI(selected).applyAction(selected);
            } else {
                for (State s : selected.children) {
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
        State new_state = new State(u_children.get(rand.nextInt(u_children.size())));
        new_state.parent = s;
        if (s.children != null) {
            s.children.add(new_state);
        } else {
            s.children = new ArrayList<>();
            s.children.add(new_state);
        }
        return new_state;
    }

    /** Simulation stage */
    private static int simulate (State s) {
        Random rand = new Random();
        State s_copy = new State(s);
        while (!s_copy.isTerminal()) {
            ArrayList<State> children = c(s_copy);
            boolean all_null = true;
            for (State state :  children) if (state != null) all_null = false;
            if (children.size() == 0) {
                return s_copy.isVictory() ? 1 : 0;
            } else {
                int rand_i = rand.nextInt( children.size() );
                if (all_null) {
                    return s_copy.isVictory() ? 1 : 0;
                } else if (children.get(rand_i) != null) {
                    s_copy = children.get(rand_i);
                }
            }
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
        State s_copy = new State(s);
        ArrayList<State> p_children = c(s_copy);
        ArrayList<Action> p_actions = new ArrayList<>();
        for (State state : p_children) if (state != null) p_actions.add(state.appliedAction);

        ArrayList<Action> a_actions = new ArrayList<>();
        if (s.children != null)
            for (State state : s.children) a_actions.add(state.appliedAction);

        ArrayList<State> children_to_add = new ArrayList<>();
        for (Action p_a : p_actions) {
            if (!actionContains(a_actions, p_a))
                children_to_add.add(p_a.applyAction(s_copy));
        }

        return children_to_add;
    }

    private static boolean actionContains (ArrayList<Action> list, Action a) {
        boolean contains = false;
        for (Action action : list) if (equalActions(a, action)) contains = true;
        return contains;
    }

    private static boolean equalActions (Action a1, Action a2) {
        if (a1 == null || a2 == null)
            return a1 == a2;
        else if (a1.firstCard == null || a2.firstCard == null)
            return (a1.name == a2.name) &&
                    (a1.usedEffect == a2.usedEffect) &&
                    (a1.firstCard == a2.firstCard);
        else if (a1.secondCard == null || a2.secondCard == null)
            return (a1.name == a2.name) &&
                (a1.usedEffect == a2.usedEffect) &&
                (a1.firstCard.equals(a2.firstCard)) &&
                (a1.secondCard == a2.secondCard);
        else
            return (a1.name == a2.name) &&
                    (a1.usedEffect == a2.usedEffect) &&
                    (a1.firstCard.equals(a2.firstCard)) &&
                    (a1.secondCard.equals(a2.secondCard));
    }

    /** Returns all possible actions of state 's'. */
    private static ArrayList<State> c (State s) {
        if (s != null) {

            c_calls++;
            //System.out.println(c_calls);

            ArrayList<State> list = new ArrayList<>();
            ArrayList<State> s_list = new ArrayList<>();
            ArrayList<State> a_list = new ArrayList<>();

            /* Suzune */
            if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Suzune)) {
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
            }

            /* Akenohoshi */
            if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Akenohoshi)) {
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
            }

            // Guest actions without effects
            for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                if (guest.isApplicableAction(s)) {
                    list.add(guest.applyAction(s));

                    int last_pi = s.turnPlayerIndex;
                    State sa_guest = list.get(list.size() - 1);
                    sa_guest.turnPlayerIndex = s.turnPlayerIndex;

                    /* Natsumi */
                    if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Natsumi)) {
                        if (sa_guest.players.get(last_pi).geisha.isApplicableEffect(
                                sa_guest,
                                null,
                                false
                        )) {
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
                                            State temp = sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                    sa_guest,
                                                    sa_guest.players.get(last_pi).hand.get(j),
                                                    null,
                                                    true,
                                                    null,
                                                    false,
                                                    l
                                            );
                                            if (temp != null)
                                                list.add(
                                                    sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            temp,
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
                                                State temp = sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest,
                                                        sa_guest.players.get(last_pi).hand.get(j),
                                                        null,
                                                        true,
                                                        null,
                                                        false,
                                                        l
                                                );
                                                if (temp != null)
                                                    list.add(
                                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                temp,
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
            }

            // Guest actions with effects
            for (int i = 0; i < s.players.size(); ++i) {
                for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {

                    int last_pi = s.turnPlayerIndex;
                    State sa_guest;
                    Card played_card = s.players.get(s.turnPlayerIndex).hand.get(j);

                    Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(j), true);
                    if (guest.isApplicableAction(s)) {

                        list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                        sa_guest = list.get(list.size() - 1);
                        if (sa_guest != null) sa_guest.turnPlayerIndex = s.turnPlayerIndex;

                        /* Momiji */
                        if (sa_guest != null && sa_guest.players.get(last_pi).geisha.name.equals(GeishasName.Momiji)) {
                            if (sa_guest.players.get(last_pi).geisha.isApplicableEffect(
                                    sa_guest,
                                    played_card,
                                    false
                            )) {
                                for (int k = 0; k < sa_guest.players.size(); ++k) {

                                    list.add(sa_guest.players.get(last_pi).geisha.applyGeisha(
                                            sa_guest,
                                            null,
                                            null,
                                            true,
                                            null,
                                            false,
                                            k
                                    ));
                                }
                            }
                        }

                        /* Natsumi */
                        if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Natsumi)) {
                            if (sa_guest != null && sa_guest.players.get(last_pi).geisha.isApplicableEffect(
                                    sa_guest,
                                    null,
                                    false
                            )) {
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
                                                State temp = sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                        sa_guest,
                                                        sa_guest.players.get(last_pi).hand.get(m),
                                                        null,
                                                        true,
                                                        null,
                                                        false,
                                                        m
                                                );
                                                if (temp != null)
                                                    list.add(
                                                        sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                temp,
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
                                                    State temp = sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                            sa_guest,
                                                            sa_guest.players.get(last_pi).hand.get(n),
                                                            null,
                                                            true,
                                                            null,
                                                            false,
                                                            n
                                                    );
                                                    if (temp != null)
                                                        list.add(
                                                                sa_guest.players.get(last_pi).geisha.applyGeisha(
                                                                        temp,
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
            }

            // Advertiser actions
            for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                Action advertiser = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
                if (advertiser.isApplicableAction(s)) {
                    list.add(advertiser.applyAction(s));

                    /* Harukaze */
                    if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Harukaze)) {
                        State last_state = list.get(list.size() - 1);
                        last_state.turnPlayerIndex = s.turnPlayerIndex;
                        if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                last_state,
                                null,
                                false
                        )) {
                            if (last_state.drawDeck > 2) {
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

            ArrayList<State> final_list = new ArrayList<>(list);

            /* Suzune */
            if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Suzune)) {
                for (int i = 0; i < list.size(); ++i) {
                    if (list.get(i) != null) {
                        if (list.get(i).players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                list.get(i),
                                null,
                                false
                        )) {
                            for (int j = 0; j < list.get(i).players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                final_list.add(list.get(i).players.get(s.turnPlayerIndex).geisha.applyGeisha(
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
                }
            }

            return final_list;
        }
        return new ArrayList<>();
    }

    /** Returns draw deck */
    public static ArrayList<Card> deckFill () {
        ArrayList<Card> deck = new ArrayList<>();
        HashMap<Colors, Integer> bonus;

        // District_Kanryou
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.District_Kanryou, Colors.Black, 0, 0, bonus));

        // Scholar
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Scholar, Colors.Red, 1, 2, bonus));
        deck.add(new Card(CardsNames.Scholar, Colors.Red, 1, 2, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Scholar, Colors.Blue, 1, 2, bonus));
        deck.add(new Card(CardsNames.Scholar, Colors.Blue, 1, 2, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Scholar, Colors.Green, 1, 2, bonus));
        deck.add(new Card(CardsNames.Scholar, Colors.Green, 1, 2, bonus));

        // Emissary
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Emissary, Colors.Red, 6, 5, bonus));
        deck.add(new Card(CardsNames.Emissary, Colors.Red, 6, 5, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Emissary, Colors.Blue, 6, 5, bonus));
        deck.add(new Card(CardsNames.Emissary, Colors.Blue, 6, 5, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Emissary, Colors.Green, 6, 5, bonus));
        deck.add(new Card(CardsNames.Emissary, Colors.Green, 6, 5, bonus));

        // Sumo-Wrestler
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Red, 5, 3, bonus));
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Red, 5, 3, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Blue, 5, 3, bonus));
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Blue, 5, 3, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Green, 5, 3, bonus));
        deck.add(new Card(CardsNames.Sumo_Wrestler, Colors.Green, 5, 3, bonus));

        // Okaasan
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 2);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Okaasan, Colors.Red, 2, 1, bonus));
        deck.add(new Card(CardsNames.Okaasan, Colors.Red, 2, 1, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 2);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Okaasan, Colors.Blue, 2, 1, bonus));
        deck.add(new Card(CardsNames.Okaasan, Colors.Blue, 2, 1, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 2);
        deck.add(new Card(CardsNames.Okaasan, Colors.Green, 2, 1, bonus));
        deck.add(new Card(CardsNames.Okaasan, Colors.Green, 2, 1, bonus));

        // Samurai
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Samurai, Colors.Red, 8, 8, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Samurai, Colors.Blue, 8, 8, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Samurai, Colors.Green, 8, 8, bonus));

        // Ronin
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Ronin, Colors.Red, 2, 2, bonus));
        deck.add(new Card(CardsNames.Ronin, Colors.Red, 2, 2, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Ronin, Colors.Blue, 2, 2, bonus));
        deck.add(new Card(CardsNames.Ronin, Colors.Blue, 2, 2, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Ronin, Colors.Green, 2, 2, bonus));
        deck.add(new Card(CardsNames.Ronin, Colors.Green, 2, 2, bonus));

        // Doctor
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Doctor, Colors.Red, 7, 6, bonus));
        deck.add(new Card(CardsNames.Doctor, Colors.Red, 7, 6, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Doctor, Colors.Blue, 7, 6, bonus));
        deck.add(new Card(CardsNames.Doctor, Colors.Blue, 7, 6, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Doctor, Colors.Green, 7, 6, bonus));
        deck.add(new Card(CardsNames.Doctor, Colors.Green, 7, 6, bonus));

        // Courtier
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Courtier, Colors.Red, 3, 0, bonus));
        deck.add(new Card(CardsNames.Courtier, Colors.Red, 3, 0, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Courtier, Colors.Blue, 3, 0, bonus));
        deck.add(new Card(CardsNames.Courtier, Colors.Blue, 3, 0, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Courtier, Colors.Green, 3, 0, bonus));
        deck.add(new Card(CardsNames.Courtier, Colors.Green, 3, 0, bonus));

        // Actor
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Actor, Colors.Red, 5, 5, bonus));
        deck.add(new Card(CardsNames.Actor, Colors.Red, 5, 5, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Actor, Colors.Blue, 5, 5, bonus));
        deck.add(new Card(CardsNames.Actor, Colors.Blue, 5, 5, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Actor, Colors.Green, 5, 5, bonus));
        deck.add(new Card(CardsNames.Actor, Colors.Green, 5, 5, bonus));

        // Daimyo
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Daimyo, Colors.Red, 9, 10, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Daimyo, Colors.Blue, 9, 10, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Daimyo, Colors.Green, 9, 10, bonus));

        // Merchant
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Merchant, Colors.Red, 4, 4, bonus));
        deck.add(new Card(CardsNames.Merchant, Colors.Red, 4, 4, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Merchant, Colors.Blue, 4, 4, bonus));
        deck.add(new Card(CardsNames.Merchant, Colors.Blue, 4, 4, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Merchant, Colors.Green, 4, 4, bonus));
        deck.add(new Card(CardsNames.Merchant, Colors.Green, 4, 4, bonus));

        // Yakuza
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Yakuza, Colors.Red, 3, 3, bonus));
        deck.add(new Card(CardsNames.Yakuza, Colors.Red, 3, 3, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Yakuza, Colors.Blue, 3, 3, bonus));
        deck.add(new Card(CardsNames.Yakuza, Colors.Blue, 3, 3, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Yakuza, Colors.Green, 3, 3, bonus));
        deck.add(new Card(CardsNames.Yakuza, Colors.Green, 3, 3, bonus));

        // Thief
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Thief, Colors.Red, 4, 4, bonus));
        deck.add(new Card(CardsNames.Thief, Colors.Red, 4, 4, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 0);
        deck.add(new Card(CardsNames.Thief, Colors.Blue, 4, 4, bonus));
        deck.add(new Card(CardsNames.Thief, Colors.Blue, 4, 4, bonus));

        bonus = new HashMap<>();
        bonus.put(Colors.Red, 0);
        bonus.put(Colors.Blue, 0);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Thief, Colors.Green, 4, 4, bonus));
        deck.add(new Card(CardsNames.Thief, Colors.Green, 4, 4, bonus));

        // Monk
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Monk, Colors.Black, 9, 10, bonus));

        // Shogun
        bonus = new HashMap<>();
        bonus.put(Colors.Red, 1);
        bonus.put(Colors.Blue, 1);
        bonus.put(Colors.Green, 1);
        deck.add(new Card(CardsNames.Shogun, Colors.Black, 10, 10, bonus));

        return deck;
    }

}
