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
package dk.dtu.compute.se.pisd.roborally.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.gameLoader.GameLoader;
import dk.dtu.compute.se.pisd.roborally.gameLoader.InterfaceAdapter;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.GameInDatabase;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.Repo;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.RepoAccesser;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.BoardModel;
import dk.dtu.compute.se.pisd.roborally.model.InterfaceAdapter;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 * @author Malaz Alzarrad, s180424@dtu.dk
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    final private ArrayList<String> BOARD_NAMES = LoadBoard.ShowTheListOfTheGameBoards();
    public GameController gameController;
    private RoboRally roboRally;
    private Board board = null;
    private BoardView boardView;
    private int chosenNumberOfPlayers = 0;
    private Repo repo = RepoAccesser.getRepo();
    private AppController appController;

    /**
     * @param roboRally
     */
    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    /**
     * @return
     */
    public BoardView newGameMain() {
        ChoiceDialog<String> boardSelection = new ChoiceDialog<>("defaultboard", BOARD_NAMES);
        boardSelection.setTitle("Board choice");
        boardSelection.setHeaderText("Select a board");
        Optional<String> choice = boardSelection.showAndWait();

        ChoiceDialog<Integer> playerAmountSelection = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        playerAmountSelection.setTitle("Player number");
        playerAmountSelection.setHeaderText("Select number of players");
        Optional<Integer> result = playerAmountSelection.showAndWait();

        if (result.isPresent() && choice.isPresent()) {
            if (gameController != null) {
                if (!stopGame()) {
                }
            }
            Board board = LoadBoard.loadBoard(choice.get());
            gameController = new GameController(board);

            int x = result.get();
            for (int i = 0; i < x; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, i));
            }
            gameController.startProgrammingPhase();
            if (board.getCurrentPlayer() == null) {
                board.setCurrentPlayer(board.getPlayer(0));
            }

            return boardView = new BoardView(gameController);
        }
        return null;
    }

    /**
     * @return
     */
    public BoardView newGame() {
        this.board = null;
        this.boardView = null;
        this.gameController = null;
        this.chosenNumberOfPlayers = 0;

        return newGameMain();
    }

    /**
     * @return
     */
    public boolean stopGame() {
        if (gameController != null) {
            // here we save the game (without asking the user).
            saveGame();
            gameController = null;
            // nedenunder skal måske udkommenteres
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        if (gameController != null) {
            /*Exit alert.*/
            Alert alertExit = new Alert(Alert.AlertType.CONFIRMATION);
            alertExit.setTitle("Exit RoboRally?");
            alertExit.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> buttonType1 = alertExit.showAndWait();
            if (!buttonType1.isPresent() || buttonType1.get() != ButtonType.OK) {
                return;
            }
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);

            /*Saving alert.*/
            Alert alertSaveBefore = new Alert(Alert.AlertType.CONFIRMATION, "Want to save before leaving?", yes, no);
            alertSaveBefore.setTitle("Do you want to save the Game?");
            Optional<ButtonType> buttonType2 = alertSaveBefore.showAndWait();

            if (!buttonType2.isPresent() || buttonType2.get() != yes) {
                Platform.exit();
            } else if (buttonType2.get() == yes) {
                stopGame();
                Platform.exit();
            } else {
                buttonType2.get();
            }
        }

        if (gameController == null) {
            Platform.exit();
        }
    }


    public void saveGame() {
        // XXX needs to be implemented eventually
        //implementering af save game
        // https://attacomsian.com/blog/gson-write-json-file
        LoadBoard gameSaver = new LoadBoard();
        //saveBoard(this.board, "defaultboard");

        if (gameController.board.getGameId() == null) {
            TextInputDialog text = new TextInputDialog("Name of the game");
        }

        if (gameController.board.getGameId() == 0) {
            TextInputDialog dialog = new TextInputDialog("Game name");
            dialog.setTitle("Chose name for save");
            dialog.setHeaderText("Name your saved game");
            dialog.setContentText("Please enter game name:");
            Optional<String> result = dialog.showAndWait();
            gameController.board.setGameName(result.get());

            /*Can be reduced, but doing the below for testing.*/
            if (result.isPresent()) {
                RepoAccesser.getRepo().insertCreatedGame(gameController.board);
                System.out.println("Game is added and saved in database");
            } else {
                System.out.println("Alert: Game has been updated.");
            }
            if (!result.isPresent()) {
                RepoAccesser.getRepo().updateCreatedGame(gameController.board);
                System.out.println("Game updated in database");
            } else {
                System.out.println("Alert: Game has been saved and added.");
            }
        }
    }

    public Map<String, Integer> getGamesAsMenuItems(){
        Map<String, Integer> gameElements = new HashMap<>();
        List<GameInDatabase> games = repo.getGamesFromDatabase();
        if (!games.isEmpty()) {
            for (GameInDatabase game: games) {
                gameElements.put(game.gameName, game.gameID);
            }
        }
        return gameElements;
    }

    public void startGame() {
        this.gameController.startProgrammingPhase();
    }

    public BoardView loadGame(Integer id) {
        // XXX needs to be implememted eventually
        // for now, we just create a new game
        Board board = RepoAccesser.getRepo().loadCreatedGame(id);
        gameController = new GameController(board);
        System.out.println("Game loaded");

        int no = board.getPlayersNumber();
        for (int i = 0; i < no; i++) {
            Player player = board.getPlayer(i);
            board.addPlayer(player);
            player.setSpace(board.getPlayer(i).getSpace());
        }
        if (gameController == null) {
            return newGame();

        } else return boardView = new BoardView(gameController);
    }

    public boolean isGameRunning() {
        return gameController != null;
    }

    public void update(Subject subject) {
        // XXX do nothing for now
    }

    public Board createBoardFromLayout(String layout) {

        GsonBuilder simpleBuilder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new InterfaceAdapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        InputStream is = null;

        if (layout.equals("default")) {
            is = this.getClass().getClassLoader().getResourceAsStream("boards/defaultboard.json");
        } else {
            try {
                is = new FileInputStream(new File(layout));
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "An error occured during the loading.");
                alert.showAndWait();
                e.printStackTrace();
            }
        }

        try {
            InputStreamReader isr = new InputStreamReader(is);

            JsonReader reader = gson.newJsonReader(isr);

            BoardModel boardTemplate = gson.fromJson(reader, BoardModel.class);
            Board board = boardTemplate.toBoard();

            reader.close();
            return board;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}