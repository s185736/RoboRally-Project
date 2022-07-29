DROP DATABASE IF EXISTS roborally;
CREATE DATABASE roborally;
Use roborally;

DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS CardFieldsCommands;


CREATE TABLE game
(GameID INT NOT NULL UNIQUE AUTO_INCREMENT,
 BoardName VARCHAR(256),
 CurrentPlayer INT,
 Phase VARCHAR(256),
 Step INT,
 GameName VARCHAR(256),
 PRIMARY KEY (GameID));

CREATE TABLE player
(PlayerNo INT,
 GameID INT,
 Name VARCHAR(256),
 Color VARCHAR(256),
 XPosition INT,
 YPosition INT,
 Heading INT,
 PRIMARY KEY (PlayerNo, GameID),
 FOREIGN KEY (GameID) REFERENCES game(GameID));

CREATE TABLE CardFieldCommands
(
    PlayerNo INT,
    GameID INT,
    Command VARCHAR(256),
    Position INT,
    Visible BIT(1),  -- 1 = true 0 = false.
    Active BIT(1),   -- 1 = true 0 = false.
    IsProgram BIT(1),
    FOREIGN KEY (PlayerNo, GameID) REFERENCES player(PlayerNo, GameID));