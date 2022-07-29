package dk.dtu.compute.se.pisd.roborally.gameLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.view.board.BoardLayout;

import java.io.FileWriter;
import java.io.IOException;

/**
 * ...
 *
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */

public class GameSaveController {

    public static void saveBoard(Board board) {
        try {
            GameSaver fs = new GameSaver();
            String filename = fs.save();
            GsonBuilder builder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new InterfaceAdapter<FieldAction>()).setPrettyPrinting();
            Gson gson = builder.create();
            FileWriter fw = new FileWriter(filename);
            JsonWriter writer = gson.newJsonWriter(fw);
            BoardLayout bt = (new BoardLayout()).getInfoFromGameBoard(board);
            gson.toJson(bt, bt.getClass(), writer);
            writer.close();

        } catch (IOException e) {
            //TODO
        }
    }
}