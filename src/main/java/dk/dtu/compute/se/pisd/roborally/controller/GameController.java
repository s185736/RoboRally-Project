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
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static java.util.Collections.*;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class GameController {

    final public Board board;
    public boolean won = false;

    public GameController(@NotNull Board board) {
        this.board = board;
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
                int playerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
                board.setCurrentPlayer(board.getPlayer(playerNumber));
            }
        }

    }

    // XXX: V2
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    // XXX: V2
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (random() * commands.length);
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

    // XXX: V2
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
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


    private void findPlayerOrder() {
        Antenna object = this.board.getAntenna();
        Map<Player, Integer> order = new HashMap<>();
        int x = 0;
        while (x < this.board.width) {
            for (int y = 0; y < this.board.height; y++) {
                Player player = this.board.getSpace(x, y).getPlayer();
                if (player == null) {
                    continue;
                }
                int final_length = abs(object.x - x) + abs(object.y - y);
                order.put(player, final_length);
            }
            x++;
        }

        List<Map.Entry<Player, Integer>> list;
        list = new LinkedList<>(order.entrySet());
        sort(list, (robot1, robot2) -> {
            if (robot1.getValue() != robot2.getValue()) {
                return robot1.getValue() - robot2.getValue();
            } else {
                /*If so, all robots have the same space/length to the object.*/
                if (robot2.getKey().getSpace().y <= object.y || robot1.getKey().getSpace().y <= object.y) {
                    if (robot2.getKey().getSpace().y < object.y && robot1.getKey().getSpace().y < object.y) {
                        return robot2.getKey().getSpace().x - robot1.getKey().getSpace().x; //All robots are under the object.
                    }
                    if (robot2.getKey().getSpace().y <= object.y && robot1.getKey().getSpace().y <= object.y) {
                        return 0;
                    }
                    return robot1.getKey().getSpace().x - robot2.getKey().getSpace().x; //One robot is over the object.
                } else {
                    return robot1.getKey().getSpace().x - robot2.getKey().getSpace().x; //All robots are over the object.
                }
            }
        });
        IntStream.range(0, list.size()).forEach(i -> list.get(i).getKey().no = i);
        this.board.setCurrentPlayer(list.get(0).getKey());
    }

    
    // XXX: V2
    private void executeNextStep(Command command) {
        Player currPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currPlayer.getProgramField(step).getCard();
                if (card != null) {
                    command = card.command;
                    executeCommand(currPlayer, command);
                } else {
                    executeCommandOptionAndContinue(currPlayer, currPlayer.getProgramField(step).getCard());
                }
                int nextPlayerNumber = board.getPlayerNumber(currPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    findPlayerOrder();
                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    // XXX: V2
    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
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
     * Replaced with notImplemented, this will execute the option of the players.
     *
     * @param player
     * @param option
     */
    public void executePlayerActions(Player player, Command option) {
        if (player != null && player.board == board && board.getCurrentPlayer() == player) {
            board.setPhase(Phase.ACTIVATION);
            execute(option);
        }
    }

    /**
     * Executing the program...
     *
     * @param command
     */
    private void execute(Command command) {
        executeNextStep(command);
            executePrograms();
    }


    /**
     * @param player
     */
    public void findWinner(Player player) {
        Alert finalAnnouncement = new Alert(Alert.AlertType.INFORMATION, player.getName() + " has won the Game!");
        this.won = true;
        finalAnnouncement.showAndWait();
    }

    /**
     *
     * This method should then switch the game back to the ACTIVATION phase, execute the selected option for the current player,
     * and then switch the execution of the robots ' program to the next program card
     *
     * status: in progress
     * @param player
     * @param card
     */
    private void executeCommandOptionAndContinue(@NotNull Player player, CommandCard card) {
        if (card == null) {
            return;
        }
        executeCommand(player, card.command);
    }

    // TODO: V2
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
    public void moveForwardHeading(@NotNull Player player, Heading heading) {
        Heading prev_heading = player.getHeading();
        player.setHeading(heading);
        moveForward(player);
        player.setHeading(prev_heading);
    }

    // TODO: V2
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
    }

    // TODO: V2
    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    // TODO: V2
    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
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