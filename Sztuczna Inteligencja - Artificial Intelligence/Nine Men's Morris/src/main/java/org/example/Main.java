package org.example;

import sac.State;
import sac.StateFunction;
import sac.game.GameState;
import sac.game.GameStateImpl;

import java.util.*;

import sac.game.AlphaBetaPruning;
import sac.game.GameSearchConfigurator;



class Mill extends GameStateImpl {

    protected static final int EMPTY = 0;
    protected static final int WHITE = 1;
    protected static final int BLACK = 2;

    static {
        setHFunction(new StateFunction() {


            @Override
            public double calculate(State state) {
                Mill mill = (Mill) state;

                if (mill.isWin()) {
                    return mill.maximizingTurnNow ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }

                double score = (mill.whitePieces + mill.whitePiecesToPlace) -
                        (mill.blackPieces + mill.blackPiecesToPlace);

                score += countMills(mill, Mill.WHITE) * 3;
                score -= countMills(mill, Mill.BLACK) * 3;

                return mill.maximizingTurnNow ? score : -score;
            }

            private int countMills(Mill mill, int player) {
                int mills = 0;
                for (int[] millPattern : Mill.MILLS) {
                    boolean isMill = true;
                    for (int pos : millPattern) {
                        if (mill.board[pos] != player) {
                            isMill = false;
                            break;
                        }
                    }
                    if (isMill) mills++;
                }
                return mills;
            }
        });
    }




    private static final String[] POSITION_NAMES = {
            "a7", "d7", "g7",  // 0,  1,  2
            "b6", "d6", "f6",  // 3,  4,  5
            "c5", "d5", "e5",  // 6,  7,  8
            "a4", "b4", "c4",  // 9,  10, 11
            "e4", "f4", "g4",  // 12, 13, 14
            "c3", "d3", "e3",  // 15, 16, 17
            "b2", "d2", "f2",  // 18, 19, 20
            "a1", "d1", "g1"   // 21, 22, 23
    };

    // Mills i ADJACENT pozostają bez zmian...
    private static final int[][] MILLS = {
            {0, 1, 2},   // top row
            {3, 4, 5},   // middle top row
            {6, 7, 8},   // center top row
            {9, 10, 11}, // left middle row
            {12, 13, 14},// right middle row
            {15, 16, 17},// center bottom row
            {18, 19, 20},// middle bottom row
            {21, 22, 23},// bottom row
            {0, 9, 21},  // left column
            {3, 10, 18}, // middle left column
            {6, 11, 15}, // inner left column
            {1, 4, 7},   // top middle column
            {16, 19, 22},// bottom middle column
            {2, 14, 23}, // right column
            {5, 13, 20}, // middle right column
            {8, 12, 17}  // inner right column
    };

    private static final int[][] ADJACENT = {
            {1, 9},      // Position 0
            {0, 2, 4},   // Position 1
            {1, 14},     // Position 2
            {4, 10},     // Position 3
            {1, 3, 5, 7},// Position 4
            {4, 13},     // Position 5
            {7, 11},     // Position 6
            {4, 6, 8},   // Position 7
            {7, 12},     // Position 8
            {0, 10, 21}, // Position 9
            {3, 9, 11, 18}, // Position 10
            {6, 10, 15}, // Position 11
            {8, 13, 17}, // Position 12
            {5, 12, 14, 20}, // Position 13
            {2, 13, 23}, // Position 14
            {11, 16},    // Position 15
            {15, 17, 19},// Position 16
            {12, 16},    // Position 17
            {10, 19},    // Position 18
            {16, 18, 20, 22}, // Position 19
            {13, 19},    // Position 20
            {9, 22},     // Position 21
            {19, 21, 23},// Position 22
            {14, 22}     // Position 23
    };

    private int[] board;
    private int whitePieces;
    private int blackPieces;
    private int whitePiecesToPlace;
    private int blackPiecesToPlace;
    private boolean waitingForRemoval;
    private int lastMovedPosition;

    public Mill() {
        board = new int[24];
        whitePieces = blackPieces = 0;
        whitePiecesToPlace = blackPiecesToPlace = 9;
        maximizingTurnNow = true;  // White starts (maximizing player)
        waitingForRemoval = false;
        lastMovedPosition = -1;
    }


    private Mill(Mill other) {
        this.board = Arrays.copyOf(other.board, other.board.length);
        this.whitePieces = other.whitePieces;
        this.blackPieces = other.blackPieces;
        this.whitePiecesToPlace = other.whitePiecesToPlace;
        this.blackPiecesToPlace = other.blackPiecesToPlace;
        this.maximizingTurnNow = other.maximizingTurnNow;
        this.waitingForRemoval = other.waitingForRemoval;
        this.lastMovedPosition = other.lastMovedPosition;
        this.setMoveName(other.getMoveName());
    }


