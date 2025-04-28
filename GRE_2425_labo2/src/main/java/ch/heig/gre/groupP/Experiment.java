package ch.heig.gre.groupP;

import ch.heig.gre.graph.GridGraph;
import ch.heig.gre.graph.GridGraph2D;
import ch.heig.gre.graph.PositiveWeightFunction;
import ch.heig.gre.graph.VertexLabelling;
import ch.heig.gre.maze.BoolVertexLabelling;
import ch.heig.gre.maze.GridMazeSolver;
import ch.heig.gre.maze.MazeBuilder;
import ch.heig.gre.maze.MazeGenerator;
import ch.heig.gre.maze.impl.GridMazeBuilder;
import ch.heig.gre.maze.impl.MazeTuner;
import ch.heig.gre.maze.impl.ShenaniganWeightFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

// TODO: renommer le package (Shift + F6) selon la lettre attribuée à votre groupe

public final class Experiment {
  /** Dimension de la grille (carrée) */
  private static final int SIDE = 1100;

  /** Sommets source et destination pour les expériences */
  private static final int SRC = 550500;
  private static final int DST = 660600;

  /** Nombre de grilles à générer pour chaque expérience */
  private static final int N = 100;

  /** Topologie de la grille */
  private static final GridGraph2D TOPOLOGY;

  static {
    var g = new GridGraph(SIDE);
    GridGraph.bindAll(g);
    TOPOLOGY = g;
  }

  /** Paramètres des expériences à réaliser */
  private static final Params[] PARAMS = {
      new Params(
          "Relief très peu dense, labyrinthe très ouvert",
          new double[]{0, 0.15, 20, 1, 20}),
      new Params(
          "Relief très peu dense, labyrinthe assez ouvert",
          new double[]{0, 0.1, 20, 1, 20}),
      new Params(
          "Relief très peu dense, labyrinthe peu ouvert",
          new double[]{0, 0.01, 20, 1, 20}),
      new Params(
          "Relief dense, labyrinthe moyennement ouvert",
          new double[]{0.25, 0.05, 25, 5, 20}),
      new Params(
          "Relief très dense, labyrinthe moyennement ouvert",
          new double[]{0.5, 0.05, 25, 5, 20}),
      new Params(
          "Relief très dense et fortement pondéré, labyrinthe moyennement ouvert",
          new double[]{0.5, 0.05, 25, 5, 100})
  };

  /**
   * <p>Paramètres d'une expérience, avec une description approximative de leurs effets sur la génération.</p>
   *
   * <p>À passer en paramètre de la méthode {@link #generateGrid} pour générer un labyrinthe.</p>
   *
   * @param description Description de l'expérience
   * @param parameters  Paramètres de l'expérience
   */
  record Params(String description, double[] parameters) {}

