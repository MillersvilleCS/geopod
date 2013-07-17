package geopod.utils.geometry;

import java.awt.Color;

import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryUpdater;
import javax.vecmath.Color4b;

/**
 * Update 4-component colors in geometry arrays that use BY_REFERENCE mode.
 * Intended use is for coloring an isosurface.
 * 
 * @author Geopod Team
 * 
 */

public class ColorUpdater
		implements GeometryUpdater
{
	// Colors include an alpha channel
	private static final int COMPONENTS_PER_COLOR = 4;
	private Color4b m_color;
	private int[] m_colorIndices;

	/**
	 * Construct a color updater.
	 * 
	 * @param color
	 *            - the color to use.
	 */
	public ColorUpdater (Color color)
	{
		m_color = new Color4b (color);
		m_colorIndices = new int[3];
	}

	/**
	 * Change the color used.
	 * 
	 * @param color
	 *            - the new color to use.
	 */
	public void setColor (Color color)
	{
		m_color = new Color4b (color);
	}

	/**
	 * Copies color indices into the vertex structure.
	 * 
	 * @param indices
	 *            - the indices to use.
	 */
	public void setColorIndices (int[] indices)
	{
		System.arraycopy (indices, 0, m_colorIndices, 0, indices.length);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void updateData (Geometry geometry)
	{
		GeometryArray geometryArray = (GeometryArray) geometry;
		byte[] colors = geometryArray.getColorRefByte ();
		this.setColor (colors, m_colorIndices[0]);
		this.setColor (colors, m_colorIndices[1]);
		this.setColor (colors, m_colorIndices[2]);
	}

	/**
	 * @param colors
	 * @param index
	 */
	private void setColor (byte[] colors, int index)
	{
		int byteIndex = index * COMPONENTS_PER_COLOR;
		colors[byteIndex + 0] = m_color.x;
		colors[byteIndex + 1] = m_color.y;
		colors[byteIndex + 2] = m_color.z;
		colors[byteIndex + 3] = m_color.w;
	}

}
