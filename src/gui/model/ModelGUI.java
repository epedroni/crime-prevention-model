package gui.model;

import gui.MapPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Model;

/**
 * Frontend for the model. The GUI consists of:
 * - MapPane: shows a visualisation of the map, the agents and crimes being committed;
 * - SidePane: provides controls to interact with the model, such as running or stepping
 * through the iterations.
 * 
 * @author Eduardo Pedroni
 *
 */
public class ModelGUI extends Application {

    public static void main(String... args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Model model = new Model(new model.Parameters(getClass().getResource("/parameters").getPath()));

        MapPane mapPane = new MapPane(model.world);
        SidePane sidePane = new SidePane(model, mapPane);

        BorderPane root = new BorderPane();
        root.setCenter(mapPane);
        root.setRight(sidePane);
        
        // shuts down the thread pool when the UI is closed
        primaryStage.setOnCloseRequest(e -> model.stop());
        
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
