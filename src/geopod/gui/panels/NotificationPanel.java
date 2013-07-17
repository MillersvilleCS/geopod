package geopod.gui.panels;

import geopod.constants.UIConstants;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.PainterFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

public class NotificationPanel
		extends JXPanel
{
	private static final long serialVersionUID = 1L;

	private JLabel m_notificationTextLabel;
	private JLabel m_secondaryNotificationTextLabel;

	public NotificationPanel ()
	{
		setupLayout ();
		setupPanelBackground ();

		m_notificationTextLabel = new JLabel ("no notification", JLabel.CENTER);
		m_secondaryNotificationTextLabel = new JLabel ("", JLabel.CENTER);
		m_secondaryNotificationTextLabel.setVisible (false);

		Font font = UIConstants.GEOPOD_VERDANA.deriveFont (15.0f);
		m_notificationTextLabel.setFont (font);
		m_secondaryNotificationTextLabel.setFont (font);

		m_notificationTextLabel.setAlignmentX (0.5f);
		m_secondaryNotificationTextLabel.setAlignmentX (0.5f);

		this.add (Box.createVerticalGlue ());
		this.add (m_notificationTextLabel);
		this.add (m_secondaryNotificationTextLabel);
		this.add (Box.createVerticalGlue ());
	}

	private void setupLayout ()
	{
		super.setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
	}

	private void setupPanelBackground ()
	{
		this.setBorder (BorderFactory.createStandardBorder ());

		Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY };
		float[] fractions = { 0.0f, 0.2f, 0.8f, 1.0f };
		MattePainter painter = PainterFactory.createMattePainter (0, 0, 420, 160, fractions, colors);
		super.setBackgroundPainter (painter);
	}

	public void setNotificationText (String notificationText)
	{
		m_notificationTextLabel.setText (notificationText);
		m_secondaryNotificationTextLabel.setVisible (false);

		int newLineBreakpoint = notificationText.indexOf ('\n');
		if (newLineBreakpoint != -1)
		{
			String topLine = notificationText.substring (0, newLineBreakpoint);
			String bottomLine = notificationText.substring (newLineBreakpoint + 1);
			m_notificationTextLabel.setText (topLine);
			m_secondaryNotificationTextLabel.setText (bottomLine);
			m_secondaryNotificationTextLabel.setVisible (true);
		}
		else
		{
			FontMetrics metrics = m_notificationTextLabel.getFontMetrics (m_notificationTextLabel.getFont ());
			int textWidth = metrics.stringWidth (notificationText);
			if (textWidth > m_notificationTextLabel.getWidth ())
			{
				int splitPoint = notificationText.indexOf (' ', notificationText.length () / 2);
				if (splitPoint == -1)
				{
					splitPoint = notificationText.indexOf (' ');
				}

				if (splitPoint != -1)
				{
					String firstHalf = notificationText.substring (0, splitPoint);
					m_notificationTextLabel.setText (firstHalf);
					String secondHalf = notificationText.substring (splitPoint + 1);
					m_secondaryNotificationTextLabel.setText (secondHalf);
					m_secondaryNotificationTextLabel.setVisible (true);
				}
			}
		}

	}

	public String getNotificationText ()
	{
		return (m_notificationTextLabel.getText ());
	}

}
