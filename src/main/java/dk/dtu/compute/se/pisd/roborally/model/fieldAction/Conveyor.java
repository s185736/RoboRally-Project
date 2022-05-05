package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;
import org.jetbrains.annotations.NotNull;

/**
 * ...
 *
 * @author
 *
 */

public class Conveyor implements FieldAction {

    private Heading heading;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }

    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player currentPlayer = space.getPlayer();
        Space neighbourSpace = space.get_NBR_Space(this.heading);
        currentPlayer.setHeading(this.heading);
        if (neighbourSpace.getPlayer() == null) {
            currentPlayer.setSpace(neighbourSpace);
            neighbourSpace.actions.stream().filter(action -> action instanceof Conveyor && ((Conveyor) action).getHeading() != this.heading.oppos()).forEach(action -> action.doAction(gameController, currentPlayer.getSpace()));
            return true;
        } else {
            return false;
        }
    }
}