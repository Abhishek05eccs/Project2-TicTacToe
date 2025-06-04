import java.io.*;
import java.util.*;

public class TicTacToeLogic implements Serializable {
    private int boardSize;
    private int marksToWin;
    private String[][] board;
    private boolean isPlayerTurn;
    private int currentRound;
    private int maxRounds;
    private int playerScore;
    private int opponentScore;
    private int drawCount;
    private boolean isVsComputer;
    private String playerName;
    private String opponentName;
    private String playerSymbol;
    private String opponentSymbol;
    private String computerSymbol;
    private int difficultyLevel;
    private List<Move> moveHistory = new ArrayList<>();
    private boolean isGameOver;

    private static final long serialVersionUID = 1L;

    public TicTacToeLogic() {
        init(3); // Default to 3x3 board
        setMaxRounds(5);
        setPlayerName("Player 1");
        setVsComputer(true);
        setPlayerSymbol("X");
        setComputerSymbol("O");
        setDifficultyLevel(1);
        System.out.println("TicTacToeLogic instantiated");
    }

    public void init(int size) {
        if (size < 3 || size > 10) {
            throw new IllegalArgumentException("Board size must be between 3 and 10");
        }
        boardSize = size;
        marksToWin = boardSize;
        board = new String[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = "";
            }
        }
        currentRound = 1;
        moveHistory.clear();
        isPlayerTurn = true;
        isGameOver = false;
        System.out.println("Board initialized: " + boardSize + "x" + boardSize + ", marks to win: " + marksToWin);
    }

    public boolean makeMove(int row, int col, String symbol) {
        System.out.println("Attempting move: row=" + row + ", col=" + col + ", symbol=" + symbol + ", isGameOver=" + isGameOver + ", isPlayerTurn=" + isPlayerTurn);
        if (isGameOver) {
            System.out.println("Move rejected: Game over");
            return false;
        }
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            System.out.println("Move rejected: Invalid coordinates (row=" + row + ", col=" + col + ")");
            return false;
        }
        if (!board[row][col].isEmpty()) {
            System.out.println("Move rejected: Cell occupied at (" + row + "," + col + ")");
            return false;
        }

        board[row][col] = symbol;
        moveHistory.add(new Move(row, col, symbol));
        System.out.println("Move successful: " + symbol + " placed at (" + row + "," + col + ")");
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.print(board[i][j].isEmpty() ? "." : board[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
        return true;
    }

    public boolean checkWinner(String symbol) {
        System.out.println("Checking win for symbol: " + symbol + ", marksToWin=" + marksToWin);
        // Check rows
        for (int i = 0; i < boardSize; i++) {
            boolean rowWin = true;
            for (int j = 0; j < boardSize; j++) {
                if (!board[i][j].equals(symbol)) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) {
                System.out.println("Win detected in row at i=" + i);
                isGameOver = true;
                return true;
            }
        }
        // Check columns
        for (int j = 0; j < boardSize; j++) {
            boolean colWin = true;
            for (int i = 0; i < boardSize; i++) {
                if (!board[i][j].equals(symbol)) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) {
                System.out.println("Win detected in column at j=" + j);
                isGameOver = true;
                return true;
            }
        }
        // Check main diagonal
        boolean diagWin = true;
        for (int i = 0; i < boardSize; i++) {
            if (!board[i][i].equals(symbol)) {
                diagWin = false;
                break;
            }
        }
        if (diagWin) {
            System.out.println("Win detected in main diagonal");
            isGameOver = true;
            return true;
        }
        // Check anti-diagonal
        boolean antiDiagWin = true;
        for (int i = 0; i < boardSize; i++) {
            if (!board[i][boardSize - i - 1].equals(symbol)) {
                antiDiagWin = false;
                break;
            }
        }
        if (antiDiagWin) {
            System.out.println("Win detected in anti-diagonal");
            isGameOver = true;
            return true;
        }
        System.out.println("No win detected for " + symbol);
        return false;
    }

    public boolean isBoardFull() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].isEmpty()) {
                    System.out.println("Board not full, empty cell at (" + i + "," + j + ")");
                    return false;
                }
            }
        }
        System.out.println("Board is full");
        isGameOver = true;
        return true;
    }

    public void computerMove() {
        System.out.println("Computer move started, difficulty=" + difficultyLevel + ", gameOver=" + isGameOver + ", isPlayerTurn=" + isPlayerTurn);
        if (isGameOver || isPlayerTurn) {
            System.out.println("Computer move skipped: " + (isGameOver ? "Game over" : "It's player's turn"));
            return;
        }

        int[] move = null;
        Random rand = new Random();

        if (difficultyLevel == 1) {
            // Easy: Completely random move
            List<int[]> emptyCells = getEmptyCells();
            if (!emptyCells.isEmpty()) {
                move = emptyCells.get(rand.nextInt(emptyCells.size()));
            }
        } else if (difficultyLevel == 2) {
            // Medium: Check for winning or blocking moves, otherwise random with 70% probability
            move = findWinningMove(computerSymbol);
            if (move == null) {
                move = findWinningMove(playerSymbol); // Block player's win
            }
            if (move == null && rand.nextDouble() < 0.7) {
                // Prefer center or corners
                int center = boardSize / 2;
                if (board[center][center].isEmpty()) {
                    move = new int[]{center, center};
                } else {
                    int[][] corners = {{0, 0}, {0, boardSize - 1}, {boardSize - 1, 0}, {boardSize - 1, boardSize - 1}};
                    List<int[]> emptyCorners = new ArrayList<>();
                    for (int[] corner : corners) {
                        if (board[corner[0]][corner[1]].isEmpty()) {
                            emptyCorners.add(corner);
                        }
                    }
                    if (!emptyCorners.isEmpty()) {
                        move = emptyCorners.get(rand.nextInt(emptyCorners.size()));
                    }
                }
            }
            if (move == null) {
                List<int[]> emptyCells = getEmptyCells();
                if (!emptyCells.isEmpty()) {
                    move = emptyCells.get(rand.nextInt(emptyCells.size()));
                }
            }
        } else if (difficultyLevel == 3) {
            // Hard: Use minimax for optimal move
            move = findBestMove();
        }

        if (move != null) {
            board[move[0]][move[1]] = computerSymbol;
            moveHistory.add(new Move(move[0], move[1], computerSymbol));
            System.out.println("Computer move: (" + move[0] + "," + move[1] + ") with symbol " + computerSymbol);
        } else {
            System.out.println("Computer move failed: No empty cells");
        }
    }

    private List<int[]> getEmptyCells() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].isEmpty()) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        return emptyCells;
    }

    private int[] findWinningMove(String symbol) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].isEmpty()) {
                    boolean wasGameOver = isGameOver;
                    board[i][j] = symbol;
                    boolean isWin = checkWinner(symbol);
                    board[i][j] = "";
                    isGameOver = wasGameOver;
                    if (isWin) {
                        return new int[]{i, j};
                    }
                }
            }
        }
        return null;
    }

    private int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = computerSymbol;
                    int score = minimax(0, false);
                    board[i][j] = "";
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{i, j};
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing) {
        if (checkWinner(computerSymbol)) return 10 - depth;
        if (checkWinner(playerSymbol)) return depth - 10;
        if (isBoardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = computerSymbol;
                        int score = minimax(depth + 1, false);
                        board[i][j] = "";
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = playerSymbol;
                        int score = minimax(depth + 1, true);
                        board[i][j] = "";
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            System.out.println("Undo failed: No moves to undo");
            return false;
        }
        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        board[lastMove.row][lastMove.col] = "";
        isPlayerTurn = !isPlayerTurn;
        isGameOver = false;
        System.out.println("Undid move: " + lastMove.symbol + " at (" + lastMove.row + "," + lastMove.col + ")");
        if (isVsComputer && !moveHistory.isEmpty() && !isPlayerTurn) {
            Move computerMove = moveHistory.remove(moveHistory.size() - 1);
            board[computerMove.row][computerMove.col] = "";
            isPlayerTurn = true;
            System.out.println("Undid computer move: " + computerMove.symbol + " at (" + computerMove.row + "," + computerMove.col + ")");
        }
        return true;
    }

    public void updateScore(String winnerSymbol) {
        System.out.println("Updating score for winner: " + winnerSymbol);
        if (winnerSymbol.isEmpty()) {
            drawCount++;
            System.out.println("Score updated: Draw");
        } else if (winnerSymbol.equals(playerSymbol)) {
            playerScore++;
            System.out.println("Score updated: Player wins");
        } else if (winnerSymbol.equals(opponentSymbol) || (isVsComputer && winnerSymbol.equals(computerSymbol))) {
            opponentScore++;
            System.out.println("Score updated: Opponent wins");
        }
        isGameOver = true;
    }

    public void nextRound() {
        System.out.println("Starting next round: " + (currentRound + 1));
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = "";
            }
        }
        moveHistory.clear();
        currentRound++;
        isPlayerTurn = true;
        isGameOver = false;
        System.out.println("Next round started: Round " + currentRound);
    }

    public void restartRound() {
        System.out.println("Restarting current round: " + currentRound);
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = "";
            }
        }
        moveHistory.clear();
        isPlayerTurn = true;
        isGameOver = false;
        System.out.println("Round restarted: Round " + currentRound);
    }

    public void replayGame() {
        System.out.println("Replaying game");
        playerScore = 0;
        opponentScore = 0;
        drawCount = 0;
        currentRound = 1;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = "";
            }
        }
        moveHistory.clear();
        isPlayerTurn = true;
        isGameOver = false;
        System.out.println("Game replay started");
    }

    public void saveGame(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
            System.out.println("Game saved to: " + filePath);
        }
    }

    public static TicTacToeLogic loadGame(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            TicTacToeLogic loaded = (TicTacToeLogic) ois.readObject();
            if (loaded.boardSize < 3 || loaded.boardSize > 10) {
                throw new IOException("Invalid board size in saved game");
            }
            System.out.println("Game loaded from: " + filePath);
            return loaded;
        }
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void switchTurn() {
        isPlayerTurn = !isPlayerTurn;
        System.out.println("Turn switched, isPlayerTurn: " + isPlayerTurn);
    }

    public String getMark(int row, int col) {
        if (row < 0 || row >= boardSize || col < 0 || col >= boardSize) {
            System.err.println("Invalid coordinates for getMark: (" + row + "," + col + ")");
            return "";
        }
        return board[row][col];
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public void setMaxRounds(int rounds) {
        if (rounds > 0) {
            maxRounds = rounds;
            System.out.println("Max rounds set to: " + rounds);
        }
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public boolean isVsComputer() {
        return isVsComputer;
    }

    public void setVsComputer(boolean vsComputer) {
        isVsComputer = vsComputer;
        opponentName = isVsComputer ? "Computer" : "Player 2";
        System.out.println("Game mode set: " + (isVsComputer ? "vs Computer" : "vs Player"));
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            playerName = name;
            System.out.println("Player name set to: " + name);
        }
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            opponentName = name;
            System.out.println("Opponent name set to: " + name);
        }
    }

    public String getPlayerSymbol() {
        return playerSymbol;
    }

    public void setPlayerSymbol(String symbol) {
        if (symbol.equals("X") || symbol.equals("O")) {
            playerSymbol = symbol;
            if (!isVsComputer) {
                opponentSymbol = symbol.equals("X") ? "O" : "X";
            }
            System.out.println("Player symbol set to: " + symbol + ", opponent symbol: " + opponentSymbol);
        }
    }

    public String getComputerSymbol() {
        return computerSymbol;
    }

    public void setComputerSymbol(String symbol) {
        if (symbol.equals("X") || symbol.equals("O")) {
            computerSymbol = symbol;
            if (isVsComputer) {
                opponentSymbol = symbol;
            }
            System.out.println("Computer symbol set to: " + symbol);
        }
    }

    public String getOpponentSymbol() {
        String symbol = isVsComputer ? computerSymbol : opponentSymbol;
        System.out.println("Opponent symbol: " + symbol);
        return symbol;
    }

    public void setOpponentSymbol(String symbol) {
        if (symbol.equals("X") || symbol.equals("O")) {
            opponentSymbol = symbol;
            System.out.println("Opponent symbol set to: " + symbol);
        }
    }

    public void setDifficultyLevel(int level) {
        if (level >= 1 && level <= 3) {
            difficultyLevel = level;
            System.out.println("Difficulty level set to: " + (level == 1 ? "Easy" : level == 2 ? "Medium" : "Hard"));
        }
    }

    public int getDifficultyLevel() {
        return difficultyLevel;
    }

    private static class Move implements Serializable {
        private static final long serialVersionUID = 1L;
        int row, col;
        String symbol;

        Move(int row, int col, String symbol) {
            this.row = row;
            this.col = col;
            this.symbol = symbol;
        }
    }
}