package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

/**
 * ...
 *
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 * @author Malaz Alzarrad, s180424@dtu.dk
 */
public class Pit extends FieldAction {

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Board boardGame = space.board;
        Player player = space.getPlayer();
        if (player == null) {
            return false;
        }
        for (int i = 0; i < boardGame.width; i++) {
            for (int j = 0; j < boardGame.height; j++) {
                Space spc = boardGame.getSpace(i,j);
                if (spc.getStartingPlayerNo() != (player.getDatabaseNo() + 1) || spc.getPlayer() != null) {
                    continue;
                }
                player.setSpace(spc);
                return true;
            }
        }
        return false;
    }
}