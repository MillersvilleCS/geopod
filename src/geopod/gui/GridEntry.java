package geopod.gui;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A GridEntry maintains both a row and column value
 * 
 * @author Geopod Team
 * 
 */
public class GridEntry
		implements Serializable
{
	private static final long serialVersionUID = -8267452851068572954L;
	private int m_row;
	private int m_col;

	/**
	 * Constructs a GridEntry with the specified row and column
	 * 
	 * @param row
	 *            the row value
	 * @param col
	 *            the column value
	 */
	public GridEntry (int row, int col)
	{
		setRow (row);
		setCol (col);
	}

	/**
	 * Sets the row of this GridEntry to the specified row value
	 * 
	 * @param row
	 *            a value indicating the row
	 */
	public void setRow (int row)
	{
		m_row = row;
	}

	/**
	 * Returns the current row value in this GridEntry
	 * 
	 * @return the row value
	 */
	public int getRow ()
	{
		return (m_row);
	}

	/**
	 * Sets the column of this GridEntry to the specified column value
	 * 
	 * @param col
	 *            a value indicating the column
	 */
	public void setCol (int col)
	{
		m_col = col;
	}

	/**
	 * Returns the current column value in this GridEntry
	 * 
	 * @return the column value
	 */
	public int getCol ()
	{
		return (m_col);
	}

	public String toString ()
	{
		return ("Row: " + getRow () + "Col: " + getCol ());
	}

	public boolean equals (Object other)
	{
		if (other == null)
		{
			return (false);
		}
		else if (this == other)
		{
			// Self equality
			return (true);
		}
		else if (other instanceof GridEntry)
		{
			// Safe to do cast and compare fields
			GridEntry t2 = (GridEntry) other;
			return (this.m_row == t2.getRow () && this.m_col == t2.getCol ());
		}
		// Other is not a GridEntry.
		return (false);
	}

	public int hashCode ()
	{
		long bits = 1L;
		bits = 31L * bits + (long) m_row;
		bits = 31L * bits + (long) m_col;
		return ((int) (bits ^ (bits >> 32)));
	}

	/**
	 * Returns a comparator that can be used to compare two {@link GridEntry}
	 * 
	 * @return the GridEntry comparator
	 */
	public static Comparator<GridEntry> getComparator ()
	{
		return new Comparator<GridEntry> ()
		{
			@Override
			public int compare (GridEntry o1, GridEntry o2)
			{
				// Check columns first
				if (o1.getCol () != o2.getCol ())
				{
					return (o1.getCol () - o2.getCol ());
				}
				else
				{
					// Then compare rows
					return (o1.getRow () - o2.getRow ());
				}
			}
		};
	}
}