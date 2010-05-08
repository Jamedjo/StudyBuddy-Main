
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


//BufferedImage takes 4MB per megapixel, so a 5megaPixel file is 20MB of java heap memory. On some VMs 64MB is max heap.

//Objectives from memory analysis
//-Clearing the buffered image from memory when it is not being displayed,keeping only a scaled down thumbnail in memory, and moving that to a fixed disk cache when not needed. 
//-Not storing more pixels than the screen can hold. If the image is zoomed in to a large extent we can reload the image at full size- only slowing the computer down when this is needed, not when scrolling through many images. 


//Before doing Javadoc should make some variables private and use getters and setters.

//Overhead caused by ImgSize.Screen is not worth it considering it is v.likely
//to be replaced by full quickly- meaning image has to be loaded twice.
//Instead huge images can be set to use a smaller image from disk cache or mosaic system. See issue 31 .

class ImageReference {
    Log log = new Log(false);
    //GUI mainGUI;
    File pathFile = null;
    ImageItem img = new ImageItem();
    ImageLoader imageLoader;
    double maxFilesizeToLoadThumb = 3;//Megabytes
    long imageFileLength, modifiedDateTime;
    //Orientation iOri;
    boolean isQuickThumb = false;
    boolean hasTriedQuickThumb = false;
    boolean hasTriedExtractDimensions = false;
    boolean hasCalledLoad = false;
    ImgRequestSize lastRequestSize=ImgRequestSize.Thumb;//could be replaced with boolean- hasRequestedFull
    //String title,filename,comments?
    boolean rotationExifChecked=false;
    
