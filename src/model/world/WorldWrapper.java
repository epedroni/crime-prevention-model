package model.world;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import model.Model;
import model.agent.Agent;
import utils.Utils;

/**
 * This class provides a variety of helper methods to interact with a world
 * object in a more intuitive manner.
 * 
 * @author Eduardo Pedroni
 *
 */
public class WorldWrapper {
    private World world;

    public WorldWrapper(World world) {
        this.world = world;
    }

    /**
     * Checks if the provided coordinates are within the bounds of the world.
     */
    public boolean withinBounds(int x, int y) {
        return world.withinBounds(x, y);
    }

    /**
     * Returns true if the cell at the specified position is of a type that
     * obstructs movement. This does not take into account cell occupancy; in
     * other words, an occupied cell is NOT a movement obstruction, even though
     * it cannot be entered.
     * 
     * This is not very useful on its own, but it is used as a primitive for the
     * more sophisticated context helpers.
     * 
     * Also returns true if the coordinates provided are out of bounds.
     */
    public boolean isMovementObstruction(int x, int y) {
        return !world.withinBounds(x, y) || this.world.grid[x][y].type.obstructsMovement;
    }

    /**
     * Returns true if the cell at the specified position is of a type that
     * obstructs view.
     * 
     * This is not very useful on its own, but it is used as a primitive for the
     * more sophisticated context helpers.
     * 
     * Also returns true if the coordinates provided are out of bounds.
     */
    public boolean isViewObstruction(int x, int y) {
        return !world.withinBounds(x, y) || this.world.grid[x][y].type.obstructsView;
    }

    /**
     * Returns true if the cell at the specified position has an assigned
     * occupant. This is not very useful on its own, but it is used as a
     * primitive for the more sophisticated context helpers.
     * 
     * Returns false if the coordinates provided are out of bounds.
     */
    public boolean isOccupied(int x, int y) {
        return world.withinBounds(x, y) && this.world.grid[x][y].occupant != null;
    }

    /**
     * Returns true if the specified cell is free to be occupied. This takes
     * into account both whether the cell type is an obstruction and whether the
     * cell is already occupied by another agent.
     * 
     * If the coordinate is not within the bounds of the world, this returns
     * false.
     */
    public boolean isFree(int x, int y) {
        return world.withinBounds(x, y) && !isMovementObstruction(x, y) && !isOccupied(x, y);
    }

    /**
     * Returns the light value at the specified position, taking into account
     * artificial and natural lighting. If the position is not within the bounds
     * of the world, this returns 0.
     */
    public double getLight(int x, int y) {
        return world.withinBounds(x, y) ? this.world.grid[x][y].light : 0;
    }

    /**
     * Returns a random cell in the grid which satisfies the specified condition
     * (the condition returns true).
     * 
     * XXX: if the condition is unsatisfiable, this blocks forever :´(
     */
    public Cell getRandomCell(Function<Cell, Boolean> condition) {
        Cell cell;
        do {
            cell = world.grid[Model.rand.nextInt(world.xDimension)][Model.rand.nextInt(world.yDimension)];
        } while (!condition.apply(cell));
        return cell;
    }

    /**
     * Creates a list of all cells in a straight line from position [fromX, fromY] to position
     * [toX, toY], taking into account the specified range and visual obstructions along
     * the line of sight.
     */
    public List<Cell> look(int fromX, int fromY, int toX, int toY, double range) {
        // TODO this can be optimised
        LinkedList<Cell> cells = new LinkedList<>();
        world.computeLine(fromX, fromY, toX, toY, cell -> {
            if (Utils.getDistance(cell.x - fromX, cell.y - fromY) > range) {
                return false;
            }
            cells.add(cell);
            return !cell.type.obstructsView;
        });
        return cells;
    }

    /**
     * Returns true if position [toX, toY] can be seen from [fromX, fromY], taking into
     * account the specified range and visual obstructions along the line of sight.
     */
    public boolean isVisible(int fromX, int fromY, int toX, int toY, double range) {
        if (range < Utils.getDistance(toX - fromX, toY - fromY)) {
            return false;
        }
        // dirty hack
        class BooleanValue {
            public boolean value = false;
        }
        BooleanValue visible = new BooleanValue();
        world.computeLine(fromX, fromY, toX, toY, cell -> {
            visible.value = cell.x == toX && cell.y == toY;
            return !cell.type.obstructsView;
        });
        return visible.value;
    }

    /**
     * Returns a list of agents within the provided range around the provided position, in order of proximity.
     * The filter function is executed for each agent found, and the agent is only added to the list if it
     * returns true. 
     */
    public List<Agent> getAgentsAround(int x, int y, double range, Function<Agent, Boolean> filter) {
        LinkedList<Agent> agents = new LinkedList<Agent>();
        int visionRadius = (int) Math.floor(range);
        int limit = 2 * visionRadius + 1;
        Agent cellAgent;

        // describe a spiral around the specified position, so that the list we are building is naturally sorted by distance
        int currentX = x, currentY = y, deltaX = 1, deltaY = 0, runLength = 1, swap;
        while (deltaX == 1 || runLength < limit) {
            for (int i = 0; i < runLength; i++) {
                currentX += deltaX;
                currentY += deltaY;
                
                if (withinBounds(currentX, currentY)) {
                    cellAgent = world.grid[currentX][currentY].occupant;
                    if (cellAgent != null 
                            && isVisible(x, y, cellAgent.x, cellAgent.y, range)
                            && filter.apply(cellAgent)) {
                        agents.add(cellAgent);
                    }
                }
            }
            
            // increment run length every 2 runs
            if (deltaX == 0)
                runLength++;
            
            // 90° clockwise turn
            swap = deltaY;
            deltaY = deltaX;
            deltaX = -swap;
        }
        
        return agents;
    }

    /**
     * Returns the occupant of the specified cell, or null if it is not occupied
     * or the cell coordinates are invalid.
     */
    public Agent getOccupant(int x, int y) {
        return withinBounds(x, y) ? world.grid[x][y].occupant : null;
    }
}
