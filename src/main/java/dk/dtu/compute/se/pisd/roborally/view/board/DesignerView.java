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
 *  @author Sammy Chauhan, s191181@dtu.dk
 *  @author Azmi Uslu, s185736@dtu.dk
 *  @author Malaz Alzarrad, s180424@dtu.dk
 *
 */

public class DesignerView extends VBox {

    private Board board;
    private GridPane gridPane;
    private SpaceView[][] spaceView;
    private SpaceEventController controller;

    String MSG = "is already on this space.";

    private List listFields = new ArrayList<String>();

    public DesignerView(Board board) {
        Button saveButtons;
        VBox paneButtons;
        this.board = board;
        addFieldActions();
        gridPane = new GridPane();
        this.getChildren().add(gridPane);
        spaceView = new SpaceView[board.width][board.height];
        controller = new SpaceEventController(this.board);

        for (int x = 0; x < this.board.width; x++) {
            int y = 0;
            while (y < this.board.height) {
                Space space = this.board.getSpace(x, y);
                SpaceView sw1;
                sw1 = new SpaceView(space);
                spaceView[x][y] = sw1;
                gridPane.add(sw1, x, y);
                sw1.setOnMouseClicked(controller);
                y++;
            }
        }
        saveButtons = new Button("Save the Game Board!");
        saveButtons.setOnAction(e -> {saveBoard(this.board);});
        paneButtons = new VBox(saveButtons);
        paneButtons.setSpacing(3.0);
        paneButtons.setAlignment(Pos.CENTER);
        this.getChildren().add(paneButtons);
    }

    private void addFieldActions() {
        this.listFields.add("Antenna");
        this.listFields.add("Conveyor Belt");
        this.listFields.add("Player StartField");
        this.listFields.add("Walls");
        this.listFields.add("Checkpoint");
        this.listFields.add("Gear");
        this.listFields.add("Pit");
    }


    private class SpaceEventController implements EventHandler<MouseEvent> {
        public GameController gameController;
        private Board board;
        public SpaceEventController(@NotNull Board board) {
            this.board = board;
        }

        @Override
        public void handle(MouseEvent event) {
            Object source = event.getSource();
            if (source instanceof SpaceView sw) {
                Space space = sw.space;
                ChoiceDialog choiceD;
                choiceD = new ChoiceDialog();
                choiceD.setContentText("What are you going to implement on the board?");
                choiceD.getItems().addAll(listFields);
                choiceD.showAndWait();

                if (choiceD.getSelectedItem() != null) {
                    switch ((String) choiceD.getSelectedItem()) {
                        case "Checkpoint" -> implementCheckpoint(space);
                        case "Gear" -> implementGear(space);
                        case "Pit" -> implementPit(space);
                        case "Antenna" -> {
                            Antenna antenna;
                            antenna = new Antenna(this.board, space.x, space.y);
                            this.board.setAntenna(antenna);
                        }
                        case "Conveyor Belt" -> addConveyorBelt(space);
                        case "Player StartField" -> selectAmountOfPlayerStart(space);
                        case "Walls" -> implementWalls(space);
                    }
                } else {
                    return;
                }
            }
            event.consume();
        }

