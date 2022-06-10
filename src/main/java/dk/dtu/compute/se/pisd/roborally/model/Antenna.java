package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 * Status: In progress.
 *
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class Antenna extends FieldAction {

        public Board board;
        public final int x;
        public final int y;
        public int ind;

        @Override
        public boolean doAction(GameController gameController, Space space) {
            Player player = space.getPlayer();
            if (player == null) {
                return true;
            }
            return true;
        }
        public Antenna(Board board, int x, int y) {
            this.board = board;
            this.x = x;
            this.y = y;
        }
        public Antenna(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getAntennaX() {
            return x;
        }

        public int getAntennaY() {
            return y;
        }

    public List<Player> playerListInSortedArray(List<Player> playerList) {
        List<Player> playerList_UnSorted;
        playerList_UnSorted = new ArrayList<>();
        for (int i = 0; i < playerList.size(); i++) {
            Player player = playerList.get(i);
            int step_x;
            step_x = player.getSpace().x;
            int step_y;
            step_y = player.getSpace().y;
            player.setAntennaDist(Math.sqrt((getAntennaX() - step_x) *(getAntennaX() - step_x) + (getAntennaY() - step_y) *(getAntennaY() - step_y)));
            playerList_UnSorted.add(player);
        }
        playersListInSortedArrays(playerList_UnSorted);
        return playerList_UnSorted;
    }

    public void playersListInSortedArrays(List<Player> playerList) {
        ind = 0;
        for (int i = 0; i < playerList.size() - 1; i++) {
            if (!(playerList.get(i).getAntennaDist() > playerList.get(i + 1).getAntennaDist())) {
                continue;
            }
            Player playerTemp = playerList.get(i);
            playerList.set(i, playerList.get(i + 1));
            playerList.set(i + 1, playerTemp);
            ind++;
        }
        while (ind > 0) {
            ind = 0;
            for (int i = 0; i < playerList.size() - 1; i++) {
                if (!(playerList.get(i).getAntennaDist() > playerList.get(i + 1).getAntennaDist())) {
                    continue;
                }
                Player playerTemp = playerList.get(i);
                playerList.set(i, playerList.get(i + 1));
                playerList.set(i + 1, playerTemp);
                ind++;
            }
        }
    }

}