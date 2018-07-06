import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * The FXML controller class
 * It handles functions connected with Graphics
 */
public class GameGraphics {
    @FXML public GridPane playerGrid;
    @FXML public Label lastAction;
    @FXML public VBox table;
    public GridPane[] tables;
    public Label[] cardCounts;
    public Label[] scoreCounts;
    private int currentTableIndex;

    @FXML public GridPane handGrid;

    @FXML public Button menuButton;
    @FXML public Button scoreButton;

    @FXML public Label scoreLabel;
    @FXML public Label deckLabel;
    @FXML public Label heapLabel;

    @FXML public Button geishaButton;
    @FXML public Button playGuestButton;
    @FXML public Button advertiseButton;
    @FXML public Button introduceButton;
    @FXML public Button exchangeButton;
    @FXML public Button searchButton;
    @FXML public Button endTurnButton;

    @FXML public ImageView showcaseCard;
    private Image showCaseCardDefault;

    private int[] guestCount;
    private int[] advertiserCount;
    public String lastActionDescription;

    public ArrayList</*ImageView*/StackPane > selection;

    private boolean actionTaken;

    /** Accessed on successful .fxml load */
    @FXML
    public void initialize() {
        showCaseCardDefault = showcaseCard.getImage();

        currentTableIndex = Main.playerIndex;

        lastActionDescription = "";

        selection = new ArrayList<>();

        if (Main.mainPlayer.getGeisha().getName().toString().equals("Oboro")
                || Main.mainPlayer.getGeisha().getName().toString().equals("Harukaze")) {
            geishaButton.setManaged(false);
            geishaButton.setVisible(false);
        }

        createTables();
        resetActionTaken();
        updateTables();
        noButtonsWithoutRepresentation();

        menuButton.requestFocus();
    }

    //todo Momiji - select guest
    //todo

    /** Creates and updates player tables, access only once */
    private void createTables() {
        tables = new GridPane[Main.state.players.size()];
        cardCounts = new Label[Main.state.players.size()];
        scoreCounts = new Label[Main.state.players.size()];

        guestCount = new int[Main.state.players.size()];
        advertiserCount = new int[Main.state.players.size()];

        for (int i = 0; i < Main.state.players.size(); i++) {
            tables[i] = new GridPane();
            tables[i].setPadding(new Insets(5, 25, 5, 5));
            tables[i].getStyleClass().add("table");
            tables[i].setAlignment(Pos.CENTER_LEFT);
            tables[i].setPrefHeight(280.0);
            tables[i].setPrefWidth(600.0);

            for (int j = 0; j < 2; j++) {
                RowConstraints rc = new RowConstraints();
                rc.setPrefHeight(140);
                rc.setMaxHeight(140);
                rc.setValignment(VPos.BOTTOM);
                rc.setVgrow(Priority.SOMETIMES);
                tables[i].getRowConstraints().add(rc);
            }

            ColumnConstraints ccg = new ColumnConstraints();
            ccg.setMinWidth(100);
            ccg.setMaxWidth(120);
            ccg.setPrefWidth(110);
            ccg.setHgrow(Priority.SOMETIMES);
            ccg.setHalignment(HPos.CENTER); //null
            tables[i].getColumnConstraints().add(ccg);

            Label playerLabel = new Label(Main.state.players.get(i).getName()); //players.get(i).name
            playerLabel.getStyleClass().add("player1Label");
            playerLabel.setAlignment(Pos.CENTER);
            playerLabel.setMnemonicParsing(true);
            playerLabel.setTextAlignment(TextAlignment.CENTER);
            playerLabel.setWrapText(true);
            tables[i].add(playerLabel, 0, 0);

            addToTable(tables[i], Main.state.players.get(i).getGeisha()); //GameView.players.get(i).geisha

            guestCount[i] = 0;
            advertiserCount[i] = 0;

            ((Label)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(1))
                    .setText(i != currentTableIndex ? Main.state.players.get(i).getName() : Main.state.players.get(i).getName().concat(" (you)")); //.name

            cardCounts[i] = new Label(Main.state.players.get(i).getHand().size()+""); // GameView.players.hand.size()
            cardCounts[i].setAlignment(Pos.CENTER);
            cardCounts[i].getStyleClass().add("statLabel");
            ((GridPane)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(2))
                        .add(cardCounts[i], 0, 1);

            scoreCounts[i] = new Label(Main.state.players.get(i).getScore()+""); // GameView.players.score
            scoreCounts[i].setAlignment(Pos.CENTER);
            scoreCounts[i].getStyleClass().add("statLabel");
            ((GridPane)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(2))
                    .add(scoreCounts[i], 1, 1);
        }

        playerGrid.getChildren().remove(Main.state.players.size(), playerGrid.getChildren().size());
        playerGrid.getColumnConstraints().remove(Main.state.players.size(), playerGrid.getColumnConstraints().size());

        table.getChildren().set(0, tables[currentTableIndex]);

        Effect effect = playerGrid.getChildren().get(0).getEffect();
        playerGrid.getChildren().get(0).setStyle("-fx-effect: null;");
        playerGrid.getChildren().get(currentTableIndex).setEffect(effect);
        playerGrid.getChildren().get(currentTableIndex).setStyle("");
    }

    public void resetActionTaken() {
        actionTaken = false; // todo leave if the Fill functions are called only once
    }

    public void updateAllGraphics() {
        updateTables();
        shiftPlayerGrid();
        noButtonsWithoutRepresentation();
    }

    public void updateTables() {
        fillHand();
        fillTables();
        updateLightGraphics();
    }

    public void updateLightGraphics() {
        updatePlayerGrid();
        updateLastAction();
        updateScore();
        updateDeck();
        updateDiscard();
    }

    private void fillTables() {
        for (int i = 0; i < Main.state.players.size(); i++) {
            guestCount[i] = 0;
            advertiserCount[i] = 0;
            tables[i].getChildren().remove(2, tables[i].getChildren().size());
            for (int j = 0; j < Main.state.players.get(i).getGuests().size(); j++) {
                addToTable(tables[i], i, Main.state.players.get(i).getGuests().get(j), true);
            }
            for (int j = 0; j < Main.state.players.get(i).getAdverts().size(); j++) {
                addToTable(tables[i], i, Main.state.players.get(i).getAdverts().get(j), false);
            }
        }
    }

    /** Reload the player hand grid contents */
    private void fillHand() {
        handGrid.getChildren().clear();
        for (int i = 0; i < Main.state.players.get(Main.playerIndex).getHand().size(); i++) {
            Card card = Main.state.players.get(Main.playerIndex).getHand().get(i);
            addToTable(handGrid, -1, card, true);
        }
    }

    /** Update the number on the Deck */
    public void updateDeck() {
        deckLabel.setText(Main.state.draw_deck.size() + "");
    }

    /** Update the number on the Discard Pile */
    public void updateDiscard() {
        heapLabel.setText(Main.state.discarded.size() + "");
    }

    /** Update the number on the Score Sheet button */
    public void updateScore() {
        scoreLabel.setText(Main.state.players.get(Main.playerIndex).getScore() + "");
    }

