package geopod.gui.panels.datadisplay;

import geopod.gui.DisplayPanelManager;
import geopod.gui.GridEntry;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

/**
 * This class is essential to supporting drag and drop operations between
 * {@link SensorDisplayPanel}
 * 
 * @author Geopod Team
 * 
 */
public class DataChoiceTransferHandler
		extends TransferHandler
{
	private static final long serialVersionUID = 6669929112720270039L;
	private DataFlavor m_panelFlavor;
	private DataFlavor m_gridFlavor;

	public DataChoiceTransferHandler (String property)
	{
		String canonicalName = SensorDisplayPanel.class.getCanonicalName ();
		try
		{
			// It is vital to ensure this class's class loader loads the
			// DataFlavor class as well
			m_panelFlavor = new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType + ";class=" + canonicalName, "Panel",
					this.getClass ().getClassLoader ());

			canonicalName = GridEntry.class.getCanonicalName ();
			m_gridFlavor = new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType + ";class=" + canonicalName, "Grid",
					this.getClass ().getClassLoader ());
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace ();
		}
	}

	@Override
	public Transferable createTransferable (JComponent component)
	{
		if (component instanceof JTextField)
		{
			// Grab the source grid cell and the display panel associated with that cell
			JTextField sourceGridCell = (JTextField) component;
			SensorDisplayPanel sourceDisplayPanel = (SensorDisplayPanel) sourceGridCell.getParent ();

			// The drag has been initiated, show borders on all cells
			DisplayPanelManager manager = sourceDisplayPanel.getDisplayManager ();
			manager.showBorders ();

			// Grab the GridEntry associated with the source grid cell
			GridEntry sourceGridEntry = sourceDisplayPanel.getGridEntry (sourceGridCell);

			return (new TextTransferable (sourceDisplayPanel, sourceGridEntry));
		}
		return (null);
	}

	@Override
	public boolean canImport (TransferSupport support)
	{
		// Check for panel flavor
		if (!support.isDataFlavorSupported (m_panelFlavor))
		{
			return (false);
		}
		return (true);

	}

	@Override
	public int getSourceActions (JComponent c)
	{
		return (COPY);
	}

	@Override
	public boolean importData (TransferSupport support)
	{
		if (!canImport (support))
		{
			return (false);
		}

		JTextField destinationGridCell = (JTextField) support.getComponent ();
		SensorDisplayPanel destinationPanel = (SensorDisplayPanel) destinationGridCell.getParent ();
		GridEntry destinationGridEntry = destinationPanel.getGridEntry (destinationGridCell);

		Transferable transferable = support.getTransferable ();

		try
		{
			GridEntry sourceGridEntry = (GridEntry) transferable.getTransferData (m_gridFlavor);
			SensorDisplayPanel sourcePanel = (SensorDisplayPanel) transferable.getTransferData (m_panelFlavor);

			if (destinationPanel == sourcePanel)
			{
				// Both the source and destination panels are the same, simply swap the cell contents
				destinationPanel.swapGridCellContents (destinationGridEntry, sourceGridEntry);
				destinationPanel.updateDisplay ();
			}
			else
			{
				// Manually swap mappings across the two panels & update both displays
				String sourceParameterName = sourcePanel.getMappedParameterName (sourceGridEntry);
				String destinationParamName = destinationPanel.getMappedParameterName (destinationGridEntry);

				sourcePanel.setParameterMapping (sourceGridEntry, destinationParamName);
				destinationPanel.setParameterMapping (destinationGridEntry, sourceParameterName);

				sourcePanel.updateDisplay ();
				destinationPanel.updateDisplay ();
			}
			// The Swap is finished, hide all borders
			DisplayPanelManager manager = sourcePanel.getDisplayManager ();
			manager.hideBorders ();
		}
		catch (UnsupportedFlavorException e)
		{
			e.printStackTrace ();
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}
		return (true);
	}

	/**
	 * This class is also necessary for drag and drop support.
	 * 
	 * @author Geopod Team
	 */
	private static class TextTransferable
			implements Transferable
	{
		DataFlavor[] m_flavors;
		GridEntry m_gridEntry;
		SensorDisplayPanel m_panel;

		/**
		 * This constructor initializes both the GridEntry and
		 * SensorDisplayPanel dataflavors.
		 * 
		 * @param sensorDisplay
		 *            - the {@link SensorDisplayPanel} to transport
		 * @param entry
		 *            - the {@link GridEntry} to transport
		 */
		public TextTransferable (SensorDisplayPanel sensorDisplay, GridEntry entry)
		{
			m_flavors = new DataFlavor[2];
			m_panel = sensorDisplay;
			m_gridEntry = entry;

			String canonicalName = SensorDisplayPanel.class.getCanonicalName ();
			try
			{
				m_flavors[0] = new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType + ";class=" + canonicalName,
						"Panel", this.getClass ().getClassLoader ());

				canonicalName = GridEntry.class.getCanonicalName ();
				m_flavors[1] = new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType + ";class=" + canonicalName,
						"Grid", this.getClass ().getClassLoader ());
			}
			catch (ClassNotFoundException e1)
			{
				e1.printStackTrace ();
			}
		}

		@Override
		public DataFlavor[] getTransferDataFlavors ()
		{
			return (m_flavors);
		}

		@Override
		public boolean isDataFlavorSupported (DataFlavor flavor)
		{
			for (DataFlavor flav : m_flavors)
			{
				if (flav == flavor)
				{
					return (true);
				}
			}
			return (false);
		}

		@Override
		public Object getTransferData (DataFlavor flavor)
				throws UnsupportedFlavorException
		{
			if (flavor.equals (m_flavors[0]))
			{
				return (m_panel);
			}
			else
			{
				return (m_gridEntry);
			}
		}
	}
}
