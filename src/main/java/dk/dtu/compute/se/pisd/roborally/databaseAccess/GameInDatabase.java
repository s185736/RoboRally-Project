package dk.dtu.compute.se.pisd.roborally.databaseAccess;

/**
 * This class holds id, name and gameName.
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class GameInDatabase {

    public final int gameID;
    public final String boardName;
    public final String gameName;

    public GameInDatabase(int id, String boardName, String gameName) {
        this.gameID = id;
        this.boardName = boardName;
        this.gameName = gameName;
    }

    @Override
    public String toString() {return gameName;}
}