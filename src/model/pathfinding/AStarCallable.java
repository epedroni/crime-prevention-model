package model.pathfinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import model.action.Direction;
import utils.Utils;

/**
 * A* search encapsulated in a callable. This can either be executed directly
 * or submitted to a thread executor for parallel computation.
 * 
 * @author Milan Pandurov
 */
class AStarCallable implements Callable<List<Direction>> {
    private static Logger log = Utils.getConsoleLogger(AStarCallable.class);
    
    private Point source, destination;
    private List<Point> closedPoints;
    private PriorityQueue<Point> openPoints;
    private BiFunction<Integer, Integer, Boolean> occupiedHandler;
    private int limit;
    
    public AStarCallable(BiFunction<Integer, Integer, Boolean> occupiedHandler, int fromX, int fromY, int toX, int toY, int limit) {
        super();
        this.limit = limit;
        this.occupiedHandler = occupiedHandler;
        this.source = new Point(fromX, fromY);
        this.destination = new Point(toX, toY);
        this.openPoints = new PriorityQueue<>((Point o1, Point o2) -> o1.getScore() - o2.getScore());
        this.closedPoints = new LinkedList<>();
    }

    @Override
    public List<Direction> call() throws Exception {
        Point currentPosition = source;
        closedPoints.add(currentPosition);

        while (!currentPosition.equals(destination)) {
//            System.out.format("adding current point to closed list: %s\n",
//                    currentPosition.toString());

            List<Point> neighbourhood = getManhattanNeighborhood(currentPosition);
            for (Point neighbour : neighbourhood) {
                if (!occupiedHandler.apply(neighbour.x, neighbour.y) && !closedPoints.contains(neighbour)) {

                    Point potentialNeighbour = openPoints.stream().filter(p -> p.equals(neighbour)).findAny()
                            .orElse(new Point(neighbour.x, neighbour.y));

                    potentialNeighbour.distance = currentPosition.distance
                            + calculateManhattanDistance(currentPosition, potentialNeighbour);
                    potentialNeighbour.heuristics = calculateManhattanDistance(potentialNeighbour, destination);
                    potentialNeighbour.parent = currentPosition;
                    
//                    System.out.format("Adding potential n: %s of score %d\n",
//                            potentialNeighbour.toString(),
//                            potentialNeighbour.getScore());
                    
                    if(limit==0 || potentialNeighbour.distance<limit){
                    	openPoints.add(potentialNeighbour);
                    }
                }
            }

            Point nextPosition = null;
            if (!openPoints.isEmpty())
                nextPosition = openPoints.remove();

            if (nextPosition == null) {
                // TODO: Handle no path available!
//            	if(limit!=0) //If we are limited then it is expected to run out of path
//            	    log.severe("Agent can't find a path!");
                return new LinkedList<Direction>();
            }

            openPoints.remove(nextPosition);
            currentPosition = nextPosition;
            closedPoints.add(currentPosition);
        }

        LinkedList<Direction> directions = new LinkedList<Direction>();
        Point nextPoint = closedPoints.get(closedPoints.size() - 1);
        while (true) {

            if (nextPoint.parent == null) {
                break;
            }

            Direction direction = convertToDirection(nextPoint.parent, nextPoint);
            if (direction == null) {
                System.err.println("Shouldn't happen");
                continue;
            }

//            log.level("Adding point " + nextPoint.toString() + " to path");
            directions.addFirst(direction);
            
            nextPoint = nextPoint.parent;
        }
        log.fine("Finished computing path from " + source + " to " + destination);
        return directions;
    }
    
    private class Point {
        public int x, y, distance, heuristics;
        public Point parent;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
            this.heuristics = 0;
            this.distance = 0;
            this.parent = null;
        }

        public int getScore() {
            return heuristics + distance;
        }

        @Override
        public boolean equals(Object obj) {
            Point p = (Point) obj;
            return p.x == this.x && p.y == this.y;
        }

        @Override
        public String toString() {
            return "[" + x + ", " + y + "]";
        }
    }

    private Direction convertToDirection(Point from, Point to) {
        if (calculateManhattanDistance(from, to) > 1) {
            System.err.println("Invalid parent point!");
        }

        if (to.x > from.x) {
            return Direction.RIGHT;
        }
        if (to.x < from.x) {
            return Direction.LEFT;
        }
        if (to.y < from.y) {
            return Direction.UP;
        }
        if (to.y > from.y) {
            return Direction.DOWN;
        }

        // TODO: Throw error!

        return null;
    }

    private int calculateManhattanDistance(Point from, Point to) {
        return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }

    private List<Point> getManhattanNeighborhood(Point from) {
        ArrayList<Point> res = new ArrayList<Point>(4);

        res.add(new Point(from.x + 1, from.y));
        res.add(new Point(from.x - 1, from.y));
        res.add(new Point(from.x, from.y + 1));
        res.add(new Point(from.x, from.y - 1));

        return res;
    }
}