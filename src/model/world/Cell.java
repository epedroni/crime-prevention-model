package model.world;

import model.agent.Agent;

public class Cell {
    // properties of the cell according to the map, these should be fixed
    public CellType type;
    public final int x, y;
    public double light = 0;

    // state of the cell as a function of the model
    public Agent occupant;
    public int crimeCount = 0;

    public Cell(CellType type, int xPosition, int yPosition) {
        this.type = type;
        this.x = xPosition;
        this.y = yPosition;
    }

}