    @Override
    public List<GameState> generateChildren() {
        List<GameState> children = new ArrayList<>();
        int currentPlayer = maximizingTurnNow ? WHITE : BLACK;

        // If waiting for removal, only generate removal moves
        if (waitingForRemoval) {
            return generateRemovalMoves();
        }

        int piecesToPlace = maximizingTurnNow ? whitePiecesToPlace : blackPiecesToPlace;

        if (piecesToPlace > 0) {
            // Placement phase - remains the same as it was working correctly
            for (int pos = 0; pos < board.length; pos++) {
                if (board[pos] == EMPTY) {
                    Mill child = new Mill(this);
                    child.board[pos] = currentPlayer;
                    child.lastMovedPosition = pos;

                    if (maximizingTurnNow) {
                        child.whitePiecesToPlace--;
                        child.whitePieces++;
                    } else {
                        child.blackPiecesToPlace--;
                        child.blackPieces++;
                    }

                    child.setMoveName(POSITION_NAMES[pos]);

                    if (child.isMill(pos, currentPlayer)) {
                        child.waitingForRemoval = true;
                    } else {
                        child.maximizingTurnNow = !maximizingTurnNow;
                    }

                    children.add(child);
                }
            }
        } else {
            // Movement phase - allow moving any piece, including those in mills
            boolean canFly = (currentPlayer == WHITE ? whitePieces : blackPieces) == 3;

            for (int from = 0; from < board.length; from++) {
                if (board[from] == currentPlayer) {  // If it's player's piece, it can be moved
                    for (int to = 0; to < board.length; to++) {

                        if (board[to] != EMPTY || board[from] != currentPlayer) {
                            continue;
                        }
                        if (board[to] == EMPTY && (canFly || isAdjacent(from, to))) {
                            Mill child = new Mill(this);
                            // Remove piece from original position
                            child.board[from] = EMPTY;
                            // Place piece in new position
                            child.board[to] = currentPlayer;
                            child.lastMovedPosition = to;
                            child.setMoveName(POSITION_NAMES[from] + "-" + POSITION_NAMES[to]);

                            // Check if the move forms a new mill
                            if (child.isMill(to, currentPlayer)) {
                                child.waitingForRemoval = true;
                            } else {
                                child.maximizingTurnNow = !maximizingTurnNow;
                            }

                            children.add(child);
                        }
                    }
                }
            }
        }

        return children;
    }



    private List<GameState> generateRemovalMoves() {
        List<GameState> children = new ArrayList<>();
        int opponent = maximizingTurnNow ? BLACK : WHITE;

        for (int pos = 0; pos < board.length; pos++) {
            if (board[pos] == opponent) {
                Mill child = new Mill(this);
                child.board[pos] = EMPTY;
                if (opponent == WHITE) {
                    child.whitePieces--;
                } else {
                    child.blackPieces--;
                }
                child.waitingForRemoval = false;
                child.maximizingTurnNow = !maximizingTurnNow;
                child.setMoveName("x" + POSITION_NAMES[pos]);
                children.add(child);
            }
        }

        return children;
    }



    private boolean isAdjacent(int pos1, int pos2) {
        for (int adj : ADJACENT[pos1]) {
            if (adj == pos2) return true;
        }
        return false;
    }

    private boolean isPartOfMill(int pos) {
        int player = board[pos];
        if (player == EMPTY) return false;
        return isMill(pos, player);
    }



