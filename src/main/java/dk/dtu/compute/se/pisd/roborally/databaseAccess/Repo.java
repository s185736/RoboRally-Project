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
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */

public class Repo {

    private final DatabaseConnector databaseConnector;

    Repo(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    /**
     * Creating the game board in the database.
     *
     * @param game
     * @return
     */
    public boolean insertCreationGame(Board game) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                connDB.setAutoCommit(false);
                PreparedStatement statement = connDB.prepareStatement(
                        "INSERT INTO Game(boardName, currentPlayer, phase, step, gameName) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, game.boardName);
                statement.setNull(2, game.getCurrentPlayer().no); // game.getPlayerNumber(game.getCurrentPlayer())); is inserted after players!
                statement.setInt(3, game.getPhase().ordinal());
                statement.setInt(4, game.getStep());
                statement.setString(5, game.getGameName());

                int affectedRows = statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (affectedRows == 1 && generatedKeys.next()) {
                    game.setGameId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                insertPlayers(game);
                createCardFields(game);
                statement = getSelectGameStatementU();
                statement.setInt(1, game.getGameId());

                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    rs.updateInt("CurrentPlayer", game. getPlayerNo(game.getCurrentPlayer()));
                    rs.updateRow();
                }
                rs.close();

                connDB.commit();
                connDB.setAutoCommit(true);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error occured.");
                try {
                    connDB.rollback();
                    connDB.setAutoCommit(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            LoadBoard.saveBoard(game, game.boardName);

        return false;
    }

    /**
     * Updating the game board in the database.
     *
     * @param game
     * @return
     */
    public boolean updateGame(Board game) {
        assert game.getGameId() != null;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            connDB.setAutoCommit(false);
            PreparedStatement statement = getSelectGameStatementU();
            statement.setInt(1, game.getGameId());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                rs.updateInt("CurrentPlayer", game.getPlayerNo(game.getCurrentPlayer()));
                rs.updateInt("Phase", game.getPhase().ordinal());
                rs.updateInt("Step", game.getStep());
                rs.updateRow();
            } else {
            }
            rs.close();

            updatePlayersInDB(game);
            updateCardFields(game);

            connDB.commit();
            connDB.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error occured.");

            try {
                connDB.rollback();
                connDB.setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Updating the current state of the board game in the database.
     *
     * @param game
     * @throws SQLException
     */
    private void updateCardFields(Board game) throws SQLException {
        PreparedStatement statement;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            statement = connDB.prepareStatement("Select * from CardFieldCommands where GameID = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            statement.setInt(1, game.getGameId());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                int type = rs.getInt("IsProgram");
                int pos = rs.getInt("Position");
                int playerID = rs.getInt("PlayerNo");
                CommandCardField cmdCardFld;
                CommandCard cmdCard;

                for (int i = 0; i < game.getPlayersNumber(); i++) {
                    Player player = game.getPlayer(i);

                    if (type == 0 && playerID == i) {
                        cmdCardFld = player.getCardField(pos);
                        cmdCard = cmdCardFld.getCard();

                    } else if (type == 1 && playerID == i) {
                        cmdCardFld = player.getProgramField(pos);
                        cmdCard = cmdCardFld.getCard();
                    } else {
                        cmdCardFld = null;
                        cmdCard = null;
                    }
                    if (cmdCardFld != null) {
                        rs.updateInt("Visible", 1);
                        if (cmdCard != null) {
                            rs.updateInt("Command", cmdCard.command.ordinal());
                        } else {
                            rs.updateNull("Command");
                            rs.updateInt("Visible", 0);
                        }
                    }
                }
                rs.updateRow();
            }
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loading an existing board game through the database.
     *
     * @param id
     * @return
     */
    public Board loadGame(int id) {
        Board boadGame;
        try {
            PreparedStatement statement = getSelectGameStatementU();
            if (id == -1) {
                id = getLatestSavedGameId();
            }
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            int playerNum = -1;
            if (rs.next()) {
                boadGame = LoadBoard.loadBoard(rs.getString(2));
                if (boadGame == null) {
                    return null;
                }
                playerNum = rs.getInt("CurrentPlayer");
                boadGame.setPhase(Phase.values()[rs.getInt("Phase")]);
                boadGame.setStep(rs.getInt("Step"));

            } else {
                return null;
            }
            rs.close();

            boadGame.setGameId(id);
            getPlayers(boadGame);

            if (playerNum >= 0 && playerNum < boadGame.getPlayersNumber()) {
                boadGame.setCurrentPlayer(boadGame.getPlayer(playerNum));
            } else {
                return null;
            }
            getCardFields(boadGame);

            return boadGame;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error occured.");
        }
        return null;
    }
    /**
     * Returning the one that is latest saved from the table.
     *
     * @return
     */
    public int getLatestSavedGameId() {
        try {
            int boardGameID;
            PreparedStatement statement = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM game ORDER BY gameID DESC LIMIT 1");
            ResultSet valuesOfReturns = statement.executeQuery();
            if (!valuesOfReturns.next()) {
                return 0;
            }
            boardGameID = valuesOfReturns.getInt(1);
            return boardGameID;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * Getting all the saved games from the database.
     *
     * @return
     */
    public List<GameIndatabase> getGames() {
        List<GameIndatabase> gameDB = new ArrayList<>();
        try {
            PreparedStatement statement = getSelectGameIdsStatement();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("GameID");
                String name = rs.getString("BoardName");
                String gameName = rs.getString(3);
                gameDB.add(new GameIndatabase(id, name, gameName));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameDB;
    }

    /**
     * Creating the game board in the database.
     *
     * @param game
     * @return
     */
    private void insertPlayers(Board game) throws SQLException {
        PreparedStatement statement;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            statement = connDB.prepareStatement(
                    "SELECT * FROM Player WHERE gameID = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            statement.setInt(1, game.getGameId());
            ResultSet rs = statement.executeQuery();
            for (int i = 0; i < game.getPlayersNumber(); i++) {
                Player player = game.getPlayer(i);
                rs.moveToInsertRow();
                rs.updateInt("GameID", game.getGameId());
                rs.updateInt("PlayerNo", i);
                rs.updateString("Name", player.getName());
                rs.updateString("Color", player.getColor());
                rs.updateInt("XPosition", player.getSpace().x);
                rs.updateInt("YPosition", player.getSpace().y);
                rs.updateInt("heading", player.getHeading().ordinal());
                rs.insertRow();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method will create the cardfields into the database.
     *
     * @param game
     * @return
     * @throws SQLException
     */
    private boolean createCardFields(Board game) throws SQLException {
        try {
            PreparedStatement statement = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM CardFieldCommands WHERE gameID = ?");
            statement.setInt(1, game.getGameId());
            statement = databaseConnector.getDatabaseConnection().prepareStatement("INSERT INTO CardFieldCommands (GameID, PlayerNo, isProgram, Active, Visible, Command, Position) VALUES (?,?,?,?,?,?,?)");
            for (int i = 0; i < game.getPlayersNumber(); i++) {
                Player player = game.getPlayer(i);
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    statement.setInt(1, game.getGameId());
                    statement.setInt(2, i);
                    statement.setInt(3, 1);
                    statement.setInt(4, player.getProgramField(j).isActive()? 1 : 0);
                    statement.setBoolean(5, player.getProgramField(j).isVisible());
                    if (player.getProgramField(j).getCard() != null) {
                        statement.setInt(6, player.getProgramField(j).getCard().command.ordinal());
                    } else {
                        statement.setNull(6,Types.INTEGER);
                    }
                    statement.setInt(7, j);
                    statement.execute();
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    statement.setInt(1, game.getGameId());
                    statement.setInt(2, i);
                    statement.setInt(3, 0);
                    statement.setInt(4, player.getCardField(j).isActive()? 1 : 0);
                    statement.setBoolean(5, player.getCardField(j).isVisible());
                    if (player.getCardField(j).getCard() != null) {
                        statement.setInt(6, player.getCardField(j).getCard().command.ordinal());
                    } else {
                        statement.setNull(6,Types.INTEGER);
                    }
                    statement.setInt(7, j);
                    statement.execute();
                }
            }return true;
        } catch(SQLException ex)
            {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Loading player through the database and sets them into the board.
     *
     * @param game
     * @throws SQLException
     */
    private void getPlayers(Board game) throws SQLException {
        PreparedStatement statement;

        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            // This statement does not need to be updatable
            statement = connDB.prepareStatement("SELECT * FROM Player WHERE GameID = ? ORDER BY PlayerNo ASC");
            statement.setInt(1, game.getGameId());

            ResultSet rs = statement.executeQuery();
            int i = 0;
            while (rs.next()) {
                int playerId = rs.getInt("PlayerNo");
                if (i++ == playerId) {
                    String name = rs.getString("Name");
                    String colour = rs.getString("Color");
                    Player player = new Player(game, colour, name);
                    game.addPlayer(player);

                    int x = rs.getInt("XPosition");
                    int y = rs.getInt("YPosition");
                    player.setSpace(game.getSpace(x, y));
                    int heading = rs.getInt("Heading");
                    player.setHeading(Heading.values()[heading]);
                } else {
                    System.err.println("Game in DB does not have a player with id " + i + "!");
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Loading the command programming card through the database and sets them to the fields of card for the player.
     *
     * @param game
     * @throws SQLException
     */
    private void getCardFields(Board game) throws SQLException {
        PreparedStatement statement = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM CardFieldCommands WHERE gameID = ?");
        statement.setInt(1, game.getGameId());
        ResultSet rs = statement.executeQuery();
        int playerID;
        int isProgram;
        int active;
        int visible;
        int position;
        Object command;

        while (rs.next()) {
            for (int i = 0; i < game.getPlayersNumber(); i++) {
                playerID = rs.getInt("PlayerNo");
                isProgram = rs.getInt("IsProgram");
                active = rs.getInt("Active");
                command = rs.getObject("Command");
                visible = rs.getInt("Visible");
                position = rs.getInt("Position");

                Player player = game.getPlayer(playerID);

                if (rs.getInt("Command") == -1){
                    command = null;
                }
                CommandCardField commandCardField;
                if(isProgram == 0) {
                    commandCardField = player.getCardField(position);
                } else commandCardField = player.getProgramField(position);


                if (command != null) {
                    if (isProgram == 0) {
                        Command c = Command.values()[Integer.parseInt((String) command)];
                        CommandCard commandCard = new CommandCard(c);
                        commandCardField.setCard(commandCard);
                    } else if (isProgram == 1) {
                        Command c = Command.values()[Integer.parseInt((String) command)];
                        CommandCard commandCard = new CommandCard(c);
                        commandCardField.setCard(commandCard);
                    }
                }
                commandCardField.setVisible(visible != 0);
                commandCardField.setActive(active != 0);
            }
        }rs.close();
    }

    /**
     * Will be updating the current state of players who are playing in the game, and that happens in the database.
     *
     * @param game
     * @throws SQLException
     */
    private void updatePlayersInDB(Board game) throws SQLException {
        PreparedStatement statement;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            // This statement does not need to be updatable
            statement = connDB.prepareStatement(
                    "SELECT * FROM Player WHERE GameID = ? ORDER BY PlayerNo ASC");
            statement.setInt(1, game.getGameId());

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int playerId = rs.getInt("PlayerNo");
                Player player = game.getPlayer(playerId);
                // rs.updateString(PLAYER_NAME, player.getName()); // not needed: player's names does not change
                rs.updateInt("XPosition", player.getSpace().x);
                rs.updateInt("YPosition", player.getSpace().y);
                rs.updateInt("Header", player.getHeading().ordinal());
                rs.updateRow();
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



    /**
     * > If the insert_game_stmt is null, then create a new PreparedStatement object and assign it to the insert_game_stmt
     * variable
     *
     * @return The generated key.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     */

    private static final String SQL_SELECT_GAME =
            "SELECT * FROM Game WHERE gameID = ?";

    private PreparedStatement selectBoardStatement = null;

    /**
     * Returning a preparedStatement which can be used to select a board game through the database.
     *
     * @return
     */
    private PreparedStatement getSelectGameStatementU() {
        if (selectBoardStatement == null) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                selectBoardStatement = connDB.prepareStatement(
                        SQL_SELECT_GAME,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return selectBoardStatement;
    }

    private static final String SQL_SELECT_GAMES =
            "SELECT gameID, boardName, gameName FROM game ORDER BY gameID DESC LIMIT 10";

    private PreparedStatement selectBoardsStatement = null;


    /**
     * Is the statement null? If so, getting a connection, creating the statement, and returning it.
     *
     * @return
     */
    private PreparedStatement getSelectGameIdsStatement() {
        if (selectBoardsStatement == null) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                selectBoardsStatement = connDB.prepareStatement(SQL_SELECT_GAMES);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return selectBoardsStatement;
    }
}
