package gui.model;

import gui.InfoPane;
import gui.MapPane;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Model;

/**
 * Provides controls for the model. The execution of the model is also
 * handled here, which is a bit tricky and involves some locking.
 * 
 * @author Eduardo Pedroni
 *
 */
public class SidePane extends VBox {
    private Model model;
    private MapPane map;

    // global model information
    private Label timeLabel = new Label();
    
    // model execution controls
    private TextField iterations = new TextField("20000");
    private Button runButton = new Button("Run");
    private Button stepButton = new Button("Step");
    private CheckBox realTimeUpdates = new CheckBox("Real time updates");
    
    private Thread worker;
    
    public SidePane(Model model, MapPane map) {
        super();
        this.model = model;
        this.map = map;
        
        realTimeUpdates.setSelected(true);
        runButton.setOnAction(event -> runButtonHandler());
        stepButton.setOnAction(event -> stepButtonHandler());
        setSpacing(4);
        setMinWidth(200);
        
        getChildren().addAll(timeLabel,
                new Separator(Orientation.HORIZONTAL), 
                new VBox(iterations, realTimeUpdates, new HBox(runButton, stepButton)),
                new Separator(Orientation.HORIZONTAL),
                new InfoPane(map, model.world));
        updateUI();
    }
    
    /**
     * Refreshes the whole interface, including the map pane.
     * 
     * This is synchronised so that the model thread knows when
     * the UI is finished updating; this in turn allows the UI
     * to update after every iteration.
     */
    private synchronized void updateUI() {
        timeLabel.setText("Time: " + model.world.time);

        for (int x = 0; x < model.world.xDimension; x++) {
            for (int y = 0; y < model.world.yDimension; y++) {
                map.getCells()[x][y].update(model.getGlobalCrimeMax());
            }
        }
        notifyAll();
    }

    /**
     * If the model isn't running, this creates a thread
     * and kicks it off. Otherwise, it stops the thread
     * and cleans up.
     */
    private void runButtonHandler() {
        if (worker != null && worker.isAlive()) {
            worker.interrupt();
        } else {
            stepButton.setDisable(true);
            iterations.setDisable(true);
            realTimeUpdates.setDisable(true);
            worker = new Thread(this::runModel);
            worker.start();
            runButton.setText("Stop");
        }
    }
    
    private void stepButtonHandler() {
        model.doIteration();
        updateUI();
    }
    
    /**
     * This is a callback used in the model thread and is
     * never called directly. If real time updates are 
     * enabled, this releases the lock after every iteration
     * and waits for the scheduled updateUI task to notify it.
     */
    private synchronized void runModel() {
        try {
            int runs = Integer.valueOf(iterations.getText());
            for (int i = 0; i < runs; i++) {
                model.doIteration();
                if (realTimeUpdates.isSelected()) {
                    Platform.runLater(this::updateUI);
                    wait();
                } else if (worker.isInterrupted()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            // e.printStackTrace();
        } finally {
            // clean up from the correct thread when we are done
            Platform.runLater(() -> {
                stepButton.setDisable(false);
                iterations.setDisable(false);
                realTimeUpdates.setDisable(false);
                runButton.setText("Run");
                updateUI();
            });
        }
    }
    
}
