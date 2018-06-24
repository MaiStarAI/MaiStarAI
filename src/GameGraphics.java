import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The FXML controller class
 * It handles functions connected with Graphics
 */
public class GameGraphics {
    //todo GameView if state is not pre-made, choose geishas and randomize hands
    //todo action -> do actions multiple times
    //todo rotating player count, highlight you, currently selected, current turn
    //todo disable all actions when its not players turn, the player can still look at cards, heap, score sheet, menu, tables. Do not stop the game when it's not your turn?
    //todo update every table as players take turns
    //todo turnNumber, roundNumber? --See round number in scoreSheet
    //todo let player know what actions other players took

    @FXML public GridPane playerGrid;
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

    @FXML public Button akenohoshiButton;
    @FXML public Button playGuestButton;
    @FXML public Button advertiseButton;
    @FXML public Button introduceButton;
    @FXML public Button exchangeButton;
    @FXML public Button searchButton;
    @FXML public Button endTurnButton;

    @FXML public ImageView showcaseCard;
    private Image showCaseCardDefault;
    public ArrayList</*ImageView*/StackPane > selection;

    /** Automatically accessed on successful .fxml load */
    @FXML
    public void initialize() {
        showCaseCardDefault = showcaseCard.getImage();

        currentTableIndex = GameView.playerIndex;

        selection = new ArrayList<>();

        if (!GameView.state.players.get(GameView.playerIndex).geisha.name.toString().equals("Akenohoshi")) {
            akenohoshiButton.setManaged(false);
            akenohoshiButton.setVisible(false);
        }

        createTables();
        fillHand();
        updatePlayerGrid();

        //todo DO NOT BIND ACTIONS TO BUTTONS YET

        menuButton.requestFocus();
    }

    /** Creates and updates player tables, access only once */
    private void createTables() {
        tables = new GridPane[GameView.state.players.size()];
        cardCounts = new Label[GameView.state.players.size()];
        scoreCounts = new Label[GameView.state.players.size()];

        for (int i = 0; i < GameView.state.players.size(); i++) {
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
            ccg.setMinWidth(98); //10
            ccg.setMaxWidth(105); //100
            ccg.setPrefWidth(105);
            ccg.setHgrow(Priority.SOMETIMES);
            ccg.setHalignment(HPos.CENTER); //null
            tables[i].getColumnConstraints().add(ccg);

            Label playerLabel = new Label(GameView.state.players.get(i).name); //players.get(i).name
            playerLabel.getStyleClass().add("player1Label");
            playerLabel.setAlignment(Pos.CENTER);
            playerLabel.setMnemonicParsing(true);
            playerLabel.setTextAlignment(TextAlignment.CENTER);
            playerLabel.setWrapText(true);
            tables[i].add(playerLabel, 0, 0);

            addToTable(tables[i], GameView.state.players.get(i).geisha); //GameView.players.get(i).geisha

            ((Label)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(1))
                    .setText(i != currentTableIndex ? GameView.state.players.get(i).name : GameView.state.players.get(i).name.concat(" (you)")); //.name

            cardCounts[i] = new Label(GameView.state.players.get(i).hand.size()+""); // GameView.players.hand.size()
            cardCounts[i].setAlignment(Pos.CENTER);
            cardCounts[i].getStyleClass().add("statLabel");
            ((GridPane)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(2))
                        .add(cardCounts[i], 0, 1);

            scoreCounts[i] = new Label(GameView.state.players.get(i).score+""); // GameView.players.score
            scoreCounts[i].setAlignment(Pos.CENTER);
            scoreCounts[i].getStyleClass().add("statLabel");
            ((GridPane)((VBox)playerGrid.getChildren().get(i))
                    .getChildren().get(2))
                    .add(scoreCounts[i], 1, 1);

            //tables[i].setOnMouseClicked(e -> changeTable(e));
        }
        playerGrid.getChildren().remove(GameView.state.players.size(), 6);
        playerGrid.getColumnConstraints().remove(GameView.state.players.size(), 6);

        table.getChildren().set(0, tables[currentTableIndex]);

        //playerGrid.getChildren().get(0).getStyleClass().add("selected");
        ((VBox)((VBox)playerGrid.getChildren().get(currentTableIndex)).getChildren().get(0))
                .getChildren().add(0,
                ((VBox)((VBox)playerGrid.getChildren().get(0)).getChildren().get(0)).getChildren().remove(0)
        );
    }

