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
  private void dfsNeighbors(MazeBuilder builder, int from) {
    int size = builder.topology().nbVertices();

    // Track visited vertices
    boolean[] visited = new boolean[size];

    // Use a simple array-based stack for better performance
    int[] stack = new int[size]; // Maximum stack size would be the number of vertices
    int stackTop = 0;

    // Start DFS from the given vertex
    stack[stackTop++] = from;
    visited[from] = true;

    while (stackTop > 0) {
      int current = stack[stackTop - 1]; // Peek the top element

      // Get all neighbors
      List<Integer> neighbors = builder.topology().neighbors(current);
      Collections.shuffle(neighbors); // Randomize neighbors

      // Find an unvisited neighbor
      boolean foundUnvisited = false;
      for (int neighbor : neighbors) {
        if (!visited[neighbor]) {
          // Remove wall between current and this unvisited neighbor
          builder.removeWall(current, neighbor);

          // Mark neighbor as visited and add to stack
          visited[neighbor] = true;
          stack[stackTop++] = neighbor;

          foundUnvisited = true;
          break; // Process only one unvisited neighbor at a time
        }
      }

      // If no unvisited neighbors, backtrack
      if (!foundUnvisited) {
        stackTop--; // Pop the current vertex
      }
    }
  }

  @Override
  public void generate(MazeBuilder builder, int from) {
    dfsNeighbors(builder, from);
  }

  @Override
  public boolean requireWalls() {
    // Walls everywhere as default
    return true;
  }
}