    public void updateLastAction() {
        String[] vibes = {
                "THIS IS A LABEL", "LIFE IS REAL", "RANDOM ALWAYS WINS", "HAMNA DOESN'T TELL ME THE TRUTH", "FUCK ALL YA ALL", "I'M SORRY",
                "SPARROW", "MAI-STAR IS A GREAT GAME", "WE WILL FIX IT!", "FALLOUT", "SHE GAVE TO ME ON THE 8TH DAY", "RUN, BOY, RUN",
                "IT'S ALWAYS FUCKING LEMONS", "WE ALL COULD USE SOME GRAMMARLY", "LIFE IS STRANGE", "FROM RANDOM, XOXO", "DO NOT POKE THE CERBERUS",
                "LIFE IS CAKE", "DANCING UNDER THE MOONLIGHT", "I AM DOING MY JOB!", "6:30", "I LOVE CYBERPUNK",
                "IT'S FINE!", "GOOGLE DOOGLE MOOGLE CLASH!", "PRESS ON THE HEADPHONES", "ZZAP!", "HOW DO YOU WANNA DO THIS?",
                "AKENOHOSHI IS A SWEET NAME", "LEMONADE IS GOOD", "NATURAL SURVIVOR", "SIR RANDOM, ESQ.",
                "KAMIKAZE SHOUTS: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA!!!"
        };
        lastAction.setText("Last action: " + lastActionDescription);
                //vibes[new Random().nextInt(vibes.length)]);
    }

    /** Update scores and hand numbers of players on the Player Card grid */
    public void updatePlayerGrid() {
        for (int i = 0; i < Main.state.players.size(); i++) {
            cardCounts[i].setText(Main.state.players.get(i).getHand().size()+"");
            scoreCounts[i].setText(Main.state.players.get(i).getScore()+"");
        }

        updateScore();
    }

    /** This is where selection magic happens */
    private void onSelection(StackPane card) {
        if (selection.contains(card)) {
            selection.remove(selection.indexOf(card)).getStyleClass().remove("selected");
        } else {
            if (selection.size() >= 2) {
                selection.remove(0).getStyleClass().remove("selected");
            }

            selection.add(card);

            //Effect effect = new javafx.scene.effect.Lighting();
            selection.get(selection.size() - 1).getStyleClass().add("selected");
            //selection.get(selection.size() - 1).setEffect(effect);
        }

        /* de-select all, select available buttons, if it's player's turn */
        noButtonsWithoutRepresentation();
    }

