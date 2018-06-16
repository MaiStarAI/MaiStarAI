import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SetupView extends Application {

    Stage window;
    private Scene scene;
    //private Card[] deck;
    private ArrayList<String> players;
    private ArrayList<Boolean> isAI;
    private GridPane setupCard;

    public int windowWidth = 940;
    public int windowHeight = 660;

    //private String[][] playerHands;
    public static final String[] geishasDeck = {
            "Momiji", "Akenohoshi", "Suzune", "Natsumi", "Harukaze", "Oboro"}; // TODO delete, access the other constant
    private ArrayList<String> geishasRemaining;
    private String[] cards; // TODO really? not needed; access the other constant

    public ArrayList<String> playerGeishas;
    public ArrayList< ArrayList<String> > playerCards;
    private int editingIndex;

    private TableView playerHands;
    private Pair<Integer, Integer> chosenCellIndex; // = new TableCell();

    private ScheduledThreadPoolExecutor executor;
    private ScheduledFuture<?> scheduledFuture;

    public static void main (String[] args) {
        launch(args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Mai-Star --Setup Example");

        window.setMinWidth(windowWidth);
        window.setMinHeight(windowHeight);

        players = new ArrayList<>();
        isAI = new ArrayList<>();

        geishasRemaining = new ArrayList<>();
        editingIndex = -1;

        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);

        setScene1();

        window.show();
    }

    @Override
    public void stop() {
        executor.shutdown();
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
            playerType[i].getSelectionModel().select(isAI.size() > i ? (isAI.get(i) ? 1 : 0) : 1);

            playerBox[i] = new HBox(5);
            playerBox[i].setAlignment(Pos.BASELINE_LEFT);

            playerBox[i].getChildren().addAll(playerNameLabel[i], playerName[i], playerType[i]);
            playerNamesBox[i / 3].getChildren().add(playerBox[i]);
        }
        playerType[0].getSelectionModel().select(0);

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
                isAI.add(playerType[i].getValue().equals("AI"));
            }

            //throw names, return, continue to phase 2
            setScene2(); //scene2
        });

        beginGame.setOnAction(e -> {
            //save names, set to player+i if empty
            for (int i = 0; i < playerCount.getValue(); i++) {
                players.set(i, !playerName[i].getText().equals("") ? playerName[i].getText().trim() : playerName[i].getPromptText());
                isAI.add(playerType[i].getValue().equals("AI"));
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

        Button back = new Button("Back");
        back.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure? You will lose the current input.",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES)
                setScene1(); //setScene1
        });
        box.getChildren().add(back);

        String[] cardSelection = {"Actor", "Courtier", "Emissary", "Merchant", "Okaasan",
                "Ronin", "Scholar", "Sumo Wrestler", "Thief", "Yakuza",
                "Daimyo", "Samurai", "District Kanryou", "Monk", "Shogun"};

        ChoiceBox<String> cardType = new ChoiceBox<>();
        cardType.getItems().addAll(cardSelection);
        cardType.getSelectionModel().select(0);

        ChoiceBox<String> cardColor = new ChoiceBox<>();
        cardColor.getItems().addAll("Red", "Blue", "Green"); //"Black"
        cardColor.getSelectionModel().select(0);

        setupCard = buildCard(cardType.getValue(), cardColor.getValue());

        HBox cardSetupBox = new HBox(20);
        final Pane rightSpacer = new Pane();
        HBox.setHgrow(rightSpacer, Priority.SOMETIMES);
        final Pane leftSpacer = new Pane();
        HBox.setHgrow(leftSpacer, Priority.SOMETIMES);
        cardSetupBox.getChildren().addAll(cardType, cardColor, leftSpacer, setupCard, rightSpacer);

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

            cardSetupBox.getChildren().remove(setupCard);
            setupCard = buildCard(cardType.getValue(), cardColor.getValue());
            cardSetupBox.getChildren().add(3, setupCard);
        });

        cardColor.setOnAction(e -> {
            if (cardType.getValue() == null || cardColor.getValue() == null) return;
            cardSetupBox.getChildren().remove(setupCard);
            setupCard = buildCard(cardType.getValue(), cardColor.getValue());
            cardSetupBox.getChildren().add(3, setupCard);
        });

        box.getChildren().add(cardSetupBox);

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
        //Every time an operation with table is done, player hands are updated

        /*if (playerHands == null)*/ playerHands = new TableView();
        playerHands.getSelectionModel().setCellSelectionEnabled(true);
        playerHands.setPrefWidth(windowWidth - box.getPadding().getLeft() - box.getPadding().getRight());
        //playerHands.setTooltip(new Tooltip("Arrange the order in which the players take their turns by clicking and dragging the columns"));

        chosenCellIndex = null;//new Pair<>(0, 0);

        geishasRemaining.clear();
        geishasRemaining.addAll(Arrays.asList(geishasDeck));

        TableColumn[] columns = new TableColumn[players.size()];

        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        ObservableList<String> row1 = FXCollections.observableArrayList();
        ObservableList<String> row2 = FXCollections.observableArrayList();

        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn<>(players.get(i));
            columns[i].setPrefWidth(playerHands.getPrefWidth() / columns.length);
            columns[i].setResizable(false);
            columns[i].setReorderable(false);
            columns[i].setSortable(false);

            int j = i;
            columns[i].setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                }
            });
            //new ReadOnlyStringWrapper(param.getValue().get()));
                /*{param.getValue().bind(param.getValue());
                return param.getValue().toString();});*/
            columns[i].setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<SimpleStringProperty, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<SimpleStringProperty, String> event) {
                    event.getTableView().getItems().get(event.getTablePosition().getRow()).set(geishasRemaining.remove(geishasRemaining.indexOf(event.getNewValue())));
                    //TablePosition tp = tableView.getFocusModel().getFocusedCell();

                }
            });
            columns[i].setCellFactory(tc -> {
                TableCell<SimpleStringProperty, String> cell = new TableCell<>() {
                    @Override
                    protected void updateItem (String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setAlignment(Pos.CENTER);
                            if (item.toLowerCase().contains("geisha")/*geishasRemaining.contains(item)*/) {
                                setText(item);
                                setFont(Font.font("Comic Sans", FontPosture.ITALIC, getFont().getSize()));
                                setTooltip(new Tooltip("Click to change your Geisha"));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: gold;");

                                setOnMouseClicked(e -> {
                                    if (e.getButton().equals(MouseButton.PRIMARY)) {
                                        if (e.getClickCount() == 1 && (chosenCellIndex == null
                                                || playerHands.getColumns().indexOf(tc) == chosenCellIndex.getKey()
                                                || getIndex() == chosenCellIndex.getValue())) {
                                            scheduledFuture = executor.schedule(() ->
                                                Platform.runLater(() -> {

                                                    chosenCellIndex = new Pair<>(playerHands.getColumns().indexOf(tc), getIndex());
                                                    System.out.println(chosenCellIndex.getKey() + " " + chosenCellIndex.getValue());

                                                    cardType.getItems().clear();
                                                    cardType.getItems().add(item);
                                                    cardType.getItems().addAll(geishasRemaining);
                                                    cardType.getSelectionModel().select(0);

                                                    if (!cardColor.getItems().contains("Geisha")) {
                                                        cardColor.getItems().clear();
                                                        cardColor.getItems().add("Geisha");
                                                        cardColor.getSelectionModel().select(0);
                                                    }

                                            }), 350, TimeUnit.MILLISECONDS);
                                        } else {
                                            if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
                                                scheduledFuture.cancel(false);
                                                if (!cardColor.getItems().contains("Geisha")) return;

                                                geishasRemaining.add(
                                                        ((TableColumn) playerHands.getColumns().get( chosenCellIndex.getKey() ))
                                                                .getCellObservableValue( (int)chosenCellIndex.getValue() )
                                                                .getValue().toString()
                                                );
                                                /*((TableColumn) playerHands.getColumns().get( chosenCellIndex.getKey() ))
                                                        .getCellObservableValue( (int)chosenCellIndex.getValue() )*/
                                                playerHands.edit(chosenCellIndex.getValue(), (TableColumn) playerHands.getColumns().get(chosenCellIndex.getKey()));

                                                geishasRemaining.add(item);
                                                setItem(geishasRemaining.remove(geishasRemaining.indexOf(cardType.getValue())));

                                                System.out.println(geishasRemaining);

                                                chosenCellIndex = null;

                                                cardType.getItems().clear();
                                                cardType.getItems().addAll(cardSelection);
                                                cardType.getSelectionModel().select(0);

                                                //return the standard card selection, ensure exchange if happened
                                                // *currentlyEditing* ?
                                            }
                                        }

                                            // TODO evoke choosing geisha card screen, exchange/add button on player to select this card,
                                            // TODO add tooltip (for exchange/add button)
                                    }
                                });
                            } else if (item.equals("add")) {
                                Button button = new Button("Add card...");
                                setGraphic(button);
                                setTooltip(new Tooltip("Press to add the card to the player's hand"));

                                button.setOnMouseClicked(e -> {
                                    // TODO add to the end of the hand, remove from the deck
                                    if (cardColor.getItems().contains("Geisha")) {
                                        //getTableColumn().getCellObservableValue(0);
                                        geishasRemaining.add(item);
                                        geishasRemaining.remove(geishasRemaining.indexOf(cardType.getValue()));
                                        // TODO replace with code from geisha
                                    }
                                });
                            } else if (!item.equals("")) {
                                setText(item);
                                setTooltip(new Tooltip("Double-click to remove the card from the hand"));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: " + getColor(cardColor.getValue()) + ";");

                                setOnMouseClicked(e -> {
                                    // TODO put to choosing card screen, double click to remove from hand, add to deck

                                    if (e.getClickCount() == 1) {
                                        scheduledFuture = executor.schedule(() ->
                                                Platform.runLater(() -> {
                                                    System.out.println("ONLY ONE!");

                                                    if (cardColor.getValue().equals("Geisha")) {
                                                        cardType.getItems().clear();
                                                        cardType.getItems().addAll(cardSelection);
                                                        cardType.getSelectionModel().select(0);
                                                        //function cardType = Selection
                                                    }

                                                }), 350, TimeUnit.MILLISECONDS);

                                    } else if (e.getClickCount() > 1) {
                                        if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone()) {
                                            scheduledFuture.cancel(false);

                                            System.out.println("MORE THAN ONE!");
                                            if (cardColor.getItems().contains("Geisha")) return;

                                        }
                                    }
                                });
                            }
                        }
                    }
                };
                return cell;
            });

            playerHands.getColumns().add(columns[i]);
            //row1.add(geishasRemaining.remove(new Random().nextInt(geishasRemaining.size())));
            row2.add("add");
        }

        data.addAll(row1, row2);
        playerHands.setItems(data);


        VBox[] handBox = new VBox[6];
        HBox[] playerHandBox = new HBox[6];
        Label[] playerName = new Label[6];
        ListView[] playerHand = new ListView[6];
        VBox[] handOptions = new VBox[6];

        HBox[] playerHandsBox = new HBox[2];
        playerHandsBox[0] = new HBox(20);
        playerHandsBox[0].setAlignment(Pos.BASELINE_LEFT);
        playerHandsBox[1] = new HBox(20);
        playerHandsBox[1].setAlignment(Pos.BASELINE_LEFT);

        for (int i = 0; i < 6; i++) {
            Button addCardButton = new Button("ADD");
            addCardButton.setTooltip(new Tooltip("Add the card in buffer to this hand"));

            Button editCardButton = new Button("EDIT");
            editCardButton.setTooltip(new Tooltip("Replace the selected card with another"));
            editCardButton.setOnAction(e -> {
            });
            Button deleteCardButton = new Button("DELETE");
            deleteCardButton.setTooltip(new Tooltip("Delete the selected card from this hand"));
            deleteCardButton.setOnAction(e -> {
            });
            Button clearCardsButton = new Button("CLEAR");
            clearCardsButton.setTooltip(new Tooltip("Empty the hand, leaving only Geisha"));
            clearCardsButton.setOnAction(e -> {
            });
            Button randomizeCardsButton = new Button("RAND.");
            randomizeCardsButton.setTooltip(new Tooltip("Empty this hand and pick a number of random cards"));

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
                playerName[i].setText(players.get(i) + (isAI.get(i) ? " [AI]" : ""));
                playerHand[i].getItems().add(geishasRemaining.remove(new Random().nextInt(geishasRemaining.size())));
            } else {
                handBox[i].setManaged(false);
                handBox[i].setVisible(false);
            }

            handBox[i].getChildren().addAll(playerName[i], playerHandBox[i]);
            playerHandsBox[i / 3].getChildren().add(handBox[i]);

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
                            if (!item.contains("_")/*geishasRemaining.contains(item)*/) {
                                setText(item);
                                setFont(Font.font("Comic Sans", FontPosture.ITALIC, getFont().getSize()));
                                //setTooltip(new Tooltip("Click to change your Geisha"));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: gold;");

                                setOnMouseClicked(e -> {
                                    //geishasRemaining.add();
                                        /*((TableColumn) playerHands.getColumns().get( chosenCellIndex.getKey() ))
                                                .getCellObservableValue( (int)chosenCellIndex.getValue() )*/

                                    /*geishasRemaining.add(item);
                                    setItem(geishasRemaining.remove(geishasRemaining.indexOf(cardType.getValue())));*/
                                });
                            } else {
                                setText(item.substring(item.indexOf('_')+1).trim());
                                //setFont(Font.font("Comic Sans", FontPosture.ITALIC, getFont().getSize()));
                                //setTooltip(new Tooltip(""));
                                setStyle("-fx-border-style: solid inside;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-insets: 5;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-border-color: " + getColor(item.substring(0, item.indexOf('_'))) + ";");
                                setOnMouseClicked(e -> {
                                    if (cardColor.getItems().contains("Geisha")) {
                                        cardType.getItems().clear();
                                        cardType.getItems().addAll(cardSelection);
                                        cardType.getSelectionModel().select(0);
                                    //}
                                        cardType.getSelectionModel().select(cardType.getItems().indexOf(getText()));
                                        cardColor.getSelectionModel().select(cardColor.getItems().indexOf(item.substring(0, item.indexOf("_"))));
                                    }
                                    //display card
                                });
                            }
                        }
                    }
                };
                return cell;
            });

            int j = i;
            addCardButton.setOnAction(e -> {
                //TODO
                if (cardColor.getItems().contains("Geisha") // or make it identical to Edit in that regard
                        /*&& card is in the deck*/) return;
                playerHand[j].getItems().add(cardColor.getValue().concat("_").concat(cardType.getValue())/*deck.remove(cardType.getValue) if present*/);
            });

            editCardButton.setOnAction(e -> {
                // if player DOESN'T choose Geisha, replace the card normally
                // if player DOES choose Geisha, add note to click Edit again to swap Geishas or pick a new one
                if (playerHand[j].getSelectionModel().getSelectedItems().isEmpty()) return;
                if (playerHand[j].getSelectionModel().getSelectedItems().get(0).toString().contains("_")) {
                    if (cardColor.getValue().equals("Geisha")) {
                        cardType.getItems().clear();
                        cardType.getItems().addAll(cardSelection);
                        cardType.getSelectionModel().select(0);
                    } else {
                        /*if not present in the deck, return;*/
                        playerHand[j].getItems().set(playerHand[j].getItems().indexOf(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString()),
                                cardColor.getValue().concat("_").concat(cardType.getValue())/*deck.remove(cardType.getValue) if present*/);
                        /*playerHand[j].getItems().add(playerHand[j].getItems().indexOf(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString()),
                                cardColor.getValue().concat("_").concat(cardType.getValue())/*deck.remove(cardType.getValue) if present*//*);
                        playerHand[j].getItems().remove(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString());*/
                    }
                    return;
                }

                if (!cardColor.getValue().equals("Geisha")) {
                    cardType.getItems().clear();
                    cardType.getItems().add(playerHand[j].getSelectionModel().getSelectedItems().get(0).toString());
                    cardType.getItems().addAll(geishasRemaining);
                    cardType.getSelectionModel().select(0);

                    editingIndex = j;

                    if (!cardColor.getItems().contains("Geisha")) {
                        cardColor.getItems().clear();
                        cardColor.getItems().add("Geisha");
                        cardColor.getSelectionModel().select(0);
                    }
                } else {
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

                /*
                if (editCardButton.getText().equals("EDIT"))
                    editCardButton.setText("SAVE");
                else
                    editCardButton.setText("EDIT");*/
            });

            deleteCardButton.setOnAction(e -> {
                ObservableList m = playerHand[j].getSelectionModel().getSelectedItems();
                for (int k = 0; k < playerHand[j].getSelectionModel().getSelectedItems().size(); k++) {
                    if (((String) m.get(k)).contains("_"))
                        playerHand[j].getItems().remove(m.get(k));
                }
            });

            clearCardsButton.setOnAction(e -> {
                // first move all cards to the deck
                playerHand[j].getItems().remove(1, playerHand[j].getItems().size());
            });

            randomizeCardsButton.setOnAction(e -> {
                //return all cards to the deck

                geishasRemaining.add(playerHand[j].getItems().get(0).toString());
                playerHand[j].getItems().clear();
                playerHand[j].getItems().add(geishasRemaining.remove(new Random().nextInt(geishasRemaining.size())));
                for (int k = 0; k < (playerHand[j].getItems().get(0).toString().equals("Oboro") ? 7 : 5); k++ )
                    playerHand[j].getItems().add("Red" + "_" + cardSelection[new Random().nextInt(cardSelection.length)]);
                    // make a function for returning possible color based on the card, useful in cardColor/Type scheme
            });
        }
        if (players.size() <= 3) playerHandsBox[1].setManaged(false);

        /*for (int i = playerHand.size(); i >= players.size(); i--) {
            System.out.println(playerHand.size());
            //TODO remove cards, add to the deck, remove all extra Boxes

        }*/


        // TODO BUTTONS: Add, Delete, Give, Empty, Randomize

        // start.setOnAction(e -> {runAlgorithm();}

        box.getChildren().addAll(playerHandsBox);//playerHands);

        scene = new Scene(box, windowWidth, windowHeight);
        window.setScene(scene);
        cardType.requestFocus();

        /*for (int i = 0; i < players.size(); i++) {
            TableColumn column = new TableColumn(players.get(i));
            column.setPrefWidth(playerHands.getPrefWidth() / players.size());


            column.setCellValueFactory(c -> {
                TableCell<Animation.Status, String> cell = new TableCell<>();
                cell.setText(cardType.getValue());
                cell.setTooltip(new Tooltip("Double-click to remove the card from the hand"));
                cell.setOnMouseClicked(e -> {
                    if(e.getButton().equals(MouseButton.PRIMARY)){
                        if(e.getClickCount() == 2){
                            System.out.println("Double clicked");
                            //
                        }
                    }
                });
                cell.setStyle("-fx-border-style: solid inside;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-insets: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: " + (!cell.getText().equals("") /* */ /*? cardColor.getValue().toLowerCase() : "white") + ";");
                return cell;
            });

            //column.getColumns().add();

            playerHands.getColumns().add(column);*/

        /*table.setRowFactory(tv -> {
            TableRow row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getButton().toString().equals("PRIMARY")
                        && e.getClickCount() == 2) {
                    boolean self = pageTables[row.getIndex()].equals("users") && pageIDs[row.getIndex()] == Librarian.id;
                    EntryWindow.open(pageTables[row.getIndex()], pageIDs[row.getIndex()], self);
                    fillColumns();
                }
            });
            row.setOnKeyPressed(e -> {
                System.out.println(e.getCode());
            });
            return row;
        });*/
    }

    //private void setupPhase3 ()
    // assume phase 2 is but a starting state, then phase 3 is full-detail mid-game state. To-do later, expand phase 2 to phase 3 and limit phase 2


                /*cell.setAlignment(Pos.CENTER);
                Text text = new Text();
                cell.setGraphic(text);
                text.textProperty().bind(cell.itemProperty());

                if (tc.getText().equals("Player 1")) {
                    //this.setTextFill(Color.BLUEVIOLET);
                    cell.setOnMouseClicked(e -> {System.out.println("Hi!");});
                    cell.setStyle("-fx-border-style: solid inside;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-insets: 5;" +
                            "-fx-border-radius: 5;" +
                            "-fx-border-color: gold;");
                } else if (tc.getText().equals("Player 2")) {
                    //cell.setText("");
                    cell.setGraphic(new Button("Add card..."));
                    cell.setTooltip(new Tooltip("Press me to add the card to the player's hand"));
                }
                //cell.setText("yo");
                return cell;
            });*/


        // ** The TableCell class has the method setTextFill(Paint p) that you
        // ** need to override the text color
        //   To obtain the TableCell we need to replace the Default CellFactory
        //   with one that returns a new TableCell instance,
        //   and @Override the updateItem(String item, boolean empty) method.
        //


        //playerHands.setItems(myTableData);

        /*vbox.getChildren().addAll(playerHands);
        VBox.setVgrow(playerHands, Priority.ALWAYS);

        window.setScene(scene);
    }*/

    /*private void setGeisha(String geisha) {

        return
    }*/

    /**
     * Restore the deck fully and put a random geisha and random 5 cards (7, if Oboro is the geisha) into the hand of each player,
     * starting from the last
     */
    private void randomiseHands (int index) {
        //TODO
    }

    /**
     * Delete all cards except for geishas in the hand and put them to the deck
     */
    private void clearHands (int index) {
        //TODO
    }

    /**
     * Delete chosen cards from a hand and put it to the deck, if possible
     */
    private void deleteCards () {
        //TODO
    }

    /**
     * Exchange chosen cards from one hand to another, if possible
     */
    private void exchangeCards () {
        //TODO
    }

    private String getColor (String name) {
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

    private GridPane buildCard(String title, String color) {
        Label name = new Label(title);
        name.setFont(Font.font("Comic Sans", FontWeight.BOLD, name.getFont().getSize() * 1.3));

        Label effect = new Label("Effect: effect happens when the card is played as a guest. " +
                "You can play a card as a guest only if you meet its requirements. Target player is ANY player."); //Card.effect
        effect.setWrapText(true);

        String borderColor = getColor(color);

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

        return card;
    }
}