package geopod.gui.panels;

import geopod.gui.components.BorderFactory;
import geopod.gui.components.PainterFactory;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

public class LoadingPanel
		extends JXPanel
{
	private static final long serialVersionUID = -321424431872575312L;

	private JLabel m_loadingLabel;
	private JLabel m_dataSourceLabel;

	public LoadingPanel ()
	{
		setupLayout ();
		setupPanelBackground ();

		m_dataSourceLabel = new JLabel ("", JLabel.CENTER);
		super.add (m_dataSourceLabel, BorderLayout.NORTH);

		m_loadingLabel = new JLabel ("Loading: ", JLabel.CENTER);
		super.add (m_loadingLabel, BorderLayout.CENTER);
	}

	private void setupLayout ()
	{
		super.setLayout (new BorderLayout ());
	}

	private void setupPanelBackground ()
	{
		this.setBorder (BorderFactory.createStandardBorder ());

		Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY };
		float[] fractions = { 0.0f, 0.2f, 0.8f, 1.0f };
		MattePainter painter = PainterFactory.createMattePainter (0, 0, 420, 160, fractions, colors);
		super.setBackgroundPainter (painter);
	}

	public void showDataLoading (String dataChoiceName, String dataSourceName)
	{
		if (dataChoiceName != null && dataSourceName != null)
		{
			if (!super.isVisible ())
			{
				// Loading panel is invisible initially, so
				//   we'll set the data source name once
				this.setDataSourceName (dataSourceName);
				this.setVisible (true);
			}
			this.setDataChoiceNameBeingLoaded (dataChoiceName);
			this.requestFocusInWindow ();
		}
		else
		{
			//this.setDataChoiceNameBeingLoaded ("Finished. Click here to close.");
			this.setVisible (false);
		}
	}

	private void setDataSourceName (String dataSource)
	{
		m_dataSourceLabel.setText ("Data source: " + dataSource);
	}

	private void setDataChoiceNameBeingLoaded (String choiceName)
	{
		m_loadingLabel.setText ("Loading: " + choiceName);
	}
}
