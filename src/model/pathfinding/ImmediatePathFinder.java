package model.pathfinding;

import java.util.List;

import model.action.Direction;
import model.world.WorldWrapper;

/**
 * Computes paths immediately in the same thread as the method is called.
 */
public class ImmediatePathFinder extends PathFinder {

    public ImmediatePathFinder(WorldWrapper worldWrapper) {
        super(worldWrapper);
    }

    /**
     * Computes a path from and to the specified coordinates, not taking
     * agents into account as obstructions. This blocks until it returns
     * the path, though it could return null if an error occurs.
     */
    @Override
    public List<Direction> computePath(int fromX, int fromY, int toX, int toY) {
        try {
            return new AStarCallable(worldWrapper::isMovementObstruction, 
                    fromX, fromY, toX, toY, Integer.MAX_VALUE).call();
        } catch (Exception e) {
            log.severe("Failed to acquire path, returning null");
            return null;
        }
    }

    @Override
    public void stop() {
    }

}
