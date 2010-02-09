import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.util.Iterator;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.File;
import java.io.RandomAccessFile;
import javax.imageio.ImageIO;
import java.io.IOException;
//import javax.swing.ImageIcon;
import java.awt.RenderingHints;
//import javax.swing.JOptionPane;
import java.net.URISyntaxException;
import java.util.Calendar;

//Library of code under lib/
//Various image utilities. needed as default image reader could not read thumbnails from exif
import org.apache.sanselan.*;

//Use imageID for name for now but change to random key from DB.
//In thumbfile, store imageID,modified date,path,filesize and a quick checksum.
//Only load thumb if all same.

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
	case ThumbFull: return "Thumb";//ThumbFull requests size Thumb, but not clearing the full image as it may be used later
	case Screen: return "Screen";
	default: return "Max";//Max is not yet implemented.
	}
    }
}

class ImageObject { //could be updated to take a File instead, or a javase7 path
    private BufferedImage bImage = null;//Full size image, may be maxed at size of screen. set to null when not needed.
    private BufferedImage bThumb = null;//Created when large created, not removed.//Will be created from exif thumb
    String absolutePath;//Kept for error messages. Likely to be similar to pathFile.toString()
    File pathFile = null;
    Orientation iOri;
    private Integer Bwidth = null;
    private Integer Bheight = null;//make private- external programs do not know if initialized
    int screenWidth, screenHeight;
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    boolean isQuickThumb = false;
    ImgSize currentLarge;//The size of the large bImage (Max or Screen)
    String imageID;
    //String title,filename,comments?


    ImageObject(String inputPath,String currentID){
	String tempPath = "";
        imageID = currentID;
        System.out.println("Image:"+imageID+" has inPath: "+inputPath);
	try{
	    if(inputPath.startsWith("///\\\\\\")){
		tempPath = inputPath.substring(6);
		absolutePath = (GUI.class.getResource(tempPath)).toURI().getPath();//could be null
		//absolutePath = "etc/img/"+tempPath;//for use with jar files.
		if(absolutePath==null){
		    absolutePath=tempPath;
		}
		else pathFile = new File(absolutePath);
	    }
	    else {
		absolutePath = inputPath;
		tempPath = inputPath;
		pathFile = new File(inputPath);	  
	    }
	}catch (URISyntaxException e){
            pathFile = null;
	    System.err.println("Couldn't load image from " + tempPath + "\nError was- " + e.toString());
        }catch (NullPointerException e) {
	    pathFile = null;
	    System.err.println("Couldn't load image from file " + tempPath + "\nError was:- " + e.toString());
	}
	
	
	if(pathFile==null){
	    System.err.println("File could not be found at " + inputPath);
	}
	//ImageObjectConstructor);
	//getImage(ImgSize.Screen);

	initVars();
    }

//    ImageObject(File inFile){
//	pathFile = inFile;
//	pathFile.getAbsolutePath();
//	initVars();
//    }

    void initVars(){
	//java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
	//java.awt.Dimension  = toolkit.getScreenSize(); //catch HeadlessException
	Dimension scrD = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = scrD.width;
	screenHeight = scrD.height;
	manualReadImage();
    }

void flush(){//called externally
bImage=null;
}

