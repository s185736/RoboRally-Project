package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Coordination;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

/**
 * ...
 *
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 * @author Malaz Alzarrad, s180424@dtu.dk
 */
public class Gear extends FieldAction {

    public Coordination coord;

    public Gear(Coordination coord) {
        this.coord = coord;
    }

    @Override
    public boolean doAction(GameController gameController, Space space){
        Player player = space.getPlayer();
        switch (coord) {
            case LEFT -> gameController.turnLeft(player);
            case RIGHT -> gameController.turnRight(player);
        }
        return true;
    }
}