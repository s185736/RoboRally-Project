package dk.dtu.compute.se.pisd.roborally.model.subject;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;
    private String color = "green";
    Space space;
    Board board;

    @Test
    public void haveLastCheckPointSet() {
        board = new Board(TEST_WIDTH,TEST_HEIGHT, String.valueOf(space));
        Player playerTester;
        playerTester = new Player(board, color);
        assertEquals(0, playerTester.getLastCheckPoints());
        playerTester.setLastCheckPoints(1);
        assertEquals(1, playerTester.getLastCheckPoints());
    }

    @Test
    public void haveCheckPointSetIfHigherThanLast() {
        board = new Board(TEST_WIDTH,TEST_HEIGHT, String.valueOf(space));
        Player playerTester;
        playerTester = new Player(board, color);
        playerTester.setLastCheckPoints(1);
        assertEquals(1, playerTester.getLastCheckPoints());
        playerTester.setLastCheckPoints(0);
        assertEquals(1, playerTester.getLastCheckPoints());
        assertNotEquals(0, playerTester.getLastCheckPoints());
    }
}