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
	if(this==Thumb) return false;/*||this==ThumbPreload*/
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
class ImageItem{
    //Will hold image, thumb, filtered version, current size, alternate place for icons and their sizes
    //Will include getters which return relevent image
    //Will not include any information on how to load the image
    //Will deal with the image being flipped/rotated/cropped/filtered
    //Will not hold image title, path
    //Will use other classes to hold related information (eg. width and height as dimension OR brightness/contrast/isFiltered as Filter)
    MultiSizeImage originalImage = null;
    MultiSizeImage filteredImage = null;
    MultiSizeImage iconImage = null;
    FilterState filter = new FilterState();
    //TransFormstate transform;
    boolean isIcon = false;


    MultiSizeImage getCurrentMultiSizeImage(){
        if(isIcon) return iconImage;
        if(filter.isFiltered) return filteredImage;
        return originalImage;
    }

}
class MultiSizeImage{
    //make these private to some extent. Or make each multiSize image private.
    BufferedImage fullImage;
    //BufferedImage mediumImage;
    BufferedImage thumbImage;
    Dimension dimensions;

}
class FilterState{
    boolean isFiltered = false;
    int contrast = 50;
    int brightness = 50;
    boolean isInverted = false;
}

class ImageReference {
    //New
    Log log = new Log(false);
    ImageItem image = new ImageItem();



    //Currently converting

    //Get image using swing worker.
    //Must set a loading icon if not ready, and then update when done
    private void loadViaSwingWorker(ImgSize size) {
        isLoading = true;
        imageLoader = new ImageLoader(this, pathFile, size, imgType, screenWidth, screenHeight, thumbMaxW, thumbMaxH, bThumb,imageFileLength,modifiedDateTime);
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

    void setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgSize size,boolean wasCancelled,boolean ranOutOfMemory){
        bThumb = thmb;
        if(!wasCancelled){
            if(b!=null) setVars(b);
            bImage = b;
            currentLarge = size;
        } else {
            bImage = null;
        }
        isBThumbIcon=false;
        isBImageIcon=false;
        isLoading = false;
        if(bImage!=null){
            if(isFiltered()) filterImage();
        }
        mainGUI.mainPanel.onResize();//resize as image have changed dimensions
        mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
    }    //gets thumbnail or full image

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



    void setVars(BufferedImage img){
	if(bImage==null){ log.print(LogType.DebugError,"ERROR setting image size as image not initilized");return;}
	Bwidth = img.getWidth();
	Bheight = img.getHeight();
	if(Bheight<Bwidth) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }





    //could be updated to use a javase7 path when java 7 released... but 7 has been delayed by a year so not possible
    //Old
    private BufferedImage bImage = null;//Full size image, may be maxed at size of screen. set to null when not needed.
    private BufferedImage bThumb = null;//Created when large created, not removed.//Will be created from exif thumb
    private BufferedImage bImageFilt = null;
    private BufferedImage bThumbFilt = null;
    private Integer Bwidth = null;
    private Integer Bheight = null;
    int brightness = 50;
    int contrast = 50;
    private boolean isFiltered = false;
    boolean isInverted = false;

    private BufferedImage localGetBufImage(){
        if(isFiltered) return bImageFilt;
        return bImage;
    }
    private BufferedImage localGetBufThumb(){
        if(isFiltered) return bThumbFilt;
        return bThumb;
    }

    //Unsorted
    private final int imgType = BufferedImage.TYPE_INT_RGB;
    File pathFile = null;
    long imageFileLength,modifiedDateTime;
    Orientation iOri;
    int screenWidth, screenHeight;
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    boolean isQuickThumb = false;
    boolean hasTriedQuickThumb = false;
    boolean hasTriedExtractDimensions = false;
    ImgSize currentLarge=ImgSize.Screen;//The size of the large bImage (Max or Screen)
    boolean isLoading = false;
    boolean isBImageIcon = false;//Is the image a loading icon
    boolean isBThumbIcon = false;//Is the thumb a loading icon
    GUI mainGUI;
    ImageLoader imageLoader;
    //String title,filename,comments?

