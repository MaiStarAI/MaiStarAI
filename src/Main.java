import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static int c_calls = 0;
    private static long end_time;
    private static int iterations = 3000;
    private static long elapsedTime = 0;
    private static ArrayList<Integer> numbers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Abilities
        HashMap<Colors, Integer> a_1 = new HashMap<>();
        a_1.put(Colors.Red, 3);
        a_1.put(Colors.Blue, 3);
        a_1.put(Colors.Green, 3);

        HashMap<Colors, Integer> a_2 = new HashMap<>();
        a_2.put(Colors.Red, 5);
        a_2.put(Colors.Blue, 5);
        a_2.put(Colors.Green, 5);

        HashMap<Colors, Integer> a_3 = new HashMap<>();
        a_3.put(Colors.Red, 1);
        a_3.put(Colors.Blue, 3);
        a_3.put(Colors.Green, 5);

        HashMap<Colors, Integer> a_4 = new HashMap<>();
        a_4.put(Colors.Red, 5);
        a_4.put(Colors.Blue, 1);
        a_4.put(Colors.Green, 3);

        HashMap<Colors, Integer> a_5 = new HashMap<>();
        a_5.put(Colors.Red, 2);
        a_5.put(Colors.Blue, 2);
        a_5.put(Colors.Green, 2);

        HashMap<Colors, Integer> a_6 = new HashMap<>();
        a_6.put(Colors.Red, 3);
        a_6.put(Colors.Blue, 5);
        a_6.put(Colors.Green, 1);

        // Geisha
        Geisha g_1 = new Geisha(GeishasName.Oboro, a_2, 0);
        Geisha g_2 = new Geisha(GeishasName.Akenohoshi, a_1, 1);
        Geisha g_3 = new Geisha(GeishasName.Momiji, a_4, 1);
        Geisha g_4 = new Geisha(GeishasName.Harukaze, a_3, 1);
        Geisha g_5 = new Geisha(GeishasName.Natsumi, a_6, 1);
        Geisha g_6 = new Geisha(GeishasName.Suzune, a_5, 1);

        Random rand = new Random();
        ArrayList<Card> deck = deckFill();

        ArrayList<Card> mcts_1_cards = new ArrayList<>();
        ArrayList<Card> random_1_cards = new ArrayList<>();
        ArrayList<Card> mcts_2_cards = new ArrayList<>();
        ArrayList<Card> random_2_cards = new ArrayList<>();
        ArrayList<Card> mcts_3_cards = new ArrayList<>();
        ArrayList<Card> random_3_cards = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            mcts_1_cards.add(deck.remove(rand.nextInt(deck.size())));
            random_1_cards.add(deck.remove(rand.nextInt(deck.size())));
            //mcts_2_cards.add(deck.remove(rand.nextInt(deck.size())));
            random_2_cards.add(deck.remove(rand.nextInt(deck.size())));
            //mcts_3_cards.add(deck.remove(rand.nextInt(deck.size())));
            //random_3_cards.add(deck.remove(rand.nextInt(deck.size())));
        }
        ArrayList<Player> players = new ArrayList<>();

        Player AI_ISMCTS_1 = new Player("ISMCTS_1", mcts_1_cards, g_2);
        AI_ISMCTS_1.setType(PlayerType.ISMCTS);
        players.add(AI_ISMCTS_1);

        Player AI_Random_1 = new Player("RANDOM_1", random_1_cards, g_1);
        AI_Random_1.setType(PlayerType.RandomAI);
        players.add(AI_Random_1);

        /*Player AI_ISMCTS_2 = new Player("ISMCTS_2", mcts_2_cards, g_4);
        AI_ISMCTS_2.setType(PlayerType.ISMCTS);
        players.add(AI_ISMCTS_2);*/

        Player AI_Random_2 = new Player("RANDOM_2", random_2_cards, g_6);
        AI_Random_2.setType(PlayerType.RandomAI);
        players.add(AI_Random_2);

        /*Player AI_ISMCTS_3 = new Player("ISMCTS_3", mcts_3_cards, g_5);
        AI_ISMCTS_3.setType(PlayerType.ISMCTS);
        players.add(AI_ISMCTS_3);*/

        /*Player AI_Random_3 = new Player("RANDOM_3", random_3_cards, g_6);
        AI_Random_3.setType(PlayerType.RandomAI);
        players.add(AI_Random_3);*/

    /* Initial state with some players and cards
       and without parent and appliedAction. */
        State initial_state = new State(players, deck, 0);

        long time_to_compute = 10;
        end_time = System.currentTimeMillis() + time_to_compute * 1000;

        Scanner in = null;
        PrintWriter out = null;
        try {
            Path file = Paths.get("statistics.log");
            if (!Files.exists(file)) Files.write(file, Arrays.asList("0"), Charset.forName("UTF-8"));
            in = new Scanner(new FileReader("statistics.log"));

            while (in.hasNext()) {
                String next = in.next();
                try {
                    numbers.add(Integer.parseInt(next));
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }
            }

            out = new PrintWriter(new FileWriter("game_" + numbers.get(0) + ".log"));
            out.println("Game " + numbers.get(0) + " history:");

        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }

        for (Player p : initial_state.players) {
            System.out.println(p.name + "'s score: " + p.score);
        }

        saveGame(initial_state);
    }

    public static void loop()
    {
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        //Background work
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Main.cycle();
                            }
                        });
                        //Keep with the background work
                        return null;
                    }
                };
            }
        };
        service.start();
    }

    public static void cycle () {
        while (!GameView.state.isTerminal()) {
            long start = System.currentTimeMillis();

            switch (GameView.state.players.get(GameView.state.turnPlayerIndex).type) {
                case ISMCTS: {
                    GameView.state.AIPlayer = GameView.state.turnPlayerIndex;
                    if (GameView.state.players.get(GameView.state.turnPlayerIndex).geisha.name == GeishasName.Akenohoshi) {
                        int blue = 3;
                        int red = 3;
                        int green = 3;
                        for (Card c : GameView.state.players.get(GameView.state.turnPlayerIndex).advertisers) {
                            switch (c.color) {
                                case Blue: { red += c.advReward.get(Colors.Blue); break; }
                                case Red: { red += c.advReward.get(Colors.Red); break; }
                                case Green: { red += c.advReward.get(Colors.Green); break; }
                            }
                        }
                        GameView.state.players.get(GameView.state.turnPlayerIndex).geisha.abilities.put(Colors.Blue, blue);
                        GameView.state.players.get(GameView.state.turnPlayerIndex).geisha.abilities.put(Colors.Red, red);
                        GameView.state.players.get(GameView.state.turnPlayerIndex).geisha.abilities.put(Colors.Green, green);
                    }
                    GameView.state = ISMCTS_iter(new State(GameView.state)).applyAction(new State(GameView.state));
                    GameView.state.parent = null;
                    GameView.state.children = null;
                    GameView.state.victories = 0;
                    GameView.state.visits = 0;
                    GameView.state.availability = 1;
                    break;
                }
                case RandomAI: {
                    GameView.state = randomAI(new State(GameView.state)).applyAction(new State(GameView.state));
                    GameView.state.parent = null;
                    GameView.state.children = null;
                    GameView.state.victories = 0;
                    GameView.state.visits = 0;
                    GameView.state.availability = 1;
                    break;
                }
                case Human: {
                    return;
                }
            }

            elapsedTime = System.currentTimeMillis() - start;
            /*try {
                saveAction(GameView.state);
            } catch (IOException e) {
                System.out.println(e + "\nERROR: Error while saving action.");
            }*/

            System.out.print(GameView.state.players.get(GameView.state.getPreviousPlayer()).name);
            System.out.print(" {");
            System.out.print(GameView.state.players.get(GameView.state.getPreviousPlayer()).geisha.name);
            System.out.print("} ");
            System.out.print(GameView.state.appliedAction.print());
            System.out.print(" ");
            System.out.print(" [" + GameView.state.cards.size() + "] ");
            System.out.println((elapsedTime / 1000f) + " sec");

            //end_time = System.currentTimeMillis() + time_to_compute * 1000;


            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            final CountDownLatch latch = new CountDownLatch(1);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        GameView.gg.updateAllGraphics();
                                    }finally{
                                        latch.countDown();
                                    }
                                }
                            });
                            latch.await();
                            return null;
                        }
                    };
                }
            };
            service.start();
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

    private static Action ISMCTS_iter (State s) {
        for (int i = 0; i < iterations; ++i) {
            if (i % 150 == 0) {
                System.out.println((Math.round(((float) i / (float) iterations) * 100f)) + " %");
            }
            s.getDeterminization();
            State selected = select(s);
            ArrayList<State> u_children = u(selected);
            if (u_children.size() != 0) {
                selected = expand(selected, u_children);
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
        while (u(selected).size() == 0 && !selected.isTerminal()) {
            double bestValue = Double.MIN_VALUE;
            if (s0.players.get(s0.turnPlayerIndex).type == PlayerType.RandomAI) {
                selected = randomAI(s0).applyAction(s0);
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
    private static State expand (State s, ArrayList<State> u_children) {
        Random rand = new Random();
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
        for (int i = 0; i < list.size(); ++i)
            if (equalActions(a, list.get(i))) return true;
        return false;
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
                    (equalCards(a1.firstCard, a2.firstCard)) &&
                    (a1.secondCard == a2.secondCard);
        else
            return (a1.name == a2.name) &&
                    (a1.usedEffect == a2.usedEffect) &&
                    (equalCards(a1.firstCard, a2.firstCard)) &&
                    (equalCards(a1.secondCard, a2.secondCard));
    }

    public static boolean equalCards(Card c1, Card c2) {
        if (c1 == null || c2 == null) {
            return c1 == c2;
        } else {
            boolean result = c1.color.equals(c2.color) &&
                    c1.name.equals(c2.name);
            //if (result) System.out.println("***Result is true***");
            return result;
        }
    }

    /** Returns all possible actions of state 's'. */
    private static ArrayList<State> c (State s) {
        if (s != null) {

            c_calls++;

            ArrayList<State> list = new ArrayList<>();

            /* Suzune */
            if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Suzune)) {
                for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                    Action suzune = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
                    suzune.usedGeisha = true;
                    suzune.firstGeishaEffect = true;
                    suzune.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(i);
                    list.add(suzune.applyAction(s));
                }
            }

            // Guest actions without effects
            for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                if (guest.isApplicableAction(s)) {

                    /* Akenohoshi */
                    if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Akenohoshi)) {
                        if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                s,
                                null,
                                true
                        )) {
                            guest.usedGeisha = true;
                            guest.firstGeishaEffect = true;
                            guest.geishaAbility = Colors.Blue;
                            list.add(guest.applyAction(s));
                            guest.geishaAbility = Colors.Red;
                            list.add(guest.applyAction(s));
                            guest.geishaAbility = Colors.Green;
                            list.add(guest.applyAction(s));
                        }
                    } else {
                        list.add(guest.applyAction(s));
                    }

                    /* Natsumi */
                    if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Natsumi)) {
                        if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                s,
                                null,
                                false
                        )) {
                            // Play one guest without effect
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                if (j != i) {
                                    Action natsumi = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                                    natsumi.usedGeisha = true;
                                    natsumi.usedEffect = false;
                                    natsumi.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                    natsumi.targetPlayer = -1;
                                    list.add(natsumi.applyAction(s));
                                }
                            }
                            // Play one guest with effect
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = 0; k < s.players.size(); ++k) {
                                    if (j != i) {
                                        Action natsumi = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), true);
                                        natsumi.usedGeisha = true;
                                        natsumi.usedEffect = true;
                                        natsumi.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                        natsumi.geishaTargetPlayer = k;
                                        list.add(natsumi.applyAction(s));
                                    }
                                }
                            }
                            // Play two guests without effects
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = j + 1; k < s.players.get(s.turnPlayerIndex).hand.size(); ++k) {
                                    if (j != i && k != i) {
                                        Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                                        Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                                        natsumi_1.usedGeisha = true;
                                        natsumi_2.usedGeisha = true;
                                        natsumi_1.usedEffect = false;
                                        natsumi_2.usedEffect = false;
                                        natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                        natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(k);
                                        natsumi_1.geishaTargetPlayer = -1;
                                        natsumi_2.geishaTargetPlayer = -1;
                                        State temp = natsumi_2.applyAction(s);
                                        temp.turnPlayerIndex = temp.getPreviousPlayer();
                                        list.add(natsumi_1.applyAction(temp));
                                    }
                                }
                            }
                            // Play one guest without and another guest with effect
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = j + 1; k < s.players.get(s.turnPlayerIndex).hand.size(); ++k) {
                                    for (int l = 0; l < s.players.size(); ++l) {
                                        if (j != i && k != i) {
                                            Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                                            Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), true);
                                            natsumi_1.usedGeisha = true;
                                            natsumi_2.usedGeisha = true;
                                            natsumi_1.usedEffect = false;
                                            natsumi_2.usedEffect = true;
                                            natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                            natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(k);
                                            natsumi_1.geishaTargetPlayer = -1;
                                            natsumi_2.geishaTargetPlayer = l;
                                            State temp = natsumi_2.applyAction(s);
                                            temp.turnPlayerIndex = temp.getPreviousPlayer();
                                            list.add(natsumi_1.applyAction(temp));
                                        }
                                    }
                                }
                            }
                            // Play one guest with effect and another without
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = j + 1; k < s.players.get(s.turnPlayerIndex).hand.size(); ++k) {
                                    for (int l = 0; l < s.players.size(); ++l) {
                                        if (j != i && k != i) {
                                            Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), true);
                                            Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), false);
                                            natsumi_1.usedGeisha = true;
                                            natsumi_2.usedGeisha = true;
                                            natsumi_1.usedEffect = true;
                                            natsumi_2.usedEffect = false;
                                            natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                            natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(k);
                                            natsumi_1.geishaTargetPlayer = l;
                                            natsumi_2.geishaTargetPlayer = -1;
                                            State temp = natsumi_2.applyAction(s);
                                            temp.turnPlayerIndex = temp.getPreviousPlayer();
                                            list.add(natsumi_1.applyAction(temp));
                                        }
                                    }
                                }
                            }
                            // Play two guests with effect
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = j + 1; k < s.players.get(s.turnPlayerIndex).hand.size(); ++k) {
                                    for (int l = 0; l < s.players.size(); ++l) {
                                        for (int m = 0; m < s.players.size(); ++m) {
                                            if (j != i && k != i) {
                                                Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), true);
                                                Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(i), true);
                                                natsumi_1.usedGeisha = true;
                                                natsumi_2.usedGeisha = true;
                                                natsumi_1.usedEffect = true;
                                                natsumi_2.usedEffect = true;
                                                natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                                natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(k);
                                                natsumi_1.geishaTargetPlayer = l;
                                                natsumi_2.geishaTargetPlayer = m;
                                                State temp_1 = natsumi_2.applyAction(s);
                                                temp_1.turnPlayerIndex = temp_1.getPreviousPlayer();
                                                State temp_2 = natsumi_1.applyAction(s);
                                                temp_2.turnPlayerIndex = temp_2.getPreviousPlayer();
                                                list.add(natsumi_1.applyAction(temp_1));
                                                list.add(natsumi_2.applyAction(temp_2));
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

                    Action guest = new Action(s.players.get(s.turnPlayerIndex).hand.get(j), true);
                    if (guest.isApplicableAction(s)) {

                        /* Akenohoshi */
                        if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Akenohoshi)) {
                            if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                    s,
                                    null,
                                    true
                            )) {
                                guest.usedGeisha = true;
                                guest.firstGeishaEffect = true;
                                guest.geishaAbility = Colors.Blue;
                                list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                                guest.geishaAbility = Colors.Red;
                                list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                                guest.geishaAbility = Colors.Green;
                                list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                            }
                        } else {
                            list.add(guest.applyEffect(guest.applyAction(s), s.players.get(s.turnPlayerIndex).hand.get(j), i, true));
                        }

                        /* Momiji */
                        if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Momiji)) {
                            for (int k = 0; k < s.players.size(); ++k) {
                                if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                        s,
                                        s.players.get(s.turnPlayerIndex).hand.get(j),
                                        false
                                )) {
                                    Action momiji = new Action(s.players.get(s.turnPlayerIndex).hand.get(j), true);
                                    momiji.usedGeisha = true;
                                    momiji.geishaTargetPlayer = k;
                                    list.add(momiji.applyAction(s));
                                }
                            }
                        }

                        /* Natsumi */
                        if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Natsumi)) {
                            if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                                    s,
                                    null,
                                    false
                            )) {
                                // Play one guest without effect
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    if (a != i) {
                                        Action natsumi = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), false);
                                        natsumi.usedGeisha = true;
                                        natsumi.usedEffect = false;
                                        natsumi.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                        natsumi.geishaTargetPlayer = -1;
                                        list.add(natsumi.applyAction(s));
                                    }
                                }
                                // Play one guest with effect
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    for (int b = 0; b < s.players.size(); ++b) {
                                        if (a != i) {
                                            Action natsumi = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), true);
                                            natsumi.usedGeisha = true;
                                            natsumi.usedEffect = true;
                                            natsumi.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                            natsumi.geishaTargetPlayer = b;
                                            list.add(natsumi.applyAction(s));
                                        }
                                    }
                                }
                                // Play two guests without effects
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    for (int b = a + 1; b < s.players.get(s.turnPlayerIndex).hand.size(); ++b) {
                                        if (a != i && b != i) {
                                            Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), false);
                                            Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(b), false);
                                            natsumi_1.usedGeisha = true;
                                            natsumi_2.usedGeisha = true;
                                            natsumi_1.usedEffect = false;
                                            natsumi_2.usedEffect = false;
                                            natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                            natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(b);
                                            natsumi_1.geishaTargetPlayer = -1;
                                            natsumi_2.geishaTargetPlayer = -1;
                                            State temp = natsumi_2.applyAction(s);
                                            temp.turnPlayerIndex = temp.getPreviousPlayer();
                                            list.add(natsumi_1.applyAction(temp));
                                        }
                                    }
                                }
                                // Play one guest without and another guest with effect
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    for (int b = a + 1; b < s.players.get(s.turnPlayerIndex).hand.size(); ++b) {
                                        for (int c = 0; c < s.players.size(); ++c) {
                                            if (a != i && b != i) {
                                                Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), false);
                                                Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(b), true);
                                                natsumi_1.usedGeisha = true;
                                                natsumi_2.usedGeisha = true;
                                                natsumi_1.usedEffect = false;
                                                natsumi_2.usedEffect = true;
                                                natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                                natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(b);
                                                natsumi_1.geishaTargetPlayer = -1;
                                                natsumi_2.geishaTargetPlayer = c;
                                                State temp = natsumi_2.applyAction(s);
                                                temp.turnPlayerIndex = temp.getPreviousPlayer();
                                                list.add(natsumi_1.applyAction(temp));
                                            }
                                        }
                                    }
                                }
                                // Play one guest with effect and another without
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    for (int b = a + 1; b < s.players.get(s.turnPlayerIndex).hand.size(); ++b) {
                                        for (int c = 0; c < s.players.size(); ++c) {
                                            if (a != i && b != i) {
                                                Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), true);
                                                Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(b), false);
                                                natsumi_1.usedGeisha = true;
                                                natsumi_2.usedGeisha = true;
                                                natsumi_1.usedEffect = true;
                                                natsumi_2.usedEffect = false;
                                                natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                                natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(b);
                                                natsumi_1.geishaTargetPlayer = c;
                                                natsumi_2.geishaTargetPlayer = -1;
                                                State temp = natsumi_2.applyAction(s);
                                                temp.turnPlayerIndex = temp.getPreviousPlayer();
                                                list.add(natsumi_1.applyAction(temp));
                                            }
                                        }
                                    }
                                }
                                // Play two guests with effect
                                for (int a = 0; a < s.players.get(s.turnPlayerIndex).hand.size(); ++a) {
                                    for (int b = a + 1; b < s.players.get(s.turnPlayerIndex).hand.size(); ++b) {
                                        for (int c = 0; c < s.players.size(); ++c) {
                                            for (int d = 0; d < s.players.size(); ++d) {
                                                if (a != i && b != i) {
                                                    Action natsumi_1 = new Action(s.players.get(s.turnPlayerIndex).hand.get(a), true);
                                                    Action natsumi_2 = new Action(s.players.get(s.turnPlayerIndex).hand.get(b), true);
                                                    natsumi_1.usedGeisha = true;
                                                    natsumi_2.usedGeisha = true;
                                                    natsumi_1.usedEffect = true;
                                                    natsumi_2.usedEffect = true;
                                                    natsumi_1.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(a);
                                                    natsumi_2.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(b);
                                                    natsumi_1.geishaTargetPlayer = c;
                                                    natsumi_2.geishaTargetPlayer = d;
                                                    State temp_1 = natsumi_2.applyAction(s);
                                                    temp_1.turnPlayerIndex = temp_1.getPreviousPlayer();
                                                    State temp_2 = natsumi_1.applyAction(s);
                                                    temp_2.turnPlayerIndex = temp_2.getPreviousPlayer();
                                                    list.add(natsumi_1.applyAction(temp_1));
                                                    list.add(natsumi_2.applyAction(temp_2));
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

            /* Harukaze */
            if (s.players.get(s.turnPlayerIndex).geisha.name.equals(GeishasName.Harukaze)) {
                if (s.players.get(s.turnPlayerIndex).geisha.isApplicableEffect(
                        s,
                        null,
                        false
                )) {
                    for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                        Action harukaze = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
                        if (harukaze.isApplicableAction(s)) {
                            for (int j = 0; j < s.players.get(s.turnPlayerIndex).hand.size(); ++j) {
                                for (int k = j + 1; k < s.players.get(s.turnPlayerIndex).hand.size(); ++k) {
                                    if (j != i && k != i) {
                                        harukaze.usedGeisha = true;
                                        harukaze.geishaCard1 = s.players.get(s.turnPlayerIndex).hand.get(j);
                                        harukaze.geishaCard2 = s.players.get(s.turnPlayerIndex).hand.get(k);
                                        list.add(harukaze.applyAction(s));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Advertiser actions
                for (int i = 0; i < s.players.get(s.turnPlayerIndex).hand.size(); ++i) {
                    Action advertiser = new Action(s.players.get(s.turnPlayerIndex).hand.get(i));
                    if (advertiser.isApplicableAction(s)) {
                        list.add(advertiser.applyAction(s));
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
                        for (int j = 0; j < list.get(i).players.get(s.turnPlayerIndex).hand.size(); ++j) {
                            State copy = new State(list.get(i));
                            copy.turnPlayerIndex = s.turnPlayerIndex;
                            Action suzune = new Action(copy.players.get(s.turnPlayerIndex).hand.get(j));
                            suzune.usedGeisha = true;
                            suzune.firstGeishaEffect = false;
                            suzune.geishaCard1 = list.get(i).players.get(s.turnPlayerIndex).hand.get(j);
                            final_list.add(suzune.applyAction(copy));
                        }
                    }
                }
            }

            return final_list;
        }
        return new ArrayList<>();
    }

    /** Returns draw deck */
    public static ArrayList<Card> deckFill() {
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

    private static void saveGame (State s) throws IOException {
        //
        PrintWriter out_1 = null;
        PrintWriter out_2 = null;

        try {

            out_1 = new PrintWriter(new FileWriter("games.log", true));

            StringBuilder temp = new StringBuilder();
            temp.append("Game ").append(numbers.get(0)).append(" results:\r\n");
            for (Player p : s.players) {
                temp.append(p.name).append(" score: ").append(p.score).append("\r\n");
            }
            temp.append("\r\n");
            out_1.print(temp.toString());
            out_1.flush();
            out_1.close();

            numbers.set(0, numbers.get(0) + 1); // Increase games number

            int best_index = -1;
            int best_score = -1;
            for (int i = 0; i < s.players.size(); ++i) {
                if (s.players.get(i).score  > best_score) {
                    best_score = s.players.get(i).score;
                    best_index = i;
                }
            }

            if (s.players.get(best_index).type.equals(PlayerType.ISMCTS)) {
                numbers.set(1, numbers.get(1) + 1);
            }

            numbers.set(2, (numbers.get(1) / numbers.get(0)) * 100);

            out_2 = new PrintWriter(new FileWriter("statistics.log", false));
            temp = new StringBuilder();
            temp.append("Game statistics;\r\n").append("Games played: ").append(numbers.get(0)).append("\r\n");
            temp.append("ISMCTS wins: ").append(numbers.get(1)).append("\r\n");
            temp.append("Win rate: ").append(numbers.get(2)).append(" %\r\n");
            out_2.print(temp.toString());

        } finally {
            if (out_1 != null) out_1.close();
            if (out_2 != null) out_2.close();
        }

        //
    }

    private static void saveAction(State s) throws IOException {
        //
        PrintWriter out = null;

        try {

            out = new PrintWriter(new FileWriter("game_" + numbers.get(0) + ".log", true));

            String temp = s.players.get(s.getPreviousPlayer()).name + ":" +
                    " {" +
                    s.players.get(s.getPreviousPlayer()).geisha.name +
                    "} " +
                    s.appliedAction.print() +
                    " [" + s.cards.size() + "] " + (elapsedTime / 1000f) + " sec\r\n";
            out.print(temp);
            out.flush();
            out.close();

        } finally {
            if (out != null) out.close();
        }

        //
    }

}