package geopod.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A utility to simplify loading files.
 * 
 * @author Geopod Team
 * 
 */
public class FileLoadingUtility
{
	/**
	 * Loads the {@link BufferedImage} from the specified file path.
	 * 
	 * @param path
	 *            the path of the image file
	 * @return a BufferedImage as a result of decoding the file at the specified
	 *         path
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static BufferedImage loadBufferedImage (String path)
			throws IOException
	{
		URL imageUrl = FileLoadingUtility.class.getResource (path);
		BufferedImage image = null;
		if (imageUrl != null)
		{
			image = ImageIO.read (imageUrl);
		}
		else
		{
			System.err.println ("imageUrl is null: " + path);
		}

		return (image);
	}

	/**
	 * Loads multiple {@link BufferedImage} from the specified file paths.
	 * 
	 * @param paths
	 *            a list of file path locations
	 * @return a list of BufferedImage
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static List<BufferedImage> loadBufferedImages (List<String> paths)
			throws IOException
	{
		List<BufferedImage> imageList = new ArrayList<BufferedImage> ();

		for (String path : paths)
		{
			URL imageUrl = FileLoadingUtility.class.getResource (path);
			BufferedImage image = null;
			if (imageUrl != null)
			{
				image = ImageIO.read (imageUrl);
				imageList.add (image);
			}
			else
			{
				System.err.println ("imageUrl is null: " + path);
			}
		}

		return (imageList);
	}

	/**
	 * Loads an ImageIcon from the specified file path
	 * 
	 * @param path
	 *            the path of the file
	 * @return an ImageIcon as a result of decoding the file at the specified
	 *         path
	 * @throws IOException
	 *             if an error occurs during reading
	 */
	public static ImageIcon loadImageIcon (String path)
			throws IOException
	{
		URL imageUrl = FileLoadingUtility.class.getResource (path);
		ImageIcon imageIcon = null;
		if (imageUrl != null)
		{
			imageIcon = new ImageIcon (ImageIO.read (imageUrl));
		}
		else
		{
			System.err.println ("imageUrl is null: " + path);
		}

		return (imageIcon);
	}

	public static InputStream loadFileStream (String path)
	{
		return FileLoadingUtility.class.getResourceAsStream (path);
	}

	/**
	 * Loads a font with the specified font format and path
	 * 
	 * @param fontFormat
	 *            the type of the font
	 * @param path
	 *            the path of the file
	 * @return the font or null if there was a problem reading the file
	 */
	public static Font loadFont (int fontFormat, String path)
	{
		InputStream inputStream = FileLoadingUtility.class.getResourceAsStream (path);
		Font font = null;

		try
		{
			font = Font.createFont (fontFormat, inputStream);
		}
		catch (FontFormatException e)
		{
			e.printStackTrace ();
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}

		return (font);
	}
}
