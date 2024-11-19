import java.util.*;

public class slideNumbers {
    private static final int[] dx = {0, 0, -1, 1};
    private static final int[] dy = {-1, 1, 0, 0};
    private static final String[] moves = {"left", "right", "up", "down"};
    private static final int INF = Integer.MAX_VALUE;
    private static List<String> finalPath;

    static class State {
        int[][] board;
        int N, zeroRow, zeroCol, manhattanDistance, goalZeroRow, goalZeroCol;
        List<String> path;

        State(int[][] board, int zeroRow, int zeroCol, List<String> path, int goalZeroRow, int goalZeroCol) {
            this.board = board;
            this.N = board.length;
            this.zeroRow = zeroRow;
            this.zeroCol = zeroCol;
            this.path = new ArrayList<>(path);
            this.goalZeroRow = goalZeroRow;
            this.goalZeroCol = goalZeroCol;
            this.manhattanDistance = calculateManhattan(board);
        }

        int calculateManhattan(int[][] board) {
            int dist = 0;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int value = board[i][j];
                    if (value != 0) {
                        int targetRow = (value - 1) / N;
                        int targetCol = (value - 1) % N;
                        dist += Math.abs(i - targetRow) + Math.abs(j - targetCol);
                    }
                }
            }
            return dist;
        }

        boolean isGoal() {
            int value = 1;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == goalZeroRow && j == goalZeroCol) {
                        if (board[i][j] != 0) return false;
                    } else {
                        if (board[i][j] != value++) return false;
                    }
                }
            }
            return true;
        }

        State move(int dir) {
            int newRow = zeroRow + dx[dir];
            int newCol = zeroCol + dy[dir];
            if (newRow < 0 || newRow >= N || newCol < 0 || newCol >= N) return null;

            int[][] newBoard = copyBoard();
            newBoard[zeroRow][zeroCol] = newBoard[newRow][newCol];
            newBoard[newRow][newCol] = 0;

            List<String> newPath = new ArrayList<>(path);
            newPath.add(moves[dir]);

            return new State(newBoard, newRow, newCol, newPath, goalZeroRow, goalZeroCol);
        }

        int[][] copyBoard() {
            int[][] newBoard = new int[N][];
            for (int i = 0; i < N; i++) {
                newBoard[i] = Arrays.copyOf(board[i], N);
            }
            return newBoard;
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(board);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof State other)) return false;
            return Arrays.deepEquals(this.board, other.board);
        }
    }

    public static int idaStar(State start) {
        int bound = start.manhattanDistance;
        while (true) {
            Set<State> visited = new HashSet<>();
            int t = search(start, 0, bound, visited);
            if (t == -1) return finalPath.size();
            if (t == INF) return -1;
            bound = t;
        }
    }

    private static int search(State state, int g, int bound, Set<State> visited) {
        int f = g + state.manhattanDistance;
        if (f > bound) return f;
        if (state.isGoal()) {
            finalPath = state.path;
            return -1;
        }

        visited.add(state);

        int min = INF;
        for (int i = 0; i < 4; i++) {
            State neighbor = state.move(i);
            if (neighbor != null && !visited.contains(neighbor)) {
                int t = search(neighbor, g + 1, bound, visited);
                if (t == -1) return -1;
                min = Math.min(min, t);
            }
        }

        visited.remove(state);
        return min;
    }

    public static void main(String[] args) {
        System.out.println("Please enter board size: ");
        Scanner scanner = new Scanner(System.in);
        int N = (int) Math.sqrt(scanner.nextInt() + 1);
        System.out.println("Enter the position of the free tile (-1 for default bottom-right position): ");
        int zeroIndex = scanner.nextInt();

        int[][] board = new int[N][N];
        int zeroRowPosition = -1, zeroColPosition = -1;

        System.out.println("Enter the board values: ");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                board[i][j] = scanner.nextInt();
                if (board[i][j] == 0) {
                    zeroRowPosition = i;
                    zeroColPosition = j;
                }
            }
        }

        int goalZeroRow, goalZeroCol;
        if (zeroIndex == -1) {
            goalZeroRow = N - 1;
            goalZeroCol = N - 1;
        } else {
            goalZeroRow = zeroIndex / N;
            goalZeroCol = zeroIndex % N;
        }

        State initialState = new State(board, zeroRowPosition, zeroColPosition, new ArrayList<>(), goalZeroRow, goalZeroCol);
        long startTime = System.currentTimeMillis();
        int result = idaStar(initialState);
        long endTime = System.currentTimeMillis();

        if (result == -1) {
            System.out.println(result);
        } else {
            System.out.println(result);
            for (String move : finalPath) {
                System.out.println(move);
            }
            System.out.printf("Time taken: %.2f seconds%n", (endTime - startTime) / 1000.0);
        }
    }
}
