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
import geopod.gui.components.OnScreenCanvas3D;
import geopod.utils.ThreadUtility;
import geopod.utils.debug.Debug;
import geopod.utils.debug.Debug.DebugLevel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ucar.unidata.idv.ui.ImageWrapper;
import ucar.unidata.ui.ImagePanel;
import ucar.unidata.ui.ImageUtils;
import ucar.unidata.ui.JpegImagesToMovie;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.LayoutUtil;
import ucar.unidata.util.LogUtil;
import ucar.unidata.util.Misc;

public class MovieCapturePanel extends JPanel implements ISubject,
		ActionListener, PropertyChangeListener, KeyListener {

	private static final long serialVersionUID = 4738143300355207450L;

	/********************** Main Dialog Commands ***************************/
	private final String CMD_CAPTURE_IMAGE = "CAPTURE_IMAGE";
	private final String CMD_CAPTURE_MOVIE = "CAPTURE_MOVIE";
	private final String CMD_TOGGLE_MAINTAIN_ASPECT_RATIO = "TOGGLE_MAINTAIN_ASPECT_RATIO";
	private final String CMD_SHOW_PREVIEW_DIALOG = "SHOW_PREVIEW_DIALOG";
	private final String CMD_IMAGES_DELETE_ALL = "IMAGES_DELETE_ALL";
	private final String CMD_IMAGES_SAVE_AS_MOVIE = "IMAGES_SAVE_AS_MOVIE";
	private final String CMD_CLOSE_MAIN_DIALOG = "CLOSE_MAIN_DIALOG";

	/********************* Preview Dialog Commands *************************/
	private final String CMD_PREVIEW_IMAGE_PREVIOUS = "PREVIEW_IMAGE_PREV";
	private final String CMD_PREVIEW_IMAGE_PLAY = "PREVIEW_IMAGE_PLAY";
	private final String CMD_PREVIEW_IMAGE_NEXT = "PREVIEW_IMAGE_NEXT";
	private final String CMD_PREVIEW_IMAGE_DELETE = "PREVIEW_IMAGE_DELETE";
	private final String CMD_PREVIEW_IMAGE_SAVE = "PREVIEW_IMAGE_SAVE";
	private final String CMD_CLOSE_PREVIEW_DIALOG = "CLOSE_PREVIEW_DIALOG";

	/********************* Main Dialog Components **************************/
	private JDialog m_dlgMain;

	private JButton m_btnCaptureImage;
	private JButton m_btnCaptureMovie;

	private JLabel m_lblCaptureWidth;
	private JTextField m_fldCaptureWidth;
	private JLabel m_lblCaptureHeight;
	private JTextField m_fldCaptureHeight;

	private JCheckBox m_chkMaintainAspectRatio;
	private JCheckBox m_chkHideGeopodInterface;

	private JTextField m_fldCaptureRate;

	private JLabel m_lblPreviewFrameCount;
	private JButton m_btnPreviewImages;
	private JButton m_btnDeleteImages;
	private JButton m_btnSaveImagesAsMovie;
	private JButton m_btnCloseMainDialog;

	private JFrame m_saveAllImagesFrame;
	private JProgressBar m_saveProgress;

	/********************* Main Dialog Variables ***************************/
	private boolean m_isCapturingMovie;

	private int m_captureWidth = 800;
	private int m_captureHeight = 600;
	private final boolean DIMENSION_WIDTH = false;
	private final boolean DIMENSION_HEIGHT = true;
	private boolean m_lastDimensionChanged = DIMENSION_WIDTH;

	private double m_aspectRatio;

	private double m_captureRateDesired;
	private double m_captureRateActual;
	private long m_captureStartTime;
	private long m_captureStopTime;
	private long m_captureFrameCount;

	/********************* Save Dialog Components **************************/
	private JFileChooser m_dlgSave;
	private JTextField m_fldOutputFrameRate;
	private JPanel m_pnlOutputFrameRate;

	/******************** Preview Dialog Components ************************/
	private JDialog m_dlgPreview;

	private JButton m_btnImagePreviewPrevious;
	private JButton m_btnImagePreviewPlay;
	private JButton m_btnImagePreviewNext;
	private JTextField m_fldImagePreviewRate;

	private JButton m_btnImagePreviewDelFrame;
	private JLabel m_lblPreviewFrameNumber;

	private ImagePanel m_pnlPreviewImage;

	private JButton m_btnImagePreviewSaveFrame;

	/******************** Preview Dialog Variables *************************/
	private Object m_previewMutex = new Object();

	private boolean m_isPreviewPlaying;
	private int m_previewIndex;

	private ImageIcon m_icnImagePreviewPlay;
	private ImageIcon m_icnImagePreviewStop;

	private String m_lastPreview;

	/************************* Other Variables *****************************/
	private Hud m_hud;
	private JComponent m_dashboard;

	private OnScreenCanvas3D m_mainCanvas;
	private OnScreenCanvas3D m_miniCanvas;
	private Rectangle m_mainCanvasBounds = null;
	private Rectangle m_miniCanvasBounds = null;
	private JPanel m_miniPanel;

	private String m_directory;
	private List<ImageWrapper> m_images = new ArrayList<ImageWrapper>();
	private BlockingDeque<BufferedImage> m_unsavedMainImages;
	private BlockingDeque<BufferedImage> m_unsavedMiniImages;
	private BlockingDeque<BufferedImage> m_unsavedDashImages;
	private BlockingDeque<Long> m_unsavedMainTimestamps;
	private BlockingDeque<Long> m_unsavedMiniTimestamps;
	private BlockingDeque<Long> m_unsavedDashTimestamps;

	private SubjectImpl m_subjectImpl;

	/***********************************************************************/
	/************************* End Data Members ****************************/
	/***********************************************************************/
	/*********************** Start Data Functions **************************/
	/***********************************************************************/

	public MovieCapturePanel(Hud hud) {
		m_hud = hud;
		m_subjectImpl = new SubjectImpl();
		m_unsavedMainImages = new LinkedBlockingDeque<BufferedImage>();
		m_unsavedMiniImages = new LinkedBlockingDeque<BufferedImage>();
		m_unsavedDashImages = new LinkedBlockingDeque<BufferedImage>();
		m_unsavedMainTimestamps = new LinkedBlockingDeque<Long>();
		m_unsavedMiniTimestamps = new LinkedBlockingDeque<Long>();
		m_unsavedDashTimestamps = new LinkedBlockingDeque<Long>();

		initMainDialog();
		initSaveDialog();
		initPreviewDialog();
	}

	/**
	 * Toggles the visibility of the movie capture settings window, or if it is
	 * currently recording, stops the recording.
	 */
	public void click() {
		if (m_mainCanvas == null) {
			m_dashboard = m_hud.getFlightFrame().getLayeredPane();
			m_miniPanel = m_hud.getFlightFrame().getTopViewPanel();
			initCanvases();
			m_fldCaptureWidth.setText("800");
			m_chkMaintainAspectRatio.doClick();
		}

		if (!m_isCapturingMovie) {
			m_dlgMain.setLocationRelativeTo(m_hud.getFlightFrame());
			m_dlgMain.setVisible(!m_dlgMain.isVisible());
			notifyObservers(GeopodEventId.MOVIECAPTURE_BUTTON_STATE_CHANGED);
		} else {
			stopMovieCapture();
		}
	}

	/************************ Initialization *******************************/
	private void initMainDialog() {
		m_dlgMain = GuiUtils.createDialog("Screen Capture", false);
		m_dlgMain.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				notifyObservers(GeopodEventId.MOVIECAPTURE_BUTTON_STATE_CHANGED);
			}
		});

		// Main dialog's "Capture" panel
		m_btnCaptureImage = makeActionButton("Capture Image",
				CMD_CAPTURE_IMAGE, null);
		m_btnCaptureMovie = makeActionButton("Capture Movie",
				CMD_CAPTURE_MOVIE, null);
		JPanel capturePanel = GuiUtils.center(GuiUtils.hflow(
				Misc.newList(m_btnCaptureImage, m_btnCaptureMovie), 25, 0));
		capturePanel = GuiUtils.inset(GuiUtils.center(capturePanel), 5);
		capturePanel.setBorder(BorderFactory.createTitledBorder("Capture"));

		// Main dialog's "Settings" panel
		m_lblCaptureWidth = new JLabel("Width:");
		m_fldCaptureWidth = new JTextField("800", 3);
		m_fldCaptureWidth.setName("width");
		m_fldCaptureWidth.addKeyListener(this);
		m_lblCaptureHeight = new JLabel("Height:");
		m_fldCaptureHeight = new JTextField("482", 3);
		m_fldCaptureHeight.setName("height");
		m_fldCaptureHeight.addKeyListener(this);
		JPanel captureSizePanel = GuiUtils.hflow(Misc.newList(
				m_lblCaptureWidth, m_fldCaptureWidth, m_lblCaptureHeight,
				m_fldCaptureHeight));

		m_chkMaintainAspectRatio = new JCheckBox("Maintain Aspect Ratio", false);
		m_chkMaintainAspectRatio
				.setActionCommand(CMD_TOGGLE_MAINTAIN_ASPECT_RATIO);
		m_chkMaintainAspectRatio.addActionListener(this);
		m_chkHideGeopodInterface = new JCheckBox("Hide Geopod Interface", false);

		m_fldCaptureRate = new JTextField("15.0", 3);
		JPanel ratePanel = GuiUtils
				.hflow(Misc.newList(GuiUtils.rLabel(" Rate: "),
						m_fldCaptureRate, new JLabel(" frames/sec")));

		JPanel settingsPanel = GuiUtils.doLayout(new Component[] {
				captureSizePanel, m_chkMaintainAspectRatio,
				m_chkHideGeopodInterface, ratePanel }, 2, GuiUtils.WT_N,
				GuiUtils.WT_N);
		settingsPanel = GuiUtils.inset(GuiUtils.left(settingsPanel), 5);
		settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

		// Main dialog's "Frames" panel
		m_lblPreviewFrameCount = GuiUtils.cLabel("No frames");
		m_btnPreviewImages = makeActionButton("Preview",
				CMD_SHOW_PREVIEW_DIALOG, null);
		m_btnDeleteImages = makeActionButton("Delete All",
				CMD_IMAGES_DELETE_ALL, null);
		m_btnSaveImagesAsMovie = makeActionButton("Save Movie",
				CMD_IMAGES_SAVE_AS_MOVIE, null);
		List<JButton> frameButtons = new ArrayList<JButton>();
		frameButtons.add(m_btnPreviewImages);
		frameButtons.add(m_btnDeleteImages);
		frameButtons.add(m_btnSaveImagesAsMovie);

		JPanel framesPanel = GuiUtils.vbox(
				GuiUtils.left(m_lblPreviewFrameCount),
				GuiUtils.hflow(frameButtons, 17, 0));
		framesPanel = GuiUtils.inset(framesPanel, 5);
		framesPanel.setBorder(BorderFactory.createTitledBorder("Frames"));

		m_btnCloseMainDialog = makeActionButton("Close", CMD_CLOSE_MAIN_DIALOG,
				null);

		GuiUtils.tmpInsets = new Insets(5, 5, 5, 5);
		JPanel contents = GuiUtils.vbox(capturePanel, settingsPanel,
				framesPanel);
		contents = GuiUtils.inset(contents, 5);
		contents = GuiUtils.topCenter(GuiUtils.filler(), contents);

		imagesChanged();
		GuiUtils.packDialog(
				m_dlgMain,
				GuiUtils.centerBottom(contents,
						GuiUtils.wrap(GuiUtils.inset(m_btnCloseMainDialog, 4))));
	}

	private void initSaveDialog() {
		m_directory = IOUtil.joinDir(m_hud.getIdvViewManager().getStore()
				.getUserTmpDirectory(),
				"m_images_" + System.currentTimeMillis());
		IOUtil.makeDir(m_directory);

		// Add an output framerate field to the save dialog.
		m_dlgSave = new JFileChooser();
		m_fldOutputFrameRate = new JTextField("15.0", 3);
		m_pnlOutputFrameRate = LayoutUtil.left(GuiUtils.wrap(GuiUtils
				.hflow(Misc.newList(GuiUtils.rLabel(" Rate: "),
						m_fldOutputFrameRate, new JLabel(" frames/sec")))));
		JPanel bottomRow = (JPanel) ((JPanel) m_dlgSave.getComponent(3))
				.getComponent(3);
		bottomRow.add(m_pnlOutputFrameRate, 0);
	}

	private void initPreviewDialog() {
		if (m_dlgPreview == null) {
			m_dlgPreview = new JDialog(m_dlgMain, "Movie Preview", false);

			String iconPath = "/auxdata/ui/icons/";

			m_icnImagePreviewPlay = GuiUtils.getImageIcon(iconPath
					+ "Play16.gif");
			m_icnImagePreviewStop = GuiUtils.getImageIcon(iconPath
					+ "Stop16.gif");

			m_btnImagePreviewPrevious = makeActionButton("Previous Frame",
					CMD_PREVIEW_IMAGE_PREVIOUS,
					GuiUtils.getImageIcon(iconPath + "StepBack16.gif"));
			m_btnImagePreviewPlay = makeActionButton("Play",
					CMD_PREVIEW_IMAGE_PLAY, m_icnImagePreviewPlay);
			m_btnImagePreviewNext = makeActionButton("Next Frame",
					CMD_PREVIEW_IMAGE_NEXT,
					GuiUtils.getImageIcon(iconPath + "StepForward16.gif"));
			m_fldImagePreviewRate = new JTextField("15.0", 3);
			List<JComponent> controlsList = Arrays.asList(
					m_btnImagePreviewPrevious, m_btnImagePreviewPlay,
					m_btnImagePreviewNext, GuiUtils.filler(20, 5), new JLabel(
							" Rate: "), m_fldImagePreviewRate, new JLabel(
							" frames/sec "));
			JPanel controlsPanel = GuiUtils.hflow(controlsList);
			controlsPanel = GuiUtils.inset(controlsPanel, 5);

			m_btnImagePreviewDelFrame = makeActionButton("Delete Frame",
					CMD_PREVIEW_IMAGE_DELETE, null);
			m_lblPreviewFrameNumber = new JLabel("  ");
			JComponent topComp = GuiUtils.leftRight(controlsPanel, GuiUtils
					.hbox(m_btnImagePreviewDelFrame, m_lblPreviewFrameNumber));

			m_pnlPreviewImage = new ImagePanel();
			m_pnlPreviewImage.setPreferredSize(new Dimension(640, 480));
			m_pnlPreviewImage.setBorder(BorderFactory.createEtchedBorder());
			m_lastPreview = null;
			m_previewIndex = 0;
			setPreviewImage();
			m_btnImagePreviewSaveFrame = makeActionButton("Save Frame",
					CMD_PREVIEW_IMAGE_SAVE, null);
			JButton closePreviewBtn = makeActionButton("Close",
					CMD_CLOSE_PREVIEW_DIALOG, null);
			JPanel contents = GuiUtils.topCenterBottom(topComp,
					m_pnlPreviewImage, GuiUtils.wrap(LayoutUtil
							.center(LayoutUtil.hgrid(Misc
									.newList(m_btnImagePreviewSaveFrame,
											closePreviewBtn), 5))));
			GuiUtils.packDialog(m_dlgPreview, contents);
		}
	}

	private void initCanvases() {
		m_mainCanvas = (OnScreenCanvas3D) m_hud.getFlightFrame().getCanvas();
		m_miniCanvas = (OnScreenCanvas3D) m_hud.getFlightFrame()
				.getTopViewCanvas();

		m_mainCanvas
				.setContainers(m_unsavedMainImages, m_unsavedMainTimestamps);
		m_miniCanvas
				.setContainers(m_unsavedMiniImages, m_unsavedMiniTimestamps);
	}

	/************************ Action Listener ******************************/
	public void actionPerformed(ActionEvent ae) {
		String cmd = ae.getActionCommand();

		if (cmd.equals(CMD_CLOSE_MAIN_DIALOG)) {
			click();
		} else if (cmd.equals(CMD_SHOW_PREVIEW_DIALOG)) {
			m_dlgPreview.setVisible(!m_dlgPreview.isVisible());
		} else if (cmd.equals(CMD_CLOSE_PREVIEW_DIALOG)) {
			m_dlgPreview.setVisible(false);
		} else if (cmd.equals(CMD_CAPTURE_IMAGE)) {
			if (updateCaptureSettings()) {
				captureFrame();
				saveImages(null);
				imagesChanged();
			}
		} else if (cmd.equals(CMD_CAPTURE_MOVIE)) {
			if (!m_isCapturingMovie) {
				startMovieCapture();
			} else {
				stopMovieCapture();
			}
		} else if (cmd.equals(CMD_TOGGLE_MAINTAIN_ASPECT_RATIO)) {
			m_aspectRatio = (double) m_dashboard.getWidth()
					/ m_dashboard.getHeight();

			if (m_chkMaintainAspectRatio.isSelected()) {
				if (m_lastDimensionChanged == DIMENSION_WIDTH) {
					m_lblCaptureHeight.setEnabled(false);
					m_fldCaptureHeight.setEnabled(false);
				} else {
					m_lblCaptureWidth.setEnabled(false);
					m_fldCaptureWidth.setEnabled(false);
				}
			} else {
				m_lblCaptureWidth.setEnabled(true);
				m_fldCaptureWidth.setEnabled(true);
				m_lblCaptureHeight.setEnabled(true);
				m_fldCaptureHeight.setEnabled(true);
			}
			updateCaptureSettings();
		} else if (cmd.equals(CMD_IMAGES_DELETE_ALL)) {
			deleteAllTmpImages();
			imagesChanged();
		} else if (cmd.equals(CMD_IMAGES_SAVE_AS_MOVIE)) {
			showSaveMovieDialog();
		} else if (cmd.equals(CMD_PREVIEW_IMAGE_PREVIOUS)) {
			previewPrevious();
		} else if (cmd.equals(CMD_PREVIEW_IMAGE_PLAY)) {
			if (!m_isPreviewPlaying) {
				previewStartPlaying();
			} else {
				previewStopPlaying();
			}
		} else if (cmd.equals(CMD_PREVIEW_IMAGE_NEXT)) {
			previewNext();
		} else if (cmd.equals(CMD_PREVIEW_IMAGE_DELETE)) {
			if ((m_images.size() > 0) && (m_previewIndex >= 0)
					&& (m_previewIndex < m_images.size())) {
				String filename = m_images.get(m_previewIndex).getPath();

				m_images.remove(m_previewIndex);
				m_previewIndex--;
				imagesChanged();
				new File(filename).delete();
			}
		} else if (cmd.equals(CMD_PREVIEW_IMAGE_SAVE)) {
			try {
				showSaveImageDialog(ImageUtils.toBufferedImage(ImageUtils
						.getImageFile(m_images.get(m_previewIndex).getPath())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**************************** Capturing ********************************/
	private void captureFrame() {
		m_mainCanvas.getScreenshot();

		if (!m_chkHideGeopodInterface.isSelected()) {
			m_miniCanvas.getScreenshot();
			m_unsavedDashImages.add(getDashboardImage(m_dashboard.getWidth(),
					m_dashboard.getHeight(), BufferedImage.TYPE_INT_ARGB));
			m_unsavedDashTimestamps.add(System.currentTimeMillis());
		}
	}

	private void startMovieCapture() {
		if (updateCaptureSettings()) {
			m_dlgMain.toBack();
			setMainDialogEnabled(false);
			// FIXME: Still can't use keyboard to move around after this call..
			m_hud.getFlightFrame().requestFocus();
			m_hud.getFlightFrame().requestFocusOnCanvas();
			m_isCapturingMovie = true;
			m_captureFrameCount = m_images.size();

			final SyncObj LOCK = new SyncObj();

			Thread saveThread = new Thread(new Runnable() {
				@Override
				public void run() {
					LOCK.doWait();
					while (m_isCapturingMovie) {
						if (!m_unsavedMainImages.isEmpty()) {
							saveImages(null);
						}
						LOCK.doWait();
					}
				}
			});
			saveThread.setPriority(Thread.MIN_PRIORITY);
			saveThread.start();

			Thread captureThread = new Thread(new Runnable() {
				@Override
				public void run() {
					final long captureDelayInMillis = (long) ((1.0 / m_captureRateDesired) * 1000);
					long startTime, endTime, millisToSleep;

					while (m_isCapturingMovie) {
						startTime = System.currentTimeMillis();
						captureFrame();
						LOCK.doNotify();
						endTime = System.currentTimeMillis();

						millisToSleep = captureDelayInMillis
								- (endTime - startTime);
						if (millisToSleep > 0) {
							try {
								Thread.sleep(millisToSleep);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					LOCK.doNotify();
				}
			});
			captureThread.start();
			m_captureStartTime = System.currentTimeMillis();
		}
	}

	private void stopMovieCapture() {
		m_captureStopTime = System.currentTimeMillis();
		m_captureFrameCount = (m_images.size() - m_captureFrameCount)
				+ m_unsavedMainImages.size();
		m_isCapturingMovie = false;

		// Calculate what the actual capture rate ended up being.
		m_captureRateActual = (double) m_captureFrameCount
				/ ((m_captureStopTime - m_captureStartTime) / 1000.0);
		m_captureRateActual = Math.round(m_captureRateActual * 10.0) / 10.0;
		m_fldImagePreviewRate.setText(Double.toString(m_captureRateActual));
		m_fldOutputFrameRate.setText(Double.toString(m_captureRateActual));

		// TODO: Not sure if needed anymore since images are being written to
		// disk during capture.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showSavingProgressBar();
			}
		});
	}

	/*************************** Previewing ********************************/
	private void previewPrevious() {
		if (--m_previewIndex < 0) {
			m_previewIndex = m_images.size() - 1;
		}

		setPreviewImage();
	}

	private void previewStartPlaying() {
		m_isPreviewPlaying = true;
		m_btnImagePreviewPlay.setText("Stop");
		m_btnImagePreviewPlay.setIcon(m_icnImagePreviewStop);
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (m_isPreviewPlaying && m_images.size() > 0) {
					previewNext();

					double frameRate = filterToValidDouble(m_fldImagePreviewRate
							.getText());
					if (frameRate == 0.0) {
						frameRate = 10.0;
					}

					Misc.sleep((long) (1000 / frameRate));

					if (m_previewIndex >= m_images.size() - 1) {
						m_isPreviewPlaying = false;
					}
				}

				if (!m_isPreviewPlaying) {
					previewStopPlaying();
				}
			}
		});
		t.start();
	}

	private void previewStopPlaying() {
		m_isPreviewPlaying = false;
		m_btnImagePreviewPlay.setText("Play");
		m_btnImagePreviewPlay.setIcon(m_icnImagePreviewPlay);
	}

	private void previewNext() {
		if (++m_previewIndex >= m_images.size()) {
			m_previewIndex = 0;
		}

		setPreviewImage();
	}

	private void imagesChanged() {
		synchronized (m_previewMutex) {
			if (m_images.size() == 0) {
				m_lastPreview = null;
				m_lblPreviewFrameCount.setText("No frames");
			} else if (m_images.size() == 1) {
				m_lblPreviewFrameCount.setText(m_images.size() + " frame");
			} else {
				m_lblPreviewFrameCount.setText(m_images.size() + " frames");
			}

			m_btnSaveImagesAsMovie.setEnabled(m_images.size() > 0);
			m_btnDeleteImages.setEnabled(m_images.size() > 0);
			m_btnPreviewImages.setEnabled(m_images.size() > 0);
			setPreviewImage();
		}
	}

	private void setPreviewImage() {
		if (m_pnlPreviewImage == null) {
			return;
		}

		synchronized (m_previewMutex) {
			if (m_previewIndex >= m_images.size()) {
				m_previewIndex = m_images.size() - 1;
			}

			if (m_previewIndex < 0) {
				m_previewIndex = 0;
			}

			boolean haveImages = m_images.size() > 0;

			m_btnImagePreviewPrevious.setEnabled(haveImages);
			m_btnImagePreviewNext.setEnabled(haveImages);
			m_btnImagePreviewPlay.setEnabled(haveImages);
			m_btnImagePreviewDelFrame.setEnabled(haveImages);

			if (haveImages) {
				String current = m_images.get(m_previewIndex).getPath();

				if (!Misc.equals(current, m_lastPreview)) {
					m_pnlPreviewImage.loadFile(current);

					m_lastPreview = current;
				}

				m_lblPreviewFrameNumber.setText("  Frame: "
						+ (m_previewIndex + 1) + "/" + m_images.size());
			} else {
				m_lblPreviewFrameNumber.setText("   No images   ");
				m_pnlPreviewImage.setImage(null);

				m_lastPreview = null;
			}
		}
	}

	/************************* Saving/Deleting *****************************/
	private void showSaveImageDialog(BufferedImage bimg) {
		m_pnlOutputFrameRate.setVisible(false);
		File fileDir = m_dlgSave.getCurrentDirectory();
		String fileName = "geopod image";

		// Find an unused file name
		for (int fileNum = 1; fileNum < 9999; fileNum++) {
			fileName = String.format("geopod image %04d", fileNum);
			fileName = fileName + ".jpg";
			File testFile = new File(fileDir, fileName);
			if (!testFile.exists()) {
				break;
			}
		}

		// Set default file name value as the unused file name we found
		m_dlgSave.setSelectedFile(new File(fileName));

		int m_saveDialogResult = m_dlgSave.showSaveDialog(m_dlgMain);
		if (m_saveDialogResult == JFileChooser.APPROVE_OPTION) {
			File f = m_dlgSave.getSelectedFile();
			String path = f.getAbsolutePath();

			if (!path.endsWith(".jpg")) {
				path = path + ".jpg";
			}

			try {
				ImageUtils.writeImageToFile(bimg, path);
			} catch (Exception e) {
				Debug.print(DebugLevel.LOW, "Failed to write image to file: "
						+ path);
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void showSaveMovieDialog() {
		m_pnlOutputFrameRate.setVisible(true);

		// Fill in a good default value for the "Save As" frame rate.
		double frameRate = 10.0;
		if (m_captureRateActual > 0.0) {
			m_fldOutputFrameRate.setText(java.lang.Double
					.toString(m_captureRateActual));
		}

		else {
			frameRate = m_captureRateDesired;
			m_fldOutputFrameRate.setText(java.lang.Double.toString(frameRate));
		}

		// Next we'll find a suitable generic name for the output movie file.
		String path = "";
		File fileDir = m_dlgSave.getCurrentDirectory();
		String fileName = "geopod movie";

		for (int fileNum = 1; fileNum < 9999; fileNum++) {
			fileName = String.format("geopod movie %04d", fileNum);
			fileName = fileName + ".mov";
			File testFile = new File(fileDir, fileName);
			if (!testFile.exists()) {
				break;
			}
		}
		m_dlgSave.setSelectedFile(new File(fileName));

		// Show the save dialog until we have valid input in all fields.
		// TODO: Check for valid input in more fields than just frame rate.

		boolean validFrameRateInput = false;

		while (!validFrameRateInput) {
			int m_saveDialogResult = m_dlgSave.showSaveDialog(m_dlgMain);
			if (m_saveDialogResult == JFileChooser.APPROVE_OPTION) {
				File f = m_dlgSave.getSelectedFile();
				path = f.getAbsolutePath();
				if (!path.endsWith(".mov")) // Ensure the file suffix.
				{
					path = path + ".mov";
				}
			} else {
				// If they press cancel, just hide the save dialog.
				m_pnlOutputFrameRate.setVisible(false);
				return;
			}

			try {
				frameRate = (new java.lang.Double(m_fldOutputFrameRate
						.getText().trim())).doubleValue();
				validFrameRateInput = true;
			} catch (NumberFormatException nfe) {
				LogUtil.userErrorMessage("Bad number format for capture rate: "
						+ m_fldCaptureRate.getText());
			}

			m_pnlOutputFrameRate.setVisible(false);
		}

		// Actually save the movie.
		// TODO: Compression video codec? The output files are currently
		// enormous.
		SecurityManager backup = System.getSecurityManager();
		System.setSecurityManager(null);
		JpegImagesToMovie.createMovie(path, m_captureWidth, m_captureHeight,
				(int) (frameRate),
				new Vector(ImageWrapper.makeFileList(m_images)));
		System.setSecurityManager(backup);
	}

	/**
	 * Handles the moving of images from unsaved collections
	 * {@link #writeImageToTmp(Image) to disk}.
	 * 
	 * @param progressBar
	 *            If null, will save exactly one image. Otherwise, will save all
	 *            remaining unsaved images and show its progress.
	 */
	private void saveImages(ImageSavingTask progressBar) {

		final int taskSize;
		if (progressBar == null) {
			taskSize = 1;
		} else {
			taskSize = m_unsavedMainImages.size();
		}

		final boolean hideInterface = m_chkHideGeopodInterface.isSelected();

		BufferedImage frameImage = null, mainImage = null, miniImage = null, dashImage = null;
		long mainTimestamp = 0, miniTimestamp = 0, dashTimestamp = 0;
		frameImage = new BufferedImage(m_captureWidth, m_captureHeight,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = frameImage.createGraphics();

		for (int frameCount = 1; frameCount <= taskSize; ++frameCount) {
			if (hideInterface) {
				writeImageToTmp(pollImageFrom(m_unsavedMainImages));
			} else {
				// Get main image.
				mainImage = pollImageFrom(m_unsavedMainImages);
				mainTimestamp = pollLongFrom(m_unsavedMainTimestamps);
				g.drawImage(mainImage, m_mainCanvasBounds.x,
						m_mainCanvasBounds.y, null);

				// Get mini image.
				if (miniImage == null) {
					miniImage = pollImageFrom(m_unsavedMiniImages);
					miniTimestamp = pollLongFrom(m_unsavedMiniTimestamps);
				} else {
					while (!m_unsavedMiniTimestamps.isEmpty()) {
						// If the next image in m_unsavedImagesMini is
						// chronologically closer to this frame's mainImage than
						// the one we're already holding, replace it.
						if (Math.abs(mainTimestamp - miniTimestamp) > Math
								.abs(mainTimestamp
										- m_unsavedMiniTimestamps.peekFirst())) {
							miniImage = pollImageFrom(m_unsavedMiniImages);
							miniTimestamp = pollLongFrom(m_unsavedMiniTimestamps);
						} else {
							break;
						}
					}
				}
				g.drawImage(miniImage, m_miniCanvasBounds.x,
						m_miniCanvasBounds.y, null);

				// Get dash image.
				if (dashImage == null) {
					dashImage = getScaledImage(m_captureWidth, m_captureHeight,
							pollImageFrom(m_unsavedDashImages));
					dashTimestamp = pollLongFrom(m_unsavedDashTimestamps);
				} else {
					while (!m_unsavedDashTimestamps.isEmpty()) {
						// Same logic as for the mini.
						if (Math.abs(mainTimestamp - dashTimestamp) > Math
								.abs(mainTimestamp
										- m_unsavedDashTimestamps.peekFirst())) {
							dashImage = getScaledImage(m_captureWidth,
									m_captureHeight,
									pollImageFrom(m_unsavedDashImages));
							dashTimestamp = pollLongFrom(m_unsavedDashTimestamps);
						} else {
							break;
						}
					}
				}
				g.drawImage(dashImage, 0, 0, null);

				writeImageToTmp(frameImage);
			}

			if (progressBar != null) {
				progressBar.setProgressPublic(Math.min((frameCount * 100)
						/ taskSize, 99));
			}
		}

		frameImage.flush();
		m_unsavedMainImages.clear();
		m_unsavedMiniImages.clear();
		m_unsavedDashImages.clear();
		m_unsavedMainTimestamps.clear();
		m_unsavedMiniTimestamps.clear();
		m_unsavedDashTimestamps.clear();

		if (progressBar != null) {
			progressBar.setProgressPublic(100);
		}
	}

	private void writeImageToTmp(Image tmpImage) {
		String path = IOUtil.joinDir(m_directory, System.currentTimeMillis()
				+ ".jpg");

		try {
			ImageUtils.writeImageToFile(tmpImage, path);
			m_images.add(new ImageWrapper(path));
		} catch (Exception e) {
			Debug.print(DebugLevel.LOW, "Failed to write image to path: "
					+ path);
			e.printStackTrace();
		}
	}

	private void deleteAllTmpImages() {
		for (int i = 0; i < m_images.size(); i++) {
			m_images.get(i).deleteFile();
		}
		m_images = new ArrayList<ImageWrapper>();
	}

	/***************************** Helpers *********************************/
	/**
	 * Paints the dashboard as the user sees it.
	 */
	private BufferedImage getDashboardImage(int width, int height, int type) {
		BufferedImage dashImg = new BufferedImage(m_dashboard.getWidth(),
				m_dashboard.getHeight(), type);
		final Graphics2D g = dashImg.createGraphics();
		final SyncObj LOCK = new SyncObj();

		ThreadUtility.invokeOnEdt(new Runnable() {
			public void run() {
				m_dashboard.paint(g);
				LOCK.doNotify();
			}
		});
		LOCK.doWait();

		g.dispose();

		if (width == m_dashboard.getWidth()
				&& height == m_dashboard.getHeight()) {
			return dashImg;
		} else {
			return getScaledImage(width, height, dashImg);
		}
	}

	/**
	 * Paints only the components required to figure out the main and mini
	 * canvases' bounding rectangles post-scaling.
	 */
	private BufferedImage getDashboardSkeletonImage(int width, int height,
			int type) {
		BufferedImage dashImg = new BufferedImage(m_dashboard.getWidth(),
				m_dashboard.getHeight(), type);
		BufferedImage borderImg = new BufferedImage(m_miniPanel.getWidth(),
				m_miniPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gDashPanel = dashImg.createGraphics();
		final Graphics2D gMiniBorder = borderImg.createGraphics();

		final SyncObj LOCK = new SyncObj();
		ThreadUtility.invokeOnEdt(new Runnable() {
			public void run() {
				m_hud.getFlightFrame().getContentPane().paint(gDashPanel);
				m_hud.getFlightFrame().getTopViewPanel().paint(gMiniBorder);
				LOCK.doNotify();
			}
		});
		LOCK.doWait();

		gDashPanel.drawImage(borderImg, m_miniPanel.getLocation().x,
				m_miniPanel.getLocation().y, null);

		gMiniBorder.dispose();
		gDashPanel.dispose();

		if (width == m_dashboard.getWidth()
				&& height == m_dashboard.getHeight()) {
			return dashImg;
		} else {
			return getScaledImage(width, height, dashImg);
		}
	}

	private BufferedImage getScaledImage(int width, int height,
			BufferedImage original) {
		BufferedImage scaled = new BufferedImage(width, height,
				original.getType());

		AffineTransform at = new AffineTransform();
		at.scale((double) scaled.getWidth() / original.getWidth(),
				(double) scaled.getHeight() / original.getHeight());
		AffineTransformOp scaleOp;
		if (at.getScaleX() + at.getScaleY() > 2.0) {
			// Better quality for enlargement
			scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		} else {
			// Better quality for ensmallment
			scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		}

		scaled = scaleOp.filter(original, scaled);
		original.flush();
		return scaled;
	}

	/**
	 * Starting from the center of the image, finds the offset and dimensions of
	 * a contiguous black area using an iterative pixel comparison.
	 */
	private Rectangle findBlackArea(BufferedImage image) {
		if (image == null) {
			Debug.print(DebugLevel.LOW,
					"MovieCapturePanel.findBlackArea(BufferedImage image) was given a null image.");
			return null;
		}

		int top, bot, left, right;
		Color pixel;

		int[] pixelLineHorz = image.getRGB(0, image.getHeight() / 2,
				image.getWidth(), 1, null, 0, image.getWidth());

		int i = image.getWidth() / 2;
		do {
			pixel = new Color(pixelLineHorz[--i]);
		} while (pixel.getRed() == 0 && pixel.getGreen() == 0
				&& pixel.getBlue() == 0 && i > 0);
		left = i;

		i = image.getWidth() / 2;
		do {
			pixel = new Color(pixelLineHorz[++i]);
		} while (pixel.getRed() == 0 && pixel.getGreen() == 0
				&& pixel.getBlue() == 0 && i < image.getWidth());
		right = i;

		int[] pixelLineVert = image.getRGB(image.getWidth() >> 1, 0, 1,
				image.getHeight(), null, 0, 1);

		i = image.getHeight() / 2;
		do {
			pixel = new Color(pixelLineVert[--i]);
		} while (pixel.getRed() == 0 && pixel.getGreen() == 0
				&& pixel.getBlue() == 0 && i > 0);
		top = i;

		i = image.getHeight() / 2;
		do {
			pixel = new Color(pixelLineVert[++i]);
		} while (pixel.getRed() == 0 && pixel.getGreen() == 0
				&& pixel.getBlue() == 0 && i < image.getHeight());
		bot = i;

		return (new Rectangle(left, top, right - left, bot - top));
	}

	private BufferedImage pollImageFrom(BlockingDeque<BufferedImage> source) {
		BufferedImage tmpImage = null;
		try {
			tmpImage = source.pollFirst(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Debug.print(DebugLevel.LOW,
					"MovieCapture failed in pollImageFrom(BlockingDeque<BufferedImage> source)");
		}

		return tmpImage;
	}

	private long pollLongFrom(BlockingDeque<Long> source) {
		long tmpLong = -1;
		try {
			tmpLong = source.pollFirst(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Debug.print(DebugLevel.LOW,
					"MovieCapture failed in pollLongFrom(BlockingDeque<Long> source)");
		}

		return tmpLong;
	}

	private int filterToValidInt(String strInput) {
		String strOutput = "";
		char[] charInput = strInput.toCharArray();
		for (int i = 0; i < charInput.length; i++) {
			if (Character.compare(charInput[i], '0') >= 0
					&& Character.compare(charInput[i], '9') <= 0) {
				strOutput = strOutput + charInput[i];
			}
		}
		if (strOutput.length() > 0) {
			return Integer.parseInt(strOutput);
		} else {
			return 0;
		}
	}

	private double filterToValidDouble(String strInput) {
		boolean decimalFound = false;
		String strOutput = "";
		char[] charInput = strInput.toCharArray();
		for (int i = charInput.length - 1; i >= 0; i--) {
			if (Character.compare(charInput[i], '0') >= 0
					&& Character.compare(charInput[i], '9') <= 0) {
				strOutput = charInput[i] + strOutput;
			} else if (Character.compare(charInput[i], '.') == 0) {
				if (!decimalFound) {
					strOutput = charInput[i] + strOutput;
					decimalFound = true;
				}
			}
		}

		if (strOutput.length() > 0) {
			if (!decimalFound) {
				strOutput += ".0";
			}
			return Double.parseDouble(strOutput);
		} else {
			return 0.0;
		}
	}

	class SyncObj {
		private boolean condition = false;
		private Object obj = new Object();

		public void doWait() {
			synchronized (obj) {
				while (!condition) {
					try {
						obj.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				condition = false;
			}
		}

		public void doNotify() {
			synchronized (obj) {
				condition = true;
				obj.notify();
			}
		}
	}

	private JButton makeActionButton(String caption, String command,
			ImageIcon icon) {
		JButton tmpButton = new JButton(caption);
		tmpButton.setActionCommand(command);
		tmpButton.addActionListener(this);

		if (icon != null) {
			tmpButton.setIcon(icon);
		}

		return tmpButton;
	}

	/**************************** Miscellany *******************************/
	/**
	 * Changes the <code>isEnabled</code> value of all buttons/text fields of
	 * <code>m_dlgMain</code>.
	 * <p>
	 * 
	 * Set to false when movie capture starts and set to true when finished
	 * saving images.
	 * 
	 * @param enabled
	 *            false to disable buttons, true to enable buttons
	 */
	private void setMainDialogEnabled(boolean enabled) {
		m_btnCaptureImage.setEnabled(enabled);
		m_fldCaptureRate.setEnabled(enabled);

		if (enabled == true) {
			m_btnCaptureMovie.setText("Start Movie");

			if (m_images.size() > 0) {
				m_btnPreviewImages.setEnabled(true);
				m_btnDeleteImages.setEnabled(true);
				m_btnSaveImagesAsMovie.setEnabled(true);
			}

			if (m_chkMaintainAspectRatio.isSelected()) {
				if (m_lastDimensionChanged == DIMENSION_WIDTH) {
					m_lblCaptureWidth.setEnabled(true);
					m_fldCaptureWidth.setEnabled(true);
				} else {
					m_lblCaptureHeight.setEnabled(true);
					m_fldCaptureHeight.setEnabled(true);
				}
			}
		} else {
			m_btnCaptureMovie.setText("Stop Movie");

			m_btnPreviewImages.setEnabled(false);
			m_btnDeleteImages.setEnabled(false);
			m_btnSaveImagesAsMovie.setEnabled(false);

			m_lblCaptureWidth.setEnabled(false);
			m_fldCaptureWidth.setEnabled(false);
			m_lblCaptureHeight.setEnabled(false);
			m_fldCaptureHeight.setEnabled(false);
		}
	}

	/**
	 * Updates the capture rate and size.
	 * 
	 * @return True if all updates were successful.
	 */
	private boolean updateCaptureSettings() {
		double captureRateDesired = filterToValidDouble(m_fldCaptureRate
				.getText());
		int captureWidth = filterToValidInt(m_fldCaptureWidth.getText());
		int captureHeight = filterToValidInt(m_fldCaptureHeight.getText());

		if (captureRateDesired == 0.0 || captureWidth == 0
				|| captureHeight == 0) {
			JOptionPane.showMessageDialog(m_dlgMain,
					"Please enter a value greater than zero.", "Invalid Input",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			m_captureRateDesired = captureRateDesired;

			if (m_chkMaintainAspectRatio.isSelected()) {
				m_aspectRatio = (double) m_dashboard.getWidth()
						/ m_dashboard.getHeight();

				if (m_lastDimensionChanged == DIMENSION_WIDTH) {
					m_captureWidth = captureWidth;
					m_captureHeight = (int) (m_captureWidth / m_aspectRatio);
					m_fldCaptureHeight.setText(String.valueOf(m_captureHeight));
				} else {
					m_captureHeight = captureHeight;
					m_captureWidth = (int) (m_captureHeight * m_aspectRatio);
					m_fldCaptureWidth.setText(String.valueOf(m_captureWidth));
				}
			} else {
				m_captureWidth = captureWidth;
				m_captureHeight = captureHeight;
			}
		}

		if (m_chkHideGeopodInterface.isSelected()) {
			m_mainCanvas.setScreenCaptureSize(m_captureWidth, m_captureHeight);
		} else {

			BufferedImage dashImg = getDashboardSkeletonImage(m_captureWidth,
					m_captureHeight, BufferedImage.TYPE_INT_ARGB);
			m_mainCanvasBounds = findBlackArea(dashImg);
			m_mainCanvas.setScreenCaptureSize(m_mainCanvasBounds.width,
					m_mainCanvasBounds.height);

			double scaleWidth = (double) m_dashboard.getWidth()
					/ m_captureWidth;
			double scaleHeight = (double) m_dashboard.getHeight()
					/ m_captureHeight;
			int subWidth = (int) ((m_miniPanel.getWidth() + (m_miniPanel
					.getLocation().x * 2)) / scaleWidth);
			int subHeight = (int) ((m_miniPanel.getHeight() + (m_miniPanel
					.getLocation().y * 2)) / scaleHeight);
			BufferedImage subImg = dashImg.getSubimage(0, 0, subWidth,
					subHeight);
			m_miniCanvasBounds = findBlackArea(subImg);
			m_miniCanvas.setScreenCaptureSize(m_miniCanvasBounds.width,
					m_miniCanvasBounds.height);

			dashImg.flush();
			subImg.flush();
		}
		return true;
	}

	private void updateCaptureDimensionFields() {
		m_aspectRatio = (double) m_dashboard.getWidth()
				/ m_dashboard.getHeight();
		if (m_chkMaintainAspectRatio.isSelected()) {
			if (m_lastDimensionChanged == DIMENSION_WIDTH) {
				int tmpValue = filterToValidInt(m_fldCaptureWidth.getText());
				int newValue = (int) ((double) tmpValue / m_aspectRatio);
				m_fldCaptureHeight.setText(String.valueOf(newValue));
			} else {
				int tmpValue = filterToValidInt(m_fldCaptureHeight.getText());
				int newValue = (int) ((double) tmpValue * m_aspectRatio);
				m_fldCaptureWidth.setText(String.valueOf(newValue));
			}
		}
	}

	/*********************** "Saving" Progress Bar *************************/
	public void showSavingProgressBar() {
		m_saveAllImagesFrame = new JFrame("Saving...");
		m_saveAllImagesFrame
				.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		m_saveAllImagesFrame.setAlwaysOnTop(true);
		m_saveAllImagesFrame.setMinimumSize(new Dimension(400, 80));

		m_saveProgress = new JProgressBar();
		m_saveProgress.setString("Saving images...");
		m_saveProgress.setMinimumSize(new Dimension(400, m_saveProgress
				.getHeight()));
		m_saveProgress.setStringPainted(true);
		m_saveProgress.setBorder(BorderFactory
				.createEmptyBorder(20, 20, 20, 20));

		m_saveAllImagesFrame.add(m_saveProgress);
		m_saveAllImagesFrame.pack();
		m_saveAllImagesFrame.setLocationRelativeTo(m_hud.getFlightFrame());
		m_saveAllImagesFrame.setVisible(true);

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		ImageSavingTask saveTask = new ImageSavingTask();
		saveTask.addPropertyChangeListener(this);
		saveTask.execute();
	}

	class ImageSavingTask extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			saveImages(this);
			return null;
		}

		public void setProgressPublic(int progress) {
			this.setProgress(progress);
		}

		@Override
		public void done() {
			imagesChanged();
			setMainDialogEnabled(true);
			m_saveAllImagesFrame.setVisible(false);
			m_saveAllImagesFrame.dispose();
			setCursor(null);
			m_dlgMain.toFront();
			m_dlgMain.requestFocus();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			m_saveProgress.setValue(progress);
		}
	}

	/******************* Inherited from KeyListener ************************/
	@Override
	public void keyReleased(KeyEvent e) {
		String senderID = ((JTextField) e.getSource()).getName();
		if (senderID.equals("width")) {
			m_lastDimensionChanged = DIMENSION_WIDTH;
		} else {
			m_lastDimensionChanged = DIMENSION_HEIGHT;
		}
		updateCaptureDimensionFields();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	/******************** Inherited from ISubject **************************/
	@Override
	public void addObserver(IObserver observer, GeopodEventId eventId) {
		m_subjectImpl.addObserver(observer, eventId);
	}

	@Override
	public void removeObserver(IObserver observer, GeopodEventId eventId) {
		m_subjectImpl.removeObserver(observer, eventId);
	}

	@Override
	public void removeObservers() {
		m_subjectImpl.removeObservers();
	}

	@Override
	public void notifyObservers(GeopodEventId eventId) {
		m_subjectImpl.notifyObservers(eventId);
	}
}
