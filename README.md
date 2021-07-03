# Sigmar's Garden Autosolver 

### Demo

https://user-images.githubusercontent.com/6569856/124358296-f5bf8b80-dc1f-11eb-9907-ba2046189858.mp4

## Implementation Details

### Image processing ([BoardVision.java](src/main/java/net/marvk/sigmarsgarden/BoardVision.java))

I'm using OpenCV's [Template Matching](https://docs.opencv.org/3.4/de/da9/tutorial_template_matching.html) functionality to compare captured tiles to previously classified examples. 

I initially thought about using a structual metric, for example SSIM, but it turned out that perfect classification could be achieved with a template matching approach and some preprocessing, despite the reflections on the tiles.

I'm using several preprocessing steps, namely: 

* Tiling the board
* Cropping tiles to exclude irrelevant details that might make it harder for the classifier to recognise the relevant parts of the image
* Convert to monochrome, as it turned out, value was the only channel needed to achieve perfect matching
* Equalize histogram to take advantage of the full range of available values
* Several contrast increasing steps to separate the relevant parts of the tiles

An example process can be found here:

https://user-images.githubusercontent.com/6569856/124358326-11c32d00-dc20-11eb-9bfa-9909b94c2a2c.mp4

### Move Generation ([Board.java](src/main/java/net/marvk/sigmarsgarden/Board.java))

Once the board is obtailed, generating valid moves is easy enough. First, find tiles that are playable, aka that have three successive free spaces.

Then generate all combinations of one, two and five tiles. 

Then reject 

* all one-moves that 
  * aren't gold
* all two-moves that 
  * don't have two Elements
  * don't have two Salt
  * don't have one Salt and one Element
  * don't have Mors and Vitae
  * don't have Quicksilver and any Metal
* all five-moves that
  * don't have every Element and the Universal tile

Additionally, reject all metal moves that are blocked by a previous metal.

### Solving ([BoardSolver.java](src/main/java/net/marvk/sigmarsgarden/BoardSolver.java))

Solving the game is probably the easiest part of the project. I'm using a simple [Backtracking](https://en.wikipedia.org/wiki/Backtracking) algorithm, enhanced by a [Transposition Table](https://en.wikipedia.org/wiki/Transposition_table), with hashes generated from two `long`s, in which each bit represents a board hex and is set if the corresponding hex is empty.

### Playing the game ([BoardRobot.java](src/main/java/net/marvk/sigmarsgarden/BoardVision.java))

The game is played by a [java.awt.Robot](https://docs.oracle.com/en/java/javase/16/docs/api/java.desktop/java/awt/Robot.html) that is able to start a new game, captures board and click tiles to play moves.

## Acknowledgements & Limitations

If you want to run this yourself, be warned that the code as it stands now is optimized for 1080p resolution and will probably break on anything but 1080p.

This project uses OpenCV.
