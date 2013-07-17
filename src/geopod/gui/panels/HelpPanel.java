package geopod.gui.panels;

import geopod.constants.UIConstants;
import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.components.BorderFactory;
import geopod.gui.components.ButtonFactory;
import geopod.gui.components.PainterFactory;
import geopod.gui.styles.GeopodTabbedPaneUI;
import geopod.utils.web.WebUtility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

public class HelpPanel
		extends JXPanel
		implements ISubject
{
	private static final long serialVersionUID = 4221258030278550685L;

	private JTabbedPane m_tabbedPane;

	private SubjectImpl m_subjectImpl;

	public HelpPanel ()
	{
		m_subjectImpl = new SubjectImpl ();

		LayoutManager layout = new MigLayout ();
		super.setLayout (layout);
		super.setBorder (BorderFactory.createStandardBorder ());

		Color[] colors = { Color.DARK_GRAY, Color.LIGHT_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY };
		float[] fractions = { 0.0f, 0.2f, 0.8f, 1.0f };
		MattePainter painter = PainterFactory.createMattePainter (0, 0, 310, 330, fractions, colors);
		super.setBackgroundPainter (painter);

		JXLabel header = createHeader ();
		super.add (header, "pushx, alignx center, wrap");

		m_tabbedPane = new JTabbedPane ();
		m_tabbedPane.setUI (new GeopodTabbedPaneUI ());

		JPanel controlsPanel = this.createControlPanel ();
		m_tabbedPane.addTab ("Controls", null, controlsPanel);

		JPanel resourcesPanel = this.createResourcesPanel ();
		m_tabbedPane.addTab ("Resources", null, resourcesPanel);

		JPanel aboutPanel = this.createAboutPanel ();
		m_tabbedPane.addTab ("About", null, aboutPanel);

		super.add (m_tabbedPane, "growx, wrap");

		JButton closeButton = createFooter ();
		super.add (closeButton, "alignx center");
	}

	private JXLabel createHeader ()
	{
		JXLabel helpLabel = new JXLabel ("HELP");
		Font font = UIConstants.GEOPOD_BANDY;
		helpLabel.setFont (font.deriveFont (UIConstants.TITLE_SIZE));
		helpLabel.setForeground (Color.black);

		return (helpLabel);
	}

	private JPanel createAboutPanel ()
	{
		JPanel aboutPanel = new JPanel (new BorderLayout ());
		JEditorPane aboutPane = new JEditorPane ();
		aboutPane.setEditable (false);

		try
		{
			URL aboutPage = this.getClass ().getResource ("//Resources/Web/about.html");
			aboutPane.setContentType ("text/html");
			aboutPane.setPage (aboutPage);

		}
		catch (IOException e2)
		{
			e2.printStackTrace ();
		}

		aboutPanel.add (aboutPane, BorderLayout.CENTER);

		// Vertical and horizontal scroll bars as needed are default
		JScrollPane scrollPane = new JScrollPane (aboutPane);
		aboutPanel.add (scrollPane);

		return (aboutPanel);
	}

	private JPanel createResourcesPanel ()
	{
		JPanel resourcesPanel = new JPanel ();
		resourcesPanel.setLayout (new BorderLayout ());

		JEditorPane resourcesPane = new JEditorPane ();
		resourcesPane.setEditable (false);
		resourcesPane.addHyperlinkListener (new LinkListener ());

		try
		{
			URL resourcesPage = this.getClass ().getResource ("//Resources/Web/resources.html");
			resourcesPane.setContentType ("text/html");
			resourcesPane.setPage (resourcesPage);
		}
		catch (IOException e2)
		{
			e2.printStackTrace ();
		}

		JScrollPane scrollPane = new JScrollPane (resourcesPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resourcesPanel.add (scrollPane);

		return (resourcesPanel);
	}

	private JPanel createControlPanel ()
	{
		JPanel controlPanel = new JPanel (new BorderLayout ());
		JEditorPane controlPane = new JEditorPane ();
		controlPane.setEditable (false);

		try
		{
			URL controlPage = this.getClass ().getResource ("//Resources/Web/controls.html");
			controlPane.setContentType ("text/html");
			controlPane.setPage (controlPage);
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}

		controlPanel.add (controlPane, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane (controlPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlPanel.add (scrollPane);

		return (controlPanel);
	}

	private JButton createFooter ()
	{
		JButton closeButton = ButtonFactory.createGradientButton (UIConstants.BUTTON_FONT_SIZE,
				UIConstants.GEOPOD_GREEN, false);
		closeButton.setText ("CLOSE");

		closeButton.addActionListener (new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				HelpPanel.this.setVisible (false);
				notifyObservers (GeopodEventId.HELP_BUTTON_STATE_CHANGED);
			}
		});

		return (closeButton);
	}

	private static final class LinkListener
			implements HyperlinkListener
	{
		@Override
		public void hyperlinkUpdate (HyperlinkEvent linkEvent)
		{
			if (linkEvent.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
			{
				WebUtility.browse (linkEvent);
			}
		}
	}

	@Override
	public void addObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.addObserver (observer, eventId);
	}

	@Override
	public void removeObserver (IObserver observer, GeopodEventId eventId)
	{
		m_subjectImpl.removeObserver (observer, eventId);
	}

	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subjectImpl.notifyObservers (eventId);
	}

	@Override
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}
}