  public static void main(String[] args) {
    // Single flag to determine which experiment to run
    // Set to true to run only the K-Manhattan experiment, false for standard experiment
    boolean runKExperimentOnly = false;
    
    System.out.println(runKExperimentOnly ? 
        "Running K-Manhattan experiment only" : 
        "Running standard experiment (H0-H3)");

    // Define the algorithms to test
    List<GridMazeSolver> solvers = Arrays.asList(
        new AStar(AStar.Heuristic.DIJKSTRA),    // H0 (reference - Dijkstra)
        new AStar(AStar.Heuristic.INFINITY_NORM),  // H1
        new AStar(AStar.Heuristic.EUCLIDEAN_NORM), // H2
        new AStar(AStar.Heuristic.MANHATTAN)       // H3
    );

    // Generator for the maze
    MazeGenerator generator = new DfsGenerator();
    
    // Iterate through each parameter set
    for (Params params : PARAMS) {
        System.out.println("\n========================================================");
        System.out.println("Experiment: " + params.description());
        System.out.println("========================================================");
        
        double refVerticesProcessed = 0;
        
        if (!runKExperimentOnly) {
            // Run the standard experiment (H0-H3)
            // Statistics arrays for all algorithms
            double[] avgPathLengths = new double[solvers.size()];
            double[] avgVerticesProcessed = new double[solvers.size()];
            
            // Run N experiments
            for (int exp = 0; exp < N; exp++) {
                // Generate a new maze with the current parameters
                RandomGenerator rng = new Random(exp); // Seed with experiment number for reproducibility
                GenerationResult result = generateGrid(generator, params.parameters(), rng);
                GridGraph2D maze = result.maze();
                PositiveWeightFunction weights = result.weights();
                
                // Run each algorithm on this maze
                for (int i = 0; i < solvers.size(); i++) {
                    GridMazeSolver solver = solvers.get(i);
                    VertexLabelling<Boolean> processed = new BoolVertexLabelling(maze.nbVertices());
                    
                    GridMazeSolver.Result solveResult = solver.solve(maze, weights, SRC, DST, processed);
                    
                    // Accumulate statistics
                    avgPathLengths[i] += solveResult.path().size();
                    avgVerticesProcessed[i] += solveResult.treatments();
                }
            }
            
            // Calculate averages and display results
            System.out.println("Results (averaged over " + N + " runs):");
            System.out.println("Algorithm\t\tAvg Path Length\tAvg Vertices Processed\t% Reduction vs Dijkstra");
            System.out.println("------------------------------------------------------------------------");
            
            // Calculate average statistics
            for (int i = 0; i < solvers.size(); i++) {
                avgPathLengths[i] /= N;
                avgVerticesProcessed[i] /= N;
            }
            
            // Reference value for Dijkstra (first algorithm)
            refVerticesProcessed = avgVerticesProcessed[0];
            
            // Display results for each algorithm
            String[] algorithmNames = {"Dijkstra (H0)", "Infinity Norm (H1)", "Euclidean (H2)", "Manhattan (H3)"};
            
            for (int i = 0; i < solvers.size(); i++) {
                double reductionPercent = i > 0 ? 100.0 * (1.0 - avgVerticesProcessed[i] / refVerticesProcessed) : 0.0;
                String reductionStr = i > 0 ? String.format("%.2f%%", reductionPercent) : "Reference";
                
                System.out.println(String.format("%-20s\t%.2f\t\t%.2f\t\t\t%s", 
                                algorithmNames[i], avgPathLengths[i], avgVerticesProcessed[i], reductionStr));
            }
        } 
        else {
            // Run only the K-Manhattan experiment
            System.out.println("Computing Dijkstra reference values...");
            // Run a smaller sample to get the Dijkstra reference
            int sampleCount = 10; // Smaller sample size for reference
            double totalProcessed = 0;
            
            for (int exp = 0; exp < sampleCount; exp++) {
                RandomGenerator rng = new Random(exp);
                GenerationResult result = generateGrid(generator, params.parameters(), rng);
                GridGraph2D maze = result.maze();
                PositiveWeightFunction weights = result.weights();
                
                VertexLabelling<Boolean> processed = new BoolVertexLabelling(maze.nbVertices());
                GridMazeSolver.Result solveResult = new AStar(AStar.Heuristic.DIJKSTRA)
                                                      .solve(maze, weights, SRC, DST, processed);
                totalProcessed += solveResult.treatments();
            }
            refVerticesProcessed = totalProcessed / sampleCount;
            System.out.println("Dijkstra reference vertices processed: " + refVerticesProcessed);
            
            System.out.println("\nSpecial experiment for K_MANHATTAN (H4):");
            System.out.println("K\t% Optimal\tMin Error\tAvg Error\tMax Error\tVertices Processed\t% Reduction");
            System.out.println("--------------------------------------------------------------------------------------");

            // Test with K values from 2 to 8
            for (int K = 2; K <= 8; K++) {
                int optimalCount = 0;
                double minError = Double.MAX_VALUE;
                double maxError = 0;
                double totalError = 0;
                totalProcessed = 0;

                // Run N experiments for this K value
                for (int exp = 0; exp < N; exp++) {
                    RandomGenerator rng = new Random(exp); // Use same seed for reproducibility
                    GenerationResult result = generateGrid(generator, params.parameters(), rng);
                    GridGraph2D maze = result.maze();
                    PositiveWeightFunction weights = result.weights();

                    // Get reference solution using Dijkstra
                    VertexLabelling<Boolean> processedRef = new BoolVertexLabelling(maze.nbVertices());
                    GridMazeSolver.Result refResult = new AStar(AStar.Heuristic.DIJKSTRA)
                            .solve(maze, weights, SRC, DST, processedRef);
                    double optimalLength = refResult.path().size();

                    // Run K_MANHATTAN with current K value
                    VertexLabelling<Boolean> processed = new BoolVertexLabelling(maze.nbVertices());
                    GridMazeSolver.Result kResult = new AStar(AStar.Heuristic.K_MANHATTAN, K)
                            .solve(maze, weights, SRC, DST, processed);

                    double kLength = kResult.path().size();

                    // Calculate error if path exists
                    if (!kResult.path().isEmpty() && !refResult.path().isEmpty()) {
                        double relativeError = (kLength - optimalLength) / optimalLength;

                        if (Math.abs(relativeError) < 1e-10) { // Consider equal with small tolerance
                            optimalCount++;
                            relativeError = 0;
                        }

                        minError = Math.min(minError, relativeError);
                        maxError = Math.max(maxError, relativeError);
                        totalError += relativeError;
                    }

                    totalProcessed += kResult.treatments();
                }

                // Calculate averages
                double avgProcessed = totalProcessed / N;
                double avgError = totalError / N;
                double reductionPercent = 100.0 * (1.0 - avgProcessed / refVerticesProcessed);
                double optimalPercent = 100.0 * optimalCount / N;

                // Display K_MANHATTAN results
                System.out.println(String.format("%d\t%.2f%%\t\t%.4f\t%.4f\t%.4f\t%.2f\t\t\t%.2f%%",
                        K, optimalPercent, minError, avgError, maxError, avgProcessed, reductionPercent));
            }
        }
    }
  }

