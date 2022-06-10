package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;

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

        @Override
        public boolean doAction(GameController gameController, Space space) {
            return false;
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
    }