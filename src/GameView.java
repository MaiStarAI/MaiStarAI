import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class GameView {
    public static Stage window;
    public static Parent root;

    public static final int windowWidth = 1040;
    public static final int windowHeight = 690;

    public static ArrayList<String> players;

    GameView () {window = SetupView.window;
        window.setTitle("Mai-Star");

        window.setMinWidth(windowWidth);
        window.setMinHeight(windowHeight);

        window.setOnCloseRequest(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to quit?",
                    ButtonType.YES, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                window.close();
            }
            e.consume();
        });

        players = SetupView.players;
        players.add("Player 1");
        players.add("Megaliermo Del Moro [Random]");
        players.add("Player 3 [ISMCTS]");

        try {
            root = FXMLLoader.load(getClass().getResource("gameGraphics.fxml"));
        } catch (IOException e) {
            System.out.println(e + "\nERROR: couldn't load 'gameGraphics.fxml'");
        } finally {
            window.setScene(new Scene(root));
            window.show();

            setScene();
        }
    }

    private void setScene () {

    }
}
