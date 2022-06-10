/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.model.fieldAction.*;
import dk.dtu.compute.se.pisd.roborally.model.subject.Player;
import dk.dtu.compute.se.pisd.roborally.model.subject.Space;
import org.jetbrains.annotations.NotNull;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;

import java.net.URISyntaxException;
import java.util.Objects;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 60;
    final public static int SPACE_WIDTH = 60;

    public final Space space;


    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);

        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }

        if (space.board.getAntenna() != null && (space.board.getAntenna().x == space.x && space.board.getAntenna().y == space.y)) {
            this.setStyle("-fx-background-color: pink;");
        }

        // updatePlayer(); //doing it through updateView..

        // This space view should listen to changes of the space
        space.attach(this);
        updateView(this.space);
    }

    private void updatePlayer() {
        //this.getChildren().clear();

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0 );
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90*player.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }
    }

    private void updateConveyor(){
        ConveyorBelt conB = space.getConveyorBelt();
        if (conB != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    60.0, 0.0,
                    30.0, 60.0);
            arrow.setFill(Color.LIGHTGRAY);
            arrow.setRotate((90*conB.getHeading().ordinal())%360);
            this.getChildren().add(arrow);
        }
    }

    private void updateWalls(){
        Space space = this.space;
        if (space != null && !space.getWalls().isEmpty()) {
            for (Heading headingWall : space.getWalls()) {
                Polygon arrow = new Polygon(.0,0.0,
                        70.0,0.0,
                        70.0,5.0,
                        0.0,5.0);

                switch (headingWall) {
                    case EAST -> {
                        arrow.setTranslateX(32.5);
                        arrow.setRotate((90 * headingWall.ordinal()) % 360);
                    }
                    case SOUTH -> arrow.setTranslateY(32.5);
                    case WEST -> {
                        arrow.setTranslateX(-32.5);
                        arrow.setRotate((90 * headingWall.ordinal()) % 360);
                    }
                    case NORTH -> arrow.setTranslateY(-32.5);
                }
                arrow.setFill(Color.ORANGERED);
                this.getChildren().add(arrow);
            }
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            this.getChildren().clear();
            updateConveyor();
            this.setStyle(space.board.getAntenna() == null || (space.board.getAntenna().x != space.x || space.board.getAntenna().y != space.y) ? (space.x + space.y) % 2 == 0 ? "-fx-background-color: white;" : "-fx-background-color: black;" : "-fx-background-color: #ffffff;");
            if (space.getStartingPlayerNo() <= 0) {
            } else {
                Image icons = null;
                try {
                    icons = new Image(Objects.requireNonNull(SpaceView.class.getClassLoader().getResource("icons/img/sp.png")).toURI().toString());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                ImageView imgView = new ImageView(icons);
                imgView.setImage(icons);
                imgView.setRotate(-90);
                imgView.setFitHeight(SPACE_HEIGHT);
                imgView.setFitWidth(SPACE_WIDTH);
                imgView.setVisible(true);
                this.getChildren().add(imgView);
            }

            space.actions.forEach(action -> {
                if (!(action instanceof CheckPoint)) {
                } else {
                    implementImagesOnSpace("icons/img/cp.png", -90);
                    //implementImagesOnSpace("icons/img/cp" + ((CheckPoint) action).no + ".png", -90);
                }
                if (!(action instanceof Antenna)) {
                } else {
                    implementImagesOnSpace("icons/img/antenna.png");
                }
                if (!(action instanceof Pit)) {
                } else {
                    implementImagesOnSpace("icons/img/pit.png");
                }
                if (!(action instanceof Gear)) {
                    return;
                }
                implementImagesOnSpace("icons/img/gear.png");

            });
            updateWalls();
            updatePlayer();
        }
    }

    private ImageView implementImagesOnSpace(String string) {
        Image image;
        image = null;
        try {
            image = new Image(SpaceView.class.getClassLoader().getResource(string).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ImageView imageview;
        imageview = new ImageView(image);
        imageview.setImage(image);
        imageview.setFitHeight(SPACE_HEIGHT);
        imageview.setFitWidth(SPACE_WIDTH);
        imageview.setVisible(true);

        this.getChildren().add(imageview);
        return imageview;
    }

    private ImageView implementImagesOnSpace(String name, double coordRotations) {
        ImageView imageView = implementImagesOnSpace(name);
        imageView.setRotate(coordRotations);
        return imageView;
    }

}
