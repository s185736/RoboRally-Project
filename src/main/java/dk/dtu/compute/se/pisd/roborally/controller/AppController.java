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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.GameIndatabase;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.Repo;
import dk.dtu.compute.se.pisd.roborally.databaseAccess.RepoAccesser;
import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.gameLoader.GameLoader;
import dk.dtu.compute.se.pisd.roborally.model.InterfaceAdapter;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import dk.dtu.compute.se.pisd.roborally.view.board.BoardLayout;
import javafx.application.Platform;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard.loadBoard;
import static dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard.saveBoard;
import static java.lang.Integer.parseInt;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    final private ArrayList<String> BOARD_NAMES = LoadBoard.ShowTheListOfTheGameBoards();

    private GameController gameController;
    private RoboRally roboRally;
    private Board board = null;
    private BoardView boardView;
    private int chosenNumberOfPlayers = 0;
    private Repo repo = RepoAccesser.getRepository();
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
        newGameSetup(); //temp solution..
        return null;
    }

    public void newGameSetup() {
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
                    return;
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
            if (board.getCurrentPlayer()==null) {
                board.setCurrentPlayer(board.getPlayer(0));
            }

            roboRally.createBoardView(gameController);
        }
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
    public int numberOfPlayers() {

        /*Following will give the amount of items to players.*/
        List amountOfItems = new ArrayList<Integer>();
        for (int i = 2; i <= 6; i++) {
            amountOfItems.add(i);
        }

        ChoiceDialog choiceD = new ChoiceDialog();
        choiceD.setTitle("Player number");
        choiceD.setHeaderText("Select number of players: ");
        choiceD.getItems().addAll(amountOfItems);
        choiceD.setSelectedItem(choiceD.getItems().get(0));
        choiceD.showAndWait();

        /*
        if (result.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }
         */

        /*
        // XXX the board should eventually be created programmatically or loaded from a file
            //     here we just create an empty board with the required number of players.
            Board board = new Board(8,8);
            gameController = new GameController(board);
            int no = result.get();
            for (int i = 0; i < no; i++) {
                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                board.addPlayer(player);
                player.setSpace(board.getSpace(i % board.width, i));
            }
        */

        if (choiceD.getSelectedItem() == null) {
            String msg = "You've tried " + ++this.chosenNumberOfPlayers + " times, game will shutdown after the 3. try.";
            Alert alert = new Alert(Alert.AlertType.ERROR, msg);
            alert.showAndWait();
            if (this.chosenNumberOfPlayers >= 3) {
                System.exit(-1);
            }
            numberOfPlayers();
        }
        /*
         // XXX: V2
            // board.setCurrentPlayer(board.getPlayer(0));
            gameController.startProgrammingPhase();

            roboRally.createBoardView(gameController);
         */
        return parseInt(choiceD.getSelectedItem().toString());
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
            Alert alertExit = new Alert(Alert.AlertType.CONFIRMATION);
            alertExit.setTitle("Exit RoboRally?");
            alertExit.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alertExit.showAndWait();
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
            ButtonType accept = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType denied = new ButtonType("No", ButtonBar.ButtonData.NO);
            Alert alertSave = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to save the game before exitting?", accept, denied);

            alertSave.setTitle("Save Board Game?");
            Optional<ButtonType> re = alertSave.showAndWait();

            if (!re.isPresent() || re.get() != accept) {
                Platform.exit(); // return without exiting the application
            } else if (re.get() == accept){
                stopGame();
                Platform.exit();
            }
            else {
                re.get();
            }
        }
        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null) {
            Platform.exit();
        }
    }

    /**
     * @param layout
     * @return
     */
    public Board createBoardFromLayout(String layout) {
        GsonBuilder simpleBuilder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new InterfaceAdapter<FieldAction>());
        Gson gson = simpleBuilder.create();

        InputStream inputS = null;

        if (layout == null) {
            return new Board(8,8,"default");
        }

        /*Will read info through the json file...*/
        if (layout.equals("default")) inputS = this.getClass().getClassLoader().getResourceAsStream("icons/info.json");
        else {
            try {
                inputS = new FileInputStream(layout);
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error occured while loading the file..");
                alert.showAndWait();
                e.printStackTrace();
            }
        }

        try {
            InputStreamReader isr = new InputStreamReader(inputS);
            JsonReader reader = gson.newJsonReader(isr);
            BoardLayout boardTemplate = gson.fromJson(reader, BoardLayout.class);
            Board board = boardTemplate.getInfoToGameBoard();
            reader.close();
            return board;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * @return
     */
    private String selectBoardLayout() {
        GameLoader fileloader = new GameLoader();
        String ss_name = fileloader.fileloaderinsider();
        return ss_name;

    }

    public void saveGame() {
        if (gameController.board.getGameId() == null) {
            TextInputDialog textSave = new TextInputDialog("Game name");
            textSave.setTitle("Chose name for save");
            textSave.setHeaderText("Name your saved game");
            textSave.setContentText("Please enter game name:");
            Optional<String> result = textSave.showAndWait();
            gameController.board.setGameName(result.get());
            if(result.isPresent()) {
                RepoAccesser.getRepository().insertGame(gameController.board);
                System.out.println("The Game Board is now saved in DB.");
            }
        } else {
            RepoAccesser.getRepository().updateGame(gameController.board);
            System.out.println("The Game Board is now updated in DB.");
        }
    }

    public Map<String, Integer> getGamesAsMenuItems(){
        Map<String, Integer> gameElements = new HashMap<>();
        List<GameIndatabase> games = repo.getGames();
        if (!games.isEmpty()) {
            for (GameIndatabase game: games) {
                gameElements.put(game.gameName, game.id);
            }
        }
        return gameElements;
    }

    public void startGame() {
        this.gameController.startProgrammingPhase();
    }

    public void loadGameSetup(int id) {
        // XXX needs to be implememted eventually
        // for now, we just create a new game
        Board board = RepoAccesser.getRepository().loadGame(id);
        gameController = new GameController(board);
        System.out.println("Game has been loaded.");

        int x = board.getPlayersNumber();
        for (int i = 0; i < x; i++) {
            Player player = board.getPlayer(i);
            board.addPlayer(player);
            player.setSpace(board.getPlayer(i).getSpace());
        }
        roboRally.createBoardView(gameController);
    }

    public BoardView loadGame(Integer id) {
        // XXX needs to be implememted eventually
        // for now, we just create a new game
        loadGameSetup(id);
        return null;
    }

    public boolean isGameRunning() {
        return gameController != null;
    }

    public void update(Subject subject) {
        // XXX do nothing for now
    }

}