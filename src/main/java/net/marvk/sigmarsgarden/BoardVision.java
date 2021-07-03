package net.marvk.sigmarsgarden;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

public class BoardVision {
    public Board readBoard(final BufferedImage image) {
        final BufferedImage[][] board = ImageUtil.split(image);

        final Tile[][] result = new Tile[board.length][];

        for (int i = 0; i < board.length; i++) {
            final BufferedImage[] tiles = board[i];
            result[i] = new Tile[tiles.length];
            for (int j = 0; j < tiles.length; j++) {
                final BufferedImage tile = tiles[j];
                result[i][j] = bestFit(tile);
            }
        }

        return new Board(result);
    }

    private static Tile bestFit(final BufferedImage bufferedImage) {
        final Mat base = ImageUtil.bufferedImageToMat(bufferedImage);

        double bestScore = Double.NEGATIVE_INFINITY;
        Tile bestTile = null;

        for (final Tile tile : Tile.values()) {
            final Mat[] mats = {tile.getInactiveMat(), tile.getActiveMat()};

            for (int i = 0; i < 2; i++) {
                final double score = compare(base, mats[i]);

                if (score > bestScore) {
                    bestScore = score;
                    bestTile = tile;
                }
            }
        }

        return bestTile;
    }

    private static double compare(final Mat mat1, final Mat mat2) {
        final Mat score = new Mat();

        Imgproc.matchTemplate(adjust(mat1), adjust(mat2), score, Imgproc.TM_CCOEFF_NORMED);

        return Core.minMaxLoc(score).maxVal;
    }

    private static Mat adjust(final Mat input) {
        final Mat equalized = new Mat(input.rows(), input.cols(), input.type());

        Imgproc.equalizeHist(input, equalized);

        for (int j = 0; j < equalized.rows(); j++) {
            for (int i = 0; i < equalized.cols(); i++) {
                final double[] pixel = equalized.get(j, i);

                pixel[0] = clamp(0, 255, 255 - Math.pow((Math.max(230 - pixel[0], 0)), 1.1));
                pixel[0] = clamp(0, 255, 255 - Math.pow((Math.max(255 - pixel[0], 0)), 1.1));
                pixel[0] = clamp(0, 255, 255 - Math.pow((Math.max(255 - pixel[0], 0)), 1.1));

                equalized.put(j, i, pixel);
            }
        }

        return equalized;
    }

    private static double clamp(final double min, final double max, final double value) {
        return Math.max(Math.min(value, max), min);
    }
}
