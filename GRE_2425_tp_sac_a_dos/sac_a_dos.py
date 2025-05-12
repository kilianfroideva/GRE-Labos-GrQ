import math
import random
import numpy as np
from time import time

def exact_solver(values, weights, max_weight):
    n = len(values)

    G = np.zeros(max_weight + 1)
    decisions = np.zeros((n + 1, max_weight + 1))

    for k in range(1, n + 1):
        for y in range(max_weight, weights[k-1] - 1, -1):
            if G[y] < G[y - weights[k - 1]] + values[k - 1]:
                G[y] = G[y - weights[k - 1]] + values[k - 1]
                decisions[k, y] = 1
            else:
                decisions[k, y] = 0
    max_value = np.max(G)
    max_weight_index = np.argmax(G)
    selected_items = []
    for k in range(n, 0, -1):
        if decisions[k, max_weight_index] == 1:
            selected_items.append(k - 1)
            max_weight_index -= weights[k - 1]
    selected_items.reverse()
    return max_value, selected_items

def heuristic_solver(values, weights, max_weight):
    n = len(values)
    items = list(range(n))
    items.sort(key=lambda i: values[i] / weights[i], reverse=True)

    max_value = 0
    selected_items = []
    current_weight = 0

    for i in items:
        if current_weight + weights[i] <= max_weight:
            selected_items.append(i)
            current_weight += weights[i]
            max_value += values[i]

    return max_value, selected_items

def experiment_once(N = 500, p_min=100, p_max=1000, q_ratio=0.25, U = 100, seed=None, verbose=False):
    if seed is not None:
        np.random.seed(seed)
    else:
        np.random.seed(0)

    if U is None:
        U = random.randint(1, 1000) # random perturbation


    weights = np.random.randint(p_min, p_max, N)
    values = weights + np.random.uniform(-U, U, N)

    max_weight = math.ceil(q_ratio * N * (p_min + p_max) / 2)

    t0 = time()
    max_value_exact, selected_items_exact = exact_solver(values, weights, max_weight)
    exact_solver_time = time() - t0 

    t0 = time()
    max_value_heuristic, selected_items_heuristic = heuristic_solver(values, weights, max_weight)
    heuristic_solver_time = time() - t0

    time_gain = 100.0 - 100*heuristic_solver_time / exact_solver_time
    value_loss = 100.0 - 100*max_value_heuristic / max_value_exact

    if verbose:
        print("Exact Solver:")
        print("Time taken:", exact_solver_time, "seconds")
        print("Max Value:", max_value_exact)
        print("Selected Items:", selected_items_exact)

        print("\nHeuristic Solver:")
        print("Time taken:", heuristic_solver_time, "seconds")
        print("Max Value:", max_value_heuristic)
        print("Selected Items:", selected_items_heuristic)

        print("\nComparison:")
        print("Relative time Gain from Heuristic Solver:", time_gain, "%")
        print("Relative value loss from Heuristic Solver:", value_loss, "%")
    return max_value_exact, max_value_heuristic, exact_solver_time, heuristic_solver_time, time_gain, value_loss


def experiment(N=500, p_min=100, p_max=1000, q_ratio=0.25, U = 100, num_trials=10, verbose=False):
    results = []
    for i in range(num_trials):
        result = experiment_once(N, p_min, p_max, q_ratio, U, seed=i)
        results.append(result)

    # Convert to numpy array for calculating statistics
    # Note that we've removed selected_items from the return value of experiment_once
    results_array = np.array(results)
    avg_results = np.mean(results_array, axis=0)
    std_results = np.std(results_array, axis=0)

    if verbose:
        print("Average Results over", num_trials, "trials:")
        print("Max Value Exact:", avg_results[0], "±", std_results[0])
        print("Max Value Heuristic:", avg_results[1], "±", std_results[1])
        print("Exact Solver Time:", avg_results[2], "±", std_results[2])
        print("Heuristic Solver Time:", avg_results[3], "±", std_results[3])
        print("Time Gain from Heuristic Solver:", avg_results[4], "% ±", std_results[4])
        print("Value Loss from Heuristic Solver:", avg_results[5], "% ±", std_results[5])

if __name__ == "__main__":
    experiment(verbose=True)
