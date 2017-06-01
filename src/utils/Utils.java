package utils;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class Utils {
    
    public static Level LOGGING_LEVEL = Level.INFO;
    private static Handler logHandler;
    
    /*
     * Why u do this java =(
     */
    static {
        logHandler = new StreamHandler(System.out, new Formatter() {
            @Override
            public String format(LogRecord arg0) {
                return String.format("%-15s : %s\n", arg0.getLoggerName(), arg0.getMessage());
            }
        }) {
            @Override
            public synchronized void publish(final LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        logHandler.setLevel(Level.ALL);
    }

    /**
     * Returns a logger that actually logs to stdout, because apparently that
     * is too much to ask of the standard library.
     */
    public static Logger getConsoleLogger(Class<?> owner) {
        Logger log = Logger.getLogger(owner.getSimpleName());
        log.setUseParentHandlers(false);
        log.addHandler(logHandler);
        log.setLevel(LOGGING_LEVEL);
        return log;
    }
    
    /**
     * Returns a logger that actually logs to stdout, because apparently that
     * is too much to ask of the standard library.
     */
    public static Logger getConsoleLogger(String name) {
        Logger log = Logger.getLogger(name);
        log.setUseParentHandlers(false);
        log.addHandler(logHandler);
        log.setLevel(LOGGING_LEVEL);
        return log;
    }
    
    /**
     * Returns the hypotenuse of the triangle formed by the specified deltas.
     */
    public static double getDistance(int x, int y) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }
}
