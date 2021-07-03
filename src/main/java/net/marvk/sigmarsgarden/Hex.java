package net.marvk.sigmarsgarden;

public class Hex {
    private final int x;
    private final int y;

    public Hex(final int x, final int y) {
        this.x = x;
        this.y = y;

    }

    public int getHexX() {
        return x;
    }

    public int getHexY() {
        return y;
    }

    public int getArrayX() {
        return convertXFromHexToArray(x, y);
    }

    public int getArrayY() {
        return y;
    }

    public Hex[] neighbours() {
        return new Hex[]{
                translate(1, 0),
                translate(0, -1),
                translate(-1, -1),
                translate(-1, 0),
                translate(0, 1),
                translate(1, 1)
        };
    }

    public static Hex fromHexCoords(final int x, final int y) {
        return new Hex(x, y);
    }

    public static Hex fromArrayCoords(final int x, final int y) {
        return new Hex(convertXfromArrayToHex(x, y), y);
    }

    public Hex translate(final int dx, final int dy) {
        return fromHexCoords(x + dx, y + dy);
    }

    private static int convertXFromHexToArray(final int x, final int y) {
        if (y < 6) {
            return x;
        }

        return x - y + 5;
    }

    private static int convertXfromArrayToHex(final int x, final int y) {
        if (y < 6) {
            return x;
        }

        return x + y - 5;
    }

    @Override
    public String toString() {
        return "Hex{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
