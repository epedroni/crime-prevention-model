package gui.maptool;

import java.io.File;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.map.Lighting;
import model.map.MapImage;
import model.map.MapParser;
import model.world.CellType;
import model.world.World;

/**
 * A very simple tool for editing maps. Currently it supports:
 * - loading .jpeg and .map files;
 * - changing the type of cells by clicking on them;
 * - "saving" (it dumps the map in .map format to stdout).
 * - lighting is updated in real time.
 * 
 * The most sensible use case is to load an image, edit it by hand
 * save it as a .map. Further tweaks can be made by loading the .map
 * again.
 * 
 * @author Eduardo Pedroni
 *
 */
public class MapTool extends Application {

    private GridPane grid = new GridPane();
    private BorderPane root = new BorderPane();
    private HBox settings = new HBox();

    private MapToolCell[][] cells;

    private TextField type = new TextField("0");
    private TextField xDimension = new TextField("");
    private TextField yDimension = new TextField("");
    private int xDim, yDim;
    private World world;
    
    public static void main(String... args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        makeSettings(settings);
        root.setCenter(new ScrollPane(grid));
        root.setTop(settings);

        primaryStage.setScene(new Scene(root));
        resetHandler(null);
        primaryStage.show();
    }

    private void makeSettings(HBox settings) {
        Button reset = new Button("Reset");
        reset.setOnAction(this::resetHandler);
        Button print = new Button("Print");
        print.setOnAction(this::printHandler);
        Button load = new Button("Load");
        load.setOnAction(this::loadHandler);
        settings.getChildren().addAll(new Label("Cell Type: "), type, new Label("Dimensions (x,y): "), xDimension,
                yDimension, reset, print, load);
    }

    private MapToolCell makeCell(int x, int y) {
        MapToolCell cell = new MapToolCell(x, y);
        cell.setOnMouseClicked(this::mouseClickedHandler);
        return cell;
    }

    /**
     * Prompts the user for a file and loads it into
     * the editor.
     */
    private void loadHandler(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(null);
        if (f != null) {
            if (f.getName().endsWith(".jpeg")) {
                world = MapImage.loadWorldFromImage(f);
            } else if (f.getName().endsWith(".map")) {
                world = MapParser.parse(f);
            } else {
                System.err.println("Unknown map format.");
                return;
            }
            xDimension.setText(String.valueOf(world.xDimension));
            yDimension.setText(String.valueOf(world.yDimension));
            resetHandler(null);
            for (int x = 0; x < world.xDimension; x++) {
                for (int y = 0; y < world.yDimension; y++) {
                    cells[x][y].setValue(String.valueOf(world.grid[x][y].type.ordinal()));
                    cells[x][y].setLight(world.grid[x][y].light);
                }
            }
        }
    }

    /**
     * Creates a completely new grid.
     */
    private void resetHandler(ActionEvent event) {
        try {
            xDim = Integer.parseInt(xDimension.getText());
            yDim = Integer.parseInt(yDimension.getText());
            cells = new MapToolCell[xDim][yDim];
            grid.getChildren().clear();
            for (int x = 0; x < xDim; x++) {
                for (int y = 0; y < yDim; y++) {
                    cells[x][y] = makeCell(x, y);
                    grid.add(cells[x][y], x, y);
                }
            }
        } catch (NumberFormatException e) {
            // doesn't matter
        }
    }

    /**
     * Prints the current state of the map to stdout in
     * the correct .map format.
     */
    private void printHandler(ActionEvent event) {
        System.out.println(xDim + " # number of columns");
        System.out.println(yDim + " # number of rows");
        for (int y = 0; y < yDim; y++) {
            for (int x = 0; x < xDim; x++) {
                System.out.print(cells[x][y].type + ",");
            }
            System.out.println();
        }
    }
    
    /**
     * Assigns the currently selected cell type to the clicked cell,
     * if valid.
     */
    private void mouseClickedHandler(MouseEvent event) {
        int newType = Integer.parseInt(type.getText());
        if (newType < CellType.types.length) {
            MapToolCell source = (MapToolCell) event.getSource();
            source.setValue(type.getText());
            world.grid[source.x][source.y].type = CellType.types[newType];
            Lighting.computeLamp(world, source.x, source.y);
            updateLighting(source.x, source.y);
        }
    }
    
    /**
     * Updates the lighting values of the cells within range around
     * the specified coordinates.
     */
    private void updateLighting(int x, int y) {
        for (int i = -Lighting.RADIUS_INT; i <= Lighting.RADIUS_INT; i++) {
            for (int j = -Lighting.RADIUS_INT; j <= Lighting.RADIUS_INT; j++) {
                if (world.withinBounds(x + i, y + j)) {
                    cells[x + i][y + j].setLight(world.grid[x + i][y + j].light);
                }
            }
        }
    }

}