    private boolean isMill(int pos, int player) {
        if (player == EMPTY) return false;

        for (int[] mill : MILLS) {
            if (contains(mill, pos)) {
                boolean isCompleteMill = true;
                for (int p : mill) {
                    if (board[p] != player) {
                        isCompleteMill = false;
                        break;
                    }
                }
                if (isCompleteMill) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean contains(int[] array, int value) {
        for (int element : array) {
            if (element == value) return true;
        }
        return false;
    }



/*    public boolean isTerminal() {
        if (whitePiecesToPlace > 0 || blackPiecesToPlace > 0) return false;
        return whitePieces < 3 || blackPieces < 3 || !hasLegalMoves();
    }*/





   public boolean  isWin() {
        if (whitePiecesToPlace > 0 || blackPiecesToPlace > 0) return false;
        if (maximizingTurnNow) {
            return blackPieces < 3 || !hasLegalMoves();
        } else {
            return whitePieces < 3 || !hasLegalMoves();
        }
    }

    @Override
    public boolean isNonWinTerminal() {
        if (whitePiecesToPlace > 0 || blackPiecesToPlace > 0) return false;
        return !hasLegalMoves() &&
                ((maximizingTurnNow && whitePieces >= 3) ||
                        (!maximizingTurnNow && blackPieces >= 3));
    }




    private boolean hasLegalMoves() {
        int currentPlayer = maximizingTurnNow ? WHITE : BLACK;
        int pieces = maximizingTurnNow ? whitePieces : blackPieces;

        if (waitingForRemoval) return true;
        if (pieces < 3) return false;

        boolean canFly = pieces == 3;
        for (int from = 0; from < board.length; from++) {
            if (board[from] == currentPlayer) {
                for (int to = 0; to < board.length; to++) {
                    if (board[to] == EMPTY && (canFly || isAdjacent(from, to))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("7  ").append(symbolAt(0)).append("--------").append(symbolAt(1)).append("--------").append(symbolAt(2)).append("\n");
        sb.append("   |        |        |\n");
        sb.append("6  |  ").append(symbolAt(3)).append("-----").append(symbolAt(4)).append("-----").append(symbolAt(5)).append("  |\n");
        sb.append("   |  |     |     |  |\n");
        sb.append("5  |  |  ").append(symbolAt(6)).append("--").append(symbolAt(7)).append("--").append(symbolAt(8)).append("  |  |\n");
        sb.append("   |  |  |     |  |  |\n");
        sb.append("4  ").append(symbolAt(9)).append("--").append(symbolAt(10)).append("--").append(symbolAt(11))
                .append("     ").append(symbolAt(12)).append("--").append(symbolAt(13)).append("--").append(symbolAt(14)).append("\n");
        sb.append("   |  |  |     |  |  |\n");
        sb.append("3  |  |  ").append(symbolAt(15)).append("--").append(symbolAt(16)).append("--").append(symbolAt(17)).append("  |  |\n");
        sb.append("   |  |     |     |  |\n");
        sb.append("2  |  ").append(symbolAt(18)).append("-----").append(symbolAt(19)).append("-----").append(symbolAt(20)).append("  |\n");
        sb.append("   |        |        |\n");
        sb.append("1  ").append(symbolAt(21)).append("--------").append(symbolAt(22)).append("--------").append(symbolAt(23)).append("\n");
        sb.append("   a  b  c  d  e  f  g\n");

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mill)) return false;

        Mill other = (Mill) o;

        if (!Arrays.equals(board, other.board)) return false;

        return whitePieces == other.whitePieces &&
                blackPieces == other.blackPieces &&
                whitePiecesToPlace == other.whitePiecesToPlace &&
                blackPiecesToPlace == other.blackPiecesToPlace &&
                waitingForRemoval == other.waitingForRemoval &&
                maximizingTurnNow == other.maximizingTurnNow;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + Arrays.hashCode(board);

        result = 31 * result + whitePieces;
        result = 31 * result + blackPieces;
        result = 31 * result + whitePiecesToPlace;
        result = 31 * result + blackPiecesToPlace;
        result = 31 * result + (waitingForRemoval ? 1 : 0);
        result = 31 * result + (maximizingTurnNow ? 1 : 0);

        return result;
    }



    public static void main(String[] args) {
        Mill game = new Mill();
        Scanner scanner = new Scanner(System.in);

        GameSearchConfigurator config = new GameSearchConfigurator();
        config.setDepthLimit(5);
        AlphaBetaPruning alg = new AlphaBetaPruning(null, config);

        String move;
        while (!game.isWinTerminal() && !game.isNonWinTerminal()) {
            System.out.println("\nCurrent position:");
            System.out.println(game.toString());

            if (game.maximizingTurnNow) {
                // Tura człowieka
                List<GameState> children = game.generateChildren();
                boolean validMove = false;
                do {
                    System.out.print("Enter your move: ");
                    move = scanner.nextLine().trim().toLowerCase();

                    for (GameState child : children) {
                        if (move.equals(child.getMoveName())) {
                            game = (Mill) child;
                            validMove = true;
                            break;
                        }
                    }

                    if (!validMove) {
                        System.out.println("Invalid move! Available moves:");
                        for (GameState child : children) {
                            System.out.println(child.getMoveName());
                        }
                    }
                } while (!validMove);
            } else {
                // Tura komputera
                System.out.println("\nComputer thinking...");

                alg.setInitial(game);
                alg.execute();
                move = alg.getFirstBestMove();

                for (GameState child : game.generateChildren()) {
                    if (move.equals(child.getMoveName())) {
                        game = (Mill) child;
                        break;
                    }
                }
                System.out.println("Computer played: " + move);
            }
        }

        System.out.println("\nGame Over!");
        System.out.println(game.toString());

        if (game.isWinTerminal()) {
            String winner = game.maximizingTurnNow ? "Black" : "White";
            System.out.println(winner + " wins!");
        } else {
            System.out.println("Game ended in a draw!");
        }

        scanner.close();
    }




    private String symbolAt(int pos) {
        switch (board[pos]) {
            case WHITE:
                return "●";
            case BLACK:
                return "○";
            default:
                return "·";
        }
    }

    public boolean isWaitingForRemoval() {
        return waitingForRemoval;
    }
}