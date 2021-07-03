package net.marvk.sigmarsgarden;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

public enum Tile {
    EMPTY(36, null),

    FIRE(8, null),
    AIR(8, null),
    EARTH(8, null),
    WATER(8, null),

    SALT(4, null),

    UNIVERSAL(2, null),

    QUICKSILVER(5, null),

    VITAE(3, null),
    MORS(3, null),

    LEAD(1, null),
    TIN(1, LEAD),
    IRON(1, TIN),
    COPPER(1, IRON),
    SILVER(1, COPPER),
    GOLD(1, SILVER);

    private final BufferedImage inactive;
    private final BufferedImage active;

    private final Mat inactiveMat;
    private final Mat activeMat;
    private final int amount;
    private final Tile requirement;

    Tile(final int amount, final Tile requirement) {
        this.amount = amount;
        this.requirement = requirement;

        try {
            inactive = readTileImage(0);
            active = readTileImage(1);
            inactiveMat = ImageUtil.bufferedImageToMat(ImageUtil.shrink(inactive));
            activeMat = ImageUtil.bufferedImageToMat(ImageUtil.shrink(active));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public BufferedImage getInactive() {
        return inactive;
    }

    public BufferedImage getActive() {
        return active;
    }

    public Mat getInactiveMat() {
        return inactiveMat;
    }

    public Mat getActiveMat() {
        return activeMat;
    }

    public Tile getRequirement() {
        return requirement;
    }

    public int getAmount() {
        return amount;
    }

    private BufferedImage readTileImage(final int id) throws IOException {
        final String name = name().toLowerCase(Locale.ROOT);
        return ImageUtil.loadImageMonochrome(Paths.get("tiles/%s_%d.png".formatted(name, id)));
    }
}
