# environment.py
GRID_WIDTH = 6  # x-axis (left to right)
GRID_HEIGHT = 6 # y-axis (top to bottom)
DISCOUNT_FACTOR = 0.99 
ACTIONS = ['up', 'down', 'left', 'right']

# (x, y) coordinates with (1, 1) at top-left
WALLS = [(2, 1), (5, 2), (2, 5), (3, 5), (4, 5)]

def get_rewards():
    # Initialize dictionary with base rewards
    rewards = {}
    for x in range(1, GRID_WIDTH + 1):
        for y in range(1, GRID_HEIGHT + 1):
            rewards[(x, y)] = -0.05
            
    # Top-to-bottom visual mapping
    green_squares = [(1, 1), (3, 1), (6, 1), (4, 2), (5, 3), (6, 4)]
    for state in green_squares:
        rewards[state] = 1.0
        
    orange_squares = [(2, 2), (6, 2), (3, 3), (4, 4), (5, 5)]
    for state in orange_squares:
        rewards[state] = -1.0
        
    return rewards

REWARDS = get_rewards()