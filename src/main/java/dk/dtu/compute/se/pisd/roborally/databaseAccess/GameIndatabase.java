package dk.dtu.compute.se.pisd.roborally.databaseAccess;

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
