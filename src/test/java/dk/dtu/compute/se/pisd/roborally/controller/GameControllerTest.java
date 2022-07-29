package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.*;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    AppController application;
    GameController boardGame;
    RoboRally roborally;
    public int playerAmount = 2;

    @org.junit.Before
    public void gameCreator() {
        this.application = new AppController(roborally);
        application.gameController = new GameController(application.createBoardFromLayout(null));
        this.boardGame = application.gameController;

    }

    @AfterEach
    void tearDown() {
        boardGame = null;
    }

    @Test
    void someTest() {
        Player playerTester = boardGame.board.getCurrentPlayer();
        boardGame.moveCurrentPlayerToSpace(boardGame.board.getSpace(0, 4));
        Assertions.assertEquals(playerTester, boardGame.board.getSpace(0, 4).getPlayer(), "Player " + playerTester.getName() + " should beSpace (0,4)!");
    }

    @Test
    public void moveForward() {
        boardGame.createPlayers(playerAmount);
        Player playerTester = boardGame.board.getPlayer(0);
        playerTester.setSpace(boardGame.board.getSpace(0,0));
        playerTester.setHeading(Heading.SOUTH);
        boardGame.moveForward(playerTester);
        Space playerSpace = playerTester.getSpace();
        assertEquals(0, playerSpace.x);
        assertEquals(1, playerSpace.y);
    }

    @Test
    public void turnLeft() {
        boardGame.createPlayers(playerAmount);
        Player playerTester = this.boardGame.board.getPlayer(0);
        playerTester.setHeading(Heading.NORTH);
        boardGame.turnLeft(playerTester);
        assertEquals(Heading.WEST, playerTester.getHeading());

    }

    @Test
    public void turnRight() {
        boardGame.createPlayers(2);
        Player playerTester = boardGame.board.getPlayer(0);
        playerTester.setHeading(Heading.NORTH);
        boardGame.turnRight(playerTester);
        assertEquals(Heading.EAST, playerTester.getHeading());
    }

    @Test
    public void pushInFront() {
        boardGame.createPlayers(2);
        Player playerTester1 = boardGame.board.getPlayer(0);
        playerTester1.setSpace(boardGame.board.getSpace(0,0));
        playerTester1.setHeading(Heading.SOUTH);
        Player playerTester2 = boardGame.board.getPlayer(1);
        playerTester2.setSpace(boardGame.board.getSpace(0,1));
        boardGame.moveForward(playerTester1);

        assertEquals(0, playerTester2.getSpace().x);
        assertEquals(2, playerTester2.getSpace().y);
        assertEquals(0, playerTester1.getSpace().x);
        assertEquals(1, playerTester1.getSpace().y);

    }

    @Test
    public void stopOnWall() {
        boardGame.createPlayers(playerAmount);
        Player playerTester = boardGame.board.getPlayer(0);
        Space testSpace = playerTester.getSpace();
        playerTester.setHeading(Heading.SOUTH);
        testSpace.get_NBR_Space(Heading.SOUTH).addWall(Heading.NORTH);
        boardGame.moveForward(playerTester);
        assertNotNull(playerTester.getSpace());
        assertEquals(testSpace, playerTester.getSpace());

    }

    @Test
    public void cannotMoveIfOtherPlayerCannotMove() {
        boardGame.createPlayers(playerAmount);
        /*test of two players.*/
        Player playerTester1 = boardGame.board.getPlayer(0);
        playerTester1.setSpace(boardGame.board.getSpace(0,0));
        playerTester1.setHeading(Heading.SOUTH);
        Player playerTester2 = boardGame.board.getPlayer(1);
        playerTester2.setSpace(boardGame.board.getSpace(0,1));
        boardGame.board.getSpace(0,2).addWall(Heading.NORTH);
        boardGame.moveForward(playerTester1);
        assertEquals(boardGame.board.getSpace(0,0),
                playerTester1.getSpace());
        assertEquals(boardGame.board.getSpace(0,1),
                playerTester2.getSpace());
    }
}