package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.model.subject.Board;

public class Antenna {

    /*Defining the variables, we're going to use.*/
    public final Board board;
    public final int x;
    public final int y;

    /**
     * @param board
     * @param x
     * @param y
     */
    public Antenna(Board board, int x, int y) {
        this.board = board;
        this.x = x;
        this.y = y;
    }
}