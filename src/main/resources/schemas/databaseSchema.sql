SET FOREIGN_KEY_CHECKS = 0;;

CREATE TABLE  if not exists game
(Game_id INT NOT NULL UNIQUE AUTO_INCREMENT,
 Board_name VARCHAR(256),
 Current_player INT,
 Phase VARCHAR(256),
 Step INT,
 Game_name VARCHAR(256),
 PRIMARY KEY (Game_id));;


CREATE TABLE if not exists player
(Player_no INT,
 Game_id INT,
 Name VARCHAR(256),
    Color VARCHAR(256),
    X_position INT,
    Y_position INT,
    Heading INT,
    PRIMARY KEY (Player_no, Game_id),
    FOREIGN KEY (Game_id) REFERENCES game(Game_id));;

CREATE TABLE if not exists Card_field_commands
(Player_no INT,
 Game_id INT,
 Command VARCHAR(256),
    Position INT,
    Visible BIT(1),  -- 1 = true 0 = false.
    Active BIT(1),   -- 1 = true 0 = false.
    Is_program BIT(1),
    FOREIGN KEY (Player_no, Game_id) REFERENCES player(Player_no, Game_id)); ;
