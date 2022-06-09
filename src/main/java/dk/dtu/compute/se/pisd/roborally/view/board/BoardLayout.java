package dk.dtu.compute.se.pisd.roborally.view.board;

import dk.dtu.compute.se.pisd.roborally.model.Antenna;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author
 *
 */

public class BoardLayout {

    public int x;
    public int y;
    public int ant_X;
    public int ant_y;

    public List<SpaceLayout> splay = new ArrayList<>();

    public BoardLayout getInfoFromGameBoard(Board board) {
        this.x = board.width;
        this.y = board.height;
        if (board.getAntenna() != null) {
            this.ant_X = board.getAntenna().x;
            this.ant_y = board.getAntenna().y;
        }

        for (int i = 0; i < board.width; i++) {
            int j = 0;
            while (j < board.height) {
                if (board.getSpace(i, j).getWalls().isEmpty() && board.getSpace(i, j).getActions().isEmpty() && board.getSpace(i, j).getStartingPlayerNo() == 0) {
                } else {
                   splay.add((new SpaceLayout()).getInfoFromGameSpace(board.getSpace(i,j)));
                }
                j++;
            }
        }
        return this;
    }

    public Board getInfoToGameBoard() {
        Board board;
        Antenna antenna;
        board = new Board(this.x, this.y,"cæemw");
        antenna = new Antenna(board, this.ant_X, this.ant_y);
        board.setAntenna(antenna);
        splay.stream().map(spaceLayout -> spaceLayout.getInfoToGameSpace(board)).forEach(space -> board.getSpaces()[space.x][space.y] = space);

        for (int i = 0; i < board.width; i++) {
            int j = 0;
            while (j < board.height) {
                if (board.getSpace(i, j) != null) {
                } else {
                    board.getSpaces()[i][j] = new Space(board,i,j);
                }
                j++;
            }
        }
        return board;
    }
}