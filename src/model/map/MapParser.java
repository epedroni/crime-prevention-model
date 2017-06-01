package model.map;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Logger;

import model.world.Cell;
import model.world.CellType;
import model.world.World;
import utils.Utils;

/**
 * Parses maps in a specific .map format.
 */
public class MapParser {
    
    private static Logger log = Utils.getConsoleLogger(MapParser.class);
    
    /**
     * Returns the world parsed from the specified file
     * if valid, null otherwise. 
     */
    public static World parse(File file) {
        try (Scanner input = new Scanner(file)) {
            input.useDelimiter("[^0-9]+");

            World w = new World(input.nextInt(), input.nextInt());

            for (int y = 0; y < w.yDimension; y++) {
                for (int x = 0; x < w.xDimension; x++) {
                    w.grid[x][y] = new Cell(CellType.types[input.nextInt()], x, y);
                }
            }
            Lighting.computeLighting(w);
            return w;
        } catch (Exception e) {
            log.severe("Could not parse map: " + file);
            e.printStackTrace();
        }
        return null;
    }
    
}
