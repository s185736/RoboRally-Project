/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally;

import dk.dtu.compute.se.pisd.roborally.controller.AppController;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class RoboRally extends Application {

    private AppController appController = new AppController(this) ;

    private RoboRallyMenuBar menubar;

    private Menu controlMenu;

    private MenuItem saveGame;

    private MenuItem newGame;

    private MenuItem loadGame;

    private MenuItem stopGame;

    private MenuItem exitApp;

    private static final int MIN_APP_WIDTH = 600;

    private BoardView boardView = null;
    private Stage stage;
    private MenuBar menuBar;
    private BorderPane boardRoot;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        AppController appController = new AppController(this);

        // create the primary scene with the a menu bar and a pane for
        // the board view (which initially is empty); it will be filled
        // when the user creates a new game or loads a game
        MenuBar menuBar = createMenu(appController);
        boardRoot = new BorderPane();
        VBox vbox = new VBox(menuBar);
        vbox.setMinWidth(MIN_APP_WIDTH);
        Scene primaryScene = new Scene(boardRoot);

        stage.setScene(primaryScene);
        primaryStage.setTitle("Roborally");
        primaryStage.setOnCloseRequest(
                e -> {
                    e.consume();
                    appController.exit();} );
        boardRoot.setTop(menuBar);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private MenuBar createMenu(AppController appController) {

        menuBar = new MenuBar();
        Menu menu = new Menu("File");

        MenuItem item1 = new MenuItem("New Game");
        item1.setOnAction(e -> {
            this.boardView = appController.newGame();

            Scene scene = this.stage.getScene();
            Parent root = scene.getRoot();
            ((BorderPane) root).setCenter(this.boardView);


            stage.sizeToScene();
            stage.centerOnScreen();

            appController.startGame();

        });


        MenuItem item2 = new MenuItem("Stop Game");
        item2.setOnAction(e -> {
            appController.stopGame();
        });

        MenuItem item3 = new MenuItem("Save Game");
        item3.setOnAction(e -> {
            appController.saveGame();
        });

        Menu item4 = new Menu("Load Game");
        Map<String, Integer> games = appController.getGamesAsMenuItems();
        for (Map.Entry<String, Integer> game: games.entrySet()) {
            MenuItem loadItem = new MenuItem(game.getKey());
            loadItem.setId(game.getValue().toString());
            loadItem.setOnAction(e -> {
                this.boardView = this.appController.loadGame(Integer.parseInt(loadItem.getId()));
                Scene scene = this.stage.getScene();
                Parent root = scene.getRoot();
                ((BorderPane) root).setCenter(this.boardView);

                stage.sizeToScene();
                stage.centerOnScreen();


            });
            item4.getItems().add(loadItem);
        }



        MenuItem item5 = new MenuItem("Exit");
        item5.setOnAction(e -> {
            appController.exit();

        });

        menu.getItems().addAll(item1, item2, item3, item5);
        menuBar.getMenus().addAll(menu, item4);

        return menuBar;
    }

   public void createBoardView(GameController gameController) {
        // if present, remove old BoardView
        boardRoot.getChildren().clear();

        if (gameController != null) {
            // create and add view for new board
            BoardView boardView = new BoardView(gameController);
            boardRoot.setCenter(boardView);
        }

        stage.sizeToScene();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // XXX just in case we need to do something here eventually;
        //     but right now the only way for the user to exit the app
        //     is delegated to the exit() method in the AppController,
        //     so that the AppController can take care of that.

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}