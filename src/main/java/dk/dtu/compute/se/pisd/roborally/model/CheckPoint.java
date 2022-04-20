package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;

public class CheckPoint {

    public final int points;

    /**
     * @param points
     */
    public CheckPoint(int points) {
        this.points = points;
    }

    /**
     * @param gameController
     * @param space
     * @return
     */
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player == null) {
            return true;
        }
        player.setLastCheckPoints(this.points);
        if (player.getLastCheckPoints() < gameController.board.getCheckPoints().size()) {
            return true;
        }
        gameController.findWinner(player);
        return true;
    }
}