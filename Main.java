public class Main {
    public static void main(String[] args) {
        Maze mymaze = Maze.createMaze("mymaze2.maze");
        // System.out.println(mymaze);

        mymaze.a_star(0,0, false);
    }
}