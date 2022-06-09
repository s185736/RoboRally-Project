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

import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.*;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 * @author Malaz Alzarrad, s180424@dtu.dk
 */
public class GameController {

    final public Board board;
    public boolean won = false;

    /**
     * @param board
     */
    public GameController(@NotNull Board board) {
        this.board = board;
        if (this.board.getPhase() == Phase.INITIALISATION)
            this.board.setPhase(Phase.PROGRAMMING);
    }

    /**
     * This is just some dummy controller operation to make a simple move to see something
     * happening on the board. This method should eventually be deleted!
     *
     * @param space the space to which the current player should move
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space)  {
        // TODO Assignment V1: method should be implemented by the students:
        //   - the current player should be moved to the given space
        //     (if it is free()
        //   - and the current player should be set to the player
        //     following the current player
        //   - the counter of moves in the game should be increased by one
        //     if the player is moved

        if (space != null && space.board == board) {
            Player currentPlayer = board.getCurrentPlayer();
            if (currentPlayer != null && space.getPlayer() == null) {
                currentPlayer.setSpace(space);
                int playerNumber = (board.getNumberOfPlayers() + 1) % board.getNumberOfPlayers();
                board.setCurrentPlayer(board.getPlayer(playerNumber));
            }
        }

    }

    /**
     * @param player
     */
    public void findWinner(@NotNull Player player) {
        Alert winMsg = new Alert(Alert.AlertType.INFORMATION, "Player " + player.getName() + " has won the Game!");
        this.won = true;
        winMsg.showAndWait();
    }

    /**
     * @param player
     */
    public void moveForward(@NotNull Player player) {
        Space space = player.getSpace();
        int update_cordX,update_cordY;
        /*The positions will be updated and set as update_pos.*/
        if (player != null && player.board == board && space != null) {
            int x = space.x;
            int y = space.y;

            if (player.getSpace().get_NBR_Space(player.getHeading()).getPlayer() == null) {
            } else {
                Player nbr_player = player.getSpace().get_NBR_Space(player.getHeading()).getPlayer();
                moveForwardHeading(nbr_player, player.getHeading());
            }
            if (!player.getSpace().get_NBR_Space(player.getHeading()).getWalls().contains(player.getHeading().oppos())) {
                if (player.getSpace().getWalls().contains(player.getHeading())) {
                    return;
                }
                update_cordX = 0;
                update_cordY = 0;

                switch (player.getHeading()) {
                    case NORTH -> {
                        update_cordX = x;
                        update_cordY = (y - 1) % board.height;
                        if (update_cordY != -1) {
                        } else {
                            update_cordY = 7;
                        }
                    }
                    case SOUTH -> {
                        update_cordX = x;
                        update_cordY = (y + 1) % board.height;
                    }
                    case WEST -> {
                        update_cordX = (x - 1) % board.width;
                        if (update_cordX != -1) {
                        } else {
                            update_cordX = 7;
                        }
                        update_cordY = y;
                    }
                    case EAST -> {
                        update_cordX = (x + 1) % board.width;
                        update_cordY = y;
                    }
                }
                /*if no updates in the current position... then it will set the new position as current position.*/
                Space updated_pos;
                updated_pos = board.getSpace(update_cordX, update_cordY);
                if (updated_pos == null || updated_pos.getPlayer() != null || updated_pos == space) {
                    return;
                }
                updated_pos.setPlayer(player);
            } else {
                return;
            }
        }
    }

    /**
     * @param player
     * @param heading
     */
    public void moveForwardHeading(Player player, Heading heading) {
        Heading prevHeading = player.getHeading();
        player.setHeading(heading);
        moveForward(player);
        player.setHeading(prevHeading);
    }

    /**
     * @param player
     */
    public void fastForward(Player player) {
        moveForward(player);
        moveForward(player);
    }

    /**
     * @param player
     */
    public void turnRight(Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    /**
     * @param player
     */
    public void turnLeft(Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    /**
     * @param source
     * @param target
     * @return
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null & targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    // XXX: V2
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    // XXX: V2
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    /**
     * @param register
     */
    // XXX: V2
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getNumberOfPlayers(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * Replaced with notImplemented, this will execute the option of the players.
     *
     * @param player
     * @param command
     */
    public void executePlayerActions(Player player, Command command) {
        if (player != null && player.board == board && board.getCurrentPlayer() == player) {
            board.setPhase(Phase.ACTIVATION);
            execute(command);
        }
    }

    public void executePrograms() {
        board.setStepMode(false);
        while (board.getPhase() == Phase.ACTIVATION) {
            continuePrograms();
        }
    }

    // XXX: V2
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep(null);
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    private void execute(Command command) {
        executeNextStep(command);
        if (board.getPhase() == Phase.ACTIVATION && !board.isStepMode()) {
            executePrograms();
        }
    }

    // XXX: V2
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getNumberOfPlayers(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    player.getProgramField(j).setCard(null);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    player.getCardField(j).setCard(generateRandomCommandCard());
                }
            }
        }
    }

