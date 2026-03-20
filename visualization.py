import os
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

# Ensure the output directory exists
OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def plot_utility_convergence(U_history, env, start_state, filename="utility_convergence.png"):
    """
    Plots the utility estimates of all states over the number of iterations.
    Highlights the specific start_state for clarity.
    """
    iterations = range(len(U_history))
    plt.figure(figsize=(10, 6))
    
    # Iterate through all possible states
    for x in range(1, env.width + 1):
        for y in range(1, env.height + 1):
            state = (x, y)
            
            # Skip walls as they do not have utility values
            if state in env.walls:
                continue
                
            # Extract the utility history for this specific state
            state_utilities = [U_dict[state] for U_dict in U_history]
            
            # Highlight the Start state, fade the rest
            if state == start_state:
                plt.plot(iterations, state_utilities, color='#E53935', linewidth=2.5, label=f'Start State {start_state}')
            else:
                plt.plot(iterations, state_utilities, color='gray', alpha=0.3, linewidth=1)
                
    plt.title(f"{env.name}: Utility Estimates vs. Iterations", fontsize=14, fontweight='bold', pad=15)
    plt.xlabel("Number of Iterations", fontsize=12)
    plt.ylabel("Utility Estimate", fontsize=12)
    plt.grid(True, linestyle='--', alpha=0.7)
    
    # Only show legend if the start state exists in this maze
    if start_state not in env.walls:
        plt.legend()
        
    plt.tight_layout()
    filepath = os.path.join(OUTPUT_DIR, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"  -> Convergence plot saved to: {filepath}")
    plt.close()

def plot_policy_and_utilities(U, policy, env, filename="policy_grid.png"):
    """
    Generates a high-quality visual grid showing cell types, utilities, and policy arrows.
    """
    fig, ax = plt.subplots(figsize=(env.width * 1.2, env.height * 1.2))
    
    # Invert y-axis so (1,1) is at the top-left
    ax.set_ylim(env.height + 0.5, 0.5)
    ax.set_xlim(0.5, env.width + 0.5)
    
    # Visual configuration
    colors = {
        'wall': '#424242',     # Dark Gray
        'green': '#66BB6A',    # Soft Green
        'orange': '#FFA726',   # Soft Orange
        'normal': '#F5F5F5'    # Off-White
    }
    
    # Arrow vectors
    arrow_map = {
        'up': (0, -0.35),
        'down': (0, 0.35),
        'left': (-0.35, 0),
        'right': (0.35, 0)
    }

    for x in range(1, env.width + 1):
        for y in range(1, env.height + 1):
            state = (x, y)
            
            # Determine cell color based on the environment's rewards
            if state in env.walls:
                bg_color = colors['wall']
            elif env.rewards[state] > 0:
                bg_color = colors['green']
            elif env.rewards[state] < -0.05: 
                bg_color = colors['orange']
            else:
                bg_color = colors['normal']

            # Draw the cell
            rect = mpatches.Rectangle((x - 0.5, y - 0.5), 1, 1, facecolor=bg_color, edgecolor='#BDBDBD', linewidth=1)
            ax.add_patch(rect)

            # Add utility text and policy arrows for non-wall states
            if state not in env.walls:
                text_color = 'white' if bg_color in [colors['green'], colors['orange']] else 'black'
                
                # Draw utility value
                ax.text(x, y + 0.35, f"{U[state]:+.2f}", ha='center', va='center', fontsize=8, color=text_color, fontweight='bold')
                
                # Draw policy arrow
                policy_action_char = policy[state]
                char_map = {'U': 'up', 'D': 'down', 'L': 'left', 'R': 'right'}
                
                if policy_action_char in char_map:
                    full_action = char_map[policy_action_char]
                    dx, dy = arrow_map[full_action]
                    
                    ax.annotate('', 
                                xy=(x + dx, y + dy), 
                                xytext=(x - dx*0.2, y - dy*0.2), 
                                arrowprops=dict(arrowstyle="->", color=text_color, lw=2, mutation_scale=15))

    # Clean up axes
    ax.set_aspect('equal')
    ax.set_xticks(range(1, env.width + 1))
    ax.set_yticks(range(1, env.height + 1))
    ax.xaxis.tick_top()
    ax.tick_params(length=0) 
    
    plt.title(f"{env.name}: Optimal Policy & Utilities", fontsize=16, fontweight='bold', pad=30)
    plt.tight_layout()
    
    filepath = os.path.join(OUTPUT_DIR, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"  -> Grid visual saved to: {filepath}")
    plt.close()