package com.github.kiprobinson.imagesizer.main;

import com.github.kiprobinson.imagesizer.util.ImageSizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Main class for running ImageSizer. When compiled as a jar file, specify this
 * class as the main class in order to run like: java -jar ImageSizer.jar ImageIn.jpg ImageOut.jpg
 * 
 * @author Kip Robinson, https://github.com/kiprobinson
 */
public final class Main
{
  
  public static void main(String[] args)
  {
    //args = new String[] { "C:\\Users\\Owner\\Desktop\\export\\10.jpg", "C:\\Users\\Owner\\Desktop\\export\\11.jpg" };
    if(args.length < 1)
    {
      printUsage();
      return;
    }
    Args params;
    try {
      params = new Args(args);
    }
    catch (Exception e) {
      System.out.println("Error parsing parameters: " + e.getMessage());
      printUsage();
      return;
    }
    
    for(int i = 0; i < params.inputFiles.size(); i++)
    {
      try {
        File inputFile = params.inputFiles.get(i);
        File outputFile = params.outputFiles.get(i);
        
        System.out.print("Reading image: " + inputFile.getAbsolutePath() + " ... ");
        BufferedImage img = ImageIO.read(inputFile);
        System.out.println("Done!");
        
        System.out.print("Resizing... ");
        ImageSizer imageSizer = new ImageSizer(params.width, params.height, params.gapWidth);
        BufferedImage output = imageSizer.filter(img, null);
        System.out.println("Done!");
        
        System.out.print("Saving result... ");
        ImageIO.write(output, "png", outputFile);
        System.out.println("Done!");
        
        System.out.println("Output file is: " + outputFile.getAbsolutePath());
        System.out.println();
      }
      catch (OutOfMemoryError e) {
        System.out.println("Error!");
        System.out.println("Out of memory error - this image was too large. Try passing a large value");
        System.out.println("for -Xmx parameter. For example: java -Xmx256m -jar ImageSizer.jar ...");
        System.out.println();
      }
      catch (Throwable t) {
        System.out.println("Error!");
        System.err.println(t);
        System.out.println();
        continue;
      }
    }
  }
  
  private static void printUsage()
  {
    System.out.println("                                                                           ");
    System.out.println("                           ImageSizer                                      ");
    System.out.println("                      (c) 2009 Kip Robinson                                ");
    System.out.println("                  https://github.com/kiprobinson                           ");
    System.out.println("---------------------------------------------------------------------------");
    System.out.println("What it is: A simple utility to resize/crop an image such that it can be   ");
    System.out.println("  shown on a two-desktop display, with equal monitor resolutions, taking   ");
    System.out.println("  into account the gap between the two displays.                           ");
    System.out.println("                                                                           ");
    System.out.println("Usage:                                                                     ");
    System.out.println("  java -jar ImageSizer.jar  [-width n | -monitorWidth n] [-height n]       ");
    System.out.println("             [-gap n] inputFiles [-outputFile outputFile]                  ");
    System.out.println("                                                                           ");
    System.out.println("Parameters:                                                                ");
    System.out.println("inputFile: images to be resized/cropped.  Supported file formats are jpg,  ");
    System.out.println("           gif, bmp, and png.                                              ");
    System.out.println("width: total width of desktop, in displayed pixels (not counting gap).     ");
    System.out.println("       Not necessary if monitorWidth parameter is supplied.                ");
    System.out.println("       Default: 2560 (1280*2).                                             ");
    System.out.println("monitorWidth: width of a single monitor, in pixels.  Not necessary if      ");
    System.out.println("              width parameter was specified. Default: 1280.                ");
    System.out.println("height: height of monitors, in pixels.  Default: 1024.                     ");
    System.out.println("gap: width of 'gap' between monitors, in pixels. Default: 120.             ");
    System.out.println("outputFile: name of a png file that will be used for output.  If the file  ");
    System.out.println("            already exists, it will be overwritten.  Default value is the  ");
    System.out.println("            same name as input file, with '.resized.png' appended to end.  ");
    System.out.println("                                                                           ");
    System.out.println("Example:                                                                   ");
    System.out.println("java -jar ImageSizer.jar -width 2720 -height 768 -gap 120 img1.jpg img2.jpg");
    System.out.println("                                                                           ");
  }
  
  private static class Args
  {
    public List<File> inputFiles = new ArrayList<File>();
    public List<File> outputFiles = new ArrayList<File>();
    public int width = 1280*2;
    public int height = 1024;
    public int gapWidth = 120;
    
    public Args(String[] args)
    {
      for(int i = 0; i < args.length; i++)
      {
        if("-width".equalsIgnoreCase(args[i]) && args.length > i+1)
          width = Integer.parseInt(args[++i]);
        else if("-monitorWidth".equalsIgnoreCase(args[i]) && args.length > i+1)
          width = Integer.parseInt(args[++i])*2;
        else if ("-height".equalsIgnoreCase(args[i]) && args.length > i+1)
          height = Integer.parseInt(args[++i]);
        else if ("-gap".equalsIgnoreCase(args[i]) && args.length > i+1)
          gapWidth = Integer.parseInt(args[++i]);
        else if ("-outputFile".equalsIgnoreCase(args[i]) && args.length > i+1)
          while(i + 1 < args.length && !args[i+1].startsWith("-"))
            outputFiles.add(new File(args[++i]));
        else if (!args[i].startsWith("-"))
          inputFiles.add(new File(args[i]));
        else
          throw new IllegalArgumentException("Unknown argument: " + args[i]);
      }
      
      if(inputFiles.size() == 0)
        throw new IllegalArgumentException("No input file given");
      
      if(outputFiles.size() == 0)
        for(File inputFile : inputFiles)
          outputFiles.add(new File(inputFile.getAbsolutePath().replaceFirst("(\\.[^/\\\\.]+)?$", ".resized.png")));
      
      if(inputFiles.size() != outputFiles.size())
        throw new IllegalArgumentException("Input file list and output file lists are of different sizes!");
    }
  }
}
