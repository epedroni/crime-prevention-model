package model.pathfinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import model.action.Direction;
import model.world.WorldWrapper;

/**
 * Computes paths using a pool of threads, returning them once the task is complete.
 */
public class ConcurrentPathFinder extends PathFinder {
    private ThreadPoolExecutor threadPool;
    private Map<Integer, FutureTask<List<Direction>>> pendingJobs;
    
    public ConcurrentPathFinder(WorldWrapper worldWrapper) {
        super(worldWrapper);
        threadPool = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        pendingJobs = new HashMap<>();
    }

    /**
     * Computes a regular path, accounting for obstructions but not agents. The first time
     * this is called, the task to compute it is sent to the thread pool. Calling this again
     * with the same parameters returns null until the job is done, at which point the route
     * is returned. Calling it again after that creates a new task.
     */
    @Override
    public List<Direction> computePath(int fromX, int fromY, int toX, int toY) {
        FutureTask<List<Direction>> routeTask;
        List<Direction> route = null;
        int job = Objects.hash(fromX, fromY, toX, toY);
        
        if (pendingJobs.containsKey(job)) {
            routeTask = pendingJobs.get(job);
        } else {
            routeTask = createJob(fromX, fromY, toX, toY);
        }
        
        if (routeTask.isDone()) {
            pendingJobs.remove(job);
            try {
                route = routeTask.get();
            } catch (InterruptedException | ExecutionException e) {
                log.severe("Failed to calculate path, retrying");
                route = null;
                createJob(fromX, fromY, toX, toY);
            }
        }
        
        return route;
    }
    
    /**
     * Creates the job specified by the argument, schedules it and returns the created FutureTask.
     */
    private FutureTask<List<Direction>> createJob(int fromX, int fromY, int toX, int toY) {
        log.finest(String.format("Computing path from [%s, %s] to [%s, %s]", 
                fromX, fromY, toX, toY));
        FutureTask<List<Direction>> routeTask = new FutureTask<>(
                new AStarCallable(worldWrapper::isMovementObstruction, 
                fromX, fromY, toX, toY, Integer.MAX_VALUE));
        pendingJobs.put(Objects.hash(fromX, fromY, toX, toY), routeTask);
        log.finest(String.format("%s pending jobs", pendingJobs.size()));
        threadPool.execute(routeTask);
        return routeTask;
    }

    /**
     * Terminates the thread pool, any computations in progress are interrupted and discarded.
     */
    @Override
    public void stop() {
        log.info("Shutting down pathfinder threads");
        threadPool.shutdownNow();
    }
}
