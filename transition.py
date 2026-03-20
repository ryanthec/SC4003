def get_next_state(state, action, env):
    x, y = state
    
    if action == 'up': next_state = (x, y - 1)
    elif action == 'down': next_state = (x, y + 1)
    elif action == 'left': next_state = (x - 1, y)
    elif action == 'right': next_state = (x + 1, y)
    else: return state

    # Check boundaries and walls dynamically using the env object
    if next_state[0] < 1 or next_state[0] > env.width or \
       next_state[1] < 1 or next_state[1] > env.height:
        return state
        
    if next_state in env.walls:
        return state
        
    return next_state

def get_transitions(state, action, env):
    right_angles = {
        'up': ('left', 'right'), 'down': ('left', 'right'),
        'left': ('up', 'down'), 'right': ('up', 'down')
    }
    right_1, right_2 = right_angles[action]
    
    return [
        (0.8, get_next_state(state, action, env)),
        (0.1, get_next_state(state, right_1, env)),
        (0.1, get_next_state(state, right_2, env))
    ]