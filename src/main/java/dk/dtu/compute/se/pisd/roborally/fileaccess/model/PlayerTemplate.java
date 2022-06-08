package dk.dtu.compute.se.pisd.roborally.fileaccess.model;

import dk.dtu.compute.se.pisd.roborally.model.subject.CommandCard;
import dk.dtu.compute.se.pisd.roborally.model.subject.CommandCardField;

import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Sammy Chauhan
 *
 */
public class PlayerTemplate {

    public String name;

    public String color;

    public SpaceTemplate space;

    public List<CommandCardFieldTemplate> cards = new ArrayList<>();

    public List<CommandCardFieldTemplate> program = new ArrayList<>();


}
