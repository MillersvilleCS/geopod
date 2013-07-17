package geopod.utils.idv;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Cone;

public class ViewBranch
{
	private static final double HORIZONTAL_FOV;
	private static final double FRONT_CLIP_DISTANCE;
	private static final double BACK_CLIP_DISTANCE;

	static
	{
		HORIZONTAL_FOV = Math.PI / 2.0;
		FRONT_CLIP_DISTANCE = 1.0 / 256;
		BACK_CLIP_DISTANCE = 10.0;
	}

	private View m_view;
	private View m_topView;
	private BranchGroup m_viewBranch;
	private TransformGroup m_movementTransformGroup;
	private TransformGroup m_topViewTransformGroup;

	public ViewBranch ()
	{
		m_view = createView (View.VISIBILITY_DRAW_ALL, View.PERSPECTIVE_PROJECTION, FRONT_CLIP_DISTANCE,
				BACK_CLIP_DISTANCE);
		m_topView = createView (View.VISIBILITY_DRAW_INVISIBLE, View.PARALLEL_PROJECTION, -5, 10.0);
		m_viewBranch = createViewBranch (m_view, m_topView);
	}

	private View createView (int visibilityPolicy, int projection, double frontClipDistance, double backClipDistance)
	{
		View view = new View ();

		view.setVisibilityPolicy (visibilityPolicy);
		view.setProjectionPolicy (projection);

		view.setFrontClipPolicy (View.VIRTUAL_EYE);
		view.setBackClipPolicy (View.VIRTUAL_EYE);
		view.setFrontClipDistance (frontClipDistance);
		view.setBackClipDistance (backClipDistance);
		view.setFieldOfView (HORIZONTAL_FOV);

		PhysicalBody body = new PhysicalBody ();
		view.setPhysicalBody (body);
		PhysicalEnvironment environment = new PhysicalEnvironment ();
		view.setPhysicalEnvironment (environment);

		return (view);
	}

	private BranchGroup createViewBranch (View view, View topView)
	{
		// Create a group to manipulate the Geopod
		BranchGroup geopodViewBranch = new BranchGroup ();
		// Allow the view branch to be detached
		geopodViewBranch.setCapability (BranchGroup.ALLOW_DETACH);

		// Create movement transform group to move the Geopod
		m_movementTransformGroup = new TransformGroup ();
		m_movementTransformGroup.setCapability (TransformGroup.ALLOW_TRANSFORM_WRITE);
		m_movementTransformGroup.setCapability (Group.ALLOW_CHILDREN_EXTEND);
		m_movementTransformGroup.setCapability (Group.ALLOW_CHILDREN_WRITE);

		ViewPlatform viewPlatform = new ViewPlatform ();
		view.attachViewPlatform (viewPlatform);
		m_movementTransformGroup.addChild (viewPlatform);
		geopodViewBranch.addChild (m_movementTransformGroup);

		Appearance app = new Appearance ();
		Color3f color = new Color3f (1, 1, 0);
		int shadeModel = ColoringAttributes.NICEST;
		ColoringAttributes coloringAttributes = new ColoringAttributes (color, shadeModel);
		app.setColoringAttributes (coloringAttributes);
		RenderingAttributes ra = new RenderingAttributes ();
		// Ensure the cone is drawn in minimap by setting it invisible
		ra.setVisible (false);
		app.setRenderingAttributes (ra);
		Cone cone = new Cone (0.5f, 1.5f, app);
		TransformGroup geopodModelTransformGroup = new TransformGroup ();
		Transform3D modelTransform = new Transform3D ();

		modelTransform.setTranslation (new Vector3f (0, 0.1f, 0));
		modelTransform.rotX (-Math.PI / 2);
		modelTransform.setScale (0.10);

		geopodModelTransformGroup.setTransform (modelTransform);
		geopodModelTransformGroup.addChild (cone);

		m_topViewTransformGroup = new TransformGroup ();
		m_topViewTransformGroup.setCapability (TransformGroup.ALLOW_TRANSFORM_WRITE);

		geopodViewBranch.addChild (m_topViewTransformGroup);
		//m_topViewTransformGroup.addChild (geopodModelTransformGroup);

		m_movementTransformGroup.addChild (geopodModelTransformGroup);

		ViewPlatform topViewPlatform = new ViewPlatform ();
		topView.attachViewPlatform (topViewPlatform);
		m_topViewTransformGroup.addChild (topViewPlatform);

		return (geopodViewBranch);
	}

	public TransformGroup getViewTransformGroup ()
	{
		return (m_movementTransformGroup);
	}

	public TransformGroup getTopViewTransformGroup ()
	{
		return (m_topViewTransformGroup);
	}

	public void addCanvas (Canvas3D canvas)
	{
		m_view.addCanvas3D (canvas);
	}

	public void addTopCanvas (Canvas3D canvas)
	{
		m_topView.addCanvas3D (canvas);
	}

	public void attachToSceneGraph ()
	{
		SceneGraphControl.spliceIntoSceneGraph (m_viewBranch);
	}

	/**
	 * Detach the Geopod nodes from the scene graph.
	 */
	public void detachFromSceneGraph ()
	{
		m_viewBranch.detach ();
	}

	public void addNodeToMovementGroup (Node node)
	{
		addNodeToGroup (node, m_movementTransformGroup);
	}

	private void addNodeToGroup (Node node, Group group)
	{
		// If the group is live and we are not adding a branch group node,
		//   create a branch group to hold the new node
		if (group.isLive () && node.getClass () != BranchGroup.class)
		{
			BranchGroup liveAttachGroup = new BranchGroup ();
			liveAttachGroup.setCapability (BranchGroup.ALLOW_DETACH);
			liveAttachGroup.addChild (node);
			group.addChild (liveAttachGroup);
		}
		else
		{
			group.addChild (node);
		}
	}

}
