package gui.heatmap;

import java.io.File;

import gui.InfoPane;
import gui.MapPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.map.MapParser;
import model.world.World;

/**
 * Loads heatmaps from experiment logs and shows them overlaid on the
 * world map.
 */
public class HeatmapGUI extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        World world = MapParser.parse(new File(getClass().getResource("/city_map_small_lit.map").getPath()));
        MapPane map = new MapPane(world);
        ControlPane control = new ControlPane(map, world);
        InfoPane info = new InfoPane(map, world);
        info.setMinWidth(300);
        
        BorderPane root = new BorderPane();
        root.setCenter(map);
        root.setTop(control);
        root.setRight(info);
        
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
    
    public static void main(String... args) {
        Application.launch(args);
    }
}
