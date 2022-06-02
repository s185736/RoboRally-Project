# RoboRally-Project - G17

This is a Roborally project from the course [02324](https://kurser.dtu.dk/course/02324) at [DTU](https://www.dtu.dk/).

###The team consists of 3 people:
1. Azmi Uslu - s185736
2. Sammy Chauhan - s191181
3. Malaz Alzarrad - s180424

## How to run the Application (Through IntelliJ)
1. First of all, open the IDE and replace the files with your empty Maven project.
2. Secondly, download [JavaFX](https://gluonhq.com/products/javafx/) and set it up. The software uses JavaFX UI framework. If you have problem with building the software and maven’s JavaFX installation, here you can find detailed information how to install it manually and setup the project properly, by following this tutorial: [HowToJavaFX](https://openjfx.io/openjfx-docs/#IDE-Intellij).
3. Start the program in the class `StartRoboRally`, this class will start the program. Remember to follow the tutorial properly, at the end you'll have to add VM Options.
4. Click 'file', and then 'New Game'.
5. Choose amount of players.
6. You're ready to play.


## Current Features:
- FileLoader - Extension to load a json file.
- InterfaceAdapter - Converts to GSON.
- 'Left or Right' is now working properly.
- Antenna - Still in progress.
- Others will move when landing (pushing) on another's space.
- Wall - You won't be able to walk through walls.
- FieldAction:
    - CheckPoints.
    - Conveyor Belt.
    - Gear.
    - Pit.

##Currently Working On:
- Saving a game / updating.
- Loading an existing game file.
- Database / Connect our Game to Database (plans).

##Plans
Creating a database and storing a game in it, where you can load the game from the database.
1. Create A Game and save it in Database.
2. Update A Game and save it in Database