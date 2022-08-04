SET FOREIGN_KEY_CHECKS = 0;;

CREATE TABLE if not exists game
(
 GameID INT NOT NULL UNIQUE AUTO_INCREMENT,
 BoardName varchar(256),
 CurrentPlayer INT,
 Phase varchar(256),
 Step INT,
 GameName varchar(256),
 PRIMARY KEY (GameID)
 );;

CREATE TABLE if not exists player
(PlayerNo INT,
 GameID INT,
 Name varchar(256),
 Color varchar(256),
 XPosition INT,
 YPosition INT,
 Heading INT,
 PRIMARY KEY (PlayerNo, GameID),
 FOREIGN KEY (GameID) REFERENCES game(GameID));;

CREATE TABLE blog (
                      id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                      title VARCHAR(500) NOT NULL,
                      content VARCHAR(5000) NOT NULL
);;

CREATE TABLE if not exists CardFieldCommands
(
    PlayerNo INT,
    GameID INT,
    Command varchar(10),
    Visible BIT(1),  -- 1 = true 0 = false.
    Active BIT(1),   -- 1 = true 0 = false.
    IsProgam BIT(1),
    FOREIGN KEY (PlayerNo, GameID) REFERENCES player(PlayerNo, GameID));;
