import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GameView {
    public static Stage window;
    public static Parent root;

    public static final int windowWidth = 1040;
    public static final int windowHeight = 690;

    public static int playerIndex;
    public static State state;
    public static GameGraphics gg;
    private ArrayList<Card> deck;

    //todo GameView -- basically, Main. SetupView only calls one function from here, GameGraphics depends entirely.
    GameView () {
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

        //todo THIS IS WHERE STATE IS CREATED

        playerIndex = SetupView.playerIndex;

        /*playerNames = new ArrayList<>();
        playerNames.addAll(SetupView.players);*/

        deck = Main.deckFill();
        ArrayList<Player> players = new ArrayList<>();

        for (int i = 0; i < SetupView.players.size(); i++) {
            //String aiType = SetupView.aiType[i] == 0 ? "HUMAN" : (SetupView.aiType[i] == 1 ? "ISMCTS" : "RANDOM");

            ArrayList<Card> cards = new ArrayList<>();
            for (String j : SetupView.playerCards.get(i)) {//int j = 0; j < SetupView.playerCards.get(i).size(); j++) {
                cards.add(
                        findCard(
                                CardsNames.valueOf(SetupView.getCardName(j).replace(" ", "_")),
                                Colors.valueOf(SetupView.getCardColor(j))
                        )
                );
            }

            players.add(new Player(SetupView.players.get(i), cards, getGeisha(GeishasName.valueOf(SetupView.playerGeishas.get(i)))));
            players.get(players.size()-1).setType(SetupView.aiType[i] == 0 ? PlayerType.Human : (SetupView.aiType[i] == 1 ? PlayerType.ISMCTS : PlayerType.RandomAI));
        }

        state = new State(players, deck, 0);
        //State initial_state = new State(players, deck, 0);

        //todo THIS IS WHERE THE GRAPHICS ARE LOADED
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameGraphics.fxml"));
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
        } finally {
            window.show();

            Main.loop();
        }
    }

    //todo VERY INEFFICIENT WAY TO HANDLE CARD SEARCH
    public Card findCard(CardsNames name, Colors color) {
        for (Card i : deck) {
            if (i.name == name && i.color == color) {
                deck.remove(i);
                return i;
            }
        }

        return null;
    }

    //todo CREATES GEISHA BY HER NAME
    public Geisha getGeisha(GeishasName name) {
        int effect = 1;
        HashMap<Colors, Integer> reputation = new HashMap<>();
        switch (name.toString()) {
            case "Momiji":
                reputation.put(Colors.Red, 5);
                reputation.put(Colors.Blue, 1);
                reputation.put(Colors.Green, 3);
                break;
            case "Akenohoshi":
                reputation.put(Colors.Red, 3);
                reputation.put(Colors.Blue, 3);
                reputation.put(Colors.Green, 3);
                break;
            case "Suzune":
                reputation.put(Colors.Red, 2);
                reputation.put(Colors.Blue, 2);
                reputation.put(Colors.Green, 2);
                break;
            case "Natsumi":
                reputation.put(Colors.Red, 3);
                reputation.put(Colors.Blue, 5);
                reputation.put(Colors.Green, 1);
                break;
            case "Oboro":
                reputation.put(Colors.Red, 5);
                reputation.put(Colors.Blue, 5);
                reputation.put(Colors.Green, 5);
                effect = 0;
                break;
            case "Harukaze":
                reputation.put(Colors.Red, 1);
                reputation.put(Colors.Blue, 3);
                reputation.put(Colors.Green, 5);
                break;
        }

        return new Geisha(name, reputation, effect);
    }
}
