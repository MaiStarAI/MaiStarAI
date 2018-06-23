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

public class GameGraphics {
    //todo GameView if state is not pre-made, choose geishas and randomize hands
    //todo handGrid -> make border
    //todo foeTable -> on click, open the big screen on either clicking on the table or clicking on a specified area
    //todo action -> do actions multiple times
    //todo playerName, End Turn, help
    //todo foeTable : Geisha, cards in hand, win points, click to see table?, stats?
    //todo multiple tables, table turning displaying whose turn is it, tables dynamically updated
    //todo heap -> open window, GridPane, a few columns, many rows
    //todo rotating player count, highlight you, currently selected, current turn
    //todo disable all actions when its not players turn, the player can still look at cards, heap, score sheet, menu, tables. Do not stop the game when it's not your turn?
    //todo update every table as players take turns
    //todo turnNumber, roundNumber?
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
    @FXML public Button playGuest;
    @FXML public Button advertise;
    @FXML public Button introduce;
    @FXML public Button exchange;
    @FXML public Button search;
    @FXML public Button endTurnButton;

    @FXML public ImageView showcaseCard;
    private Image showCaseCardDefault;

    @FXML
    public void initialize() {
        showCaseCardDefault = showcaseCard.getImage();

        //if (!GameView.player.geisha.name.equals("Akenohoshi"))
        akenohoshiButton.setManaged(false);
        akenohoshiButton.setVisible(false);

        createTables();
        currentTableIndex = 0; //playerIndex

        menuButton.requestFocus();
    }

    private void createTables() {
        tables = new GridPane[GameView.players.size()];
        cardCounts = new Label[GameView.players.size()];
        scoreCounts = new Label[GameView.players.size()];

        for (int i = 0; i < GameView.players.size(); i++) {
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

            Label playerLabel = new Label(GameView.players.get(i)); //players.get(i).name
            playerLabel.getStyleClass().add("player1Label");
            playerLabel.setAlignment(Pos.CENTER);
            playerLabel.setMnemonicParsing(true);
            playerLabel.setTextAlignment(TextAlignment.CENTER);
            playerLabel.setWrapText(true);
            tables[i].add(playerLabel, 0, 0);

            addToTable(tables[i], "Harukaze", -1, false); //GameView.players.get(i).geisha
            addToTable(tables[i], "Red_Actor", 0, false);
            addToTable(tables[i], "green_emissary", 0, true);
            addToTable(tables[i], "green_emissary", 1, true);
            addToTable(tables[i], "green_emissary", 2, true);
            addToTable(tables[i], "green_emissary", 3, true);
            addToTable(tables[i], "green_emissary", 4, true);
            addToTable(tables[i], "green_emissary", 5, true);
            addToTable(tables[i], "green_emissary", 6, true);
            addToTable(tables[i], "green_emissary", 7, true);
            addToTable(tables[i], "green_emissary", 8, true);
            addToTable(tables[i], "green_emissary", 9, true);


            //<ColumnConstraints hgrow="SOMETIMES" minWidth="10.0" prefWidth="100.0" />
            ColumnConstraints ccInfo = new ColumnConstraints();
            ccInfo.setHgrow(Priority.SOMETIMES);
            ccInfo.setMinWidth(10);
            ccInfo.setPrefWidth(100);
            //playerGrid.getColumnConstraints().add(ccInfo);

            VBox playerInfo = new VBox();
            playerInfo.setAlignment(Pos.TOP_CENTER);
            playerInfo.setPrefSize(100, 120);

            Label playerName = new Label(GameView.players.get(i)); //.name
            playerName.getStyleClass().add("playerLabel");

            GridPane playerStats = new GridPane();
            playerStats.setGridLinesVisible(true); //todo replace with CSS
            for (int j = 0; j < 2; j++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setMinWidth(10);
                cc.setPrefWidth(100);
                cc.setHalignment(HPos.CENTER);
                cc.setHgrow(Priority.SOMETIMES);
                playerStats.getColumnConstraints().add(cc);

                RowConstraints rc = new RowConstraints();
                rc.setMinHeight(10);
                rc.setPrefHeight(20 + 13*j);
                rc.setVgrow(Priority.SOMETIMES);
                playerStats.getRowConstraints().add(rc);

                Label statName = new Label(j == 0 ? "HAND" : "SCORE");
                statName.getStyleClass().add("statNameLabel");
                playerStats.add(statName, j, 0);
            }

            cardCounts[i] = new Label("5"); // GameView.players.hand.size()
            cardCounts[i].setAlignment(Pos.CENTER);
            cardCounts[i].getStyleClass().add("statLabel");
            playerStats.add(cardCounts[i], 0, 1);

            scoreCounts[i] = new Label("0"); // GameView.players.score
            scoreCounts[i].setAlignment(Pos.CENTER);
            scoreCounts[i].getStyleClass().add("statLabel");
            playerStats.add(scoreCounts[i], 1, 1); //todo delete from FXML

            HBox tableBox = new HBox();
            tableBox.setAlignment(Pos.CENTER_LEFT);
            tableBox.setPrefSize(100, 76);
            tableBox.setDisable(true);
            tableBox.getChildren().add(tables[i]);

            double scale = tableBox.getPrefHeight() / tables[i].getPrefHeight();
            tableBox.setStyle(
                    "-fx-scale-x: " + scale + ";" +
                    "-fx-scale-y: " + scale + ";" +
                    "-fx-border-style: solid;"
            );

            playerInfo.getChildren().addAll(tableBox, playerName, playerStats);

            //playerGrid.add(playerInfo, i, 0);
            //((VBox)playerGrid.getChildren().get(i)).getChildren().set(0, playerInfo);

            //tables[i].setOnMouseClicked(e -> changeTable(e));
        }
        table.getChildren().set(0, tables[0]);
    }

