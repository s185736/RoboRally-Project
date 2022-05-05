package dk.dtu.compute.se.pisd.roborally.gameLoader;

import javafx.stage.FileChooser;

import java.io.File;

/**
 * ...
 *
 * @author
 *
 */

public class GameSaver {

    public String save() {

        FileChooser c = new FileChooser();
        c.getExtensionFilters().add(new FileChooser.ExtensionFilter("Json Files", "*.json"));
        File selectedFile = c.showSaveDialog(null);

        return selectedFile.getAbsolutePath();

    }

}
