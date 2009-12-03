import javax.imageio.ImageReader;
import java.util.Iterator;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
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

//use jpeg thumbs where availiable

//BufferedImage takes 4MB per megapixel, so a 5megaPixel file is 20MB of java heap memory. On some VMs 64MB is max heap.

//Objectives from memory analysis
//-Clearing the buffered image from memory when it is not being displayed,keeping only a scaled down thumbnail in memory, and moving that to a fixed disk cache when not needed. 
//-Not storing more pixels than the screen can hold. If the image is zoomed in to a large extent we can reload the image at full size- only slowing the computer down when this is needed, not when scrolling through many images. 
//-When a filter is applied, particularly ShowAll, only the images about to be viewed should be loaded to prevent a sudden hit on CPU usage and a sudden expectation of masses of heap memory. 
//-Lastly using SwingWorker thread to allow GUI to be unnaffected by long loading times.

//Before doing Javadoc should make some variables private and use getters and setters.

enum Orientation {Landscape,Portrait}

enum ImgSize {Thumb,Screen,Max,ThumbFull;
    boolean isLarge(){
	if(this==Thumb||this==ThumbFull) return false;
	else return true;
    }
    boolean isThumb(){
	if(this==Screen||this==Max) return false;
	else return true;
    }
    public String toString(){
	switch (this){
	    case Thumb: return "ThumbOnly";
	    case ThumbFull: return "Thumb";//ThumbFull requests size Thumb, but not flushing the full image as it may be used later
	    case Screen: return "Screen";
	    default: return "Max";//Max is not yet implemented.
	}
    }
}

class ImageObject { //could be updated to take a File instead, or a javase7 path
    private BufferedImage bImage = null;//Full size image, may be maxed at size of screen. Flushed when not needed.
    private BufferedImage bThumb = null;//Created when large created, not removed.//Will be created from exif thumb
    String absolutePath;//Kept for error messages. Likely to be similar to pathFile.toString()
    File pathFile;
    Orientation iOri;
    private Integer Bwidth = null;
    private Integer Bheight = null;//make private- external programs do not know if initialized
    int screenWidth, screenHeight;
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    ImgSize currentLarge;//The size of the large bImage (Max or Screen)
    //String imageID;
    //String title,filename,comments?


    ImageObject(String inputPath){
	String tempPath = "";
	try{
	    if(inputPath.startsWith("///\\\\\\")){
		tempPath = inputPath.substring(6);
		absolutePath = (GUI.class.getResource(tempPath)).getPath();//could be null
		pathFile = new File(absolutePath);
	    }
	    else {
		absolutePath = inputPath;
		tempPath = inputPath;
		pathFile = new File(inputPath);	  
	    }
	}catch (NullPointerException e) {
	    pathFile = null;
	    System.err.println("Couldn't load image from file " + tempPath + "\nError was: " + e.toString());
	}
	
	
	if(pathFile==null){
	    System.err.println("File could not be found at " + inputPath);
	}
	//ImageObjectConstructor);
	//getImage(ImgSize.Screen);

	initVars();
    }

    ImageObject(File inFile){
	pathFile = inFile;
	pathFile.getAbsolutePath();
	initVars();
    }

    void initVars(){
	//java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
	//java.awt.Dimension  = toolkit.getScreenSize(); //catch HeadlessException
	Dimension scrD = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = scrD.width;
	screenHeight = scrD.height;
    }

    int getWidthAndMake(){
	if(Bheight!=null) return Bwidth;
	getImage(ImgSize.Screen);
	//note that the bImage should now be flushed to clear memory
	return Bwidth;
    }
    int getHeightAndMake(){
	if(Bheight!=null) return Bheight;
	getImage(ImgSize.Screen);
	//note that the bImage should now be flushed to clear memory
	return Bheight;
    }

