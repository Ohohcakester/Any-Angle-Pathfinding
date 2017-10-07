Any-Angle Pathfinding
=====================

A collection of algorithms used for Any-Angle Pathfinding with visualisations.
* [My Slides explaining Any-Angle Pathfinding and the Algorithms](https://github.com/Ohohcakester/Any-Angle-Pathfinding/files/1286723/AnyAnglePathfindingPresentation.pdf)
* [My Slides for Strict Theta* *- ICAPS 2016*](https://github.com/Ohohcakester/Any-Angle-Pathfinding/files/1286725/StrictTheta_ICAPS16.pdf)
* [My Slides for ENLSVGs (Edge N-Level Sparse Visibility Graph Algorithm) *- SoCS 2017*](https://github.com/Ohohcakester/Any-Angle-Pathfinding/files/1286724/ENLSVG_SoCS17.pdf)
* [Wikipedia](http://en.wikipedia.org/wiki/Any-angle_path_planning)

#### Some useful papers that include comparisons of Any-Angle Pathfinding algorithms:
1. https://www.aaai.org/ocs/index.php/SOCS/SOCS15/paper/view/11295
2. https://aaai.org/ocs/index.php/SOCS/SOCS17/paper/view/15790

Licensing of Source Code
=====================
The implementation of [Anya 2016](./src/algorithms/anya16), from [here](https://bitbucket.org/dharabor/pathfinding), and [JGraphT](./src/algorithms/jgrapht), from [here](http://jgrapht.org/), are licensed under their respective licenses.

Everything else is [unlicensed](http://unlicense.org/).


Features
=====================
1.	Visualisations of the [implemented algorithms](./src/algorithms):
    *	Breadth-First Search
        * optimal 4-directional path (not any-angle)
    * Dijkstra’s Algorithm
      * optimal 8-directional path (not any-angle)
    * A* Search Algorithm (optionally with Post-Smoothing)
      * optimal 8 directional path
      * with post-smoothing, sub-optimal any-angle path
    * Jump Point Search Algorithm (optionally with Post-Smoothing)
      * optimal 8 directional path, much faster than A*
      * http://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-grastien-aaai11.pdf
    * Theta* Algorithm
      * sub-optimal any-angle path, very close to optimal
      * http://idm-lab.org/bib/abstracts/papers/aaai07a.pdf
    * Lazy Theta* Algorithm
      * sub-optimal any-angle path, runs faster than Theta* with slightly longer path lengths
      * http://aigamedev.com/open/tutorial/lazy-theta-star/
    * Strict Theta* Algorithm
      * sub-optimal any-angle paths, much shorter path lengths with (very) slightly longer search times
      * https://www.aaai.org/ocs/index.php/ICAPS/ICAPS16/paper/view/13049
    * Accelerated A* Algorithm
      * sub-optimal any-angle paths
      * Note: not very well optimised.
      * https://agents.felk.cvut.cz/publications/download/76
    * Visibility Graph Algorithm
      * gives optimal solutions, but can be quite slow.
      * included is an option to reuse an existing visibility graph instead of regenerating it. (preprocessing)
      * Includes three algorithms to construct Visibility Graphs over grids:
        1. All-pairs Bresenham Line-of-Sight Checks
        2. Rotational Plane Sweep Algorithm
            - Choset, H. M. 2005. Principles of robot motion: theory, algorithms, and implementation. MIT press.
        3. Line-of-Sight Scans. (fastest)
    * Sparse Visibility Graph Algorithm
      * works similarly to Visibility Graph Algorithm, but a lot faster (and still optimal).
      * requires preprocessing.
      * described in same paper as the Edge N-Level Sparse Visibility Graph Algorithm
    * Edge N-Level Sparse Visibility Graph Algorithm
      * gives optimal solutions, and also the fastest Any-Angle Pathfinding algorithm.
      * requires preprocessing (unlike Anya16).
      * millisecond runtimes on 6000x6000 maps.
      * https://aaai.org/ocs/index.php/SOCS/SOCS17/paper/view/15790
    * Anya
      * gives optimal solutions. Much faster than Visibility Graphs.
      * http://www.aaai.org/ocs/index.php/ICAPS/ICAPS13/paper/viewFile/6060/6194
      * implementation based on [this paper](http://idm-lab.org/bib/abstracts/papers/socs15a.pdf)
    * Anya16
      * NOT IMPLEMENTED BY ME. (other than the visualisations)
      * implementation taken from [Daniel Harabor's Code Repository](https://bitbucket.org/dharabor/pathfinding)
      * gives optimal solutions. Fastest online Any-Angle Pathfinding algorithm.
      * Much faster than other implementation of Anya.
      * http://jair.org/media/5007/live-5007-9321-jair.pdf
    * SG16: Speeding Up A* Search on Visibility Graphs Defined over Quadtrees
      * originally optimal, but includes some sub-optimal optimisations
      * unsure about completeness due to some of the optimisations
      * implemented over grids instead of over quadtrees.
      * https://www.aaai.org/ocs/index.php/ICAPS/ICAPS16/paper/view/13155/12717

2.	Testing functions that test computed path lengths and running times.

3. [Map Generators](./src/main/graphgeneration) to generate grid maps:
    * DefaultGenerator
      * Generates random grid maps. A random seed can be set for determinstic map generation.
    * AutomataGenerator
      * Generates random cave-maps using cellular automata. (modified from original algorithm)
      * https://pdfs.semanticscholar.org/5f05/6b9bb84015cdd650f043e07f6e7d7d193ae6.pdf
    * MazeMapGenerator
      * Generates random maze-maps using a modification of Kruskal's algorithm.
    * TiledMapGenerator
      * Generates maps from tiling a set of existing maps
    * UpscaledMapGenerator
      * Generates maps from upscaling an existing map
      * Post-processing (smoothing) is done after upscaling with cellular automata iterations
    * AffineMapTransformation
      * Applies affine transformations (scaling/rotation/shear) to an existing map to generate new maps.

4. Maps can also be imported from files. Refer to the uiandio/GraphImporter.java comments for details on how to create a grid file.

Information
=====================
Language: Java 8 Required

Visualisation: Java Swing

Dependencies: JUnit (not required if using ant build script)

Pre-Generated Maps
=====================
If you want to load existing pre-generated maps, the maps can be downloaded here:

* [mazedata.7z](https://drive.google.com/open?id=0B3uasjmPSIjIdHpOWUtwVkFlQms): Maps with pre-generated test cases
* [originalbenchmarks.7z](https://drive.google.com/open?id=0B3uasjmPSIjIZUstM0NkU19xOHc): Benchmarks from the [Moving AI Lab](http://www.movingai.com/benchmarks/)

To use them, unzip them and place the `mazedata/` and `originalbenchmarks/` directories in the root of the repository.

### Larger Maps

The larger maps used for the comparison in [these slides](https://github.com/Ohohcakester/Any-Angle-Pathfinding/files/1286724/ENLSVG_SoCS17.pdf) and [this paper](https://aaai.org/ocs/index.php/SOCS/SOCS17/paper/view/15790) are stored only as code in [StoredTestMazes](./src/main/mazes/StoredTestMazes.java) (they are loaded by running the respective generation code). The exact maps used for the tests are found in [AlgoTest](src/main/AlgoTest.java). The large maps come from these four test sets: **"scaledmazes"**, **"tiledmazes"**, **"automatadcmazes"**, **"mazemaps"**.

I have also pre-generated the above maps for convenience. They are available here:
* [largemaps.7z](https://drive.google.com/open?id=0B3uasjmPSIjISFZVVFpMRWt6MVk) (Note: large file size when decompressed)

Build using Apache Ant
=====================
To build and run the code, run the following from the base directory (where build.xml is).
```
ant
java -jar dist/AAP.jar -Xmx4096m
```
To clean up generated files from the build script,
```
ant clean
```

How to use
=====================
The main class is in AnyAnglePathfinding.java.

Setting the choice variable in main() chooses which component of the program is to be run. More details of each component are given in the **Components** section.
* [0] Visualisation: Generates an interactive visualisation of the algorithm.
* [1] AlgoTest: Conducts running-time and path-length tests on the algorithms.
* [2] Experiment: For other tests for the algorithms. (e.g. checking optimality of an algorithm)
* [3] TestDataGenerator: Generate test data for the /mazedata directory.
* [4] GridGraphVisualiser: Generates a visualisation of the graph. A handy tool for choosing start and end point coordinates.
* [5] TextOutputVisualiser: Generates an interactive visualisation of the algorithm from a string.
* [6] AlgoTest.runWithArgs: Run tests using command line arguments

In general, the maze to be used is configured in loadMaze(), and the algorithm to be run is configured in setDefaultAlgoFunction(), by setting the choice variable.

* Some important Mazes in loadMaze(): (you can change the parameters in the corresponding case definition)
    - choice 0: Random mazes
    - choice 1: Random mazes with specified seeds
    - choice 58: Upscaled maps
    - choice 59: Tiled maps
    - choice 60: Automata Maps (Dynamic Cutoff)
    - choice 63: Affine transformation of existing maps
    - choice 66: Maze maps


Components
=====================

Components I use most often:
* [0] Visualisation
* [4] GridGraphVisualiser
* [6] AlgoTest.runWithArgs

## [0] Visualisation
**Use this to generate a trace of the algorithm's search tree.**

The initial view will be the completed path in blue. Step through the frames to view the algorithm's trace.
Generally, blue circles are explored nodes, red lines are parent pointers.
The start point and goal points are marked with circles.

Left/Right: Move backward/forward one step at a time.

PgUp/PgDown: Move backward/forward multiple steps at a time.

Up/Down: Move backward/forward one step at a time, will not loop around to the first frame from the last frame.

A/D and S/W: Move backward/forward multiple steps at a time, will not loop around to the first frame from the last frame.

O: Moves a step forward and takes a screenshot at the same time.

P: Same as O, but does not loop around to the first frame from the last frame.

L: Same as P, but jumps multiple steps at a time.

## [1] AlgoTest
**Use this to test algorithm runtimes/pathlength using the test suite defined in the code**

The test to be run is specified in the function `AlgoTest.run()` in `main/Algotest.java`. The test parameters can be edited there.
```
String[] algoNames = new String[]{
    // Define algorithms to test here
    "Anya16",
    "BasicThetaStar",
};

String[] mapSetNames = new String[]{
    // Define the map sets to test on here
    "benchmarks",
    "automatadcmazes",
};
```

The list of algorithm names can be found in `AlgoTest.getAlgo()` and the list of map sets can be found in `AlgoTest.testSequence`.

The following map sets are used for the tests in [these slides](https://github.com/Ohohcakester/Any-Angle-Pathfinding/files/1286724/ENLSVG_SoCS17.pdf):
* benchmarks
* benchmarksrandom
* scaledmazes
* tiledmazes
* automatadcmazes
* mazemaps

## [2] Experiment
**Used to run various experiments on the algorithm. Check `Experiment.run()` for details**

The `Experiment.run()` function has a bunch of commented-out experiments. I usually just define these on the fly as I need to test certain properties of certain algorithms.

Examples of some more notable experiments:
* `testAlgorithmOptimality()`: Compares an algorithm's computed path length against a known optimal algorithm over an infinite sequence of randomly generated test cases. Very useful for quickly finding example test cases where an algorithm is suboptimal.


## [3] TestDataGenerator
**Used to generate/export grid maps to external files**

I don't use this very frequently. (after all, I only need to generate test data once)

## [4] GridGraphVisualiser
**Used to preview a map**

A very useful tool. It generates a visualisation of the currently-selected grid map. Press any unused key to print the list of hotkeys to the console. (e.g. arrow keys)

Use the left/right mouse buttons to place the start/goal points (respectively).

(Current) list of hotkeys:
```
ESC: Close the window.
9: Generates the path file from the currently selected points.
0: Generates the maze analysis for the maze.
A: Prints the maze analysis for the maze.
P: Prints the path analysis for the current selected path.
S: Generates a .map and a .scen file from the maze.
Z: Switch mode: Automatically display path between points.
X: Switch mode: Automatically display search tree between points.
C: Switch mode: Disable path computation.
V: Hold down for real-time pathfinding to mouse location
```

A common use of this tool is to find a good pair of start/goal points to use in **[0] Visualisation**. My usual workflow:
1. Select a map in `AnyAnglePathfinding.loadMaze()` by setting the `choice` variable
2. Run GridGraphVisualiser to generate a preview of the map
3. Press `Z` to automatically display the shortest path between the start/goal points (powered by ENLSVGs)
4. Use left/right mouse buttons to select a good pair of start/goal points.
5. Press `P` to print the path analysis + coordinates of the two points.
6. Copy down these coordinates and use them for **[0] Visualisation**.

## [5] TextOutputVisualisation
**Used to generate an algorithm trace from a string**

I use this to view algorithm traces of algorithms not implemented in this framework. (e.g. implemented in C++). To do so, I run the algorithm on a certain map, and insert print statements to print out the algorithm trace.

(e.g., when it explores (5,8) with parent (2,4), I print out "2 4 5 8"). One item is printed per line.

The trace is terminated with a `#` character.

The trace should be printed in chronological order for the visualisation to make sense.

Each line of the trace is sequence of *integers*, seperated by spaces.

The following formats can be used in the trace:
* `<x1> <y1> <x2> <y2>`: Draws a line from `(x1,y1)` to `(x2,y2)`
* `<x> <y>`: Draws a point at `(x,y)`
* `<y> <xLn> <xLd> <xRn> <xRd> <px> <py>`: Generates a fractional horizontal interval on row `y`, from `xLn/xLd` (left) to `xRn/xRd` (right). Also draws lines ot the base point `(px, py)`. Used to trace Anya.
* `<y> <xLn> <xLd> <xRn> <xRd>`: Same as above, but without the base point.

(Note: These are defined in the function `GridObjects.create()`)

## [6] AlgoTest.runWithArgs
**Similar to AlgoTest, but we run with test parameters from the command line instead.**

The arguments are as follows:
* `java -jar dist/AAP.jar <algorithmName> <mapSetName> <testType> <outputDirectory>`

Example bash script used for running tests:
```
runtest() {
java -jar dist/AAP.jar -Xmx4096m  "$@"
}

runtest Anya16 benchmarks default output_benchmarks
runtest BasicThetaStar benchmarks default output_benchmarks

runtest Anya16 benchmarksrandom default output_benchmarksrandom
runtest BasicThetaStar benchmarksrandom default output_benchmarksrandom
```

The list of algorithm names can be found in `AlgoTest.getAlgo()` and the list of map sets and test types can be found in `AlgoTest.testSequence`.


