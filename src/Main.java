import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static Stage window;
    public static Parent root;

    public static final int windowWidth = 1040;
    public static final int windowHeight = 690;

    public static int gameNumber = 0;

    public static State state;
    public static int playerIndex;
    public static Player mainPlayer;
    public static GameGraphics gg;
    public static int[][] score;
    public static ArrayList<String> actions;
    public static int round;

    private static AlgorithmISMCTS ISMCTS = new AlgorithmISMCTS();
    private static AlgorithmRandom RANDOM = new AlgorithmRandom();

    public static boolean turnEnded;
    public static boolean stopRightThere;

    public static void setGraphics() {
        window = SetupView.window;
        window.setTitle("Mai-Star");

        window.setOnCloseRequest(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?",
                    ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                window.close();
            }
            e.consume();
        });

        //gg.playGuestButton.fireEvent(new ActionEvent());

        window.setMinWidth(windowWidth);
        window.setWidth(windowWidth);
        window.setMinHeight(windowHeight);
        window.setHeight(windowHeight);

        window.show();

        Platform.runLater(() -> main(new String[0]));
    }

    private static void loadGraphics() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("gameGraphics.fxml"));
            root = loader.load();
            gg = loader.getController();
            window.setScene(new Scene(root, windowWidth, windowHeight));
        } catch (IOException e) {
            System.out.println(e + "\nERROR: failed to load 'gameGraphics.fxml'");
        }
    }

    public static void main(String[] args) {
        playerIndex = SetupView.playerIndex;

        score = new int[SetupView.players.size()][3];

        actions = new ArrayList<>();

        round = 0;

        gameNumber = 0;

        try {
            File file = new File("logs");
            if (!file.exists()) file.mkdir();

            File file2 = new File("logs/statistics");
            if (!file2.exists()) file2.mkdir();

            for (final File fileEntry : file.listFiles()) {
                if (fileEntry.isFile() && fileEntry.getName().startsWith("game") && fileEntry.getName().endsWith(".log"))
                    try {
                        gameNumber = Integer.parseInt(fileEntry.getName().substring(4, fileEntry.getName().lastIndexOf(".")));
                    } catch (NumberFormatException e) {
                        System.out.println(e.toString());
                    }
            }
        } catch (NullPointerException e) {
            System.out.println(e + "\nERROR: 'logs' folder could not be found.");
        }

        gameNumber++;

        ISMCTS = new AlgorithmISMCTS();
        RANDOM = new AlgorithmRandom();

        stopRightThere = false;

        turnEnded = false;

        nextRound();
    }

    public static void nextRound() {
        /* Initialization of the round */
        state = new State();

        state.fillDrawDeck(getAllCards()); // Fill the draw deck with the initial cards
        state.setRound(round);

        for (int i = 0; i < SetupView.players.size(); i++) {
            state.players.add(
                    new Player(
                            SetupView.aiType[i] == 0 ? Player.Type.Human : (SetupView.aiType[i] == 1 ? Player.Type.ISMCTS : Player.Type.Random),
                            SetupView.players.get(i), new Geisha(Geisha.Name.valueOf(SetupView.playerGeishas.get(i))))
            );

            if (round == 0) {
                for (String j : SetupView.playerCards.get(i)) {
                    state.players.get(i).addCard(state.removeCard(
                            Card.Name.valueOf(SetupView.getCardName(j).replace(" ", "_")),
                            Card.Color.valueOf(SetupView.getCardColor(j))
                    ));
                }
            } else {
                for (int j = 0; j < 5 + round; ++j) { // 5, 6, 7 cards
                    state.players.get(i).addCard(state.getRandomCard());
                }
            }

            /* Oboro geisha (+2 cards when the round starts) */
            if (state.players.get(i).getGeisha().getName() == Geisha.Name.Oboro) {
                state.players.get(i).addCard(state.getRandomCard());
                state.players.get(i).addCard(state.getRandomCard());
            }
        }

        mainPlayer = state.players.get(playerIndex);
        state.setTurnPlayer(state.players.get(0));
        state.setRound(round);

        loadGraphics();

        Service<Void> service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(() -> loop());
                        return null;
                    }
                };
            }
        };
        service.start();
    }

    public static void loop () {
        // Run the game loop
        while (!state.isTerminal()) {
            gg.updateAllGraphics();

            Action action;

            /* Makes a decision how to move */
            switch (state.getTurnPlayer().getType()) {
                case ISMCTS: {
                    action = ISMCTS.getAction(state);
                    state = state.applyAction(action);
                    addActionToLog(action);
                    break;
                }
                case Random: {
                    action = RANDOM.getAction(state);
                    state = state.applyAction(action);
                    addActionToLog(action);
                    break;
                }
                case Human: {
                    if (!state.allowed_actions.isEmpty()) System.out.println(state.allowed_actions);
                    if (state.allowed_actions.contains(Action.Name.AllowEffect)) {
                        gg.cancelEffect();
                        return;
                    }

                    if (!turnEnded) return;
                    turnEnded = false;
                    break;
                }
            }

            if (stopRightThere) return;

            if (state.getTurnPlayer().getType() != Player.Type.Human)
                state = changeStateAfterAction(state);
            else if (state.getLastAppliedAction() != null && state.getLastAppliedAction().getName() != Action.Name.EndTurn) {
                gg.changeStateAfterAction();
            }
        }

        gg.updateAllGraphics();

        saveScores();

        state.recordTurn();
        state.recordWin();
        recordStatistics();

        gg.winScreen();

        /* Round management */
        if (round < 2) {
            round++;
            nextRound();
        }
    }

    public static void saveScores() {
        for (int i = 0; i < state.getPlayers().size(); i++) {
            score[i][round] = state.players.get(i).getScore() + (round >= 1 ? score[i][round-1] : 0);
        }
    }

    public static void addActionToLog(Action action) {
        if (action.getName() == Action.Name.EndTurn
                || action.getName() == Action.Name.HarukazeDiscard
                || action.getName() == Action.Name.AllowEffect) return;

        String description = describeAction(action);
        actions.add("Turn " + (state.turn + 1) + ". " + description);
        if (actions.size() > 100) actions.remove(0);
        gg.lastActionDescription = description;

        System.out.println(describeAction(action)); //todo remove
    }

    public static String describeAction(Action action) {
        String description = "";
                /*action.getName() + " by " + action.getPlayer().getName()
                + " - " + (action.getCard1() == null ? "" : action.getCard1().getName())
                + " " + (action.getTargetPlayer() == null ? "" : action.getTargetPlayer().getName());*/

        description = description.concat(action.getPlayer().getName() + " ");

        String color = action.getCard1() == null ? "" :
                (action.getCard1().getColor() == Card.Color.BLACK ? "" : (action.getCard1().getColor().toString().toLowerCase() + " "));
        String color2 = action.getCard2() == null ? "" :
                (action.getCard2().getColor() == Card.Color.BLACK ? "" : (action.getCard2().getColor().toString().toLowerCase() + " "));

        switch (action.getName()) {
            case Guest:
                description = description + "played " + color + action.getCard1().getName().toString().replace("_", " ") + " as a guest";
                break;
            case GuestEffect:
                description = description + "used " + color + action.getCard1().getName().toString().replace("_", " ") + "'s effect"
                        + (action.getTargetPlayer() == null/* || action.getTargetPlayer().equals(action.getPlayer())*/ ? "" : " on " + action.getTargetPlayer().getName());
                break;
            case Advertiser:
                description = description + "played " + color + action.getCard1().getName().toString().replace("_", " ") + " as an advertiser";
                break;
            case Exchange:
                description = description + "exchanged their advertiser, " + color2 + action.getCard2().getName().toString().replace("_", " ") + " with "
                        + color + action.getCard1().getName().toString().replace("_", " ")  + " from their hand";
                break;
            case Introduce:
                description = description + "introduced " + color + action.getCard1().getName().toString().replace("_", " ")
                        + " and " + color2 + action.getCard2().getName().toString().replace("_", " ");
                break;
            case Search:
                description = description + "spent their turn searching for another patron";
                break;
            case CancelEffectRonin:
                description = description + "used their Ronin to cancel "
                        + (state.getLastPlayer() != null ? state.getLastPlayer().getName() : "another player") + "'s effect";
                break;
            case CancelEffectDistrict:
                description = description + "used their District Kanryou to cancel "
                        + (state.getLastPlayer() != null ? state.getLastPlayer().getName() : "another player") + "'s effect";
                break;
            case AllowEffect:
                description = description + "allowed "
                        + (state.getLastPlayer() != null ? state.getLastPlayer().getName() : "another player") + "'s effect";
                break;
            case Geisha:
                description = description + "used their Geisha's special effect";
                break;
            case HarukazeDiscard:
                description = description + "discarded their cards";
                break;
            case EndTurn:
                description = description + "ended their turn";
                break;
        }

        description += ".";

        return description;
    }

    public static void recordStatistics() {
        try {
            PrintWriter stats = new PrintWriter(new FileWriter("logs/statistics/.log", true));

            Scanner inStats = new Scanner(new FileReader("logs/statistics/.log"));

            ArrayList<Integer> challengers = new ArrayList<>(5);
            challengers.addAll(Arrays.asList(0, 0, 0, 0, 0));

            while (inStats.hasNextLine()) {
                String string = inStats.nextLine();
                if (string.startsWith("-") || string.endsWith("-") || string.equals("")) continue;
                try {
                    if (string.startsWith("ISMCTS won: ")) {
                        challengers.set(0, Integer.parseInt(string.split(" ")[2]));
                    } else if (string.startsWith("Human won: ")) {
                        challengers.set(1, Integer.parseInt(string.split(" ")[2]));
                    } else if (string.startsWith("Random won: ")) {
                        challengers.set(2, Integer.parseInt(string.split(" ")[2]));
                    } else if (string.startsWith("Draws: ")) {
                        challengers.set(3, Integer.parseInt(string.split(" ")[1]));
                    } else if (string.startsWith("Total rounds played: ")) {
                        challengers.set(4, Integer.parseInt(string.split(" ")[3]));
                    }
                } catch (NumberFormatException ignored) {}
            }

            inStats.close();

            if (challengers.get(0) + challengers.get(1) + challengers.get(2) + challengers.get(3) != challengers.get(4)) {
                String message = "ERROR: 'statistics' file had been damaged. Unable to read data. Starting next output from zero games played.";
                System.out.println(message);
                stats.println(message);
                challengers.clear();
                challengers.addAll(Arrays.asList(0, 0, 0, 0, 0));
            }

            int winnerIndex = 0;
            boolean draw = false;

            stats.println("Game " + gameNumber + " - Round " + (state.getRound() + 1));
            stats.println();

            for (int i = 0; i < state.players.size(); i++) {
                if (score[i][state.getRound()] > score[winnerIndex][state.getRound()]) {
                    winnerIndex = i;
                    draw = false;
                }

                if (i != winnerIndex && score[i][state.getRound()] == score[winnerIndex][state.getRound()]) draw = true;

                stats.println(state.getPlayers().get(i).getType().toString() + ": " + score[i][state.getRound()]);
            }

            if (draw)
                challengers.set(3, challengers.get(3) + 1);
            else {
                switch (state.getPlayers().get(winnerIndex).getType()) {
                    case ISMCTS:
                        challengers.set(0, challengers.get(0) + 1);
                        break;
                    case Human:
                        challengers.set(1, challengers.get(1) + 1);
                        break;
                    case Random:
                        challengers.set(2, challengers.get(2) + 1);
                        break;
                }
            }

            challengers.set(4, challengers.get(4) + 1);

            stats.println();
            stats.println("ISMCTS won: " + challengers.get(0));
            stats.println("Human won: " + challengers.get(1));
            stats.println("Random won: " + challengers.get(2));
            stats.println("Draws: " + challengers.get(3));
            stats.println("Total rounds played: " + challengers.get(4));
            stats.println("------------------------------------");

            stats.close();
        } catch (IOException e) {
            System.out.println("ERROR: Failed to record game statistics.");
        }
    }

    /** Filling a deck with the 75 cards */
    public static ArrayList<Card> getAllCards () {

        ArrayList<Card> list = new ArrayList<>();

        list.add(new Card(Card.Name.District_Kanryou, Card.Color.BLACK));

        list.add(new Card(Card.Name.Scholar, Card.Color.RED));
        list.add(new Card(Card.Name.Scholar, Card.Color.RED));
        list.add(new Card(Card.Name.Scholar, Card.Color.BLUE));
        list.add(new Card(Card.Name.Scholar, Card.Color.BLUE));
        list.add(new Card(Card.Name.Scholar, Card.Color.GREEN));
        list.add(new Card(Card.Name.Scholar, Card.Color.GREEN));

        list.add(new Card(Card.Name.Shogun, Card.Color.BLACK));

        list.add(new Card(Card.Name.Emissary, Card.Color.RED));
        list.add(new Card(Card.Name.Emissary, Card.Color.RED));
        list.add(new Card(Card.Name.Emissary, Card.Color.BLUE));
        list.add(new Card(Card.Name.Emissary, Card.Color.BLUE));
        list.add(new Card(Card.Name.Emissary, Card.Color.GREEN));
        list.add(new Card(Card.Name.Emissary, Card.Color.GREEN));

        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.RED));
        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.RED));
        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.BLUE));
        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.BLUE));
        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.GREEN));
        list.add(new Card(Card.Name.Sumo_Wrestler, Card.Color.GREEN));

        list.add(new Card(Card.Name.Okaasan, Card.Color.RED));
        list.add(new Card(Card.Name.Okaasan, Card.Color.RED));
        list.add(new Card(Card.Name.Okaasan, Card.Color.BLUE));
        list.add(new Card(Card.Name.Okaasan, Card.Color.BLUE));
        list.add(new Card(Card.Name.Okaasan, Card.Color.GREEN));
        list.add(new Card(Card.Name.Okaasan, Card.Color.GREEN));

        list.add(new Card(Card.Name.Samurai, Card.Color.RED));
        list.add(new Card(Card.Name.Samurai, Card.Color.BLUE));
        list.add(new Card(Card.Name.Samurai, Card.Color.GREEN));

        list.add(new Card(Card.Name.Ronin, Card.Color.RED));
        list.add(new Card(Card.Name.Ronin, Card.Color.RED));
        list.add(new Card(Card.Name.Ronin, Card.Color.BLUE));
        list.add(new Card(Card.Name.Ronin, Card.Color.BLUE));
        list.add(new Card(Card.Name.Ronin, Card.Color.GREEN));
        list.add(new Card(Card.Name.Ronin, Card.Color.GREEN));

        list.add(new Card(Card.Name.Doctor, Card.Color.RED));
        list.add(new Card(Card.Name.Doctor, Card.Color.RED));
        list.add(new Card(Card.Name.Doctor, Card.Color.BLUE));
        list.add(new Card(Card.Name.Doctor, Card.Color.BLUE));
        list.add(new Card(Card.Name.Doctor, Card.Color.GREEN));
        list.add(new Card(Card.Name.Doctor, Card.Color.GREEN));

        list.add(new Card(Card.Name.Monk, Card.Color.BLACK));

        list.add(new Card(Card.Name.Courtier, Card.Color.RED));
        list.add(new Card(Card.Name.Courtier, Card.Color.RED));
        list.add(new Card(Card.Name.Courtier, Card.Color.BLUE));
        list.add(new Card(Card.Name.Courtier, Card.Color.BLUE));
        list.add(new Card(Card.Name.Courtier, Card.Color.GREEN));
        list.add(new Card(Card.Name.Courtier, Card.Color.GREEN));

        list.add(new Card(Card.Name.Actor, Card.Color.RED));
        list.add(new Card(Card.Name.Actor, Card.Color.RED));
        list.add(new Card(Card.Name.Actor, Card.Color.BLUE));
        list.add(new Card(Card.Name.Actor, Card.Color.BLUE));
        list.add(new Card(Card.Name.Actor, Card.Color.GREEN));
        list.add(new Card(Card.Name.Actor, Card.Color.GREEN));

        list.add(new Card(Card.Name.Daimyo, Card.Color.RED));
        list.add(new Card(Card.Name.Daimyo, Card.Color.BLUE));
        list.add(new Card(Card.Name.Daimyo, Card.Color.GREEN));

        list.add(new Card(Card.Name.Merchant, Card.Color.RED));
        list.add(new Card(Card.Name.Merchant, Card.Color.RED));
        list.add(new Card(Card.Name.Merchant, Card.Color.BLUE));
        list.add(new Card(Card.Name.Merchant, Card.Color.BLUE));
        list.add(new Card(Card.Name.Merchant, Card.Color.GREEN));
        list.add(new Card(Card.Name.Merchant, Card.Color.GREEN));

        list.add(new Card(Card.Name.Yakuza, Card.Color.RED));
        list.add(new Card(Card.Name.Yakuza, Card.Color.RED));
        list.add(new Card(Card.Name.Yakuza, Card.Color.BLUE));
        list.add(new Card(Card.Name.Yakuza, Card.Color.BLUE));
        list.add(new Card(Card.Name.Yakuza, Card.Color.GREEN));
        list.add(new Card(Card.Name.Yakuza, Card.Color.GREEN));

        list.add(new Card(Card.Name.Thief, Card.Color.RED));
        list.add(new Card(Card.Name.Thief, Card.Color.RED));
        list.add(new Card(Card.Name.Thief, Card.Color.BLUE));
        list.add(new Card(Card.Name.Thief, Card.Color.BLUE));
        list.add(new Card(Card.Name.Thief, Card.Color.GREEN));
        list.add(new Card(Card.Name.Thief, Card.Color.GREEN));

        return list;
    }

    private static State changeStateAfterAction (State state) {

        /* If someone used effect against another player */
        if (state.special_turn) {
            return state;
        }

        switch (state.getLastAppliedAction().getName()) {
            case GuestEffect: {
                /* When a doctor effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Doctor) {
                    String turning_player = state.getTurnPlayer().getName();

                    /* Delete Akenohoshi bonus */
                    if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi &&
                            state.getTurnPlayer().getGeishaUsages() > 0) {
                        state.getTurnPlayer().setAkenohoshiBonus(null);
                        state = state.nextTurn();
                        for (Player p : state.getPlayers()) {
                            if (p.getName().equals(turning_player)) {
                                state.setTurnPlayer(p);
                            }
                        }
                        return state;
                    }

                    state = state.nextTurn();
                    for (Player p : state.getPlayers()) {
                        if (p.getName().equals(turning_player)) {
                            state.setTurnPlayer(p);
                        }
                    }
                    return state;
                }

                /* When an okaasan effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Okaasan) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Advertiser);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                /* When a courtier effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Courtier) {
                    state.use_allowed_actions = true;
                    state.allowed_color = state.getLastAppliedAction().getCard1().getColor();
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Guest);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                break;
            }
            case Guest: {
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Sumo_Wrestler) {
                    Player target = null;
                    int max_cards = -1;
                    for (Player p : state.getPlayers()) {
                        if (p.getHand().size() > max_cards) {
                            max_cards = p.getHand().size();
                            target = p;
                        }
                    }

                    state.sumo_player = target;
                    for (Card c : target.getHand()) c.is_known = true;

                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    return state;
                } else {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }
            }
            case Geisha: {
                /* Akenohoshi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi) {
                    return state;
                }

                /* Suzune */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Suzune) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Advertiser);
                    return state;
                }

                /* Momiji */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji) {
                    if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Sumo_Wrestler) {
                        Player target = null;
                        int max_cards = -1;
                        for (Player p : state.getPlayers()) {
                            if (p.getHand().size() > max_cards) {
                                max_cards = p.getHand().size();
                                target = p;
                            }
                        }

                        state.sumo_player = target;
                        for (Card c : target.getHand()) c.is_known = true;

                        state.use_allowed_actions = true;
                        state.allowed_actions.clear();
                        state.allowed_actions.add(Action.Name.GuestEffect);
                        return state;
                    } else {
                        state.use_allowed_actions = true;
                        state.allowed_actions.clear();
                        state.allowed_actions.add(Action.Name.GuestEffect);
                        state.allowed_actions.add(Action.Name.EndTurn);
                        return state;
                    }
                }

                /* Natsumi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.Guest);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
                }

                break;
            }
            case Advertiser: {
                /* Harukaze */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Harukaze) {
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.HarukazeDiscard);
                    switch (state.getDrawDeck().size()) {
                        case 0: {
                            state.allowed_color = Card.Color.RED;
                            break;
                        }
                        case 1: {
                            state.allowed_color = Card.Color.BLUE;
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            break;
                        }
                        default: {
                            state.allowed_color = Card.Color.GREEN;
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            state.getTurnPlayer().addCard(state.getRandomCard());
                            break;
                        }
                    }
                    return state;
                }

                break;
            }
            case EndTurn: case HarukazeDiscard: {
                /* Natsumi */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
                    if (state.isApplicableAction(new Action(
                            Action.Name.Geisha,
                            state.getTurnPlayer(),
                            state.getLastAppliedAction().getCard1(),
                            null,
                            null,
                            null
                    ))) {
                        state.use_allowed_actions = true;
                        state.allowed_actions.clear();
                        state.allowed_color = Card.Color.BLUE;
                        state.allowed_actions.add(Action.Name.Geisha);
                        state.allowed_actions.add(Action.Name.EndTurn);
                        return state;
                    }
                }

                /* Delete Akenohoshi bonus */
                if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi &&
                        state.getTurnPlayer().getGeishaUsages() > 0) {
                    state.getTurnPlayer().setAkenohoshiBonus(null);
                    state = state.nextTurn();
                    return state;
                }

                state = state.nextTurn();
                return state;
            }
        }

        /* Momiji */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Momiji) {
            if (state.isApplicableAction(new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    state.getLastAppliedAction().getCard1(),
                    null,
                    null,
                    null
            ))) {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_color = Card.Color.RED;
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Natsumi */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Natsumi) {
            if (state.isApplicableAction(new Action(
                    Action.Name.Geisha,
                    state.getTurnPlayer(),
                    state.getLastAppliedAction().getCard1(),
                    null,
                    null,
                    null
            ))) {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_color = Card.Color.BLUE;
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Suzune */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Suzune) {
            boolean geisha_was_used = false;
            for (Action act : state.getAppliedActions()) {
                if (act.getName() == Action.Name.Geisha) {
                    geisha_was_used = true;
                    break;
                }
            }
            if (geisha_was_used) {
                if (state.getAppliedActions().get(0).getName() == Action.Name.Geisha) {
                    if (state.getAppliedActions().size() == 2) {
                        state.use_allowed_actions = true;
                        ArrayList<Action.Name> names = state.getAllActions();
                        names.remove(Action.Name.Geisha);
                        state.allowed_actions.clear();
                        state.allowed_actions.addAll(names);
                        return state;
                    }
                } else {
                    state = state.nextTurn();
                    return state;
                }
            } else {
                state.use_allowed_actions = true;
                state.allowed_actions.clear();
                state.allowed_actions.add(Action.Name.Geisha);
                state.allowed_actions.add(Action.Name.EndTurn);
                return state;
            }
        }

        /* Delete Akenohoshi bonus */
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi &&
                state.getTurnPlayer().getGeishaUsages() > 0) {
            state.getTurnPlayer().setAkenohoshiBonus(null);
            state = state.nextTurn();
            return state;
        }

        /* Next turn as usual */
        state = state.nextTurn();
        return state;
    }

}
