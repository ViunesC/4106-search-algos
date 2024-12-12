public class Main {
    public static void main(String[] args) {
        Maze mymaze = Maze.createMaze("gpt200x200.maze");
        // System.out.println(mymaze);

        mymaze.bfs(0,0, false);
        System.out.println("");
        mymaze.a_star(0,0, false);
    }
}