package dk.dtu.compute.se.pisd.roborally.view.board;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Antenna;
import dk.dtu.compute.se.pisd.roborally.model.Coordination;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.*;
import dk.dtu.compute.se.pisd.roborally.model.subject.Board;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;
import dk.dtu.compute.se.pisd.roborally.view.SpaceView;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dk.dtu.compute.se.pisd.roborally.gameLoader.GameSaveController.saveBoard;

/**
 * ...
 *
 * @author
 *
 */

public class DesignerView extends VBox {

    private Board board;
    private GridPane gp;
    private SpaceView[][] sw;
    private SpaceEventHandler spaceEH;

    String stringMessage = "is already on this space.";

    private List optFields = new ArrayList<String>();

    public DesignerView(Board board) {
        Button saveButton;
        VBox buttonsPane;
        this.board = board;
        addFieldActions();
        gp = new GridPane();
        this.getChildren().add(gp);
        sw = new SpaceView[board.width][board.height];
        spaceEH = new SpaceEventHandler(this.board);

        for (int x = 0; x < this.board.width; x++) {
            int y = 0;
            while (y < this.board.height) {
                Space space = this.board.getSpace(x, y);
                SpaceView sw1;
                sw1 = new SpaceView(space);
                sw[x][y] = sw1;
                gp.add(sw1, x, y);
                sw1.setOnMouseClicked(spaceEH);
                y++;
            }
        }
        saveButton = new Button("Save the Game Board.");
        saveButton.setOnAction(e -> {saveBoard(this.board);});
        buttonsPane = new VBox(saveButton);
        buttonsPane.setSpacing(3.0);
        buttonsPane.setAlignment(Pos.CENTER);
        this.getChildren().add(buttonsPane);
    }

    private void addFieldActions() {
        this.optFields.add("Antenna");
        this.optFields.add("Conveyor Belt");
        this.optFields.add("Player StartField");
        this.optFields.add("Walls");
        this.optFields.add("Checkpoint");
        this.optFields.add("Gear");
        this.optFields.add("Pit");
    }

    private class SpaceEventHandler implements EventHandler<MouseEvent> {
        public GameController gameController;
        private Board board;
        public SpaceEventHandler(@NotNull Board board) {
            this.board = board;
        }

        @Override
        public void handle(MouseEvent event) {
            Object source = event.getSource();
            if (source instanceof SpaceView sw) {
                Space space = sw.space;
                ChoiceDialog choiceD;
                choiceD = new ChoiceDialog();
                choiceD.setContentText("What do you want to add?");
                choiceD.getItems().addAll(optFields);
                choiceD.showAndWait();

                if (choiceD.getSelectedItem() != null) {
                    switch ((String) choiceD.getSelectedItem()) {
                        case "Checkpoint" -> addCheckPoint(space);
                        case "Gear" -> addGear(space);
                        case "Pit" -> addPit(space);
                        case "Antenna" -> {
                            Antenna antenna;
                            antenna = new Antenna(this.board, space.x, space.y);
                            this.board.setAntenna(antenna);
                        }
                        case "Conveyor Belt" -> addConveyor(space);
                        case "Player StartField" -> selectPlayerStartNumber(space);
                        case "Walls" -> addWalls(space);
                    }
                } else {
                    return;
                }
            }
            event.consume();
        }

        private void addConveyor(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof Conveyor)) {
                    continue;
                }
                showAlertMessage("A Conveyor Belt " + stringMessage);
                return;
            }
            ChoiceDialog choiceD = new ChoiceDialog();
            choiceD.setContentText("Which direction should it move the player?");
            choiceD.getItems().add(Heading.NORTH);
            choiceD.getItems().add(Heading.EAST);
            choiceD.getItems().add(Heading.SOUTH);
            choiceD.getItems().add(Heading.WEST);

            choiceD.showAndWait();
            if (choiceD.getSelectedItem() != null) {
                Conveyor conB = new Conveyor();
                conB.setHeading((Heading) choiceD.getSelectedItem());
                space.addAction(conB);
            } else {
                return;
            }

        }

        private void selectPlayerStartNumber(Space space) {
            TextInputDialog textD;
            textD = new TextInputDialog();
            textD.setContentText("Please select the player number, that should start here from 1-6.");
            textD.showAndWait();
            if (textD.getResult() != null) {
                space.setStartingPlayerNo(Integer.parseInt(textD.getResult()));
            } else {
                return;
            }

        }

        private void showAlertMessage(String text) {
            Alert alert = new Alert(Alert.AlertType.WARNING, text);
            alert.showAndWait();
        }

        private void addWalls(Space space) {
            List<Heading> currWalls;
            currWalls = space.getWalls();
            List<Heading> freeWalls = Collections.unmodifiableList(new ArrayList<>());
            List<Heading> headings = Collections.unmodifiableList(new ArrayList<>());

            headings.add(Heading.NORTH);
            headings.add(Heading.EAST);
            headings.add(Heading.SOUTH);
            headings.add(Heading.WEST);

            headings.stream().filter(heading -> !currWalls.contains(heading)).forEachOrdered(freeWalls::add);
            if (freeWalls.isEmpty()) {
                return;
            }
            ChoiceDialog textD = new ChoiceDialog();
            textD.setContentText("Choose the direction where a wall should be placed.");
            textD.getItems().addAll(freeWalls);
            textD.showAndWait();
            if (textD.getSelectedItem() != null) {
                space.addWall((Heading) textD.getSelectedItem());
            } else {
                return;
            }
        }

        private void addCheckPoint(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof CheckPoint)) {
                } else {
                    showAlertMessage("A Checkpoint " + stringMessage);
                    return;
                }
            }

            TextInputDialog textD = new TextInputDialog();
            textD.setContentText("Which number of checkpoint should this be?");
            textD.showAndWait();
            if (textD.getResult() == null) {
                return;
            }
            int no;
            no = Integer.parseInt(textD.getResult());
            if (no > board.getCheckPoints().size()) {
            } else {
                String stringMessage = "You have inserted a number, that already exists. You must atleast insert " + (board.getCheckPoints().size() + 1+".");
                Alert warning = new Alert(Alert.AlertType.WARNING, stringMessage);
                warning.showAndWait();
                addCheckPoint(space);
            }
            CheckPoint checkpoint = new CheckPoint(no);
            space.addAction(checkpoint);
        }

        private void addGear(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof Gear)) {
                } else {
                    showAlertMessage("A Gear " + stringMessage);
                    return;
                }
            }

            List<Coordination> directions = new ArrayList<>();

            directions.add(Coordination.LEFT);
            directions.add(Coordination.RIGHT);

            ChoiceDialog choiceD = new ChoiceDialog();
            choiceD.setContentText("Choose the direction where the gear should turn.");
            choiceD.getItems().addAll(directions);
            choiceD.showAndWait();

            if (choiceD.getSelectedItem() == null) {
                return;
            }
            space.addGear((Coordination) choiceD.getSelectedItem());

        }

        private void addPit(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof Pit)) {
                    continue;
                }
                showAlertMessage("A Pit " + stringMessage);
                return;
            }
            space.addAction(new Pit());
        }
    }
}