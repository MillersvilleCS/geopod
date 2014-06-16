package geopod.utils.debug;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;

import javax.media.j3d.Geometry;
import javax.media.j3d.Group;
import javax.media.j3d.Leaf;
import javax.media.j3d.Node;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix4d;

/**
 * Dump a scene graph to a PrintStream. Adapted from code by Tim Bray at
 * http://www.mail-archive.com/java3d-interest@java.sun.com/msg00146.html
 * 
 * Sample usage:
 * 
 * // Debug: Dump the entire scene graph to a file FileOutputStream out;
 * PrintStream p; try { out = new FileOutputStream ("SceneGraphDump.txt"); p =
 * new PrintStream (out);
 * 
 * Node rootNode = this.m_displayRenderer.getRoot (); DumpSceneGraph.dump
 * (rootNode, p); } catch (Exception e) { e.printStackTrace (); }
 * 
 * @author Geopod Team
 * 
 */
public class SceneGraphDumper
{
	/**
	 * Dump a branch of the scene graph.
	 * 
	 * @param n
	 *            - the top {@link Node} of the scene graph segment to draw.
	 *            Pass the root node to dump the entire graph.
	 * @param out
	 *            - the {@link PrintStream} to use for output.
	 */
	public static void dump (Node n, PrintStream out)
	{
		dumpNode (n, 0, out);
	}

	private static void dumpNode (Node n, int depth, PrintStream out)
	{
		// Create the indentation
		char[] spaces = new char[2 * depth];
		Arrays.fill (spaces, ' ');
		out.print (spaces);
		out.print (n);

		if (n instanceof Leaf)
		{
			if (n instanceof Shape3D)
			{
				Shape3D shape = (Shape3D) n;
				Geometry geometry = shape.getGeometry ();
				out.print ("; " + geometry);
			}
			out.println ();
		}
		else if (n instanceof Group)
		{
			if (n instanceof TransformGroup)
			{
				// By default skip display of transform data
				//dumpTransformGroup ((TransformGroup) n, out, spaces);
			}
			out.println ();
			@SuppressWarnings("unchecked")
			Enumeration<SceneGraphObject> children = ((Group) n).getAllChildren ();
			while (children.hasMoreElements ())
			{
				SceneGraphObject child = children.nextElement ();
				if (child instanceof Node)
				{
					dumpNode ((Node) child, depth + 1, out);
				}
			}
		}
	}

	/**
	 * Dump a human-readable representation of a {@link TransformGroup}.
	 * 
	 * @param tg
	 *            - the transformGroup to print
	 * @param out
	 *            - the stream to print to.
	 * @param indentMarker
	 *            - the character to use for indentation. Space ' ', and tab
	 *            "\t" recommended.
	 */
	public static void dumpTransformGroup (TransformGroup tg, PrintStream out, String indentMarker)
	{
		Transform3D t = new Transform3D ();
		Matrix4d d = new Matrix4d ();
		tg.getTransform (t);
		t.get (d);
		out.println (tg);
		out.format ("%s %6.4f %6.4f %6.4f %6.4f %n", indentMarker, d.m00, d.m01, d.m02, d.m03);
		out.format ("%s %6.4f %6.4f %6.4f %6.4f %n", indentMarker, d.m10, d.m11, d.m12, d.m13);
		out.format ("%s %6.4f %6.4f %6.4f %6.4f %n", indentMarker, d.m20, d.m21, d.m22, d.m23);
		out.format ("%s %6.4f %6.4f %6.4f %6.4f %n", indentMarker, d.m30, d.m31, d.m32, d.m33);
		out.println ();
	}
}
