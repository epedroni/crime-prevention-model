package model;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.Lighting;
import utils.Utils;

/**
 * Holds all parameters needed to run an experiment.
 * Should be created from a parameter file.
 */
public final class Parameters {
    
    private Logger log = Utils.getConsoleLogger(Parameters.class);

    // these are the defaults, in case something is missing from the file
    public int agents = 50;
    public int rounds = 10000;
    
    public boolean parallel = false;
    public int threads = 4;
    public File map;
    public File heatMapOverlay;
    public long seed = 123456789;
    public int roundTimeOut = 600;
    public int dumpFrequency = 10000;
    
    public int ageWeight = 1;
    public int educationWeight = 1;
    public int incomeWeight = 1;
    public int wouldRobRandomWeight = 1;
    public int totalWouldRobWeight = ageWeight + educationWeight + incomeWeight + wouldRobRandomWeight;
    
    public int lightWeight = 1;
    public int lightRandomWeight = 1;
    public double crimeLightThreshold = Lighting.LIGHT_THRESHOLD;
    public int maxWitnessesInSight = 2;
    public int totalConditionsWeight = lightWeight + lightRandomWeight;

    public Parameters(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            throw new Exception("Invalid parameter file path: " + filePath);
        }
        int line = 0;
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                String[] parameter = input.nextLine().split("(?<!\\\\) ");
                line++;
                if (parameter.length > 1) {
                    switch (parameter[0]) {
                    case "agents":
                        agents = Integer.parseInt(parameter[1]);
                        break;
                    case "rounds":
                        rounds = Integer.parseInt(parameter[1]);
                        break;
                    case "map":
                        map = new File(parameter[1]);
                        if (!map.exists() || !map.canRead()) {
                            throw new Exception("Invalid map path: " + parameter[1]);
                        }
                        break;
                    case "parallel":
                        parallel = Boolean.parseBoolean(parameter[1]);
                        break;
                    case "threads":
                        threads = Integer.parseInt(parameter[1]); 
                        break;
                    case "seed":
                        seed = Long.parseLong(parameter[1]);
                        break;
                    case "round_timeout":
                        roundTimeOut = Integer.parseInt(parameter[1]);
                    case "dump_frequency":
                        dumpFrequency = Integer.parseInt(parameter[1]);
                    case "age_weight":
                        ageWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "education_weight":
                        educationWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "income_weight":
                        incomeWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "wouldrob_random_weight":
                        wouldRobRandomWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "light_weight":
                        lightWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "light_random_weight":
                        lightRandomWeight = Integer.parseInt(parameter[1]);
                        break;
                    case "crime_light_threshold":
                        crimeLightThreshold = Double.parseDouble(parameter[1]);
                        break;
                    case "max_witnesses_in_sight":
                        maxWitnessesInSight = Integer.parseInt(parameter[1]);
                        break;
                    case "heat_map":
                    	heatMapOverlay = new File(parameter[1]);
                        if (!heatMapOverlay.exists() || !heatMapOverlay.canRead()) {
                            throw new Exception("Invalid heat map path: " + parameter[1]);
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
            updateTotals();
            log();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to parse line " + line, e);
            throw e;
        }
    }
    
    private void updateTotals() {
        totalWouldRobWeight = ageWeight + educationWeight + incomeWeight + wouldRobRandomWeight;
        totalConditionsWeight = lightWeight + lightRandomWeight;
    }
    
    public void log() {
        log.info("Parameters:");
        log.info("Agents: " + agents);
        log.info("Rounds: " + rounds);
        log.info("Map: " + map.getAbsolutePath());
        log.info("Running in parallel: " + parallel);
        log.info("Threads: " + threads);
        log.info("Seed: " + seed);
        log.info("Round timeout: " + roundTimeOut);
        log.info("Dump frequency: " + dumpFrequency);
        log.info("Age weight: " + ageWeight);
        log.info("Education weight: " + educationWeight);
        log.info("Income weight: " + incomeWeight);
        log.info("Would rob random weight: " + wouldRobRandomWeight);
        log.info("Light weight: " + lightWeight);
        log.info("Light random weight: " + lightRandomWeight);
        log.info("Crime light threshold: " + crimeLightThreshold);
        log.info("Maximum witnesses in sight: " + maxWitnessesInSight);
        log.info("Heat map file: " + heatMapOverlay);
    }
}    
