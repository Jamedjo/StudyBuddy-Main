import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import java.util.Iterator;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.RenderingHints;
import java.awt.image.RescaleOp;
import java.net.URISyntaxException;
import java.util.Calendar;

//Library of code under lib/
//Various image utilities. needed as default image reader could not read thumbnails from exif
import org.apache.sanselan.*;

//use jpeg thumbs where availiable

//BufferedImage takes 4MB per megapixel, so a 5megaPixel file is 20MB of java heap memory. On some VMs 64MB is max heap.

//Objectives from memory analysis
//-Clearing the buffered image from memory when it is not being displayed,keeping only a scaled down thumbnail in memory, and moving that to a fixed disk cache when not needed. 
//-Not storing more pixels than the screen can hold. If the image is zoomed in to a large extent we can reload the image at full size- only slowing the computer down when this is needed, not when scrolling through many images. 
//-When a filter is applied, particularly ShowAll, only the images about to be viewed should be loaded to prevent a sudden hit on CPU usage and a sudden expectation of masses of heap memory. 
//-Lastly using SwingWorker thread to allow GUI to be unnaffected by long loading times.

//Before doing Javadoc should make some variables private and use getters and setters.

enum Orientation {Landscape,Portrait} //For drawing/painting not for rotation

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
	case ThumbFull: return "ThumbFull";//ThumbFull requests size Thumb, but not clearing the full image as it may be used later
	case Screen: return "Screen";
	default: return "Max";//Max is not yet implemented.
	}
    }
}

class ImageObject { //could be updated to take a File instead, or a javase7 path
    Log log = new Log(false);
    private BufferedImage bImage = null;//Full size image, may be maxed at size of screen. set to null when not needed.
    private BufferedImage bThumb = null;//Created when large created, not removed.//Will be created from exif thumb
    private BufferedImage bImageFilt = null;
    private BufferedImage bThumbFilt = null;
    private boolean isFiltered = false;
    private final int imgType = BufferedImage.TYPE_INT_RGB;
    String absolutePath;//Kept for error messages. Likely to be similar to pathFile.toString()
    File pathFile = null;
    File thumbPath = null;
    Orientation iOri;
    private Integer Bwidth = null;
    private Integer Bheight = null;//make private- external programs do not know if initialized
    int screenWidth, screenHeight;
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    boolean isQuickThumb = false;
    ImgSize currentLarge;//The size of the large bImage (Max or Screen)
    String imageID;
    int brightness = 50;
    int contrast = 50;
    boolean isInverted = false;
    boolean isLoading = false;
    boolean isBImageIcon = false;//Is the image a loading icon
    boolean isBThumbIcon = false;//Is the thumb a loading icon
    GUI mainGUI;
    //String title,filename,comments?

