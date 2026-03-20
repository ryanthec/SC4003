from environment import GRID_WIDTH, GRID_HEIGHT
from policy import value_iteration, get_optimal_policy, policy_iteration
from visualization import plot_utility_convergence, plot_policy_and_utilities

if __name__ == "__main__":
    # ==========================================
    # 1. VALUE ITERATION
    # ==========================================
    print("--- Running Value Iteration ---")
    U_vi, U_history_vi = value_iteration()
    policy_vi = get_optimal_policy(U_vi)
    
    plot_utility_convergence(U_history_vi, filename="VI_convergence.png")
    plot_policy_and_utilities(U_vi, policy_vi, filename="VI_grid.png")
    print(f"Value Iteration converged in {len(U_history_vi) - 1} iterations.\n")

    # ==========================================
    # 2. POLICY ITERATION
    # ==========================================
    print("--- Running Policy Iteration ---")
    U_pi, policy_pi, U_history_pi = policy_iteration()
    
    plot_utility_convergence(U_history_pi, filename="PI_convergence.png")
    plot_policy_and_utilities(U_pi, policy_pi, filename="PI_grid.png")
    print(f"Policy Iteration converged in {len(U_history_pi) - 1} iterations.\n")
    
    print("Process complete! Check the 'output' folder for your report-ready images.")