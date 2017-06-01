package model.agent;

import java.util.List;
import java.util.logging.Logger;

import model.Model;
import model.action.Action;
import model.action.Direction;
import utils.Utils;

public class Agent {
    private Logger log = Utils.getConsoleLogger(this.toString());

    /**
     * The agent's position.
     */
    public int x, y;

    /**
     * These are fixed characteristics of each agent. Whether the agent commits
     * crimes or not is a function of these values.
     * 
     * Age, education, income and apparentWealth are stored as doubles between 0
     * and 1, where 0 represents the minimum possible value and 1 represents the
     * maximum possible value.
     */
    public final double age, education, income, apparentWealth;

    /**
     * Each agent has an inherent vision range. This value, measured in cells,
     * determines how far the agent is willing to look around when considering
     * its surroundings.
     */
    public final double visionRange;

    /**
     * The agent's current state. See {@code AgentState} for more information. 
     */
    public AgentState state = AgentState.THINKING;

    /**
     * This object exposes the model world through the eyes the agent, taking into
     * account visual obstructions, vision range and individual traits.
     */
    private DecisionContext context;

    /**
     * The agent's current route, if any.
     */
    private List<Direction> route;

    /**
     * The agent that this agent is currently interested in robbing, if any.
     */
    private Agent targetAgent = null;

    /**
     * Keeps track of how long the agent has been waiting for, and how
     * long the agent is willing to wait (chosen randomly and periodically).
     */
    private int wait = 0, maxWait = 0;
    
    /**
     * Active agents are in the model, inactive ones have been removed.
     */
    public boolean active = true;
    
    /**
     * How many rounds this agent has done. Agents are removed when they
     * reach the round limit.
     */
    private int rounds = 0;

    /**
     * Agents are created using AgentFactory.
     */
    Agent(DecisionContext context, double age, double education, double income, double apparentWealth,
            double visionRange) {
        this.context = context;
        this.age = age;
        this.education = education;
        this.income = income;
        this.apparentWealth = apparentWealth;
        this.visionRange = visionRange;
    }

    /**
     * Return an action for the model.
     */
    public Action act() {
        log.finest(String.format("State: %s", state));

        Action action;
        switch (state) {
        case LOITERING:
            action = loiter();
            break;
        case MOVING:
            action = move();
            break;
        case STALKING:
            action = stalk();
            break;
        case THINKING:
            action = think();
            break;
        default:
            // disaster recovery
            setState(AgentState.THINKING);
            action = Action.WAIT;
            break;
        }

        // update wait counter
        if (action == Action.WAIT) {
            wait--;
        } else {
            wait = maxWait;
        }
        
        rounds++;
        
        return action;
    }

    /**
     * - Request new route;
     * - Check if route is ready;
     * - When it is, switch to moving.
     */
    private Action think() {
        log.fine("Requesting random route");
        route = context.getRandomRoute();

        if (route != null) {
            log.fine("Got route, moving");
            setState(AgentState.MOVING);
        }

        return Action.WAIT;
    }

    /**
     * - If a suitable victim is near, get route and stalk;
     * - If not and the route is empty, loiter;
     * - If not and the next step is free, take it;
     * - If not, flip a coin, if heads figure out a detour;
     * - If not and we have waited too long, think;
     * - Otherwise, wait.
     */
    private Action move() {
        Action action = Action.WAIT;
        targetAgent = context.getVictim();
        
        if (targetAgent != null) {
            route = context.getRouteToAgent(targetAgent);
            setState(AgentState.STALKING);
        } else if (route.isEmpty()) {
            setState(AgentState.LOITERING);
        } else if (context.isFree(route.get(0))) {
            action = Action.MOVE;
            action.direction = route.remove(0);
        } else if(Model.rand.nextBoolean()) {
            log.fine("Attempting to dodge obstruction");
            if (context.dodgeObstruction(route)) {
                log.fine("Got detour, moving");
                action = Action.MOVE;
                action.direction = route.remove(0);
            }
        } else if (wait <= 0) {
            log.fine("Timed out, thinking");
            setState(AgentState.THINKING);
        }

        return action;
    }

    /**
     * - If the target is gone or stalking someone, loiter.
     * - If not and the route is empty, poll for a new one.
     * - If not and the next step is free, take it.
     * - If not and the next step is blocked by the target, rob it.
     * - If not, flip a coin, if heads figure out a detour;
     * - If not and we have waited too long, loiter;
     */
    private Action stalk() {
        Action action = Action.WAIT;
        if (!targetAgent.active || targetAgent.getState() == AgentState.STALKING) {
            setState(AgentState.LOITERING);
        } else if (route == null || route.isEmpty()) {
            route = context.getRouteToAgent(targetAgent);
        } else if (context.isFree(route.get(0))) {
            action = Action.MOVE;
            action.direction = route.remove(0);
        } else if (context.getOccupant(route.get(0)) == targetAgent
                && context.conditionsAreRight()) {
            action = Action.ROB;
            action.direction = route.get(0);
        } else if (Model.rand.nextBoolean()) {
            if (context.dodgeObstruction(route)) {
                action = Action.MOVE;
                action.direction = route.remove(0);
            }
        } else if (wait <= 0) {
            setState(AgentState.LOITERING);
        }
        
        return action;
    }

    /**
     * - If a suitable victim is near, get route and stalk;
     * - If not and we have waited long enough, think;
     * - Else, do nothing.
     */
    private Action loiter() {
        targetAgent = context.getVictim();
        if (targetAgent != null) {
            route = context.getRouteToAgent(targetAgent);
            setState(AgentState.STALKING);
        } else if (wait <= 0) {
            setState(AgentState.THINKING);
        }
        return Action.WAIT;
    }
    
    /**
     * Updates the state field and resets the wait counter.
     */
    private void setState(AgentState newState) {
        state = newState;
        switch (state) {
        case LOITERING:
            maxWait = AgentState.BASE_LOITER + Model.rand.nextInt(AgentState.MAX_LOITER);
            wait = maxWait;
            break;
        case MOVING:
        case STALKING:
            maxWait = AgentState.BASE_TIMEOUT + Model.rand.nextInt(AgentState.MAX_TIMEOUT);
            wait = maxWait;
            break;
        default:
            break;
        
        }
    }

    public AgentState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Agent@" + Integer.toHexString(hashCode());
    }

    public int getRounds() {
        return rounds;
    }

    public Agent getTarget() {
        return targetAgent;
    }

}
