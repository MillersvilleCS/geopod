package geopod.gui.panels.dropsonde;

import geopod.constants.parameters.IDV4ParameterConstants;
import geopod.constants.parameters.ParameterUtil;
import geopod.devices.Dropsonde;
import geopod.gui.styles.GeopodTabbedPaneUI;

import java.awt.BorderLayout;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ucar.visad.quantities.CommonUnits;
import visad.FlatField;
import visad.Unit;
import visad.VisADException;

/**
 * A panel for displaying data charts using data from a dropsonde. Displays
 * parameters on a separate tabs, with some parameters sharing a tab.
 * 
 */
public class DropsondePanel
		extends JTabbedPane
{
	private static final long serialVersionUID = 18001267516642637L;

	/**
	 * Construct a dropsonde panel.
	 * 
	 * @param ds
	 *            - the dropsonde to display data from.
	 */
	public DropsondePanel (Dropsonde ds)
	{
		super ();
		super.setUI (new GeopodTabbedPaneUI ());

		constructDataTabs (ds);
	}

	/**
	 * Construct tabs containing chart panels.
	 * 
	 * @param ds
	 *            - the dropsonde to create charts for.
	 * @throws RuntimeException
	 */
	private void constructDataTabs (Dropsonde ds)
			throws RuntimeException
	{
		try
		{
			/*String[] names = { "Temperature @ isobaric", "Dewpoint (from Temperature & Relative_humidity)",
					"Temperature @ pressure", "Dew_point_temperature @ pressure" }; */
			//List<String> names = IDV4ParameterConstants.getDropsondeTDpParameters ();
			List<String> names = ParameterUtil.getDropsondeTDpParameters ();
			ChartPanel chart = createChartPanel (names, "M", "Celsius", ds, true);
			if (chart != null)
			{
				JPanel containerPanel = new JPanel (new BorderLayout ());
				containerPanel.add (chart, BorderLayout.CENTER);
				this.addTab ("T/Dew", containerPanel);
			}
		}
		catch (VisADException e)
		{
			// Do not add this tab.
		}

		try
		{
			/*String[] names = { "u_wind @ isobaric", "v_wind @ isobaric", "U-component_of_wind @ pressure",
					"V-component_of_wind @ pressure" }; */
			//List<String> names = IDV4ParameterConstants.getDropsondeUVWindParameters ();
			List<String> names = ParameterUtil.getDropsondeUVWindParameters ();
			ChartPanel chart = createChartPanel (names, "M", "M/s", ds, false);
			if (chart != null)
			{
				JPanel containerPanel = new JPanel (new BorderLayout ());
				containerPanel.add (chart, BorderLayout.CENTER);
				this.addTab ("Wind", containerPanel);
			}
		}
		catch (VisADException e)
		{
			// Do not add this tab.
		}

		try
		{
			/* String[] names = { "Relative_humidity @ isobaric", "Relative_humidity @ pressure" }; */
			//List<String> names = IDV4ParameterConstants.getDropsondeRHParameters ();
			List<String> names = ParameterUtil.getDropsondeRHParameters ();
			ChartPanel chart = createChartPanel (names, "M", "%", ds, false);
			if (chart != null)
			{
				JPanel containerPanel = new JPanel (new BorderLayout ());
				containerPanel.add (chart, BorderLayout.CENTER);
				this.addTab ("RH", containerPanel);
			}
		}
		catch (VisADException e)
		{
			// Do not add this tab.
		}

		try
		{
			// KW: This parameter is also called theta-e
			/*String[] names = { "Equiv. Potential Temperature (from Temperature & Relative_humidity)",
					"Potential_temperature @ height_above_ground" }; */
			//List<String> names = IDV4ParameterConstants.getDropsondeThetaEParameters ();
			List<String> names = ParameterUtil.getDropsondeThetaEParameters ();
			ChartPanel chart = createChartPanel (names, "M", "Celsius", ds, false);
			if (chart != null)
			{
				JPanel containerPanel = new JPanel (new BorderLayout ());
				containerPanel.add (chart, BorderLayout.CENTER);
				this.addTab ("Theta-e", containerPanel);
			}
		}
		catch (VisADException e)
		{
			// Do not add this tab.
		}
	}

	/**
	 * Create a chart panel with the given parameter names. Converts all
	 * temperatures to celsius.
	 * 
	 * @param names
	 * @param xLabel
	 * @param yLabel
	 * @param ds
	 * @param convertUnits
	 *            - convert temps to celsius or not
	 * @return
	 * @throws VisADException
	 */
	private ChartPanel createChartPanel (List<String> names, String xLabel, String yLabel, Dropsonde ds,
			boolean convertUnits)
			throws VisADException
	{
		boolean hasData = false;

		XYSeriesCollection seriesCollection = new XYSeriesCollection ();

		for (String name : names)
		{
			FlatField ff = ds.getData (name);

			if (ff != null)
			{
				visad.Set domainSet = ff.getDomainSet ();

				float[] domainData = domainSet.getSamples ()[0];
				float[] rangeData = ff.getFloats (false)[0];

				// if the data range contains empty values (NaNs), do not create this chart
				if (Double.isNaN (rangeData[0]))
				{
					// Do nothing and skip to the next parameter.
				}
				else
				{
					hasData = true;

					Unit rangeUnit = ff.getRangeUnits ()[0][0];

					XYSeries dataSeries = new XYSeries (name);

					// Ensure all temperatures are in Celcius.
					if (convertUnits && rangeUnit.isConvertible (CommonUnits.CELSIUS))
					{
						for (int i = 0; i < domainData.length; ++i)
						{
							float x = domainData[i];
							float y = (float) rangeUnit.toThat (rangeData[i], CommonUnits.CELSIUS);
							dataSeries.add (x, y);
						}
					}
					else
					{
						for (int i = 0; i < domainData.length; ++i)
						{
							float x = domainData[i];
							float y = rangeData[i];
							dataSeries.add (x, y);
						}
					}

					seriesCollection.addSeries (dataSeries);
				}
			}
		}

		if (hasData)
		{
			boolean useLegand = names.size () > 1;

			JFreeChart chart = ChartFactory.createXYLineChart (null, xLabel, yLabel, seriesCollection,
					PlotOrientation.HORIZONTAL, useLegand, true, false);
			chart.setTextAntiAlias (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

			if (ds.isUsingPressureForDomain ())
			{
				XYPlot plot = chart.getXYPlot ();
				ValueAxis domainAxis = new LogarithmicAxis ("hPa");
				domainAxis.setLowerBound (100);
				domainAxis.setUpperBound (1050);
				domainAxis.setInverted (true);
				plot.setDomainAxis (domainAxis);
			}

			return (new DropsondeChartPanel (chart));
		}

		// No data found.
		return (null);
	}
}
