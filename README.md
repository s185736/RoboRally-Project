# RoboRally-Project - G17

This is a Roborally project from the course [02324](https://kurser.dtu.dk/course/02324) at [DTU](https://www.dtu.dk/).

###The team consists of 3 people:
1. Azmi Uslu - s185736
2. Sammy Chauhan - s191181
3. Malaz Alzarrad - s180424

## How to run the Application as new game(Through IntelliJ)
1. First of all, open the IDE and replace the files with your empty Maven project.
2. Secondly, download [JavaFX](https://gluonhq.com/products/javafx/) and set it up. The software uses JavaFX UI framework. If you have problem with building the software and maven’s JavaFX installation, here you can find detailed information how to install it manually and setup the project properly, by following this tutorial: [HowToJavaFX](https://openjfx.io/openjfx-docs/#IDE-Intellij).
3. Download and run the database, we have done it in MySQLWorkbench. 
4. Write the password for your MySQL in the `DatabaseConnector` file in the string at the password variable. 
5. Above the src folder, you'll see a sql folder. Open it and place the SQL code into the Databse.
6. Start the program in the class `StartRoboRally`, this class will start the program. Remember to follow the tutorial properly, at the end you'll have to add VM Options.
7. Click 'file', and then 'New Game'.
8. Choose which board to play with
9. Choose amount of players.
10. You're ready to play.


## How to run the Application by loading a save file(Through IntelliJ)
1. Done all the step from running the application as new game from 1 to 5.
2. Click 'Load Game' and choose the save file.
3. You're ready to play.


## Current Features:
- Database - Server connected to a Database.
- Adapter - Converts to GSON.
- 'Left or Right' is now working properly.
- Antenna - Still in progress (boardtest: antennaboard_inprogress.json).
- Others will move when landing (pushing) on another's space.
- Walls - You won't be able to walk through walls.
- FieldAction:
    - Conveyor Belt (Blue Squire).
    - Gear right & left.
    - Pit.
    - Start Point.
- Up to 3 Game Boards are defined by the JSON files.
- Interactive cards.
- CheckPoints - Once you've been in 1st and then the 2rd checkpoint, you'll win.

##Currently Working On:
- Saving a game / updating.
- Loading an existing game file.
- Database / Connect our Game to Database (plans).

##Plans
Creating a database and storing a game in it, where you can load the game from the database.
1. Create A Game and save it in Database.
2. Update A Game and save it in Database