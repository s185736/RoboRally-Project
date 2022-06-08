package dk.dtu.compute.se.pisd.roborally.databaseAccess;

import com.mysql.cj.util.StringUtils;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {


    // Tilpas variable til jeres database.
    // -----------------------------------
    String host = "localhost"; //host is "localhost" or "127.0.0.1"
    String port = "3306"; //port is where to communicate with the RDBM system
    String database = "roboRally"; //database containing tables to be queried
    String cp = "utf8"; //Database codepage supporting danish (i.e. æøåÆØÅ)

    // Set username og password.
    // -------------------------
    String username = "root";		// Username for connection
    String password = "02327";	// Password for username
    String delimiter = ";;";

    Connection connection = null;


    DatabaseConnector(){
        try{
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=" + cp;

            // Get connection to database.
            connection = DriverManager.getConnection(url, username, password);

            // database schema metode skal kaldes her
            createSchemaForDatabase();
            System.out.println("Connected to database");

        } catch (Exception e){
            e.getMessage();
        }
    }
    // der laves en metode der opretter en database schema
    private void createSchemaForDatabase(){
        String createStatementTables = IOUtil.readResource("schemas/databaseSchema.sql");

        try {
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            for (String SQL : createStatementTables.split(delimiter)){
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
