package gui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * This is the base class that determines what cells look like on the screen.
 * The GUI model and the map tool both use extensions of this.
 * 
 * @author Eduardo Pedroni
 *
 */
public abstract class BaseCellNode extends StackPane {
    public static final int SIZE = 8;
    protected final String baseStyle = "-fx-alignment: center;";
    private Rectangle lightLayer = new Rectangle(SIZE, SIZE, Color.web("#ffff99"));

    public BaseCellNode() {
        super();
        setStyle(baseStyle);
        setPrefSize(SIZE, SIZE);
        lightLayer.setOpacity(0);
        getChildren().add(lightLayer);
    }
    
    /**
     * Control the opacity of the light layer.
     * 
     * @param opacity a double between 0 and 1, 0 being dark and 1 being lit.
     */
    public void setLight(double opacity) {
        lightLayer.setOpacity(opacity);
    }
    
    public double getLight() {
        return lightLayer.getOpacity(); 
    }
}
