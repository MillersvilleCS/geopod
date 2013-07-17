package geopod.gui.panels.datadisplay;

import geopod.constants.UIConstants;
import geopod.gui.GridEntry;
import geopod.gui.components.GeopodTextField;
import geopod.gui.panels.ImagePanel;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;

/**
 * GeopodGridCellPanel is a container that maintains cells in a grid format and
 * supports drag and drop between cells.
 * 
 * @author Geopod Team
 * 
 */
public class GeopodGridCellPanel
		extends ImagePanel
{
	private static final long serialVersionUID = 4446980552339985126L;
	private static final int DEFAULT_ROW;
	private static final int DEFAULT_COL;
	private static final float DEFAULT_FONT_SIZE;
	private static final String PROPERTY_NAME;

	static
	{
		DEFAULT_ROW = 3;
		DEFAULT_COL = 3;
		DEFAULT_FONT_SIZE = 17.0f;
		PROPERTY_NAME = "text";
	}

	private List<JTextField> m_gridCells;
	private Font m_font;
	private GridLayout m_gridLayout;
	private TransferHandler m_transferHandler;
	private DragAndDropListener m_listener;

	/**
	 * Default GeopodGridCell constructor. This constructor initializes a grid
	 * layout and initial grid cells.
	 */
	public GeopodGridCellPanel ()
	{
		m_gridLayout = new GridLayout (DEFAULT_ROW, DEFAULT_COL);
		m_gridCells = new ArrayList<JTextField> ();
		m_transferHandler = new DataChoiceTransferHandler (PROPERTY_NAME);
		m_listener = new DragAndDropListener ();

		super.setOpaque (false);
		super.setLayout (m_gridLayout);
		int size = m_gridLayout.getRows () * m_gridLayout.getColumns ();
		setDefaultFont ();
		adjustGridCells (size);
		this.addGridCells ();
	}

	/**
	 * Returns the {@link GridEntry} associated with a given grid cell.
	 * 
	 * @param gridCell
	 *            the gridCell object
	 * @return the GridEntry object
	 */
	public GridEntry getGridEntry (JTextField gridCell)
	{
		int index = m_gridCells.indexOf (gridCell);

		int rows = index / m_gridLayout.getColumns ();
		int cols = index % m_gridLayout.getColumns ();

		return (new GridEntry (rows, cols));
	}

	/**
	 * Returns the grid cell associated with the {@link GridEntry}.
	 * 
	 * @param entry
	 *            the GridEntry object
	 * @return the grid cell object
	 */
	public JTextField getGridCell (GridEntry entry)
	{
		return (this.getGridCell (entry.getRow (), entry.getCol ()));
	}

	/**
	 * Returns the grid cell at a row and column
	 * 
	 * @param row
	 *            the row value
	 * @param col
	 *            the column value
	 * @return the grid cell object
	 */
	public JTextField getGridCell (int row, int col)
	{
		return (m_gridCells.get (m_gridLayout.getColumns () * row + col));
	}

	/**
	 * Returns the total number of cells in this grid
	 * 
	 * @return the current capacity
	 */
	public int getCurrentCapacity ()
	{
		return (m_gridCells.size ());
	}

	/**
	 * Returns the number of rows in this grid
	 * 
	 * @return the number of rows
	 */
	public int getRows ()
	{
		return (m_gridLayout.getRows ());
	}

	/**
	 * Returns the number of columns in this grid
	 * 
	 * @return the number of columns
	 */
	public int getColumns ()
	{
		return (m_gridLayout.getColumns ());
	}

	/**
	 * Sets the default font that is used for all grid cells.
	 */
	private void setDefaultFont ()
	{
		m_font = UIConstants.GEOPOD_VERDANA.deriveFont (DEFAULT_FONT_SIZE);
	}

	/**
	 * Sets the tool tip text associated with a grid cell.
	 * 
	 * @param entry
	 *            the {@link GridEntry} associated with the grid cell
	 * @param text
	 *            the tool tip text
	 */
	public void setCellToolTip (GridEntry entry, String text)
	{
		JTextField gridCell = this.getGridCell (entry);

		if (gridCell != null)
		{
			gridCell.setToolTipText (text);
		}
	}

	/**
	 * Sets the text of a grid cell
	 * 
	 * @param row
	 *            the row of the grid cell
	 * @param col
	 *            the column of the grid cell
	 * @param text
	 *            the text to be set
	 */
	public void setGridCellText (int row, int col, String text)
	{
		this.getGridCell (row, col).setText (text);
		this.getGridCell (row, col).setCaretPosition (0);
	}

	/**
	 * Sets the size of the grid
	 * 
	 * @param rows
	 *            the desired number of rows
	 * @param cols
	 *            the desired number of columns
	 */
	public void setGridSize (int rows, int cols)
	{
		int newSize = rows * cols;
		int currentSize = m_gridLayout.getColumns () * m_gridLayout.getRows ();

		this.adjustGridCells (newSize - currentSize);

		super.removeAll ();

		m_gridLayout = new GridLayout (rows, cols);
		super.setLayout (m_gridLayout);

		this.addGridCells ();

		SwingUtilities.invokeLater (new Runnable ()
		{
			@Override
			public void run ()
			{
				repaint ();
			}
		});
	}

	/**
	 * Sets the font for all cells in the grid
	 * 
	 * @param font
	 *            the desired {@link Font} for all grid cells
	 */
	public void setGridCellFont (Font font)
	{
		m_font = font;

		for (JTextField gridCell : m_gridCells)
		{
			gridCell.setFont (m_font);
		}
	}

	/**
	 * Sets the border for all cells in the grid
	 * 
	 * @param border
	 *            the desired {@link Border} for all grid cells
	 */
	public void setGridCellBorder (Border border)
	{
		for (JTextField gridCell : m_gridCells)
		{
			gridCell.setBorder (border);
		}
	}

	/**
	 * Adds grid cells to the panel
	 */
	private void addGridCells ()
	{
		for (JTextField gridCell : m_gridCells)
		{
			super.add (gridCell);
		}
	}

	/**
	 * Adjusts the grid cell count by either removing or creating additional
	 * grid cells
	 * 
	 * @param quantity
	 *            a positive number indicates an addition of quantity cells. A
	 *            negative quantity will remove cells.
	 */
	private void adjustGridCells (int quantity)
	{
		if (quantity < 0)
		{
			removeGridCells (-quantity);
		}
		else
		{
			for (int i = 0; i < quantity; ++i)
			{
				JTextField gridCell = new GeopodTextField ();
				gridCell.setFont (m_font);
				gridCell.setFocusable (false);
				gridCell.setOpaque (false);
				gridCell.setEditable (false);
				gridCell.setBorder (BorderFactory.createEmptyBorder ());

				gridCell.setTransferHandler (m_transferHandler);
				gridCell.addMouseListener (m_listener);

				m_gridCells.add (gridCell);
			}
		}
	}

	/**
	 * Removes grid cells from the grid
	 * 
	 * @param quantity
	 *            the number of cells to remove
	 */
	private void removeGridCells (int quantity)
	{
		int fromIndex = m_gridCells.size () - quantity;
		int toIndex = (m_gridCells.size ());

		m_gridCells.subList (fromIndex, toIndex).clear ();
	}

	/**
	 * A listener to initiate drag and drop
	 * 
	 * @author Geopod Team
	 * 
	 */
	private static class DragAndDropListener
			extends MouseAdapter
	{
		@Override
		public void mousePressed (MouseEvent evt)
		{
			JComponent comp = (JComponent) evt.getSource ();
			DataChoiceTransferHandler th = (DataChoiceTransferHandler) comp.getTransferHandler ();
			// Start the drag operation 
			th.exportAsDrag (comp, evt, TransferHandler.COPY);
		}
	}
}
