package dk.dtu.compute.se.pisd.roborally.databaseAccess;

import com.mysql.cj.util.StringUtils;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class DatabaseConnector {


    // Configurations / connections to DB.
    // -----------------------------------
    String host = "localhost"; //host is "localhost" or "127.0.0.1"
    String port = "3306"; //port is where to communicate with the RDBM system
    String database = "roborally"; //database containing tables to be queried

    // Set username and password.
    // -------------------------
    String username = "root";		// Username for connection
    String password = "123skole";	// Password for username
    String delimiter = ";;";

    Connection connection = null;

    DatabaseConnector(){
        try{
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC&useSSL=false";
            // Get connection to database.
            connection = DriverManager.getConnection(url, username, password);
            createSchemaDB(); //calling..
            System.out.println("Connected to the Database..");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // The schema is being connected to the Database.
    private void createSchemaDB(){
        String schema = IOUtil.readResource("schemas/databaseSchema.sql");
        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String SQL : schema.split(delimiter)){
                if (!StringUtils.isEmptyOrWhitespaceOnly(SQL)){
                    statement.executeUpdate(SQL);
                }
            }
            statement.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    Connection getDatabaseConnection(){
        return connection;
    }
}