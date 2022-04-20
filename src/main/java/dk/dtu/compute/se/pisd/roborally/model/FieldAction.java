package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;

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