    /**
     * @param command
     */
    // XXX: V2
    private void executeNextStep(Command command) {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                if (command != null) {
                    executeCommand(currentPlayer, command);
                } else {
                    executeCommandCard(currentPlayer, currentPlayer.getProgramField(step).getCard());
                }
                if (board.getPhase() == Phase.ACTIVATION) {
                    if (currentPlayer.no + 1 < board.getNumberOfPlayers()) {
                        board.setCurrentPlayer(board.getPlayer(currentPlayer.no + 1));
                    } else {
                        for (Player player : this.board.getPlayers()) {
                            for (FieldAction action : player.getSpace().getActions()) {
                                if (won)
                                    break;
                                action.doAction(this, player.getSpace());
                            }
                        }

                        calculatePlayerOrder();
                        step++;
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    }
                }
            }
            if (board.getPhase() == Phase.ACTIVATION && (step < 0 || step >= Player.NO_REGISTERS)) {
                startProgrammingPhase();
            }
        }

        if (board.getPhase() == Phase.INITIALISATION) {
            startProgrammingPhase();
        }
    }

    private void executeCommandCard(@NotNull Player player, CommandCard card) {
        if (card != null) {
            executeCommand(player, card.command);
        }
    }

    /**
     * @param player
     * @param command
     */
    // XXX: V2
    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {
            // XXX This is an very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a much more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).
            if (command.isInteractive()) {
                board.setPhase(Phase.PLAYER_INTERACTION);
            } else {
                switch (command) {
                    case FORWARD:
                        this.moveForward(player);
                        break;
                    case RIGHT:
                        this.turnRight(player);
                        break;
                    case LEFT:
                        this.turnLeft(player);
                        break;
                    case FAST_FORWARD:
                        this.fastForward(player);
                        break;
                    default:
                        // DO NOTHING (for now)
                }
            }
        }
    }

    /**
     * @param noOfPlayers
     */
    public void createPlayers(int noOfPlayers) {
        String[] colors = new String[]{"red", "green", "blue", "orange", "grey", "purple"};

        for (int i = 0; i < noOfPlayers; i++) {

            Player player = new Player(this.board, colors[i], "Player " + (i + 1));
            player.setDatabaseNo(i);
            board.addPlayer(player);
            for(Space[] spacerow : this.board.getSpaces()){
                if (player.getSpace() != null) {
                    break;
                }

                for(Space space : spacerow){
                    if(space.getStartingPlayerNo() == (i + 1)){
                        player.setSpace(space);
                        break;
                    }
                }
            }

            if (player.getSpace() == null) {
                for (int j = 0; j < board.width; j++) {
                    Space space = board.getSpace(j, 0);
                    if (space != null && space.getPlayer() == null) {
                        player.setSpace(space);
                        break;
                    }
                }
            }

        }
        calculatePlayerOrder();
        board.setCurrentPlayer(board.getPlayer(0));
    }


    private void calculatePlayerOrder() {
        Antenna antenna = this.board.getAntenna();
        Map<Player, Integer> PlayersOrdered = new HashMap<Player, Integer>();

        for (int x = 0; x < this.board.width; x++) {
            for (int y = 0; y < this.board.height; y++) {
                Player player = this.board.getSpace(x, y).getPlayer();
                if (player != null) {

                    int length = Math.abs(antenna.x - x) + Math.abs(antenna.y - y);

                    PlayersOrdered.put(player, length);

                }
            }
        }

        List<Map.Entry<Player, Integer>> list = new LinkedList<>(PlayersOrdered.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Player, Integer>>() {
            @Override
            public int compare(Map.Entry<Player, Integer> o1, Map.Entry<Player, Integer> o2) {
                if (o1.getValue() == o2.getValue()) {
                    if (o2.getKey().getSpace().y > antenna.y && o1.getKey().getSpace().y > antenna.y) {
                        return o1.getKey().getSpace().x - o2.getKey().getSpace().x;
                    }

                    if (o2.getKey().getSpace().y < antenna.y && o1.getKey().getSpace().y < antenna.y) {
                        return o2.getKey().getSpace().x - o1.getKey().getSpace().x;
                    }

                    if (o2.getKey().getSpace().y > antenna.y || o1.getKey().getSpace().y > antenna.y) {
                        return o1.getKey().getSpace().x - o2.getKey().getSpace().x;
                    }


                } else {
                    return o1.getValue() - o2.getValue();
                }

                return 0;
            }
        });

        for (int i = 0; i < list.size(); i++) {
            list.get(i).getKey().no = i;
        }

        this.board.setCurrentPlayer(list.get(0).getKey());

    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }
}