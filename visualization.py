import os
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from environment import GRID_WIDTH, GRID_HEIGHT, WALLS, REWARDS

# Ensure the output directory exists
OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def plot_utility_convergence(U_history, filename="utility_convergence.png"):
    """
    Plots the utility estimates of all states over the number of iterations.
    Highlights the Start state (3, 4) for clarity.
    """
    iterations = range(len(U_history))
    plt.figure(figsize=(10, 6))
    
    start_state = (3, 4)
    
    # Iterate through all possible states
    for x in range(1, GRID_WIDTH + 1):
        for y in range(1, GRID_HEIGHT + 1):
            state = (x, y)
            
            # Skip walls as they do not have utility values
            if state in WALLS:
                continue
                
            # Extract the utility history for this specific state
            state_utilities = [U_dict[state] for U_dict in U_history]
            
            # Highlight the Start state, fade the rest
            if state == start_state:
                plt.plot(iterations, state_utilities, color='#E53935', linewidth=2.5, label='Start State (3, 4)')
            else:
                plt.plot(iterations, state_utilities, color='gray', alpha=0.3, linewidth=1)
                
    plt.title("Utility Estimates vs. Number of Iterations", fontsize=14, fontweight='bold', pad=15)
    plt.xlabel("Number of Iterations", fontsize=12)
    plt.ylabel("Utility Estimate", fontsize=12)
    plt.grid(True, linestyle='--', alpha=0.7)
    plt.legend()
    
    # Save the plot
    plt.tight_layout()
    filepath = os.path.join(OUTPUT_DIR, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Convergence plot saved to: {filepath}")
    plt.close()

def plot_policy_and_utilities(U, policy, filename="policy_grid.png"):
    """
    Generates a high-quality visual grid showing cell types, utilities, and policy arrows.
    """
    fig, ax = plt.subplots(figsize=(GRID_WIDTH * 1.2, GRID_HEIGHT * 1.2))
    
    # Invert y-axis so (1,1) is at the top-left
    ax.set_ylim(GRID_HEIGHT + 0.5, 0.5)
    ax.set_xlim(0.5, GRID_WIDTH + 0.5)
    
    # Visual configuration
    colors = {
        'wall': '#424242',     # Dark Gray
        'green': '#66BB6A',    # Soft Green
        'orange': '#FFA726',   # Soft Orange
        'normal': '#F5F5F5'    # Off-White
    }
    
    # Arrow vectors based on our (x, y) top-left system
    arrow_map = {
        'up': (0, -0.35),
        'down': (0, 0.35),
        'left': (-0.35, 0),
        'right': (0.35, 0)
    }

    for x in range(1, GRID_WIDTH + 1):
        for y in range(1, GRID_HEIGHT + 1):
            state = (x, y)
            
            # Determine cell color
            if state in WALLS:
                bg_color = colors['wall']
            elif REWARDS[state] > 0:
                bg_color = colors['green']
            elif REWARDS[state] < -0.05: # Less than base reward
                bg_color = colors['orange']
            else:
                bg_color = colors['normal']

            # Draw the cell
            rect = mpatches.Rectangle((x - 0.5, y - 0.5), 1, 1, facecolor=bg_color, edgecolor='#BDBDBD', linewidth=1)
            ax.add_patch(rect)

            # Add utility text and policy arrows for non-wall states
            if state not in WALLS:
                # Decide text color based on background for contrast
                text_color = 'white' if bg_color in [colors['green'], colors['orange']] else 'black'
                
                # Draw utility value at the bottom of the cell
                ax.text(x, y + 0.35, f"{U[state]:+.2f}", ha='center', va='center', fontsize=9, color=text_color, fontweight='bold')
                
                # Draw policy arrow in the center of the cell
                # Because policy dictionary holds "U", "D", "L", "R", we map those back to full names
                policy_action_char = policy[state]
                char_map = {'U': 'up', 'D': 'down', 'L': 'left', 'R': 'right'}
                
                if policy_action_char in char_map:
                    full_action = char_map[policy_action_char]
                    dx, dy = arrow_map[full_action]
                    
                    # Draw a crisp arrow starting from center of cell pointing in dx, dy direction
                    ax.annotate('', 
                                xy=(x + dx, y + dy), 
                                xytext=(x - dx*0.2, y - dy*0.2), # Start slightly offset from center
                                arrowprops=dict(arrowstyle="->", color=text_color, lw=2, mutation_scale=15))

    # Clean up axes for a polished look
    ax.set_aspect('equal')
    ax.set_xticks(range(1, GRID_WIDTH + 1))
    ax.set_yticks(range(1, GRID_HEIGHT + 1))
    ax.xaxis.tick_top() # Move x-axis labels to the top to match our visual model
    ax.tick_params(length=0) # Hide tick marks
    
    plt.title("Optimal Policy & State Utilities", fontsize=16, fontweight='bold', pad=30)
    plt.tight_layout()
    
    # Save the visual grid
    filepath = os.path.join(OUTPUT_DIR, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"Grid visual saved to: {filepath}")
    plt.close()