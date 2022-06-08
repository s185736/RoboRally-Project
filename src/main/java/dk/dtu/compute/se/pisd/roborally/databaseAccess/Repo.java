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

public class Repo{

    private final DatabaseConnector databaseConnector;

    Repo(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    /**
     * Creates the game in connected database.
     *
     * @author @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     * */
    public boolean insertGame(Board game) {
            Connection connection = databaseConnector.getDatabaseConnection();
            try {
                connection.setAutoCommit(false);

                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO Game(name, currentPlayer, phase, step, gameName) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, game.boardName);
                ps.setNull(2, game.getCurrentPlayer().no); // game.getPlayerNumber(game.getCurrentPlayer())); is inserted after players!
                ps.setInt(3, game.getPhase().ordinal());
                ps.setInt(4, game.getStep());
                ps.setString(5, game.getGameName());

                int affectedRows = ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();

                if (affectedRows == 1 && generatedKeys.next()) {
                    game.setGameId(generatedKeys.getInt(1));
                }
                generatedKeys.close();

                insertPlayers(game);

                createCardFields(game);

                ps = getSelectGameStatementU();
                ps.setInt(1, game.getGameId());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    rs.updateInt(GAME_CURRENTPLAYER, game. getPlayerNo(game.getCurrentPlayer()));
                    rs.updateRow();
                }
                rs.close();

                connection.commit();
                connection.setAutoCommit(true);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Some DB error");

                try {
                    connection.rollback();
                    connection.setAutoCommit(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            LoadBoard.saveBoard(game, game.boardName);
        } else {
            System.err.println("Game cannot be created in DB, since it has a game id already!");
            updateGame(game);
            try {
                updatePlayersInDB(game);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Method responsible for updating the game in database.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     * */
    public boolean updateGame(Board game) {
        assert game.getGameId() != null;

        Connection connection = databaseConnector.getDatabaseConnection();
        try {
            connection.setAutoCommit(false);

            PreparedStatement ps = getSelectGameStatementU();
            ps.setInt(1, game.getGameId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNo(game.getCurrentPlayer()));
                rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
                rs.updateInt(GAME_STEP, game.getStep());
                rs.updateRow();
            } else {
            }
            rs.close();

            updatePlayersInDB(game);
            updateCardFields(game);

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Some DB error");

            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        return false;
    }

    /**
     * It updates the database with the current state of the game
     *
     * @param game the game object
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     */
/*
    PreparedStatement ps = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM cardfield WHERE gameID = ?");
            ps.setInt(1, game.getGameId());
                    ps = databaseConnector.getDatabaseConnection().prepareStatement("INSERT INTO cardfield (GameID, PlayerNo, isProgram, Active, Visible, Command) VALUES (?,?,?,?,?,?)");

                    for (int i = 0; i < game.getPlayersNumber(); i++) {
        Player player = game.getPlayer(i);
        for (int j = 0; j < Player.NO_REGISTERS; j++) {
        ps.setInt(1, game.getGameId());
        ps.setInt(2, i);
        ps.setInt(3, 1);
        ps.setInt(4, player.getProgramField(j).isActive()? 1 : 0);
        ps.setBoolean(5, player.getProgramField(j).isVisible());
        if (player.getProgramField(j).getCard() != null) {
        ps.setInt(6, player.getProgramField(j).getCard().command.ordinal());
        } else {
        ps.setNull(6,Types.INTEGER);
        }
        ps.execute();

        Connection connection = databaseConnector.getDatabaseConnection();
            try {
                select_game_stmt = connection.prepareStatement(
                        SQL_SELECT_GAME,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);

 */
    private void updateCardFields(Board game) throws SQLException {
        PreparedStatement ps;
        Connection connection = databaseConnector.getDatabaseConnection();
        try {
            ps = connection.prepareStatement(
                    "Select all from CommandCardField where GameID = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            ps.setInt(1, game.getGameId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int type = rs.getInt("IsProgram");
                int position = rs.getInt("Position");
                int playerID = rs.getInt("PlayerNo");
                CommandCardField commandCardField;
                CommandCard commandCard;

                for (int i = 0; i < game.getPlayersNumber(); i++) {
                    Player player = game.getPlayer(i);

                    if (type == 0 && playerID == i) {
                        commandCardField = player.getCardField(position);
                        commandCard = commandCardField.getCard();

                    } else if (type == 1 && playerID == i) {
                        commandCardField = player.getProgramField(position);
                        commandCard = commandCardField.getCard();
                    } else {
                        commandCardField = null;
                        commandCard = null;
                    }

                    if (commandCardField != null) {
                        rs.updateInt("Visible", 1);

                        if (commandCard != null) {
                            rs.updateInt("Command", commandCard.command.ordinal());
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

    //@Override
    /**
     * Method for loading game from the database
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     * */
    public Board loadGame(int id) {
        Board game;
        try {
            PreparedStatement ps = getSelectGameStatementU();
            if (id == -1) {
                id = getLatestSavedGameid();
            }
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            int playerNo = -1;
            if (rs.next()) {
                game = LoadBoard.loadBoard(rs.getString(2));
                if (game == null) {
                    return null;
                }
                playerNo = rs.getInt("CurrentPlayer");
                game.setPhase(Phase.values()[rs.getInt("Phase")]);
                game.setStep(rs.getInt("Step"));
            } else {
                return null;
            }
            rs.close();

            game.setGameId(id);
            getPlayers(game);

            if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
                game.setCurrentPlayer(game.getPlayer(playerNo));
            } else {
                return null;
            }
            getCardFields(game);

            return game;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Some DB error");
        }
        return null;
    }
    /**
     * > This function returns the latest gameID from the game table
     *
     * @return The latest gameID from the game table.
     *
     * @author Berfin
     */
    public int getLatestSavedGameid() {
        try {
            int gameId;
            PreparedStatement stmt = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM game ORDER BY gameID DESC LIMIT 1");
            ResultSet sqlReturnValues = stmt.executeQuery();
            if (!sqlReturnValues.next()) {
                return 0;
            }
            gameId = sqlReturnValues.getInt(1);
            return gameId;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * Method for getting all the games from the database.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     * */
    public List<GameIndatabase> getGames() {
        List<GameIndatabase> result = new ArrayList<>();
        try {
            PreparedStatement ps = getSelectGameIdsStatement();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(GAME_GAMEID);
                String name = rs.getString(GAME_NAME);
                String gameName = rs.getString(3);
                result.add(new GameIndatabase(id, name, gameName));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * It creates a new row in the database for each player in the game
     *
     * @param game The game to be saved
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     */
    private void insertPlayers(Board game) throws SQLException {
        PreparedStatement ps;
        Connection connection = databaseConnector.getDatabaseConnection();
        try {
            ps = connection.prepareStatement(
                    "SELECT * FROM Player WHERE gameID = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);

            ps.setInt(1, game.getGameId());

            ResultSet rs = ps.executeQuery();
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
     * It creates the card fields in the database
     *
     * @param game the game object
     * @return A boolean value.
     *
     * @author Berfin
     */
    private boolean createCardFields(Board game) throws SQLException {
        try {
            PreparedStatement ps = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM cardfield WHERE gameID = ?");
            ps.setInt(1, game.getGameId());
            ps = databaseConnector.getDatabaseConnection().prepareStatement("INSERT INTO cardfield (GameID, PlayerNo, isProgram, Active, Visible, Command) VALUES (?,?,?,?,?,?)");

            for (int i = 0; i < game.getPlayersNumber(); i++) {
                Player player = game.getPlayer(i);
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    ps.setInt(1, game.getGameId());
                    ps.setInt(2, i);
                    ps.setInt(3, 1);
                    ps.setInt(4, player.getProgramField(j).isActive()? 1 : 0);
                    ps.setBoolean(5, player.getProgramField(j).isVisible());
                    if (player.getProgramField(j).getCard() != null) {
                        ps.setInt(6, player.getProgramField(j).getCard().command.ordinal());
                    } else {
                        ps.setNull(6,Types.INTEGER);
                    }
                    ps.execute();
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {

                    ps.setInt(1, game.getGameId());
                    ps.setInt(2, i);
                    ps.setInt(3, 0);
                    ps.setInt(4, player.getProgramField(j).isActive()? 1 : 0);
                    ps.setBoolean(5, player.getCardField(j).isVisible());
                    if (player.getCardField(j).getCard() != null) {
                        ps.setInt(6, player.getCardField(j).getCard().command.ordinal());
                    } else {
                        ps.setNull(6,Types.INTEGER);
                    }
                    ps.execute();
                }
            }return true;
        } catch(
                SQLException ex)

        {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * It loads the players from the database and adds them to the game
     *
     * @param game The game object that we're loading the players into.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     */
    private void getPlayers(Board game) throws SQLException {
        PreparedStatement ps;

        Connection connection = databaseConnector.getDatabaseConnection();
        try {
            // This statement does not need to be updatable
            ps = connection.prepareStatement(
                    "SELECT * FROM Player WHERE GameID = ? ORDER BY PlayerNo ASC");
            ps.setInt(1, game.getGameId());

            ResultSet rs = ps.executeQuery();
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
     * It loads the command cards from the database and puts them into the card fields of the players
     *
     * @param game the game object
     *
     * @author Berfin
     */
    private void getCardFields(Board game) throws SQLException {
        PreparedStatement ps = databaseConnector.getDatabaseConnection().prepareStatement("SELECT * FROM cardfield WHERE gameID = ?");
        ps.setInt(1, game.getGameId());
        ResultSet rs = ps.executeQuery();
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

                if (command != null) {
                    if (isProgram == 1) {
                        CommandCardField commandCardField;
                        Command c = Command.values()[(int) command];
                        CommandCard commandCard = new CommandCard(c);
                        commandCardField = player.getCardField(position);
                        commandCardField.setCard(commandCard);
                        commandCardField.setVisible(visible != 0);
                        commandCardField.setActive(active != 0);
                    } else if (isProgram == 0) {
                        CommandCardField commandCardField;
                        Command c = Command.values()[(int) command];
                        CommandCard commandCard = new CommandCard(c);
                        commandCardField = player.getProgramField(position);
                        commandCardField.setCard(commandCard);
                        commandCardField.setVisible(visible != 1);
                    }
                }
            }
        }rs.close();
    }

    /**
     * "Update the database with the current state of the players in the game."
     *
     * The first thing we do is get a prepared statement for updating the players.  We then set the first parameter of the
     * prepared statement to the game id.  This will be used to select the players for the game
     *
     * @param game the game to save
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     */
    private void updatePlayersInDB(Board game) throws SQLException {
        PreparedStatement ps;
        Connection connection = databaseConnector.getDatabaseConnection();
        try {
            // This statement does not need to be updatable
            ps = connection.prepareStatement(
                    "SELECT * FROM Player WHERE GameID = ? ORDER BY PlayerNo ASC");
            ps.setInt(1, game.getGameId());

            ResultSet rs = ps.executeQuery();
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

    private PreparedStatement select_game_stmt = null;

    /**
     * > This function returns a prepared statement that can be used to select a game from the database
     *
     * @return A PreparedStatement object.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     */
    private PreparedStatement getSelectGameStatementU() {
        if (select_game_stmt == null) {
            Connection connection = databaseConnector.getDatabaseConnection();
            try {
                select_game_stmt = connection.prepareStatement(
                        SQL_SELECT_GAME,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_game_stmt;
    }


    private PreparedStatement select_players_stmt = null;

    /**
     * > This function returns a PreparedStatement that can be used to select all the cards in the database
     *
     * @return A PreparedStatement object.
     *
     * @author Berfin
     */
    private PreparedStatement getSelectCardsStatementU() {
        if (select_cards_stmt == null) {
            Connection connection = databaseConnector.getDatabaseConnection();
            try {
                select_cards_stmt = connection.prepareStatement(
                        SQL_SELECT_CARDS,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_UPDATABLE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_cards_stmt;
    }

    private PreparedStatement select_cards_stmt = null;

    private static final String SQL_SELECT_CARDS =
            "SELECT * FROM cardfield WHERE gameID = ?";


    private PreparedStatement select_players_asc_stmt = null;


    private static final String SQL_SELECT_GAMES =
            "SELECT gameID, name, gameName FROM game ORDER BY gameID DESC LIMIT 10";

    private PreparedStatement select_games_stmt = null;

    /**
     * If the statement is null, get a connection, create the statement, and return it
     *
     * @return A PreparedStatement object.
     *
     * @author Ekkart Kindler, ekki@dtu.dk
     * @author Berfin
     */
    private PreparedStatement getSelectGameIdsStatement() {
        if (select_games_stmt == null) {
            Connection connection = databaseConnector.getDatabaseConnection();
            try {
                select_games_stmt = connection.prepareStatement(
                        SQL_SELECT_GAMES);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_games_stmt;
    }
}