  /**
   * Résultat de la méthode {@link #generateGrid}, fournit un labyrinthe et une fonction de pondération des arêtes
   * associée.
   *
   * @param maze    labyrinthe généré
   * @param weights Fonction de pondération associée
   */
  private record GenerationResult(GridGraph2D maze, PositiveWeightFunction weights) {}

  /**
   * Génère un labyrinthe en forme de grille avec un générateur donné et des réglages spécifiques pour le relief et
   * l'ouverture (i.e. densité de murs) du labyrinthe.
   *
   * @param generator      Générateur de labyrinthe.
   * @param tuneParameters Paramètres de réglage du relief et de l'ouverture du labyrinthe.
   * @param rng            Générateur de nombres aléatoires.
   * @return Un {@link GenerationResult} contenant le labyrinthe et la fonction de pondération associée.
   */
  private static GenerationResult generateGrid(MazeGenerator generator, double[] tuneParameters, RandomGenerator rng) {
    GridGraph maze = new GridGraph(SIDE);
    if (!generator.requireWalls())
      GridGraph.bindAll(maze);

    MazeBuilder builder = new GridMazeBuilder(TOPOLOGY, maze);
    generator.generate(builder, 0);

    MazeTuner tuner = new MazeTuner()
        .setRandomGenerator(rng)
        .setReliefDensityFactor(tuneParameters[0])
        .setWallRemovalProbability(tuneParameters[1])
        .setReliefRadiusRatio(tuneParameters[2])
        .setReliefSummitsPerRange((int) tuneParameters[3])
        .setReliefMaxSummitWeight((int) tuneParameters[4]);

    tuner.removeWalls(TOPOLOGY, maze);
    int[] weights = tuner.generateRelief(SIDE, SIDE);
    PositiveWeightFunction wf = new ShenaniganWeightFunction(weights, tuner.getReliefMinWeight());

    return new GenerationResult(maze, wf);
  }
}
