package net.marvk.sigmarsgarden;

import nu.pattern.OpenCV;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BoardRobot {
    private final boolean saveCaptures;
    private Robot robot;

    public static void main(final String[] args) throws Exception {
        OpenCV.loadLocally();
        new BoardRobot(false).solveIndefinitely();
    }

    public BoardRobot(final boolean saveCaptures) throws AWTException {
        this.saveCaptures = saveCaptures;
        this.robot = new Robot();
    }

    private void solveOnce() throws IOException {
        System.out.println("NEW GAME");
        robot.mouseMove(870, 886);
        click();
        robot.delay(4750);
        System.out.println("CAPTURING BOARD...");
        final BufferedImage capture = screenCapture();
        if (saveCaptures) {
            saveToSolvedBoards(screenCapture());
        }
        System.out.println("CAPTURED BOARD");

        System.out.println("READING BOARD...");
        final Board board = new BoardVision().readBoard(ImageUtil.monochrome(capture));
        System.out.println("READ BOARD");

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final BoardSolver boardSolver = new BoardSolver();
        final Future<List<Board.Move>> solutionFuture = executor.submit(() -> getSolution(board, boardSolver));

        try {
            System.out.println("SOLVING...");
            final LocalDateTime start = LocalDateTime.now();
            final List<Board.Move> solution = solutionFuture.get(10, TimeUnit.SECONDS);
            System.out.println("SOLVED IN %s WITH %d ITERATIONS".formatted(Duration.between(start, LocalDateTime.now()), boardSolver.getIterations()));
            System.out.println("EXECUTING...");
            executeSolution(solution);
            System.out.println("WE HOPEFULLY DID IT");
            robot.delay(100);
        } catch (InterruptedException | TimeoutException | ExecutionException ignored) {
            System.out.println("FAILED TO SOLVE IN DUE TIME");
            solutionFuture.cancel(true);
        } finally {
            if (saveCaptures) {
                saveToSolvedBoards(screenCapture());
            }
        }
    }

    public void solveIndefinitely() throws IOException {
        while (true) {
            solveOnce();
        }
    }

    private static void saveToSolvedBoards(final BufferedImage capture) throws IOException {
        ImageIO.write(
                capture,
                "png",
                Paths.get("solved_boards")
                     .resolve(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".png")
                     .toFile()
        );
    }

    private BufferedImage screenCapture() {
        return robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit()
                                                              .getScreenSize()));
    }

    private List<Board.Move> getSolution(final Board board, final BoardSolver boardSolver) throws InterruptedException {
        return boardSolver.solve(board);
    }

    public void executeSolution(final List<Board.Move> solution) {
        for (final Board.Move move : solution) {
            final List<Hex> hexes = getHexes(move);

            for (final Hex hex : hexes) {
                robot.mouseMove(screenX(hex), screenY(hex));
                click();
                robot.delay(10);
            }
        }
    }

    private void click() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(10);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private List<Hex> getHexes(final Board.Move move) {
        final List<Hex> hexes = move.getHexes();

        if (move.size() != 5) {
            return hexes;
        }

        final int universalIndex = move.getTiles().indexOf(Tile.UNIVERSAL);

        if (universalIndex == 0) {
            return hexes;
        }

        final List<Hex> result = new ArrayList<>(hexes);
        final Hex universalHex = result.remove(universalIndex);
        result.add(0, universalHex);
        return result;
    }

    public static int screenX(final Hex hex) {
        return ImageUtil.X_START + (ImageUtil.SIZE + ImageUtil.X_DIST) * hex.getHexX() - hex.getHexY() * ImageUtil.X_OFFSET + ImageUtil.SIZE / 2;
    }

    public static int screenY(final Hex hex) {
        return ImageUtil.Y_START + (ImageUtil.SIZE + ImageUtil.Y_DIST) * hex.getHexY() + ImageUtil.SIZE / 2;
    }
}
