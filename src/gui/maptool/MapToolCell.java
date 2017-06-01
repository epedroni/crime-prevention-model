package gui.maptool;

import gui.BaseCellNode;
import model.world.CellType;

/**
 * Cell used in the map tool, its type is accessible so that the map
 * can be edited.
 * 
 * @author Eduardo Pedroni
 *
 */
public class MapToolCell extends BaseCellNode {

    public final int x, y;
    public int type = 0;

    public MapToolCell(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public void setValue(String value) {
        try {
            type = Integer.parseInt(value);
        } catch (Exception e) {
            System.err.println("Bad type: " + value + ", defaulting to 0");
            type = 0;
        }
        setStyle(baseStyle + " -fx-background-color: " + CellType.types[type].colour);
    }
}
