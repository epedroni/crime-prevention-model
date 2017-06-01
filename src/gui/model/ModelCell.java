package gui.model;

import gui.BaseCellNode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.world.Cell;

/**
 * A cell in the model GUI. The appearance of the cell
 * changes depending on whether it is occupied or not,
 * state of the occupant (if any) and how many crimes
 * have been committed there.
 * 
 * @author Eduardo Pedroni
 *
 */
public class ModelCell extends BaseCellNode {

    private Cell cell;
    public final int xPosition, yPosition;
    private Rectangle crimeLayer = new Rectangle(SIZE, SIZE, Color.web("#ff0000"));

    public ModelCell(Cell cell) {
        super();
        this.cell = cell;
        crimeLayer.setOpacity(0);
        xPosition = cell.x;
        yPosition = cell.y;
        getChildren().add(crimeLayer);
    }
    
    /**
     * Called by the MapPane when the GUI needs to be updated.
     */
    public void update(int globalCrimeMax) {
        setStyle(baseStyle + " -fx-background-color: " + (cell.occupant == null ? cell.type.colour : cell.occupant.getState().colour));
        setLight(cell.light);
        if (globalCrimeMax > 0) {
            crimeLayer.setOpacity((double) cell.crimeCount / (double) (globalCrimeMax));
        }
    }
}
