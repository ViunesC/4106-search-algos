public class Main {
    public static void main(String[] args) {
        Maze mymaze = Maze.createMaze("gpt1200x1200.maze");
        // System.out.println(mymaze);

        mymaze.bfs(0,0, false);
    }
}