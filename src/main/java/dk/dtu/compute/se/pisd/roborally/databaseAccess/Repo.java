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
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class Repo{

    private final DatabaseConnector databaseConnector;

    Repo(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public boolean insertCreatedGame(Board game) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                connDB.setAutoCommit(false);
                PreparedStatement statement = connDB.prepareStatement(
                        "INSERT INTO Game(boardName, currentPlayer, phase, step, gameName) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, game.boardName);
                statement.setNull(2, game.getCurrentPlayer().no);
                statement.setInt(3, game.getPhase().ordinal());
                statement.setInt(4, game.getStep());
                statement.setString(5, game.getGameName());

                int concernRows = statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (concernRows == 1 && generatedKeys.next()) {
                    game.setGameId(generatedKeys.getInt(1));
                }
                generatedKeys.close();
                insertPlayers(game);
                createNewCardFields(game);
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
                System.err.println("Error in DB.");
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

    public boolean updateCreatedGame(Board game) {
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
            updateCreatedCardFields(game);

            connDB.commit();
            connDB.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Some DB error");

            try {
                connDB.rollback();
                connDB.setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

        return false;
    }

    private void updateCreatedCardFields(Board game) throws SQLException {
        PreparedStatement statement;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            statement = connDB.prepareStatement(
                    "Select * from CardFieldCommands where GameID = ?",
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE);
            statement.setInt(1, game.getGameId());
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int type = rs.getInt("IsProgram");
                int coord = rs.getInt("Position");
                int playerID = rs.getInt("PlayerNo");
                CommandCardField cmdCardField;
                CommandCard progCmdCard;

                for (int i = 0; i < game.getPlayersNumber(); i++) {
                    Player player = game.getPlayer(i);
                    if (type == 0 && playerID == i) {
                        cmdCardField = player.getCardField(coord);
                        progCmdCard = cmdCardField.getCard();
                    } else if (type == 1 && playerID == i) {
                        cmdCardField = player.getProgramField(coord);
                        progCmdCard = cmdCardField.getCard();
                    } else {
                        cmdCardField = null;
                        progCmdCard = null;
                    }
                    if (cmdCardField != null) {
                        rs.updateInt("Visible", 1);
                        if (progCmdCard != null) {
                            rs.updateInt("Command", progCmdCard.command.ordinal());
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

    public Board loadCreatedGame(int id) {
        Board game;
        try {
            PreparedStatement statement = getSelectGameStatementU();
            if (id == -1) {
                id = showLatestGame();
            }
            statement.setInt(1, id);

            ResultSet rs = statement.executeQuery();
            int playerID = -1;
            if (rs.next()) {
                game = LoadBoard.loadBoard(rs.getString(2));
                if (game == null) {
                    return null;
                }
                playerID = rs.getInt("CurrentPlayer");
                game.setPhase(Phase.values()[rs.getInt("Phase")]);
                game.setStep(rs.getInt("Step"));

            } else {
                return null;
            }
            rs.close();

            game.setGameId(id);
            getPlayers(game);

            if (playerID >= 0 && playerID < game.getPlayersNumber()) {
                game.setCurrentPlayer(game.getPlayer(playerID));
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


    public int showLatestGame() {
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

    public List<GameIndatabase> getGames() {
        List<GameIndatabase> listInDB = new ArrayList<>();
        try {
            PreparedStatement statement = getSelectGameIdsStatement();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("GameID");
                String name = rs.getString("BoardName");
                String gameName = rs.getString(3);
                listInDB.add(new GameIndatabase(id, name, gameName));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listInDB;
    }

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

    private boolean createNewCardFields(Board game) throws SQLException {
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
        } catch(
                SQLException ex)

        {
            ex.printStackTrace();
            return false;
        }
    }

    private void getPlayers(Board game) throws SQLException {
        PreparedStatement statement;

        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
            statement = connDB.prepareStatement(
                    "SELECT * FROM Player WHERE GameID = ? ORDER BY PlayerNo ASC");
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

    private void updatePlayersInDB(Board game) throws SQLException {
        PreparedStatement statement;
        Connection connDB = databaseConnector.getDatabaseConnection();
        try {
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

    private static final String SQL_SELECT_GAME =
            "SELECT * FROM Game WHERE gameID = ?";

    private PreparedStatement select_game_stmt = null;

    private PreparedStatement getSelectGameStatementU() {
        if (select_game_stmt == null) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                select_game_stmt = connDB.prepareStatement(
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

    private PreparedStatement getSelectCardsStatementU() {
        if (select_cards_stmt == null) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                select_cards_stmt = connDB.prepareStatement(
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
            "SELECT gameID, boardName, gameName FROM game ORDER BY gameID DESC LIMIT 10";

    private PreparedStatement select_games_stmt = null;


    private PreparedStatement getSelectGameIdsStatement() {
        if (select_games_stmt == null) {
            Connection connDB = databaseConnector.getDatabaseConnection();
            try {
                select_games_stmt = connDB.prepareStatement(
                        SQL_SELECT_GAMES);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return select_games_stmt;
    }
}
