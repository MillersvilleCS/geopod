package geopod;

import geopod.utils.collections.IterableVisADSet;
import geopod.utils.comparators.GridPointComparator;
import geopod.utils.coordinate.IdvCoordinateUtility;
import geopod.utils.debug.Debug;
import geopod.utils.idv.SceneGraphControl;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.IndexedPointArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3f;

import ucar.unidata.data.DataInstance;
import ucar.unidata.data.grid.GridDataInstance;
import ucar.unidata.data.grid.GridUtil;
import visad.CoordinateSystem;
import visad.Data;
import visad.FieldImpl;
import visad.FlatField;
import visad.SampledSet;
import visad.VisADException;
import visad.georef.EarthLocationLite;

/**
 * A class to handle displaying the grid points from a {@link DataInstance}.
 * 
 */
public class GridPointDisplayer
{
	private static final int NONE_SELECTED;
	public static final int DEFAULT_GRID_POINT_STRIDE;
	private static final float POINT_SIZE;

	static
	{
		NONE_SELECTED = -1;
		DEFAULT_GRID_POINT_STRIDE = 4;
		POINT_SIZE = 8;
	}

	private BranchGroup m_gridPointBranch;
	private Switch m_pointSwitch;
	private Shape3D m_pointsShape;
	IndexedPointArray m_pointArray;
	private boolean m_isVisible;
	private float[] m_selectionColor;
	private float[] m_defaultColor;
	private int m_currentlySelected;
	FlatField m_flatField;
	boolean m_hasPoints;

	/**
	 * Length of the dimensions in [lat, lon, alt] format.
	 */
	private int[] m_dimLengths;

	/**
	 * Construct a default {@link GridPointDisplayer}.
	 */
	public GridPointDisplayer ()
	{
		m_hasPoints = false;
		m_currentlySelected = NONE_SELECTED;
		m_selectionColor = Color.RED.getRGBColorComponents (null);
		m_defaultColor = Color.GRAY.getRGBColorComponents (null);

		// These are initialized when buildGridPoints is called.
		m_pointSwitch = null;
		m_pointsShape = null;
	}

	/**
	 * Build a set of visible grid points based on the given {@link DataInstance}.
	 * 
	 * @param dataInstance
	 */
	public void buildGridPoints (DataInstance dataInstance, int gridPointStride)
	{
		Debug.printf ("Building grid points using the sensor for %s.\n", dataInstance.getParamName ());

		try
		{
			FieldImpl grid = getGridFromData (dataInstance);
			Point3f[] dataArray = createGridDataArray (grid);
			m_pointsShape = createGridPointsShape (dataArray, gridPointStride);
			m_pointArray = (IndexedPointArray) m_pointsShape.getGeometry ();
			m_pointSwitch = createPointsSwitchGroup (m_pointsShape);

			attachToSceneGraph (m_pointSwitch);
			setVisible (false);

			m_hasPoints = true;
		}
		catch (Exception e)
		{
			System.out.printf ("Unable to create grid points for %s.\n", dataInstance.getParamName ());
			e.printStackTrace ();
		}
	}

	/**
	 * @return true if grid points are available for display.
	 */
	public boolean hasPoints ()
	{
		return (m_hasPoints);
	}

	/**
	 * Turn point display on or off.
	 * 
	 * @param visible
	 */
	public void setVisible (boolean visible)
	{
		if (m_pointSwitch != null)
		{
    			m_isVisible = visible;
    			m_pointSwitch.setWhichChild (visible ? Switch.CHILD_ALL : Switch.CHILD_NONE);
		}
	}

	/**
	 * @return true if the grid points are visible.
	 */
	public boolean isVisible ()
	{
		return (m_isVisible);
	}

	private static FieldImpl getGridFromData (DataInstance data)
	{
		GridDataInstance gridInstance = (GridDataInstance) data;

		return (gridInstance.getGrid ());
	}

