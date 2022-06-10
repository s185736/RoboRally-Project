package dk.dtu.compute.se.pisd.roborally.view.board;

import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 *  @author Sammy Chauhan, s191181@dtu.dk
 *  @author Azmi Uslu, s185736@dtu.dk
 *  @author Malaz Alzarrad, s180424@dtu.dk
 *
 */

public class SpaceLayout {

    public int playerNo;
    public int x;
    public int y;
    public List<Heading> walls = new ArrayList<>();
    public List<FieldAction> actions = new ArrayList<>();

    public SpaceLayout getInfoFromGameSpace(Space space) {
        this.x = space.x;
        this.y = space.y;
        this.playerNo = space.getStartingPlayerNo();
        this.walls = space.getWalls();
        this.actions = space.getActions();
        return this;
    }

    public Space getInfoToGameSpace(Board board) {
        Space space = new Space(board, this.x, this.y);
        space.setStartingPlayerNo(this.playerNo);
        actions.forEach(space::addAction);
        walls.forEach(space::addWall);
        return space;
    }
}