package model.action;

/**
 * Possible actions for an agent to take, including the direction
 * in which they are executed (not applicable for WAIT).
 * 
 * @author Eduardo Pedroni
 *
 */
public enum Action {
    MOVE, ROB, WAIT;

    public Direction direction;
}
