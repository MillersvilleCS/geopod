package geopod;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;
import geopod.utils.geometry.PickUtility;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.media.j3d.IndexedPointArray;
import javax.media.j3d.PickConeRay;
import javax.media.j3d.PickInfo;
import javax.vecmath.Point3d;

import visad.RealTuple;

public class GridPointSelector
        extends MouseAdapter
        implements ISubject
{
    private static final int TOLERANCE_IN_PIXELS;

    static
    {
        TOLERANCE_IN_PIXELS = 8;
    }

    private int m_index;
    private GridPointDisplayer m_pointDisplayer;
    private SubjectImpl m_subjectImpl;

    public GridPointSelector (GridPointDisplayer gpd)
    {
        m_pointDisplayer = gpd;
        m_subjectImpl = new SubjectImpl ();
    }

    @Override
    public void mouseClicked (MouseEvent e)
    {
        if (m_pointDisplayer.isVisible ())
        {
            selectPoint (e);
        }
    }

    private void selectPoint (MouseEvent e)
    {
        // Get x and y from mouse event
        int x = e.getX ();
        int y = e.getY ();

        PickConeRay pickRay = PickUtility.computePickConeRay (x, y, TOLERANCE_IN_PIXELS);
        PickInfo pi = m_pointDisplayer.getGridPointBranch ().pickClosest (PickInfo.PICK_GEOMETRY, PickInfo.CLOSEST_GEOM_INFO, pickRay);

        if (pi != null)
        {
            PickInfo.IntersectionInfo[] intersectionInfo = pi.getIntersectionInfos ();
            m_index = intersectionInfo[0].getVertexIndices ()[0];

            Debug.println (DebugLevel.MEDIUM, "Picked index = " + m_index);
            try
            {
                RealTuple sample = (RealTuple) m_pointDisplayer.sampleAtIndex (m_index);
                Debug.printf (DebugLevel.MEDIUM, " Sample value = %5.2f %s\n", sample.getValues ()[0], sample.getTupleUnits ()[0]);
            }
            catch (Exception e1)
            {
                e1.printStackTrace ();
            }

            m_pointDisplayer.setSelectedPoint (m_index);
            this.notifyObservers (GeopodEventId.GRID_POINT_SELECTED);
        }
    }

    public int getSelectedPointIndex ()
    {
        return (m_index);
    }

    public IndexedPointArray getPointArray ()
    {
        IndexedPointArray p = (IndexedPointArray) m_pointDisplayer.getPointShape ().getGeometry ();
        return (p);
    }

    public Point3d getIntersectedPoint ()
    {
        IndexedPointArray p = (IndexedPointArray) m_pointDisplayer.getPointShape ().getGeometry ();
        Point3d worldPoint = new Point3d ();
        int pointIndex = p.getCoordinateIndex (m_index);
        p.getCoordinate (pointIndex, worldPoint);

        return (worldPoint);
    }

    @Override
    public void addObserver (IObserver observer, GeopodEventId eventId)
    {
        m_subjectImpl.addObserver (observer, eventId);
    }

    @Override
    public void notifyObservers (GeopodEventId eventId)
    {
        m_subjectImpl.notifyObservers (eventId);
    }

    @Override
    public void removeObserver (IObserver observer, GeopodEventId eventId)
    {
        m_subjectImpl.removeObserver (observer, eventId);
    }

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}

}
