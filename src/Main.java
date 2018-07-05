import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static Stage window;
    public static Parent root;

    public static final int windowWidth = 1040;
    public static final int windowHeight = 690;

    public static int playerIndex;
    public static Player mainPlayer;
    public static GameGraphics gg;
    public static State state;
    public static int[][] score;

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

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("gameGraphics.fxml"));
            root = loader.load();
            gg = loader.getController();
            //gg.playGuestButton.fireEvent(new ActionEvent());

            window.setMinWidth(windowWidth);
            window.setWidth(windowWidth);
            window.setMinHeight(windowHeight);
            window.setHeight(windowHeight);

            window.setScene(new Scene(root, windowWidth, windowHeight));
        } catch (IOException e) {
            System.out.println(e + "\nERROR: couldn't load 'gameGraphics.fxml'");
        }

        window.show();

        main(new String[0]);
    }

    public static void main(String[] args) {
        playerIndex = SetupView.playerIndex;

        score = new int[SetupView.players.size()][3];

        /* Round management */
        for (int round = 0; round < 3; round++) {

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
                    for (String j : SetupView.playerCards.get(i)) {//int j = 0; j < SetupView.playerCards.get(i).size(); j++) {
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

            gg.initialize();

            Platform.runLater(() -> loop());
        }
    }

    public static void loop () {
        AlgorithmISMCTS ISMCTS = new AlgorithmISMCTS();
        AlgorithmRandom RANDOM = new AlgorithmRandom();

        // Run the game loop
        while (!state.isTerminal()) {

            /* Makes a decision how to move */
            switch (state.getTurnPlayer().getType()) {
                case ISMCTS: {
                    state = state.applyAction(ISMCTS.getAction(state));
                    break;
                }
                case Random: {
                    state = state.applyAction(RANDOM.getAction(state));
                    break;
                }
                case Human: {
                    break;
                }
            }

            try {
                state = changeStateAfterAction(state);
            } catch (IOException e) {
                System.out.println(e + "\nERROR: Failed to save data to the .log file.");
            }

        }

        // Print turn information
        System.out.println("--------------------------------------------");
        System.out.println("Round " + (state.getRound() + 1) + ", turn " + (state.getTurn() + 1) + ":");
        System.out.println();
        System.out.println("Turn player: " + state.getTurnPlayer().toString());
        System.out.println();
        for (Action act : state.getAppliedActions()) {
            System.out.println(act.toString());
        }

        // Get scores
        System.out.println("--------------------------------------------");
        for (Player p : state.getPlayers()) {
            System.out.println(p.getName() + "'s score: " + p.getScore());
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

    private static State changeStateAfterAction (State state) throws IOException {

        /* If someone used effect against another player */
        if (state.special_turn) {
            return state;
        }

        switch (state.getLastAppliedAction().getName()) {
            case GuestEffect: {

                /* When a doctor effect was taken */
                if (state.getLastAppliedAction().getCard1().getName() == Card.Name.Doctor) {
                    String turning_player = state.getTurnPlayer().getName();
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
                    Player target = state.getTurnPlayer();
                    int max_cards = target.getHand().size();
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
                    state.use_allowed_actions = true;
                    state.allowed_actions.clear();
                    state.allowed_actions.add(Action.Name.GuestEffect);
                    state.allowed_actions.add(Action.Name.EndTurn);
                    return state;
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
        if (state.getTurnPlayer().getGeisha().getName() == Geisha.Name.Akenohoshi) {
            state.getTurnPlayer().setAkenohoshiBonus(null);
            state = state.nextTurn();
            return state;
        }

        /* Next turn as usual */
        state = state.nextTurn();
        return state;
    }

}