    //gets files dimension and thumbnail without loading it to memory
    void manualReadImage(){
	if(pathFile==null) return;
	String ext = null;
	int pos = pathFile.getName().lastIndexOf(".");
	if(pos>0 && pos<(absolutePath.length() - 1)){
	    ext = absolutePath.substring(pos+1).toLowerCase();
	}
	if(ext==null) {
	    System.err.println("Unable to get file extension from "+absolutePath);
	    return;
	}

	Iterator readers = ImageIO.getImageReadersBySuffix("jpg");
	ImageReader reader = (ImageReader)readers.next();

	try{
	    ImageInputStream inputStream = ImageIO.createImageInputStream(pathFile);
	    reader.setInput(inputStream,false);

	    Bwidth = reader.getWidth(0);//gets the width of the first image in the file
	    Bheight = reader.getHeight(0);
	} catch (IOException e) {
	    System.err.println("Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	}
    
	//reader.getNumThumbnails(imageIndex);
	//If a thumbnail image is present, it can be retrieved by calling:
	//int thumbailIndex = 0;
	//BufferedImage bi;
	//bi = reader.readThumbnail(imageIndex, thumbnailIndex);
	System.out.println("Dimensions "+Bwidth+"x"+Bheight+"suceesfully got for " +absolutePath);
	stophere();
    }
    void stophere(){}

    int getWidthForThumb(){
	if(bImage!=null) return Bwidth;
	manualReadImage();	
	return getWidthAndMake();//Makes error icon if pathFile was null. Returns value if present.
    }
    int getHeightForThumb(){
	if(bImage!=null) return Bheight;
	manualReadImage();
	return getHeightAndMake();//Returns Bheight if manualReadImage worked, makes an errror icon if path was null
    }

    void flush(){
	if(bImage!=null) bImage.flush();
    }

    //ImageObject(URL urlAddress){
    //absolutePath = urlAddress.toString();//should find inputPath for printing
    //initVars();
    ////ImageObjectConstructor();
    ////getImage(ImgSize.Screen);
    //}

    //ImageObject(URL urlAddress, String inputPath){
    //	ImageObjectConstructor(urlAddress, inputPath);
    //}

    //BufferedImage extractIcon(...)

    BufferedImage getImage(ImgSize size){
	System.out.println("Image requested: " + absolutePath + " at size " + size);
	//gets thumbnail or full image
	if(size.isThumb()&&bThumb!=null) return bThumb;
	if(size==currentLarge&&bImage!=null) return bImage;//If there is an image which matches size
	//Build large icon and small icon, return relevent.
        try {
            bImage = ImageIO.read(pathFile);//VERY BAD code, loads all images into limited memory
	    if(size!=ImgSize.Max){//&& not thumb only (as this would be extra work)
		bImage = makeScreenImg(bImage);
		currentLarge = ImgSize.Screen;
	    }
	    else currentLarge = ImgSize.Max;
	    bThumb =  makeThumb(bImage);
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
	    bImage.flush();//	if(bImage!=null) not needed as would have returned
	    return bThumb;
	}
    }

    BufferedImage makeThumb(BufferedImage bigImg){
	int[] iconWH = scaleDownToMax(bigImg.getWidth(),bigImg.getHeight(),thumbMaxW,thumbMaxH);
	if(!(iconWH[0]<bigImg.getWidth())) return bigImg;

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

    //could merge two functions
    BufferedImage makeScreenImg(BufferedImage bigImg){
	int[] iconWH = scaleDownToMax(bigImg.getWidth(),bigImg.getHeight(),screenWidth,screenHeight);
	if(!(iconWH[0]<bigImg.getWidth())) return bigImg;
	//Image tempimage = bigIcon.getImage();
	BufferedImage tempimage =bigImg;

        Graphics2D g2 = (new BufferedImage(iconWH[0],iconWH[1],BufferedImage.TYPE_3BYTE_BGR)).createGraphics();//TYPE_INT_RGB takes 4bytes, this takes 3. Less memory used
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//might not be avaliable on all systems // might not have spelt biquibic right
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(tempimage, 0, 0, iconWH[0],iconWH[1], null);
        g2.dispose();

	return tempimage;	
    }

    void setVars(){
	if(bImage==null) System.err.println("ERROR getting image size as image not initilized");
	Bwidth = bImage.getWidth();
	Bheight = bImage.getHeight();
	if(Bheight<Bwidth) iOri = Orientation.Landscape;
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

    void clearMem(){
	//clears the full size image.
	if(bImage!=null) bImage.flush();
	bImage = null;
	//bThumb = null;
    }

    void destroy(){
	if(bImage!=null) bImage.flush();
	if(bThumb!=null) bThumb.flush();
	bImage = null;
	bThumb = null;
	pathFile = null;
	absolutePath = null;
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
    static int[] scaleDownToMax(int inW, int inH, int maxW, int maxH) {
	int[] tempWH = scaleToMax(inW, inH, maxW, maxH);
	if(tempWH[0]<inW) return tempWH;
	return new int[] {inW,inH};
    }
}
