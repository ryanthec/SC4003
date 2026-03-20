# solvers.py
from environment import GRID_WIDTH, GRID_HEIGHT, ACTIONS, WALLS, REWARDS, DISCOUNT_FACTOR
from transition import get_transitions

def value_iteration(epsilon=1e-4):
    # Initialize U as a dictionary for 1-based indexing
    U = { (x, y): 0.0 for x in range(1, GRID_WIDTH + 1) for y in range(1, GRID_HEIGHT + 1) }
    U_history = []
    
    while True:
        U_history.append(U.copy())
        U_next = { (x, y): 0.0 for x in range(1, GRID_WIDTH + 1) for y in range(1, GRID_HEIGHT + 1) }
        delta = 0 
        
        for x in range(1, GRID_WIDTH + 1):
            for y in range(1, GRID_HEIGHT + 1):
                state = (x, y)
                
                if state in WALLS:
                    continue
                
                action_utilities = []
                for action in ACTIONS:
                    expected_u = 0.0
                    for prob, next_state in get_transitions(state, action):
                        expected_u += prob * U[next_state]
                    action_utilities.append(expected_u)
                
                U_next[state] = REWARDS[state] + DISCOUNT_FACTOR * max(action_utilities)
                delta = max(delta, abs(U_next[state] - U[state]))
        
        U = U_next
        
        # Convergence check [cite: 31]
        if delta < epsilon * (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR:
            U_history.append(U.copy())
            break
            
    return U, U_history


def policy_evaluation(policy, U, epsilon=1e-4):
    """
    Calculates the utilities of a given policy.
    We use iterative policy evaluation here to match the Value Iteration style.
    """
    U_eval = U.copy()
    
    while True:
        U_next = U_eval.copy()
        delta = 0
        
        for x in range(1, GRID_WIDTH + 1):
            for y in range(1, GRID_HEIGHT + 1):
                state = (x, y)
                if state in WALLS:
                    continue
                
                action = policy[state]
                expected_u = 0.0
                for prob, next_state in get_transitions(state, action):
                    expected_u += prob * U_eval[next_state]
                    
                # The Bellman update for a fixed policy (no 'max' operation)
                U_next[state] = REWARDS[state] + DISCOUNT_FACTOR * expected_u
                delta = max(delta, abs(U_next[state] - U_eval[state]))
                
        U_eval = U_next
        
        # Convergence check for the evaluation phase
        if delta < epsilon * (1 - DISCOUNT_FACTOR) / DISCOUNT_FACTOR:
            break
            
    return U_eval

def policy_iteration():
    """
    Executes the Policy Iteration algorithm.
    Returns the final utilities, the optimal policy, and the utility history for plotting.
    """
    # 1. Initialization
    U = { (x, y): 0.0 for x in range(1, GRID_WIDTH + 1) for y in range(1, GRID_HEIGHT + 1) }
    
    # Start with a completely arbitrary policy (e.g., everyone goes 'up')
    policy = { (x, y): 'up' for x in range(1, GRID_WIDTH + 1) for y in range(1, GRID_HEIGHT + 1) }
    for w in WALLS:
        policy[w] = 'W'

    U_history = [U.copy()]
    
    while True:
        # 2. Policy Evaluation
        U = policy_evaluation(policy, U)
        U_history.append(U.copy())
        
        # 3. Policy Improvement
        unchanged = True
        
        for x in range(1, GRID_WIDTH + 1):
            for y in range(1, GRID_HEIGHT + 1):
                state = (x, y)
                if state in WALLS:
                    continue
                
                # Calculate expected utility of our CURRENT action
                current_action = policy[state]
                current_u = 0.0
                for prob, next_state in get_transitions(state, current_action):
                    current_u += prob * U[next_state]
                    
                # Check if any OTHER action gives a higher utility
                best_action = current_action
                max_u = current_u
                
                for action in ACTIONS:
                    expected_u = 0.0
                    for prob, next_state in get_transitions(state, action):
                        expected_u += prob * U[next_state]
                        
                    # We use + 1e-8 to prevent infinite flip-flopping due to floating point precision
                    if expected_u > max_u + 1e-8: 
                        max_u = expected_u
                        best_action = action
                        
                # If we found a better action, update the policy and flag that a change occurred
                if best_action != current_action:
                    policy[state] = best_action
                    unchanged = False
                    
        # If we made it through the whole board without changing a single arrow, we are done!
        if unchanged:
            break
            
    # Convert full action names ('up', 'down') back to single letters ('U', 'D') for our visualizer
    clean_policy = {s: (policy[s][0].upper() if policy[s] != 'W' else 'W') for s in policy}
            
    return U, clean_policy, U_history



def get_optimal_policy(U):
    policy = { (x, y): ' ' for x in range(1, GRID_WIDTH + 1) for y in range(1, GRID_HEIGHT + 1) }
    
    for x in range(1, GRID_WIDTH + 1):
        for y in range(1, GRID_HEIGHT + 1):
            state = (x, y)
            if state in WALLS:
                policy[state] = 'W' 
                continue
                
            best_action = None
            max_u = -float('inf')
            
            for action in ACTIONS:
                expected_u = 0.0
                for prob, next_state in get_transitions(state, action):
                    expected_u += prob * U[next_state]
                    
                if expected_u > max_u:
                    max_u = expected_u
                    best_action = action
                    
            policy[state] = best_action[0].upper()
            
    return policy