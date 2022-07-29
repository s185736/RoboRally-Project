package dk.dtu.compute.se.pisd.roborally.databaseAccess;

/**
 * A singleton class used to provide us access to the repository.
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class RepoAccesser {
    private static Repo repo;

    public static Repo getRepo() {
        if(repo == null) {
            repo = new Repo(new DatabaseConnector());
        }
        return repo;
    }
}
