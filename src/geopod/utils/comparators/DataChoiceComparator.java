package geopod.utils.comparators;

import java.io.Serializable;
import java.util.Comparator;

import ucar.unidata.data.DataChoice;

/**
 * Comparator for {@link DataChoice data choices}. Does string comparison
 * between the DataChoice descriptions.
 * 
 * @author Geopod Team
 * 
 */
public class DataChoiceComparator
		implements Comparator<DataChoice>, Serializable
{
	private static final long serialVersionUID = 6353274223502352133L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare (DataChoice choice1, DataChoice choice2)
	{
		String choice1Description = choice1.getDescription ();
		String choice2Description = choice2.getDescription ();

		return (choice1Description.compareTo (choice2Description));
	}
}
