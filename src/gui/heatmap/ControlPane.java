package gui.heatmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import gui.MapPane;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import model.world.World;
import utils.Utils;

/**
 * Provides an interface to load log files and scroll through
 * the dump history.
 * 
 * @author Eduardo Pedroni
 *
 */
public class ControlPane extends HBox {
    private Logger log = Utils.getConsoleLogger(getClass());
    private MapPane map;
    private World world;
    
    private Button loadButton = new Button("Load log file");
    private Slider slider = new Slider(0, 1, 0);
    
    private ArrayList<HeatMap> heatMaps = new ArrayList<>();
    
    public ControlPane(MapPane map, World world) {
        this.map = map;
        this.world = world;
        
        loadButton.setOnAction(this::loadHandler);
        slider.valueProperty().addListener(this::slideHandler);
        slider.setPrefWidth(400);

        getChildren().addAll(loadButton, slider);
        updateUI(0);
    }
    
    private void updateUI(int max) {
        for (int x = 0; x < world.xDimension; x++) {
            for (int y = 0; y < world.yDimension; y++) {
                map.getCells()[x][y].update(max);
            }
        }
    }
    
    private void slideHandler(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (!heatMaps.isEmpty()) {
            applyHeatMap(heatMaps.get((int) (newValue.doubleValue() * (heatMaps.size() - 1))));
        }
    }
    
    private void loadHandler(ActionEvent event) {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(null);
        parseLog(f);
    }
    
    /**
     * Populates the heatmap list with all heatmap dumps in the
     * specified log file.
     */
    private void parseLog(File f) {
        heatMaps.clear();
        try (Scanner input = new Scanner(f)) {
            while (input.hasNextLine()) {
                if (input.nextLine().matches(".*?Heat map after ([0-9]*) rounds.*?")) {
                    heatMaps.add(parseHeatMap(input));
                }
            }
        } catch (Exception e) {
            log.severe("Could not parse heat map: " + f);
            e.printStackTrace();
        }
    }
    
    /**
     * Parses a heatmap of the same size as the world using the
     * specified scanner. This only works properly if the scanner
     * position is at the beginning of the heatmap dump.
     */
    private HeatMap parseHeatMap(Scanner scanner) {
        Pattern previousDelimiter = scanner.delimiter();
        scanner.useDelimiter("[^0-9]+");
        
        int[][] map = new int[world.xDimension][world.yDimension];
        int max = 0;
        
        for (int y = 0; y < world.yDimension; y++) {
            for (int x = 0; x < world.xDimension; x++) {
                int crimeCount = scanner.nextInt();
                map[x][y] = crimeCount;
                max = crimeCount > max ? crimeCount : max;
            }
        }
        
        scanner.useDelimiter(previousDelimiter);
        return new HeatMap(map, max);
    }
    
    /**
     * Sets the crime count of every cell to match the
     * provided heatmap.
     */
    private void applyHeatMap(HeatMap map){
        if (map == null) {
            return;
        }
        for (int y = 0; y < world.yDimension; y++) {
            for (int x = 0; x < world.xDimension; x++) {
                world.grid[x][y].crimeCount = map.map[x][y];
            }
        }
        updateUI(heatMaps.get(heatMaps.size() - 1).max);
    }
    
    private class HeatMap {
        public final int[][] map;
        public final int max;
        
        public HeatMap(int[][] map, int max) {
            this.map = map;
            this.max = max;
        }
    }

}
