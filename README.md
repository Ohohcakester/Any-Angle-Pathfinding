Any-Angle Pathfinding
=====================

A collection of algorithms used for Any-Angle Pathfinding with visualisations.

[My Slides explaining Any-Angle Pathfinding and the Algorithms](http://ohoh.byethost7.com/Contents/Projects/AnyAnglePathfinding/AnyAnglePathfindingPresentation.pdf)

[Slides for Strict Theta* - ICAPS 2016](http://ohoh.byethost7.com/Contents/Projects/AnyAnglePathfinding/StrictTheta_ICAPS16.pdf)

[Wikipedia](http://en.wikipedia.org/wiki/Any-angle_path_planning)

Features
=====================
1.	Visualisations of the [implemented algorithms](https://github.com/Ohohcakester/Any-Angle-Pathfinding/tree/master/src/algorithms):
    *	Breadth-First Search
    * Dijkstra’s Algorithm
    * A* Search Algorithm (optionally with Post-Smoothing)
    * Jump Point Search Algorithm (optionally with Post-Smoothing)
      * http://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-grastien-aaai11.pdf
    * Theta* Algorithm
      * http://idm-lab.org/bib/abstracts/papers/aaai07a.pdf
    * Lazy Theta* Algorithm
      * http://aigamedev.com/open/tutorial/lazy-theta-star/
    * Strict Theta* Algorithm
      * http://www.aaai.org/ocs/index.php/ICAPS/ICAPS16/paper/view/13049/12685
    * Accelerated A* Algorithm
      * Note: not very well optimised.
      * https://agents.felk.cvut.cz/publications/download/76
    * Visibility Graph Algorithm
      * gives optimal solutions, but very slow.
      * included is an option to reuse an existing visibility graph instead of regenerating it.
      * Includes three algorithms to construct Visibility Graphs over grids:
        1. All-pairs Bresenham Line-of-Sight Checks
            - Bresenham, J. E. 1965. Algorithm for computer control of a digital plotter. IBM Systems journal 4(1):25–30.
        2. Rotational Plane Sweep Algorithm
            - Choset, H. M. 2005. Principles of robot motion: theory, algorithms, and implementation. MIT press.
        3. Line-of-Sight Scans. (fastest)
    * Sparse Visibility Graph Algorithm
      * similar to Visibility Graph Algorithm, but a lot faster.
      * requires preprocessing.
    * Edge N-Level Sparse Visibility Graph Algorithm
      * fastest Any-Angle Pathfinding algorithm, but requires preprocessing unlike Anya16.
      * millisecond runtimes on 6000x6000 maps.
    * Anya
      * gives optimal solutions. Much faster than Visibility Graphs.
      * http://www.aaai.org/ocs/index.php/ICAPS/ICAPS13/paper/viewFile/6060/6194
      * Implementation based on [this paper](http://idm-lab.org/bib/abstracts/papers/socs15a.pdf)
    * Anya16
      * NOT IMPLEMENTED BY ME. (other than the visualisations)
      * Implementation taken from [Daniel Harabor's Code Repository](https://bitbucket.org/dharabor/pathfinding)
      * gives optimal solutions. Much faster than other implementation of Anya.
      * http://jair.org/media/5007/live-5007-9321-jair.pdf
    * SG16: Speeding Up A* Search on Visibility Graphs Defined over Quadtrees
      * implemented over grids instead of over quadtrees.
      * https://www.aaai.org/ocs/index.php/ICAPS/ICAPS16/paper/view/13155/12717

2.	Testing functions that test computed path lengths and running times.

3. Map Generators:
    * DefaultGenerator
      * Generates random grid maps. A random seed can be set for determinstic map generation.
    * AutomataGenerator
      * Generates random cave-maps using cellular automata. (modified from original algorithm)
      * https://pdfs.semanticscholar.org/5f05/6b9bb84015cdd650f043e07f6e7d7d193ae6.pdf
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

Dependencies: JUnit


How to use
=====================
The main class is in AnyAnglePathfinding.java.

Setting the choice variable in main() chooses which function of the program is to be run.
* Visualisation: Generates a visualisation of the algorithm.
* AlgoTest: Conducts running-time and path-length tests on the algorithms.
* Experiment: For other tests for the algorithms. (e.g. checking optimality of an algorithm)
* TestDataGenerator: Generate test data for the /mazedata directory.
* GridGraphVisualiser: Generates a visualisation of the graph. A handy tool for choosing start and end point coordinates.

In general, the maze to be used is configured in loadMaze(), and the algorithm to be run is configured in setDefaultAlgoFunction(), by setting the choice variable.
- choice = 0 for loadMaze generates a random maze. The parameters are configured in case 0 of the switch.
- choice = 1 for loadMaze generates a seeded random maze. The seed can also be configured for seeded random mazes.


Visualisation Controls
=====================
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