    /** Reload the player hand grid contents */
    public void fillHand() {
        //todo do dynamic changes, not change whole grid
        handGrid.getChildren().clear();
        for (int i = 0; i < GameView.state.players.get(GameView.playerIndex).hand.size(); i++) {
            Card card = GameView.state.players.get(GameView.playerIndex).hand.get(i);
            addToTable(handGrid, -1, card, true);
        }
    }

    /** Update the number on the Deck */
    public void updateDeck() {
        deckLabel.setText(GameView.state.drawDeck + "");
    }

    /** Update the number on the Discard Pile */
    public void updateDiscard() {
        heapLabel.setText(75-GameView.state.drawDeck + ""); //discardPile todo
    }

    /** Update the number on the Score Sheet button */
    public void updateScore() {
        scoreLabel.setText(GameView.state.players.get(GameView.playerIndex).score + "");
    }

    /** Update scores and hand numbers of players on the Player Card grid */
    public void updatePlayerGrid() {
        for (int i = 0; i < GameView.state.players.size(); i++) {
            cardCounts[i].setText(GameView.state.players.get(i).cardsNumber+"");
            scoreCounts[i].setText(GameView.state.players.get(i).score+"");
        }

        updateScore();
    }

    /**
     * add a card to one of the gridPanes
     * @param table GridPane, one of the player tables
     * @param geisha just an object of class Geisha
     */
    public void addToTable(GridPane table, Geisha geisha) {
        String cardName = geisha.name.toString();

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
        String cardName = card.color + "_" + card.name.toString().replace(" ", "_");

        int index = tableIndex == -1 ? handGrid.getChildren().size() - 1 :
            (isGuest ? GameView.state.players.get(tableIndex).guests.size() - 1 : GameView.state.players.get(tableIndex).advertisers.size() - 1);

        addToTable(table, cardName, index, isGuest);
    }

    private void addToTable(GridPane table, String cardName, int index, boolean isGuest) { //todo delete INDEX, add CARDNAME parse
        ImageView img = getCardImage(cardName);
        img.setFitWidth(200);
        img.setFitHeight(130);
        if (table.equals(handGrid)) img.setFitHeight(120);
        img.setPickOnBounds(true);
        img.setPreserveRatio(true);

        StackPane card = new StackPane();
        card.setAlignment(Pos.CENTER);
        card.setMaxHeight(img.getFitHeight());
        card.setMaxWidth(img.getFitHeight() * 0.7114);
        card.getChildren().add(img);
        card.getStyleClass().add("card");

        //todo there be selection code, perhaps, place it in another function
        card.setOnMouseClicked(e -> {
            if (selection.size() >= 2) {
                selection.remove(0).getStyleClass().remove("selected");
            }
            selection.add(card);
            selection.get(selection.size()-1).getStyleClass().add("selected");

            /*if (index == -1) {
                playGuestButton.setDisable(true);
                advertiseButton.setDisable(true);
                introduceButton.setDisable(true);
                exchangeButton.setDisable(true);
                searchButton.setDisable(true);
            }*/ // else {...

        });
        //card.setOnMouseClicked(e -> setShowcaseCard(e)); SELECTION (get advertisers / guests index, not THE^ index)
        card.setOnMouseEntered(ev -> setShowcaseCard(ev));
        card.setOnMouseExited(ev -> setShowcaseCardDefault());
        card.getStyleClass().add("card");

        if (table.getColumnConstraints().size() <= index + 1) {//+1 for the first column (geisha), +1 for size
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setHalignment(HPos.CENTER);
            cc.setMaxWidth(100);
            cc.setMinWidth(5);
            if (table.equals(handGrid)) {
                cc.setHalignment(HPos.LEFT);
                cc.setMaxWidth(85);
                cc.setMinWidth(5);
            }
            table.getColumnConstraints().add(cc);
        }

        table.add(card, index + 1, isGuest ? 0 : 1); //GameView.players.advertisers / guests .size()
    }

