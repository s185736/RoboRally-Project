package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Coordination;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

/**
 * ...
 *
 * @author
 *
 */
public class Gear implements FieldAction {

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