package dk.dtu.compute.se.pisd.roborally.view.board;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * ...
 *
 *  @author Sammy Chauhan, s191181@dtu.dk
 *  @author Azmi Uslu, s185736@dtu.dk
 *  @author Malaz Alzarrad, s180424@dtu.dk
 *
 */

public class Designer extends Application {

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        int width;
        int height;

        Controller controller;
        BorderPane borderPane;
        Scene primaryScene;

        TextInputDialog dialog = new TextInputDialog();

        dialog.setContentText("Selection: Width? ");
        dialog.showAndWait();
        width = Integer.parseInt(dialog.getResult());

        dialog.setContentText("Selection: Height? ");
        dialog.showAndWait();
        height = Integer.parseInt(dialog.getResult());

        controller = new Controller(width, height);

        stage.setTitle("Board Designer..");
        borderPane = new BorderPane();
        primaryScene = new Scene(borderPane);
        stage.setScene(primaryScene);
        borderPane.setCenter(controller.newViewOperation());
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