    ImageReference(String inputPath,String currentID,GUI gui){
        log.print(LogType.Debug,"Image:"+currentID+" has inPath: "+inputPath);
	mainGUI = gui;
        String tempPath = "";
	try{
	    if(inputPath.startsWith("///\\\\\\")){
		tempPath = inputPath.substring(6);
		tempPath = (GUI.class.getResource(tempPath)).toURI().getPath();//could be null
		//absolutePath = "etc/img/"+tempPath;//for use with jar files.
		if(tempPath==null){
		    tempPath=inputPath.substring(6);
		}
		else pathFile = new File(tempPath);
	    }
	    else {
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
	
	
	if(pathFile==null||(!pathFile.exists())||(!pathFile.isFile())){
	    log.print(LogType.Error,"File could not be found at " + inputPath);
	}
	//ImageObjectConstructor);
	//getImage(ImgSize.Screen);

	//initVars()
	Dimension scrD = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = scrD.width;
	screenHeight = scrD.height;
        modifiedDateTime = pathFile.lastModified();
        imageFileLength = pathFile.length();
    }

//    ImageReference(File inFile){
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

    int getWidthAndMake(){
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile);
	if(Bwidth!=null) return Bwidth;
	getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory???????????????????
	return Bwidth;
    }
    int getHeightAndMake(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile);
	if(Bheight!=null) return Bheight;
	getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory????????????????????
	return Bheight;
    }
    int getWidthAndMakeBig(){
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile);
	if(Bwidth!=null) return Bwidth;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory???????????????
	return Bwidth;
    }
    int getHeightAndMakeBig(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile);
	if(Bheight!=null) return Bheight;
	getImage(ImgSize.Max);
	//note that the bImage should now be set to null to clear memory??????????????
	return Bheight;
    }

    String getImageUID(){
        return ImageUtils.getUID(pathFile, imageFileLength, modifiedDateTime);
    }
    
    void getImageBySampling(){
        try {
            long start =Calendar.getInstance().getTimeInMillis();
        String ext = ImageUtils.getFileExtLowercase(pathFile.getName());
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
            log.print(LogType.Debug,"Read thumbnail from image "+pathFile.toString()+"\n        -by reading every "+sampleFactor+" pixels");
            log.print(LogType.Debug, "   -sampled every " + sampleFactor + " pixels from " + Bwidth + "x" + Bheight + " in " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds");
            isQuickThumb = true;
        }
    } catch (IOException e) {
        log.print(LogType.DebugError, "Error reading dimensions of image " + pathFile.toString() + "\nError was: " + e.toString());
    }
        }

void getThumbQuick() {
    if (hasTriedQuickThumb) {
        return;
    }
    if (pathFile!= null) {
    BufferedImage tempImage = ImageUtils.getThumbFromExif(pathFile);
    if (tempImage != null) {
        Log.Print(LogType.Debug, "Read exif of image " + pathFile.toString());
        bThumb = tempImage;
        mainGUI.thumbPanel.repaint();//not onResize
        return;
    }
        if(Bwidth==null)
        extractDimensionsFromFile(pathFile);
        if((Bwidth!=null)&&(Bheight!=null)&&((Bwidth*Bheight)<(6*1024*1024))) getImageBySampling();
    }
        hasTriedQuickThumb = true;
   

    //int thumbnum = reader.getNumThumbnails(0);//imageIndex = 0 as we look at the first image in file
    //log.print(LogType.Debug,"Has "+thumbnum+" thumbnails. Using reader " +reader.getClass().getName());
    //If a thumbnail image is present, it can be retrieved by calling:
    //int thumbailIndex = 0;
    //BufferedImage bi;
    //bi = reader.readThumbnail(imageIndex, thumbnailIndex);

    //if (bThumb != null) {//Don't want to save sampled thumb as it will prevent better copy being saved
    //    ImageUtils.saveThumbToFile(thumbPath, bThumb, pathFile, imageFileLength,  modifiedDateTime);
    //}
}

  void extractDimensionsFromFile(File pathFile) {
      if(isBImageIcon) return;
    if(hasTriedExtractDimensions) return;
    Dimension temp = ImageUtils.getImageDimensionsSanslan(pathFile);
    if (temp != null) {
        Bwidth = temp.width;
        Bheight = temp.height;
    } else {
        log.print(LogType.DebugError, "Error reading exif dimensions of image " + pathFile.toString());
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
    int getNoPixels(){
        if((Bwidth==null)||(Bheight==null)){
            extractDimensionsFromFile(pathFile);
        }

        if((Bwidth==null)||(Bheight==null)) return Integer.MAX_VALUE;
        return Bwidth*Bheight;
    }

    void preload(ImgSize size){
        if(getNoPixels() < 15 * 1024 * 1024) {//if can verify is less than 15 megapixels
            if (size == ImgSize.Max) {
                if (getNoPixels() > 8 * 1024 * 1024)//If has more than 8 megapixels
                {
                    size = ImgSize.Screen;
                }
            }
            getImage(size);
        }
        else flush();//if image is really huge we need to flush it.
    }
    


    void saveFullToPath(String path){
        try{
            File f = new File(path);
            ImageIO.write(localGetBufImage(),"jpg",f);//should use same format as file
        } catch (IOException e){
            log.print(LogType.Error,"Error creating saving image: "+pathFile.toString()+"\nTo path: "+path);
        }
    }

    private BufferedImage createLoadingThumb(){
        return SysIcon.Loading.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    }

    void getThumbIfCached() {
        File thumbPath = new File(mainGUI.settings.getSetting("homeDir") + mainGUI.settings.getSetting("thumbnailPathExt"));
        File checkFile = new File(thumbPath, (ImageUtils.getSaveEncoding(pathFile, imageFileLength,  modifiedDateTime)));
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
    }
}