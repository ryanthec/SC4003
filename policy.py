from transition import get_transitions


# 1. VALUE ITERATION
def value_iteration(env, epsilon=1e-6):
    # Initialize U as a dictionary for 1-based indexing
    U = { (x, y): 0.0 for x in range(1, env.width + 1) for y in range(1, env.height + 1) }
    U_history = []
    
    while True:
        U_history.append(U.copy())
        U_next = { (x, y): 0.0 for x in range(1, env.width + 1) for y in range(1, env.height + 1) }
        delta = 0 
        
        for x in range(1, env.width + 1):
            for y in range(1, env.height + 1):
                state = (x, y)
                
                # Skip walls
                if state in env.walls:
                    continue
                
                # Calculate expected utility for all possible actions
                action_utilities = []
                for action in env.actions:
                    expected_u = 0.0
                    for prob, next_state in get_transitions(state, action, env):
                        expected_u += prob * U[next_state]
                    action_utilities.append(expected_u)
                
                # Bellman Update
                U_next[state] = env.rewards[state] + env.discount_factor * max(action_utilities)
                
                # Track the maximum change to check for convergence
                delta = max(delta, abs(U_next[state] - U[state]))
        
        # Update utilities for the next iteration
        U = U_next
        
        # Convergence check
        if delta < epsilon * (1 - env.discount_factor) / env.discount_factor:
            U_history.append(U.copy())
            break
            
    return U, U_history

def get_optimal_policy(U, env):

    policy = { (x, y): ' ' for x in range(1, env.width + 1) for y in range(1, env.height + 1) }
    
    for x in range(1, env.width + 1):
        for y in range(1, env.height + 1):
            state = (x, y)
            if state in env.walls:
                policy[state] = 'W' 
                continue
                
            best_action = None
            max_u = -float('inf')
            
            for action in env.actions:
                expected_u = 0.0
                for prob, next_state in get_transitions(state, action, env):
                    expected_u += prob * U[next_state]
                    
                if expected_u > max_u:
                    max_u = expected_u
                    best_action = action
                    
            # Taking the first letter (U/D/L/R) for cleaner grid printing and plotting
            policy[state] = best_action[0].upper()
            
    return policy



# 2. POLICY ITERATION
def policy_evaluation(policy, U, env, epsilon=1e-6):

    U_eval = U.copy()
    
    while True:
        U_next = U_eval.copy()
        delta = 0
        
        for x in range(1, env.width + 1):
            for y in range(1, env.height + 1):
                state = (x, y)
                if state in env.walls:
                    continue
                
                action = policy[state]
                expected_u = 0.0
                for prob, next_state in get_transitions(state, action, env):
                    expected_u += prob * U_eval[next_state]
                    
                # Bellman update for a fixed policy (no 'max' operation)
                U_next[state] = env.rewards[state] + env.discount_factor * expected_u
                delta = max(delta, abs(U_next[state] - U_eval[state]))
                
        U_eval = U_next
        
        # Convergence check for the evaluation phase
        if delta < epsilon * (1 - env.discount_factor) / env.discount_factor:
            break
            
    return U_eval

def policy_iteration(env):                                                                                                  

    # 1. Initialization
    U = { (x, y): 0.0 for x in range(1, env.width + 1) for y in range(1, env.height + 1) }
    
    # Start with an arbitrary policy (e.g., all 'up' arrows) and mark walls with 'W'
    policy = { (x, y): 'up' for x in range(1, env.width + 1) for y in range(1, env.height + 1) }
    for w in env.walls:
        policy[w] = 'W'

    U_history = [U.copy()]
    
    while True:
        # 2. Policy Evaluation
        U = policy_evaluation(policy, U, env)
        U_history.append(U.copy())
        
        # 3. Policy Improvement
        unchanged = True
        
        for x in range(1, env.width + 1):
            for y in range(1, env.height + 1):
                state = (x, y)
                if state in env.walls:
                    continue
                
                # Calculate expected utility of our CURRENT action
                current_action = policy[state]
                current_u = 0.0
                for prob, next_state in get_transitions(state, current_action, env):
                    current_u += prob * U[next_state]
                    
                # Check if any OTHER action gives a higher utility
                best_action = current_action
                max_u = current_u
                
                for action in env.actions:
                    expected_u = 0.0
                    for prob, next_state in get_transitions(state, action, env):
                        expected_u += prob * U[next_state]
                        
                    # Add a tiny epsilon (1e-8) to prevent infinite loops
                    if expected_u > max_u + 1e-8: 
                        max_u = expected_u
                        best_action = action
                        
                # If we found a better action, update the policy and flag that a change occurred
                if best_action != current_action:
                    policy[state] = best_action
                    unchanged = False
                    
        # If we made it through the whole board without changing a single arrow, we are done
        if unchanged:
            break
            
    # Convert full action names ('up', 'down') back to single letters ('U', 'D') for the visualizer
    clean_policy = {s: (policy[s][0].upper() if policy[s] != 'W' else 'W') for s in policy}
            
    return U, clean_policy, U_history