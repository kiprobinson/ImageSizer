package com.github.kiprobinson.imagesizer.util;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * Class that can be used to resize an image so that it will fit nicely on a
 * multi-monitor setup.
 * 
 * @author Kip Robinson, https://github.com/kiprobinson
 */
public class ImageSizer implements BufferedImageOp
{
  private final int width;
  private final int height;
  private final int gapWidth;
  private final int realWidth;
  private final double realRatio;
  
  /**
   * Constructs an image sizer that will operate on a width x height display,
   * with a gap between monitors of gapWidth pixels.
   * 
   * @param width    Total size of desktop (not including gap).
   * @param height   Height of desktop.
   * @param gapWidth Width (in pixels) of gap between monitors.
   * 
   * @throws IllegalArgumentException if any parameters are nonpositive.
   */
  public ImageSizer(int width, int height, int gapWidth)
  {
    if(width <= 0 || height <= 0 || gapWidth < 0)
      throw new IllegalArgumentException("The width, height, and gapWidth parameters must all be positive.");
    this.width = width;
    this.height = height;
    this.gapWidth = gapWidth;
    this.realWidth = width + gapWidth;
    this.realRatio = (double)realWidth/(double)height;
  }
  
  
  /**
   * Creates a zeroed destination Raster with the correct size and number of bands.
   */
  @Override
  public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM)
  {
    if (destCM == null)
      destCM = src.getColorModel();
    return new BufferedImage(destCM,
            destCM.createCompatibleWritableRaster(width,height),
            destCM.isAlphaPremultiplied(),
            null);
  }


  @Override
  public BufferedImage filter(BufferedImage src, BufferedImage dest)
  {
    if(dest == null)
      dest = createCompatibleDestImage(src, null);
    else if (dest.getWidth() != width && dest.getHeight() != height)
      throw new IllegalArgumentException("Illegal dimensions for destination buffer.");
    
    //resize source image
    final double srcRatio = (double)src.getWidth() / (double)src.getHeight();
    if(srcRatio < realRatio)
      src = getResizeOp((double)realWidth/(double)src.getWidth()).filter(src, null);
    else if (srcRatio > realRatio)
      src = getResizeOp((double)height/(double)src.getHeight()).filter(src, null);
    //else: do nothing (no need to resize to the same size)
    
    int startX = (src.getWidth() - realWidth)/2;
    int startY = (src.getHeight() - height)/2;
    
    for(int i = 0; i < width; i++)
      for(int j = 0; j < height; j++)
        dest.setRGB(i, j, src.getRGB(i+startX+(i>=width/2?gapWidth:0), j+startY));
    
    return dest;
  }
  
  
  @Override
  public Rectangle2D getBounds2D(BufferedImage src)
  {
    return new Rectangle(width, height);
  }
  
  
  @Override
  public Point2D getPoint2D(Point2D srcPt, Point2D dstPt)
  {
    throw new UnsupportedOperationException();
  }
  
  
  @Override
  public RenderingHints getRenderingHints()
  {
    return null;
  }
  
  /**
   * Helper method so that this can be done in one line.
   */
  private static AffineTransformOp getResizeOp(double scale)
  {
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    return new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
  }


}
