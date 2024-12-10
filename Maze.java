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
    private static class Cord {
        private int x;
        private int y;
        private double g;
        private Cord(int x, int y) {
            this.x = x;
            this.y = y;
            this.g = 0.0;
        }
        private void incrementG() {
            this.g++;
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
            if (displayEachStep)
                System.out.println("Iteration " + numIteration++ + ":\n");

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
                        cordMatrix[next.x+r][next.y+c].incrementG();
                        toBeExplored.add(cordMatrix[next.x+r][next.y+c]);
                    }
                }
            }
            explored.add(next);
            if (displayEachStep)
                System.out.println(printCurrent(next) + "\n");
        }

        long end = System.currentTimeMillis();
        if (completed) {
            System.out.println("AI have reached the destination!");
        } else
            System.out.println("There is no path that lead to destination");

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
        PriorityQueue<Cord> toBeExplored = new PriorityQueue<>(Maze::compare_prio);

        // Flag indicating the destination has been reached
        boolean completed = false;
        Cord current;
        Cord dest = new Cord(0,0);
        int numIteration = 0;

        for (int r=0;r<cordMatrix.length;++r)
            for (int c=0;c<cordMatrix[r].length;++c)
                if (map[r][c] == 2) {
                    dest = cordMatrix[r][c];
                    break;
                }

        System.out.println("====Start searching for goal tile using A*====");
        long start = System.currentTimeMillis();

        toBeExplored.add(src);
        while (!toBeExplored.isEmpty() && !completed) {
            current = toBeExplored.poll();
            // System.out.println("Checking (" + current.x + "," + current.y + ")");
            if (displayEachStep)
                System.out.println("Iteration " + numIteration++ + ":\n");

            if (explored.contains(current))
                break;

            if (current.x == dest.x && current.y == dest.y) {
                completed = true;
                continue;
            }

            // Add all possible surrounding tiles into queue
            for (int r=-1;r<2;++r) {
                if (current.x + r < 0 || current.x + r >= height)
                    continue;
                for (int c=-1;c<2;++c) {
                    if (current.y + c < 0 || current.y + c >= width)
                        continue;
                    if (r == 0 && c == 0)
                        continue;

                    // If already explored, skip to next element
                    if (explored.contains(cordMatrix[current.x+r][current.y+c]) || toBeExplored.contains(cordMatrix[current.x+r][current.y+c])) {
                        continue;
                    }

                    // If not a barricade tile, add to queue
                    if (map[current.x+r][current.y+c] == 0 || map[current.x+r][current.y+c] == 2) {
                        // System.out.println("Adding (" + (current.x+r) + "," + (current.y+c) + ")");
                        toBeExplored.add(cordMatrix[current.x+r][current.y+c]);
                    }
                }
            }

            explored.add(current);
            if (displayEachStep)
                System.out.println(printCurrent(current) + "\n");
        }

        long end = System.currentTimeMillis();
        if (completed)
            System.out.println("AI have reached the destination!");
        else
            System.out.println("There is no path that lead to destination");

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
        return (Math.abs(src.x-dest.x) + Math.abs(src.y-dest.y));
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

    /**
     * Comparator function for priority queue in A*
     * @param c1 first cord
     * @param c2 second cord
     * @return less than 0 - c1 < c2, equal to 0 - c1 == c2, greater than 0 - c1 > c2
     */
    private static int compare_prio(Cord c1, Cord c2) {
        double c1_prio = c1.g + manhattan_distance(c1, end_tile);
        double c2_prio = c2.g + manhattan_distance(c2, end_tile);

        return Double.compare(c1_prio, c2_prio);
    }

}
