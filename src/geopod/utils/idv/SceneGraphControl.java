package geopod.utils.idv;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Locale;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;

import visad.java3d.DefaultDisplayRendererJ3D;

/**
 * Provides methods to manipulate the IDV scene graph.
 * 
 */
public class SceneGraphControl
{
	private static DefaultDisplayRendererJ3D m_displayRenderer;

	private SceneGraphControl ()
	{
		// Static class, no constructor.
	}

	/**
	 * Set the DisplayRendererJ3D, which provides access to important scene
	 * graph information. This must be done before invoking any other method.
	 * 
	 * @param displayRenderer
	 */
	public static void setDisplayRenderer (DefaultDisplayRendererJ3D displayRenderer)
	{
		m_displayRenderer = displayRenderer;
	}

	/**
	 * Get the {@link DefaultDisplayRendererJ3D}
	 * 
	 * @return the displayRenderer used.
	 */
	public static DefaultDisplayRendererJ3D getDisplayRenderer ()
	{
		return (m_displayRenderer);
	}

	/**
	 * Splice a branch group into the locale of the graph. You cannot attach
	 * anything other then BranchGroup nodes after this (once the graph is
	 * live).
	 * 
	 */
	public static void spliceIntoSceneGraph (BranchGroup branch)
	{
		BranchGroup rootNode = getIdvContentBranch ();
		Locale locale = rootNode.getLocale ();
		locale.addBranchGraph (branch);
	}

	/**
	 * Add a {@link BranchGroup} under the IDV content branch.
	 * 
	 * @param branch
	 *            - the branchGroup to add.
	 */
	public static void spliceIntoIdvContentBranch (BranchGroup branch)
	{
		BranchGroup rootNode = getIdvContentBranch ();
		rootNode.addChild (branch);
	}

	/**
	 * Add a {@link BranchGroup} under the IDV data volume branch.
	 * 
	 * @param branch
	 *            - the branchGroup to add.
	 */
	public static void spliceIntoIdvVolumeGroup (BranchGroup branch)
	{
		TransformGroup transform = getDataVolumeTransform ();
		transform.addChild (branch);
	}

	/**
	 * @return TransformGroup above the IDV data volume. Contains a non-uniform
	 *         scale (typically).
	 */
	public static TransformGroup getDataVolumeTransform ()
	{
		TransformGroup volumeTransform = m_displayRenderer.getTrans ();

		return (volumeTransform);
	}

	/**
	 * @return - the IDV content branch.
	 */
	public static BranchGroup getIdvContentBranch ()
	{
		return (m_displayRenderer.getRoot ());
	}

	/**
	 * Find the nodes of the specified type at or beneath the specified node in
	 * the scene graph.
	 * 
	 */
	public static <T> List<T> findNodesOfType (Node node, Class<T> type)
	{
		List<T> nodes = new ArrayList<T> ();
		findNodesOfType (node, type, nodes);

		return (nodes);
	}

	/**
	 * Find the nodes of the specified type in the IDV content branch.
	 * 
	 */
	public static <T> List<T> findNodesOfType (Class<T> type)
	{
		BranchGroup idvBranch = getIdvContentBranch ();
		List<T> nodes = findNodesOfType (idvBranch, type);

		return (nodes);
	}

	/**
	 * Find all nodes of a specified type in the scene graph.
	 * 
	 * @param <T>
	 * @param node
	 * @param type
	 * @param nodes
	 */
	@SuppressWarnings("unchecked")
	private static <T> void findNodesOfType (Node node, Class<T> type, List<T> nodes)
	{
		if (type.isAssignableFrom (node.getClass ()))
		{
			nodes.add ((T) node);
			return;
		}

		if (node instanceof Group)
		{
			Group group = (Group) node;
			Enumeration<Node> children = group.getAllChildren ();
			while (children.hasMoreElements ())
			{
				Node child = children.nextElement ();
				findNodesOfType (child, type, nodes);
			}
		}
	}

	/**
	 * 
	 * Find the node with the specified hashCode in the IDV content branch.
	 * 
	 * @param hashCode
	 *            - the hash code to search for.
	 * @return the requested node.
	 */
	public static Node findNodeWithHash (int hashCode)
	{
		BranchGroup idvBranch = getIdvContentBranch ();
		Node node = findNodeWithHashCode (idvBranch, hashCode);

		return (node);
	}

	@SuppressWarnings("unchecked")
	private static Node findNodeWithHashCode (Node node, int hashCode)
	{
		if (node.hashCode () == hashCode)
		{
			return (node);
		}

		if (node instanceof Group)
		{
			Group group = (Group) node;
			Enumeration<Node> children = group.getAllChildren ();
			while (children.hasMoreElements ())
			{
				Node child = children.nextElement ();
				Node possibleNode = findNodeWithHashCode (child, hashCode);
				if (possibleNode != null)
				{
					return (possibleNode);
				}
			}
		}

		return (null);
	}
}
