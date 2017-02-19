package pgraph.anya;

import pgraph.base.BaseVertex;
import pgraph.grid.GridPosition;

import java.awt.geom.Point2D;

/**
 * Created with IntelliJ IDEA.
 * User: dindaro
 * Date: 15.01.2013
 * Time: 22:32
 *
 * Vertex class on Lattice Graphs
 */
public class AnyaVertex extends BaseVertex
{
    public enum CellDirections  {CD_LEFTDOWN, CD_LEFTUP, CD_RIGHTDOWN, CD_RIGHTUP} ;

    public enum VertexDirections  {VD_LEFT, VD_RIGHT, VD_DOWN, VD_UP} ;
    /**
     * Lattice coordinates of the vertex
     */
    public GridPosition gridPos;

    @Override
    public String toString() {
        return "GV["+gridPos+"]";
    }

    public AnyaVertex(int id, Point2D.Double pos, GridPosition gridPos) {
        super(id, pos);
        this.gridPos = gridPos;
    }



}
