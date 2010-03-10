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



//BufferedImage takes 4MB per megapixel, so a 5megaPixel file is 20MB of java heap memory. On some VMs 64MB is max heap.

//Objectives from memory analysis
//-Clearing the buffered image from memory when it is not being displayed,keeping only a scaled down thumbnail in memory, and moving that to a fixed disk cache when not needed. 
//-Not storing more pixels than the screen can hold. If the image is zoomed in to a large extent we can reload the image at full size- only slowing the computer down when this is needed, not when scrolling through many images. 
//-When a filter is applied, particularly ShowAll, only the images about to be viewed should be loaded to prevent a sudden hit on CPU usage and a sudden expectation of masses of heap memory. 
//-Lastly using SwingWorker thread to allow GUI to be unnaffected by long loading times.

//Before doing Javadoc should make some variables private and use getters and setters.

enum Orientation {Landscape,Portrait} //For drawing/painting not for rotation

enum ImgSize {Screen,Max,Thumb;//,ThumbOnly,ThumbPreload;
    boolean isLarge(){
	if(this==Thumb/*||this==ThumbPreload*/) return false;
	return true;
    }
    boolean isThumb(){
	if(this==Screen||this==Max) return false;
	else return true;
    }
    public String toString(){
	switch (this){
	case Thumb: return "Thumb";
	//case ThumbPreload: return "ThumbFull";//ThumbFull requests size Thumb, but not clearing the full image as it may be used later
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
    boolean hasTriedQuickThumb = false;
    boolean hasTriedExtractDimensions = false;
    ImgSize currentLarge=ImgSize.Screen;//The size of the large bImage (Max or Screen)
    String imageID;
    int brightness = 50;
    int contrast = 50;
    boolean isInverted = false;
    boolean isLoading = false;
    boolean isBImageIcon = false;//Is the image a loading icon
    boolean isBThumbIcon = false;//Is the thumb a loading icon
    GUI mainGUI;
    ImageLoader imageLoader;
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

	//initVars()
	Dimension scrD = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = scrD.width;
	screenHeight = scrD.height;
    }

//    ImageObject(File inFile){
//	pathFile = inFile;
//	pathFile.getAbsolutePath();
//	initVars();
//    }

    boolean isFiltered(){
        return isFiltered;
    }
    void setFiltered(boolean newBool){
        isFiltered = newBool;
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
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile,absolutePath);
	if(Bwidth!=null) return Bwidth;
	getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory???????????????????
	return Bwidth;
    }
    int getHeightAndMake(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile,absolutePath);
	if(Bheight!=null) return Bheight;
	getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory????????????????????
	return Bheight;
    }
    int getWidthAndMakeBig(){
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile,absolutePath);
	if(Bwidth!=null) return Bwidth;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory???????????????
	return Bwidth;
    }
    int getHeightAndMakeBig(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile,absolutePath);
	if(Bheight!=null) return Bheight;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory??????????????
	return Bheight;
    }
    
    void getImageBySampling(){
        try {
            long start =Calendar.getInstance().getTimeInMillis();
        String ext = ImageObjectUtils.getFileExtLowercase(pathFile, absolutePath);
        if (ext == null)return;
        //ImageIO.scanForPlugins();
        Iterator readers = ImageIO.getImageReadersBySuffix(ext);
        ImageReader reader = (ImageReader) readers.next();
        if (readers.hasNext()) {
            reader = (ImageReader) readers.next();
            log.print(LogType.Debug, "next reader");
        }
        //ImageInputStream inputStream = ImageIO.createImageInputStream(pathFile);
        ImageInputStream inputStream = ImageIO.createImageInputStream(new RandomAccessFile(pathFile, "r"));
        reader.setInput(inputStream, false);
        ImageReadParam readParam = reader.getDefaultReadParam();
        //readParam.setSourceProgressivePasses(0,1);

        //To make thumnail at least 200 pixels, finds how many times bigger input is.
        //Looks at largest dimension as a square thumnail is limited by largest dimension.

        int sampleFactor = (int) Math.floor((double) Math.max((double) Bwidth, (double) Bheight) / ((double) 200));//9));
        if (sampleFactor <= 1) {
            return;//Full size image is less than thumbImage, return full size image.
        }
        readParam.setSourceSubsampling(sampleFactor, sampleFactor, 0, 0);//reads the image at 1/4 size
        bThumb = reader.read(0, readParam);
        reader.dispose();
        inputStream.close();
        
        if (bThumb != null) {
            log.print(LogType.Debug,"Read thumbnail from image "+absolutePath+"\n        -by reading every "+sampleFactor+" pixels");
            log.print(LogType.Debug, "   -sampled every " + sampleFactor + " pixels from " + Bwidth + "x" + Bheight + " in " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds");
            isQuickThumb = true;
        }
    } catch (IOException e) {
        log.print(LogType.DebugError, "Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
    }
        }

void getThumbQuick() {
    if (hasTriedQuickThumb) {
        return;
    }
    if (pathFile!= null) {
    BufferedImage tempImage = ImageObjectUtils.getThumbFromExif(pathFile, absolutePath);
    if (tempImage != null) {
        Log.Print(LogType.Debug, "Read exif of image " + absolutePath);
        bThumb = tempImage;
        mainGUI.thumbPanel.repaint();//not onResize
        return;
    }
        if(Bwidth==null)
        extractDimensionsFromFile(pathFile,absolutePath);
        if(Bwidth!=null) getImageBySampling();
    }
        hasTriedQuickThumb = true;
   

    //int thumbnum = reader.getNumThumbnails(0);//imageIndex = 0 as we look at the first image in file
    //log.print(LogType.Debug,"Has "+thumbnum+" thumbnails. Using reader " +reader.getClass().getName());
    //If a thumbnail image is present, it can be retrieved by calling:
    //int thumbailIndex = 0;
    //BufferedImage bi;
    //bi = reader.readThumbnail(imageIndex, thumbnailIndex);

    if (bThumb != null) {//Don't want to save sampled thumb as it will prevent better copy being saved
        ImageObjectUtils.saveThumbToFile(thumbPath ,pathFile, absolutePath, bThumb, imageID);
    }
}

  void extractDimensionsFromFile(File pathFile, String absolutePath) {
      if(isBImageIcon) return;
    if(hasTriedExtractDimensions) return;
    Dimension temp = ImageObjectUtils.getImageDimensionsSanslan(pathFile, absolutePath);
    if (temp != null) {
        Bwidth = temp.width;
        Bheight = temp.height;
    } else {
        log.print(LogType.DebugError, "Error reading exif dimensions of image " + absolutePath);
        //Bwidth = reader.getWidth(0);//gets the width of the first image in the file
        //Bheight = reader.getHeight(0);
    }
    hasTriedExtractDimensions = true;
  }

    int getWidthForThumb(){
	//if(bThumb!=null) return bThumb.getWidth();
	return getImage(ImgSize.Thumb).getWidth();
    }
    int getHeightForThumb(){
	//if(bThumb!=null) return bThumb.getHeight();
	return getImage(ImgSize.Thumb).getHeight();//Thumbnail height not image height or Bheight
    }

    void preload(ImgSize size){
        if(size==ImgSize.Max){
            //if file is known huge
            size=ImgSize.Screen;
        }
        getImage(size); //Should only do if size will not be huge- huge images use too much memory to preload at Max.
    }
    
    //gets thumbnail or full image
    BufferedImage getImage(ImgSize size) {
        //**//log.print(LogType.Debug,"Image requested: " + absolutePath + " at size " + size);
        if(size==ImgSize.Screen) size=ImgSize.Max;

        //If requested image already exists, return it.
        if (size.isThumb() && bThumb != null) {
            return localGetBufThumb();
        }
        if ((size == currentLarge) && (bImage != null)) {
            return localGetBufImage();
        }	
        
        if ((!isLoading)&&bThumb==null) {//If swing worker is loading an image, it will then write a thumb. Must avoid reading thumb at the same time as it is being written.
            getThumbIfCached();
            if(bThumb==null) {
                getThumbQuick();//if no longer null thumbIsQuick();//add thumb to list of thumbs which are ThumbQuicks, so they can be loaded while idle.
            }
            if (size == ImgSize.Thumb) {//If ImgSize.ThumbFull still want to create swingworker
                if (bThumb != null) {
                    return localGetBufThumb();
                }
            }
        }
        
        
        //Build large icon and small icon, return relevent.
        if (!isLoading) {
        loadViaSwingWorker(size);
        }

        
        if (bImage!=null){
            if(size.isLarge()){//if bImage exists but is not currentlarge, use for now but swingworker will replace
            return localGetBufImage();
            }
            //If ImgSize.ThumbOnly then clear bImage
            return localGetBufThumb();
        }
        else {
            //bImage is null, bThumb may be null, returns either bThumb or null
            return localGetBufThumb();
        }
    }

    //Get image using swing worker.
    //Must set a loading icon if not ready, and then update when done
    private void loadViaSwingWorker(ImgSize size) {
        isLoading = true;
        imageLoader = new ImageLoader(this, pathFile, absolutePath, size, imgType, thumbPath, imageID, screenWidth, screenHeight, thumbMaxW, thumbMaxH, bThumb);
        if (bImage == null) {
            bImage = createLoadingThumb();
            currentLarge = size;
            isBImageIcon = true;
            setVars(bImage);
        }
        if (bThumb == null) {
            getThumbIfCached();
        }
        if(bThumb==null){
            bThumb = createLoadingThumb();
            isBThumbIcon = true;
        }
        imageLoader.execute();
    }

    void setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgSize size,boolean wasCancelled){
        if(!wasCancelled){
            setVars(b);
            bImage = b;
            currentLarge = size;
        } else {
            bImage = null;
        }
        bThumb = thmb;
        isBThumbIcon=false;
        isBImageIcon=false;
        isLoading = false;
        if(bImage!=null) 
        if(isFiltered()) filterImage();
        mainGUI.mainPanel.onResize();//resize as image have changed dimensions
        mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
    }

    void setVars(BufferedImage img){
	if(bImage==null){ log.print(LogType.DebugError,"ERROR setting image size as image not initilized");return;}
	Bwidth = img.getWidth();
	Bheight = img.getHeight();
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
        return SysIcon.Loading.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    }

    void getThumbIfCached() {
        File checkFile = new File(thumbPath, (ImageObjectUtils.getSaveEncoding(pathFile,absolutePath)));
        if (checkFile.exists()) {
            boolean success = false;
            try {
                BufferedImage tempThumb = ImageIO.read(checkFile);
                bThumb = tempThumb; //Stops incomplete thumbnails from being returned if error while loading.
            } catch (IOException e) {
                log.print(LogType.Error,"Error opening thumbnail " + checkFile + "\nError was: "+e.toString());
            } catch (ArrayIndexOutOfBoundsException e){
                log.print(LogType.Error,"Error opening thumbnail " + checkFile + "\nError was: "+e.toString());
                e.printStackTrace();
            } catch (Exception e){
                log.print(LogType.Error,"Error opening thumbnail " + checkFile + "\nError was: "+e.toString());
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
    void flush() {//called externally
        if(imageLoader!=null){
            imageLoader.cancel(false);
        }
        clearMem();
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
