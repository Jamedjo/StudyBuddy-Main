//<editor-fold desc="topstuff">
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


//Before doing Javadoc should make some variables private and use getters and setters.

//Overhead caused by ImgSize.Screen is not worth it considering it is v.likely
//to be replaced by full quickly- meaning image has to be loaded twice.
//Instead huge images can be set to use a smaller image from disk cache or mosaic system. See issue 31 .

enum N_ImgSize{Full,Thumb}
enum ImageType{Original,Filtered,Icon,None;
    boolean isNone(){
        if(this==None) return true;
        return false;
    }
    boolean isBad(){
        if((this==None)||(this==Icon)) return true;
        return false;
    }
    boolean isFiltered(){
        if(this==Filtered) return true;
        return false;
    }
}
 //</editor-fold>
//<editor-fold desc="ImageItem">
class ImageItem{
    //Will hold image, thumb, filtered version, current size, alternate place for icons and their sizes
    //Will include getters which return relevent image
    //Will not include any information on how to load the image
    //Will deal with the image being flipped/rotated/cropped/filtered
    //Will not hold image title, path
    //Will use other classes to hold related information (eg. width and height as dimension OR brightness/contrast/isFiltered as Filter)

    private MultiSizeImage originalImage = new MultiSizeImage();
    private MultiSizeImage filteredImage = new MultiSizeImage();
    private MultiSizeImage iconImage = new MultiSizeImage();
    FilterState filter = new FilterState();
    ImageType fullType = ImageType.None;
    ImageType thumbType = ImageType.None;
    //TransFormstate transform;
    //Orientation iOri

    
//	if(Bheight<Bwidth) iOri = Orientation.Landscape;
//	else iOri = Orientation.Portrait;


    boolean hasNoCurrentFullImage(){
        if(fullType.isNone()) return true;
        return false;
    }
    boolean hasNoCurrentThumbImage(){
        if(thumbType.isNone()) return true;
        return false;
    }
    boolean hasFullImage(){
        return !hasNoCurrentFullImage();
    }
    boolean hasThumbImage(){
        return !hasNoCurrentThumbImage();
    }

    void setFullImage(BufferedImage img,ImageType type){
        originalImage.fullImage = img;
        if(type.isBad()) fullType = type;
        else if(type.isFiltered()) {
            filterImage(N_ImgSize.Full);
            fullType = ImageType.Filtered;
        }
        fullType = type;
        originalImage.dimensions=new Dimension(img.getWidth(),img.getHeight());
    }
    void setThumbImage(BufferedImage img,ImageType type){
        originalImage.thumbImage = img;
        if(type.isBad()) thumbType = type;
        else if(type.isFiltered()) {
            filterImage(N_ImgSize.Thumb);
            thumbType = ImageType.Filtered;
        }
        thumbType = type;
    }

//    void setAllToIconImage(BufferedImage icon){
//        setToIconImage(icon,N_ImgSize.Full);
//        setToIconImage(icon,N_ImgSize.Thumb);
//    }
    void setToIconImage(BufferedImage icon,N_ImgSize size){
        if(size==N_ImgSize.Full){
        fullType = ImageType.Icon;
        iconImage.fullImage = icon;
        }
        if(size==N_ImgSize.Thumb){
        thumbType = ImageType.Icon;
        iconImage.thumbImage = icon;
        }
        iconImage.dimensions=new Dimension(icon.getWidth(),icon.getHeight());
    }

    BufferedImage getCurrentFullImage(){
        switch(fullType){
            case Icon:
                return iconImage.fullImage;
            case Filtered:
                return filteredImage.fullImage;
            default:
                return originalImage.fullImage;
        }
    }
    private void filterImage(N_ImgSize size){
        int i;
        BufferedImage[] srcs = {originalImage.fullImage,originalImage.thumbImage};
        BufferedImage[] dests = {filteredImage.fullImage,filteredImage.thumbImage};
        if(size==N_ImgSize.Full) i=0;
        else i=1;

        RenderingHints hints = null;
        float offset = (filter.brightness-50f)*5.10f;
        float scale = 1.0f+(filter.contrast-50f)/50f;
        if(filter.isInverted){
            offset = 255f-offset;
            scale = (-scale);
        }
        RescaleOp op = new RescaleOp(scale,offset,hints);

        if (srcs[i] == null) return;
        if (dests[i] == null) dests[i] = new BufferedImage(srcs[i].getWidth(), srcs[i].getHeight(), BufferedImage.TYPE_INT_ARGB);//*/op.createCompatibleDestImage(srcImg, null);
        op.filter(srcs[i], dests[i]);
        if(size==N_ImgSize.Full) filteredImage.dimensions = new Dimension(filteredImage.fullImage.getWidth(),filteredImage.fullImage.getHeight());
    }

    private void resetFilters(){//set to original values
        //filter = new FilterState();
        if(fullType==ImageType.Filtered) fullType=ImageType.Original;
        if(thumbType==ImageType.Filtered) thumbType=ImageType.Original;
    }
    private void updateFilters(){
        filterImage(N_ImgSize.Full);
        filterImage(N_ImgSize.Thumb);
        fullType = ImageType.Filtered;
        thumbType = ImageType.Filtered;
    }
    void refreshFilters(){
        if((filter.contrast==50)&&(filter.brightness==50)&&(!filter.isInverted)) {
            resetFilters();
            return;
        }
        updateFilters();
    }
}
//</editor-fold>

