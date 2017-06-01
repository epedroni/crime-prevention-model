package model;

import model.world.World;

public final class ModelCLI {

    private ModelCLI() {}

    /**
     * Run the model headless for higher performance. Output is
     * written to stdout, including the periodic heatmap dumps.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Missing argument, please provide a parameter file.");
            System.exit(1);
        }

        Parameters parameters = new Parameters(args[0]);
        Model model = new Model(parameters);
        
        for (int i = 0; i < parameters.rounds; i++) {
            model.doIteration();
            if (i % parameters.dumpFrequency == 0) {
                dumpHeatMap(model.world);
            }
        }
        
        model.stop();
        dumpHeatMap(model.world);
    }
    
    private static void dumpHeatMap(World w) {
        System.out.format("Heat map after %s rounds\n", w.time);
        for (int y = 0; y < w.yDimension; y++) {
            for (int x = 0; x < w.xDimension; x++) {
                System.out.print(w.grid[x][y].crimeCount + ",");
            }
            System.out.println();
        }
        System.out.println("Done.");
    }
}
