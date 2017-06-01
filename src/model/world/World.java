package model.world;

import java.util.function.Function;

/**
 * Fundamental representation of a world, consisting of a grid of cells.
 */
public class World {

    public Cell[][] grid;
    public int time, xDimension, yDimension;

    public World(int xDimension, int yDimension) {
        this.xDimension = xDimension;
        this.yDimension = yDimension;
        this.grid = new Cell[xDimension][yDimension];
        this.time = 0;
    }

    public void stateTick() {
        this.time++;
    }

    /**
     * Checks if the provided coordinates are within the bounds of the world.
     */
    public boolean withinBounds(int x, int y) {
        return x < xDimension && x >= 0 && y < yDimension && y >= 0;
    }

    /**
     * Computes the line of cells which connects the specified start and end
     * points. Each cell along the path is passed to the callback before the
     * next cell is computed. If the callback returns false, the method returns
     * immediately without computing the rest of the path.
     * 
     * This is a full 8 octant implementation of Bresenham's line algorithm,
     * adapted from:
     * http://tech-algorithm.com/articles/drawing-line-using-bresenham-algorithm/
     */
    public void computeLine(int startX, int startY, int endX, int endY, Function<Cell, Boolean> callback) {
        if (!withinBounds(startX, startY) || !withinBounds(endX, endY)) {
            return;
        }
        int x = startX;
        int y = startY;

        int deltaX = endX - startX;
        int deltaY = endY - startY;

        int dx1 = deltaX < 0 ? -1 : deltaX > 0 ? 1 : 0;
        int dy1 = deltaY < 0 ? -1 : deltaY > 0 ? 1 : 0;

        int dx2 = deltaX < 0 ? -1 : deltaX > 0 ? 1 : 0;
        int dy2 = 0;

        if (!(Math.abs(deltaX) > Math.abs(deltaY))) {
            dy2 = deltaY < 0 ? -1 : deltaY > 0 ? 1 : 0;
            dx2 = 0;
        }

        int longest = Math.max(Math.abs(deltaX), Math.abs(deltaY));
        int shortest = Math.min(Math.abs(deltaX), Math.abs(deltaY));
        int numerator = longest >> 1;

        for (int i = 0; i <= longest; i++) {
            if (!callback.apply(grid[x][y])) {
                return;
            }
            numerator += shortest;
            if (!(numerator < longest)) {
                numerator -= longest;
                x += dx1;
                y += dy1;
            } else {
                x += dx2;
                y += dy2;
            }
        }
    }
}