    private void addToTable(GridPane table, String cardName, int index, boolean isGuest) { //todo delete INDEX, add CARDNAME parse
        ImageView card = getCardImage(cardName);
        card.setFitWidth(200);
        card.setFitHeight(130);
        //card.setOnMouseClicked(e -> setShowcaseCard(e)); SELECTION - if not foe table
        card.setOnMouseEntered(ev -> setShowcaseCard(ev)); //if not foe table
        card.setOnMouseExited(ev -> setShowcaseCardDefault()); //if not foe table
        card.setPickOnBounds(true);
        card.setPreserveRatio(true);
        card.getStyleClass().add("card");
        if (table.getColumnConstraints().size() <= index+1) {//+1 for the first column (geisha), +1 for size
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setMaxWidth(100);
            cc.setMinWidth(10);
            table.getColumnConstraints().add(cc);
        }
        table.add(card, index + 1, isGuest ? 0 : 1); //GameView.players.advertisers / guests .size()
    }

    private ImageView getCardImage(String cardName) {
        ImageView img;

        int index = SetupView.cardImagesNames.indexOf(cardName.toLowerCase());
        if (index != -1)
            img = new ImageView(new Image("file:cards/".concat(SetupView.cardImagesNames.get(index)).concat(".png")));
        else
            img = new ImageView(new Image("file:cards/card.png"));

        return img;
    }

    @FXML
    public void setShowcaseCard(Event e) {
        showcaseCard.setImage(
                ((ImageView)e.getSource()).getImage()
        );
    }

    @FXML
    public void setShowcaseCardDefault() {
        showcaseCard.setImage(showCaseCardDefault);
    }

    @FXML
    public void changeDeckLabel(Event e) {
        Label label = (Label)e.getSource();
        label.setText(Integer.parseInt(label.getText()) + 1 + "");
    }

    int turnCount = 0;
    @FXML
    public void shiftPlayerGrid() {
        GridPane.setColumnIndex(playerGrid.getChildren().get(turnCount), GameView.players.size()-1);
        for (int i = 0; i < GameView.players.size()-1; i++) { //todo remove -1
            if (i != turnCount) {
                GridPane.setColumnIndex(playerGrid.getChildren().get(i), GridPane.getColumnIndex(playerGrid.getChildren().get(i)) - 1);
            }
        }

        if (++turnCount == GameView.players.size()) turnCount = 0;//todo delete, turnCount is dealt with in the state node
    }

    ObservableList<Node> playerTableChildren;
    @FXML
    public void changeTable(Event e) {
        int newIndex = playerGrid.getChildren().indexOf(e.getSource());
        boolean needsChange = playerGrid.getChildren().indexOf(e.getSource()) == currentTableIndex;
        System.out.println(newIndex);
        if (!needsChange) { //todo it does need
            boolean isThePlayer = newIndex == 0/*playerIndex*/;

            table.getChildren().set(0, ((VBox)e.getSource()).getChildren().get(0));
            //setTable((GridPane)table.getChildren().get(0));

            for (int i = 0; i < 20; i++) { //guests.size. then advertisers.size
                addToTable((GridPane)table.getChildren().get(0), "green_actor", i, false); //todo delete
            }

            currentTableIndex = newIndex;
        }
    }

    private void setTable(GridPane table) {
        table = tables[currentTableIndex];
    }

    private void changeDeckLabel(int value) {
        deckLabel.setText(value + "");
    }

    private void changeHeapLabel(int value) {
        heapLabel.setText(value + "");
    }

    @FXML
    public void openMenu() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Menu");
        //window.getIcons().add(Window.icon);
        window.setMinWidth(310);
        window.setMinHeight(330);

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
                    ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                //todo
                window.close();
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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?",
                    ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                //todo
                window.close();
            }
            window.close();
        });

        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("menuButton");
        quitButton.setMaxWidth(200);
        quitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?",
                    ButtonType.YES, ButtonType.CANCEL);
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
        window.setMinWidth(550 + 135 * (GameView.players.size()-3));
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
            Label labelRound = new Label("Round " + (j+1)); //todo actual Player class (.getName)
            labelRound.setFont(Font.font("", FontWeight.BOLD, 12));
            scoreSheet.add(labelRound, 0, j+1);
        }

        for (int i = 0; i < GameView.players.size(); i++) {
            ColumnConstraints cc = new ColumnConstraints(135);
            cc.setHalignment(HPos.CENTER);
            scoreSheet.getColumnConstraints().add(cc);

            Label label = new Label(GameView.players.get(i)); //todo actual Player class (.getName)
            label.setFont(Font.font("", FontWeight.BOLD, 12));
            scoreSheet.add(label, i+1, 0);

            //for (j = 0; j < number of rounds) scoreSheet.getChildren().add(score[i][j]); todo
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
}
