package dk.dtu.compute.se.pisd.roborally.databaseAccess;

/**
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */

public class GameIndatabase {

    public final int id;
    public final String name;
    public final String boardName;

    public GameIndatabase(int id, String name, String boardName) {
        this.id = id;
        this.name = name;
        this.boardName = boardName;

    }

    @Override
    public String toString() {return boardName;}
}
