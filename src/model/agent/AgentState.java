package model.agent;

public enum AgentState {
    /**
     * The agent is thinking about where to walk next. Transitioning into
     * this state causes the agent to stop whatever it is doing, acquire
     * a new route to a random location and immediately begin following
     * that route.
     */
    THINKING("#99ffff"),
    /**
     * The agent is following a random route, while taking in the surroundings.
     * The primary goal of an agent in this state is to find someone to rob.
     * Failing that, the secondary goal is to take the next step in the route.
     */
    MOVING("#00ff00"),
    /**
     * The agent is following another agent with criminal intentions. The
     * primary goal of an agent in this state is to approach and rob the
     * victim.
     */
    STALKING("#ff8800"),
    /**
     * The agent has taken the last step in its route and is now waiting
     * around before setting out again. The primary goal of an agent in this
     * state is to find someone to rob. The secondary goal of an agent in this
     * state is to wait for the predetermined number of rounds.
     */
    LOITERING("#ff00ff");
    
    /**
     * When an agent has a destination and gets stuck, it waits for a given
     * timeout before finding a new destination. The timeout is chosen randomly
     * as BASE_TIMEOUT plus a random integer between 0 and MAX_TIMEOUT.
     */
    public static final int BASE_TIMEOUT = 5, MAX_TIMEOUT = 5;
    
    /**
     * When the agent reaches a destination, it is busy for a random number of
     * rounds which is equal to BASE_LOITER plus a random integer between 0
     * and MAX_LOITER.
     */
    public static final int BASE_LOITER = 10, MAX_LOITER = 30;
    
    public final String colour;
    
    private AgentState(String colour) {
        this.colour = colour;
    }
}
