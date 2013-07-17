package geopod.utils.idv;

import java.rmi.RemoteException;

import ucar.unidata.data.grid.GridUtil;
import visad.CommonUnit;
import visad.CoordinateSystem;
import visad.ErrorEstimate;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DSet;
import visad.MathType;
import visad.RealTupleType;
import visad.RealType;
import visad.SampledSet;
import visad.SetType;
import visad.TupleType;
import visad.Unit;
import visad.VisADException;

public class VerticalProfileUtility
{
	/**
	 * Code modified from source at
	 * {@link ucar.unidata.idv.control.VerticalProfileControl}, lines 574-650.
	 * Originally a private method.
	 * 
	 * Make a profile (Time->(Altitude->Parameter)) or
	 * (Time->(Pressure->Parameter)).
	 * 
	 * @param fieldImpl
	 *            a VisAD FlatField or sequence of FlatFields with 3 or more
	 *            domain coordinates, manifold dimension 1.
	 * 
	 * 
	 * @return the profile.
	 * 
	 * @throws RemoteException
	 *             Java RMI error
	 * @throws VisADException
	 *             VisAD Error
	 */
	public static FieldImpl makeProfile (FieldImpl fieldImpl, boolean convertDomainToAltitude)
			throws VisADException, RemoteException
	{
		boolean isSequence = GridUtil.isTimeSequence (fieldImpl);
		TupleType parm = GridUtil.getParamType (fieldImpl);
		SampledSet domain = GridUtil.getSpatialDomain (fieldImpl);

		RealType height = (RealType) ((SetType) domain.getType ()).getDomain ().getComponent (2);

		RealTupleType domainType = null;
		if (convertDomainToAltitude)
		{
			domainType = new RealTupleType (RealType.Altitude);
		}
		else
		{
			domainType = new RealTupleType (height); // Get pressure from domain. Default unit is the hectopascal.
		}

		// Create function type based on the domain and range
		FunctionType pType = new FunctionType (domainType, parm);

		FunctionType profileType = null;
		FieldImpl profile = null;
		Gridded1DSet profileDomain = null;
		int numIters = 1;
		if (isSequence)
		{
			SampledSet timeDomain = (SampledSet) fieldImpl.getDomainSet ();
			numIters = timeDomain.getLength ();
			MathType tMT = ((SetType) timeDomain.getType ()).getDomain ();
			profileType = new FunctionType (tMT, pType);
			profile = new FieldImpl (profileType, timeDomain);
		}
		else
		{
			profileType = pType;
		}

		for (int i = 0; i < numIters; i++)
		{
			SampledSet ss = GridUtil.getSpatialDomain (fieldImpl, i);
			if ((profileDomain == null) || !GridUtil.isConstantSpatialDomain (fieldImpl))
			{
				Unit vUnit = ss.getSetUnits ()[2];

				float[][] domainVals = ss.getSamples ();
				if (!height.equals (RealType.Altitude) && convertDomainToAltitude)
				{
					CoordinateSystem cs = ss.getCoordinateSystem ();
					domainVals = ss.getCoordinateSystem ().toReference (domainVals, ss.getSetUnits ());
					vUnit = cs.getReferenceUnits ()[2];
				}

				float[] alts = domainVals[2];
				if (!vUnit.equals (CommonUnit.meter) && convertDomainToAltitude)
				{
					alts = CommonUnit.meter.toThis (alts, vUnit);
				}

				try
				{ // domain might have NaN's in it
					profileDomain = new Gridded1DSet (domainType, new float[][] { alts }, domainVals[0].length,
							(CoordinateSystem) null, (Unit[]) null, (ErrorEstimate[]) null, false);
				}
				catch (Exception e)
				{
					break;
				}
			}
			FlatField flatField = new FlatField (pType, profileDomain);
			if (isSequence)
			{
				flatField.setSamples (((FlatField) fieldImpl.getSample (i)).getFloats (false));
				profile.setSample (i, flatField);
			}
			else
			{
				flatField.setSamples (((FlatField) fieldImpl).getFloats (false));
				profile = flatField;
			}
		}
		return (profile);
	}
}
