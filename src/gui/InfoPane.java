package gui;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.world.Cell;
import model.world.World;

/**
 * A VBox with labels that provide information relative to the
 * specified MapPane. The labels change as the mouse hovers over
 * the cell grid.
 * 
 * @author Eduardo Pedroni
 *
 */
public class InfoPane extends VBox {
    private Label cellCoordinates = new Label("Cell coordinates: ");
    private Label cellType = new Label("Type: ");
    private Label cellCrimeCount = new Label("Crime count: ");
    private Label cellLight = new Label("Light: ");
    private Label cellOccupant = new Label("Occupant: ");
    private Label cellOccupantState = new Label("Occupant state: ");
    private Label cellOccupantTarget = new Label("Occupant target: ");
    
    public InfoPane(MapPane map, World world) {
        super();
        
        map.setGridMouseMoveHandler(event -> {
            int x = (int) Math.floor(event.getX() / BaseCellNode.SIZE);
            int y = (int) Math.floor(event.getY() / BaseCellNode.SIZE);
            if (world.withinBounds(x, y)) {
                updateCellInformation(world.grid[x][y]);
            }
        });
        
        getChildren().addAll(
                cellCoordinates,
                cellType,
                cellCrimeCount,
                cellLight,
                cellOccupant,
                cellOccupantState,
                cellOccupantTarget);
    }
    
    private void updateCellInformation(Cell cell) {
        cellCoordinates.setText(String.format("Cell coordinates: [%s, %s]", cell.x, cell.y));
        cellType.setText(String.format("Type: %s", cell.type));
        cellCrimeCount.setText(String.format("Crime count: %s", cell.crimeCount));
        cellLight.setText(String.format("Light: %.4f", cell.light));
        cellOccupant.setText(String.format("Occupant: %s", cell.occupant));
        cellOccupantState.setText(String.format("Occupant state: %s", (cell.occupant != null ? cell.occupant.getState() : "n/a")));
        cellOccupantTarget.setText(String.format("Occupant target: %s", (cell.occupant != null ? cell.occupant.getTarget() : "n/a")));;
    }
}
