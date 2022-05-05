package dk.dtu.compute.se.pisd.roborally.view.board;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * ...
 *
 * @author
 *
 */

public class Designer extends Application {

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        int width, height;
        Controller controller;
        BorderPane root;
        Scene primaryScene;

        TextInputDialog textD = new TextInputDialog();
        textD.setContentText("Choose the width of the board: ");
        textD.showAndWait();
        width = Integer.parseInt(textD.getResult());
        textD.setContentText("Choose the height of the board: ");
        textD.showAndWait();
        height = Integer.parseInt(textD.getResult());
        controller = new Controller(width, height);
        stage.setTitle("The Designer of the Board!");
        root = new BorderPane();
        primaryScene = new Scene(root);
        stage.setScene(primaryScene);
        root.setCenter(controller.newViewOperation());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