    ImageReference(String inputPath){//, GUI gui) {
        //mainGUI = gui;
        String tempPath = "";
        try {
            if (inputPath.startsWith("///\\\\\\")) {
                tempPath = inputPath.substring(6);
                tempPath = (GUI.class.getResource(tempPath)).toURI().getPath();//could be null
                //absolutePath = "etc/img/"+tempPath;//for use with jar files.
                if (tempPath == null) {
                    tempPath = inputPath.substring(6);
                } else pathFile = new File(tempPath);
            } else {
                tempPath = inputPath;
                pathFile = new File(inputPath);
            }
        } catch (URISyntaxException e) {
            pathFile = null;
            log.print(LogType.Error, "Couldn't load image from " + tempPath + "\nError was- " + e.toString());
        } catch (NullPointerException e) {
            pathFile = null;
            log.print(LogType.Error, "Couldn't load image from file " + tempPath + "\nError was:- " + e.toString());
        }


        if (pathFile == null || (!pathFile.exists()) || (!pathFile.isFile())) {
            if(!inputPath.equals("NoExistingFiles:a:b:c:d:e:f:g:h.i.j.k.l.m.n:o:p:non.ex")) log.print(LogType.Error, "File could not be found at " + inputPath);
        }
        //ImageObjectConstructor);
        //getImage(ImgRequestSize.Screen);

        //initVars()

        modifiedDateTime = pathFile.lastModified();
        imageFileLength = pathFile.length();
    }
    synchronized private void loadViaSwingWorker(ImgRequestSize size) {
        // if ( making a load request for the first time ) OR ( requesting full size for first time ) THEN new ImageLoader
        if (lastRequestSize==ImgRequestSize.Max) return;
        if ((hasCalledLoad)&&(size==ImgRequestSize.Thumb)) return;
        if((hasCalledLoad)&&img.hasNoGoodThumbImage()) return;
            hasCalledLoad = true;
            if (size.isThumb() && (pathFile.length() > (maxFilesizeToLoadThumb * 1024 * 1024))) {
                    //hasCalledLoad=false;
                    log.print(LogType.Error, "Error: requested thumbnail for " + (pathFile.length() / (1024 * 1024)) + "MB image when the max is " + maxFilesizeToLoadThumb + "MB.");
                    setImageFromLoader(ErrorImages.outOfMemory,ImageType.Icon,false);//false as we don't want it to try again
                    return;
            }
                imageLoader = new ImageLoader(this, pathFile);
            lastRequestSize=size;
            if (img.hasNoGoodFullImage()) {
                img.setToIconImage(ErrorImages.loading, ImgSize.Full);
            }
            if (img.hasNoGoodThumbImage()) {
                getThumbIfCached();
            }
            if (img.hasNoGoodThumbImage()) {
                img.setToIconImage(ErrorImages.loading, ImgSize.Thumb);
            }
//            mainGUI.mainPanel.onResize();//resize as image may have changed dimensions //Not needed al likely already repainting
//            mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
            imageLoader.execute();
    }
//if thumb image an icon just set both to icon and ignore full.
    void setImageFromLoader(BufferedImage returnImage, ImageType returnImageType,boolean outOfMemory) {//outOfMemory is equivelent to it being cancelled due to memory limitations. boolean outOfMemory will be true if cancelled for any reason from now.
        if(returnImage!=null){
            if(img==null)return;
            img.setFullImage(returnImage, returnImageType);
            //lastRequstSize

            BufferedImage thmb=null;
            if (returnImageType == ImageType.Original) thmb = makeThumb(returnImage);
            if (returnImageType == ImageType.Icon) thmb = returnImage;
            if(thmb!=null) img.setThumbImage(thmb, returnImageType);

            if(lastRequestSize!=ImgRequestSize.Max) img.clearMem();

            RepaintManager.repaint(RepaintType.ImageUpdated);
        }
        //else has been cancelled or has failed

        
        //After onResize and repaint calls
        if(outOfMemory) hasCalledLoad = false;//If it ran out of memory during the call it is asif it was never called
        //??lastRequestSize ?? if out of memory ??
    }
    BufferedImage getImage(ImgRequestSize size){//This method should be replaced where possible
        return getImage(size,true);
    }
    //loadViaWorker exists to allow thumbnails to signal they only want lowQ versions if the calling method is aware of the flush/preload system in use.
    BufferedImage getImage(ImgRequestSize size,boolean loadViaWorker) {
        if(!rotationExifChecked) {
            int rotval = ImageUtils.getRotationFromExif(pathFile);
            for(;rotval>=90;rotval-=90){
                img.transform.rotate90();
            }
            rotationExifChecked=true;
        }

        //If requested image already exists, return it.
        if (size.isThumb() && img.hasGoodThumbImage()) {
            return img.getCurrentThumbImage();
        }
        if (size.isLarge() && img.hasGoodFullImage()) {
            return img.getCurrentFullImage();
        }

        if ((!hasCalledLoad) && img.hasNoGoodThumbImage()) {//If has called before this will have been done before
            //If swing worker is loading an image, it will then write a thumb. Must avoid reading thumb at the same time as it is being written.
            getThumbIfCached();
            if (img.hasNoGoodThumbImage()) {
                getThumbQuick();//if no longer null thumbIsQuick();//add thumb to list of thumbs which are ThumbQuicks, so they can be loaded while idle.
            }
            if (size == ImgRequestSize.Thumb) {//If ImgRequestSize.ThumbFull still want to create swingworker
                if (img.hasGoodThumbImage()) {
                    return img.getCurrentThumbImage();
                }
            }
        }

        if(loadViaWorker)
            loadViaSwingWorker(size);
        
//        if(img.hasNoCurrentThumbImage()) img.setToIconImage(ErrorImages.fileNotFound, ImgSize.Thumb);
//        if(img.hasNoCurrentFullImage()) img.setToIconImage(ErrorImages.fileNotFound, ImgSize.Full);
        //if (img.hasAnyFullImage())
            if (size.isLarge()) {//if bImage exists but is not currentlarge, use for now but swingworker will replace
                return img.getCurrentFullImage();
            }
        return img.getCurrentThumbImage();
    }
    String getImageUID() {
        return ImageUtils.getUID(pathFile, imageFileLength, modifiedDateTime);
    }
    void getImageBySampling() { //TODO: move to sampleImage SwingWorker??
        try {
            LogType logthis = LogType.Plain;
            long start = Calendar.getInstance().getTimeInMillis();
            String ext = ImageUtils.getFileExtLowercase(pathFile.getName());
            if (ext == null) return;
            //ImageIO.scanForPlugins();
            Iterator readers = ImageIO.getImageReadersBySuffix(ext);
            ImageReader reader = (ImageReader) readers.next();
            if (readers.hasNext()) {
                reader = (ImageReader) readers.next();
                log.print(logthis, "next reader");
            }
            //ImageInputStream inputStream = ImageIO.createImageInputStream(pathFile);
            ImageInputStream inputStream = ImageIO.createImageInputStream(new RandomAccessFile(pathFile, "r"));
            reader.setInput(inputStream, false);
            ImageReadParam readParam = reader.getDefaultReadParam();
            //readParam.setSourceProgressivePasses(0,1);

            //To make thumnail at least 200 pixels, finds how many times bigger input is.
            //Looks at largest dimension as a square thumnail is limited by largest dimension.

            int sampleFactor = (int) Math.floor((double) Math.max((double) img.getFullDimensions().width, (double) img.getFullDimensions().height) / ((double) 200));//9));
            if (sampleFactor <= 1) {
                return;//Full size image is less than thumbImage, return full size image.
            }
            readParam.setSourceSubsampling(sampleFactor, sampleFactor, 0, 0);//reads the image at 1/4 size
            BufferedImage sampledImage = reader.read(0, readParam);
            if(sampledImage!=null) img.setThumbImage(sampledImage, ImageType.Original);
            reader.dispose();
            inputStream.close();

            if (img.hasAnyThumbImage()) {
                log.print(logthis, "Read thumbnail from image " + pathFile.toString() + "\n        -by reading every " + sampleFactor + " pixels");
                log.print(logthis, "   -sampled every " + sampleFactor + " pixels from " + img.getFullDimensions() + " in " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds");
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
        if (pathFile != null) {
            BufferedImage tempImage = ImageUtils.getThumbFromExif(pathFile);
            if (tempImage != null) {
                Log.Print(LogType.Plain, "Read exif of image " + pathFile.toString());
                img.setThumbImage(tempImage, ImageType.Original);
                RepaintManager.repaint(RepaintType.NewThumb);
                return;
            }
            if (img.getFullDimensions() == null)
                extractDimensionsFromFile(pathFile);
            if ((img.getFullDimensions() != null) && ((img.getFullDimensions().width * img.getFullDimensions().height) < (6 * 1024 * 1024))) getImageBySampling();
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
        if (hasTriedExtractDimensions) return;
        Dimension temp = ImageUtils.getImageDimensionsSanslan(pathFile);
        if (temp != null) {
            img.setFullDimensions(temp);
        } else {
            log.print(LogType.DebugError, "Error reading exif dimensions of image " + pathFile.toString());
            //img.setFullDimensions(new Dimensinon(reader.getWidth(0),reader.getHeight(0)));//uses the width of the first image in the file
        }
        hasTriedExtractDimensions = true;
    }
    int getNoPixels() {
        if (img.getFullDimensions() == null) {
            extractDimensionsFromFile(pathFile);
        }
        if (img.getFullDimensions() == null) return Integer.MAX_VALUE;
        return img.getNoPixels();
    }
    void saveFullToPath(String path) {
        try {
            File f = new File(path);
            ImageIO.write(img.getCurrentFullImage(), "jpg", f);//should use same format as file
        } catch (IOException e) {
            log.print(LogType.Error, "Error creating saving image: " + pathFile.toString() + "\nTo path: " + path);
        }
    }
    void preload(ImgRequestSize size) {
        if ((getNoPixels() < (12 * 1024 * 1024)) ){// && (System.)){//if can verify is less than 12 megapixels
            getImage(size);
            //log.print(LogType.Plain,"mem"+Runtime.getRuntime().freeMemory());
        }
        else {
            flush();//if image is really huge we need to flush it.
            //System.gc();
        }
    }
    void getThumbIfCached() {
        File thumbPath = new File(Settings.getSetting("homeDir") + Settings.getSetting("thumbnailPathExt"));
        File checkFile = new File(thumbPath, (ImageUtils.getSaveEncoding(pathFile, imageFileLength, modifiedDateTime)));
        if (checkFile.isFile()) {
            try {
                BufferedImage tempThumb = ImageIO.read(checkFile);
                if(tempThumb!=null)
                    img.setThumbImage(tempThumb, ImageType.Original);
                //?????Stops incomplete thumbnails from being returned if error while loading.
            } catch (IOException e) {
                log.print(LogType.Error, "Error opening thumbnail " + checkFile + "\nError was: " + e.toString());
            } catch (ArrayIndexOutOfBoundsException e) {
                log.print(LogType.Error, "Error opening thumbnail " + checkFile + "\nError was: " + e.toString());
                e.printStackTrace();
            } catch (Exception e) {
                log.print(LogType.Error, "Error opening thumbnail " + checkFile + "\nError was: " + e.toString());
            }
        }
    }
    void replaceFilter(boolean isInvert, int contrast, int brightness) {
        img.filter = new FilterState(isInvert, contrast, brightness);
        img.refreshFilters();
    }
    void flush() {//called externally
        if (imageLoader != null) {
            imageLoader.cancel(false);
            imageLoader=null;
        }
        img.clearMem();
        lastRequestSize=ImgRequestSize.Thumb;
    }
    void destroy() {
        flush();
        img = null;
        pathFile = null;
    }
    
    
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    BufferedImage makeThumb(BufferedImage bigImg) {    //TODO: move to thumbSizer SwingWorker??
        BufferedImage returnThumb;
        long start = Calendar.getInstance().getTimeInMillis();
        Dimension iconWH = ImageUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), thumbMaxW, thumbMaxH);
        if (!(iconWH.width < bigImg.getWidth())) {
            returnThumb = bigImg;
        } else {

            //Image tempimage = bigIcon.getImage();
            returnThumb = new BufferedImage(iconWH.width, iconWH.height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2 = returnThumb.createGraphics();//TYPE_INT_RGB
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2.drawImage(bigImg, 0, 0, iconWH.width, iconWH.height, null);
            g2.dispose();
            log.print(LogType.Debug, "  -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to scale thumbnail");
        }
        ImageUtils.saveThumbToFile(returnThumb, pathFile, imageFileLength, modifiedDateTime);
        return returnThumb;
    }
//<editor-fold desc="unchecked methods">
//    ImageReference(File inFile){
//	pathFile = inFile;
//	pathFile.getAbsolutePath();
//	initVars();
//    }
    Dimension getDimensionsWithMake() {
        if (img.getCurrentFullDimensions() != null) return img.getCurrentFullDimensions();
        extractDimensionsFromFile(pathFile);
        if (img.getCurrentFullDimensions() != null) return img.getCurrentFullDimensions();
        getImage(ImgRequestSize.Max);
        //note that the bImage should now be set to null to clear memory??????????????
        return img.getCurrentFullDimensions();
    }
        Dimension getThumbDimensionsWithMake() {//Make component exists as dimensions often requested immediately before image requested.
        getImage(ImgRequestSize.Thumb);
        if (img.getThumbDimensions() != null) return img.getThumbDimensions();
        //getThumbIfCached();
//        if (img.getThumbDimensions() != null) return img.getThumbDimensions();
        if(img.getThumbDimensions()==null) {log.print(LogType.Error, "Null image height: "+img.thumbType); Thread.dumpStack(); return new Dimension(256,256);}

        return img.getThumbDimensions();
    }
}
//could be updated to use a javase7 path when java 7 released... but 7 has been delayed by a year so not possible
//Image may be maxed at size of screen.

enum ImgRequestSize {
    Max, Thumb;//,ThumbOnly,ThumbPreload;
    boolean isLarge() {
        if (this == Thumb) return false;/*||this==ThumbPreload*/
        return true;
    }
    boolean isThumb() {
        if (this == Max) return false;
        else return true;
    }
    public String toString() {
        switch (this) {
            case Thumb:
                return "Thumb";
            //case ThumbPreload: return "ThumbFull";//ThumbFull requests size Thumb, but not clearing the full image as it may be used later
            default:
                return "Max";//Max is not yet implemented.
            }
    }
}

// </editor-fold>
