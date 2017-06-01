package model.map;

import model.world.CellType;
import model.world.World;
import utils.Utils;

public class Lighting {
    /**
     * Radius reached by a light source. At this distance, the light value will
     * be equal to LIGHT_THRESHOLD.
     */
    public static final double LIGHT_RADIUS = 15;
    /**
     * Any light values below this will be rounded to 0. Therefore, any cells
     * beyond LIGHT_DISTANCE cells away from the light source will be dark
     * (light = 0).
     */
    public static final double LIGHT_THRESHOLD = 0.1;
    
    /**
     * These are pre-computed from the ones above, don't touch them
     */
    public static final double EXPONENT = Math.log(LIGHT_THRESHOLD) / Math.log(1 + LIGHT_RADIUS);
    public static final int RADIUS_INT = (int) Math.floor(LIGHT_RADIUS);
    
    /**
     * Each cell has a light value which is calculated based on its proximity to
     * lamps.
     * 
     * The light from lamps attenuates with the square of the distance? Maybe in
     * real, but here it can be customised.
     * 
     */
    public static void computeLighting(World w) {
        for (int y = 0; y < w.yDimension; y++) {
            for (int x = 0; x < w.xDimension; x++) {
                // compute the light value around each lamp based on the range
                if (w.grid[x][y].type == CellType.LAMP) {
                    computeLamp(w, x, y);
                }
            }
        }
    }

    /**
     * Computes the lighting for a single lamp and assigns it to that lamp.
     */
    public static void computeLamp(World w, int x, int y) {
        for (int i = -RADIUS_INT; i <= RADIUS_INT; i++) {
            for (int j = -RADIUS_INT; j <= RADIUS_INT; j++) {
                if (w.withinBounds(x + i, y + j) 
                        && (w.grid[x + i][y + j].type == CellType.PATH || w.grid[x + i][y + j].type == CellType.LAMP) 
                        && !isObstructed(w, x, y, x + i, y + j)) {
                    double light = Math.pow(1 + Utils.getDistance(i, j), EXPONENT);
                    if (light < LIGHT_THRESHOLD)
                        light = 0;
                    w.grid[x + i][y + j].light = Math.max(w.grid[x + i][y + j].light, light);
                }
            }
        }
    }
    
    /**
     * Used to determine if there is a line of sight between two points in the world.
     */
    private static boolean isObstructed(World w, int startX, int startY, int endX, int endY) {
        // dirty hack
        class BooleanValue {
            public boolean value = true;
        }
        BooleanValue obstructed = new BooleanValue();
        w.computeLine(startX, startY, endX, endY, cell -> {
            obstructed.value = !(cell.x == endX && cell.y == endY);
            return !cell.type.obstructsView;
        });
        return obstructed.value;
    }
}
