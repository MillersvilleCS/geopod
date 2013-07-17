package geopod.utils.collections;

import java.util.Iterator;

import visad.CoordinateSystem;
import visad.Set;
import visad.VisADException;

/**
 * A wrapper around a VisAD set to allow iteration. Handles only floats
 * currently.
 */
@SuppressWarnings("rawtypes")
public class IterableVisADSet
		implements Iterable
{
	private Set m_set;
	private float[][] m_samples;

	/**
	 * Constructor.
	 * 
	 * @param set
	 *            - the {@link visad.Set VisAD set} to iterate over.
	 */
	public IterableVisADSet (visad.Set set)
	{
		m_set = set;
		try
		{
			m_samples = set.getSamples ();
			CoordinateSystem cs = set.getCoordinateSystem ();
			if (cs != null)
			{
				m_samples = cs.toReference (m_samples);
			}
		}
		catch (VisADException e)
		{
			e.printStackTrace ();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator iterator ()
	{
		return (new VisADSetIterator ());
	}

	/**
	 * Iterator over a {@link visad.Set VisAD set}.
	 * 
	 * @author Geopod Team
	 * 
	 */
	class VisADSetIterator
			implements Iterator
	{
		private int m_sampleIndex;
		private int m_numSamples;
		private float[] m_sample;

		/**
		 * Constructor.
		 */
		VisADSetIterator ()
		{
			m_sampleIndex = 0;
			m_sample = new float[m_set.getDimension ()];
			m_numSamples = 0;
			try
			{
				m_numSamples = m_set.getLength ();
			}
			catch (VisADException e)
			{
				throw (new RuntimeException ("Geopod: Can't obtain the number of samples"));
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasNext ()
		{
			return (m_sampleIndex < m_numSamples);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public float[] next ()
		{
			for (int i = 0; i < m_set.getDimension (); ++i)
			{
				m_sample[i] = m_samples[i][m_sampleIndex];
			}
			++m_sampleIndex;

			return (m_sample);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void remove ()
		{
			throw (new UnsupportedOperationException ());
		}
	}
}
