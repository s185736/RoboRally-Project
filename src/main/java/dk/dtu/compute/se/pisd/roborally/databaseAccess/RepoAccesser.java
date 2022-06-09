package dk.dtu.compute.se.pisd.roborally.databaseAccess;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class RepoAccesser {
    private static Repo repo;

    public static Repo getRepository() {
        if(repo == null) {
            repo = new Repo(new DatabaseConnector());
        }
        return repo;
    }
}
