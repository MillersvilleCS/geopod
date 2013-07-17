/*
 * A lean and mean of ucar.unidata.idv.ui.ImageSequenceGrabber with some minor
 * tweaks
 */

package geopod.gui.panels.moviecapture;

import geopod.eventsystem.IObserver;
import geopod.eventsystem.ISubject;
import geopod.eventsystem.SubjectImpl;
import geopod.eventsystem.events.GeopodEventId;
import geopod.gui.Hud;
import geopod.gui.components.OffScreenCanvas3D;
import geopod.gui.components.OnScreenCanvas3D;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ucar.unidata.idv.ui.ImageWrapper;
import ucar.unidata.ui.ImagePanel;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.ui.JpegImagesToMovie;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LayoutUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

public class MovieCapturePanel
		extends JPanel
		implements ISubject, ActionListener, Runnable
{

	private static final long serialVersionUID = 4738143300355207450L;

	/* Command vars for the action listener */
	private final String CMD_CLOSE_MAIN_DIALOG = "CLOSE_MAIN_DIALOG";
	private final String CMD_SHOW_PREVIEW_DIALOG = "SHOW_PREVIEW_DIALOG";
	private final String CMD_CLOSE_PREVIEW_DIALOG = "CLOSE_PREVIEW_DIALOG";
	private final String CMD_CAPTURE_IMAGE = "CAPTURE_IMAGE";
	private final String CMD_CAPTURE_MOVIE = "CAPTURE_MOVIE";
	private final String CMD_IMAGES_DELETE_ALL = "IMAGES_DELETE_ALL";
	private final String CMD_IMAGES_SAVE_AS_MOVIE = "IMAGES_SAVE_AS_MOVIE";

	private final String CMD_PREVIEW_IMAGE_PREV = "PREVIEW_IMAGE_PREV";
	private final String CMD_PREVIEW_IMAGE_PLAY = "PREVIEW_IMAGE_PLAY";
	private final String CMD_PREVIEW_IMAGE_NEXT = "PREVIEW_IMAGE_NEXT";
	private final String CMD_PREVIEW_IMAGE_DELETE = "PREVIEW_IMAGE_DELETE";
	private final String CMD_PREVIEW_IMAGE_SAVE = "PREVIEW_IMAGE_SAVE";

	/* Dialog Window vars. Made in the same fashion as ucar.unidata.idv.ui.ImageSequenceGrabber */
	private JDialog m_dlgMain;
	private JDialog m_dlgPreview;
	private JFileChooser m_dlgSave;

	private JLabel m_lblNote;

	private JPanel m_pnlFrameRate;
	private double m_captureRate;
	private double m_captureRateActual;

	private JButton m_btnCaptureImage;
	private JButton m_btnCaptureMovie;
	private JButton m_btnPreviewImages;
	private JButton m_btnSaveImagesAsMovie;
	private JButton m_btnDeleteImages;
	private JButton m_btnClose;
	private JButton m_btnImagePreviewPrev;
	private JButton m_btnImagePreviewPlay;
	private JButton m_btnImagePreviewNext;
	private JButton m_btnImagePreviewDelFrame;
	private JButton m_btnImagePreviewSaveFrame;

	private ImageIcon m_icnImagePreviewPlay;
	private ImageIcon m_icnImagePreviewStop;

	private ImagePanel m_pnlPreview;
	private int m_previewIndex;
	private int m_timestamp;

	private boolean m_isCapturingMovie;
	private boolean m_isPreviewPlaying;

	private JLabel m_lblFrame;
	private JLabel m_lblPreview;

	private JTextField m_fldDisplayRate;
	private JTextField m_fldCaptureRate;
	private JTextField m_fldImagePreviewRate;

	private Object m_MUTEX = new Object ();
	private String m_directory;
	private String m_lastPreview;

	private List<ImageWrapper> m_images = new ArrayList<ImageWrapper> ();

	private Hud m_hud;
	private SubjectImpl m_subjectImpl;

	private OnScreenCanvas3D m_onScreenCanvas;
	private OffScreenCanvas3D m_offScreenCanvas;

	private long m_startRecordTime;
	private long m_stopRecordTime;

	public MovieCapturePanel (Hud hud)
			throws AWTException, SecurityException, IllegalArgumentException
	{
		m_hud = hud;
		m_subjectImpl = new SubjectImpl ();

		createDialogWindow ();
	}

	/**
	 * Toggles the visibility of the movie capture settings window, or if it is
	 * currently recording, stops the recording.
	 */
	public void click ()
	{
		if (m_onScreenCanvas == null)
		{
			initCanvases ();
		}

		if (!m_isCapturingMovie)
		{
			m_dlgMain.setLocationRelativeTo (m_hud.getFlightFrame ());
			m_dlgMain.setVisible (!m_dlgMain.isVisible ());
			notifyObservers (GeopodEventId.MOVIECAPTURE_BUTTON_STATE_CHANGED);
		}
		else
		{
			stopMovieCapture ();
		}
	}

	/****************************** INHERITED FROM ISubject *****************************************/
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
	public void removeObservers ()
	{
		m_subjectImpl.removeObservers ();
	}

	@Override
	public void notifyObservers (GeopodEventId eventId)
	{
		m_subjectImpl.notifyObservers (eventId);
	}

	/****************************** MODIFIED FROM ucar.unidata.idv.ui.ImageSequenceGrabber *****************************************/
	private void createDialogWindow ()
	{

		// Store the m_images in a unique (by current time) subdir of the user's tmp  dir
		m_directory = IOUtil.joinDir (m_hud.getIdvViewManager ().getStore ().getUserTmpDirectory (), "m_images_"
				+ System.currentTimeMillis ());
		IOUtil.makeDir (m_directory);

		m_dlgMain = GuiUtils.createDialog ("Movie Capture", false);
		m_lblFrame = GuiUtils.cLabel ("No frames");
		m_fldDisplayRate = new JTextField ("4.0", 3);
		m_fldCaptureRate = new JTextField ("4.0", 3);

		m_pnlFrameRate = LayoutUtil.left (GuiUtils.wrap (GuiUtils.hflow (Misc.newList (GuiUtils.rLabel (" Rate: "),
				m_fldDisplayRate, new JLabel (" frames/sec")))));
		m_dlgSave = new JFileChooser ();

		JPanel bottomRow = (JPanel) ((JPanel) m_dlgSave.getComponent (3)).getComponent (3);
		bottomRow.add (m_pnlFrameRate, 0);

		m_btnCaptureImage = new JButton ("Capture Image");
		m_btnCaptureImage.setActionCommand (CMD_CAPTURE_IMAGE);
		m_btnCaptureImage.addActionListener (this);
		m_btnCaptureMovie = new JButton ("Capture Movie");
		m_btnCaptureMovie.setActionCommand (CMD_CAPTURE_MOVIE);
		m_btnCaptureMovie.addActionListener (this);

		List<JButton> frameButtons = new ArrayList<JButton> ();

		m_btnPreviewImages = new JButton ("Preview");
		m_btnPreviewImages.setActionCommand (CMD_SHOW_PREVIEW_DIALOG);
		m_btnPreviewImages.addActionListener (this);
		frameButtons.add (m_btnPreviewImages);
		m_btnDeleteImages = new JButton ("Delete All");
		m_btnDeleteImages.setActionCommand (CMD_IMAGES_DELETE_ALL);
		m_btnDeleteImages.addActionListener (this);
		frameButtons.add (m_btnDeleteImages);
		m_btnSaveImagesAsMovie = new JButton ("Save Movie");
		m_btnSaveImagesAsMovie.setActionCommand (CMD_IMAGES_SAVE_AS_MOVIE);
		m_btnSaveImagesAsMovie.addActionListener (this);
		frameButtons.add (m_btnSaveImagesAsMovie);

		m_btnClose = new JButton ("Close");
		m_btnClose.setActionCommand (CMD_CLOSE_MAIN_DIALOG);
		m_btnClose.addActionListener (this);

		m_lblNote = new JLabel ("empty", SwingConstants.CENTER);
		m_lblNote.setVisible (false);
		JPanel titlePanel = GuiUtils.inset (m_lblNote, 8);
		JPanel runPanel = GuiUtils.hflow (Misc.newList (GuiUtils.rLabel (" Rate: "), m_fldCaptureRate, new JLabel (
				" frames/sec")));

		int maxBtnWidth = Math.max (m_btnCaptureImage.getPreferredSize ().width,
				m_btnCaptureMovie.getPreferredSize ().width);

		GuiUtils.tmpInsets = new Insets (5, 5, 5, 5);

		JPanel capturePanel = GuiUtils.doLayout (new Component[] { m_btnCaptureImage, GuiUtils.filler (),
				m_btnCaptureMovie, runPanel, GuiUtils.filler (maxBtnWidth + 10, 1), GuiUtils.filler (), }, 2,
				GuiUtils.WT_N, GuiUtils.WT_N);

		capturePanel = GuiUtils.inset (GuiUtils.left (capturePanel), 5);
		capturePanel.setBorder (BorderFactory.createTitledBorder ("Capture"));
		GuiUtils.setHFill ();

		JPanel framesPanel = GuiUtils.vbox (GuiUtils.left (m_lblFrame), GuiUtils.hflow (frameButtons, 4, 0));

		framesPanel = GuiUtils.inset (framesPanel, 5);
		framesPanel.setBorder (BorderFactory.createTitledBorder ("Frames"));

		JPanel contents = GuiUtils.vbox (capturePanel, framesPanel);

		contents = GuiUtils.inset (contents, 5);

		contents = GuiUtils.topCenter (titlePanel, contents);
		imagesChanged ();

		GuiUtils.packDialog (m_dlgMain,
				GuiUtils.centerBottom (contents, GuiUtils.wrap (GuiUtils.inset (m_btnClose, 4))));
	}

	private void initCanvases ()
	{
		m_onScreenCanvas = (OnScreenCanvas3D) m_hud.getFlightFrame ().getCanvas ();
		m_offScreenCanvas = m_onScreenCanvas.getOffScreenCanvas ();

		m_onScreenCanvas.syncSizesWithOffScreenCanvas ();
		m_offScreenCanvas.setScreenCaptureSize (800);
		//        m_offScreenCanvas.setScreenCaptureSize (1311);

		m_onScreenCanvas.setOffScreenCanvas (m_offScreenCanvas);
	}

	//TODO: clean this up.. change name to updateImagePreview or something
	private void imagesChanged ()
	{
		synchronized (m_MUTEX)
		{
			if (m_images.size () == 0)
			{
				m_lastPreview = null;
				m_lblFrame.setText ("No frames");
			}
			else if (m_images.size () == 1)
			{
				m_lblFrame.setText (m_images.size () + " frame");
			}
			else
			{
				m_lblFrame.setText (m_images.size () + " frames");
			}

			m_btnSaveImagesAsMovie.setEnabled (m_images.size () > 0);
			m_btnDeleteImages.setEnabled (m_images.size () > 0);
			m_btnPreviewImages.setEnabled (m_images.size () > 0);
			setPreviewImage ();
		}
	}

	private void setPreviewImage ()
	{

		if (m_pnlPreview == null)
		{
			return;
		}

		synchronized (m_MUTEX)
		{
			if (m_previewIndex >= m_images.size ())
			{
				m_previewIndex = m_images.size () - 1;
			}

			if (m_previewIndex < 0)
			{
				m_previewIndex = 0;
			}

			boolean haveImages = m_images.size () > 0;

			m_btnImagePreviewPrev.setEnabled (haveImages);
			m_btnImagePreviewNext.setEnabled (haveImages);
			m_btnImagePreviewPlay.setEnabled (haveImages);
			m_btnImagePreviewDelFrame.setEnabled (haveImages);

			if (haveImages)
			{
				String current = m_images.get (m_previewIndex).getPath ();

				if (!Misc.equals (current, m_lastPreview))
				{
					m_pnlPreview.loadFile (current);

					m_lastPreview = current;
				}

				m_lblPreview.setText ("  Frame: " + (m_previewIndex + 1) + "/" + m_images.size ());
			}
			else
			{
				m_lblPreview.setText ("   No m_images   ");
				m_pnlPreview.setImage (null);

				m_lastPreview = null;
			}
		}
	}

	private void showPreview ()
	{
		if (m_dlgPreview == null)
		{
			m_dlgPreview = new JDialog (m_dlgMain, "Movie Preview", false);

			String imgp = "/auxdata/ui/icons/";

			m_icnImagePreviewPlay = GuiUtils.getImageIcon (imgp + "Play16.gif");
			m_icnImagePreviewStop = GuiUtils.getImageIcon (imgp + "Stop16.gif");
			m_fldImagePreviewRate = new JTextField (".1", 3);

			ChangeListener rateListener = new ChangeListener ()
			{
				public void stateChanged (ChangeEvent e)
				{
					JSlider slide = (JSlider) e.getSource ();

					double value = slide.getValue () / 4.0;

					m_fldImagePreviewRate.setText ("" + value);
				}
			};
			JComponent[] comps = GuiUtils.makeSliderPopup (1, 20, 4, rateListener);
			JComponent sliderBtn = comps[0];

			m_btnImagePreviewPlay = new JButton ("Play");
			m_btnImagePreviewPlay.setActionCommand (CMD_PREVIEW_IMAGE_PLAY);
			m_btnImagePreviewPlay.setIcon (m_icnImagePreviewPlay);
			m_btnImagePreviewPlay.addActionListener (this);

			m_btnImagePreviewNext = new JButton ("Next Frame");
			m_btnImagePreviewNext.setActionCommand (CMD_PREVIEW_IMAGE_NEXT);
			m_btnImagePreviewNext.addActionListener (this);

			m_btnImagePreviewPrev = new JButton ("Previous Frame");
			m_btnImagePreviewPrev.setActionCommand (CMD_PREVIEW_IMAGE_PREV);
			m_btnImagePreviewPrev.addActionListener (this);

			m_btnImagePreviewDelFrame = new JButton ("Delete Frame");
			m_btnImagePreviewDelFrame.setActionCommand (CMD_PREVIEW_IMAGE_DELETE);
			m_btnImagePreviewDelFrame.addActionListener (this);

			m_btnImagePreviewSaveFrame = new JButton ("Save Frame");
			m_btnImagePreviewSaveFrame.setActionCommand (CMD_PREVIEW_IMAGE_SAVE);
			m_btnImagePreviewSaveFrame.addActionListener (this);

			List buttonList = Misc.newList (m_btnImagePreviewPrev, m_btnImagePreviewPlay, m_btnImagePreviewNext);

			buttonList.add (GuiUtils.filler (20, 5));
			buttonList.add (new JLabel (" Delay: "));
			buttonList.add (m_fldImagePreviewRate);
			buttonList.add (new JLabel ("(s)  "));
			buttonList.add (sliderBtn);

			JPanel buttons = GuiUtils.hflow (buttonList);

			buttons = GuiUtils.inset (buttons, 5);
			m_lblPreview = new JLabel ("  ");

			m_pnlPreview = new ImagePanel ();
			m_pnlPreview.setPreferredSize (new Dimension (640, 480));
			m_lastPreview = null;
			m_previewIndex = 0;
			setPreviewImage ();

			m_pnlPreview.setBorder (BorderFactory.createEtchedBorder ());

			JComponent topComp = GuiUtils.leftRight (buttons, GuiUtils.hbox (m_btnImagePreviewDelFrame, m_lblPreview));
			JButton closePreviewBtn = new JButton ("Close");
			closePreviewBtn.setActionCommand (CMD_CLOSE_PREVIEW_DIALOG);
			closePreviewBtn.addActionListener (this);
			JPanel contents = GuiUtils.topCenterBottom (
					topComp,
					m_pnlPreview,
					GuiUtils.wrap (LayoutUtil.center (LayoutUtil.hgrid (
							Misc.newList (m_btnImagePreviewSaveFrame, closePreviewBtn), 5))));
			GuiUtils.packDialog (m_dlgPreview, contents);
		}

		m_dlgPreview.setVisible (true);
	}

	private void previewStartPlaying ()
	{
		m_isPreviewPlaying = true;
		m_btnImagePreviewPlay.setText ("Stop");
		m_btnImagePreviewPlay.setIcon (m_icnImagePreviewStop);
		Thread t = new Thread (new Runnable ()
		{
			public void run ()
			{
				previewStartPlaying (++m_timestamp);
			}
		});
		t.start ();
	}

	private void previewStartPlaying (int ts)
	{
		while (m_isPreviewPlaying && (ts == m_timestamp) && (m_images.size () > 0))
		{
			previewNext ();

			try
			{
				double sleepTime = 1;

				try
				{
					sleepTime = new Double (m_fldImagePreviewRate.getText ().trim ()).doubleValue ();
				}
				catch (Exception noop)
				{
				}

				Misc.sleep ((long) (sleepTime * 1000));
			}
			catch (Exception exc)
			{
			}

			if (m_previewIndex >= m_images.size () - 1)
			{
				m_isPreviewPlaying = false;
			}
		}

		if (!m_isPreviewPlaying)
		{
			previewStopPlaying ();
		}
	}

	private void previewPrevious ()
	{
		m_previewIndex--;

		if (m_previewIndex < 0)
		{
			m_previewIndex = m_images.size () - 1;
		}

		setPreviewImage ();
	}

	private void previewNext ()
	{
		m_previewIndex++;

		if (m_previewIndex >= m_images.size ())
		{
			m_previewIndex = 0;
		}

		setPreviewImage ();
	}

	private void previewStopPlaying ()
	{
		m_isPreviewPlaying = false;
		m_btnImagePreviewPlay.setText ("Play");
		m_btnImagePreviewPlay.setIcon (m_icnImagePreviewPlay);
	}

	public void actionPerformed (ActionEvent ae)
	{
		String cmd = ae.getActionCommand ();

		if (cmd.equals (CMD_CLOSE_MAIN_DIALOG))
		{
			click ();
		}
		else if (cmd.equals (CMD_SHOW_PREVIEW_DIALOG))
		{
			showPreview ();
		}
		else if (cmd.equals (CMD_CLOSE_PREVIEW_DIALOG))
		{
			m_dlgPreview.setVisible (false);
		}
		else if (cmd.equals (CMD_CAPTURE_IMAGE))
		{
			captureImage ();
			saveTmpImage (m_offScreenCanvas.getLastUnsavedImage ());
			imagesChanged ();
		}
		else if (cmd.equals (CMD_CAPTURE_MOVIE))
		{
			if (!m_isCapturingMovie)
			{
				startMovieCapture ();
			}
			else
			{
				stopMovieCapture ();
			}
		}
		else if (cmd.equals (CMD_IMAGES_DELETE_ALL))
		{
			//TODO: disable other buttons when capturing movie, then remove lines in this block as appropriate
			m_isCapturingMovie = false;
			deleteCurrentImages ();
			imagesChanged ();
			m_btnCaptureMovie.setEnabled (true);
		}
		else if (cmd.equals (CMD_IMAGES_SAVE_AS_MOVIE))
		{
			saveMovie ();
		}
		else if (cmd.equals (CMD_PREVIEW_IMAGE_PREV))
		{
			previewPrevious ();
		}
		else if (cmd.equals (CMD_PREVIEW_IMAGE_PLAY))
		{
			if (!m_isPreviewPlaying)
			{
				previewStartPlaying ();
			}
			else
			{
				previewStopPlaying ();
			}
		}
		else if (cmd.equals (CMD_PREVIEW_IMAGE_NEXT))
		{
			previewNext ();
		}
		else if (cmd.equals (CMD_PREVIEW_IMAGE_DELETE))
		{
			if ((m_images.size () > 0) && (m_previewIndex >= 0) && (m_previewIndex < m_images.size ()))
			{
				String filename = m_images.get (m_previewIndex).getPath ();

				m_images.remove (m_previewIndex);
				m_previewIndex--;
				imagesChanged ();
				new File (filename).delete ();
			}
		}
		else if (cmd.equals (CMD_PREVIEW_IMAGE_SAVE))
		{
			try
			{
				//TODO: this ugliness could go away if we just transferred BufferedImages from our unsavedImages deque in OffScreenCanvas
				// to a "previewImages" vector here.
				saveImage (ImageUtils.toBufferedImage (ImageUtils.getImageFile (m_images.get (m_previewIndex)
						.getPath ())));
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace ();
			}
		}
	}

	private void captureImage ()
	{
		m_onScreenCanvas.getScreenshot ();
	}

	private void saveImage (BufferedImage bimg)
	{
		m_pnlFrameRate.setVisible (false);
		File fileDir = m_dlgSave.getCurrentDirectory ();
		String fileName = "error";

		// Find an unused file name
		for (int fileNum = 1; fileNum < 999; fileNum++)
		{
			fileName = String.format ("geopod image %03d", fileNum);
			fileName = fileName + ".jpg";
			File testFile = new File (fileDir, fileName);
			if (!testFile.exists ())
			{
				break;
			}
		}

		// Set default file name value as the unused file name we found
		m_dlgSave.setSelectedFile (new File (fileName));

		int m_saveDialogResult = m_dlgSave.showSaveDialog (m_dlgMain);
		if (m_saveDialogResult == JFileChooser.APPROVE_OPTION)
		{
			File f = m_dlgSave.getSelectedFile ();
			String path = f.getAbsolutePath ();

			// Make sure it ends with .jpg
			if (!path.endsWith (".jpg"))
			{
				path = path + ".jpg";
			}

			try
			{
				ImageUtils.writeImageToFile (bimg, path);
			}
			catch (Exception e)
			{
				System.err.println ("ERROR: Failed to write image to path:" + path);
				e.printStackTrace ();
			}
		}
	}

	private void saveTmpImage (BufferedImage tmpImage)
	{
		assert (tmpImage != null);

		String path = IOUtil.joinDir (m_directory, System.currentTimeMillis () + ".jpg");

		try
		{
			ImageUtils.writeImageToFile (tmpImage, path);
			m_images.add (new ImageWrapper (path)); //TODO: why load again from disk? instead, push images from OffScreenCanvas' unsaved images
		} //into here?
		catch (Exception e)
		{
			System.err.println ("ERROR: Failed to write image to path from MovieCapturePanel.saveTmpImage().");
			e.printStackTrace ();
		}
	}

	private void startMovieCapture ()
	{
		if (updateCaptureRate ())
		{
			m_isCapturingMovie = true;
			m_btnCaptureMovie.setText ("Stop Movie");

			Thread t = new Thread (this);
			t.start ();
			m_startRecordTime = System.currentTimeMillis ();
		}
	}

	private void stopMovieCapture ()
	{
		//TODO: disable all buttons and make a message that says "processing..."
		m_stopRecordTime = System.currentTimeMillis ();
		m_isCapturingMovie = false;
		m_btnCaptureMovie.setText ("Start Movie");
		lockMainDialog (true, "saving...");
		m_dlgMain.toFront ();

		// Save all of the images we just captured
		BufferedImage tmpImage = m_offScreenCanvas.getNextUnsavedImage ();
		int imageCount = 0;
		while (tmpImage != null)
		{
			imageCount++;
			saveTmpImage (tmpImage);
			tmpImage = m_offScreenCanvas.getNextUnsavedImage ();
		}
		imagesChanged ();

		unlockMainDialog ();

		// Calculate what the actual capture rate ended up being.
		m_captureRateActual = (double) (imageCount * 1000) / (double) (m_stopRecordTime - m_startRecordTime);
		m_captureRateActual = Math.round (m_captureRateActual * 10.0) / 10.0;
		m_fldImagePreviewRate.setText (Double.toString (1.0 / m_captureRateActual));
	}

	/**
	 * Changes the isEnabled value of all buttons/text fields on the main
	 * dialog. This is used when we stop movie capture and want to inform the
	 * user that we are busy saving the images to temporary files and updating
	 * the preview dialog.
	 * 
	 * @param enabled
	 *            true to enable buttons, false to disable buttons
	 */
	private void unlockMainDialog ()
	{
		lockMainDialog (false, "empty");
	}

	/**
	 * Changes the isEnabled value of all buttons/text fields on the main
	 * dialog. This is used when we stop movie capture and want to inform the
	 * user that we are busy saving the images to temporary files and updating
	 * the preview dialog.
	 * 
	 * @param enabled
	 *            true to enable buttons, false to disable buttons
	 * @param labelMessage
	 *            what the text underneath the main dialog's title should
	 *            display
	 */
	private void lockMainDialog (boolean enabled, String labelMessage)
	{
		//TODO: implement
		m_btnCaptureImage.setEnabled (enabled);
		m_btnCaptureMovie.setEnabled (enabled);
		m_fldCaptureRate.setEnabled (enabled);

		if (enabled && m_images.size () > 0) // Should always evaluate true since this method is only called while saving images...
		{
			m_btnPreviewImages.setEnabled (enabled);
			m_btnDeleteImages.setEnabled (enabled);
			m_btnSaveImagesAsMovie.setEnabled (enabled);
		}
		else
		{
			m_btnPreviewImages.setEnabled (false);
			m_btnDeleteImages.setEnabled (false);
			m_btnSaveImagesAsMovie.setEnabled (false);
		}

		m_lblNote.setText (labelMessage);
		m_lblNote.setVisible (enabled);
	}

	/**
	 * Updates the m_captureRate variable by taking the value of
	 * m_captureRateFld
	 * 
	 * @return true if the user's input was valid.
	 */
	public boolean updateCaptureRate ()
	{
		try
		{
			m_captureRate = (new Double (m_fldCaptureRate.getText ().trim ())).doubleValue (); // frames per second
		}
		catch (NumberFormatException nfe)
		{
			m_btnCaptureMovie.setText ("Start Movie");
			m_isCapturingMovie = false;
			LogUtil.userErrorMessage ("Bad number format for capture rate: " + m_fldCaptureRate.getText ());
			return false;
		}

		return true;
	}

	/**
	 * Captures screenshots of the flight frame's Canvas3D at a rate entered by
	 * the user.
	 */
	public void run ()
	{
		final long captureDelayInMillis = (long) ((1.0 / m_captureRate) * 1000);
		long startTime, endTime, millisToSleep;

		while (m_isCapturingMovie)
		{
			startTime = System.currentTimeMillis ();
			captureImage ();
			endTime = System.currentTimeMillis ();

			millisToSleep = captureDelayInMillis - (endTime - startTime);
			if (millisToSleep > 0)
			{
				try
				{
					Thread.sleep (millisToSleep);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace ();
				}
			}
		}
	}

	/**
	 * Display the dialog for saving the sequence of images as a movie.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void saveMovie ()
	{
		m_pnlFrameRate.setVisible (true);

		// Fill in a good default value for the "Save As" frame rate.
		double frameRate = 10.0;
		if (m_captureRateActual > 0.0) // Actual frame rate calculated in stopMovieCapture. 
		{ // Only false when user is saving a string of individual screen captures as a movie...
			m_fldDisplayRate.setText (Double.toString (m_captureRateActual));
		}

		else
		{
			try
			{ // Can we get a frame rate from the capture field?
				frameRate = (new Double (m_fldCaptureRate.getText ().trim ())).doubleValue ();
			}
			catch (NumberFormatException nfe)
			{
				// Default value of 10.0 frames/sec will fall through,
			}
			m_fldDisplayRate.setText (Double.toString (frameRate));
		}

		// Next we'll find a suitable generic name for the output movie file.
		String path = "";
		File fileDir = m_dlgSave.getCurrentDirectory ();
		String fileName = "error";

		for (int fileNum = 1; fileNum < 999; fileNum++)
		{
			fileName = String.format ("geopod movie %03d", fileNum);
			fileName = fileName + ".mov";
			File testFile = new File (fileDir, fileName);
			if (!testFile.exists ())
			{
				break;
			}
		}
		m_dlgSave.setSelectedFile (new File (fileName));

		// Show the save dialog until we have valid input in all fields.
		//TODO: Check for valid input in more fields than just frame rate.

		boolean validFrameRateInput = false;

		while (!validFrameRateInput)
		{
			int m_saveDialogResult = m_dlgSave.showSaveDialog (m_dlgMain);
			if (m_saveDialogResult == JFileChooser.APPROVE_OPTION)
			{
				File f = m_dlgSave.getSelectedFile ();
				path = f.getAbsolutePath ();
				if (!path.endsWith (".mov")) // Ensure the file suffix.
				{
					path = path + ".mov";
				}
			}
			else
			// If they press cancel, just hide the save dialog.
			{
				m_pnlFrameRate.setVisible (false);
				return;
			}

			try
			{
				frameRate = (new Double (m_fldDisplayRate.getText ().trim ())).doubleValue ();
			}
			catch (NumberFormatException nfe)
			{
				LogUtil.userErrorMessage ("Bad number format for capture rate: " + m_fldCaptureRate.getText ());
				validFrameRateInput = true;
			}

			m_pnlFrameRate.setVisible (false);
		}

		// Actually save the movie.
		//TODO: Compression video codec? The output files are currently enormous.
		SecurityManager backup = System.getSecurityManager ();
		int width = m_offScreenCanvas.getScreenCaptureSize ().width;
		int height = m_offScreenCanvas.getScreenCaptureSize ().height;
		System.setSecurityManager (null);
		JpegImagesToMovie.createMovie (path, width, height, (int) (frameRate),
				new Vector (ImageWrapper.makeFileList (m_images)));
		System.setSecurityManager (backup);
	}

	private void deleteCurrentImages ()
	{
		for (int i = 0; i < m_images.size (); i++)
		{
			m_images.get (i).deleteFile ();
		}

		m_images = new ArrayList<ImageWrapper> ();
	}
}