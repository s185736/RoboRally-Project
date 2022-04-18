# RoboRally-Project

This is a Roborally project from the course [02324](https://kurser.dtu.dk/course/02324) at [DTU Compute](https://compute.dtu.dk).

## How to run the Application (Through IntelliJ)
1. First of all, open the IDE and clone the project or replace the files with your empty Maven project.
2. Secondly, Download [JavaFX](https://gluonhq.com/products/javafx/).
3. Open 'File' > 'Project Structure..' > 'Global Libraries' and add JavaFX.
4. Add VM options, click on Run -> Edit Configurations and add these VM options:

Linux/Mac
--module-path /path/to/javafx-sdk-17.0.1/lib --add-modules javafx.controls,javafx.fxml

Windows
--module-path "\path\to\javafx-sdk-17.0.1\lib" --add-modules javafx.controls,javafx.fxml

6. Start the program in the class `StartRoboRally`, this class will start the program.
7. Click 'file', and then 'New Game'.
