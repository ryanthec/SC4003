class MazeEnv:
    def __init__(self, name, width, height, walls, green_squares, orange_squares):
        self.name = name
        self.width = width
        self.height = height
        self.walls = walls
        self.discount_factor = 0.99
        self.actions = ['up', 'down', 'left', 'right']
        self.rewards = self._build_rewards(green_squares, orange_squares)

    def _build_rewards(self, green, orange):
        rewards = {}
        for x in range(1, self.width + 1):
            for y in range(1, self.height + 1):
                rewards[(x, y)] = -0.05
        for state in green: rewards[state] = 1.0
        for state in orange: rewards[state] = -1.0
        return rewards

def get_maze(task_name):
    """Factory function to build and return the requested maze."""
    if task_name == 'task1':
        return MazeEnv(
            name="Task 1 (6x6)",
            width=6, height=6,
            walls=[(2, 1), (5, 2), (2, 5), (3, 5), (4, 5)],
            green_squares=[(1, 1), (3, 1), (6, 1), (4, 2), (5, 3), (6, 4)],
            orange_squares=[(2, 2), (6, 2), (3, 3), (4, 4), (5, 5)]
        )
        
    elif task_name == 'task2':
        return MazeEnv(
            name="Task 2 (12x12)",
            width=12, height=12,
            walls=[
                (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (3, 10), 
                (4, 4), (4, 5), (4, 6), (4, 10), (5, 4), (5, 10), (6, 4), (6, 10), 
                (7, 4), (7, 10), (8, 4), (8, 7), (8, 8), (8, 9), (8, 10), (9, 4), 
                (9, 7), (10, 10), (11, 7), (11, 8), (11, 9), (11, 10)
            ],
            green_squares=[
                (1, 1), (4, 12), (5, 2), (5, 3), (5, 7), (5, 8), (5, 12), 
                (6, 2), (6, 3), (6, 7), (6, 8), (11, 2), (11, 3)
            ],
            orange_squares=[
                (1, 2), (2, 1), (2, 9), (2, 10), (4, 2), (4, 8), (7, 3), 
                (7, 7), (9, 5), (9, 6), (9, 10), (10, 7)
            ]
        )
    else:
        raise ValueError(f"Unknown maze task: {task_name}")