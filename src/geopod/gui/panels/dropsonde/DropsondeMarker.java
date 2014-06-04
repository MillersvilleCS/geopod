package geopod.gui.panels.dropsonde;


import geopod.utils.coordinate.IdvCoordinateUtility;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import com.sun.j3d.utils.geometry.Box;

public class DropsondeMarker
		extends BranchGroup
{
	private static final float BOX_SIDE_LENGTH = 0.005f;
	private static final float HALF_WORLD_HEIGHT_IN_BOX_COORDS = 0.5f;
	private static final float WORLD_HEIGHT_IN_BOX_COORDS = 1f;
	private static final Appearance BASE_APPERANCE, FOCUS_APPERANCE;
	
	private Box boxShape;
	private Shape3D lineShape;
	static
	{
		BASE_APPERANCE = genAppearance ( new Color3f (1, 1, 0));
		FOCUS_APPERANCE = genAppearance ( new Color3f (1, 0, 0));
	}

	public DropsondeMarker (Vector3d position)
	{
		setCapability (ALLOW_DETACH);
		setCapability (BranchGroup.ALLOW_PARENT_READ);
		
		addChild (createBox (position));
		addChild (createLine (position));
	}
	public void focus ()
	{
		lineShape.setAppearance (FOCUS_APPERANCE);
		boxShape.setAppearance (FOCUS_APPERANCE);
	}
	public void unFocus ()
	{
		lineShape.setAppearance (BASE_APPERANCE);
		boxShape.setAppearance (BASE_APPERANCE);
	}
	private TransformGroup createBox (Vector3d position)
	{
		boxShape = new Box (BOX_SIDE_LENGTH, BOX_SIDE_LENGTH, BOX_SIDE_LENGTH, BASE_APPERANCE);
		for (int i = 0; i < 6; ++i)
		{
			boxShape.getChild (i).setCapability (Shape3D.ALLOW_APPEARANCE_WRITE);
		}
		boxShape.setCapability (Box.ENABLE_APPEARANCE_MODIFY);
		Transform3D boxTransform = new Transform3D ();
		boxTransform.setTranslation (position);

		TransformGroup dropsondeBoxGroup = new TransformGroup ();
		dropsondeBoxGroup.setTransform (boxTransform);
		dropsondeBoxGroup.addChild (boxShape);

		return dropsondeBoxGroup;
	}

	private TransformGroup createLine (Vector3d position)
	{
		TransformGroup dropsondeLineGroup = new TransformGroup ();

		float lineZ = (float) (position.getZ () + HALF_WORLD_HEIGHT_IN_BOX_COORDS);

		LineArray line = new LineArray (2, LineArray.COORDINATES);
		line.setCoordinate (0, new Point3f ((float) position.getX (), (float) position.getY (), lineZ
				- HALF_WORLD_HEIGHT_IN_BOX_COORDS));
		Point3d coords = IdvCoordinateUtility.convertBoxToWorld (new Point3d (0f, 0f, -WORLD_HEIGHT_IN_BOX_COORDS));
		line.setCoordinate (1, new Point3d (position.getX (), position.getY (), coords.getZ ()));

		lineShape = new Shape3D (line, BASE_APPERANCE);
		lineShape.setCapability (Shape3D.ALLOW_APPEARANCE_WRITE);
		dropsondeLineGroup.addChild (lineShape);

		return dropsondeLineGroup;
	}

	private static Appearance genAppearance (Color3f color)
	{
		Appearance app = new Appearance ();
		int shadeModel = ColoringAttributes.NICEST;
		ColoringAttributes coloringAttributes = new ColoringAttributes (color, shadeModel);
		app.setColoringAttributes (coloringAttributes);
		RenderingAttributes ra = new RenderingAttributes ();
		ra.setVisible (false);
		app.setRenderingAttributes (ra);
		
		return app;
	}
}
