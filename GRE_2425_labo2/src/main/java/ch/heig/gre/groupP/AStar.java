package ch.heig.gre.groupP;

import ch.heig.gre.graph.GridGraph2D;
import ch.heig.gre.graph.PositiveWeightFunction;
import ch.heig.gre.graph.VertexLabelling;
import ch.heig.gre.maze.GridMazeSolver;
import javafx.util.Pair;

import java.util.*;

import static java.lang.Double.POSITIVE_INFINITY;

public final class AStar implements GridMazeSolver {
  public enum Heuristic {
    DIJKSTRA, INFINITY_NORM, EUCLIDEAN_NORM, MANHATTAN, K_MANHATTAN
  }

  private double evaluate(int delta_x, int delta_y, double minWeight) {
    double result = 0;
    switch (this.heuristic) {
      case INFINITY_NORM:
        result = Math.max(delta_x, delta_y);
        break;
      case EUCLIDEAN_NORM:
        result = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
        break;
      case MANHATTAN:
        result = Math.abs(delta_x) + Math.abs(delta_y);
        break;
      case K_MANHATTAN:
        result = this.kManhattan * (Math.abs(delta_x) + Math.abs(delta_y));
        break;
      case DIJKSTRA:
        return 0;
      // default <=> Dijstra
    }
    return result * minWeight;
  }

  /**
   * Heuristique utilisée pour l'algorithme A*.
   */
  private final Heuristic heuristic;

  /**
   * Facteur multiplicatif de la distance de Manhattan utilisé par l'heuristique K-Manhattan.
   */
  private final int kManhattan;

  public AStar(Heuristic heuristic) {
    this(heuristic, 1);
  }

  public AStar(Heuristic heuristic, int kManhattan) {
    this.heuristic = heuristic;
    this.kManhattan = kManhattan;
  }

  @Override
  public Result solve(GridGraph2D grid,
                      PositiveWeightFunction weights,
                      int source,
                      int destination,
                      VertexLabelling<Boolean> processed) {


    // Store the cost + heuristic
    PriorityQueue<Pair<Double, Integer>> pq = new PriorityQueue<>(
            Comparator.comparing(Pair::getKey));

    // Target coordinates for heuristic calculation
    int dest_x = destination % grid.width();
    int dest_y = destination / grid.width();

    // This stores the actual cost from source to each vertex
    double[] lambdas = new double[grid.nbVertices()];
    Arrays.fill(lambdas, POSITIVE_INFINITY);
    lambdas[source] = 0;

    // Track predecessors for path reconstruction
    int[] predecessors = new int[grid.nbVertices()];
    Arrays.fill(predecessors, -1);

    // Initial heuristic for source
    double sourceHeuristic = evaluate(
            Math.abs(source % grid.width() - dest_x),
            Math.abs(source / grid.width() - dest_y),
            weights.minWeight()
    );

    int numberTreated = 0;
    pq.add(new Pair<>(sourceHeuristic, source));

    // Main A* loop
    while (!pq.isEmpty()) {
      Pair<Double, Integer> current = pq.poll();
      int currentVertex = current.getValue();

      // Mark as processed
      processed.setLabel(currentVertex, true);
      ++numberTreated;

      // Check if destination reached
      if (currentVertex == destination) {
        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        int vertex = destination;

        while (vertex != source) {
          path.add(0, vertex);
          vertex = predecessors[vertex];
        }
        path.add(0, source);

        return new Result(path, path.size(), numberTreated);
      }

      // Explore neighbors
      for (int neighbor : grid.neighbors(currentVertex)) {
        // Skip processed vertices
        if (processed.getLabel(neighbor)) {
          continue;
        }

        double tentativeLambda = lambdas[currentVertex] + weights.get(currentVertex, neighbor);

        // If we found a better path to the neighbor
        if (tentativeLambda < lambdas[neighbor]) {
          // Update predecessor and lambda
          predecessors[neighbor] = currentVertex;
          lambdas[neighbor] = tentativeLambda;

          // Calculate heuristic value for this neighbor to destination
          double heuristic = evaluate(
                  Math.abs(neighbor % grid.width() - dest_x),
                  Math.abs(neighbor / grid.width() - dest_y),
                  weights.minWeight()
          );

          // Add to priority queue with predicted weights as priority (weights+heuristic)
          pq.add(new Pair<>(lambdas[neighbor] + heuristic, neighbor));
        }
      }
    }
    return new Result(Collections.emptyList(), 0, 0);
  }
}