	private static FlatField getFlatField (FieldImpl field)
			throws RemoteException, VisADException
	{
		// Is the field of the form (time -> range)?
		// Range is typically of the form (point -> parameter),
		// where point is a (lat, lon[, alt]) tuple
		FlatField pointToParamMapping = null;
		if (GridUtil.isTimeSequence (field))
		{
			// Use the first time
			pointToParamMapping = (FlatField) field.getSample (0);
		}
		else
		{
			// No, it's of the form (point -> parameter)
			pointToParamMapping = (FlatField) field;
		}
		return (pointToParamMapping);
	}

	/**
	 * Create a list of grid points from a flat field
	 * 
	 * @param grid
	 * @return - an array of grid points
	 * @throws VisADException
	 * @throws RemoteException
	 */
	private Point3f[] createGridDataArray (FieldImpl grid)
			throws VisADException, RemoteException
	{
		m_flatField = getFlatField (grid);

		// Get the set of points constituting the domain
		SampledSet spatialDomainSet = GridUtil.getSpatialDomain (m_flatField);
		int numEarthTuples = spatialDomainSet.getLength ();
		int setDimension = spatialDomainSet.getDimension ();
		Debug.printf ("Spatial domain contains %d grid points of dimension %d.\n", numEarthTuples, setDimension);

		// Get the number of grid points in each dimension.
		visad.GriddedSet setInterface = (visad.GriddedSet) spatialDomainSet;
		m_dimLengths = setInterface.getLengths (); // Get the dimensions as [lat, lon, alt]

		Set<Point3f> pointSet = new TreeSet<Point3f> (new GridPointComparator ());
		Point3f[] gridPoints = null;

		// Is the domain 3D or 2D?
		if (setDimension == 3 || setDimension == 2)
		{
			IterableVisADSet locations = new IterableVisADSet (spatialDomainSet);
			
			@SuppressWarnings("rawtypes")
			Iterator locIter = locations.iterator ();

			int[] coordinateIndex = getLatLonAltIndexes(spatialDomainSet.getType().prettyString());
			
			while (locIter.hasNext ())
			{
				float[] elArray = (float[]) locIter.next ();
				EarthLocationLite el = new EarthLocationLite(elArray[coordinateIndex[0]], elArray[coordinateIndex[1]], elArray[coordinateIndex[2]]);
				Point3f boxPoint = IdvCoordinateUtility.convertEarthToBoxFloat (el);
				Point3f point = new Point3f (boxPoint);
				pointSet.add (point);
			}

			int numDuplicates = spatialDomainSet.getLength () - pointSet.size ();
			if (numDuplicates > 0)
			{
				System.out.printf ("Duplicate points found. Number of points skipped is %d.\n", numDuplicates);
			}
			gridPoints = pointSet.toArray (new Point3f[0]);
		}
		else
		{
			throw (new RuntimeException ("Unable to handle " + setDimension + "D grid points."));
		}

		return (gridPoints);
	}
	
	/** 
	 * @param dataType A string that contains information about the ordering of latitude, 
	 * longitude, and in the data set.
	 * @return an array that "translates" index access such that:<br>
	 * {@code returnval[0]} will be the index of the latitude,<br>
	 * {@code returnval[1]} will be the index of the longitude, and<br>
	 * {@code returnval[2]} will be the index of the altitude. 
	 */
	private int[] getLatLonAltIndexes(String dataType) 
	{
		int lat, lon, alt;
		
		if (dataType.contains("lat[")) 
		{
			lat = dataType.indexOf("lat[");
			lon = dataType.indexOf("lon[");
			alt = dataType.indexOf("isobaric["); // (Sean Arms') OpenDAP Dataset
		}
		else if (dataType.contains(("x["))) // NCEP
		{
			lat = dataType.indexOf("x[");
			lon = dataType.indexOf("y[");
			alt = dataType.indexOf("isobaric["); 
		}
		else {
			System.out.println("GridPointDisplayer.determineLatLonAltIndexes(): Failed to correctly identify data type format (0). Assuming [lat, lon, alt].");
			return new int[] {0, 1, 2};
		}
		
		if (lat < 0 || lon < 0 || alt < 0) {
			System.out.println("GridPointDisplayer.determineLatLonAltIndexes(): Failed to correctly identify data type format (1). Assuming [lat, lon, alt].");
			return new int[] {0, 1, 2};
		}
		
		// Sadly can't think of a more concise way to do this without making a sorted list...
		if (lat < lon && lat < alt) {
			if (lon < alt) {
				return new int[] {0, 1, 2}; // [lat, lon, alt]
			}
			else {
				return new int[] {0, 2, 1}; // [lat, alt, lon]
			}
		}
		else if (lon < lat && lon < alt) {
			if (lat < alt) {
				return new int[] {1, 0, 2}; // [lon, lat, alt]
			}
			else {
				return new int[] {1, 2, 0}; // [lon, alt, lat]
			}
		}
		else {
			if (lat < lon) {
				return new int[] {2, 0, 1}; // [alt, lat, lon]
			}
			else {
				return new int[] {2, 1, 0}; // [alt, lon, lat]
			}
		}	
	}

