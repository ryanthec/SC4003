from environment import get_maze
from policy import value_iteration, get_optimal_policy, policy_iteration
from visualization import plot_utility_convergence, plot_policy_and_utilities

def run_experiment(task_name, start_state):
    print(f"\n{'='*50}")
    print(f" EXECUTING: {task_name.upper()}")
    print(f"{'='*50}")
    
    # 1. Instantiate the specific environment
    env = get_maze(task_name)
    
    # 2. Run Value Iteration
    print(f"\n[Value Iteration] - Solving {env.width}x{env.height} grid...")
    U_vi, U_hist_vi = value_iteration(env)
    policy_vi = get_optimal_policy(U_vi, env)
    
    print(f"  -> Converged in {len(U_hist_vi)-1} iterations.")
    plot_utility_convergence(U_hist_vi, env, start_state, filename=f"{task_name}_VI_convergence.png")
    plot_policy_and_utilities(U_vi, policy_vi, env, filename=f"{task_name}_VI_grid.png")
    
    # 3. Run Policy Iteration
    print(f"\n[Policy Iteration] - Solving {env.width}x{env.height} grid...")
    U_pi, policy_pi, U_hist_pi = policy_iteration(env)
    
    print(f"  -> Converged in {len(U_hist_pi)-1} iterations.")
    plot_utility_convergence(U_hist_pi, env, start_state, filename=f"{task_name}_PI_convergence.png")
    plot_policy_and_utilities(U_pi, policy_pi, env, filename=f"{task_name}_PI_grid.png")

if __name__ == "__main__":
    print("Initializing Intelligent Agents MDP Engine...")
    
    # Run Task 1 (Start state matches the assignment PDF)
    run_experiment('task1', start_state=(3, 4))
    
    # Run Task 2 (Using your custom bottom-left start state)
    run_experiment('task2', start_state=(1, 12))
    
    print("\nAll tasks complete! Check the 'output' folder for your report images.")