class MultiSizeImage{
    //make these private to some extent. Or make each multiSize image private.
    BufferedImage fullImage = null;
    //BufferedImage mediumImage = null;
    BufferedImage thumbImage = null;
    Dimension dimensions = null;

}
class FilterState{
    int contrast = 50;
    int brightness = 50;
    boolean isInverted = false;
}

class ImageReference {
    //New
    Log log = new Log(false);
    GUI mainGUI;
    File pathFile = null;
    ImageItem img = new ImageItem();
    ImageLoader imageLoader;

private void loadViaSwingWorker(ImgRequestSize size) {
        isLoading = true;
        imageLoader = new ImageLoader(this, pathFile, size,imageFileLength,modifiedDateTime);
        if (img.hasNoCurrentFullImage()) {
            img.setToIconImage(createLoadingThumb(),N_ImgSize.Full);
        }
        if (img.hasNoCurrentThumbImage()) {
            getThumbIfCached();
        }
        if(img.hasNoCurrentThumbImage()){
            img.setToIconImage(createLoadingThumb(),N_ImgSize.Thumb);
        }
        imageLoader.execute();
    }


//if thumb image an icon just set both to icon and ignore full.
    void setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgRequestSize size,ImageType returnImageType,ImageType returnThumbType){
        img.setThumbImage(b, returnThumbType);
        img.setFullImage(b, returnImageType);
        
        isLoading = false;

        mainGUI.mainPanel.onResize();//resize as image have changed dimensions
        mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
    }

    BufferedImage getImage(ImgRequestSize size) {
        //If requested image already exists, return it.
        if (size.isThumb() && img.hasThumbImage()) {
            return localGetBufThumb();
        }
        if (size.isLarge() && img.hasFullImage()) {
            return localGetBufImage();
        }

        if ((!isLoading)&&img.hasNoCurrentThumbImage()) {//If swing worker is loading an image, it will then write a thumb. Must avoid reading thumb at the same time as it is being written.
            getThumbIfCached();
            if(img.hasNoCurrentThumbImage()) {
                getThumbQuick();//if no longer null thumbIsQuick();//add thumb to list of thumbs which are ThumbQuicks, so they can be loaded while idle.
            }
            if (size == ImgRequestSize.Thumb) {//If ImgRequestSize.ThumbFull still want to create swingworker
                if (img.hasThumbImage()) {
                    return localGetBufThumb();
                }
            }
        }

        //Build large icon and small icon, return relevent.
        if (!isLoading) {
            loadViaSwingWorker(size);
        }

        if (img.hasFullImage()){
            if(size.isLarge()){//if bImage exists but is not currentlarge, use for now but swingworker will replace
            return localGetBufImage();
            }
            //If ImgRequestSize.ThumbOnly then clear bImage
            return localGetBufThumb();
        }
        else {
            //bImage is null, bThumb may be null, returns either bThumb or null
            return localGetBufThumb();
        }
    }

    String getImageUID(){
        return ImageUtils.getUID(pathFile, imageFileLength, modifiedDateTime);
    }