    private void dropSelection() {
        for (int i = selection.size() - 1; i >= 0; i--) {
            selection.remove(i).getStyleClass().remove("selected");
        }

        playGuestButton.setDisable(false);
        advertiseButton.setDisable(false);
        introduceButton.setDisable(false);
        exchangeButton.setDisable(false);
        searchButton.setDisable(false);

        akenohoshiButton.setDisable(false);
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
        GridPane.setColumnIndex(playerGrid.getChildren().get(turnCount), GameView.state.players.size()-1);
        //playerGrid.getChildren().get(turnCount).getStyleClass().remove("selected");
        Effect effect = playerGrid.getChildren().get(turnCount).getEffect();
        playerGrid.getChildren().get(turnCount).setStyle("-fx-effect: null;");

        for (int i = 0; i < GameView.state.players.size(); i++) {
            if (i != turnCount) {
                GridPane.setColumnIndex(playerGrid.getChildren().get(i), GridPane.getColumnIndex(playerGrid.getChildren().get(i)) - 1);
            }
        }

        if (++turnCount == GameView.state.players.size()) turnCount = 0;//todo delete, turnCount is dealt with in the state node
        //playerGrid.getChildren().get(turnCount).getStyleClass().add("selected");
        playerGrid.getChildren().get(turnCount).setEffect(effect);
        playerGrid.getChildren().get(turnCount).setStyle("");
    }

    @FXML
    public void changeTable(Event e) {
        int newIndex = playerGrid.getChildren().indexOf(e.getSource());
        boolean needsChange = newIndex != currentTableIndex;
        if (needsChange) {
            boolean isThePlayer = newIndex == GameView.playerIndex/*playerIndex*/;

            //table.getChildren().set(0, ((VBox)e.getSource()).getChildren().get(0));
            //setTable((GridPane)table.getChildren().get(0));

            ((VBox)((VBox)e.getSource()).getChildren().get(0))
                    .getChildren().add(0,
                        ((VBox)((VBox)playerGrid.getChildren().get(currentTableIndex)).getChildren().get(0)).getChildren().remove(0)
            );

            table.getChildren().set(0, tables[newIndex]);

            currentTableIndex = newIndex;
        }
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
                //todo
                window.close();

                GameView gameView = new GameView();
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
                //todo
                window.close();

                try {
                    SetupView setupView = new SetupView();
                    setupView.start(SetupView.window);
                } catch (Exception exception) {
                    System.out.println(exception + "\nERROR: Failed to reload SetupView.");
                }
            }
            window.close();
        });

        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("menuButton");
        quitButton.setMaxWidth(200);
        quitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?",
                    ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                GameView.window.close();
                window.close();
            }
            window.close();
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
        window.setMinWidth(550 + 135 * (GameView.state.players.size()-3));
        window.setMinHeight(305);

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

        for (int j = 0; j < 3; j++) {
            Label labelRound = new Label("Round " + (j+1));
            labelRound.setFont(Font.font("", FontWeight.BOLD, 12)); //todo underscore current round
            scoreSheet.add(labelRound, 0, j+1);
        }

        for (int i = 0; i < GameView.state.players.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints(135);
            cc.setHalignment(HPos.CENTER);
            scoreSheet.getColumnConstraints().add(cc);

            Label label = new Label(GameView.state.players.get(i).name); //todo actual Player class (.getName)
            label.setFont(Font.font("", FontWeight.BOLD, 12));
            scoreSheet.add(label, i+1, 0);

            for (int j = 0; j < 1/*round count*/; j++) {
                Label score = new Label(GameView.state.players.get(i).score+"");
                score.setFont(Font.font("", FontWeight.BOLD, 16));
                scoreSheet.add(score, i+1, j+1);//score[i][j]); //todo fill scores
            }
        }

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
        layout.getChildren().addAll(scoreSheet, resumeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
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
                "Hello! This is Mai-Star.\n" +
                "Mai-Star is a relly great game, you shud totaly chek it out! BLALBLAVLAfeiqn wagiwrg iwargiwragqwgwari gwrog wrg oqef\n"
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

        ArrayList<String> heap = new ArrayList<>(); //todo delete
        heap.addAll(Arrays.asList("red_actor", "red_emissary", "black_monk", "blue_thief", "red_doctor", "green_courtier", "blue_merchant", "green_merchant",
                "red_actor", "red_emissary", "black_monk", "blue_thief", "red_doctor", "green_courtier", "blue_merchant", "green_merchant",
                "red_actor", "red_emissary", "black_monk", "blue_thief", "red_doctor", "green_courtier", "blue_merchant", "green_merchant"));

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
                ImageView card = getCardImage(heap.get(i*row+j)); //.name

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
        layout.getChildren().addAll(discardPane, resumeButton);
        layout.setPadding(new Insets(0, 0, 10, 0));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, window.getMinWidth(), window.getMinHeight());
        scene.getStylesheets().add(this.getClass().getResource("graphics.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }
}
