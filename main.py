import numpy as np
import matplotlib.pyplot as plt


# ===== 1. Set up environment =====
ACTIONS = ['up', 'down', 'left', 'right']
DISCOUNT_FACTOR = 0.9
GRID_ROWS = 6
GRID_COLS = 6

# Set walls
walls = [(1, 1), (2, 1), (3, 1), (1, 5), (4, 4)]

# Set rewards
rewards = np.full((GRID_ROWS, GRID_COLS), -0.05) #set the base rewards for all squares

# Set special squares with their respective rewards
green_squares = [(0, 5), (2, 5), (3, 4), (4, 3), (5, 2), (5, 5)]
for row, col in green_squares:
    rewards[row, col] = 1

orange_squares = [(1, 4), (2, 3), (3, 2), (4, 1), (5, 4)]
for row, col in orange_squares:
    rewards[row, col] = -1


# ===== 2. Initialize transition model =====

# Function to calculate the next state given a current state and an action
def get_next_state(state, action):
    """
    Calculates the resulting state given a starting state and an action.
    Must check if the move hits a wall or a grid boundary[cite: 26].
    """
    row, col = state
    
    if action == 'up':
        if(row, col + 1) in walls or col + 1 >= GRID_COLS:
            return state
        next_state = (row, col + 1)
    elif action == 'down':
        if(row, col - 1) in walls or col - 1 < 0:
            return state
        next_state = (row, col - 1)
    elif action == 'left':
        if(row - 1, col) in walls or row - 1 < 0:
            return state
        next_state = (row - 1, col)
    elif action == 'right':
        if(row + 1, col) in walls or row + 1 >= GRID_ROWS:
            return state
        next_state = (row + 1, col)

    return next_state

# Returns a list of (probability, next_state) tuples based on the intended action
def get_transitions(state, action):
    # The intended outcome occurs with probability 0.8, or at a right angle with probability 0.1 each

    if(action == 'up'):
        intended_action = 'up'
        right_angle_1 = 'left'
        right_angle_2 = 'right'

    elif(action == 'down'):
        intended_action = 'down'
        right_angle_1 = 'left'
        right_angle_2 = 'right'

    elif(action == 'left'):
        intended_action = 'left'
        right_angle_1 = 'up'
        right_angle_2 = 'down'

    elif(action == 'right'):
        intended_action = 'right'
        right_angle_1 = 'up'
        right_angle_2 = 'down'
    

    transitions = [
        (0.8, get_next_state(state, intended_action)),
        (0.1, get_next_state(state, right_angle_1)),
        (0.1, get_next_state(state, right_angle_2))
    ]

    return transitions


# ===== 3. Value Iteration =====

# Value iteration algorithm to built the utility matrix
def value_iteration(epsilon=1e-4):
    # Initialize utilities to 0 for all states
    U = np.zeros((GRID_ROWS, GRID_COLS))
    U_history = [] # To store U at each iteration for plotting
    
    # Algorithm loops till convergence
    while True:
        U_history.append(U.copy())
        U_next = np.zeros((GRID_ROWS, GRID_COLS))
        delta = 0 # delta tracks the maximum change in utility for all states in this iteration
        
        for r in range(GRID_ROWS):
            for c in range(GRID_COLS):
                state = (r, c)
                
                # Skip walls as they have no utility and cannot be entered
                if state in walls:
                    continue
                
                # Calculate expected utility for all possible actions
                action_utilities = []
                for action in ACTIONS:
                    # same action can lead to multiple next states
                    for prob, next_state in get_transitions(state, action):
                        expected_u = sum(prob * U[next_state[0], next_state[1]])

                    action_utilities.append(expected_u)
                
                # Bellman Update
                U_next[r, c] = rewards[r, c] + DISCOUNT_FACTOR * max(action_utilities)
                
                # Track the maximum change to check for convergence
                delta = max(delta, abs(U_next[r, c] - U[r, c]))
        
        # Update utilities for the next iteration
        U = U_next
        
        # Convergence check 
        if delta < epsilon * (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR:
            U_history.append(U.copy())
            break
            
    return U, U_history


# ===== 4. Get optimal policy =====

def get_optimal_policy(U):

    # Initialize policy with empty strings, will fill in with best action for each state
    policy = np.full((GRID_ROWS, GRID_COLS), ' ', dtype=object)
    
    for r in range(GRID_ROWS):
        for c in range(GRID_COLS):
            state = (r, c)
            if state in walls:
                policy[r, c] = 'W' # Mark walls in the policy
                continue
                
            # Find the action that maximizes expected utility
            best_action = None
            max_u = -float('inf')
            
            for action in ACTIONS:
                for prob, next_state in get_transitions(state, action):
                    expected_u = sum(prob * U[next_state[0], next_state[1]])

                # Update best action if a better action is found
                if expected_u > max_u:
                    max_u = expected_u
                    best_action = action
                    
            policy[r, c] = best_action
            
    return policy