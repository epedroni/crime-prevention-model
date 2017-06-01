package model.agent;

import java.util.List;
import java.util.logging.Logger;

import model.Model;
import model.Parameters;
import model.action.Direction;
import model.pathfinding.PathFinder;
import model.world.Cell;
import model.world.WorldWrapper;
import utils.Utils;

/**
 * This class provides a number of helper methods for agents to assess their
 * surroundings when making their move.
 */
public class DecisionContext {
    private static Logger log = Utils.getConsoleLogger(DecisionContext.class);
    private WorldWrapper world;
    private Agent agent;
    private PathFinder pathFinder;
    private Parameters parameters;
    
    private Cell randomDestination = null;
    
    DecisionContext(WorldWrapper worldWrapper, PathFinder pathFinder, Parameters parameters) {
        this.world = worldWrapper;
        this.pathFinder = pathFinder;
        this.parameters = parameters;
    }

    /**
     * DI due to the circular dependency between this and Agent.
     * Do not call this more than once!
     */
    void setAgent(Agent agent) {
        if (this.agent == null) {
            this.agent = agent;
            // get the first random route here (or at least get the computation started)
            randomDestination = world.getRandomCell(c -> !world.isMovementObstruction(c.x, c.y));
        } else {
            throw new IllegalStateException("RTFM!");
        }
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
    public boolean isMovementObstruction(Direction direction) {
        return world.isMovementObstruction(agent.x + direction.x, agent.y + direction.y);
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
    public boolean isViewObstruction(Direction direction) {
        return world.isViewObstruction(agent.x + direction.x, agent.y + direction.y);
    }

    /**
     * Returns true if the cell at the specified position has an assigned
     * occupant. This is not very useful on its own, but it is used as a
     * primitive for the more sophisticated context helpers.
     * 
     * Returns false if the coordinates provided are out of bounds.
     */
    public boolean isOccupied(Direction direction) {
        return world.isOccupied(agent.x + direction.x, agent.y + direction.y);
    }

    /**
     * Returns true if the specified cell is free to be occupied. This takes
     * into account both whether the cell type is an obstruction and whether the
     * cell is already occupied by another agent.
     * 
     * If the coordinate is not within the bounds of the world, this returns
     * false.
     */
    public boolean isFree(Direction direction) {
        return world.isFree(agent.x + direction.x, agent.y + direction.y);
    }

    /**
     * Returns the light value at the agent's current position.
     */
    public double getLight() {
        return world.getLight(agent.x, agent.y);
    }
    
    /**
     * Returns the light value at the specified position. If the position is not
     * within the bounds of the world, this returns 0.
     */
    public double getLight(Direction direction) {
        return world.getLight(agent.x + direction.x, agent.y + direction.y);
    }

    /**
     * Returns a list of every cell the agent sees if it looks at the specified
     * coordinates. This takes into account view obstructions; the agent cannot
     * see past cells which obstruct its view. The first cell in the list is the
     * cell where the agent is located. If no obstructions are found, the
     * returned list contains the line of cells up to the agent's vision range.
     * If the specified coordinate is reached within the agent's vision range,
     * only the cells up to the coordinate are returned.
     * 
     * If the specified position is not within the bounds of the map, an empty
     * list is returned.
     */
    public List<Cell> look(int x, int y) {
        return world.look(agent.x, agent.y, x, y, agent.visionRange);
    }

    /**
     * Returns true if the specified cell is visible, false otherwise. This
     * takes into account visibility obstructions and the agent's vision range.
     */
    public boolean isVisible(int x, int y) {
        return world.isVisible(agent.x, agent.y, x, y, agent.visionRange);
    }
    
    /**
     * Returns a list of agents surrounding this agent, in ascending order of
     * proximity. 
     */
    public List<Agent> getAgentsInSight() {
        return world.getAgentsAround(agent.x, agent.y, agent.visionRange, agent -> true);
    }
    
    /**
     * Returns a list of agents surrounding this agent, in descending order of
     * proximity (nearest first).
     */
    public List<Agent> getWitnessesInSight() {
        return world.getAgentsAround(agent.x, agent.y, agent.visionRange, a -> a.getState() != AgentState.STALKING);
    }
    
    /**
     * Returns a list of agents around this agent which satisfy this agent's
     * criminal preconditions, in descending order of proximity (nearest first).
     */
    public List<Agent> getPotentialVictims() {
        return world.getAgentsAround(agent.x, agent.y, agent.visionRange, this::wouldRobAgent);
    }
    
    /**
     * Returns a potential victim, if any are around. Otherwise, returns null.
     */
    public Agent getVictim() {
        // TODO could optimise this by first checking if the agent is likely to commit crimes at all
        List<Agent> victims = getPotentialVictims();
        if (victims.isEmpty()) {
            return null;
        } else {
            victims.sort((a1, a2) -> a1.apparentWealth > a2.apparentWealth ? -1 : a1.apparentWealth < a2.apparentWealth ? 1 : 0);
            log.fine(String.format("Found victim for %s: %s", agent, victims.get(0)));
            return victims.get(0);
        }
    }

    /**
     * Returns a route to the specified position, from the agent's location.
     * This may return null if the route is not ready yet. When a route is 
     * computed, this will return it.
     */
    public List<Direction> getRoute(int x, int y) {
        return world.withinBounds(x, y) && !world.isMovementObstruction(x, y) ?
                pathFinder.computePath(agent.x, agent.y, x, y) : null;
    }
    
    /**
     * Requests a route to a random, valid position in the world, starting from
     * the agent's position. The route might be computed in parallel to the model,
     * so this method may null until the route is ready. At that point, it
     * returns the route.
     */
    public List<Direction> getRandomRoute() {
        // if we are not waiting for a route, request one
        if (randomDestination == null) {
            randomDestination = world.getRandomCell(c -> !world.isMovementObstruction(c.x, c.y));
            log.finest(String.format("New random destination: [%s, %s]", randomDestination.x, randomDestination.y));
        }
        
        List<Direction> route = getRoute(randomDestination.x, randomDestination.y);

        if (route != null) {
            randomDestination = null;
        }
        
        return route;
    }

    /**
     * Returns the necessary steps for the agent to get to the position of the
     * provided target agent, in order.
     * 
     * This may return null if the route is not ready yet. When a route is 
     * computed, this will return it.
     */
    public List<Direction> getRouteToAgent(Agent targetAgent) {
        return getRoute(targetAgent.x, targetAgent.y);
    }
    
    /**
     * Prepends additional steps to the specified route in order to avoid
     * an immediate obstruction. This only works within the agent's vision
     * range; if the detour would exceed that, then this returns false and
     * the route is unchanged. Otherwise, this returns true and the route
     * is changed.
     */
    public boolean dodgeObstruction(List<Direction> currentRoute) {
        return pathFinder.computeDetour(agent.x, agent.y, (int) agent.visionRange, currentRoute);
    }

    /**
     * Returns the occupant of the specified cell, if any. If the cell has no
     * occupant, returns null.
     */
    public Agent getOccupant(Direction direction) {
        return world.getOccupant(agent.x + direction.x, agent.y + direction.y);
    }
    
    /**
     * Returns true if this agent would rob the provided victim, false otherwise.
     */
    public boolean wouldRobAgent(Agent victim) {
        // this is the agent's inclination to rob
        double wouldRob = ((agent.age * parameters.ageWeight) 
                + (agent.education * parameters.educationWeight)
                + (agent.income * parameters.incomeWeight)
                + (Model.rand.nextDouble() * parameters.wouldRobRandomWeight)) 
                / parameters.totalWouldRobWeight;
        
        return wouldRob < victim.apparentWealth 
                && victim.getState() != AgentState.STALKING;
    }
    
    /**
     * Returns true if the conditions around the agent are conducive
     * to criminal activities.
     */
    public boolean conditionsAreRight() {
        double lightThreshold = ((parameters.crimeLightThreshold * parameters.lightWeight)
                + (Model.rand.nextDouble() * parameters.lightRandomWeight))
                / parameters.totalConditionsWeight;
        
        return getLight() < lightThreshold 
                && getWitnessesInSight().size() <= parameters.maxWitnessesInSight;
    }
}
