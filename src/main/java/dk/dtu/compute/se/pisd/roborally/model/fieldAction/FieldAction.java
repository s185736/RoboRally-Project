package dk.dtu.compute.se.pisd.roborally.model.fieldAction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

public interface FieldAction {

    /**
     *
     * Status: Still in progress.
     *
     * @param gameController
     * @param space
     * @return
     */
    boolean doAction(GameController gameController, Space space);
}