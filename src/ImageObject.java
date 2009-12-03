import java.net.URL;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;
import java.net.MalformedURLException;
import java.io.IOException;
//import javax.swing.ImageIcon;
import java.awt.RenderingHints;
//import javax.swing.JOptionPane;

//BufferedImage takes 4MB per megapixel, so a 5megaPixel file is 20MB of java heap memory. On some VMs 64MB is max heap.

//Objectives from memory analysis
//-Clearing the buffered image from memory when it is not being displayed,keeping only a scaled down thumbnail in memory, and moving that to a fixed disk cache when not needed. 
//-Not storing more pixels than the screen can hold. If the image is zoomed in to a large extent we can reload the image at full size- only slowing the computer down when this is needed, not when scrolling through many images. 
//-When a filter is applied, particularly ShowAll, only the images about to be viewed should be loaded to prevent a sudden hit on CPU usage and a sudden expectation of masses of heap memory. 
//-Lastly using SwingWorker thread to allow GUI to be unnaffected by long loading times.

//Before doing Javadoc should make some variables private and use getters and setters.

enum Orientation {Landscape,Portrait}

enum ImgSize {Thumb,Screen,Full,ThumbFull;
    boolean isLarge(){
	if(this==Thumb||this==ThumbFull) return false;
	else return true;
    }
    boolean isThumb(){
	if(this==Screen||this==Full) return false;
	else return true;
    }
    public String toString(){
	switch (this){
	    case Thumb: return "ThumbOnly";
	    case ThumbFull: return "Thumb";//ThumbFull requests size Thumb, but not flushing the full image as it may be used later
	    case Screen: return "Screen";
	    default: return "Full";
	}
    }
}

class ImageObject {//could be updated to take a File instead, or a javase7 path
    private BufferedImage bImage = null;//Full size image, may be maxed at size of screen. Flushed when not needed.
    private BufferedImage bThumb = null;//Created when large created, not removed.
    URL urlAddress;
    String absolutePath;//Kept for error messages. Likely to be similar to urlAddress.toString()
    Orientation iOri;
    private int width,height;//make private- external programs do not know if initialized
    //String imageID;
    //String title,filename,comments?
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;

    ImageObject(String absoluteURL){
	if(absoluteURL.startsWith("///\\\\\\")){
	    String relativeURL = absoluteURL.substring(6);
	    //System.out.println(relativeURL +  " is relative and absolute is " + absoluteURL);
	    urlAddress = GUI.class.getResource(relativeURL); //could be null
	    absolutePath = urlAddress.toString();
	}
	else {
	    File file = new File(absoluteURL);
	    try{
		absolutePath = absoluteURL;
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
	//ImageObjectConstructor();
	//getImage(ImgSize.Full);
    }
    int getWidthAndMake(){
	if(bImage!=null) return width;
	getImage(ImgSize.Full);
	//note that the bImage should now be flushed to clear memory
	return width;
    }
    int getHeightAndMake(){
	if(bImage!=null) return height;
	getImage(ImgSize.Full);
	//note that the bImage should now be flushed to clear memory
	return height;
    }
    void flush(){
	bImage.flush();
    }

    ImageObject(URL urlAddress){
	absolutePath = urlAddress.toString();//should find absoluteURL for printing
	//ImageObjectConstructor();
	//getImage(ImgSize.Full);
    }

    //ImageObject(URL urlAddress, String absoluteURL){
    //	ImageObjectConstructor(urlAddress, absoluteURL);
    //}

    BufferedImage getImage(ImgSize size){
	System.out.println("Image requested: " + absolutePath + " at size " + size);
	//gets thumbnail or full image
	if(size.isThumb()&&bThumb!=null) return bThumb;
	if(size.isLarge()&&bImage!=null) return bImage;
	//Build large icon and small icon, return relevent.
        try {
            bImage = ImageIO.read(urlAddress);//VERY BAD code, loads all images into limited memory

	    bThumb =  makeThumb(bImage);
            //File fileAddress = new File(relativeURL);
            //img = ImageIO.read(fileAddress)
	    setVars();
        } catch (IOException e) {
	    System.err.println("Error loading image " + absolutePath + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
	    System.err.println("Image file " + absolutePath + " could not be found " + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	} catch (NullPointerException e) {
	    System.err.println("Could not load image from file " + absolutePath + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	} catch(java.lang.OutOfMemoryError e){
           System.err.println("Fatal Error. Out of heap memory.\nSwingWorker should be used in code, and not all images should be buffered");
	}
	if(bImage==null) return null;//if big is null, so is thumb.
	if(size.isLarge()) return bImage;
	if(size==ImgSize.ThumbFull) return bThumb;
	else{
	    //as only thumb needed, flush bImage
	    bImage.flush();
	    return bThumb;
	}
    }

    BufferedImage makeThumb(BufferedImage bigImg){
	int[] iconWH = scaleToMax(bigImg.getWidth(),bigImg.getHeight(),thumbMaxW,thumbMaxH);

	//Image tempimage = bigIcon.getImage();
	BufferedImage tempimage =bigImg;

        Graphics2D g2 = (new BufferedImage(iconWH[0],iconWH[1],BufferedImage.TYPE_3BYTE_BGR)).createGraphics();//TYPE_INT_RGB
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.drawImage(tempimage, 0, 0, iconWH[0],iconWH[1], null);
        g2.dispose();
	
	
	
	
	
	
	return tempimage;
	
	
    }


    void clearMem(){
	//clears the full size image.
	bImage.flush();
	bImage = null;
	//bThumb = null;
    }

    void setVars(){
	if(bImage==null) System.err.println("ERROR getting image size as image not initilized");
	width = bImage.getWidth();
	height = bImage.getHeight();
	if(height<width) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }

    void setToXasFileNotFound(){
	//set image to error icon
	//improvement: set the buffered image to a java graphics drawn X icon
	try{
	bImage = ImageIO.read(SysIcon.Error.imgURL);
	bThumb = ImageIO.read(SysIcon.Error.imgURL);
	setVars();
	} catch (IOException e) {
	    System.err.println("Error loading image: " + e.toString());
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } 
    }

    //Finds maximum with and height somthing can be scaled to, without changing aspect ratio
    //Takes the dimensions of the object inW and inH
    //and the dimensions of the box it is to be fitted into maxW and maxH
    //Returns (Width,Height) as an array of two integers.
    //Not sure which class it belongs in.
    static int[] scaleToMax(int inW, int inH, int maxW, int maxH) {
	float f_inW,f_inH,f_maxW,f_maxH;
	f_inW = inW;
	f_inH = inH;
	f_maxW = maxW;
	f_maxH = maxH;
	int[] outWH = new int[2];
	if ( (f_inW/f_inH)<(f_maxW/f_maxH) ) {
	    //narrower at same scale
	    outWH[1] = maxH;
	    outWH[0] = Math.round((f_maxH / f_inH)* f_inW);
	}
	else {
	    //wider at same scale
	    outWH[0] = maxW;
	    outWH[1] = Math.round(( f_maxW / f_inW)* f_inH);
	}
	return outWH;
    }
}
