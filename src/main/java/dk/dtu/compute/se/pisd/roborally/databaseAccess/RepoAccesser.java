package dk.dtu.compute.se.pisd.roborally.databaseAccess;


public class RepoAccesser {
    private static Repo repo;

    public static Repo getRepository() {
        if(repo == null) {
            repo = new Repo(new DatabaseConnector());
        }
        return repo;
    }
}
