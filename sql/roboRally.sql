DROP DATABASE IF EXISTS roborally;
CREATE DATABASE roborally;
Use roborally;

DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS CardFieldsCommands;


CREATE TABLE game
(GameID INT NOT NULL UNIQUE AUTO_INCREMENT,
BoardName VARCHAR(30),
CurrentPlayer INT,
Phase VARCHAR(30),
Step INT,
GameName VARCHAR(256),
PRIMARY KEY (GameID));

CREATE TABLE player 
(PlayerNo INT,
GameID INT,
Name VARCHAR(30),  -- eller 255 måske.
Color VARCHAR(30),
XPosition INT,
YPosition INT,
Heading INT,
-- måske også en variabel hvor spilleren peger imod.
PRIMARY KEY (PlayerNo, GameID),
FOREIGN KEY (GameID) REFERENCES game(GameID));

CREATE TABLE CardFieldCommands
(
PlayerNo INT,
GameID INT,
Command VARCHAR(10),
Position INT,
Visible BIT(1),  -- 1 = true 0 = false.
Active BIT(1),   -- 1 = true 0 = false.
IsProgram BIT(1),
FOREIGN KEY (PlayerNo, GameID) REFERENCES player(PlayerNo, GameID)); 


Select * from cardfieldcommands;

-- CREATE TABLE playerCards
-- (
-- PlayerNo INT,
-- GameID INT,
-- Command VARCHAR(10),
-- Visible BIT(1),  -- 1 = true 0 = false.
-- Active BIT(1),   -- 1 = true 0 = false.
-- FOREIGN KEY (PlayerNo, GameID) REFERENCES player(PlayerNo, GameID)); 

-- PLAYER PK(PLAYERID,GAMEID) 1 FK(GAMEID)

-- PROGRAMS 1 FK (PLAYERID), STRING COMMAND, 2 BOOLEANS (ACTIVE, VISIBILE)

-- PLAYERCARDS 1 FK (PLAYERID), STRING COMMAND, 2 BOOLEANS (ACTIVE, VISIBILE)

-- GAME 
