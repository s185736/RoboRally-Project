package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

/**
 * ...
 *
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 * @author Malaz Alzarrad, s180424@dtu.dk
 */
public class CheckPoint extends FieldAction {

   public final int no;

    /**
     * @param points
     */
    public CheckPoint(int no) {
        this.no = no;
    }

    /**
     * @param gameController
     * @param space
     * @return
     */
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player == null) {
            return true;
        }
        player.setLastCheckPoints(this.no);
        if (player.getLastCheckPoints() < gameController.board.getCheckPoints().size()) {
            return true;
        }
        if (player.getLastCheckPoints() >= 2) {
            gameController.findWinner(player);
        }
        return true;
    }
}
