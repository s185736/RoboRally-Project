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
import dk.dtu.compute.se.pisd.roborally.model.Antenna;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.CheckPoint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static dk.dtu.compute.se.pisd.roborally.model.Phase.INITIALISATION;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */

public  class Board extends Subject {

    public final int width;

    public final int height;

    private int gameId;

    private final Space[][] spaces;

    private final List<Player> players = new ArrayList<>();

    private List<CheckPoint> checkPoints = new ArrayList<>();

    private Antenna antenna;

    private Player current;

    private Phase phase = INITIALISATION;

    private int step = 0;

    private boolean stepMode;

    public int change;

    public String boardName;

    private String gameName;

    private ListIterator<Player> playerIte;

    /**
     * @param width
     * @param height
     */
    public Board(int width, int height, @NotNull String boardName) {
        this.boardName = boardName;
        this.width = width;
        this.height = height;
        spaces = new Space[width][height];
        this.antenna = new Antenna(this, ((int) (Math.random() * width)), ((int) (Math.random() * height)));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Space space = new Space(this, x, y);
                spaces[x][y] = space;
            }
        }
        this.stepMode = false;
    }

    /**
     * @param width
     * @param height
     * @param spaces
     */
    public Board(int width, int height, Space[][] spaces) {
        //this(width, height, "defaultboard");
        this.width = width;
        this.height = height;
        this.spaces = spaces;

    }

    public void sortPlayers(){
        playerIte=antenna.sortedPlayers(players).listIterator();
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
    /*
    public void setGameId(int gameId) {
        if (this.gameId == null) {
            this.gameId = gameId;
        } else {
            if (!this.gameId.equals(gameId)) {
                throw new IllegalStateException("A game with a set id may not be assigned a new id!");
            }
        }
    }*/

    /**
     * @param x
     * @param y
     * @return
     */
    public Space getSpace(int x, int y) {
        if (x >= 0 && x < width &&
                y >= 0 && y < height) {
            return spaces[x][y];
        } else {
            return null;
        }
    }

    /**
     * @return
     */
    public Space[][] getSpaces() {
        return spaces;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    /**
     * @param player
     */
    public void addPlayer(@NotNull Player player) {
        if (player.board == this && !players.contains(player)) {
            players.add(player);
            notifyChange();
        }
    }

    public Player getPlayer(int i) {
        if (i >= 0 && i < players.size()) {
            for (Player player: players) {
                if (player.no == i)
                    return player;
            }
        }

        return null;

    }

    public Player getCurrentPlayer() {
        return current;
    }

    /**
     * @param player
     */
    public void setCurrentPlayer(Player player) {
        if (player != this.current && players.contains(player)) {
            this.current = player;
            notifyChange();
        }
    }

    public Phase getPhase() {
        return phase;
    }

    /**
     * @param phase
     */
    public void setPhase(Phase phase) {
        if (phase != this.phase) {
            this.phase = phase;
            notifyChange();
        }
    }

    public int getStep() {
        return step;
    }

    /**
     * @param step
     */
    public void setStep(int step) {
        if (step != this.step) {
            this.step = step;
            notifyChange();
        }
    }
    public void setChange(int change) {
        this.change = change;
    }

    public int getChange() {
        return change;
    }

    /**
     * Returns the neighbour of the given space of the board in the given heading.
     * The neighbour is returned only, if it can be reached from the given space
     * (no walls or obstacles in either of the involved spaces); otherwise,
     * null will be returned.
     *
     * @param space the space for which the neighbour should be computed
     * @param heading the heading of the neighbour
     * @return the space in the given coord; null if there is no (reachable) neighbour
     */
    /*This can be removed, due to moving and improved this method in class: Space > get_NBR_Space*/
    /*public Space getNeighbour(@NotNull Space space, @NotNull Heading heading) {
        int x = space.x;
        int y = space.y;
        switch (heading) {
            case SOUTH:
                y = (y + 1) % height;
                break;
            case WEST:
                x = (x + width - 1) % width;
                break;
            case NORTH:
                y = (y + height - 1) % height;
                break;
            case EAST:
                x = (x + 1) % width;
                break;
        }

        return getSpace(x, y);
    }*/

    public Antenna getAntenna() {
        return this.antenna;
    }

    /**
     * @param antenna
     */
    public void setAntenna(Antenna antenna) {
        this.antenna = antenna;
        for (Space[] spaces : this.spaces) {
            for (Space space : spaces) {
                space.playerChanged();
            }
        }
    }


    public List<CheckPoint> getCheckPoints() {
        return this.checkPoints;
    }

    public void setCheckpoint(CheckPoint checkpoint) {
        this.checkPoints.add(checkpoint);
    }

    public boolean isStepMode() {
        return stepMode;
    }

    /**
     * @param stepMode
     */
    public void setStepMode(boolean stepMode) {
        if (stepMode != this.stepMode) {
            this.stepMode = stepMode;
            notifyChange();
        }
    }

    public List<Player> getPlayers() {
        return players;
    }


    public String getStatusMessage() {
        // this is actually a view aspect, but for making assignment V1 easy for
        // the students, this method gives a string representation of the current
        // status of the game

        // XXX: V2 changed the status so that it shows the phase, the player and the step
        return "Phase: " + getPhase().name() +
                ", Player = " + getCurrentPlayer().getName() +
                ", Step: " + getStep() +
                "(Amount of moves: "+getCount()+")";
    }
    /*Below; counting the number of moves in the board.*/
    private int count;

    /*Setter & Getter*/
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        if (this.count != count) {
            this.count = count;
            notifyChange();
        }
    }
    public Iterator<Player> getPlayerIte(){
        return playerIte;
    }
    public int getPlayersNumber() {
        return players.size();
    }

    public int getPlayerNo(@NotNull Player p) {
        if (p.board == this) {
            return players.indexOf(p);
        } else {
            return -1;
        }
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
