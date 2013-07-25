package geopod.gui.components;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingDeque;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Screen3D;

public class OffScreenCanvas3D extends Canvas3D {
	private static final long serialVersionUID = -5850049957890111127L;

	private OnScreenCanvas3D m_onScreenCanvas3D;
	private ImageComponent2D m_imageBuffer;
	private int m_screenCaptureWidth;
	private int m_screenCaptureHeight;

	private BlockingDeque<BufferedImage> m_images;
	private BlockingDeque<Long> m_timestamps;

	public OffScreenCanvas3D(GraphicsConfiguration gconfig,
			boolean offscreenflag) {
		super(gconfig, offscreenflag);
	}

	protected void postInit(OnScreenCanvas3D onScreenCanvas3D) {
		m_onScreenCanvas3D = onScreenCanvas3D;
		this.setScreenCaptureSize(onScreenCanvas3D.getWidth(),
				onScreenCanvas3D.getHeight());
	}

	protected void setContainers(BlockingDeque<BufferedImage> images,
			BlockingDeque<Long> timestamps) {
		m_images = images;
		m_timestamps = timestamps;
	}

	protected int getScreenCaptureWidth() {
		return m_screenCaptureWidth;
	}

	protected int getScreenCaptureHeight() {
		return m_screenCaptureHeight;
	}

	protected void setScreenCaptureSize(int width, int height) {
		m_screenCaptureWidth = width;
		m_screenCaptureHeight = height;

		Screen3D sOff = this.getScreen3D();
		Screen3D sOn = m_onScreenCanvas3D.getScreen3D();
		sOff.setSize(sOn.getSize());
		sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
		sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());

		BufferedImage bImage = new BufferedImage(m_screenCaptureWidth,
				m_screenCaptureHeight, BufferedImage.TYPE_INT_ARGB);
		m_imageBuffer = new ImageComponent2D(bImage.getType(), bImage);
		m_imageBuffer
				.setCapabilityIsFrequent(ImageComponent2D.ALLOW_IMAGE_READ);
		this.setOffScreenBuffer(m_imageBuffer);
	}

	@Override
	public void postSwap() {
		m_images.add(m_imageBuffer.getImage());
		m_timestamps.add(System.currentTimeMillis());
	}

	@Override
	public void paint(Graphics g) {
	}
}
