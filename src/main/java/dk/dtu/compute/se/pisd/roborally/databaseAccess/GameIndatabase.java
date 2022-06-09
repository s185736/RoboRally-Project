package dk.dtu.compute.se.pisd.roborally.databaseAccess;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class GameIndatabase {

    public final int id;
    public final String name;
    public final String gameName;

    public GameIndatabase(int id, String name, String gameName) {
        this.id = id;
        this.name = name;
        this.gameName = gameName;

    }

    @Override
    public String toString() {return gameName;}
}