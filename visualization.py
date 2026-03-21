import os
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

# Ensure the output directory exists
OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def plot_utility_convergence(U_history, env, start_state, filename="utility_convergence.png"):
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


def plot_initial_grid(env, start_state, filename="initial_grid.png"):

    fig, ax = plt.subplots(figsize=(env.width * 1.2, env.height * 1.2))
    
    # Invert y-axis so (1,1) is at the top-left
    ax.set_ylim(env.height + 0.5, 0.5)
    ax.set_xlim(0.5, env.width + 0.5)
    
    colors = {
        'wall': '#424242',
        'green': '#66BB6A',
        'orange': '#FFA726',
        'normal': '#F5F5F5'
    }

    for x in range(1, env.width + 1):
        for y in range(1, env.height + 1):
            state = (x, y)
            
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

            # Determine text color for contrast
            text_color = 'white' if bg_color in [colors['wall'], colors['green'], colors['orange']] else 'black'
            
            if state in env.walls:
                ax.text(x, y, "WALL", ha='center', va='center', fontsize=10, color=text_color, fontweight='bold')
            else:
                # Print explicit rewards
                if env.rewards[state] > 0:
                    ax.text(x, y, "+1", ha='center', va='center', fontsize=12, color=text_color, fontweight='bold')
                elif env.rewards[state] < -0.05:
                    ax.text(x, y, "-1", ha='center', va='center', fontsize=12, color=text_color, fontweight='bold')
                
                # Print Start label
                if state == start_state:
                    # If the start state happens to share a cell with a +1 or -1, shift "Start" down slightly so they don't overlap
                    y_offset = 0.25 if (env.rewards[state] > 0 or env.rewards[state] < -0.05) else 0
                    ax.text(x, y + y_offset, "Start", ha='center', va='center', fontsize=10, color=text_color, fontweight='bold')

    ax.set_aspect('equal')
    ax.set_xticks(range(1, env.width + 1))
    ax.set_yticks(range(1, env.height + 1))
    ax.xaxis.tick_top()
    ax.tick_params(length=0) 
    
    plt.title(f"{env.name}: Initial Environment Layout", fontsize=16, fontweight='bold', pad=30)
    plt.tight_layout()
    
    filepath = os.path.join(OUTPUT_DIR, filename)
    plt.savefig(filepath, dpi=300, bbox_inches='tight')
    print(f"  -> Initial layout grid saved to: {filepath}")
    plt.close()


def plot_policy_and_utilities(U, policy, env, filename="policy_grid.png"):

    fig, ax = plt.subplots(figsize=(env.width * 1.2, env.height * 1.2))
    
    # Invert y-axis so (1,1) is at the top-left
    ax.set_ylim(env.height + 0.5, 0.5)
    ax.set_xlim(0.5, env.width + 0.5)
    
    # Visual configuration
    colors = {
        'wall': '#424242',
        'green': '#66BB6A',
        'orange': '#FFA726',
        'normal': '#F5F5F5'
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
                
                # Draw utility value at the bottom
                ax.text(x, y + 0.35, f"{U[state]:+.2f}", ha='center', va='center', fontsize=8, color=text_color, fontweight='bold')
                
                # Draw policy arrow
                policy_action_char = policy[state]
                char_map = {'U': 'up', 'D': 'down', 'L': 'left', 'R': 'right'}
                
                if policy_action_char in char_map:
                    full_action = char_map[policy_action_char]
                    dx, dy = arrow_map[full_action]
                    
                    # SHIFT FIX: Move the center of the arrow up slightly (y - 0.1) 
                    # so downward arrows don't crash into the utility numbers.
                    cy = y - 0.1
                    
                    ax.annotate('', 
                                xy=(x + dx, cy + dy), 
                                xytext=(x - dx*0.2, cy - dy*0.2), 
                                arrowprops=dict(arrowstyle="->", color=text_color, lw=2, mutation_scale=15))

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