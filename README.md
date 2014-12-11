Any-Angle Pathfinding
=====================

A collection of algorithms used for Any-Angle Pathfinding with visualisations.

[My Slides explaining Any-Angle Pathfinding and the Algorithms](http://ohoh.byethost7.com/Contents/Projects/AnyAnglePathfinding/OhAAPSlides.pdf)

[Algorithm Videos](http://ohoh.byethost7.com/?page=anyanglepathfinding#media)

[Wikipedia](http://en.wikipedia.org/wiki/Any-angle_path_planning)

Features
=====================
1.	Visualisations of the implemented algorithms:
    *	Breadth-First Search
    * Dijkstra’s Algorithm
    * A* Search Algorithm with Post-Smoothing
    * Theta* Algorithm
      * http://idm-lab.org/bib/abstracts/papers/aaai07a.pdf
    * Visibility Graph Algorithm
      * gives optimal solutions, but very slow
    * Anya
      * implementation still a bit buggy. Not giving optimal solutions.
      * http://www.aaai.org/ocs/index.php/ICAPS/ICAPS13/paper/viewFile/6060/6194

2.	Testing functions that test computed path lengths and running times.

3.	Ability to generate pseudo-random graphs from a seed and graph specifications, as well as load graphs from a file. Refer to the GraphImporter.java comments for details on how to create a grid file.


Information
=====================
Language: Java 8 Required (uses Eclipse)

Visualisation: Java Swing

Dependencies: JUnit


How to use
=====================
All the main functions are within AnyAnglePathfinding.java.

The static attributes of the class AnyAnglePathfinding are used to configure the generated maze. Refer to comments for more details.

Choose the algorithm to execute in the method setDefaultAlgoFunction(). The value of the variable "choice" decides the algorithm.

Choose the maze to use in the method loadMaze(). The value of the variable "choice" decides the maze that is used. Set choice = 0 to use the maze determined by the static attributes mentioned above.

runTestAllAlgos() is used to run time and path-length tests on the algorithms using the given test cases. Test output is written to files in the testdata subdirecotry.

traceAlgorithm() is used to trace and generate a visualisation of the selected algorithm.


Visualisation Controls
=====================
The initial view will be the completed path in blue. Step through the frames to view the algorithm's trace.
Generally, blue circles are explored nodes, red lines are parent pointers.
The start point and goal points are marked with circles.

Left/Right: Move backward/forward one step at a time.

PgUp/PgDown: Move backward/forward multiple steps at a time.

Up/Down: Move backward/forward one step at a time, will not loop around to first frame from last frame.

A/D and S/W: Move backward/forward multiple steps at a time, will not loop around to first frame from last frame.
