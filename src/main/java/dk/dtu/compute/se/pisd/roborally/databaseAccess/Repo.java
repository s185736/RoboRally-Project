package dk.dtu.compute.se.pisd.roborally.databaseAccess;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Command;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Phase;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.CommandCard;
import dk.dtu.compute.se.pisd.roborally.model.subject.CommandCardField;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class Repo{

    private final DatabaseConnector databaseConnector;

    Repo(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }
    private static final String Database_CardSelect = "SELECT * FROM Card_field_command WHERE Game_id = ?";
    private PreparedStatement STMT_Card_Select = null;

    private static final String Database_GamesSelect = "SELECT game_id, board_name, game_name FROM game ORDER BY game_id DESC LIMIT 10";
    private PreparedStatement STMT_Games_Select = null;

    private static final String Database_GameSelect = "SELECT * FROM Game WHERE game_id = ?";
    private PreparedStatement STMT_Game_Select = null;

    public boolean insertCreatedGame(Board boardGame) {
            Connection DBConnector = databaseConnector.getDatabaseConnection();
            try {
                DBConnector.setAutoCommit(false);
                PreparedStatement pStatement = DBConnector.prepareStatement(
                        "INSERT INTO Game(board_name, current_player, phase, step, game_name) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                pStatement.setString(1, boardGame.boardName);
                pStatement.setNull(2, boardGame.getCurrentPlayer().no);
                pStatement.setInt(3, boardGame.getPhase().ordinal());
                pStatement.setInt(4, boardGame.getStep());
                pStatement.setString(5, boardGame.getGameName());

                int concernedRows = pStatement.executeUpdate();
                ResultSet keysGenerated = pStatement.getGeneratedKeys();

                if (concernedRows == 1 && keysGenerated.next()) {
                    boardGame.setGameId(keysGenerated.getInt(1));
                }
                keysGenerated.close();
                insertPlayers(boardGame);
                createNewCardFields(boardGame);
                pStatement = getSelectGameStatement();
                pStatement.setInt(1, boardGame.getGameId());

                ResultSet resultSet = pStatement.executeQuery();
                if (resultSet.next()) {
                    resultSet.updateInt("Current_player", boardGame. getPlayerNo(boardGame.getCurrentPlayer()));
                    resultSet.updateRow();
                }
                resultSet.close();
                DBConnector.commit();
                DBConnector.setAutoCommit(true);
                return true;
                
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error in database.");
            }
            LoadBoard.saveBoard(boardGame, boardGame.boardName);
        return false;
    }

    //This method is for updating an existing game in the database.
    public boolean updateCreatedGame(Board game) {
        assert game.getGameId() != null;

        Connection dbConnector = databaseConnector.getDatabaseConnection();
        try {
            dbConnector.setAutoCommit(false);

            PreparedStatement pStatement = getSelectGameStatement();
            pStatement.setInt(1, game.getGameId());

            ResultSet resultSet = pStatement.executeQuery();
            if (resultSet.next()) {
                resultSet.updateInt("Current_player", game.getPlayerNo(game.getCurrentPlayer()));
                resultSet.updateInt("Phase", game.getPhase().ordinal());
                resultSet.updateInt("Step", game.getStep());
                resultSet.updateRow();
            } else {
            }
            resultSet.close();

            updatePlayers(game);
            updateCreatedCardFields(game);

            dbConnector.commit();
            dbConnector.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in database.");

            try {
                dbConnector.rollback();
                dbConnector.setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    //This function is for updating the current state of the game in the database.
    private void updateCreatedCardFields(Board boardGame) throws SQLException {
        PreparedStatement pStatement;
        Connection dbConnector = databaseConnector.getDatabaseConnection();
        try {
            pStatement = dbConnector.prepareStatement(
                    "Select * from CardFieldCommands where Game_id = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            pStatement.setInt(1, boardGame.getGameId());
            ResultSet resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                int type = resultSet.getInt("Is_program");
                int coordination = resultSet.getInt("Position");
                int playerID = resultSet.getInt("Player_no");
                CommandCardField cmdCardField;
                CommandCard progCmdCard;

                for (int i = 0; i < boardGame.getPlayersNumber(); i++) {
                    Player p = boardGame.getPlayer(i);
                    if (type == 0 && playerID == i) {
                        cmdCardField = p.getCardField(coordination);
                        progCmdCard = cmdCardField.getCard();
                    } else if (type == 1 && playerID == i) {
                        cmdCardField = p.getProgramField(coordination);
                        progCmdCard = cmdCardField.getCard();
                    } else {
                        cmdCardField = null;
                        progCmdCard = null;
                    }
                    if (cmdCardField != null) {
                        resultSet.updateInt("Visible", 1);
                        if (progCmdCard != null) {
                            resultSet.updateInt("Command", progCmdCard.command.ordinal());
                        } else {
                            resultSet.updateNull("Command");
                            resultSet.updateInt("Visible", 0);
                        }
                    }
                }
                resultSet.updateRow();
            }
            resultSet.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //This method is used for loading an existing game in the database.
    public Board loadCreatedGame(int id) {
        Board boardGame;
        try {
            PreparedStatement pStatement = getSelectGameStatement();
            pStatement.setInt(1, id);
            ResultSet resultSet = pStatement.executeQuery();

            int playerID = -1;
            if (resultSet.next()) {
                boardGame = LoadBoard.loadBoard(resultSet.getString(2));
                if (boardGame == null) {
                    return null;
                }
                playerID = resultSet.getInt("Current_player");
                boardGame.setPhase(Phase.values()[resultSet.getInt("Phase")]);
                boardGame.setStep(resultSet.getInt("Step"));

            } else {
                return null;
            }
            resultSet.close();

            boardGame.setGameId(id);
            getPlayers(boardGame);

            if (playerID >= 0 && playerID < boardGame.getPlayersNumber()) {
                boardGame.setCurrentPlayer(boardGame.getPlayer(playerID));
            } else {
                return null;
            }
            getCardFields(boardGame);

            return boardGame;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in database.");
        }
        return null;
    }

    //Method to get all the games that are in the database.
    public List<GameInDatabase> getGamesFromDatabase() {
        List<GameInDatabase> listInDB = new ArrayList<>();
        try {
            PreparedStatement pStatement = getSelectGameIdsStatement();
            ResultSet resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                int gameID = resultSet.getInt("Game_id");
                String boardName = resultSet.getString("Board_name");
                String gameName = resultSet.getString(3);
                listInDB.add(new GameInDatabase(gameID, boardName, gameName));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listInDB;
    }

    private void insertPlayers(Board boardGame) throws SQLException {
        PreparedStatement pStatement;
        Connection dbConnector = databaseConnector.getDatabaseConnection();
        try {
            pStatement = dbConnector.prepareStatement(
                    "SELECT * FROM Player WHERE game_id = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);

            pStatement.setInt(1, boardGame.getGameId());
            ResultSet resultSet = pStatement.executeQuery();
            for (int i = 0; i < boardGame.getPlayersNumber(); i++) {
                Player player = boardGame.getPlayer(i);
                resultSet.moveToInsertRow();
                resultSet.updateInt("Game_id", boardGame.getGameId());
                resultSet.updateInt("Player_no", i);
                resultSet.updateString("Name", player.getName());
                resultSet.updateString("Color", player.getColor());
                resultSet.updateInt("X_position", player.getSpace().x);
                resultSet.updateInt("Y_position", player.getSpace().y);
                resultSet.updateInt("heading", player.getHeading().ordinal());
                resultSet.insertRow();
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Method for inserting card fields in the database
    private boolean createNewCardFields(Board game) throws SQLException {
        try {
            PreparedStatement pStatement = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM Card_field_command WHERE game_id = ?");
            pStatement.setInt(1, game.getGameId());
            pStatement = databaseConnector.getDatabaseConnection().prepareStatement("INSERT INTO Card_field_command (Game_id, Player_no, is_program, Active, Visible, Command, Position) VALUES (?,?,?,?,?,?,?)");

            for (int i = 0; i < game.getPlayersNumber(); i++) {
                Player p = game.getPlayer(i);
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    pStatement.setInt(1, game.getGameId());
                    pStatement.setInt(2, i);
                    pStatement.setInt(3, 1);
                    pStatement.setInt(4, p.getProgramField(j).isActive()? 1 : 0);
                    pStatement.setBoolean(5, p.getProgramField(j).isVisible());
                    if (p.getProgramField(j).getCard() != null) {
                        pStatement.setInt(6, p.getProgramField(j).getCard().command.ordinal());
                    } else {
                        pStatement.setNull(6,Types.INTEGER);
                    }
                    pStatement.setInt(7, j);
                    pStatement.execute();
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {

                    pStatement.setInt(1, game.getGameId());
                    pStatement.setInt(2, i);
                    pStatement.setInt(3, 0);
                    pStatement.setInt(4, p.getCardField(j).isActive()? 1 : 0);
                    pStatement.setBoolean(5, p.getCardField(j).isVisible());
                    if (p.getCardField(j).getCard() != null) {
                        pStatement.setInt(6, p.getCardField(j).getCard().command.ordinal());
                    } else {
                        pStatement.setNull(6,Types.INTEGER);
                    }
                    pStatement.setInt(7, j);
                    pStatement.execute();
                }
            } return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //This method adds player to the game from the database.
    private void getPlayers(Board boardGame) throws SQLException {
        PreparedStatement pStatement;

        Connection dbConnector = databaseConnector.getDatabaseConnection();
        try {
            pStatement = dbConnector.prepareStatement(
                    "SELECT * FROM Player WHERE Game_id = ? ORDER BY Player_no ASC");
            pStatement.setInt(1, boardGame.getGameId());

            ResultSet resultSet = pStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                int playerNo = resultSet.getInt("Player_no");
                if (i++ == playerNo) {
                    String name = resultSet.getString("Name");
                    String color = resultSet.getString("Color");
                    Player p = new Player(boardGame, color, name);
                    boardGame.addPlayer(p);

                    int x = resultSet.getInt("X_position");
                    int y = resultSet.getInt("Y_position");
                    p.setSpace(boardGame.getSpace(x, y));
                    int direction = resultSet.getInt("Heading");
                    p.setHeading(Heading.values()[direction]);

                } else {
                    System.err.println("Invalid player in that game, the database cannot find a player with ID: " + i + ".");
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Method to put the command cards into the card fields of the player from the database.
    private void getCardFields(Board boardGame) throws SQLException {
        PreparedStatement pStatement = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM Card_field_command WHERE game_id = ?");
        pStatement.setInt(1, boardGame.getGameId());
        ResultSet resultSet = pStatement.executeQuery();
        int playerNo;
        int isProgram;
        int active;
        int visible;
        int coordination;
        Object cmd;

        while (resultSet.next()) {
            for (int i = 0; i < boardGame.getPlayersNumber(); i++) {
                playerNo = resultSet.getInt("Player_no");
                isProgram = resultSet.getInt("Is_program");
                active = resultSet.getInt("Active");
                cmd = resultSet.getObject("Command");
                visible = resultSet.getInt("Visible");
                coordination = resultSet.getInt("Position");

                Player p = boardGame.getPlayer(playerNo);

                if (resultSet.getInt("Command") == -1){
                    cmd = null;
                }
                CommandCardField cmdCardField;
                if(isProgram == 0) {
                    cmdCardField = p.getCardField(coordination);
                } else cmdCardField = p.getProgramField(coordination);
                if (cmd != null) {
                    if (isProgram == 0) {
                        Command command = Command.values()[Integer.parseInt((String) cmd)];
                        CommandCard commandCard = new CommandCard(command);
                        cmdCardField.setCard(commandCard);
                    } else if (isProgram == 1) {
                        Command c = Command.values()[Integer.parseInt((String) cmd)];
                        CommandCard commandCard = new CommandCard(c);
                        cmdCardField.setCard(commandCard);
                    }
                }
                cmdCardField.setVisible(visible != 0);
                cmdCardField.setActive(active != 0);
            }
        }resultSet.close();
    }

    //This method is for updating the current state of the player in the game to the database.
    private void updatePlayers(Board boardGame) throws SQLException {
        PreparedStatement pStatement;
        Connection dbConnector = databaseConnector.getDatabaseConnection();
        try {
            pStatement = dbConnector.prepareStatement(
                    "SELECT * FROM Player WHERE Game_id = ? ORDER BY Player_no ASC");
            pStatement.setInt(1, boardGame.getGameId());

            ResultSet resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                int playerNo = resultSet.getInt("Player_no");
                Player p = boardGame.getPlayer(playerNo);
                resultSet.updateInt("X_position", p.getSpace().x);
                resultSet.updateInt("Y_position", p.getSpace().y);
                resultSet.updateInt("Header", p.getHeading().ordinal());
                resultSet.updateRow();
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement getSelectGameStatement() {
        if (STMT_Game_Select == null) {
            Connection dbConnector = databaseConnector.getDatabaseConnection();
            try {
                STMT_Game_Select = dbConnector.prepareStatement(
                        Database_GameSelect,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return STMT_Game_Select;
    }

    private PreparedStatement getSelectGameIdsStatement() {
        if (STMT_Games_Select == null) {
            Connection dbConnector = databaseConnector.getDatabaseConnection();
            try {
                STMT_Games_Select = dbConnector.prepareStatement(
                        Database_GamesSelect);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return STMT_Games_Select;
    }
}
