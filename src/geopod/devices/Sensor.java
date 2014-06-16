package geopod.devices;

import geopod.GeopodPlugin;
import geopod.constants.UIConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import ucar.unidata.data.DataChoice;
import ucar.unidata.data.DataInstance;
import ucar.unidata.data.grid.GridDataInstance;
import ucar.unidata.data.grid.GridUtil;
import ucar.unidata.data.point.PointOb;
import ucar.unidata.data.point.PointObFactory;
import ucar.visad.Util;
import ucar.visad.display.Animation;
import visad.Data;
import visad.FieldImpl;
import visad.MathType;
import visad.Real;
import visad.RealTuple;
import visad.RealType;
import visad.Set;
import visad.Tuple;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;
import visad.georef.EarthLocation;
import visad.georef.LatLonPoint;

public class Sensor
{
	/** The controller class for this data set */
	GeopodPlugin m_control;

	/** The data instance */
	private DataInstance m_dataInstance;

	/** The default unit for the DataInstance */
	private Unit m_cachedUnit;

	/** Sampling mode */
	private int m_samplingMode;

	/** point index */
	private int m_pointIndex = -1;

	/** For probing on point data */
	private String m_pointParameter;

	private String m_simpleName;

	/**
	 * Construct a DataInstance wrapper.
	 * 
	 * @param control
	 *            - the GeopodPlugin that this data set is associated with.
	 * @param dataInstance
	 *            - the DataInstance to sample from
	 */
	public Sensor (GeopodPlugin control, DataInstance dataInstance)
	{
		m_control = control;
		m_dataInstance = dataInstance;
		m_simpleName = UIConstants.SIMPLE_NAME_MAP.get (this.getName ());

		this.setSamplingMode (GridUtil.WEIGHTED_AVERAGE);
	}

	/**
	 * @return the dataInstance
	 */
	public DataInstance getDataInstance ()
	{
		return (m_dataInstance);
	}

	public void setDataInstance (DataInstance datainstance)
	{
		m_dataInstance = datainstance;
	}

	/**
	 * @return the parameter name of the sensor. Should be unique.
	 */
	public String getName ()
	{
		// Get the name from the DataChoice corresponding to the DataInstance
		DataChoice choice = m_dataInstance.getDataChoice ();

		return (choice.getDescription ());
	}

	/**
	 * 
	 * @return a simple name/abbreviation of the sensor's parameter name. Not
	 *         unique.
	 */
	public String getSimpleName ()
	{
		return (m_simpleName);
	}

	/**************************************************************************/

	/**
	 * Returns a sample at the given point as a Real value.
	 * 
	 * @param el
	 *            - the {@link EarthLocation} to sample at.
	 * 
	 * @return the sample collected at this location as a {@link Real} value.
	 * 
	 * @throws Exception
	 */
	public Real obtainSampleAsReal (EarthLocation el)
			throws Exception
	{
		Data sample = this.obtainSampleAsData (el);
		Real value = this.convertDataToReal (sample);

		return (value);
	}

	/**
	 * Returns a sample at the given point and the current time as a Data
	 * object. The current time is defined by the GeopodPlugin.
	 * 
	 * @param location
	 *            - the {@link EarthLocation} to sample at.
	 * 
	 * @return the {@link Data} collected at this location
	 * 
	 * @throws Exception
	 */
	public Data obtainSampleAsData (EarthLocation location)
			throws Exception
	{
		// Get animation and step values
		Animation ani = m_control.getAnimation ();
		int step = ani.getCurrent ();
		Real aniValue = ani.getAniValue ();

		// Get sample given animation step and value 
		//   If 4 times are loaded step ranges from 0 to 3
		//   An animation value is a time 
		Data[] sampleArray = this.sampleAtPointAndTime (location, step, aniValue);
		// The 0'th element is the FieldImpl (time -> value function), and the 1st is a RealTuple
		Data sampleAtTime = sampleArray[1];

		if (m_cachedUnit == null)
		{
			// Calculate units
			m_cachedUnit = ((RealTuple) sampleAtTime).getTupleUnits ()[0];
		}

		return (sampleAtTime);
	}

	/**
	 * Get the point index. Can also set the point index.
	 * 
	 * This has something to do with point samples, but I am not sure exactly
	 * what. -KPW
	 * 
	 * @return the index
	 * 
	 * @throws RemoteException
	 *             On badness
	 * @throws VisADException
	 *             On badness
	 */
	private int getPointIndex ()
			throws VisADException, RemoteException
	{
		if (m_pointIndex >= 0)
		{
			return (m_pointIndex);
		}
		// Attempt to update b/c index is invalid
		updatePointIndex ();

		return (m_pointIndex);
	}

