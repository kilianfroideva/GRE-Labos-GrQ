package ch.heig.gre.groupP;

import ch.heig.gre.maze.MazeBuilder;
import ch.heig.gre.maze.MazeGenerator;

import java.util.*;

// TODO: renommer le package (Shift + F6) selon la lettre attribuée à votre groupe


public final class DfsGenerator implements MazeGenerator {
  /**
   * Performs a depth-first search traversal starting from a given vertex.
   * Builds a maze by removing walls between vertices as they are discovered.
   *
   * @param builder The maze builder containing topology and progression information
   * @param from The starting vertex for the DFS traversal
   */
  private void DFS_neigbors(MazeBuilder builder, int from) {
    int size = builder.topology().nbVertices();

    // Use int[] for discovered vertices
    // -2: not visited, -1: root node, other values: parent vertex
    int[] discovered = new int[size];

    // Initialize all vertices as not visited (-2)
    Arrays.fill(discovered, -2);

    // Create stack for DFS using array
    Stack<Integer> vertexStack = new Stack<>();

    // Push initial vertex
    vertexStack.push(from);
    discovered[from] = -1; // -1 indicates root node (no parent)

    while (!vertexStack.isEmpty()) {
      int current = vertexStack.pop();

      // Get parent from the discovered array
      int parent = discovered[current];

      // Remove wall between current and parent (if not the root)
      if (parent != -1) {
        builder.removeWall(current, parent);
      }

      // Shuffle the neighbors to walk randomly in the labyrinth
      List<Integer> neighbors = builder.topology().neighbors(current);
      Collections.shuffle(neighbors);

      // For each neighbor being not discovered, push into the stack
      for (Integer neighbor : neighbors) {
        // Check if not visited (-2)
        if (discovered[neighbor] == -2) {
          vertexStack.push(neighbor);
          discovered[neighbor] = current; // Store current as the parent of neighbor
        }
      }
    }
  }


  @Override
  public void generate(MazeBuilder builder, int from) {
    DFS_neigbors(builder, from);
  }

  @Override
  public boolean requireWalls() {
    // Walls everywhere as default
    return true;
  }
}
