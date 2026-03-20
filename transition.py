# transition.py
from environment import GRID_WIDTH, GRID_HEIGHT, WALLS

def get_next_state(state, action):
    x, y = state
    
    # (1,1) is top-left, so 'up' decreases y and 'down' increases y
    if action == 'up':
        next_state = (x, y - 1)
    elif action == 'down':
        next_state = (x, y + 1)
    elif action == 'left':
        next_state = (x - 1, y)
    elif action == 'right':
        next_state = (x + 1, y)
    else:
        return state

    # Check 1-based grid boundaries
    if next_state[0] < 1 or next_state[0] > GRID_WIDTH or \
       next_state[1] < 1 or next_state[1] > GRID_HEIGHT:
        return state
        
    # Check if the move makes the agent walk into a wall
    if next_state in WALLS:
        return state
        
    return next_state

def get_transitions(state, action):
    right_angles = {
        'up': ('left', 'right'),
        'down': ('left', 'right'),
        'left': ('up', 'down'),
        'right': ('up', 'down')
    }
    
    right_angle_1, right_angle_2 = right_angles[action]
    
    return [
        (0.8, get_next_state(state, action)),
        (0.1, get_next_state(state, right_angle_1)),
        (0.1, get_next_state(state, right_angle_2))
    ]