import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.*;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class SetupView extends Application {

    Stage window;
    private Scene scene;
    private ArrayList<String> players;
    private boolean[] isHuman;
    //private GridPane setupCard;
    private ImageView setupCard;

    public static final int windowWidth = 940;
    public static final int windowHeight = 660;

    public static ArrayList<String> cardImagesNames;
    private String[] cardSelection = {"Actor", "Courtier", "Doctor", "Emissary", "Merchant", "Okaasan",
            "Ronin", "Scholar", "Sumo Wrestler", "Thief", "Yakuza",
            "Daimyo", "Samurai", "District Kanryou", "Monk", "Shogun"};

    public static final String[] geishasDeck = {
            "Momiji", "Akenohoshi", "Suzune", "Natsumi", "Harukaze", "Oboro"};
    private ArrayList<String> geishasRemaining;

    private String[] cardsDeck;
    private ArrayList<String> cardsRemaining;

    public ArrayList<String> playerGeishas;
    public ArrayList< ArrayList<String> > playerCards;
    public ArrayList<String> heapCards;
    private ListView[] playerHand;
    private int editingIndex;

    public static void main (String[] args) {
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Mai-Star --Setup Example");

        window.setMinWidth(windowWidth);
        window.setMinHeight(windowHeight);

        cardImagesNames = new ArrayList<>();
        File file = new File("cards");
        try {
            if (!file.exists()) file.mkdir();
            for (final File fileEntry : file.listFiles()) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith(".png"))
                    cardImagesNames.add(fileEntry.getName().replace(".png", ""));
                    //files.add(fileEntry.getName());
            }
        } catch (NullPointerException e) {
            System.out.println(e + "\nERROR: 'cards' folder could not be found.");
        }

        players = new ArrayList<>();
        isHuman = new boolean[6];
        isHuman[0] = true;

        playerGeishas = new ArrayList<>();
        playerCards = new ArrayList<>();

        geishasRemaining = new ArrayList<>();
        cardsRemaining = new ArrayList<>();
        editingIndex = -1;

        ArrayList<Card> deck = new ArrayList<>();
        deck.addAll(Main.deckFill());

        cardsDeck = new String[deck.size()];
        for (int i = 0; i < deck.size(); i++) {
            cardsDeck[i] = deck.get(i).color + "_" + deck.get(i).name.toString().replace("_", " ");
        }

        cardsRemaining.clear();
        cardsRemaining.addAll(Arrays.asList(cardsDeck));

        geishasRemaining.clear();
        geishasRemaining.addAll(Arrays.asList(geishasDeck));

        setScene1();

        window.show();
    }

    private void setScene1 () {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));

        Label playerCountLabel = new Label("Number of players: ");

        ChoiceBox<Integer> playerCount = new ChoiceBox<>();
        playerCount.getItems().addAll(3, 4, 5, 6);
        playerCount.getSelectionModel().select(players.size() <= 3 ? 0 : players.size() - 3);

        HBox playerCountBox = new HBox(10);
        playerCountBox.setAlignment(Pos.BASELINE_LEFT);
        playerCountBox.getChildren().addAll(playerCountLabel, playerCount);

        box.getChildren().add(playerCountBox);

        HBox[] playerBox = new HBox[6];

        Label[] playerNameLabel = new Label[6];
        TextField[] playerName = new TextField[6];
        ChoiceBox<String>[] playerType = new ChoiceBox[6];

        HBox[] playerNamesBox = new HBox[2];
        playerNamesBox[0] = new HBox(10);
        playerNamesBox[0].setAlignment(Pos.BASELINE_LEFT);
        playerNamesBox[1] = new HBox(10);
        playerNamesBox[1].setAlignment(Pos.BASELINE_LEFT);

        for (int i = 0; i < 6; i++) {
            if (players.size() <= i) {
                players.add("");
            }
            playerNameLabel[i] = new Label("Name " + (i+1) + ": ");

            playerName[i] = new TextField(players.get(i));
            playerName[i].setPromptText("Player " + (i+1));
            playerName[i].setTooltip(new Tooltip("You can leave this field blank"));

            playerType[i] = new ChoiceBox<>();
            playerType[i].getItems().addAll("Human", "AI");
            playerType[i].getSelectionModel().select(isHuman[i] ? 0 : 1);

            playerBox[i] = new HBox(5);
            playerBox[i].setAlignment(Pos.BASELINE_LEFT);

            playerBox[i].getChildren().addAll(playerNameLabel[i], playerName[i], playerType[i]);
            playerNamesBox[i / 3].getChildren().add(playerBox[i]);
        }

        box.getChildren().addAll(new Separator(), playerNamesBox[0], playerNamesBox[1]);

        Button beginGame = new Button("Start Game");
        beginGame.setDisable(true);

        Button setState = new Button("Advanced...");
        setState.setDefaultButton(true); //beginGame

        HBox buttonsBox = new HBox(50);
        buttonsBox.getChildren().addAll(beginGame, setState);
        buttonsBox.setAlignment(Pos.CENTER);

        box.getChildren().addAll(new Separator(), buttonsBox);

        playerCount.setOnAction(e -> {
            if (playerCount.getValue() == 3) playerNamesBox[1].setManaged(false);
            else playerNamesBox[1].setManaged(true);
            for (int i = players.size()-1; i >= playerCount.getValue(); i--) {
                playerBox[i].setManaged(false);
                playerBox[i].setVisible(false);
                players.remove(players.size()-1);
            }
            for (int i = players.size(); i < playerCount.getValue(); i++) {
                playerBox[i].setManaged(true);
                playerBox[i].setVisible(true);
                players.add("");
            }
        });

        setState.setOnAction(e -> {
            //save names, set to player+i if empty
            for (int i = 0; i < playerCount.getValue(); i++) {
                players.set(i, !playerName[i].getText().equals("") ? playerName[i].getText().trim() : playerName[i].getPromptText());
                isHuman[i] = !playerType[i].getValue().equals("AI");
            }

            //throw names, return, continue to phase 2
            setScene2();
        });

        beginGame.setOnAction(e -> {
            //save names, set to player+i if empty
            for (int i = 0; i < playerCount.getValue(); i++) {
                players.set(i, !playerName[i].getText().equals("") ? playerName[i].getText().trim() : playerName[i].getPromptText());
                isHuman[i] = !playerType[i].getValue().equals("AI");
            }

            //throw names, return, continue to simulation
        });

        playerCount.fireEvent(new ActionEvent());

        scene = new Scene(box, windowWidth, windowHeight);
        window.setScene(scene);
    }

    private void setScene2 () {
        VBox box = new VBox(20);
        box.setPadding(new Insets(20));

        playerHand = new ListView[6];

        Button back = new Button("Back");
        back.setCancelButton(true);
        back.setOnAction(e -> {
            loadHandsToArrays();
            setScene1();
        });

        box.getChildren().add(back);

        ChoiceBox<String> cardType = new ChoiceBox<>();
        cardType.setMinWidth(120);
        cardType.getItems().addAll(cardSelection);
        cardType.getSelectionModel().select(0);

        ChoiceBox<String> cardColor = new ChoiceBox<>();
        cardColor.setMinWidth(70);
        cardColor.getItems().addAll("Red", "Blue", "Green"); //"Black"
        cardColor.getSelectionModel().select(0);

        HBox setupCardButtons = new HBox(15);
        setupCardButtons.getChildren().addAll(cardType, cardColor);

        Label cardsLeft = new Label();
        cardsLeft.setFont(Font.font("", FontWeight.BOLD, cardsLeft.getFont().getSize()));

        Label typeCardsLeft = new Label();
        typeCardsLeft.setFont(Font.font("", FontWeight.BOLD, typeCardsLeft.getFont().getSize()));

        setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));

        HBox deckInfoBox = new HBox(10);
        deckInfoBox.getChildren().addAll(cardsLeft, typeCardsLeft);

        Button savePresetButton = new Button("Save this preset...");
        //TODO savePresetButton.setTooltip(new Tooltip(""));
        savePresetButton.setOnAction(e -> {
            setPresetScene("Save");
            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        Button loadPresetButton = new Button("Load preset...");
        //TODO loadPresetButton.setTooltip(new Tooltip(""));
        loadPresetButton.setOnAction(e -> {
            setPresetScene("Load");
            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        HBox presetsButtons = new HBox(15);
        presetsButtons.getChildren().addAll(savePresetButton, loadPresetButton);

        Button helpButton = new Button("Help");
        helpButton.setOnAction(e -> {
            File file = new File("rules.pdf");
            try {
                if (file.exists())
                    Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                System.out.println(ex + "\nERROR: Couldn't open 'rules.pdf'.");
            }
            //setHelpScene();
            //add link to the rules
        });

        setupCard = buildCard(cardType.getValue(), cardColor.getValue());

        VBox setupButtons = new VBox(13);
        setupButtons.setPadding(new Insets(0, 0, 0, 10));
        setupButtons.getChildren().addAll(deckInfoBox, setupCardButtons,
                new Separator(), presetsButtons,
                new Separator(), helpButton,
                new Separator(), setupCard);

        //box.getChildren().add(deckInfoBox);

        HBox cardSetupBox = new HBox(20);
        final Pane leftSpacer = new Pane();
        HBox.setHgrow(leftSpacer, Priority.SOMETIMES);
        cardSetupBox.getChildren().addAll(setupButtons, leftSpacer);

        cardType.setOnAction(e -> {
            if (cardType.getValue() == null || cardColor.getValue() == null) return;
            if (cardType.getValue().equals("District Kanryou") || cardType.getValue().equals("Monk") || cardType.getValue().equals("Shogun")) {
                cardColor.getItems().clear();
                cardColor.getItems().add("Black");
                cardColor.getSelectionModel().select(0);
            } else if (cardColor.getValue().equals("Black")
                    || cardColor.getValue().equals("Geisha") && cardType.getItems().size() > 6) {
                cardColor.getItems().clear();
                cardColor.getItems().addAll("Red", "Blue", "Green");
                cardColor.getSelectionModel().select(0);
            }

            setupButtons.getChildren().remove(setupCard);
            setupCard = buildCard(cardType.getValue(), cardColor.getValue());
            setupButtons.getChildren().add(setupCard);

            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        cardColor.setOnAction(e -> {
            if (cardType.getValue() == null || cardColor.getValue() == null) return;
            setupButtons.getChildren().remove(setupCard);
            setupCard = buildCard(cardType.getValue(), cardColor.getValue());
            setupButtons.getChildren().add(setupCard);

            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        HBox centerBox = new HBox(20);
        centerBox.getChildren().add(cardSetupBox);

        //Button back
        //Place card to the right, make it look good
        //TableView
        //Geishas - click on geisha to change it, exchange geishas with another player if dragged
        //Add card to table
        //Add card to hand - max. # cards in hand + 2, if less rows, add one more row
        //Cards have colors in the table
        //Click to show card
        //Double click to remove card
        //Save preset, load preset
        //Clear, Random, Start
        //Help
        //At least one card should be in each hand to start with
        //Can add the card if only the card is in the deck, remove from deck/put a mark on it
        //Player hands are updated on exits from the scene
        //Add 'Disable Oboro's special ability on start' checkbox
        //Currently Editing: Player 1
        //Heap

        //Possibly, one click to select, double click to show? (also, double click for geisha can select edit automatically)
        //Display if the card is not available on preview

        VBox[] handBox = new VBox[6];
        HBox[] playerHandBox = new HBox[6];
        Label[] playerName = new Label[6];
        VBox[] handOptions = new VBox[6];

        HBox playerHandsBoxesBox = new HBox(20);
        playerHandsBoxesBox.setAlignment(Pos.BASELINE_RIGHT);
        VBox[] playerHandsBoxes = new VBox[2];
        playerHandsBoxes[0] = new VBox(20);
        playerHandsBoxes[0].setAlignment(Pos.CENTER_RIGHT);
        playerHandsBoxes[1] = new VBox(20);
        playerHandsBoxes[1].setAlignment(Pos.CENTER_RIGHT);

        for (int i = 0; i < 6; i++) {
            Button addCardButton = new Button("ADD");
            addCardButton.setTooltip(new Tooltip("Add the card in buffer to this hand"));

            Button editCardButton = new Button("EDIT");
            editCardButton.setTooltip(new Tooltip("Replace selected card with another currently shown on the screen"));
            editCardButton.setOnAction(e -> {
            });
            Button deleteCardButton = new Button("DELETE");
            deleteCardButton.setTooltip(new Tooltip("Delete selected card from this hand"));
            deleteCardButton.setOnAction(e -> {
            });
            Button clearCardsButton = new Button("CLEAR");
            clearCardsButton.setTooltip(new Tooltip("Discard the hand, leaving only Geisha"));
            clearCardsButton.setOnAction(e -> {
            });
            Button randomizeCardsButton = new Button("RAND.");
            randomizeCardsButton.setTooltip(new Tooltip("Discard this hand and pick 5 random cards"));

            // Set tooltips, actions
            handOptions[i] = new VBox(2);
            handOptions[i].getChildren().addAll(addCardButton, editCardButton, deleteCardButton, clearCardsButton, randomizeCardsButton);

            playerHand[i] = new ListView<String>();
            playerHand[i].setPrefSize((
                            windowWidth - box.getPadding().getLeft() - box.getPadding().getRight()) / 4,
                    windowHeight / (2 + 2 * players.size() / 3)
            );

            playerName[i] = new Label();
            playerName[i].setFont(Font.font("Comic Sans", FontWeight.BOLD, playerName[i].getFont().getSize()));
            playerName[i].setAlignment(Pos.CENTER);

            playerHandBox[i] = new HBox(5);
            playerHandBox[i].getChildren().addAll(playerHand[i], handOptions[i]);

            handBox[i] = new VBox(5);
            handBox[i].setAlignment(Pos.BASELINE_LEFT);

            if (i < players.size()) {
                playerName[i].setText(players.get(i) + (!isHuman[i] ? " [AI]" : ""));
            } else {
                handBox[i].setManaged(false);
                handBox[i].setVisible(false);
            }

            handBox[i].getChildren().addAll(playerName[i], playerHandBox[i]);
            playerHandsBoxes[i / 3].getChildren().add(handBox[i]);

            int j = i;

            playerHand[i].setCellFactory(c -> {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle(null);
                            setFont(Font.font(null));
                        } else {
                            setAlignment(Pos.CENTER);
                            if (!item.contains("_")/*Card.type.equals("Geisha")*/) {
                                setText(item);
                                setFont(Font.font("Comic Sans", FontPosture.ITALIC, getFont().getSize()));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: gold;");

                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() > 1) {
                                        if (editingIndex != -1) {
                                            swapGeishas(cardType, j);
                                        } else if (focusOnGeisha(cardType, cardColor, j)) {
                                            cardType.getSelectionModel().select(cardType.getItems().indexOf(getText()));
                                        } else {
                                            cardType.getItems().clear();
                                            cardType.getItems().addAll(cardSelection);
                                            cardType.getSelectionModel().select(0);
                                        }
                                    }
                                });
                            } else {
                                setText(getCardName(item));
                                if (getText().equals("District Kanryou"))
                                    setFont(Font.font("Comic Sans", FontPosture.ITALIC, getFont().getSize()));
                                else
                                    setFont(Font.font("Comic Sans"));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: " + getCssColor(getCardColor(item)) + ";");

                                setOnMouseClicked(e -> {
                                    if (e.getClickCount() > 1) {
                                        editingIndex = -1;
                                        
                                        if (cardColor.getItems().contains("Geisha")) {
                                            cardType.getItems().clear();
                                            cardType.getItems().addAll(cardSelection);
                                            cardType.getSelectionModel().select(0);
                                        }
                                        cardType.getSelectionModel().select(cardType.getItems().indexOf(getText()));
                                        cardColor.getSelectionModel().select(cardColor.getItems().indexOf(item.substring(0, item.indexOf("_"))));

                                        setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
                                    }
                                    /*System.out.println(cardsRemaining.size() + "\n" +
                                            cardColor.getValue().concat("_").concat(cardType.getValue()) + "\n" +
                                            cardsRemaining.contains(cardColor.getValue().concat("_").concat(cardType.getValue()))
                                    );*/
                                });
                            }
                        }
                    }
                };
                return cell;
            });

            addCardButton.setOnAction(e -> {
                //TODO --DECK-- -DONE
                if (editingIndex == -1 && !cardColor.getItems().contains("Geisha")) {
                    if (!cardsRemaining.contains(cardColor.getValue().concat("_").concat(cardType.getValue()))) return;
                    playerHand[j].getItems().add(cardsRemaining.remove(
                            cardsRemaining.indexOf(
                                    cardColor.getValue().concat("_").concat(cardType.getValue()))));
                } else swapGeishas(cardType, j);

                setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
            });

            editCardButton.setOnAction(e -> {
                if (editingIndex == -1 && !playerHand[j].getSelectionModel().getSelectedItems().isEmpty()) {
                    if (playerHand[j].getSelectionModel().getSelectedItems().get(0).toString().contains("_")) {
                        if (!discardFocusOnGeisha(cardType, cardColor.getValue())) {
                            /*TODO if not present in the --DECK--, return; -DONE*/
                            if (!cardsRemaining.contains(cardColor.getValue().concat("_").concat(cardType.getValue()))) return;

                            cardsRemaining.add(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString());
                            playerHand[j].getItems().set(playerHand[j].getItems().indexOf(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString()),
                                    cardsRemaining.remove(
                                            cardsRemaining.indexOf(
                                                    cardColor.getValue().concat("_").concat(cardType.getValue()))));
                        }

                        setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
                        return;
                    }
                }

                if (!focusOnGeisha(cardType, cardColor, j))
                    swapGeishas(cardType, j);

                setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
            });

            deleteCardButton.setOnAction(e -> {
                // TODO --DECK-- move them to the deck -DONE
                ObservableList m = playerHand[j].getSelectionModel().getSelectedItems();
                for (int k = 0; k < playerHand[j].getSelectionModel().getSelectedItems().size(); k++) {
                    if (((String) m.get(k)).contains("_")) {
                        cardsRemaining.add(m.get(k).toString());
                        playerHand[j].getItems().remove(m.get(k));
                    }
                }

                setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
            });

            clearCardsButton.setOnAction(e -> {
                discardFocusOnGeisha(cardType, cardColor.getValue());
                clearHands(j);

                setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
            });

            randomizeCardsButton.setOnAction(e -> {
                discardFocusOnGeisha(cardType, cardColor.getValue());
                randomizeHands(j);

                setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
            });
        }
        nestHandsInInput();
        //if (players.size() <= 3) playerHandsBoxes[1].setManaged(false);

        playerHandsBoxesBox.getChildren().addAll(playerHandsBoxes);

        //VBox rightBox = new VBox(20);
        centerBox.getChildren().addAll(playerHandsBoxesBox);
        box.getChildren().add(centerBox);

        Button startButton = new Button("START GAME");
        startButton.setDefaultButton(true);

        Button clearHandsButton = new Button("Clear Hands");
        clearHandsButton.setTooltip(new Tooltip("Discard all players' cards except for Geishas"));

        Button randomizeHandsButton = new Button("Randomize Hands");
        randomizeHandsButton.setTooltip(new Tooltip("Discard all players' hands and place random 5 cards into each of them"));

        startButton.setOnAction(e -> {
            discardFocusOnGeisha(cardType, cardColor.getValue());

            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));

            if (!loadHandsToArrays()) return;
            for (int i = 0; i < players.size(); i++) {
                if (playerCards.get(i).size() == 0) {
                    //TODO WARNING
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "You cannot have empty hands for players! That would violate the rules of the game." +
                                    "\n\nPlease, ensure you have at least one card apart from Geisha in each of your hands.");
                    alert.showAndWait();
                    return;
                }
            }
            // runAlgorithm();
            // TODO throw names, check if all hands have at least two cards (Geisha and one more)
        });

        clearHandsButton.setOnAction(e -> {
            discardFocusOnGeisha(cardType, cardColor.getValue());
            clearHands();

            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        randomizeHandsButton.setOnAction(e -> {
            discardFocusOnGeisha(cardType, cardColor.getValue());
            randomizeHands();

            setDeckLabels(cardsLeft, typeCardsLeft, cardColor.getValue().concat("_".concat(cardType.getValue())));
        });

        HBox stateButtons = new HBox(20);
        stateButtons.setAlignment(Pos.CENTER);
        stateButtons.getChildren().addAll(clearHandsButton, startButton, randomizeHandsButton);
        //box.getChildren().addcenterBox);
        box.getChildren().add(stateButtons);

        scene = new Scene(box, windowWidth, windowHeight);
        window.setScene(scene);

        cardType.requestFocus();
    }

    private boolean focusOnGeisha (ChoiceBox<String> cardType, ChoiceBox<String> cardColor, int j) {
        if (!cardColor.getValue().equals("Geisha")) {
            cardType.getItems().clear();
            cardType.getItems().add(playerHand[j].getItems().get(0).toString());
            cardType.getItems().addAll(geishasRemaining);
            cardType.getSelectionModel().select(0);

            editingIndex = j;

            cardColor.getItems().clear();
            cardColor.getItems().add("Geisha");
            cardColor.getSelectionModel().select(0);

            return true;
        }

        return false;
    }

    private boolean discardFocusOnGeisha (ChoiceBox<String> cardType, String cardColor) {
        editingIndex = -1;
        if (cardColor.equals("Geisha")) {
            cardType.getItems().clear();
            cardType.getItems().addAll(cardSelection);
            cardType.getSelectionModel().select(0);
            return true;
        }

        return false;
    }

    private void swapGeishas (ChoiceBox<String> cardType, int j) {
        {
            if (editingIndex == -1) return;
            boolean isGeisha = false;
            for (String i : geishasDeck)
                if (cardType.getValue().equals(i)) isGeisha = true;
            if (!isGeisha) return;
            geishasRemaining.add((String)playerHand[j].getItems().get(0));
            if (playerHand[editingIndex].getItems().get(0).equals(cardType.getValue())) {
                playerHand[editingIndex].getItems().set(0, playerHand[j].getItems().get(0));
                geishasRemaining.remove(playerHand[editingIndex].getItems().get(0));
            }
            playerHand[j].getItems().set(0, cardType.getValue());
            geishasRemaining.remove(cardType.getValue());

            cardType.getItems().clear();
            cardType.getItems().addAll(cardSelection);
            cardType.getSelectionModel().select(0);

            editingIndex = -1;
        }
    }

    //private void setupPhase3 ()
    // assume phase 2 is but a starting state, then phase 3 is full-detail mid-game state. To-do later, expand phase 2 to phase 3 and limit phase 2

    /** Restore the deck fully and put a random geisha and random 5 cards into the hand of each player,
     * starting from the last */
    private void randomizeHands (int index, boolean leaveGeishas) {
        //TODO --DECK-- return all cards to the deck -DONE
        int i = index;
        int min = i;
        if (index == -1) {
            i = players.size() - 1;
            min = 0;
        }

        for (int j = i; j >= min; j--) {
            if (!leaveGeishas) geishasRemaining.add(playerHand[j].getItems().get(0).toString());
            for (int k = 1; k < playerHand[j].getItems().size(); k++) {
                cardsRemaining.add(playerHand[j].getItems().get(k).toString());
            }
        }

        for (; i >= min; i--) {
            if (!leaveGeishas) {
                playerHand[i].getItems().clear();
                playerHand[i].getItems().add(geishasRemaining.remove(new Random().nextInt(geishasRemaining.size())));
            } else {
                clearHands(i);
            }

            for (int k = 0; k <  5; k++) /*(playerHand[i].getItems().get(0).toString().equals("Oboro") ? 7 : )*/
                playerHand[i].getItems().add(cardsRemaining.remove(new Random().nextInt(cardsRemaining.size())));
        }
    }

    private void randomizeHands (int index) {
        randomizeHands(index, false);
    }

    private void randomizeHands (boolean leaveGeishas) {
        randomizeHands(-1, leaveGeishas);
    }

    private void randomizeHands () {
        randomizeHands(-1, false);
    }

    /** Delete all cards except for geishas in the hand and place them into the deck */
    private void clearHands (int index) {
        int i = index;
        int max = index + 1;
        if (index == -1) {
            i = 0;
            max = players.size();
        }

        for (; i < max; i++) {
            //TODO first move all cards to the --DECK-- -DONE
            for (int j = 1; j < playerHand[i].getItems().size(); j++)
                cardsRemaining.add(playerHand[i].getItems().get(j).toString());
            playerHand[i].getItems().remove(1, playerHand[i].getItems().size());
        }
    }

    private void clearHands () {
        clearHands(-1);
    }

    /** Transfer cards from ListView input form to readable array form */
    private boolean loadHandsToArrays() {
        playerGeishas.clear();
        playerCards.clear();

        ArrayList<String> geishasRemaining = new ArrayList<>();
        geishasRemaining.addAll(Arrays.asList(geishasDeck));
        ArrayList<String> cardsRemaining = new ArrayList<>();
        cardsRemaining.addAll(Arrays.asList(cardsDeck));

        try {
            for (int i = 0; i < players.size(); i++) {
                playerCards.add(new ArrayList<>());
                playerGeishas.add
                        (geishasRemaining.remove(
                                geishasRemaining.indexOf(playerHand[i].getItems().get(0).toString())
                ));
                for (int j = 1; j < playerHand[i].getItems().size(); j++) {
                    playerCards.get(i).add(cardsRemaining.remove(
                            cardsRemaining.indexOf(playerHand[i].getItems().get(j).toString())));
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            System.out.println(exception + "\nERROR: loadHands - inconsistent input. Rolling to default.");

            for (int i = 0; i < players.size(); i++)
                playerHand[i].getItems().clear();
            playerGeishas.clear();
            playerCards.clear();

            Alert alert = new Alert(Alert.AlertType.ERROR, "Oops! It seems like you have previously entered incorrect input. Rolling back...");
            alert.showAndWait();

            nestHandsInInput();
            loadHandsToArrays(); //TODO delete?

            return false;
        }
        return true;
        // TODO check that nothing goes against rules -> all cards are part of the deck
    }

    /** Transfer all cards from arrays into input form of ListView */
    private boolean nestHandsInInput() {
        geishasRemaining.clear();
        geishasRemaining.addAll(Arrays.asList(geishasDeck));

        cardsRemaining.clear();
        cardsRemaining.addAll(Arrays.asList(cardsDeck));

        try {
            for (int i = 0; i < players.size(); i++) {
                playerHand[i].getItems().clear();
                playerHand[i].getItems().add(geishasRemaining.remove(
                        playerGeishas.size() > i && !playerGeishas.get(i).isEmpty()
                                ? geishasRemaining.indexOf(playerGeishas.get(i))
                                : new Random().nextInt(geishasRemaining.size())
                ));
                if (playerCards.size() > i) {
                    for (int k = 0; k < playerCards.get(i).size(); k++) {
                        playerHand[i].getItems().add(cardsRemaining.remove(
                                cardsRemaining.indexOf(playerCards.get(i).get(k))));
                    }
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            System.out.println(exception + "\nERROR: nestHands - inconsistent input. Rolling to default.");

            for (int i = 0; i < players.size(); i++)
                playerHand[i].getItems().clear();
            playerGeishas.clear();
            playerCards.clear();

            Alert alert = new Alert(Alert.AlertType.ERROR, "Oops! It seems like you have entered incorrect input. Rolling back...");
            alert.showAndWait();

            nestHandsInInput();
            return false;
        }
        return true;
    }

    private ArrayList<String> loadFiles (String folder) {
        ArrayList<String> files = new ArrayList<>();
        File file = new File(folder);
        try {
            if (!file.exists()) file.mkdir();
            for (final File fileEntry : file.listFiles()) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith(".xml"))
                    files.add(fileEntry.getName());
            }
        } catch (NullPointerException e) {
            System.out.println(e + "\nERROR: 'presets' folder could not be found.");
        }
        return files;
    }

    private void setPresetScene (String operation) {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(operation + " Preset");
        //window.getIcons().add();
        window.setMinWidth(400);
        window.setMinHeight(400);

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        String folder = "presets";

        ListView<String> presets = new ListView<>();
        presets.getItems().addAll(loadFiles(folder));

        HBox presetsBox = new HBox();
        presetsBox.setAlignment(Pos.CENTER);
        presetsBox.getChildren().add(presets);

        TextField nameField = new TextField();

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            savePreset(nameField.getText(), folder);
            window.close();
        });

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> {
            loadPreset(nameField.getText(), folder);
            window.close();
        });

        HBox nameBox = new HBox(5);
        nameBox.setAlignment(Pos.CENTER);
        nameBox.getChildren().add(nameField);
        if (operation.equals("Save")) {
            saveButton.setDefaultButton(true);
            nameBox.getChildren().add(saveButton);
        } else {
            loadButton.setDefaultButton(true);
            nameBox.getChildren().add(loadButton);
        }

        Button renameButton = new Button("Rename");
        renameButton.setOnAction(e -> {
            if (presets.getSelectionModel().getSelectedItem() == null) return;
            File file = new File(folder + "/" + presets.getSelectionModel().getSelectedItem());

            String newName = nameField.getText().endsWith(".xml") //if not endsWith .xml
                    ? nameField.getText() : nameField.getText().concat(".xml");

            if (file.renameTo(new File(folder + "/" + newName))) {
                System.out.println("Rename successful");
                presets.getItems().clear();
                presets.getItems().addAll(loadFiles(folder));
                presets.getSelectionModel().select(presets.getItems().indexOf(newName));
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            if (presets.getSelectionModel().getSelectedItem() == null) return;
            File file = new File(folder + "/" + presets.getSelectionModel().getSelectedItem());

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this file? This cannot be undone.",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {

                if (file.delete()) {
                    System.out.println("Delete successful");
                    presets.getItems().clear();
                    presets.getItems().addAll(loadFiles(folder));
                    presets.getSelectionModel().select(0);
                }

            }
        });

        HBox presetOperationBox = new HBox(15);
        presetOperationBox.setAlignment(Pos.CENTER);
        presetOperationBox.getChildren().addAll(renameButton, deleteButton);

        box.getChildren().addAll(presets, nameBox, presetOperationBox);

        presets.setCellFactory(c -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.substring(0, item.lastIndexOf(".")));
                        setOnMouseClicked(e -> {
                            nameField.setText(getText());
                            //file = new File(folder + "/" + item);
                        });
                    }
                }
            };
            return cell;
        });

        Scene scene = new Scene(box, window.getMinWidth(), window.getMinHeight());
        window.setScene(scene);
        nameField.requestFocus();
        window.showAndWait();
    }

    /**
     *
     * @return
     */
    private boolean savePreset (String name, String folder) {
        if (!loadHandsToArrays()) return false;
        // ListView, TextField, Save (Overwrite), Rename(?), Delete

        if (name.isEmpty()) return false;
        String newName = name.trim();
        newName = newName.endsWith(".xml") ? newName : newName.concat(".xml");
        newName = (folder + "/").concat(newName);
        Path file = Paths.get(newName);

        try {
            if (Files.exists(file)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "A file with such name already exists. Are you sure you want to overwrite this file? This cannot be undone.",
                        ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();

                if (alert.getResult() != ButtonType.YES) return false;
            }

            StringWriter stringWriter = new StringWriter();

            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xMLStreamWriter =
                    xMLOutputFactory.createXMLStreamWriter(stringWriter);

            xMLStreamWriter.writeStartDocument();
            xMLStreamWriter.writeStartElement("preset");

            for (int i = 0; i < players.size(); i++) {
                xMLStreamWriter.writeStartElement("player");
                xMLStreamWriter.writeAttribute("geisha", playerGeishas.get(i));

                for (int j = 0; j < playerCards.get(i).size(); j++) {
                    xMLStreamWriter.writeStartElement("card");
                    xMLStreamWriter.writeAttribute("name", playerCards.get(i).get(j));
                    xMLStreamWriter.writeEndElement();
                }

                xMLStreamWriter.writeEndElement();
            }

            xMLStreamWriter.writeEndDocument();

            xMLStreamWriter.flush();
            xMLStreamWriter.close();

            String xmlString = stringWriter.getBuffer().toString();

            stringWriter.close();

            Files.write(file, Arrays.asList(xmlString), Charset.forName("UTF-8"));

        } catch (Exception e) {
            System.out.println(e + "\nERROR: Couldn't save preset.");
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Oops! Something went wrong. Preset couldn't be saved.");
            alert.showAndWait();
            return false;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Preset successfully saved!");
        alert.showAndWait();
        return true;
    }

    /**
     *
     * @return
     */
    private boolean loadPreset (String name, String folder) {
        //discardFocusOnGeisha();
        // ListView, TextField, Load --check that exists, Rename(?), Delete --enable when selected
        // copy deck and geishas, exclude one card at a time
        // failed to load preset, changes are discarded. make backup copy of hands, load into hands, if something went wrong - reload
        String newName = name.trim();
        newName = newName.endsWith(".xml") ? newName : newName.concat(".xml");
        newName = (folder + "/").concat(newName);

        Path file = Paths.get(newName);
        if (!Files.exists(file)) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "File with such name doesn't exist. Preset couldn't be loaded.");
            alert.showAndWait();
            return false;
        }

        ArrayList<String> geishasRemaining = new ArrayList<>();
        geishasRemaining.addAll(Arrays.asList(geishasDeck));
        ArrayList<String> cardsRemaining = new ArrayList<>();
        cardsRemaining.addAll(Arrays.asList(cardsDeck));

        ArrayList<String> geishas = new ArrayList<>();
        ArrayList< ArrayList<String> > cards = new ArrayList<>();

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader =
                    factory.createXMLEventReader(new FileReader(newName));

            while(eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                switch(event.getEventType()) {

                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();

                        if (qName.equalsIgnoreCase("player")) {
                            Iterator<Attribute> attributes = startElement.getAttributes();
                            geishas.add(
                                    geishasRemaining.remove(
                                            geishasRemaining.indexOf(
                                                    attributes.next().getValue())));
                            cards.add(new ArrayList<>());

                        } else if (qName.equalsIgnoreCase("card")) {
                            Iterator<Attribute> attributes = startElement.getAttributes();
                            cards.get(geishas.size()-1).add(
                                    cardsRemaining.remove(
                                            cardsRemaining.indexOf(
                                                attributes.next().getValue())));
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e + "\nERROR: Couldn't load preset.");

            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Oops! Something went wrong. Preset couldn't be loaded.");
            alert.showAndWait();

            return false;
        }
        playerGeishas.clear();
        playerGeishas.addAll(geishas);
        playerCards.clear();
        for (int i = 0; i < players.size() && i < geishas.size(); i++) {
            playerCards.add(new ArrayList<>());
            playerCards.get(i).addAll(cards.get(i));
        }
        nestHandsInInput();

        // load preset
        // todo can't load too little players [min. number of players]
        return true;
    }

    private void setDeckLabels (Label total, Label instance, String currentCard) {
        int counter = 0;
        for (String i : cardsRemaining) {
            if (i.equals(currentCard)) counter++;
        }

        total.setText("Cards in the deck left: " + cardsRemaining.size());
        instance.setText("This card : " + counter);
    }

    private String getCardName (String item) {
        return item.substring(item.indexOf('_')+1).trim();
    }

    private String getCardColor (String item) {
        return item.substring(0, item.indexOf('_'));
    }

    private String getCssColor (String name) {
        switch (name.toLowerCase()) {
            case "red":
                return "crimson";
            case "blue":
                return "steelblue";
            case "green":
                return "limegreen";
            case "black":
                return "black";
            case "geisha":
            case "gold":
                return "gold";
        }
        return "white";
    }

    private ImageView buildCard(String title, String color) {
        // TODO there be images
        String cardName;
        if (color.equals("Geisha") || cardImagesNames.indexOf(title) != -1)
            cardName = ""; else cardName = color.concat("_");
        cardName = cardName.concat(title.replace(" ", "_"));

        ImageView img = new ImageView(new Image("file:cards/".concat(cardImagesNames.get(cardImagesNames.indexOf(cardName))).concat(".png")));

        img.setPreserveRatio(true);
        //img.fitWidthProperty().bind(window.widthProperty().divide(5).add(20));
        img.fitHeightProperty().bind(window.heightProperty().add(-40).divide(2));
        img.setSmooth(true);
        img.setCache(true);

        return img;
        /*Label name = new Label(title);
        name.setFont(Font.font("Comic Sans", FontWeight.BOLD, name.getFont().getSize() * 1.3));

        Label effect = new Label("Effect: effect happens when the card is played as a guest. " +
                "You can play a card as a guest only if you meet its requirements. Target player is ANY player."); //Card.effect
        effect.setWrapText(true);

        String borderColor = getCssColor(color);

        Label requirement = new Label();
        switch (color) {
            case "Red":
                requirement.setText("PERFORMANCE");
                break;
            case "Blue":
                requirement.setText("SERVICE");
                break;
            case "Green":
                requirement.setText("INTELLIGENCE");
                break;
            case "Black":
                requirement.setText("ANY");
                break;
            case "Geisha":
            case "Gold":
                requirement.setText("");
                break;
        }

        requirement.setText(requirement.getText() + "\n10"); //Card.requirement
        requirement.setFont(Font.font("Arial", requirement.getFont().getSize() * 1.1));
        requirement.setAlignment(Pos.CENTER);

        Label income = new Label("INCOME" + "\n10"); //Card.income  --District Kanryou
        income.setFont(Font.font("Arial", income.getFont().getSize() * 1.1));
        income.setAlignment(Pos.CENTER);

        Label reputation = new Label("+1 PERFORMANCE" + "\n" + "+1 SERVICE" + "\n" + "+1 INTELLIGENCE"); //Card.reputation
        reputation.setAlignment(Pos.CENTER);

        GridPane card = new GridPane();
        card.setPadding(new Insets(5));
        card.setAlignment(Pos.CENTER);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setHgap(15);
        card.setVgap(15);
        card.setMinWidth(300);
        card.setPrefWidth(300);
        card.setMaxWidth(350);
        card.setPrefHeight(350);
        card.getColumnConstraints().addAll(
                new ColumnConstraints(card.getPrefWidth() * 0.35),
                new ColumnConstraints(card.getPrefWidth() * 0.65));

        card.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: " + borderColor + ";");

        int rowIndexCounter = 0;
        GridPane.setConstraints(name, 1, rowIndexCounter);
        GridPane.setConstraints(requirement, 0, rowIndexCounter++);
        GridPane.setConstraints(income, 0, rowIndexCounter++);
        GridPane.setConstraints(reputation, 0, rowIndexCounter);
        GridPane.setConstraints(effect, 1, rowIndexCounter);

        card.getChildren().addAll(name, requirement, income, reputation, effect);

        return card;*/
    }
}