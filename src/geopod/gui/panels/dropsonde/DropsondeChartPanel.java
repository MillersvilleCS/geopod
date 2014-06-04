package geopod.gui.panels.dropsonde;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

public class DropsondeChartPanel
		extends ChartPanel
{
	private static final long serialVersionUID = -4588642046623226146L;

	public DropsondeChartPanel (JFreeChart chart)
	{
		super (chart);
	}

	@Override
	public void mouseClicked (MouseEvent e)
	{
		if (e.getButton () == MouseEvent.BUTTON1)
		{
			JDialog axisChangeDialog = new AxisChangeDialog (getChart ().getXYPlot ());
			axisChangeDialog.setVisible (true);
			axisChangeDialog.setLocationRelativeTo (this);
		}
	}

	private class AxisChangeDialog
			extends JDialog
	{

		private static final long serialVersionUID = -9067530679915147938L;

		private JCheckBox autoRangeBox;

		private JTextField xMinTextField;
		private JTextField xMaxTextField;
		private JTextField yMinTextField;
		private JTextField yMaxTextField;

		private JButton okButton;
		private JButton cancelButton;

		protected XYPlot xyPlot;

		public AxisChangeDialog (XYPlot xyPlot)
		{
			super ();
			setTitle ("Dropsonde Chart Options");

			autoRangeBox = new JCheckBox ("Enable auto-axis scaling", true);
			autoRangeBox.addActionListener (new ActionListener ()
			{
				@Override
				public void actionPerformed (ActionEvent e)
				{
					xMinTextField.setEnabled (!autoRangeBox.isSelected ());
					xMaxTextField.setEnabled (!autoRangeBox.isSelected ());
					yMinTextField.setEnabled (!autoRangeBox.isSelected ());
					yMaxTextField.setEnabled (!autoRangeBox.isSelected ());
				}
			});

			this.xyPlot = xyPlot;
			ValueAxis xAxis = xyPlot.getRangeAxis ();
			ValueAxis yAxis = xyPlot.getDomainAxis ();

			xMinTextField = new JTextField (Double.toString (xAxis.getLowerBound ()));
			xMinTextField.setEnabled (false);

			xMaxTextField = new JTextField (Double.toString (xAxis.getUpperBound ()));
			xMaxTextField.setEnabled (false);

			yMinTextField = new JTextField (Double.toString (yAxis.getLowerBound ()));
			yMinTextField.setEnabled (false);

			yMaxTextField = new JTextField (Double.toString (yAxis.getUpperBound ()));
			yMaxTextField.setEnabled (false);

			okButton = new JButton ("Ok");
			okButton.addActionListener (new ActionListener ()
			{

				@Override
				public void actionPerformed (ActionEvent e)
				{
					XYPlot xyPlot = getXYPlot ();
					ValueAxis xAxis = xyPlot.getRangeAxis ();
					ValueAxis yAxis = xyPlot.getDomainAxis ();
					if (autoRangeBox.isSelected ())
					{
						xAxis.setAutoRange (true);
						yAxis.setAutoRange (true);
					}
					else
					{
						xAxis.setRange (Double.valueOf (xMinTextField.getText ()),
								Double.valueOf (xMaxTextField.getText ()));
						yAxis.setRange (Double.valueOf (yMinTextField.getText ()),
								Double.valueOf (yMaxTextField.getText ()));
					}

					dispose ();
				}
			});

			cancelButton = new JButton ("Cancel");
			cancelButton.addActionListener (new ActionListener ()
			{

				@Override
				public void actionPerformed (ActionEvent e)
				{
					dispose ();
				}
			});

			setLayout (new GridBagLayout ());

			GridBagConstraints constraints = new GridBagConstraints ();
			constraints.fill = GridBagConstraints.BOTH;

			constraints.gridx = 0;
			constraints.gridy = 0;
			add (new JLabel ("X-Axis Minimum"), constraints);

			constraints.gridx++;
			add (new JLabel ("X-Axis Maximum"), constraints);

			constraints.gridx = 0;
			constraints.gridy++;
			add (xMinTextField, constraints);

			constraints.gridx++;
			add (xMaxTextField, constraints);

			constraints.gridx = 0;
			constraints.gridy++;
			add (new JLabel ("Y-Axis Maximum"), constraints);

			constraints.gridx++;
			add (new JLabel ("Y-Axis Minimum"), constraints);

			constraints.gridx = 0;
			constraints.gridy++;
			add (yMinTextField, constraints);

			constraints.gridx++;
			add (yMaxTextField, constraints);

			constraints.gridx = 0;
			constraints.gridy++;
			constraints.gridwidth = 2;
			add (autoRangeBox, constraints);

			constraints.gridy++;
			constraints.gridwidth = 1;
			add (okButton, constraints);

			constraints.gridx++;
			add (cancelButton, constraints);

			pack ();
		}

		public XYPlot getXYPlot ()
		{
			return xyPlot;
		}
	}
}
