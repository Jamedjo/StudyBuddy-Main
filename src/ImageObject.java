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
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.net.URISyntaxException;
import java.util.Calendar;

//Library of code under lib/
//Various image utilities. needed as default image reader could not read thumbnails from exif
import javax.swing.SwingWorker;
import org.apache.sanselan.*;

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
    boolean bImageLoaded = false;//Only set if loaded via swingWorker
    boolean bThumbLoaded = false;//Only set if loaded via swingWorker
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

        setVars();

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
        bImage = createLoadingThumb();
        bThumb = createLoadingThumb();
    }

    void setImageFromLoader(BufferedImage b,BufferedImage thmb,ImgSize size){
        bImage = b;
        bThumb = thmb;
        isLoading = false;
        currentLarge = size;
//            if ((bImageLoaded == false)&&()) {
//                bImageLoaded=true;
//            }
//            if ((bThumbLoaded == false)&&()) {
//                bImageLoaded = true;
//            }
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

class ImageLoader extends SwingWorker<BufferedImage, Void> {
    BufferedImage loadBImage,loadBThumb;
    Log log = new Log();
    File pathFile,thumbPath;
    String absolutePath,imageID;
    ImgSize size,currentLarge;
    int imgType;
    int screenWidth,screenHeight,thumbMaxW,thumbMaxH;
    ImageObject parent;//needed to publish result
    boolean success = false;

    ImageLoader(ImageObject p,File pF, String aP, ImgSize sz,
            int iT,File tP,String iID,int sW,int sH,int tW,int tH,BufferedImage lBT) {
        pathFile = pF;
        absolutePath = aP;
        size = sz;
        imgType = iT;
        thumbPath = tP;
        imageID = iID;
        screenWidth = sW;
        screenHeight = sH;
        thumbMaxW = tW;
        thumbMaxH = tH;
        parent=p;
        loadBThumb=lBT;
    }

    public BufferedImage doInBackground() {
        try {
            long start = Calendar.getInstance().getTimeInMillis();
            //ImageIO.setUseCache(true);
            //ImageIO.setCacheDirectory(pathFile);
            loadBImage = ImageIO.read(pathFile);
            log.print(LogType.Debug, "Loading image " + absolutePath + "\n      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to read image to memory");
            start = Calendar.getInstance().getTimeInMillis();

            if ((size == ImgSize.Screen) || (size == ImgSize.ThumbFull)) {//&& not thumb only (as this would be extra work)
                loadBImage = makeScreenImg(loadBImage);
                currentLarge = ImgSize.Screen;
            } else {
                currentLarge = ImgSize.Max;
            }

            log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to process image");
            makeThumb(loadBImage);
            start = Calendar.getInstance().getTimeInMillis();
            log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to get width,height&orientation");
            start = Calendar.getInstance().getTimeInMillis();
        success = true;
        } catch (IOException e) {
            log.print(LogType.Error, "Error loading image " + absolutePath + "\nError was: " + e.toString());
            setToXasFileNotFound();
            //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            log.print(LogType.Error, "Image file " + absolutePath + " could not be found " + "\nError was: " + e.toString());
            setToXasFileNotFound();
        } catch (NullPointerException e) {
            log.print(LogType.Error, "Could not load image from file " + absolutePath + "\nError was: " + e.toString());
            setToXasFileNotFound();
        } catch (java.lang.OutOfMemoryError e) {
            log.print(LogType.Error, "Fatal Error. Out of heap memory.\nSwingWorker should be used in code, and not all images should be buffered");
        }
        return loadBImage;
    }

    protected void done(){
        try {
               parent.setImageFromLoader(get(),loadBThumb,currentLarge);
           } catch (Exception ignore) {
           }

    }

    void setToXasFileNotFound() {
        //set image to error icon
        //improvement: set the buffered image to a java graphics drawn X icon
        try {
            loadBImage = ImageIO.read(SysIcon.FileNotFound.imgURL);
            loadBThumb = ImageIO.read(SysIcon.FileNotFound.imgURL);
        } catch (IOException e) {
            log.print(LogType.Error, "Error loading image: " + e.toString());
            //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    void makeThumb(BufferedImage bigImg) {//quick one image read to bimage
        long start = Calendar.getInstance().getTimeInMillis();
        Dimension iconWH = ImageObjectUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), thumbMaxW, thumbMaxH);
        if (!(iconWH.width < bigImg.getWidth())) {
            loadBThumb = bigImg;
            return;
        }

        //Image tempimage = bigIcon.getImage();
        BufferedImage tempimage = new BufferedImage(iconWH.width, iconWH.height, imgType);

        Graphics2D g2 = tempimage.createGraphics();//TYPE_INT_RGB
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.drawImage(bigImg, 0, 0, iconWH.width, iconWH.height, null);
        g2.dispose();
        log.print(LogType.Debug, "  -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to scale thumbnail");
        loadBThumb = tempimage;
        ImageObjectUtils.saveThumbToFile(thumbPath, absolutePath, loadBImage, imageID);
    }

    //could merge two functions
    BufferedImage makeScreenImg(BufferedImage bigImg) {
        Dimension iconWH = ImageObjectUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), screenWidth, screenHeight);
        if (!(iconWH.width < bigImg.getWidth())) {
            return bigImg;
        }
        //Image tempimage = bigIcon.getImage();
        BufferedImage tempimage = (new BufferedImage(iconWH.width, iconWH.height, imgType));

        Graphics2D g2 = tempimage.createGraphics();//TYPE_INT_RGB takes 4bytes, this takes 3. Less memory used
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//might not be avaliable on all systems // might not have spelt biquibic right
        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(bigImg, 0, 0, iconWH.width, iconWH.height, null);
        g2.dispose();

        return tempimage;
    }

}

class ImageObjectUtils{

    static void saveThumbToFile(File thumbPath, String absolutePath, BufferedImage bThumb,String imageID){
        try{
            File thumbfile = new File(thumbPath,getSaveEncoding(imageID));
            ImageIO.write(bThumb,"jpg",thumbfile);//should use same format as file
        } catch (IOException e){
            Log.Print(LogType.Error,"Error creating thumbnail for image: "+absolutePath);
        }
    }

    //Use imageID for name for now but change to include random key from DB.
    //In also use imageID,modified date,path,filesize to create quick pseudo checksum.
    //Only load thumb if all same.
    static String getSaveEncoding(String imageID){
        return imageID+"_thumb.jpg";
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