    ImageObject(String inputPath,String currentID,File thumbnailPath,GUI gui){
	mainGUI = gui;
        String tempPath = "";
        imageID = currentID;
        thumbPath = thumbnailPath;
        log.print(LogType.Debug,"Image:"+imageID+" has inPath: "+inputPath);
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
	    log.print(LogType.Error,"Couldn't load image from " + tempPath + "\nError was- " + e.toString());
        }catch (NullPointerException e) {
	    pathFile = null;
	    log.print(LogType.Error,"Couldn't load image from file " + tempPath + "\nError was:- " + e.toString());
	}
	
	
	if(pathFile==null){
	    log.print(LogType.Error,"File could not be found at " + inputPath);
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
    }
    boolean isFiltered(){
        return isFiltered;
    }
    void setFiltered(boolean newBool){
        isFiltered = newBool;
    }
    void flush() {//called externally
        bImage = null;
    }
    private BufferedImage localGetBufImage(){
        if(isFiltered) return bImageFilt;
        return bImage;
    }
    private BufferedImage localGetBufThumb(){
        if(isFiltered) return bThumbFilt;
        return bThumb;
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
	    //if(bThumb!=null) log.print(LogType.Debug,"Read exif of image " + absolutePath);
	    //log.print(LogType.Debug,"Reading thumbnail from exif for file "+absolutePath+" with dimensions "+Bwidth+"x"+Bheight+"\n        -took "+(Calendar.getInstance().getTimeInMillis()-start));
start = Calendar.getInstance().getTimeInMillis();
	    String ext = null;
	    int pos = pathFile.getName().lastIndexOf(".");
	    if(pos>0 && pos<(pathFile.getName().length() - 1)){
		ext = pathFile.getName().substring(pos+1).toLowerCase();
	    }
	    if(ext==null) {
		log.print(LogType.Error,"Unable to get file extension from "+absolutePath);
		return;
	    }
	    int sampleFactor = (int)Math.floor((double)Math.max((double)Bwidth,(double)Bheight)/((double)200));//9));
	    if(sampleFactor<=1) return;
//ImageIO.scanForPlugins();
	    Iterator readers = ImageIO.getImageReadersBySuffix(ext);
	    ImageReader reader = (ImageReader)readers.next();
if(readers.hasNext()) {reader = (ImageReader)readers.next(); log.print(LogType.Debug,"next reader");}
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

	    //if(bThumb!=null) {log.print(LogType.Debug,"Read thumbnail from image "+absolutePath+"\n        -by reading every "+sampleFactor+" pixels for image Dimensions "+Bwidth+"x"+Bheight+"\n        -took "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds to sample image to read thumb"); isQuickThumb = true;}
            if(bThumb!=null) {log.print(LogType.Debug,"        -sampled every "+sampleFactor+" pixels from "+Bwidth+"x"+Bheight+" in "+(Calendar.getInstance().getTimeInMillis()-start)+" milliseconds"); isQuickThumb = true;}
	    //Bwidth = reader.getWidth(0);//gets the width of the first image in the file
	    //Bheight = reader.getHeight(0);
	} catch (IOException e) {
	    log.print(LogType.Error,"Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	} //catch (ImageReadException e) {
	//log.print(LogType.Error,"Error reading exif of image " + absolutePath + "\nError was: " + e.toString());
	//}

	//int thumbnum = reader.getNumThumbnails(0);//imageIndex = 0 as we look at the first image in file
	//log.print(LogType.Debug,"Has "+thumbnum+" thumbnails. Using reader " +reader.getClass().getName());
	//If a thumbnail image is present, it can be retrieved by calling:
	//int thumbailIndex = 0;
	//BufferedImage bi;
	//bi = reader.readThumbnail(imageIndex, thumbnailIndex);

        if(bThumb!=null) ImageObjectUtils.saveThumbToFile(thumbPath, absolutePath, bThumb, imageID);
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
	    log.print(LogType.Error,"Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	}  catch (ImageReadException e) {
	log.print(LogType.Error,"Error reading exif of image " + absolutePath + "\nError was: " + e.toString());
	}
	if(Bwidth==null) log.print(LogType.Error,"Error reading exif dimensions of image " + absolutePath);
	//log.print(LogType.Debug,"Dimensions "+Bwidth+"x"+Bheight+" suceesfully got for " +absolutePath);
    }

    int getWidthForThumb(){
	if(bThumb!=null) return bThumb.getWidth();
	return getWidthAndMake();//Makes error icon if pathFile was null. Returns value if present.
    }
    int getHeightForThumb(){
	if(bThumb!=null) return bThumb.getHeight();
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

    void preload(ImgSize size){
        if(size==ImgSize.Max){
            //if file is known huge
            size=ImgSize.Screen;
        }
        getImage(size); //Should only do if size will not be huge- huge images use too much memory to preload at Max.
    }

    BufferedImage getImage(ImgSize size) {
        //**//log.print(LogType.Debug,"Image requested: " + absolutePath + " at size " + size);
        //gets thumbnail or full image
        if (size.isThumb() && bThumb != null) {
            return localGetBufThumb();
        }
        if (size == currentLarge && bImage != null) {
            return localGetBufImage();//If there is an image which matches size
        }	//Build large icon and small icon, return relevent.
        if (size == ImgSize.Thumb) {//If thumbfull do long way
            getThumbIfCached();
            if (bThumb != null) {
                return localGetBufThumb();
            }
            getThumbQuick();
            if (bThumb != null) {
                return localGetBufThumb();
            }
//should now add rendering the thumb properly to a task list for another thread
        }
        log.print(LogType.Debug,"...");

        if (!isLoading) {
        loadViaSwingWorker(size);
        }


        if (bImage == null) {
            return null;//if big is null, so is thumb.
        }
        if (size.isLarge()) {
            return localGetBufImage();
        }
        //**//log.print(LogType.Debug,"Made thumb for "+absolutePath);
        if (size == ImgSize.ThumbFull) {
            return localGetBufThumb();
        } else {
            //as only thumb needed, clear bImage
            bImage = null;
            return localGetBufThumb();
        }
    }

    //Get image using swing worker.
    //Must set a loading icon if not ready, and then update when done
    private void loadViaSwingWorker(ImgSize size) {
        isLoading = true;
        ImageLoader imageLoader = new ImageLoader(this, pathFile, absolutePath, size, imgType, thumbPath, imageID, screenWidth, screenHeight, thumbMaxW, thumbMaxH, bThumb);
        imageLoader.execute();
        if (bImage == null) {
            bImage = createLoadingThumb();
            isBImageIcon = true;
        setVars();
        }
        if (bThumb == null) {
            bThumb = createLoadingThumb();
            isBThumbIcon = true;
        }
    }

    void setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgSize size){
        bImage = b;
        bThumb = thmb;
        isLoading = false;
        currentLarge = size;
        isBImageIcon=false;
        isBThumbIcon=false;
        setVars();
        mainGUI.mainPanel.onResize();
        mainGUI.thumbPanel.onResize();
    }

    void setVars(){
	if(bImage==null) log.print(LogType.Error,"ERROR getting image size as image not initilized");
	Bwidth = bImage.getWidth();
	Bheight = bImage.getHeight();
	if(Bheight<Bwidth) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }

    void saveFullToPath(String path){
        try{
            File f = new File(path);
            ImageIO.write(localGetBufImage(),"jpg",f);//should use same format as file
        } catch (IOException e){
            log.print(LogType.Error,"Error creating saving image: "+absolutePath+"\nTo path: "+path);
        }
    }

    private BufferedImage createLoadingThumb(){
        BufferedImage tempB = new BufferedImage(SysIcon.Loading.Icon.getIconWidth()*2,SysIcon.Loading.Icon.getIconHeight()*2,BufferedImage.TYPE_INT_ARGB);
        SysIcon.Loading.Icon.paintIcon(null, tempB.createGraphics(), SysIcon.Loading.Icon.getIconWidth()/2, SysIcon.Loading.Icon.getIconHeight()/2);
        return tempB;
    }

    void getThumbIfCached() {
        File checkFile = new File(thumbPath, ImageObjectUtils.getSaveEncoding(imageID));
        if (checkFile.exists()) {
            try {
                bThumb = ImageIO.read(checkFile);
            } catch (IOException e) {
                log.print(LogType.Error,"Error opening thumbnail " + checkFile + "\nError was: " + e.toString());
            }
        }
    }

    void filterImage(){
        if((contrast==50)&&(brightness==50)&&(!isInverted)) {
            isFiltered = false;
            return;
        }
        isFiltered = true;
        filterBufImage(true);
        filterBufImage(false);
    }
    private void filterBufImage(boolean isThumb){
        BufferedImage srcImg;
        if(isThumb){
            srcImg=bThumb;
        } else{
            srcImg=bImage;
        }
        if(srcImg==null){
            return;//should change to a call which sets it up.
        }
        RenderingHints hints = null;
        float offset = (brightness-50f)*5.10f;
        float scale = 1.0f+(contrast-50f)/50f;
        if(isInverted){
            offset = 255f-offset;
            scale = (-scale);
        }
        RescaleOp op = new RescaleOp(scale,offset,hints);
        if(isThumb){
            if(bThumbFilt==null) bThumbFilt = new BufferedImage(srcImg.getWidth(),srcImg.getHeight(),imgType);//*/op.createCompatibleDestImage(srcImg, null);
        } else{
            if(bImageFilt==null) bImageFilt = new BufferedImage(srcImg.getWidth(),srcImg.getHeight(),imgType);//*/op.createCompatibleDestImage(srcImg, null);
        }
        if(isThumb){
        op.filter(srcImg,bThumbFilt);
        } else op.filter(srcImg,bImageFilt);
    }

    void clearMem(){
	//clears the full size image.
	bImage = null;
        bImageFilt=null;
	//bThumb = null;
    }

    void destroy(){
	bImage = null;
	bThumb = null;
        bImageFilt=null;
        bThumbFilt=null;
	pathFile = null;
	absolutePath = null;
    }
}