	/**
	 * Obtain a sample from the grid point referenced by the given index.
	 * 
	 * @param pointIndex
	 * @return - the sample {@link Data} from this index
	 * @throws RemoteException
	 *             - problem getting data from remote object (forwarded from IDV.
	 * @throws VisADException
	 *             - problem getting data (forwarded from IDV).
	 */
	public Data sampleAtIndex (int pointIndex)
			throws RemoteException, VisADException
	{
		int vertexNum = convertIndexToVertexNumber (pointIndex);

		return (m_flatField.getSample (vertexNum));
	}

	private int convertIndexToVertexNumber (int index)
	{
		int vertexNum = m_pointArray.getCoordinateIndex (index);

		return (vertexNum);
	}

	private void selectPointsByStride (IndexedPointArray indPointArray, int stride)
	{
		// Use index array to select a subset of the vertices.
		// Use the same stride in each dimension except for alt (due to the low vertical resolution of most models).
		// TODO: need to decide what to do about low vertical resolution models.
		int index = 0;
		final int lon = 0, lat = 1, alt = 2; // Constants used for named indexing.
		for (int latIndex = 0; latIndex < m_dimLengths[lat]; latIndex += stride)
		{
			for (int lonIndex = 0; lonIndex < m_dimLengths[lon]; lonIndex += stride)
			{
				for (int altIndex = 0; altIndex < m_dimLengths[alt]; altIndex++)
				{
					// Points are sorted by y-z-x (lat, lon, alt) order. Manually index into this array.
					int vertexNum = latIndex * m_dimLengths[lon] * m_dimLengths[alt] + altIndex * m_dimLengths[lon]
							+ lonIndex;

					indPointArray.setCoordinateIndex (index, vertexNum);
					indPointArray.setColorIndex (index, vertexNum);
					index++;
				}
			}
		}
		indPointArray.setValidIndexCount (index);
	}

	/**
	 * Create a Shape3D containing all the grid points, given an array of Point3f objects.
	 * 
	 * @param points
	 * @param sgc
	 * @return
	 */
	private Shape3D createGridPointsShape (Point3f[] points, int stride)
	{
		IndexedPointArray indPointArray = new IndexedPointArray (points.length, GeometryArray.COORDINATES
				| GeometryArray.COLOR_3, points.length);
		indPointArray.setCapability (PointArray.ALLOW_COLOR_WRITE);
		indPointArray.setCapability (IndexedPointArray.ALLOW_COORDINATE_INDEX_WRITE);
		indPointArray.setCapability (IndexedPointArray.ALLOW_COLOR_INDEX_WRITE);
		indPointArray.setCapability (PointArray.ALLOW_COUNT_WRITE);

		// Set all the vertex coordinates and colors
		for (int vertexInd = 0; vertexInd < points.length; ++vertexInd)
		{
			Point3f point = points[vertexInd];
			indPointArray.setCoordinate (vertexInd, point);
			indPointArray.setColor (vertexInd, m_defaultColor);
		}

		// Select which grid points to display
		selectPointsByStride (indPointArray, stride);

		// Create the material for the points
		Appearance pointApp = new Appearance ();
		// Set the point size and enable anti-aliasing
		//   GMZ: Anti-aliasing breaks z-testing, so changed to false
		PointAttributes attrib = new PointAttributes (POINT_SIZE, false);
		pointApp.setPointAttributes (attrib);

		Shape3D pointShape = new Shape3D (indPointArray, pointApp);
		pointShape.setCapability (Shape3D.ALLOW_GEOMETRY_WRITE);

		return (pointShape);
	}
	
