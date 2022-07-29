package dk.dtu.compute.se.pisd.roborally.gameLoader;

import javafx.stage.FileChooser;

import java.io.File;

/**
 * ...
 *
 * @author Sammy Chauhan, s191181@dtu.dk
 * @author Azmi Uslu, s185736@dtu.dk
 */
public class GameLoader {

    /**
     * @return
     */
    public String fileloaderinsider() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json Files", "*.json"));
        File chosenOne = fileChooser.showOpenDialog(null);
        return chosenOne == null ? "" : chosenOne.getAbsolutePath();
    }
}