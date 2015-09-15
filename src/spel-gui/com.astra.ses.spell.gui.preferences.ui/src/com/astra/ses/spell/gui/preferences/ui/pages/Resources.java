package com.astra.ses.spell.gui.preferences.ui.pages;

import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;

public class Resources 
{

	  public static final String IMG_ARROW_DOWN  = "/icons/arrow_down.png";
	  public static final String IMG_ARROW_LEFT  = "/icons/arrow_left.png";
	  public static final String IMG_ARROW_RIGHT = "/icons/arrow_right.png";
	  public static final String IMG_ARROW_UP    = "/icons/arrow_up.png";
	  public static final String IMG_WRENCH      = "/icons/wrench.png";

	  /**
	   * Extracting plugin resource
	   * 
	   * @param resource
	   * @return
	   */
	  public static InputStream getResource(String resource)
	  {
	    return Resources.class.getClassLoader().getResourceAsStream(resource);
	  }

	  /**
	   * Extracting image descriptor
	   * 
	   * @param resource
	   * @return
	   */
	  public static ImageDescriptor getImage(String resource)
	  {
	    return ImageDescriptor.createFromFile(Resources.class, resource);
	  }

}