    /** Set buttons to be disabled, except for Search and Geisha. Use if selection is empty. */
    private void noButtonsWithoutRepresentation() {

        playGuestButton.setDisable(true);
        advertiseButton.setDisable(true);
        introduceButton.setDisable(true);
        exchangeButton.setDisable(true);
        searchButton.setDisable(true);
        geishaButton.setDisable(true);
        endTurnButton.setDisable(true);

        //todo if (courtier in action) set guest true, make new determinization, if selection.size == 0
        //turnCount = GameView.playerIndex; //todo delete
        if (Main.state.getTurnPlayer().getName().equals(Main.mainPlayer.getName())) {
            /* end turn */
            if (actionTaken) {
                endTurnButton.setDisable(false);
                endTurnButton.setOnAction(e -> finishTurn());
                //return;
            } else {
                /* search */
                setButtonAvailable(searchButton, new Action(Action.Name.Search,
                        Main.state.turning_player,
                        null, null, null, null));
            }

            /* geisha */
            isGeishaApplicable(null);

            if (selection.size() == 1) {
                if (handGrid.getChildren().contains(selection.get(0))) {
                    /* play a guest */
                    Action actionGuest = new Action(Action.Name.Guest,
                            Main.state.turning_player, Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(0))),
                            null, null, null);
                    if (Main.state.isApplicableAction(actionGuest)) {
                        playGuestButton.setDisable(false);
                        playGuestButton.setOnAction(e -> {

                            effectWindow(actionGuest);
                            if (effectAnswer == -1) return;

                        });
                    }

                    /* advertise */
                    setButtonAvailable(advertiseButton, new Action(Action.Name.Advertiser,
                            Main.state.turning_player, Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(0))),
                            null, null, null));

                }

                /* geisha, again */
                Card geishaCard;
                if (Main.mainPlayer.getGeisha().getName() == Geisha.Name.Momiji
                        && tables[Main.playerIndex].getChildren().contains(selection.get(0))) {
                    geishaCard = Main.state.turning_player.getGuests().get(GridPane.getColumnIndex(selection.get(0)));
                    isGeishaApplicable(geishaCard);
                } else if (handGrid.getChildren().contains(selection.get(0))){
                    geishaCard = Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(0)));
                    isGeishaApplicable(geishaCard);
                }

            } else if (selection.size() == 2) {
                /* introduce */
                if (handGrid.getChildren().contains(selection.get(0)) && handGrid.getChildren().contains(selection.get(1))) {

                    Action action = new Action(Action.Name.Introduce,
                            Main.state.turning_player,
                            Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(0))),
                            Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(1))),
                            null, null
                    );


                    setButtonAvailable(introduceButton, action);
                }
                else {
                    /* exchange */
                    if (handGrid.getChildren().contains(selection.get(0)) && tables[Main.playerIndex].getChildren().contains(selection.get(1))) {

                        Action action = new Action(Action.Name.Exchange,
                                Main.state.turning_player,
                                Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(0))),
                                Main.state.turning_player.getAdverts().get(GridPane.getColumnIndex(selection.get(1)) - 1),
                                null, null
                        );

                        setButtonAvailable(exchangeButton, action);
                    } else if (handGrid.getChildren().contains(selection.get(1)) && tables[Main.playerIndex].getChildren().contains(selection.get(0))) {

                        Action action = new Action(Action.Name.Exchange,
                                Main.state.turning_player,
                                Main.state.turning_player.getAdverts().get(GridPane.getColumnIndex(selection.get(0)) - 1),
                                Main.state.turning_player.getHand().get(GridPane.getColumnIndex(selection.get(1))),
                                null, null
                        );

                        setButtonAvailable(exchangeButton, action);
                    }
                }
            }
        }
    }

    private void setButtonAvailable(Button button, Action action) {
        if (Main.state.isApplicableAction(action)) {
            button.setDisable(false);
            button.setOnAction(e -> {
                actionTaken = true;
                executeAction(action);
                if (action.getName() == Action.Name.Advertiser && Main.mainPlayer.getGeisha().getName() == Geisha.Name.Harukaze) {
                    useGeishaWindow(action);
                }
            });
        }
    }

    private void executeAction(Action action) {
        Main.state = Main.state.applyAction(action);
        Main.addActionToLog(action);

        Main.loop();
    }
    //todo endturn state.nextPlayer

    //todo
    private void finishTurn() {
        Main.turnEnded = true;
        Main.state = Main.changeStateAfterAction(Main.state);
        Main.loop();
    }

    private void effectWindow(Action action) {
        //todo target, ok, do not use effect, cancel
        effectAnswer = -1;

        Card card = action.getCard1();
        switch (card.getName()) {
            case Courtier: //return; // pick your card
            case Okaasan:
            case Doctor:
                haveEffect();
                if (effectAnswer == -1) return;
                executeAction(action);
                actionTaken = true;

                Action actionEffectTurn = new Action(Action.Name.GuestEffect,
                        Main.state.getTurnPlayer(), Main.state.getTurnPlayer().getHand().get(GridPane.getColumnIndex(selection.get(0))),
                        null, Main.state.players.get(effectAnswer),null);
                executeAction(actionEffectTurn);
                return;
            case Daimyo:
            case Monk:
            case Shogun:
                confirmEffect();
                if (effectAnswer == -1) return;
                executeAction(action);
                actionTaken = true;

                Action actionEffectCall = new Action(Action.Name.GuestEffect,
                        Main.state.getTurnPlayer(), Main.state.getTurnPlayer().getHand().get(GridPane.getColumnIndex(selection.get(0))),
                        null, Main.state.players.get(effectAnswer),null);
                executeAction(actionEffectCall);
                return;
            case Merchant:
            case Scholar:
            case Thief:
            case Yakuza:
            case Samurai:
            case Emissary:
                targetEffect();
                if (effectAnswer == -1) return;
                executeAction(action);
                actionTaken = true;
                if (effectAnswer == -2) return;

                Action actionEffectTarget = new Action(Action.Name.GuestEffect,
                        Main.state.getTurnPlayer(), Main.state.getTurnPlayer().getHand().get(GridPane.getColumnIndex(selection.get(0))),
                        null, Main.state.players.get(effectAnswer),null);
                executeAction(actionEffectTarget);
                System.out.println(Main.state.players.get(effectAnswer).getName());
                return;
            case Sumo_Wrestler:
                targetEffect();
                int targetPlayer = effectAnswer;

                if (targetPlayer == -1) return;
                executeAction(action);
                actionTaken = true;
                if (effectAnswer == -2) return;

                sumoEffect(card);
                int targetCard = effectAnswer;

                Action actionEffectSumo = new Action(Action.Name.GuestEffect,
                        Main.state.getTurnPlayer(), Main.state.getTurnPlayer().getHand().get(GridPane.getColumnIndex(selection.get(0))),
                        Main.state.players.get(targetPlayer).getHand().get(targetCard), Main.state.players.get(targetPlayer),null);
                executeAction(actionEffectSumo);
                return;
            default:
                effectAnswer = 1;
                executeAction(action);
                actionTaken = true;
                return;
        }
    }

    private void isGeishaApplicable(Card card) {
        if (!geishaButton.isVisible()) return;

        //if (card == null && GameView.state.players.get(GameView.playerIndex).geisha.name.equals(GeishasName.Momji)) return;

        Action action = new Action(Action.Name.Geisha, Main.state.getTurnPlayer(),
                card, null,null, null
        );

        if (Main.state.isApplicableAction(action)) {
            geishaButton.setDisable(false);
            geishaButton.setOnAction(e -> {
                useGeishaWindow(action);
                if (effectAnswer == -1) return;
                //todo
                // Suzune, Natsumi, Momiji - click, play
                // Akenohoshi - new window, select one of the three reputations
                // Harukaze - when taken advertisement action, select two cards from new hand
            });
        }
    }

    private void useGeishaWindow(Action action) {
        effectAnswer = -1;

        switch (action.getTargetPlayer().getGeisha().getName()) {
            case Akenohoshi:
                akenohoshiEffect();
                Action akenohoshiEffect = new Action(Action.Name.Geisha,  Main.state.getTurnPlayer(),
                        null,null, null,
                        new Reputation(effectAnswer == 0 ? 3 : 0, effectAnswer == 1 ? 3 : 0, effectAnswer == 2 ? 3 : 0));
                executeAction(akenohoshiEffect);
                System.out.println(Main.state.getTurnPlayer().getName() + " " + Main.state.getTurnPlayer().getReputation());
                //todo
                return;
            case Harukaze:
                harukazeEffect();
                Action harukazeEffect = new Action(Action.Name.HarukazeDiscard, Main.state.turning_player,
                        Main.state.getTurnPlayer().getHand().get(harukazeSelection.get(0)),
                        Main.state.getTurnPlayer().getHand().get(harukazeSelection.get(1)),
                        null, null);
                executeAction(harukazeEffect);
                //todo
                return;
            default:
                executeAction(action);
        }
    }

    private int effectAnswer;
    private void confirmEffect() {
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Confirm Effect");

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER_LEFT);

        Label message = new Label("Do you want to use this guest's effect?");
        message.setFont(Font.font("", FontWeight.BOLD, 14));
        message.setPadding(new Insets(0, 0, 0, 20));

        Button buttonOK = new Button("Yes");
        buttonOK.setMnemonicParsing(true);
        buttonOK.setDefaultButton(true);
        buttonOK.getStyleClass().add("actionButton");
        buttonOK.setOnAction(e -> {
            effectAnswer = 1;
            window.close();
        });

        Button buttonNo = new Button("No");
        buttonNo.setMnemonicParsing(true);
        buttonNo.getStyleClass().add("actionButton");
        buttonNo.setOnAction(e -> {
            effectAnswer = 0;
            window.close();
        });

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setMnemonicParsing(true);
        buttonCancel.setCancelButton(true);
        buttonCancel.getStyleClass().add("actionButton");
        buttonCancel.setOnAction(e -> {
            effectAnswer = -1;
            window.close();
        });

        ButtonBar buttons = new ButtonBar();
        buttons.setPadding(new Insets(0, 20,0,0));
        buttons.getButtons().addAll(buttonOK, buttonNo, buttonCancel);

        layout.getChildren().addAll(message, buttons);

        Scene scene = new Scene(layout, 360, 130);
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    //todo make a variable for checking additional turn
    private void haveEffect() {
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Guest Effect");

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER_LEFT);

        Label message = new Label("You can now use this guest's effect.");
        message.setFont(Font.font("", FontWeight.BOLD, 14));
        message.setPadding(new Insets(0, 0, 0, 20));

        Button buttonOK = new Button("OK");
        buttonOK.setMnemonicParsing(true);
        buttonOK.setDefaultButton(true);
        buttonOK.setCancelButton(true);
        buttonOK.getStyleClass().add("actionButton");
        buttonOK.setOnAction(e -> {
            effectAnswer = 1;
            window.close();
        });

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setMnemonicParsing(true);
        buttonCancel.getStyleClass().add("actionButton");
        buttonCancel.setOnAction(e -> {
            effectAnswer = -1;
            window.close();
        });

        ButtonBar buttons = new ButtonBar();
        buttons.setPadding(new Insets(0, 20,0,0));
        buttons.getButtons().addAll(buttonOK, buttonCancel);

        layout.getChildren().addAll(message, buttons);

        Scene scene = new Scene(layout, 360, 130);
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    //todo sourcePlayerName, cardName, effect
    public int cancelEffect() {
        // todo check if the player has district kanryou OR ronin, then proceed

        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Cancel Effect");

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER_LEFT);

        Label message = new Label(Main.state.getLastPlayer().getName() + " wants to use their effect on you."); //todo
        message.setFont(Font.font("", FontWeight.BOLD, 14));
        message.setPadding(new Insets(0, 0, 0, 20));

        Button button1 = new Button("Use Ronin");
        button1.setMnemonicParsing(true);
        //if (GameView.state.players.get(GameView.playerIndex).guests. has Ronin)
        //button1.setManaged(false); todo if has the card on table
        button1.getStyleClass().add("actionButton");
        button1.setOnAction(e -> {
            effectAnswer = 1;
            //todo maybe remove from guests
            window.close();
        });

        Button button2 = new Button("Use District Kanryou");
        button2.setMnemonicParsing(true);
        //if (GameView.state.players.get(GameView.playerIndex).hand. has District Kanryou)
        //button2.setManaged(false); todo if has the card in hand
        button2.getStyleClass().add("actionButton");
        button2.setOnAction(e -> {
            effectAnswer = 2;
            //todo maybe remove from hand
            window.close();
        });

        Button buttonCancel = new Button("Do Nothing");
        buttonCancel.setMnemonicParsing(true);
        buttonCancel.getStyleClass().add("actionButton");
        buttonCancel.setOnAction(e -> {
            effectAnswer = 0;
            window.close();
        });

        window.setOnCloseRequest(e -> {
            effectAnswer = 0;
            window.close();
        });

        ButtonBar buttons = new ButtonBar();
        buttons.setPadding(new Insets(0, 20,0,0));
        buttons.getButtons().addAll(button1, button2, buttonCancel);

        layout.getChildren().addAll(message, buttons);

        Scene scene = new Scene(layout, 500, 130);
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();

        return effectAnswer;
    }

    private void targetEffect() {
        Stage window = new Stage();
        //window.setResizable(false);
        window.setMinWidth(720);
        window.setMinHeight(210 + 70 * (Main.state.players.size() > 3 ? 1 : 0));
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Pick Target for Effect");

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER);

        Label message = new Label("Pick a target for your guest's effect."); //todo
        message.setFont(Font.font("", FontWeight.BOLD, 14));
        message.setPadding(new Insets(0, 0, 0, 20));

        GridPane playerGrid = new GridPane();
        playerGrid.setAlignment(Pos.CENTER);
        playerGrid.getRowConstraints().add(new RowConstraints(70));
        if (Main.state.players.size() > 3) playerGrid.getRowConstraints().add(new RowConstraints(70));

        Button[] player = new Button[Main.state.players.size()];

        for (int j = 0; j < 3; j++) {
            ColumnConstraints ccRounds = new ColumnConstraints(200);
            ccRounds.setHalignment(HPos.CENTER);
            playerGrid.getColumnConstraints().add(ccRounds);

            for (int i = 0; i < 2; i++) {
                int k = j+i*3;
                if (k >= Main.state.players.size()) break;
                player[k] = new Button(Main.state.players.get(k).getName());
                player[k].getStyleClass().add("menuButton");
                player[k].setMinWidth(190);
                player[k].setMaxWidth(190);

                player[k].setOnMouseClicked(e -> {
                    if (effectAnswer != -1) player[effectAnswer].getStyleClass().remove("selected");
                    effectAnswer = k;
                    player[effectAnswer].getStyleClass().add("selected");
                });

                playerGrid.add(player[k], j, i);
            }
        }

        effectAnswer = 0;
        player[effectAnswer].getStyleClass().add("selected");

        Button buttonOK = new Button("Apply");
        buttonOK.setMnemonicParsing(true);
        buttonOK.setDefaultButton(true);
        buttonOK.getStyleClass().add("actionButton");
        buttonOK.setOnAction(e -> {
            //effectAnswer = ; todo
            window.close();
        });

        Button buttonNo = new Button("Do Not Apply");
        buttonNo.setMnemonicParsing(true);
        //button2.setManaged(false); todo if has the card in hand
        buttonNo.getStyleClass().add("actionButton");
        buttonNo.setOnAction(e -> {
            effectAnswer = -2;
            window.close();
        });

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setMnemonicParsing(true);
        buttonCancel.setCancelButton(true);
        buttonCancel.getStyleClass().add("actionButton");
        buttonCancel.setOnAction(e -> {
            effectAnswer = -1;
            window.close();
        });

        window.setOnCloseRequest(e -> {
            effectAnswer = -1;
            window.close();
        });

        ButtonBar buttons = new ButtonBar();
        buttons.setPadding(new Insets(0, 20,0,0));
        buttons.getButtons().addAll(buttonOK, buttonNo, buttonCancel);

        layout.getChildren().addAll(message, playerGrid, buttons);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private void sumoEffect(Card source) {
        Stage window = new Stage();
        //window.setResizable(false);
        window.setMinWidth(860);
        window.setMinHeight(420);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Pick Target for Effect");

        VBox layout = new VBox(7);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER);

        Label message = new Label("Pick a card to discard from " + Main.state.players.get(effectAnswer).getName()); //todo
        message.setFont(Font.font("", FontWeight.BOLD, 14));
        message.setPadding(new Insets(0, 0, 0, 20));

        //fitHeight="300.0" fitWidth="225.0" preserveRatio
        GridPane playerGrid = new GridPane();
        //playerGrid.setPrefTileHeight(300);
        playerGrid.getRowConstraints().add(new RowConstraints(300));
        playerGrid.setPadding(new Insets(10, 30, 10, 30));
        playerGrid.getStyleClass().add("table");
        playerGrid.setAlignment(Pos.CENTER_LEFT);
        playerGrid.setPrefHeight(300.0);
        playerGrid.setPrefWidth(600.0);

        int forbidden = -1;

        for (int i = 0; i < Main.state.players.get(effectAnswer).getHand().size(); i++) {
            Card cardCard = Main.state.players.get(effectAnswer).getHand().get(i);

            ImageView img = getCardImage(cardCard.getColor() + "_" + cardCard.getName().toString().replace(" ", "_"));
            img.setFitWidth(220);
            img.setFitHeight(300);
            img.setPickOnBounds(true);
            img.setPreserveRatio(true);

            StackPane card = new StackPane();
            card.setMaxHeight(img.getFitHeight());
            card.setMaxWidth(img.getFitHeight() * 0.7114);
            card.getChildren().add(img);
            card.getStyleClass().add("card");

            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setHalignment(HPos.LEFT);
            cc.setMaxWidth(220);
            cc.setMinWidth(5);
            playerGrid.getColumnConstraints().add(cc);

            int k = i;
            card.setOnMouseEntered(e -> {
                playerGrid.getColumnConstraints().get(k).setMinWidth(200);
                playerGrid.getColumnConstraints().get(k).setHgrow(Priority.NEVER);
            });

            card.setOnMouseExited(e -> {
                if (k == playerGrid.getColumnCount() - 1) return;
                playerGrid.getColumnConstraints().get(k).setMinWidth(5);
                playerGrid.getColumnConstraints().get(k).setHgrow(Priority.SOMETIMES);
            });

            card.setOnMouseClicked(e -> {
                if (cardCard.equals(source)) return;
                playerGrid.getChildren().get(effectAnswer).getStyleClass().remove("target");
                effectAnswer = k;
                playerGrid.getChildren().get(effectAnswer).getStyleClass().add("target");
            });

            playerGrid.add(card, i, 0);

            if (cardCard.equals(source)) {
                card.setOpacity(0.74);
                forbidden = i;
            }
        }

        playerGrid.getColumnConstraints().get(playerGrid.getColumnCount() - 1).setMinWidth(200);
        playerGrid.getColumnConstraints().get(playerGrid.getColumnCount() - 1).setHgrow(Priority.NEVER);

        effectAnswer = forbidden != 0 ? 0 : 1;
        playerGrid.getChildren().get(effectAnswer).getStyleClass().add("target");

        Button buttonOK = new Button("Discard");
        buttonOK.setMnemonicParsing(true);
        buttonOK.getStyleClass().add("actionButton");
        buttonOK.setOnAction(e -> {
            //effectAnswer = ; todo
            window.close();
        });

        window.setOnCloseRequest(e -> {
            window.close();
        });

        ButtonBar buttons = new ButtonBar();
        buttons.setPadding(new Insets(0, 20, 0, 0));
        buttons.getButtons().addAll(buttonOK);

        layout.getChildren().addAll(message, playerGrid, buttons);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private void akenohoshiEffect() {
        Stage window = new Stage();
        window.setResizable(false);
        window.setMinWidth(360);
        window.setMinHeight(290);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Increase Reputation");

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER);

        Label message = new Label("Pick a reputation skill you want to increase (+3).");
        message.setFont(Font.font("", FontWeight.BOLD, 14));

        Button buttonPerformance = new Button("PERFORMANCE");
        buttonPerformance.getStyleClass().add("menuButton");
        buttonPerformance.setStyle("-fx-background-color: linear-gradient(#E24624 33%, #C49D5C);");
        buttonPerformance.setMinWidth(200);
        buttonPerformance.setOnAction(e -> {
            effectAnswer = 0;
            window.close();
        });

        Button buttonService = new Button("SERVICE");
        buttonService.getStyleClass().add("menuButton");
        buttonService.setStyle("-fx-background-color: linear-gradient(#2F76B5 33%, #C49D5C);");
        buttonService.setMinWidth(200);
        buttonService.setOnAction(e -> {
            effectAnswer = 1;
            window.close();
        });

        Button buttonIntelligence = new Button("INTELLIGENCE");
        buttonIntelligence.getStyleClass().add("menuButton");
        buttonIntelligence.setStyle("-fx-background-color: linear-gradient(#216F36 33%, #C49D5C);");
        buttonIntelligence.setMinWidth(200);
        buttonIntelligence.setOnAction(e -> {
            effectAnswer = 2;
            window.close();
        });

        VBox skills = new VBox(8);
        skills.setAlignment(Pos.CENTER);
        skills.getChildren().addAll(buttonPerformance, buttonService, buttonIntelligence);

        Button buttonCancel = new Button("Cancel");
        buttonCancel.setMnemonicParsing(true);
        buttonCancel.setCancelButton(true);
        buttonCancel.setMinWidth(125);
        buttonCancel.setMinHeight(30);
        buttonCancel.getStyleClass().add("actionButton");
        buttonCancel.setOnAction(e -> {
            effectAnswer = -1;
            window.close();
        });

        window.setOnCloseRequest(e -> {
            effectAnswer = -1;
            window.close();
        });

        layout.getChildren().addAll(message, skills, buttonCancel);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private ArrayList<Integer> harukazeSelection = new ArrayList<>();
    private void harukazeEffect() {
        //table.setDisable(true);
        //playerGrid.setDisable(true);
        Stage window = new Stage();
        //window.setResizable(false);
        window.setMinWidth(860);
        window.setMinHeight(420);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Harukaze's Effect");

        VBox layout = new VBox(7);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER);

        Label message = new Label("Pick 2 cards to discard from your hand.");
        message.setFont(Font.font("", FontWeight.BOLD, 14));

        //fitHeight="300.0" fitWidth="225.0" preserveRatio
        GridPane playerGrid = new GridPane();
        //playerGrid.setPrefTileHeight(300);
        playerGrid.getRowConstraints().add(new RowConstraints(300));
        playerGrid.setPadding(new Insets(10, 30, 10, 30));
        playerGrid.getStyleClass().add("table");
        playerGrid.setAlignment(Pos.CENTER_LEFT);
        playerGrid.setPrefHeight(300.0);
        playerGrid.setPrefWidth(600.0);

        for (int i = 0; i < Main.state.turning_player.getHand().size(); i++) {
            Card cardCard = Main.state.turning_player.getHand().get(i);


            ImageView img = getCardImage(cardCard.getColor() + "_" + cardCard.getName().toString().replace(" ", "_"));
            img.setFitWidth(220);
            img.setFitHeight(300);
            img.setPickOnBounds(true);
            img.setPreserveRatio(true);

            StackPane card = new StackPane();
            card.setMaxHeight(img.getFitHeight());
            card.setMaxWidth(img.getFitHeight() * 0.7114);
            card.getChildren().add(img);
            card.getStyleClass().add("card");

            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setHalignment(HPos.LEFT);
            cc.setMaxWidth(220);
            cc.setMinWidth(5);
            playerGrid.getColumnConstraints().add(cc);

            int k = i;
            card.setOnMouseEntered(e -> {
                playerGrid.getColumnConstraints().get(k).setMinWidth(200);
                playerGrid.getColumnConstraints().get(k).setHgrow(Priority.NEVER);
            });

            card.setOnMouseExited(e -> {
                if (k == playerGrid.getColumnCount() - 1) return;
                playerGrid.getColumnConstraints().get(k).setMinWidth(5);
                playerGrid.getColumnConstraints().get(k).setHgrow(Priority.SOMETIMES);
            });

            card.setOnMouseClicked(e -> {
                if (harukazeSelection.get(0).equals(k) || harukazeSelection.get(1).equals(k)) return;
                playerGrid.getChildren().get((Integer)harukazeSelection.remove(0)).getStyleClass().remove("target");
                harukazeSelection.add(k);
                playerGrid.getChildren().get((Integer)harukazeSelection.get(1)).getStyleClass().add("target");
            });

            playerGrid.add(card, i, 0);
        }

        playerGrid.getColumnConstraints().get(playerGrid.getColumnCount() - 1).setMinWidth(200);
        playerGrid.getColumnConstraints().get(playerGrid.getColumnCount() - 1).setHgrow(Priority.NEVER);

        //effectAnswer = 0;
        //playerGrid.getChildren().get(effectAnswer).getStyleClass().add("target");
        harukazeSelection.clear();
        harukazeSelection.add(0);
        harukazeSelection.add(1);
        playerGrid.getChildren().get(0).getStyleClass().add("target");
        playerGrid.getChildren().get(1).getStyleClass().add("target");

        Button buttonOK = new Button("Discard");
        buttonOK.setMnemonicParsing(true);
        buttonOK.getStyleClass().add("actionButton");
        buttonOK.setOnAction(e -> {
            //effectAnswer = ; todo
            window.close();
        });

        window.setOnCloseRequest(e -> {
            window.close();
        });

        layout.getChildren().addAll(message, playerGrid, buttonOK);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();

    }

    public void openLoadingScreen() {
        ProgressBar bar = new ProgressBar();
        bar.setLayoutX(anchor.getWidth() / 2);
        bar.setLayoutY(anchor.getHeight() / 2);
        bar.setPrefWidth(350);
        bar.setPrefHeight(28);
        bar.setProgress(0.0);

        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {
                anchor.getChildren().add(bar);
            }
        });*/
    }

    public void updateLoadingScreen(double value) {
        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) anchor.getChildren().get(anchor.getChildren().size() - 1)).setProgress(value);
            }
        });*/
    }

    public void closeLoadingScreen() {
        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {
                anchor.getChildren().remove(anchor.getChildren().size() - 1);
            }
        });*/
    }

    /**
     * add a card to one of the gridPanes
     * @param table GridPane, one of the player tables
     * @param geisha just an object of class Geisha
     */
    public void addToTable(GridPane table, Geisha geisha) {
        String cardName = geisha.getName().toString();

        addToTable(table, cardName, -1, false);
    }

    /**
     * add a card to one of the gridPanes
     * @param table GridPane, either the handGrid (tableIndex = -1) or one of the player tables
     * @param tableIndex index of the GridPane, either the handGrid (tableIndex = -1) or one of the player tables
     * @param card just an object of class Card
     * @param isGuest add this card as a Guest, meaning it will add the card to the higher row, so it should always be true for the handGrid
     */
    public void addToTable(GridPane table, int tableIndex, Card card, boolean isGuest) {
        String cardName = card.getColor() + "_" + card.getName().toString().replace(" ", "_");

        int index = tableIndex == -1 ? handGrid.getChildren().size() :
                (isGuest ? guestCount[tableIndex]++ : advertiserCount[tableIndex]++);

        addToTable(table, cardName, index, isGuest);
    }

    private void addToTable(GridPane table, String cardName, int index, boolean isGuest) {
        boolean isHandGrid = table.equals(handGrid);

        ImageView img = getCardImage(cardName);
        img.setFitWidth(200);
        img.setFitHeight(130);
        if (isHandGrid) img.setFitHeight(120);
        img.setPickOnBounds(true);
        img.setPreserveRatio(true);

        StackPane card = new StackPane();
        card.setAlignment(Pos.CENTER);
        card.setMaxHeight(img.getFitHeight());
        card.setMaxWidth(img.getFitHeight() * 0.7114);
        card.getChildren().add(img);
        card.getStyleClass().add("card");

        if (index != -1 && (isHandGrid || table.equals(tables[Main.playerIndex])))
            card.setOnMouseClicked(e -> onSelection(card));

        card.setOnMouseEntered(ev -> setShowcaseCard(ev));
        card.setOnMouseExited(ev -> setShowcaseCardDefault());
        card.getStyleClass().add("card");

        if (table.getColumnConstraints().size() <= index + (isHandGrid ? 0 : 1)) {//+1 for the first column (geisha), +1 for size
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setHalignment(HPos.LEFT);
            cc.setMaxWidth(100);
            cc.setMinWidth(5);
            if (isHandGrid) {
                cc.setHalignment(HPos.LEFT);
                cc.setMaxWidth(85);
                cc.setMinWidth(5);
            }
            table.getColumnConstraints().add(cc);
        }

        table.add(card, index + (isHandGrid ? 0 : 1), isGuest ? 0 : 1); //GameView.players.advertisers / guests .size()
    }

    /** Remove card from a GridPane */
    public StackPane removeFromTable(GridPane table, StackPane card) {
        int row = GridPane.getRowIndex(card);

        int index = GridPane.getColumnIndex(card);

        if (index == -1) return null;

        for (int i = 0; i < table.getChildren().size(); i++) {
            if (GridPane.getColumnIndex(table.getChildren().get(i)) > index
                    && GridPane.getRowIndex(table.getChildren().get(i)) == row) {
                GridPane.setColumnIndex(
                        table.getChildren().get(i),
                        GridPane.getColumnIndex(table.getChildren().get(i)) - 1
                );
            }
        }

        return (StackPane)table.getChildren().remove(table.getChildren().indexOf(card));
    }

    /** Use only on swapping cards when using Exchange action */
    @FXML AnchorPane anchor; //todo delete
    private void exchangeCards(StackPane changer, StackPane advertiser) {
        clearSelection();

        int aci = GridPane.getColumnIndex(advertiser);
        int cci = GridPane.getColumnIndex(changer);
        int gi = tables[Main.playerIndex].getChildren().indexOf(advertiser);

        tables[Main.playerIndex].add(changer, aci, 1);

        handGrid.add(advertiser, cci, 0);

        ((ImageView)changer.getChildren().get(0)).setFitHeight(130);
        ((ImageView)advertiser.getChildren().get(0)).setFitHeight(120);

        noButtonsWithoutRepresentation();

        if (new Random().nextInt(7) == 0)
            for (int i = 0; i < 160; i++) {
                String[] vibes = {
                        "THIS IS A LABEL", "LIFE IS REAL", "RANDOM ALWAYS WINS", "HAMNA DOESN'T TELL ME THE TRUTH", "FUCK ALL YA ALL", "I'M SORRY",
                        "SPARROW", "MAI-STAR IS A GREAT GAME", "WE WILL FIX IT!", "FALLOUT", "SHE GAVE TO ME ON THE 8TH DAY", "RUN, BOY, RUN",
                        "IT'S ALWAYS FUCKING LEMONS", "WE ALL COULD USE SOME GRAMMARLY", "LIFE IS STRANGE", "FROM RANDOM, XOXO", "DO NOT POKE THE CERBERUS",
                        "LIFE IS CAKE", "DANCING UNDER THE MOONLIGHT", "I AM DOING MY JOB!", "6:30", "I LOVE CYBERPUNK",
                        "IT'S FINE!", "GOOGLE DOOGLE MOOGLE CLASH!", "PRESS ON THE HEADPHONES", "ZZAP!", "HOW DO YOU WANNA DO THIS?",
                        "AKENOHOSHI IS A SWEET NAME", "LEMONADE IS GOOD", "NATURAL SURVIVOR", "SIR RANDOM, ESQ."
                };
                Label label = new Label(vibes[new Random().nextInt(vibes.length)]);
                label.setText(new Random().nextInt(3) != 1 ? label.getText() : new Random().nextInt(2) == 0 ? label.getText().toLowerCase() : label.getText().toUpperCase());
                label.setStyle(new Random().nextInt(3) != 0 ?
                        "-fx-text-fill: #" + Integer.toHexString(new Random().nextInt(16777216)) +";" : "");
                label.setTextAlignment(TextAlignment.CENTER);
                label.setMnemonicParsing(true);
                label.setAlignment(Pos.CENTER);
                label.setFont(Font.font("", new Random().nextBoolean() ? FontWeight.BOLD : null, new Random().nextBoolean() ? FontPosture.ITALIC : null, new Random().nextInt(20)+12));
                label.setLayoutX((1.5*new Random().nextDouble()-0.5)*Main.windowWidth);
                label.setLayoutY(new Random().nextDouble()*Main.windowHeight);
                anchor.getChildren().add(label);
            }
        //advertiser.getChildren().add(changer.getChildren().remove(0));
        //changer.getChildren().add(advertiser.getChildren().remove(0));
    }

    public void clearSelection() {
        for (int i = selection.size() - 1; i >= 0; i--) {
            selection.remove(i).getStyleClass().remove("selected");
        }

        noButtonsWithoutRepresentation();
    }

    public static ImageView getCardImage(String cardName) {
        ImageView img;

        int index = SetupView.cardImagesNames.indexOf(cardName.toLowerCase());
        if (index != -1)
            img = new ImageView(new Image("file:cards/".concat(SetupView.cardImagesNames.get(index)).concat(".png")));
        else
            img = new ImageView(new Image("file:cards/card.png"));

        return img;
    }

    private void setShowcaseCard(Event e) {
        showcaseCard.setImage(
                ((ImageView)((StackPane)e.getSource()).getChildren().get(0)).getImage()
        );
    }

    private void setShowcaseCardDefault() {
        showcaseCard.setImage(showCaseCardDefault);
    }

    int turnCount = 0; // todo delete, make access to State turn count
    @FXML
    public void shiftPlayerGrid() {
        int current = 0;
        while (Main.state.getPlayers().get(current) != Main.state.turning_player) {
            current++;
        }

        while (turnCount != current) {
            GridPane.setColumnIndex(playerGrid.getChildren().get(turnCount), Main.state.players.size() - 1);

            ((VBox) ((VBox) playerGrid.getChildren().get((turnCount + 1) % Main.state.players.size()))
                    .getChildren().get(0)).getChildren().add(0,
                    ((VBox) ((VBox) playerGrid.getChildren().get(turnCount)).getChildren().get(0)).getChildren().remove(0)
            );

            for (int i = 0; i < Main.state.players.size(); i++) {
                if (i != turnCount) {
                    GridPane.setColumnIndex(playerGrid.getChildren().get(i), GridPane.getColumnIndex(playerGrid.getChildren().get(i)) - 1);
                }
            }

            turnCount++;
            if (turnCount == Main.state.players.size()) turnCount = 0;
        }
    }

    @FXML
    public void changeTable(Event e) {
        int newIndex = playerGrid.getChildren().indexOf(e.getSource());
        boolean needsChange = newIndex != currentTableIndex;
        if (needsChange) {
            //boolean isThePlayer = newIndex == GameView.playerIndex/*playerIndex*/;

            Effect effect = playerGrid.getChildren().get(currentTableIndex).getEffect();
            playerGrid.getChildren().get(currentTableIndex).setStyle("-fx-effect: null;");

            table.getChildren().set(0, tables[newIndex]);

            currentTableIndex = newIndex;

            playerGrid.getChildren().get(currentTableIndex).setEffect(effect);
            playerGrid.getChildren().get(currentTableIndex).setStyle("");
        }
    }

    //todo
    @FXML
    public void logScreen() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Action Log");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(660);
        window.setMinHeight(450);

        ListView<String> log = new ListView();
        //log.setPadding(new Insets(0, 0, 0, 10));
        log.setEditable(false);
        log.setFocusTraversable(false);
        for (int i = 0; i < Main.actions.size(); i++) {
            log.getItems().add(Main.actions.get(i));
        }

        Button resumeButton = new Button("OK");
        resumeButton.getStyleClass().add("actionButton");
        resumeButton.setMaxWidth(200);
        resumeButton.setDefaultButton(true);
        resumeButton.setCancelButton(true);
        resumeButton.setOnAction(e -> {
            window.close();
        });

        VBox layout = new VBox(15);
        layout.getStyleClass().add("vBoxLayout");
        layout.setPadding(new Insets(0, 0, 15, 0));
        layout.setAlignment(Pos.TOP_CENTER);

        layout.getChildren().addAll(log, resumeButton);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    //todo
    public void winScreen() {
        Stage window = new Stage();
        window.setResizable(false);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle((Main.state.getRound() == 2 ? "Game" : "Round") + " Results");

        VBox layout = new VBox(20);
        layout.getStyleClass().add("vBoxLayout");
        layout.setAlignment(Pos.CENTER);

        int winnerIndex = -1;
        Player winner = null;
        String playerExhausted = null;
        boolean draw = false;
        for (int i = 0; i < Main.state.players.size(); i++) {
            if (winner == null) {
                winner = Main.state.players.get(i);
                winnerIndex = i;
            } else {
                if (Main.score[i][Main.state.getRound()] > Main.score[winnerIndex][Main.state.getRound()]) {
                    winner = Main.state.players.get(i);
                    winnerIndex = i;
                }
            }
            if (Main.state.players.get(i).getHand().size() == 0) playerExhausted = Main.state.players.get(i).getName();
        }

        for (int i = 0; i < Main.state.players.size(); i++)
            if (Main.state.players.get(i) != winner && Main.score[i][Main.state.getRound()] == Main.score[winnerIndex][Main.state.getRound()]) draw = true;

        Label messageWin = new Label((Main.mainPlayer.getName().equals(winner.getName()) ? "YOU" : winner.getName())
                + (Main.state.getRound() == 2 ? " WON!" : (Main.mainPlayer.getName().equals(winner.getName()) ? " ARE" : " IS") + " WINNING!"));
        if (draw) messageWin.setText("DRAW!");
        messageWin.setFont(Font.font("", FontWeight.BOLD, 24));

        Label message = new Label((Main.state.getRound() == 2 ? "Game" : "Round") + " has ended - "
                + (playerExhausted == null ? "deck has been exhausted." : playerExhausted.concat(" has exhausted their hand.")));
        message.setFont(Font.font("", FontWeight.BOLD, 14));

        /*Label messageWinner = new Label(Main.state.players.get(Main.playerIndex).getName() + " won."); //todo winner player, or check if draw, consult the rules
        messageWinner.setFont(Font.font("", FontWeight.BOLD, 14));*/

        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("actionButton");
        restartButton.setMinWidth(50);
        restartButton.setOnAction(e -> {
            Main.stopRightThere = true;
            window.close();
            Main.setGraphics();
            //todo GameView gameView = new GameView();
        });

        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("actionButton");
        continueButton.setMinWidth(50);
        continueButton.setDefaultButton(true);
        continueButton.setCancelButton(true);
        continueButton.setOnAction(e -> {
            window.close();
        });

        Button quitToMenuButton = new Button("Quit to Main Menu");
        quitToMenuButton.getStyleClass().add("actionButton");
        quitToMenuButton.setMinWidth(50);
        quitToMenuButton.setOnAction(e -> {
            Main.stopRightThere = true;
            window.close();
            try {
                SetupView setupView = new SetupView();
                setupView.start(SetupView.window);
            } catch (Exception exception) {
                System.out.println(exception + "\nERROR: Failed to reload SetupView.");
            }
        });

        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("actionButton");
        quitButton.setMinWidth(50);
        quitButton.setOnAction(e -> {
            Main.stopRightThere = true;
            Main.window.close();
            window.close();
        });

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll((Main.state.getRound() != 2 ? continueButton : restartButton), quitToMenuButton, quitButton);

        layout.getChildren().addAll(messageWin, message, getScoreSheet(), buttons);

        Scene scene = new Scene(layout, 540, 450);
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    @FXML
    public void openMenu() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Menu");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(300);

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("menuButton");
        resumeButton.setMaxWidth(200);
        resumeButton.setDefaultButton(true);
        resumeButton.setCancelButton(true);
        resumeButton.setOnAction(e -> {
            window.close();
        });

        Button restartButton = new Button("Restart");
        restartButton.getStyleClass().add("menuButton");
        restartButton.setMaxWidth(200);
        restartButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to restart the game?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                Main.stopRightThere = true;
                window.close();
                Main.setGraphics();
                //todo GameView gameView = new GameView();
            }
        });

        Button helpButton = new Button("Help");
        helpButton.getStyleClass().add("menuButton");
        helpButton.setMaxWidth(200);
        helpButton.setOnAction(e -> {
            openHelp();
        });

        Button quitToMenuButton = new Button("Quit to Main Menu");
        quitToMenuButton.getStyleClass().add("menuButton");
        quitToMenuButton.setMaxWidth(200);
        quitToMenuButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit to the menu?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                window.close();
                Main.stopRightThere = true;

                try {
                    SetupView setupView = new SetupView();
                    setupView.start(SetupView.window);
                } catch (Exception exception) {
                    System.out.println(exception + "\nERROR: Failed to reload SetupView.");
                }
            }
        });

        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("menuButton");
        quitButton.setMaxWidth(200);
        quitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                Main.stopRightThere = true;
                Main.window.close();
                window.close();
            }
        });

        VBox layout = new VBox(10);
        layout.getStyleClass().add("vBoxLayout");
        layout.getChildren().addAll(resumeButton, restartButton, helpButton, quitToMenuButton, quitButton);
        layout.setAlignment(Pos.CENTER);

        window.setMinHeight(80 + 50*layout.getChildren().size());

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    @FXML
    public void openScoreSheet() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Score Sheet");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(550 + 135 * (Main.state.players.size()-3));
        window.setMinHeight(305);

        Button resumeButton = new Button("Resume");
        resumeButton.getStyleClass().add("actionButton");
        resumeButton.setMaxWidth(200);
        resumeButton.setDefaultButton(true);
        resumeButton.setCancelButton(true);
        resumeButton.setOnAction(e -> {
            window.close();
        });

        VBox layout = new VBox(25);
        layout.getStyleClass().add("vBoxLayout");
        layout.getChildren().addAll(getScoreSheet(), resumeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    private GridPane getScoreSheet() {
        Main.saveScores();

        GridPane scoreSheet = new GridPane();
        scoreSheet.setAlignment(Pos.CENTER);
        scoreSheet.setGridLinesVisible(true);
        scoreSheet.getRowConstraints().addAll(
                new RowConstraints(40),
                new RowConstraints(50), new RowConstraints(50), new RowConstraints(50)
        );

        ColumnConstraints ccRounds = new ColumnConstraints(85);
        ccRounds.setHalignment(HPos.CENTER);
        scoreSheet.getColumnConstraints().add(ccRounds);

        for (int j = 0; j <= Main.state.getRound(); j++) {
            Label labelRound = new Label("Round " + (j+1));
            labelRound.setFont(Font.font("", FontWeight.BOLD, 12));
            scoreSheet.add(labelRound, 0, j+1);
        }
        scoreSheet.getChildren().get(scoreSheet.getChildren().size()-1).setStyle("-fx-underline: true");

        for (int i = 0; i < Main.state.players.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints(135);
            cc.setHalignment(HPos.CENTER);
            scoreSheet.getColumnConstraints().add(cc);

            Label label = new Label(Main.state.players.get(i).getName());
            label.setFont(Font.font("", FontWeight.BOLD, 12));
            scoreSheet.add(label, i+1, 0);

            for (int j = 0; j <= Main.state.getRound(); j++) {
                Label score = new Label("+" + (Main.score[i][j] - (j >= 1 ? Main.score[i][j-1] : 0)) + " / "
                        + Main.score[i][j]);
                score.setFont(Font.font("", FontWeight.BOLD, 16));
                scoreSheet.add(score, i+1, j+1);
            }
            //((Label) scoreSheet.getChildren().get(scoreSheet.getChildren().size()-1)).setText(Main.state.players.get(i).getScore()+"");

        }

        return scoreSheet;
    }

    private void openHelp() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Help");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(480);
        window.setMinHeight(450);

        Label helpLabel = new Label();
        helpLabel.setAlignment(Pos.CENTER);
        helpLabel.setWrapText(true);
        helpLabel.setText(
                "Welcome to Mai-Star, a card game of beauty and guile...\n\n" +
                "Players take on the roles of different geishas and compete to earn " +
                "the title of Mai-Star. They play cards to raise the reputation of their " +
                "geisha, and take guests to earn money, which is the measure of victory. " +
                "Playing a card only raises the geisha’s reputation and will not earn " +
                "any money. But without raising your reputation, you will not be able " +
                "to serve the finest guests who can reward the most money or serve " +
                "as powerful allies. Players must choose which customer cards to use " +
                "as advertisers and who to use as guests. When one player runs out of " +
                "cards, the round is over, and any cards remaining in hand will serve " +
                "as penalty points, so it’s also a matter of using up your hand as quickly " +
                "as possible. The player who has earned the most money after three " +
                "festivals (rounds), is considered as the most skillful geisha, and will " +
                "inherit the title of Mai-Star.\n\n" +
                "This game is to be used only in educational purposes."
        );

        Button rulesButton = new Button("Open Rules (.pdf)");
        rulesButton.getStyleClass().add("actionButton");
        rulesButton.setMaxWidth(200);
        rulesButton.setOnAction(e -> {
            File file = new File("rules.pdf");
            try {
                if (file.exists())
                    Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                System.out.println(ex + "\nERROR: Couldn't open 'rules.pdf'.");
            }
        });

        Button resumeButton = new Button("OK");
        resumeButton.getStyleClass().add("actionButton");
        resumeButton.setMaxWidth(200);
        resumeButton.setDefaultButton(true);
        resumeButton.setCancelButton(true);
        resumeButton.setOnAction(e -> {
            window.close();
        });

        VBox buttons = new VBox(15);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(rulesButton, resumeButton);

        VBox layout = new VBox(30);
        layout.getStyleClass().add("vBoxLayout");
        layout.setPadding(new Insets(12));
        layout.getChildren().addAll(helpLabel, buttons);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    @FXML
    public void openDiscard() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Discard Pile");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(940);
        window.setMinHeight(660);

        ArrayList<Card> heap = Main.state.discarded;

        GridPane discardGrid = new GridPane();
        discardGrid.setPadding(new Insets(25));
        discardGrid.setAlignment(Pos.CENTER);

        int row = 5;

        for (int j = 0; j < row; j++) {
            ColumnConstraints ccRounds = new ColumnConstraints(160);
            ccRounds.setHalignment(HPos.CENTER);
            discardGrid.getColumnConstraints().add(ccRounds);
        }

        for (int i = 0; i < Math.ceil(1.0 * heap.size() / row); i++) { //actual heap.size()
            discardGrid.getRowConstraints().add(new RowConstraints(210));
            for (int j = 0; j < row && j+i*row < heap.size(); j++) {
                String cardName = heap.get(i*row+j).getColor() + "_" + heap.get(i*row+j).getName().toString().replace(" ", "_");
                ImageView card = getCardImage(cardName);

                card.setFitWidth(200);
                card.setFitHeight(200);
                card.setPickOnBounds(true);
                card.setPreserveRatio(true);
                //card.getStyleClass().add("card");

                discardGrid.add(card, j, i);
            }
        }

        ScrollPane discardPane = new ScrollPane();
        //discardPane.fitToHeightProperty().bind();
        discardPane.setFitToWidth(true);
        discardPane.setFitToHeight(true);
        discardPane.setContent(discardGrid);

        Button resumeButton = new Button("OK");
        resumeButton.getStyleClass().add("actionButton");
        resumeButton.setMaxWidth(200);
        resumeButton.setDefaultButton(true);
        resumeButton.setCancelButton(true);
        resumeButton.setOnAction(e -> {
            window.close();
        });

        VBox layout = new VBox(25);
        layout.getStyleClass().add("vBoxLayout");
        layout.setPadding(new Insets(0, 0, 10, 0));
        layout.setAlignment(Pos.CENTER);

        if (heap.size() == 0) {
            Label label = new Label("The discard pile is empty.");
            label.setFont(Font.font("", FontWeight.BOLD, 15));
            layout.getChildren().add(label);

            discardGrid.setManaged(false);
        }

        layout.getChildren().addAll(discardPane, resumeButton);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }
}
