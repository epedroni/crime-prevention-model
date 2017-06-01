package gui;

import gui.model.ModelCell;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import model.world.Cell;
import model.world.World;

/**
 * Custom pane which contains the map of the simulation. Extends ScrollPane
 * to provide scrolling support out of the box.
 * 
 * @author Eduardo Pedroni
 *
 */
public class MapPane extends ScrollPane {

    private ModelCell[][] cells;
    private GridPane grid;

    public MapPane(World world) {
        super();
        grid = new GridPane();
        setContent(grid);
        cells = new ModelCell[world.xDimension][world.yDimension];
        for (int x = 0; x < world.xDimension; x++) {
            for (int y = 0; y < world.yDimension; y++) {
                cells[x][y] = makeCell(world.grid[x][y]);
                grid.add(cells[x][y], x, y);
            }
        }
    }

    private ModelCell makeCell(Cell c) {
        ModelCell cell = new ModelCell(c);
        return cell;
    }

    public ModelCell[][] getCells() {
        return cells;
    }
    
    public void setGridMouseMoveHandler(EventHandler<MouseEvent> handler) {
        this.grid.setOnMouseMoved(handler);
    }
    
}
