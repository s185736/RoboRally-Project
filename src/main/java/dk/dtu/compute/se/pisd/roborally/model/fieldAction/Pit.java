package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

/**
 * ...
 *
 * @author
 *
 */
public class Pit implements FieldAction {

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Board board = space.board;
        Player player = space.getPlayer();
        if (player == null) {
            return false;
        }
        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                Space space1 = board.getSpace(i,j);
                if (space1.getStartingPlayerNo() != (player.getDatabaseNo() + 1) || space1.getPlayer() != null) {
                    continue;
                }
                player.setSpace(space1);
                return true;
            }
        }
        return false;
    }
}