package net.marvk.sigmarsgarden;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Board {
    private static final Tile[] ELEMENTS = {Tile.FIRE, Tile.WATER, Tile.AIR, Tile.EARTH};
    private static final Tile[] METALS = {Tile.TIN, Tile.LEAD, Tile.IRON, Tile.COPPER, Tile.SILVER, Tile.GOLD};

    private final Tile[][] board;

    public Board(final Tile[][] board) {
        this.board = board;
    }

    public Hash hash() {
        return new Hash();
    }

    public boolean isSolved() {
        for (final Tile[] tiles : board) {
            for (final Tile tile : tiles) {
                if (tile != Tile.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }

    public int numTiles() {
        int sum = 0;

        for (final Tile[] tiles : board) {
            for (final Tile tile : tiles) {
                if (tile != Tile.EMPTY) {
                    sum++;
                }
            }
        }

        return sum;
    }

    public List<Hex> playableHexes() {
        final List<Hex> result = new ArrayList<>();

        for (int y = 0; y < board.length; y++) {
            final Tile[] tiles = board[y];

            for (int x = 0; x < tiles.length; x++) {
                final Tile tile = tiles[x];

                if (tile == Tile.EMPTY) {
                    continue;
                }

                final Hex hex = Hex.fromArrayCoords(x, y);

                if (isPlayable(hex)) {
                    result.add(hex);
                }
            }
        }

        return result;
    }

    public Tile get(final Hex hex) {
        return board[hex.getArrayY()][hex.getArrayX()];
    }

    public void set(final Hex hex, final Tile tile) {
        board[hex.getArrayY()][hex.getArrayX()] = tile;
    }

    public boolean has(final Tile query) {
        return Arrays.stream(board).flatMap(Arrays::stream).anyMatch(tile -> query == tile);
    }

    private boolean isPlayable(final Hex hex) {
        final boolean[] freeNeighbours = freeNeighbours(hex);

        boolean result = false;

        for (int i = 0; i < 6; i++) {
            boolean current = true;

            for (int j = 0; j < 3; j++) {
                current &= freeNeighbours[(i + j) % 6];
            }

            result |= current;
        }

        if (result && isBlockedMetal(hex)) {
            return false;
        }

        return result;
    }

    private boolean isBlockedMetal(final Hex hex) {
        final Tile requirement = get(hex).getRequirement();

        if (requirement != null) {
            return has(requirement);
        }

        return false;
    }

    private boolean[] freeNeighbours(final Hex hex) {
        final boolean[] freeNeighbours = new boolean[6];

        final Hex[] neighbours = hex.neighbours();
        for (int i = 0; i < neighbours.length; i++) {
            final Hex neighbour = neighbours[i];

            final int arrayX = neighbour.getArrayX();
            final int arrayY = neighbour.getArrayY();

            if (arrayY < 0 || arrayY >= board.length) {
                freeNeighbours[i] = true;
                continue;
            }

            final Tile[] row = board[arrayY];

            if (arrayX < 0 || arrayX >= row.length) {
                freeNeighbours[i] = true;
                continue;
            }

            if (row[arrayX] == Tile.EMPTY) {
                freeNeighbours[i] = true;
            }
        }
        return freeNeighbours;
    }

    public List<Move> validMoves() {
        final List<Move> result = new ArrayList<>();

        final List<Hex> hexes = playableHexes();

        for (int i = 0; i < hexes.size(); i++) {
            final Hex hex1 = hexes.get(i);

            final Move oneMove = new Move(List.of(hex1));
            if (validOneMove(oneMove)) {
                result.add(oneMove);
            }

            for (int j = i + 1; j < hexes.size(); j++) {
                final Hex hex2 = hexes.get(j);

                final Move twoMove = new Move(List.of(hex1, hex2));

                if (validTwoMove(twoMove)) {
                    result.add(twoMove);
                }

                for (int k = j + 1; k < hexes.size(); k++) {
                    final Hex hex3 = hexes.get(k);

                    for (int l = k + 1; l < hexes.size(); l++) {
                        final Hex hex4 = hexes.get(l);

                        //Yikes...
                        for (int m = l + 1; m < hexes.size(); m++) {
                            final Hex hex5 = hexes.get(m);

                            final Move fiveMove = new Move(List.of(hex1, hex2, hex3, hex4, hex5));

                            if (validFiveMove(fiveMove)) {
                                result.add(fiveMove);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean validOneMove(final Move move) {
        if (move.size() != 1) {
            return false;
        }

        return move.tiles.get(0) == Tile.GOLD;
    }

    private boolean validTwoMove(final Move move) {
        if (move.size() != 2) {
            return false;
        }

        final List<Tile> tiles = move.tiles;

        if (tiles.contains(Tile.VITAE) && tiles.contains(Tile.MORS)) {
            return true;
        }

        if (tiles.get(0) == Tile.SALT && tiles.get(1) == Tile.SALT) {
            return true;
        }

        for (final Tile element : ELEMENTS) {
            if (tiles.get(0) == element && tiles.get(1) == element) {
                return true;
            }

            if (tiles.contains(element) && tiles.contains(Tile.SALT)) {
                return true;
            }
        }

        for (final Tile metal : METALS) {
            if (tiles.contains(metal) && tiles.contains(Tile.QUICKSILVER)) {
                return true;
            }
        }

        return false;
    }

    private boolean validFiveMove(final Move move) {
        if (move.size() != 5) {
            return false;
        }

        return Stream.concat(Arrays.stream(ELEMENTS), Stream.of(Tile.UNIVERSAL))
                     .allMatch(move.tiles::contains);
    }

    private List<Tile> getTiles(final Move move) {
        return getTiles(move.hexes);
    }

    private List<Tile> getTiles(final List<Hex> hexes) {
        return hexes.stream().map(this::get).collect(Collectors.toList());
    }

    public void doMove(final Move move) {
        final List<Hex> hexes = move.hexes;
        for (int i = 0; i < hexes.size(); i++) {
            final Hex hex = hexes.get(i);
            set(hex, Tile.EMPTY);
        }
    }

    public void undoMove(final Move move) {
        final List<Hex> hexes = move.hexes;
        for (int i = 0; i < hexes.size(); i++) {
            final Hex hex = hexes.get(i);
            final Tile tile = move.tiles.get(i);
            set(hex, tile);
        }
    }

    public class Move {
        private final List<Hex> hexes;
        private final List<Tile> tiles;

        public Move(final List<Hex> hexes) {
            this.hexes = hexes;
            this.tiles = Board.this.getTiles(hexes);
        }

        public List<Hex> getHexes() {
            return hexes;
        }

        public List<Tile> getTiles() {
            return tiles;
        }

        public int size() {
            return hexes.size();
        }

        @Override
        public String toString() {
            return "Move{" +
                    "hexes=" + hexes +
                    '}';
        }
    }

    public class Hash {
        private final long l1;
        private final long l2;

        public Hash() {
            long l1 = 0;
            long l2 = 0;

            int index = 0;

            for (final Tile[] tiles : board) {
                for (final Tile tile : tiles) {
                    if (tile == Tile.EMPTY) {
                        if (index < 64) {
                            l1 |= 1L << index;
                        } else {
                            l2 |= 1L << (index - 64);
                        }

                    }

                    index++;
                }
            }

            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Hash hash = (Hash) o;

            if (l1 != hash.l1) return false;
            return l2 == hash.l2;
        }

        @Override
        public int hashCode() {
            int result = (int) (l1 ^ (l1 >>> 32));
            result = 31 * result + (int) (l2 ^ (l2 >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Hash{" +
                    "l1=" + l1 +
                    ", l2=" + l2 +
                    '}';
        }
    }

}
