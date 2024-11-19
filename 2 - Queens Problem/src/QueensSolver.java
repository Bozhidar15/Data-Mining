import java.util.*;

public class QueensSolver {

    private static final int MAX_STEPS = 50000;

    private static void diagonalsOutOfBoard(int y, int x, int size, int[][] conflicts, boolean increment) {
        if (x >= size || y >= size || x < 0 || y < 0)
            return;
        conflicts[x][y] += increment ? 1 : -1;
    }

    private static void fixRowAndDiagonal(int row, int col, int[][] conflicts, int size, boolean increment) {
        for (int j = 0; j < size; j++) {
            if (col != j) {
                conflicts[row][j] += increment ? 1 : -1;
            }
        }
        for (int j = 1; j < size; j++) {
            diagonalsOutOfBoard(col - j, row + j, size, conflicts, increment);
            diagonalsOutOfBoard(col - j, row - j, size, conflicts, increment);
            diagonalsOutOfBoard(col + j, row + j, size, conflicts, increment);
            diagonalsOutOfBoard(col + j, row - j, size, conflicts, increment);
        }
    }

    private static void initializeBoard(int[] queens, int[][] conflicts, int size) {
        int horse = 1;

        for (int i = 0; i < size; i++) {
            if (horse >= size) {
                horse = (size % 2 == 0) ? horse - size - 1 : horse - size;
            }
            queens[i] = horse;
            for (int j = 0; j < size; j++) {
                conflicts[j][i]++;
            }
            fixRowAndDiagonal(horse, i, conflicts, size, true);
            horse += 2;
        }
    }

    private static boolean solveWithMinConflicts(int[] queens, int size, int[][] conflicts) {
        int steps = 0;

        while (steps < MAX_STEPS) {
            int maxConflicts = 0;
            List<Integer> conflictColumns = new ArrayList<>();

            for (int col = 0; col < size; col++) {
                int currentConflicts = conflicts[queens[col]][col];
                if (currentConflicts > maxConflicts) {
                    maxConflicts = currentConflicts;
                    conflictColumns.clear();
                    conflictColumns.add(col);
                } else if (currentConflicts == maxConflicts) {
                    conflictColumns.add(col);
                }
            }

            if (maxConflicts == 1) {
                return true;
            }

            int bestCol = -1;
            int bestRow = -1;
            int minConflictsForMove = Integer.MAX_VALUE;
            Random random = new Random();

            for (int col : conflictColumns) {
                int currentRow = queens[col];

                for (int row = 0; row < size; row++) {
                    if (row == currentRow) continue;
                    int moveConflicts = conflicts[row][col];

                    if (moveConflicts < minConflictsForMove) {
                        minConflictsForMove = moveConflicts;
                        bestCol = col;
                        bestRow = row;
                    } else if (moveConflicts == minConflictsForMove && random.nextBoolean()) {
                        bestCol = col;
                        bestRow = row;
                    }
                }
            }

            int currentRow = queens[bestCol];
            fixRowAndDiagonal(currentRow, bestCol, conflicts, size, false);
            fixRowAndDiagonal(bestRow, bestCol, conflicts, size, true);
            queens[bestCol] = bestRow;

            steps++;
        }

        return false;
    }

    public static void main(String[] args) {
        System.out.println("Queens count: ");
        Scanner scanner = new Scanner(System.in);
        int N = scanner.nextInt();
        if (N <= 0 || N == 2 || N == 3) {
            System.out.println(-1);
            return;
        }

        int[] queens = new int[N];
        int[][] conflicts = new int[N][N];

        initializeBoard(queens, conflicts, N);

        long startTime = System.currentTimeMillis();
        boolean solved = solveWithMinConflicts(queens, N, conflicts);
        long endTime = System.currentTimeMillis();

        if (!solved) {
            System.out.println(-1);
        } else if (N <= 100) {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    System.out.print(i == queens[j] ? '*' : '_');
                }
                System.out.println();
            }
        }
        System.out.printf("Time taken: %.2f seconds%n", (endTime - startTime) / 1000.0);
    }
}
