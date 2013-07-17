package geopod.utils.geometry;

import geopod.utils.TransformGroupControl;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Class to allow a Geopod to lock on and follow an IDV Isosurface.
 * 
 * @author Geopod Team
 * 
 */
public class IsosurfaceLock
{
	/**
	 * Casts rays to find the nearest isosurface.
	 */
	private IsosurfaceRayCaster m_isosurfaceCaster;

	/**
	 * Handles moving the geopod.
	 */
	private TransformGroupControl m_transformControl;

	/**
	 * The surface normal needs to be used each frame, so cache it to avoid
	 * repeated memory allocations.
	 */
	private Vector3d m_cachedSurfaceNormal;

	/**
	 * Is the isosurface lock enabled or not.
	 */
	private boolean m_isEnabled;
	
	private boolean m_onSurface;

	/**
	 * Constructor.
	 * 
	 * @param transformControl
	 *            - the {@link TransformGroupControl} to move along the
	 *            isosurface.
	 */
	public IsosurfaceLock (TransformGroupControl transformControl)
	{
		m_isosurfaceCaster = new IsosurfaceRayCaster ();
		m_transformControl = transformControl;
		m_cachedSurfaceNormal = null;
		m_onSurface = false;
		setEnabled (false);
	}

	/**
	 * Turn the isosurface lock on or off.
	 * 
	 * @param isEnabled
	 */
	public void setEnabled (boolean isEnabled)
	{
		m_isEnabled = isEnabled;
		if(!isEnabled) m_onSurface = false;
	}

	/**
	 * 
	 * @return - true if the isosurface lock in enabled.
	 */
	public boolean getEnabled ()
	{
		return (m_isEnabled);
	}
	
	public boolean inOnSurface ()
	{
		return (m_onSurface);
	}

	/**
	 * Adjust the Geopod's position so that it's on the isosurface. Does nothing
	 * if the isosurface lock is disabled.
	 */
	public void lockOnSurface ()
	{
		if (m_isEnabled)
		{
			Point3d position = m_transformControl.getPosition ();
			if (m_cachedSurfaceNormal == null)
			{
				m_cachedSurfaceNormal = m_transformControl.getUp ();
			}

			Tuple3d[] collisionPointAndNormal = m_isosurfaceCaster.computeNearestCollision (position,
					m_cachedSurfaceNormal);
			if (collisionPointAndNormal != null)
			{
				m_transformControl.setPosition (collisionPointAndNormal[0]);
				m_cachedSurfaceNormal.set (collisionPointAndNormal[1]);
				m_onSurface = true;
			}
			else
			{
				m_onSurface = false;
			}
		}
	}
}
