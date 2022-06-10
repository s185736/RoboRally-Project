package dk.dtu.compute.se.pisd.roborally.view.board;

import dk.dtu.compute.se.pisd.roborally.model.subject.Board;

/**
 * ...
 *
 *  @author Sammy Chauhan, s191181@dtu.dk
 *  @author Azmi Uslu, s185736@dtu.dk
 *  @author Malaz Alzarrad, s180424@dtu.dk
 *
 */

public class Controller {

    private DesignerView dv;
    private int x;
    private int y;
    private Board board;

    public Controller(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void newCretBoard() {
        //this.board = new Board(this.x, this.y);
        this.board.setAntenna(null);
    }

    public DesignerView newViewOperation() {
        if (this.board == null) {
            this.newCretBoard();
            this.dv = new DesignerView(this.board);
        } else {
            this.dv = new DesignerView(this.board);
        }
        return this.dv;
    }
}