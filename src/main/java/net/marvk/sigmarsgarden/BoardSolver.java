package net.marvk.sigmarsgarden;

import java.util.*;

public class BoardSolver {
    private final Set<Board.Hash> transpositions = new HashSet<>();
    private final boolean enableTranspositionTable;

    private int iterations = 0;

    private boolean spent = false;

    public BoardSolver(final boolean enableTranspositionTable) {
        this.enableTranspositionTable = enableTranspositionTable;
    }

    public BoardSolver() {
        this(true);
    }

    public List<Board.Move> solve(final Board board) throws InterruptedException {
        if (spent) {
            throw new IllegalArgumentException();
        }
        spent = true;

        return backtrack(board).orElseThrow(() -> new IllegalStateException("Failed to solve board"));
    }

    private Optional<List<Board.Move>> backtrack(final Board board) throws InterruptedException {
        return backtrack(new ArrayList<>(), board);
    }

    private Optional<List<Board.Move>> backtrack(final List<Board.Move> solution, final Board board) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        if (board.isSolved()) {
            return Optional.of(solution);
        }

        iterations++;

        if (enableTranspositionTable) {
            transpositions.add(board.hash());
        }

        final List<Board.Move> moves = board.validMoves();

        for (final Board.Move move : moves) {
            board.doMove(move);

            if (!(enableTranspositionTable && transpositions.contains(board.hash()))) {
                solution.add(move);

                final Optional<List<Board.Move>> result = backtrack(solution, board);
                if (result.isPresent()) {
                    return result;
                }

                solution.remove(move);
            }

            board.undoMove(move);
        }

        return Optional.empty();
    }

    public int getIterations() {
        return iterations;
    }
}