    int getWidthAndMake(){
	if(Bheight!=null) return Bwidth;
	getImage(ImgSize.Screen);
	//note that the bImage should now be set to null to clear memory
	return Bwidth;
    }
    int getHeightAndMake(){
	if(Bheight!=null) return Bheight;
	getImage(ImgSize.Screen);
	//note that the bImage should now be set to null to clear memory
	return Bheight;
    }
    int getWidthAndMakeBig(){
	if(Bheight!=null) return Bwidth;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory
	return Bwidth;
    }
    int getHeightAndMakeBig(){
	if(Bheight!=null) return Bheight;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory
	return Bheight;
    }

void getThumbQuick(){
long start = Calendar.getInstance().getTimeInMillis();
	if(pathFile==null||Bwidth==null) return;
	try{
	    //IImageMetadata metadata = Sanselan.getMetadata(pathFile);
	    //if (metadata instanceof JpegImageMetadata) {
		//JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		//bThumb = jpegMetadata.getEXIFThumbnail();
	    //} //Wasn't working very well so has been removed
	    //if(bThumb!=null) System.out.println("Read exif of image " + absolutePath);
	    //System.out.println("Reading thumbnail from exif for file "+absolutePath+" with dimensions "+Bwidth+"x"+Bheight+"\n        -took "+(Calendar.getInstance().getTimeInMillis()-start));
start = Calendar.getInstance().getTimeInMillis();
	    String ext = null;
	    int pos = pathFile.getName().lastIndexOf(".");
	    if(pos>0 && pos<(pathFile.getName().length() - 1)){
		ext = pathFile.getName().substring(pos+1).toLowerCase();
	    }
	    if(ext==null) {
		System.err.println("Unable to get file extension from "+absolutePath);
		return;
	    }
	    int sampleFactor = (int)Math.floor((double)Math.max((double)Bwidth,(double)Bheight)/((double)200));//9));
	    if(sampleFactor<=1) return;
//ImageIO.scanForPlugins();
	    Iterator readers = ImageIO.getImageReadersBySuffix(ext);
	    ImageReader reader = (ImageReader)readers.next();
if(readers.hasNext()) {reader = (ImageReader)readers.next(); System.out.println("next reader");}
	    //ImageInputStream inputStream = ImageIO.createImageInputStream(pathFile);
            ImageInputStream inputStream = ImageIO.createImageInputStream(new RandomAccessFile(pathFile,"r"));
	    reader.setInput(inputStream,false);
	    ImageReadParam readParam = reader.getDefaultReadParam();
	    //readParam.setSourceProgressivePasses(0,1);
	    //To make thumnail at least 200 pixels, finds how many times bigger input is.
	    //Looks at largest dimension as a square thumnail is limited by largest dimension.
	    readParam.setSourceSubsampling(sampleFactor,sampleFactor,0,0);//reads the image at 1/4 size
	    bThumb = reader.read(0,readParam);
	    reader.dispose();
	    inputStream.close();

	    if(bThumb!=null) {System.out.println("Read thumbnail from image "+absolutePath+"\n        -by reading every "+sampleFactor+" pixels for image Dimensions "+Bwidth+"x"+Bheight+"\n        -took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to sample image to read thumb"); isQuickThumb = true;}
	    //Bwidth = reader.getWidth(0);//gets the width of the first image in the file
	    //Bheight = reader.getHeight(0);
	} catch (IOException e) {
	    System.err.println("Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	} //catch (ImageReadException e) {
	//System.err.println("Error reading exif of image " + absolutePath + "\nError was: " + e.toString());
	//}

	//int thumbnum = reader.getNumThumbnails(0);//imageIndex = 0 as we look at the first image in file
	//System.out.println("Has "+thumbnum+" thumbnails. Using reader " +reader.getClass().getName());
	//If a thumbnail image is present, it can be retrieved by calling:
	//int thumbailIndex = 0;
	//BufferedImage bi;
	//bi = reader.readThumbnail(imageIndex, thumbnailIndex);
}

    //gets files dimension and thumbnail without loading it to memory
    void manualReadImage(){
	if(pathFile==null) return;
	try{
	    // get the image's width and height. 
	    Dimension image_d = Sanselan.getImageSize(pathFile);
	    Bwidth = image_d.width;
	    Bheight = image_d.height;	    
	} catch (IOException e) {
	    System.err.println("Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	}  catch (ImageReadException e) {
	System.err.println("Error reading exif of image " + absolutePath + "\nError was: " + e.toString());
	}
	if(Bwidth==null) System.err.println("Error reading exif dimensions of image " + absolutePath);
	//System.out.println("Dimensions "+Bwidth+"x"+Bheight+" suceesfully got for " +absolutePath);
    }

    int getWidthForThumb(){
	if(Bwidth!=null) return Bwidth;
	return getWidthAndMake();//Makes error icon if pathFile was null. Returns value if present.
    }
    int getHeightForThumb(){
	if(Bheight!=null) return Bheight;
	return getHeightAndMake();//Returns Bheight if manualReadImage worked, makes an errror icon if path was null
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
	if(size.isThumb()) {
	    getThumbQuick();
	    if(bThumb!=null) return bThumb;
//should now add rendering the thumb properly to a task list for another thread
	}
        System.out.print("...");
        try {
long start = Calendar.getInstance().getTimeInMillis();
//ImageIO.setUseCache(true);
//ImageIO.setCacheDirectory(pathFile);
            bImage = ImageIO.read(pathFile);
System.out.println("Loading image "+absolutePath+"\n      -Took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to read image to memory");
start = Calendar.getInstance().getTimeInMillis();

	    if(size==ImgSize.Screen||size==ImgSize.ThumbFull){//&& not thumb only (as this would be extra work)
		bImage = makeScreenImg(bImage);
		currentLarge = ImgSize.Screen;
	    }
	    else {currentLarge = ImgSize.Max;}

System.out.println("      -Took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to process image");
start = Calendar.getInstance().getTimeInMillis();
	    bThumb =  makeThumb(bImage);
            System.out.println("      -Took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to process thumbnail");
	    setVars();
System.out.println("      -Took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to get width,height&orientation");
start = Calendar.getInstance().getTimeInMillis();
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
	System.out.println("Made thumb for "+absolutePath);
	if(size==ImgSize.ThumbFull) return bThumb;
	else{
	    //as only thumb needed, clear bImage
	    bImage = null;
	    return bThumb;
	}
    }

    BufferedImage makeThumb(BufferedImage bigImg){//quick one image read to bimage
	Dimension iconWH = scaleDownToMax(bigImg.getWidth(),bigImg.getHeight(),thumbMaxW,thumbMaxH);
	if(!(iconWH.width<bigImg.getWidth())) return bigImg;

	//Image tempimage = bigIcon.getImage();
	BufferedImage tempimage =bigImg;

        Graphics2D g2 = (new BufferedImage(iconWH.width,iconWH.height,BufferedImage.TYPE_3BYTE_BGR)).createGraphics();//TYPE_INT_RGB
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.drawImage(tempimage, 0, 0, iconWH.width,iconWH.height, null);
        g2.dispose();
	return tempimage;	
    }

    //could merge two functions
    BufferedImage makeScreenImg(BufferedImage bigImg){
	Dimension iconWH = scaleDownToMax(bigImg.getWidth(),bigImg.getHeight(),screenWidth,screenHeight);
	if(!(iconWH.width<bigImg.getWidth())) return bigImg;
	//Image tempimage = bigIcon.getImage();
	BufferedImage tempimage =bigImg;

        Graphics2D g2 = (new BufferedImage(iconWH.width,iconWH.height,BufferedImage.TYPE_3BYTE_BGR)).createGraphics();//TYPE_INT_RGB takes 4bytes, this takes 3. Less memory used
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//might not be avaliable on all systems // might not have spelt biquibic right
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(tempimage, 0, 0, iconWH.width,iconWH.height, null);
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
	bImage = null;
	//bThumb = null;
    }

    void destroy(){
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
    static Dimension scaleToMax(int inW, int inH, int maxW, int maxH) {
	float f_inW,f_inH,f_maxW,f_maxH;
	f_inW = inW;
	f_inH = inH;
	f_maxW = maxW;
	f_maxH = maxH;
	//int[] outWH = new int[2];
Dimension outWH = new Dimension();
	if ( (f_inW/f_inH)<(f_maxW/f_maxH) ) {
	    //narrower at same scale
	    outWH.height = maxH;
	    outWH.width = Math.round((f_maxH / f_inH)* f_inW);
	}
	else {
	    //wider at same scale
	    outWH.width = maxW;
	    outWH.height = Math.round(( f_maxW / f_inW)* f_inH);
	}
	return outWH;
    }
    static Dimension scaleDownToMax(int inW, int inH, int maxW, int maxH) {
	Dimension tempWH = scaleToMax(inW, inH, maxW, maxH);
	if(tempWH.width<inW) return tempWH;
	return new Dimension(inW,inH);
    }
    static Dimension useMaxMax(int inW, int inH, int maxW, int maxH){
        int outW,outH;
        outW = Math.max(inW, maxW);
        outH = Math.max(inH,maxH);
        return new Dimension(outW,outH);
    }
}