	private void updatePointIndex ()
			throws RemoteException, VisADException
	{
		String parameterName = this.getPointParameterName ();
		if (parameterName == null)
		{
			return;
		}
		TupleType tupleType = this.getTupleType ();
		if (tupleType == null)
		{
			return;
		}
		for (int componentNum = 0; componentNum < tupleType.getDimension (); ++componentNum)
		{
			MathType componentType = tupleType.getComponent (componentNum);
			String cleanTypeName = Util.cleanTypeName (componentType.toString ());
			if (cleanTypeName.equals (parameterName))
			{
				m_pointIndex = componentNum;
				break;
			}
		}
	}

	/**
	 * Get the point parameter name
	 * 
	 * @return the name
	 * 
	 * @throws RemoteException
	 *             On badness
	 * @throws VisADException
	 *             On badness
	 */
	private String getPointParameterName ()
			throws VisADException, RemoteException
	{
		if (m_pointParameter == null)
		{
			return (null);
		}
		// Non-null, so see if update is required
		updatePointParameterName ();

		return (m_pointParameter);
	}

	private void updatePointParameterName ()
			throws RemoteException, VisADException
	{
		TupleType tupleType = this.getTupleType ();
		if (tupleType == null)
		{
			return;
		}
		// The first component of RealType will be the parameter's (pressure, wind speed, etc.) type
		//   Point observation tuples are of form ((lat, lon, alt), dateTime, parameter)
		String cleanTypeName = null;
		for (int componentNum = 0; componentNum < tupleType.getDimension (); ++componentNum)
		{
			MathType componentType = tupleType.getComponent (componentNum);
			if (componentType instanceof RealType)
			{
				cleanTypeName = Util.cleanTypeName (componentType.toString ());
				this.setPointParameterName (cleanTypeName);
				break;
			}
		}
	}

	/**
	 * Get the value at the time sample.
	 * 
	 * @param timeSample
	 *            the time sample
	 * 
	 * @return the value
	 * 
	 * @throws RemoteException
	 *             On badness
	 * @throws VisADException
	 *             On badness
	 * 
	 */
	public Real convertDataToReal (Data timeSample)
			throws VisADException, RemoteException
	{
		if (timeSample == null)
		{
			return (null);
		}
		if (timeSample instanceof Real)
		{
			return ((Real) timeSample);
		}
		if (timeSample instanceof FieldImpl)
		{
			// (X -> parameter)
			Data data = ((FieldImpl) timeSample).getSample (0);
			if (data instanceof Real)
			{
				return ((Real) data);
			}

			if (data instanceof PointOb)
			{
				PointOb obs = (PointOb) data;
				Tuple t = (Tuple) obs.getData ();
				int index = getPointIndex ();
				if (index < 0)
				{
					return (null);
				}
				return ((Real) t.getComponent (index));
			}
			return (null);
		}
		RealTuple tuple = (RealTuple) timeSample;

		return ((Real) tuple.getComponent (0));
	}

	/**
	 * Sample at the location and time
	 * 
	 * @param location
	 *            The point to sample at
	 * @param aniValue
	 *            The animation time
	 * @param step
	 *            The animation step
	 * 
	 * @return The sample as an array containing all time samples as a FieldImpl
	 *         and the specific time sample as a Data object. Data[0] = Range of
	 *         time samples Data[1] = Sample at specific time
	 * @throws Exception
	 * @throws Exception
	 */
	private Data[] sampleAtPointAndTime (EarthLocation location, int animationStep, Real animationValue)
			throws Exception
	{
		// Get sample for every time value at location
		// Sample domain: time
		// Sample range: data
		FieldImpl sample = this.sampleAtEarthLocation (location);

		Data data = null;
		if (sample != null)
		{
			// Sample exists
			boolean animationValueOk = animationValue != null && !animationValue.isMissing ();
			if (animationValueOk)
			{
				// Can't use this because it uses floats (?)
				data = sample.evaluate (animationValue, getSamplingMode (), Data.NO_ERRORS);
			}
			else
			{
				// If animation value does not exist, return average value at
				//   animation step
				data = sample.getSample (animationStep);
			}
		}
		if (data == null)
		{
			return (null);
		}

		// Return both the data range and the particular value
		return (new Data[] { sample, data });
	}

	/**
	 * Sample the data held by the DataInstance at the given earth location.
	 * 
	 * @param location
	 *            The point to sample.
	 * 
	 * @return A FieldImpl of all the data samples at a given point.
	 * 
	 * @throws Exception
	 */
	private FieldImpl sampleAtEarthLocation (EarthLocation location)
			throws Exception
	{
		FieldImpl samples = null;
		if (this.isGrid ())
		{
			// Use grid sampling method
			samples = this.sampleGrid (location);
		}
		else
		{
			// Use point sampling method
			samples = this.samplePointList (location);
		}
		return (samples);
	}

