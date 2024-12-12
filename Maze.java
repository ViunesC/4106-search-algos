import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * Shitty-written Maze class for comparing different search algorithm
 * @author Viunec
 * For self-entertainment only
 * December 10th, 2024
 */
public class Maze {
    private static class Tuple {
        double priority;
        Cord cord;

        private Tuple(double priority, Cord cord) {
            this.priority = priority;
            this.cord = cord;
        }

        private int compareTo(Tuple other) {
            return Double.compare(this.priority, other.priority);
        }
    }

    private static class Cord {
        private int x;
        private int y;
        private Cord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class MyPriorityQueue {
        private List<Tuple> queue;

        private MyPriorityQueue() {
            this.queue = new ArrayList<>();
        }

        private int size() { return queue.size();}

        private boolean isEmpty() { return queue.isEmpty(); }

        private void enqueue (double priority, Cord item) {
            queue.add(new Tuple(priority,item));
            onUpdate();
        }

        private Cord dequeue() {
            if (!isEmpty())
                return queue.remove(0).cord;
            else
                throw new ArrayIndexOutOfBoundsException("Empty list");
        }

        private void onUpdate() {
            queue.sort(Tuple::compareTo);
        }
    }

    private int width;
    private int height;
    private static Cord start_tile;
    private static Cord end_tile;
    public int[][] map;


    private Maze(int width, int height, int[][] map) {
        this.width = width;
        this.height = height;
        this.map = map;

        start_tile = new Cord(-1,-1);
        end_tile = new Cord(-1,-1);
        for (int r=0;r<map.length;++r)
            for (int c=0;c<map[r].length;++c) {
                if (map[r][c] == 2)
                    updateEnd(r,c);
            }
    }