        private void addConveyorBelt(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof ConveyorBelt)) {
                    continue;
                }
                displayAlerts("A Conveyor Belt " + MSG);
                return;
            }
            ChoiceDialog choiceD = new ChoiceDialog();
            choiceD.setContentText("Choose a direction, so the element can rotate the players: ");
            choiceD.getItems().add(Heading.NORTH);
            choiceD.getItems().add(Heading.EAST);
            choiceD.getItems().add(Heading.SOUTH);
            choiceD.getItems().add(Heading.WEST);

            choiceD.showAndWait();
            if (choiceD.getSelectedItem() != null) {
                ConveyorBelt conB = new ConveyorBelt();
                conB.setHeading((Heading) choiceD.getSelectedItem());
                space.addAction(conB);
            } else {
                return;
            }

        }

        private void selectAmountOfPlayerStart(Space space) {
            TextInputDialog textD;
            textD = new TextInputDialog();
            textD.setContentText("How many players are going to play? Choose between 2-6.");
            textD.showAndWait();
            if (textD.getResult() != null) {
                space.setStartingPlayerNo(Integer.parseInt(textD.getResult()));
            } else {
                return;
            }

        }

        private void displayAlerts(String text) {
            Alert alert = new Alert(Alert.AlertType.WARNING, text);
            alert.showAndWait();
        }
        private void implementWalls(Space space) {
            List<Heading> listingCurrWalls;
            listingCurrWalls = space.getWalls();
            List<Heading> walls_free = Collections.unmodifiableList(new ArrayList<>());
            List<Heading> walls_heading = Collections.unmodifiableList(new ArrayList<>());

            walls_heading.add(Heading.NORTH);
            walls_heading.add(Heading.EAST);
            walls_heading.add(Heading.SOUTH);
            walls_heading.add(Heading.WEST);

            walls_heading.stream().filter(heading -> !listingCurrWalls.contains(heading)).forEachOrdered(walls_free::add);
            if (!walls_free.isEmpty()) {
                ChoiceDialog textD = new ChoiceDialog();
                textD.setContentText("Choose a direction of a wall: ");
                textD.getItems().addAll(walls_free);
                textD.showAndWait();
                if (textD.getSelectedItem() != null) {
                    space.addWall((Heading) textD.getSelectedItem());
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        private void implementCheckpoint(Space space) {
            List<FieldAction> fieldActions = space.getActions();
            for (int i = 0, actionsSize = fieldActions.size(); i < actionsSize; i++) {
                FieldAction action = fieldActions.get(i);
                if (!(action instanceof CheckPoint)) {
                    continue;
                }
                displayAlerts("A Checkpoint " + MSG);
                return;
            }
            TextInputDialog dialog = new TextInputDialog();
            dialog.setContentText("How many check points should this have? ");
            dialog.showAndWait();
            if (dialog.getResult() == null) {
                return;
            }
            int num;
            num = Integer.parseInt(dialog.getResult());
            if (num > board.getCheckPoints().size()) {
            } else {
                String stringMessage = "That one does already exist, try again..";
                Alert warning = new Alert(Alert.AlertType.WARNING, stringMessage);
                warning.showAndWait();
                implementCheckpoint(space);
            }
            CheckPoint checkpoint = new CheckPoint(num);
            space.addAction(checkpoint);
        }

        private void implementGear(Space space) {
            List<FieldAction> fieldActions = space.getActions();
            for (int i = 0, actionsSize = fieldActions.size(); i < actionsSize; i++) {
                FieldAction fieldAction = fieldActions.get(i);
                if (!(fieldAction instanceof Gear)) {
                    continue;
                }
                displayAlerts("A Gear " + MSG);
                return;
            }

            List<Coordination> directions = new ArrayList<>();
            directions.add(Coordination.LEFT);
            directions.add(Coordination.RIGHT);
            ChoiceDialog choiceD = new ChoiceDialog();
            choiceD.setContentText("Choose a direction of gear, where it should rotate: ");
            choiceD.getItems().addAll(directions);
            choiceD.showAndWait();
            if (choiceD.getSelectedItem() == null) {
                return;
            }
            space.addGear((Coordination) choiceD.getSelectedItem());
        }

        private void implementPit(Space space) {
            List<FieldAction> actions = space.getActions();
            for (int i = 0, actionsSize = actions.size(); i < actionsSize; i++) {
                FieldAction action = actions.get(i);
                if (!(action instanceof Pit)) {
                    continue;
                }
                displayAlerts("A Pit " + MSG);
                return;
            }
            space.addAction(new Pit());
        }
    }
}