	public void resetGridPoints ()
	{
	    m_hasPoints = false;
	    m_isVisible = false;
	    m_currentlySelected = NONE_SELECTED;
	    detachFromSceneGraph ();
	    
	    m_pointSwitch = null;
	    m_pointArray = null;
	    m_pointsShape = null;
	}

	/**
	 * Change how many grid points will be displayed. A stride of 1 will display every point.
	 * 
	 * @param pointStride
	 *            - display every stride'th point.
	 */
	public void adjustGridPointDensity (int pointStride)
	{
	    if (m_gridPointBranch != null)
	    {
		// Deselect the previously selected point.
		deselectCurrentlySelectedPoint ();

		// Detach from the scene graph before resizing (avoids slowdown)
		Group parent = (Group) m_gridPointBranch.getParent ();
		m_gridPointBranch.detach ();

		// Select which grid points to display
		selectPointsByStride (m_pointArray, pointStride);

		// Re-attach to the scene graph
		parent.addChild (m_gridPointBranch);
	    }
	}

	private static Switch createPointsSwitchGroup (Shape3D points)
	{
		// Create a switch node to turn the point display on and off
		Switch pointSwitch = new Switch ();
		pointSwitch.setCapability (Group.ALLOW_CHILDREN_WRITE);
		pointSwitch.setCapability (Group.ALLOW_CHILDREN_EXTEND);
		pointSwitch.setCapability (Switch.ALLOW_SWITCH_WRITE);

		pointSwitch.addChild (points);
		pointSwitch.setWhichChild (Switch.CHILD_NONE);

		return (pointSwitch);
	}

	private void attachToSceneGraph (Switch dataSwitchGroup)
	{
		// Create a branch group to hold the grid points
		m_gridPointBranch = new BranchGroup ();
		m_gridPointBranch.setCapability (BranchGroup.ALLOW_DETACH);
		m_gridPointBranch.addChild (dataSwitchGroup);

		// Add this branch to the scene graph
		TransformGroup volumeTransform = SceneGraphControl.getDataVolumeTransform ();
		volumeTransform.addChild (m_gridPointBranch);
	}

	/**
	 * Remove the visible grid points from the scene graph.
	 */
	public void detachFromSceneGraph ()
	{
		if (m_gridPointBranch != null)
		{
			m_gridPointBranch.detach ();
		}
	}

	/**
	 * @return the scene graph branch holding the visible grid points.
	 */
	public BranchGroup getGridPointBranch ()
	{
		return (m_gridPointBranch);
	}

	/**
	 * @return the {@link Shape3D} used to represent the grid points.
	 */
	public Shape3D getPointShape ()
	{
		return (m_pointsShape);
	}

	/**
	 * @param index
	 *            - the index of the selected grid point.
	 */
	public void setSelectedPoint (int index)
	{
		// Restore the color of the previously selected point if it exists
		deselectCurrentlySelectedPoint ();

		// store the new index
		m_currentlySelected = index;

		// Get the vertex number that corresponds with the index
		int vertexNum = m_pointArray.getColorIndex (m_currentlySelected);
		m_pointArray.setColor (vertexNum, m_selectionColor);
	}

	/**
	 * Reset the grid points so that none are selected.
	 */
	public void deselectCurrentlySelectedPoint ()
	{
		if (m_currentlySelected != NONE_SELECTED)
		{
			int vertexNum = m_pointArray.getColorIndex (m_currentlySelected);
			m_pointArray.setColor (vertexNum, m_defaultColor);
			m_currentlySelected = NONE_SELECTED;
		}
	}

}