//<editor-fold defaultstate="collapsed" desc="Currently converting">


 private BufferedImage localGetBufImage(){
        if(isFiltered) return bImageFilt;
        return bImage;
    }
    private BufferedImage localGetBufThumb(){
        if(isFiltered) return bThumbFilt;
        return bThumb;
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



//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Unsorted">
    long imageFileLength,modifiedDateTime;
    Orientation iOri;
    boolean isQuickThumb = false;
    boolean hasTriedQuickThumb = false;
    boolean hasTriedExtractDimensions = false;
    ImgRequestSize currentLarge=ImgRequestSize.Max;//The size of the large bImage (Max or Screen)
    boolean isLoading = false;
    //String title,filename,comments?
//</editor-fold>
    ImageReference(String inputPath ,GUI gui){
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
	//getImage(ImgRequestSize.Screen);

	//initVars()
	
        modifiedDateTime = pathFile.lastModified();
        imageFileLength = pathFile.length();
    }
//<editor-fold defaultstate="collapsed" desc="old Stuffs">
//    ImageReference(File inFile){
//	pathFile = inFile;
//	pathFile.getAbsolutePath();
//	initVars();
//    }


    int getWidthAndMake(){
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile);
	if(Bwidth!=null) return Bwidth;
	OLD_getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory???????????????????
	return Bwidth;
    }
    int getHeightAndMake(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile);
	if(Bheight!=null) return Bheight;
	OLD_getImage(currentLarge);
	//note that the bImage should now be set to null to clear memory????????????????????
	return Bheight;
    }
    int getWidthAndMakeBig(){
	if(Bwidth!=null) return Bwidth;
        extractDimensionsFromFile(pathFile);
	if(Bwidth!=null) return Bwidth;
	OLD_getImage(ImgRequestSize.Max);
	//note that the bImage should now be set to null to clear memory???????????????
	return Bwidth;
    }
    int getHeightAndMakeBig(){
	if(Bheight!=null) return Bheight;
        extractDimensionsFromFile(pathFile);
	if(Bheight!=null) return Bheight;
	OLD_getImage(ImgRequestSize.Max);
	//note that the bImage should now be set to null to clear memory??????????????
	return Bheight;
    }

    int getWidthForThumb(){
	//if(bThumb!=null) return bThumb.getWidth();
	return OLD_getImage(ImgRequestSize.Thumb).getWidth();
    }
    int getHeightForThumb(){
	//if(bThumb!=null) return bThumb.getHeight();
	return OLD_getImage(ImgRequestSize.Thumb).getHeight();//Thumbnail height not image height or Bheight
    }
    int getNoPixels(){
        if((Bwidth==null)||(Bheight==null)){
            extractDimensionsFromFile(pathFile);
        }

        if((Bwidth==null)||(Bheight==null)) return Integer.MAX_VALUE;
        return Bwidth*Bheight;
    }

    void preload(ImgRequestSize size){
        if(getNoPixels() < 15 * 1024 * 1024) {//if can verify is less than 15 megapixels
            OLD_getImage(size);
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

    //<editor-fold desc="Very Old and replaced">
        //could be updated to use a javase7 path when java 7 released... but 7 has been delayed by a year so not possible
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
    private final int imgType = BufferedImage.TYPE_INT_RGB;
    OLD__ImageLoader OLD_imageLoader;
    boolean isBImageIcon = false;//Is the image a loading icon
    boolean isBThumbIcon = false;//Is the thumb a loading icon
    BufferedImage OLD_getImage(ImgRequestSize size) {
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
            if (size == ImgRequestSize.Thumb) {//If ImgRequestSize.ThumbFull still want to create swingworker
                if (bThumb != null) {
                    return localGetBufThumb();
                }
            }
        }
        //Build large icon and small icon, return relevent.
        if (!isLoading) {
        OLD__loadViaSwingWorker(size);
        }
        if (bImage!=null){
            if(size.isLarge()){//if bImage exists but is not currentlarge, use for now but swingworker will replace
            return localGetBufImage();
            }
            //If ImgRequestSize.ThumbOnly then clear bImage
            return localGetBufThumb();
        }
        else {
            //bImage is null, bThumb may be null, returns either bThumb or null
            return localGetBufThumb();
        }
    }

           private void OLD_filterBufImage(boolean isThumb){
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
    boolean isFiltered(){
        return isFiltered;
    }
    void setFiltered(boolean newBool){
        isFiltered = newBool;
    }
        private void OLD__loadViaSwingWorker(ImgRequestSize size) {
        isLoading = true;
        OLD_imageLoader = new OLD__ImageLoader(this,pathFile,size,imgType, bThumb,imageFileLength,modifiedDateTime);
        if (bImage == null) {
            bImage = createLoadingThumb();
            currentLarge = size;
            isBImageIcon = true;
            OLD__setVars(bImage);
        }
        if (bThumb == null) {
            getThumbIfCached();
        }
        if(bThumb==null){
            bThumb = createLoadingThumb();
            isBThumbIcon = true;
        }
        OLD_imageLoader.execute();
    }
            void OLD_setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgRequestSize size,boolean wasCancelled,boolean ranOutOfMemory){
        if(bThumb==null) bThumb = thmb;
        //if bThumb not an icon, set it.
        if(!wasCancelled){
            if(b!=null) OLD__setVars(b);
            bImage = b;
            currentLarge = size;
        } else {
            bImage = null;
        }
        isBThumbIcon=false;
        isBImageIcon=false;
        isLoading = false;
        if(bImage!=null){
            if(isFiltered()) OLD__filterImage();
        }
        mainGUI.mainPanel.onResize();//resize as image have changed dimensions
        mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
    }    //gets thumbnail or full image
    void OLD__filterImage(){
        if((contrast==50)&&(brightness==50)&&(!isInverted)) {
            isFiltered = false;
            return;
        }
        isFiltered = true;
        OLD_filterBufImage(true);
        OLD_filterBufImage(false);
    }
    void OLD__setVars(BufferedImage img){
	if(bImage==null){ log.print(LogType.DebugError,"ERROR setting image size as image not initilized");return;}
	Bwidth = img.getWidth();
	Bheight = img.getHeight();
	if(Bheight<Bwidth) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }
}
//</editor-fold>
enum Orientation {Landscape,Portrait} //For drawing/painting not for rotation

enum ImgRequestSize {Max,Thumb;//,ThumbOnly,ThumbPreload;
    boolean isLarge(){
	if(this==Thumb) return false;/*||this==ThumbPreload*/
	return true;
    }
    boolean isThumb(){
	if(this==Max) return false;
	else return true;
    }
    public String toString(){
	switch (this){
	case Thumb: return "Thumb";
	//case ThumbPreload: return "ThumbFull";//ThumbFull requests size Thumb, but not clearing the full image as it may be used later
	default: return "Max";//Max is not yet implemented.
	}
    }
}

// </editor-fold>