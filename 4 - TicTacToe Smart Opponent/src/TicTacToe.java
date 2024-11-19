import java.util.Scanner;

public class TicTacToe {

    private static final int SIZE = 3;
    private static final int PLAYER = 1;
    private static final int COMPUTER = -1;
    private static final int[][] board = new int[SIZE][SIZE];
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("Choose who plays first (1: Player, 2: Computer): ");
        boolean playerFirst = scanner.nextInt() == 1;

        while (true) {
            if (playerFirst) {
                playerMove();
                printBoard();
                if (checkWin(PLAYER)) {
                    System.out.println("Player wins!");
                    break;
                }
                playerFirst = false;
            } else {
                computerMove();
                printBoard();
                if (checkWin(COMPUTER)) {
                    System.out.println("Computer wins!");
                    break;
                }
                playerFirst = true;
            }

            if (isBoardFull()) {
                System.out.println("It's a draw!");
                break;
            }
        }
    }

    private static void playerMove() {
        int row, col;
        while (true) {
            System.out.print("Enter your move (row and column [1-3]): ");
            row = scanner.nextInt() - 1;
            col = scanner.nextInt() - 1;
            if (row >= 0 && row < SIZE && col >= 0 && col < SIZE && board[row][col] == 0) {
                board[row][col] = PLAYER;
                break;
            } else {
                System.out.println("Invalid move. Try again.");
            }
        }
    }

    private static void computerMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1, bestCol = -1;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = COMPUTER;
                    int score = minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                    board[i][j] = 0;
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = i;
                        bestCol = j;
                    }
                }
            }
        }
        board[bestRow][bestCol] = COMPUTER;
    }

    private static int minimax(int depth, int alpha, int beta, boolean isMaximizing) {
        if (checkWin(COMPUTER)) return 10 - depth;
        if (checkWin(PLAYER)) return depth - 10;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = COMPUTER;
                        int eval = minimax(depth + 1, alpha, beta, false);
                        board[i][j] = 0;
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) break;
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = PLAYER;
                        int eval = minimax(depth + 1, alpha, beta, true);
                        board[i][j] = 0; 
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) break;
                    }
                }
            }
            return minEval;
        }
    }

    private static boolean checkWin(int player) {
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true;
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true;
        }
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true;
        return board[0][2] == player && board[1][1] == player && board[2][0] == player;
    }

    private static boolean isBoardFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    private static void printBoard() {
        System.out.println("Current Board:");
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                char symbol = (board[i][j] == PLAYER) ? 'X' : (board[i][j] == COMPUTER) ? 'O' : '-';
                System.out.print(symbol + " ");
            }
            System.out.println();
        }
    }
}
