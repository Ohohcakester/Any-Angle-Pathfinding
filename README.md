Any-Angle Pathfinding
=====================

A collection of algorithms used for Any-Angle Pathfinding with visualisations.

[My Slides explaining Any-Angle Pathfinding and the Algorithms](http://ohoh.byethost7.com/Contents/Projects/AnyAnglePathfinding/AnyAnglePathfindingPresentation.pdf)

[Wikipedia](http://en.wikipedia.org/wiki/Any-angle_path_planning)

Features
=====================
1.	Visualisations of the [implemented algorithms](https://github.com/Ohohcakester/Any-Angle-Pathfinding/tree/master/src/algorithms):
    *	Breadth-First Search
    * Dijkstra’s Algorithm
    * A* Search Algorithm with Post-Smoothing
    * Jump Point Search Algorithm (with Post-Smoothing)
      * http://users.cecs.anu.edu.au/~dharabor/data/papers/harabor-grastien-aaai11.pdf
    * Theta* Algorithm
      * http://idm-lab.org/bib/abstracts/papers/aaai07a.pdf
    * Lazy Theta* Algorithm
      * http://aigamedev.com/open/tutorial/lazy-theta-star/
    * Accelerated A*
      * https://agents.felk.cvut.cz/publications/download/76
    * Visibility Graph Algorithm
      * gives optimal solutions, but very slow
      * included is an option to reuse an existing visibility graph instead of regenerating it.
    * Anya
      * gives optimal solutions. Much faster than Visibility Graphs.
      * http://www.aaai.org/ocs/index.php/ICAPS/ICAPS13/paper/viewFile/6060/6194

2.	Testing functions that test computed path lengths and running times.

3.	Ability to generate pseudo-random graphs from a seed and graph specifications, as well as load graphs from a file. Refer to the uiandio/GraphImporter.java comments for details on how to create a grid file.


Information
=====================
Language: Java 8 Required (uses Eclipse)

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
