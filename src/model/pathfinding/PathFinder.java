package model.pathfinding;

import java.util.List;
import java.util.logging.Logger;

import model.action.Direction;
import model.world.WorldWrapper;
import utils.Utils;

public abstract class PathFinder {
    protected Logger log = Utils.getConsoleLogger(this.getClass());
    protected WorldWrapper worldWrapper;

    public PathFinder(WorldWrapper worldWrapper) {
        this.worldWrapper = worldWrapper;
    }

    public abstract List<Direction> computePath(int fromX, int fromY, int toX, int toY);

    /**
     * Computes a detour around immediate obstructions. This method always blocks while the
     * route is being computed. It returns true if the provided route was modified to dodge
     * the obstruction, or false if a path could not be found (in which the case the route
     * is unchanged).
     * 
     * @author Milan Pandurov
     */
    public final boolean computeDetour(int fromX, int fromY, int limit, List<Direction> currentRoute) {
        boolean foundFreeCell = false;
        int elements = 0;
        int x = fromX, y = fromY;
        for (Direction direction : currentRoute) {
            x += direction.x;
            y += direction.y;
            elements++;
            if (worldWrapper.isFree(x, y)) {
                foundFreeCell = true;
                break;
            } else if (elements > limit) {
                break;
            }
        }

        if (!foundFreeCell) {
            log.finest("Can't find a new route");
            return false;
        }

        log.finest("Calculating detour");
        try {
            List<Direction> additionalRoute = new AStarCallable((cx, cy) -> !worldWrapper.isFree(cx, cy), 
                    fromX, fromY, x, y, limit).call();
            if (!additionalRoute.isEmpty()) {
                currentRoute.subList(0, elements).clear();
                currentRoute.addAll(0, additionalRoute);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.severe("Failed to acquire path, returning false");
            return false;
        }
    }

    public abstract void stop();
}