    /**
     * Construct Maze from given text file
     * @param filename name of input file, with following format:
     *                 1. First line: width of maze(integer),height of maze(integer)
     *                 2. Second to n line: content of maze (without wall), 0 represent a empty tile,
     *                      1 for blocker (barricade) tile, 2 for goal tile (one per maze)
     *                 3. an "/", escape character, signifying end-of-file
     * @return An instance of Maze, properly initialized and ready to be tested
     */
    public static Maze createMaze(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            String[] args = reader.readLine().split(",");
            int width = Integer.parseInt(args[0]);
            int height = Integer.parseInt(args[1]);

            int[][] map = new int[height][width];
            int i = 0;
            String[] nextTokens;
            while (reader.ready()) {
                nextTokens = reader.readLine().split("");
                for (int c=0;c<width;++c) {
                    if (nextTokens[0].equals("/"))
                        break;

                    map[i][c] = Integer.parseInt(nextTokens[c]);
                }
                ++i;
            }

            reader.close();
            return new Maze(width,height,map);
        } catch (Exception e) {
            System.out.println("There was an error when initializing maze:");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Overwritten Java's built-in toString() method, for our custom Maze object
     * @return string representation of the Maze
     */
    public String toString() {
        StringBuilder output = new StringBuilder();

        // Top wall
        for (int i=0;i<width+2;++i) {
            output.append("x");
        }
        output.append("\n");

        for (int[] arr:map) {
            output.append("x");
            for (int token:arr) {
                if (token == 1)
                    output.append("x");
                else if (token == 0)
                    output.append(" ");
                else
                    output.append("o");
            }
            output.append("x\n");
        }

        // Bottom wall
        for (int i=0;i<width+2;++i) {
            output.append("x");
        }

        return output.toString();
    }

    /**
     * Generate string representation of state within the search, indicate the current tile being examined
     * @param current Cord object for current tile (being examined by algorithm)
     * @return string representation of the Maze
     */
    public String printCurrent(Cord current) {
        StringBuilder output = new StringBuilder();

        // Top wall
        for (int i=0;i<width+2;++i) {
            output.append("x");
        }
        output.append("\n");

        for (int r=0;r<map.length;++r) {
            output.append("x");
            for (int c=0;c<map[r].length;++c) {
                if (r==current.x && c==current.y) {
                    output.append("*");
                    continue;
                }

                if (map[r][c] == 1)
                    output.append("x");
                else if (map[r][c] == 0)
                    output.append(" ");
                else
                    output.append("o");
            }
            output.append("x\n");
        }

        // Bottom wall
        for (int i=0;i<width+2;++i) {
            output.append("x");
        }

        return output.toString();
    }

    /**
     * Update start point of the maze
     * @param new_x new x(horizontal) coordinate
     * @param new_y new y(horizontal) coordinate
     * @return true if update is successful, false otherwise
     */
    public boolean updateStart(int new_x, int new_y) {
        if (new_x < 0 || new_x >= width || new_y < 0 || new_y >= height)
            throw new IllegalArgumentException("Invalid x or y.");
        else if (map[new_x][new_y] == 1 || map[new_x][new_y] == 2) {
            System.out.println("Cannot place start point here");
            return false;
        }

        start_tile.x = new_x;
        start_tile.y = new_y;
        return true;
    }

    /**
     * Update goal tile of the maze
     * @param new_x new x(horizontal) coordinate
     * @param new_y new y(horizontal) coordinate
     * @return true if update is successful, false otherwise
     */
    public boolean updateEnd(int new_x, int new_y) {
        if (new_x < 0 || new_x >= height || new_y < 0 || new_y >= width)
            throw new IllegalArgumentException("Invalid x or y.");
        else if (map[new_x][new_y] == 1 || (start_tile.x == new_x && start_tile.y == new_y)) {
            System.out.println("Cannot place end point here");
            return false;
        }

        if (end_tile.x != -1)
            map[end_tile.x][end_tile.y] = 0;
        end_tile.x = new_x;
        end_tile.y = new_y;
        map[end_tile.x][end_tile.y] = 2;
        return true;
    }

    /**
     * Running breadth-first-search on the maze, beginning from start point, trying to reach the goal tile
     * <a href="https://en.wikipedia.org/wiki/Breadth-first_search">See wikipedia</a>
     * @param src_row x coordinate of start point
     * @param src_col y coordinate of start point
     * @param displayEachStep true to print state of the maze for each iteration, false to hide them
     */
    public void bfs(int src_row, int src_col, boolean displayEachStep) {
        updateStart(src_row, src_col);

        if (src_row >= height || src_col >= width)
            throw new IllegalArgumentException("Source cordinates must be valid");

        Cord[][] cordMatrix = new Cord[height][width];
        for (int r=0;r<map.length;++r)
            for (int c=0;c<map[r].length;++c)
                cordMatrix[r][c] = new Cord(r,c);


        Cord src = cordMatrix[src_row][src_col];
        if (map[src_row][src_col] == 1) {
            System.out.println("This point cannot be the start point. Try again");
            return;
        } else if (map[src_row][src_col] == 2) {
            System.out.println("Game over!");
            return;
        }

        Set<Cord> explored = new HashSet<>();
        Queue<Cord> toBeExplored = new ArrayDeque<>();

        // Flag indicating the destination has been reached
        boolean completed = false;
        Cord next;
        Cord dest = new Cord(0,0);
        int numIteration = 0;

        for (int r=0;r<cordMatrix.length;++r)
            for (int c=0;c<cordMatrix[r].length;++c)
                if (map[r][c] == 2) {
                    dest = cordMatrix[r][c];
                    break;
                }

        System.out.println("====Start searching for goal tile using BFS====");
        long start = System.currentTimeMillis();

        toBeExplored.add(src);
        while (!toBeExplored.isEmpty() && !completed) {
            next = toBeExplored.poll();
            // System.out.println("Checking (" + next.x + "," + next.y + ")");
            numIteration++;
            if (displayEachStep) {
                System.out.println("Iteration " + numIteration + ":\n");
                System.out.println(printCurrent(next) + "\n");
            }

            if (explored.contains(next))
                continue;

            if (next.x == dest.x && next.y == dest.y) {
                completed = true;
                continue;
            }

            // Add all possible surrounding tiles into queue
            for (int r=-1;r<2;++r) {
                if (next.x + r < 0 || next.x + r >= height)
                    continue;
                for (int c=-1;c<2;++c) {
                    if (next.y + c < 0 || next.y + c >= width)
                        continue;
                    if (r == 0 && c == 0)
                        continue;

                    // If already explored, skip to next element
                    if (explored.contains(cordMatrix[next.x+r][next.y+c]) || toBeExplored.contains(cordMatrix[next.x+r][next.y+c])) {
                        continue;
                    }

                    // If not a barricade tile, add to queue
                    if (map[next.x+r][next.y+c] == 0 || map[next.x+r][next.y+c] == 2) {
                        // System.out.println("Adding (" + (next.x+r) + "," + (next.y+c) + ")");
                        toBeExplored.add(cordMatrix[next.x+r][next.y+c]);
                    }
                }
            }
            explored.add(next);
        }

        long end = System.currentTimeMillis();
        if (completed)
            System.out.println("AI have reached the destination!\n");
        else
            System.out.println("Failed to reach the destination\n");

        System.out.println("Search completed. Time elapsed: " + (end-start) + " ms.");
        System.out.println("Total iteration used: " + numIteration);
    }

    /**
     * Running A* on the maze, beginning from start point, trying to reach the goal tile
     * <a href="https://www.geeksforgeeks.org/a-search-algorithm/">See GeeksforGeeks</a>
     * @param src_row x coordinate of start point
     * @param src_col y coordinate of start point
     * @param displayEachStep true to print state of the maze for each iteration, false to hide them
     */
    public void a_star(int src_row, int src_col, boolean displayEachStep) {
        if (src_row < 0 || src_row >= height || src_col < 0 || src_col >= width)
            throw new IllegalArgumentException("Source coordinates must be valid");

        Cord[][] cord_list = new Cord[height][width];
        for (int r=0;r<height;++r)
            for (int c=0;c<width;++c)
                cord_list[r][c] = new Cord(r,c);

        start_tile.x = src_row;
        start_tile.y = src_col;

        MyPriorityQueue frontier = new MyPriorityQueue();
        Cord current, next;
        Map<Cord, Double> cost_so_far = new Hashtable<>();
        boolean success = false;
        double new_cost, f;
        int[][] neighbors = {{-1,-1},{0,-1},{-1,0},{0,1},{1,0},{1,1}};
        int numIteration = 0;

        System.out.println("====Start searching for goal tile using A*====");
        long start = System.currentTimeMillis();

        frontier.enqueue(0.0,cord_list[start_tile.x][start_tile.y]);
        cost_so_far.put(cord_list[start_tile.x][start_tile.y], 0.0);
        while (!frontier.isEmpty()) {
            current = frontier.dequeue();

            numIteration++;
            if (displayEachStep) {
                System.out.println("Iteration " + numIteration + ":\n");
                System.out.println(printCurrent(current) + "\n");
            }


            if (current.x == end_tile.x && current.y == end_tile.y) {
                success = true;
                break;
            }

            for (int[] neighbor: neighbors) {
                // If the neighbor is out-of-bound or is a blocker, skip to next
                if (current.x+neighbor[0] < 0 || current.x+neighbor[0] >= height || current.y+neighbor[1] < 0 || current.y+neighbor[1] >= width)
                    continue;
                else if (map[current.x+neighbor[0]][current.y+neighbor[1]] == 1)
                    continue;

                next = cord_list[current.x+neighbor[0]][current.y+neighbor[1]];
                new_cost = cost_so_far.get(current) + 1;

                // If neighbor is not explored, or current iteration have better f value
                if (!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)) {
                    // Add neighbor to frontier
                    cost_so_far.put(next, new_cost);
                    f = new_cost + manhattan_distance(next, end_tile);
                    frontier.enqueue(f, next);
                }
            }
        }

        long end = System.currentTimeMillis();
        if (success)
            System.out.println("AI have reached the destination!\n");
        else
            System.out.println("Failed to reach the destination\n");

        System.out.println("Search completed. Time elapsed: " + (end-start) + " ms.");
        System.out.println("Total iteration used: " + numIteration);

    }

    /**
     * Calculate the manhattan distance between two tiles
     * @param src Cord for first tile
     * @param dest Cord for second tile
     * @return double represent the manhattan distance between two
     */
    private static double manhattan_distance(Cord src, Cord dest) {
        return Math.abs(src.x-dest.x) + Math.abs(src.y-dest.y);
    }

    /**
     * Calculate the Euclidean distance between two tiles
     * @param src Cord for first tile
     * @param dest Cord for second tile
     * @return double represent the Euclidean distance between two
     */
    private static double euclidean_distance(Cord src, Cord dest) {
        return Math.sqrt(Math.pow(src.x-dest.x, 2) + Math.pow(src.y-dest.y, 2));
    }

}
