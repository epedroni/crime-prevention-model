package model.agent;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import model.Model;
import model.Parameters;
import model.pathfinding.PathFinder;
import model.world.Cell;
import model.world.WorldWrapper;
import utils.Utils;

/**
 * Handles the creation of agents, since it involves more steps than just
 * instantiating the Agent class.
 * 
 * @author Eduardo Pedroni
 *
 */
public class AgentFactory {
    
    private static Logger log = Utils.getConsoleLogger(AgentFactory.class);

    private AgentFactory() {
    }

    /**
     * Adds a bunch of agents to the world and returns a list of all the
     * agents created.
     * 
     * TODO sort out agent, education and income distributions, should use
     * something like poisson instead of uniform
     */
    public static List<Agent> populate(PathFinder pathFinder, WorldWrapper worldWrapper, Parameters parameters) {
        LinkedList<Agent> agents = new LinkedList<>();
        log.info("Creating agents");
        for (int i = 0; i < parameters.agents; i++) {
            agents.add(createAgent(pathFinder, worldWrapper, parameters));
        }
        return agents;
    }
    
    /**
     * Creates a single agent, adds it to the world and returns it.
     */
    public static Agent createAgent(PathFinder pathFinder, WorldWrapper worldWrapper, Parameters parameters) {
        DecisionContext context = new DecisionContext(worldWrapper, pathFinder, parameters);
        Agent agent = new Agent(context, Model.rand.nextDouble(), Model.rand.nextDouble(), Model.rand.nextDouble(),
                Model.rand.nextDouble(), (Model.rand.nextDouble() * 4) + 4);
        context.setAgent(agent);

        // find an available cell
        Cell cell = worldWrapper.getRandomCell(c -> worldWrapper.isFree(c.x, c.y));

        // put the agent in it
        agent.x = cell.x;
        agent.y = cell.y;
        cell.occupant = agent;

        log.finest("Created new agent at [" + agent.x + "," + agent.y + "]");
        return agent;
    }
}
