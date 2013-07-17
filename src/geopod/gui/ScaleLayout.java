package geopod.gui;

import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

/**
 * An absolute layout manager that performs component layout and scaling using a
 * reference frame. Typically components will be added with a {@link Rectangle}
 * object as constraints. This functionality allows for precise placement of
 * components in terms of an alternate coordinate system.
 * 
 * @author Geopod Team
 * 
 */
public class ScaleLayout
		implements LayoutManager2
{

	private Dimension m_referenceFrame;
	private Map<Component, Rectangle> m_constraintsMap;

	public ScaleLayout (Dimension referenceFrame)
	{
		m_referenceFrame = new Dimension (referenceFrame);
		m_constraintsMap = new HashMap<Component, Rectangle> ();
	}

	@Override
	public void addLayoutComponent (String name, Component component)
	{
		// We don't associate strings with components so we do nothing here
	}

	@Override
	public void addLayoutComponent (Component comp, Object constraints)
	{
		if (constraints instanceof Rectangle)
		{
			m_constraintsMap.put (comp, (Rectangle) constraints);
		}
		else
		{
			System.out.println ("ScaleLayout.addLayoutComponent: Attempt to add non-rectangle");
		}
	}

	@Override
	public void removeLayoutComponent (Component component)
	{
	}

	private void computeAndSetSizes (Container container)
	{
		int numComponents = container.getComponentCount ();
		for (int i = 0; i < numComponents; ++i)
		{
			Component component = container.getComponent (i);

			Rectangle refBounds = m_constraintsMap.get (component);
			if (refBounds == null)
			{
				// No reference bounds, so skip the component
				continue;
			}

			Rectangle scaledBounds = this.computeScaledBounds (container, refBounds);
			if (component instanceof JXCollapsiblePane)
			{
				JXCollapsiblePane cp = (JXCollapsiblePane) component;

				if (cp.getDirection ().equals (Direction.LEFT))
				{
					component.setLocation (scaledBounds.x, scaledBounds.y);
					// Preserve width
					component.setSize (component.getWidth (), scaledBounds.height);
				}
				else if (cp.getDirection ().equals (Direction.DOWN))
				{
					component.setLocation (scaledBounds.x,
							(scaledBounds.y + scaledBounds.height) - component.getHeight ());
					// Preserve height
					component.setSize (scaledBounds.width, component.getPreferredSize ().height);
				}
			}
			else
			{
				component.setBounds (scaledBounds);
			}
		}
	}

	/**
	 * Returns a scaled version of the specified reference bounds
	 * 
	 * @param container
	 * @param refBounds
	 * @return the scaled bounds
	 */
	private Rectangle computeScaledBounds (Container container, Rectangle refBounds)
	{
		float scaleX = (float) container.getWidth () / m_referenceFrame.width;
		int newX = (int) (refBounds.x * scaleX);
		int newWidth = (int) (refBounds.width * scaleX);
		float scaleY = (float) container.getHeight () / m_referenceFrame.height;
		int newY = (int) (refBounds.y * scaleY);
		int newHeight = (int) (refBounds.height * scaleY);

		return (new Rectangle (newX, newY, newWidth, newHeight));
	}

	@Override
	public Dimension preferredLayoutSize (Container container)
	{
		this.computeAndSetSizes (container);

		return (container.getSize ());
	}

	@Override
	public Dimension minimumLayoutSize (Container container)
	{
		Debug.println (DebugLevel.MEDIUM, "ScaleLayout.minimumLayoutSize: Invoked");

		return (this.preferredLayoutSize (container));
	}

	@Override
	public Dimension maximumLayoutSize (Container container)
	{
		Debug.println (DebugLevel.MEDIUM, "ScaleLayout.maximumLayoutSize: Invoked");

		return (null);
	}

	/*
	 * This is called when the panel is first displayed, and every time its size
	 * changes. Note: You CAN'T assume preferredLayoutSize or minimumLayoutSize
	 * will be called -- in the case of applets, at least, they probably won't
	 * be.
	 */
	@Override
	public void layoutContainer (Container container)
	{
		Debug.println (DebugLevel.HIGH, "ScaleLayout.layoutContainer: Invoked");

		this.computeAndSetSizes (container);
	}

	@Override
	public String toString ()
	{
		String string = String.format ("%s; Ref Frame: %s", getClass (), m_referenceFrame.toString ());

		return (string);
	}

	//**

	@Override
	public float getLayoutAlignmentX (Container container)
	{
		return (0);
	}

	@Override
	public float getLayoutAlignmentY (Container container)
	{
		return (0);
	}

	@Override
	public void invalidateLayout (Container container)
	{
	}
}
