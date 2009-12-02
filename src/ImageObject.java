import java.net.URL;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.net.MalformedURLException;
import java.io.IOException;

enum Orientation {Landscape,Portrait}

class ImageObject {
    BufferedImage bImage = null;
    Orientation iOri;
    int width,height;
    //String imageID;

    ImageObject(String absoluteURL){
	URL urlAddress;
	if(absoluteURL.startsWith("///\\\\\\")){
	    String relativeURL = absoluteURL.substring(6);
	    //System.out.println(relativeURL +  " is relative and absolute is " + absoluteURL);
	    urlAddress = GUI.class.getResource(relativeURL); //could be null
	}
	else {
	    File file = new File(absoluteURL);
	    try{
		urlAddress = file.toURI().toURL();
		//System.out.println(absoluteURL +  " is absolute and file is "+file.toString() +" and URL is " + urlAddress.toString());
	    } catch (MalformedURLException e){
		urlAddress = null;
		System.err.println("Image file " + absoluteURL + " could not be found " + "\nError was: " + e.toString());
	    }
	}
	if(urlAddress==null){
	    System.err.println("File could not be found at " + absoluteURL);
	}
	ImageObjectConstructor(urlAddress,absoluteURL);
    }

    ImageObject(URL urlAddress){
	String absoluteURL = urlAddress.toString();//should find absoluteURL for printing
	ImageObjectConstructor(urlAddress,absoluteURL);
    }

    //ImageObject(URL urlAddress, String absoluteURL){
    //	ImageObjectConstructor(urlAddress, absoluteURL);
    //}

    void ImageObjectConstructor(URL urlAddress, String absoluteURL){
        try {
            bImage = ImageIO.read(urlAddress);
            //File fileAddress = new File(relativeURL);
            //img = ImageIO.read(fileAddress)
	    setVars();
        } catch (IOException e) {
	    System.err.println("Error loading image " + absoluteURL + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
	    System.err.println("Image file " + absoluteURL + " could not be found " + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	} catch (NullPointerException e) {
	    System.err.println("Could not load image from file " + absoluteURL + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	}
    }

    void setVars(){
	width = bImage.getWidth(null);
	height = bImage.getHeight(null);
	if(height<width) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }

    void setToXasFileNotFound(){
	//set image to error icon
	//improvement: set the buffered image to a java graphics drawn X icon
	try{
	bImage = ImageIO.read(SysIcon.Error.imgURL);
	setVars();
	} catch (IOException e) {
	    System.err.println("Error loading image: " + e.toString());
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } 
    }
}