	private FieldImpl sampleGrid (EarthLocation el)
			throws VisADException
	{
		// Treat the data instance as a grid
		FieldImpl workingGrid = ((GridDataInstance) m_dataInstance).getGrid ();
		if (workingGrid == null)
		{
			return (null);
		}

		FieldImpl sample = null;
		if (GridUtil.is3D (workingGrid))
		{
			if (GridUtil.isVolume (workingGrid))
			{
				// 3D and volumetric
				sample = GridUtil.sample (workingGrid, el, getSamplingMode ());
			}
			else
			{
				// It's a slice, so convert to 2D grid
				workingGrid = GridUtil.make2DGridFromSlice (workingGrid, false);
				// Then sample
				sample = sampleGrid2D (workingGrid, el);
			}
		}
		else
		{
			// 2D grid
			sample = sampleGrid2D (workingGrid, el);
		}

		return (sample);
	}

	private FieldImpl sampleGrid2D (FieldImpl grid, EarthLocation el)
			throws VisADException
	{
		LatLonPoint latLonPoint = el.getLatLonPoint ();
		FieldImpl sample = GridUtil.sample (grid, latLonPoint, getSamplingMode ());

		return (sample);
	}

	private FieldImpl samplePointList (EarthLocation elt)
			throws Exception
	{
		FieldImpl pointObs = (FieldImpl) m_dataInstance.getData ();
		if (pointObs == null)
		{
			// No data
			return (null);
		}

		int numObservations = pointObs.getDomainSet ().getLength ();
		PointOb closestObs = null;
		double minDistance = 0;
		for (int observationNum = 0; observationNum < numObservations; ++observationNum)
		{
			PointOb obs = (PointOb) pointObs.getSample (observationNum);
			double distance = Util.bearingDistance (obs.getEarthLocation (), elt).getValue ();
			if ((closestObs == null) || (distance < minDistance))
			{
				// Found new closest observation
				closestObs = obs;
				minDistance = distance;
			}
		}
		// No closest observation found (shouldn't happen)
		if (closestObs == null)
		{
			return (null);
		}

		// Find all of the observations located at "closestEarthLoc"
		List<PointOb> closestObservations = new ArrayList<PointOb> ();
		EarthLocation closestEarthLoc = closestObs.getEarthLocation ();
		for (int observationNum = 0; observationNum < numObservations; ++observationNum)
		{
			PointOb obs = (PointOb) pointObs.getSample (observationNum);
			if (obs.getEarthLocation ().equals (closestEarthLoc))
			{
				closestObservations.add (obs);
			}
		}
		FieldImpl sample = PointObFactory.makeTimeSequenceOfPointObs (closestObservations, 0, getPointIndex ());

		return (sample);
	}

	/**
	 * @return Get the sampling mode
	 */
	private int getSamplingMode ()
	{
		return (m_samplingMode);
	}

	/**
	 * Get the TupleType for the data if it's point observations
	 * 
	 * @return the type
	 * 
	 * @throws RemoteException
	 *             Java RMI problem
	 * @throws VisADException
	 *             VisAD problem
	 */
	private TupleType getTupleType ()
			throws VisADException, RemoteException
	{
		TupleType tupleType = null;

		if (isGrid ())
		{
			return (null);
		}
		FieldImpl pointObs = (FieldImpl) m_dataInstance.getData ();
		if (pointObs == null)
		{
			return (null);
		}
		Set domainSet = pointObs.getDomainSet ();
		int numObs = domainSet.getLength ();
		if (numObs == 0)
		{
			return (null);
		}
		PointOb obs = (PointOb) pointObs.getSample (0);
		tupleType = (TupleType) ((Tuple) obs.getData ()).getType ();

		return (tupleType);
	}

	/**
	 * Get the Unit property.
	 * 
	 * @return The Unit
	 */
	public Unit getUnit ()
	{
		return (m_cachedUnit);
	}

	/**
	 * Is this data set a grid?
	 * 
	 * @return true if this data set is a grid
	 */
	private boolean isGrid ()
	{
		return (m_dataInstance instanceof GridDataInstance);
	}

	/**
	 * Set the PointParameter property.
	 * 
	 * @param value
	 *            The new value for PointParameter
	 */
	private void setPointParameterName (String parameterName)
	{
		m_pointParameter = parameterName;
		m_cachedUnit = null;
		m_pointIndex = -1;
	}

	/**
	 * Set the SamplingMode property.
	 * 
	 * @param value
	 *            The new value for SamplingMode
	 */
	public void setSamplingMode (int value)
	{
		m_samplingMode = value;
	}
}