package dk.dtu.compute.se.pisd.roborally.databaseAccess;

import com.mysql.cj.util.StringUtils;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnector is used to connect to the database for the Roborally game.
 * The course 02327 Introductory Databases and Database Programming, has been used as inspiration for this class.
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class DatabaseConnector {

    // Configurations/connections to the database.
    String host = "localhost"; // The host is "localhost" or "127.0.0.1"
    String port = "3306"; //Port 3306 is used to communicate with the RDBM system.
    String database = "roborally"; //Database containing the tables from roborally.

    // Set username and password for the mySQL.
    String username = "root";		// Username for connection
    String password = "Sammy123";	// Password for username
    String dlm = ";;";

    // Initialize connection to null.
    Connection connection = null;

    DatabaseConnector(){
        try{
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC&useSSL=false";
            // Get connection to database.
            connection = DriverManager.getConnection(url, username, password);
            createSchema();
            System.out.println("Connected to the Database.");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // The schema is being connected to the Database.
    private void createSchema(){
        String schema = IOUtil.readResource("schemas/databaseSchema.sql");
        try {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            for (String SQL : schema.split(dlm)){
                if (!StringUtils.isEmptyOrWhitespaceOnly(SQL)){
                    stmt.executeUpdate(SQL);
                }
            }
            stmt.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    Connection getDatabaseConnection(){
        return connection;
    }
}