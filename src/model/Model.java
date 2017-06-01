package model;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import model.action.Action;
import model.agent.Agent;
import model.agent.AgentFactory;
import model.map.MapImage;
import model.map.MapParser;
import model.pathfinding.ConcurrentPathFinder;
import model.pathfinding.ImmediatePathFinder;
import model.pathfinding.PathFinder;
import model.world.Cell;
import model.world.World;
import model.world.WorldWrapper;
import utils.Utils;

/**
 * Top-level class which represents the whole model.
 * 
 * @author Eduardo Pedroni
 *
 */
public class Model {
    public World world;
    private WorldWrapper worldWrapper;
    private PathFinder pathFinder;
    private List<Agent> agents, victims;
    private int round = 0, maxCrime = 0;
    private Parameters parameters;
    
    public static Random rand;
    private Logger log = Utils.getConsoleLogger(getClass());

    public Model(Parameters parameters) {
        log.info("Initialising model");
        this.parameters = parameters;
        rand = new Random(parameters.seed);
        log.info("Loading map: " + parameters.map.getPath());
        if (parameters.map.getName().endsWith(".map")) {
            this.world = MapParser.parse(parameters.map);
        } else if (parameters.map.getName().endsWith(".jpeg")) {
            this.world = MapImage.loadWorldFromImage(parameters.map);
        } else {
            System.err.println("Unknown map format!");
            System.exit(1);
        }
    	
        this.worldWrapper = new WorldWrapper(world);
        this.pathFinder = parameters.parallel ? new ConcurrentPathFinder(worldWrapper) : new ImmediatePathFinder(worldWrapper);
        log.info("Populating map with " + parameters.agents + " agents");
        this.agents = AgentFactory.populate(pathFinder, worldWrapper, parameters);
        this.victims = new LinkedList<Agent>();
    }

    /**
     * Simulates a single iteration.
     */
    public void doIteration() {
        log.fine("Start round ------------------------------------------------------------------------------");

        LinkedList<Agent> shuffledList = new LinkedList<Agent>();
        
        for (Agent agent : agents) {
            
            // if this agent was robbed this round, simply leave it out
            if (victims.contains(agent)) {
                continue;
            }
            
            Agent victim = applyAction(agent);
            
            /* if the action returned a victim, it means that the agent robbed it
             * in this case, leave the robber out and mark the victim as such
             */
            if (victim != null) {
                victims.add(victim);
                // remove both agents from the model as well
                removeAgent(victim);
                removeAgent(agent);
            } else if (agent.getRounds() > parameters.roundTimeOut) {
                removeAgent(agent);
            } else {
                if (rand.nextBoolean()) {
                    shuffledList.addFirst(agent);
                } else {
                    shuffledList.addLast(agent);
                }
            }
        }
        
        // remove all victims from the model
        if (shuffledList.removeAll(victims)) {
            log.fine(String.format("Victims were removed from the model, current agent count: %s", shuffledList.size()));
        }
        
        // top up the model if necessary
        while (shuffledList.size() < parameters.agents) {
            shuffledList.addFirst(AgentFactory.createAgent(pathFinder, worldWrapper, parameters));
            log.fine(String.format("Added new agent to model: %s", shuffledList.getFirst()));
        }
        
        agents = shuffledList;
        victims.clear();
        world.stateTick();
        
        log.fine(String.format("Round %s done", round++));
    }

    /**
     * Gets the specified agent's action and commits it to the model.
     * Returns a victim agent, if there was a victim in this action.
     * Otherwise, returns null.
     */
    private Agent applyAction(Agent agent) {
        Agent victim = null;
        Action action = agent.act();
        
        if (action != Action.WAIT && action.direction == null) {
            return null;
        }

        Cell targetCell;
        switch (action) {
        case MOVE:
            targetCell = world.grid[agent.x + action.direction.x][agent.y + action.direction.y];
            if (worldWrapper.isFree(targetCell.x, targetCell.y)) {
                world.grid[agent.x][agent.y].occupant = null;
                agent.x += action.direction.x;
                agent.y += action.direction.y;
                world.grid[agent.x][agent.y].occupant = agent;
                log.finer(String.format("%s moved %s", agent, action.direction));
            }
            break;
        case ROB:
            targetCell = world.grid[agent.x + action.direction.x][agent.y + action.direction.y];
            victim = targetCell.occupant;
            if (victim != null) {
                maxCrime = Math.max(maxCrime, ++world.grid[agent.x][agent.y].crimeCount);
                log.info(String.format("Round %d: %s robbed %s!", round, agent, victim));
            }
            break;
        case WAIT:
            log.finer(String.format("%s waited", agent));
            break;
        default:
            log.finer(String.format("%s has no idea what it's doing, go home %s", agent, agent));
            break;
        }
        
        return victim;
    }
    
    private void removeAgent(Agent agent) {
        agent.active = false;
        world.grid[agent.x][agent.y].occupant = null;
    }
    
    /**
     * Call this to cleanly stop the model (deal with threads, etc).
     */
    public void stop() {
        pathFinder.stop();
    }
    
    /**
     * Returns the highest crime count of all cells.
     */
    public int getGlobalCrimeMax() {
        return maxCrime;
    }
}