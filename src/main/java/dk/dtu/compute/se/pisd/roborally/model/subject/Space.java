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
package dk.dtu.compute.se.pisd.roborally.model.subject;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.model.Coordination;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.CheckPoint;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.ConveyorBelt;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.Gear;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */

public class Space extends Subject {

    public final Board board;

    public final int x;
    public final int y;

    private Player player;
    public List<FieldAction> actions = new ArrayList<>();
    private List<Heading> walls = new ArrayList<>();

    private int startingPlayerNo;

    /**
     * @param board
     * @param x
     * @param y
     */
    public Space(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
        player = null;
    }

    /**
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @param player
     */
    public void setPlayer(Player player) {
        Player oldPlayer = this.player;
        if (player != oldPlayer &&
                (player == null || board == player.board)) {
            this.player = player;
            if (oldPlayer != null) {
                // this should actually not happen
                oldPlayer.setSpace(null);
            }
            if (player != null) {
                player.setSpace(this);
            }
            notifyChange();
        }
    }


    /**
     * @param startingPlayerNo
     */
    public void setStartingPlayerNo(int startingPlayerNo) {
        this.startingPlayerNo = startingPlayerNo;
        notifyChange();
    }

    /**
     * @return
     */
    public int getStartingPlayerNo() {
        return startingPlayerNo;
    }


    /**
     * @param heading
     * @return
     */
    public Space get_NBR_Space(@NotNull Heading heading) {
        int update_cordX;
        int update_cordY;
        switch (heading) {
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
            default -> {
                throw new IllegalStateException("Shouldn't occur; Not an expected value: " + heading);
            }
        }
        return this.board.getSpace(update_cordX, update_cordY);
    }

    public List<Heading> getWalls() {
        return walls;
    }

    /**
     * @param heading
     */
    public void addWall(Heading heading) {
        if (walls.contains(heading)) {
            return;
        }
        walls.add(heading);
        notifyChange();
    }

    public List<FieldAction> getActions() {
        return actions;
    }

    /**
     * @param action
     */
    public void addAction(FieldAction action) {
        this.actions.add(action);
        if (action instanceof CheckPoint) {
            this.board.setCheckpoint((CheckPoint) action);
        }
        notifyChange();
    }

    /**
     * @param coord
     */
    public void addGear(Coordination coord) {
        boolean status = false;
        for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
            FieldAction action = actions.get(i);
            if (action instanceof Gear) status = true;
        }
        if (status) {
            return;
        }
        this.actions.add(new Gear(coord));
        notifyChange();
    }

    /**
     * @return
     */
    public ConveyorBelt getConveyorBelt() {
        ConveyorBelt convoy = null;
        List<FieldAction> fieldActions = this.actions;
        for (int i = 0; i < fieldActions.size(); i++) {
            FieldAction action = fieldActions.get(i);
            if (action instanceof ConveyorBelt && convoy == null) convoy = (ConveyorBelt) action;
        }
        return convoy;
    }

    void playerChanged() {
        // This is a minor hack; since some views that are registered with the space
        // also need to update when some player attributes change, the player can
        // notify the space of these changes by calling this method.
        notifyChange();
    }
}