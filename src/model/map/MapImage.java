package model.map;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import model.world.Cell;
import model.world.CellType;
import model.world.World;
import utils.Utils;

/**
 * Loads an image as a map.
 */
public class MapImage {
    private static Logger log = Utils.getConsoleLogger(MapImage.class);
    
    /**
     * Number of pixels which are averaged to create a single cell.
     */
    private static int SCALE = 4;

    private static boolean isBlockRoad(BufferedImage img, int x, int y) {
        int roadPixels = 0;
        int totalPixels = SCALE * SCALE;
        for (int i = x; i < x + SCALE; ++i) {
            for (int j = y; j < y + SCALE; ++j) {
                int a = img.getRGB(i, j);
                Color c = new Color(a);
                int clr = c.getRed();
                if (clr >= 230) { // Fucking Java
                    roadPixels++;
                }
            }
        }

        if (roadPixels > (totalPixels / 3)) {
            return true;
        } else
            return false;
    }

    /**
     * Returns the world loaded from the specified image file if valid, 
     * null otherwise.
     */
    public static World loadWorldFromImage(File file) {
        World w = null;
        try {
            BufferedImage img = ImageIO.read(file);
            w = new World(img.getWidth() / SCALE, img.getHeight() / SCALE);
            log.info("Image size is: " + img.getWidth() / SCALE + " " + img.getHeight() / SCALE);

            int i = 0;
            int j = 0;
            for (int x = 0; x <= img.getWidth() - SCALE; x += SCALE) {
                j = 0;
                for (int y = 0; y <= img.getHeight() - SCALE; y += SCALE) {
                    if (isBlockRoad(img, x, y)) {
                        w.grid[i][j] = new Cell(CellType.PATH, i, j);
                    } else {
                        w.grid[i][j] = new Cell(CellType.HOUSE, i, j);
                    }
                    j++;
                }
                i++;
            }
        } catch (Exception e) {
            log.severe(e.getMessage());
            e.printStackTrace();
        }

        return w;
    }

}
