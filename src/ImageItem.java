
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

enum ImgSize{Full,Thumb}
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
//enum ErrorImageType{FileNotFound,OutOfMemory}
class ErrorImages implements Runnable {
    static final BufferedImage[] loadingAnim= {
        SysIcon.LoadingAni1.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni2.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni3.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni4.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni5.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni6.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni7.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni8.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB)
    };
    static final BufferedImage fileNotFound = SysIcon.FileNotFound.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage outOfMemory = SysIcon.OutOfMemory.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage loading = SysIcon.Loading.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage unknownError = SysIcon.Error.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage noNotesFound = SysIcon.NoNotesFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    //improvement: use java graphics to draw without relying on any external files, so GUI won't crash if no external file access
    
    static final int angle = 10;
    static int current = 0;//animations go from 1to8, array from 0to7.
    static final int numberOfSprites = 8;
    int t;//milliseconds
    GUI mainGUI;
    static boolean mainPanelShouldRepaint = false;

     ErrorImages(int updatePeriod,GUI gui){
        t = updatePeriod;
        mainGUI = gui;
    }

    @Override public void run(){
        while(true){
            try{
                Thread.sleep(t);
                updateIcons();
            } catch (InterruptedException e){
                return;
                //remember when you 'stop' thread, to create a new one to allow thread to be started again
            }
        }
    }

    void updateIcons(){
        if(current==(numberOfSprites-1)) current=0;
        else current++;
        mainGUI.mainPanel.repaint();
        //loading=loadingAnim[current];
    }

    public static BufferedImage getLoading(){
        mainPanelShouldRepaint=true;
        return loadingAnim[current];
    }

    public static void stopAnim(){
        mainPanelShouldRepaint=false;
    }

}
class MultiSizeImage {
    //make these private to some extent. Or make each multiSize image private.
    volatile BufferedImage fullImage = null;
    //BufferedImage mediumImage = null;
    volatile BufferedImage thumbImage = null;
    volatile Dimension fullDimensions = null;
    volatile Dimension thumbDimensions = null;
}

class FilterState {
    int contrast;
    int brightness;
    boolean isInverted;
    FilterState() {
        constructor(false, 50, 50);
    }
    FilterState(boolean isInvert, int contrst, int bright) {
        constructor(isInvert, contrst, bright);
    }
    void constructor(boolean isInvert, int contrst, int bright) {
        isInverted = isInvert;
        contrast = contrst;
        brightness = bright;
    }
}
//enum ImageQuality{Sampled,Low,Medium,High}
class ImageItem{
    //Will deal with the image being flipped/rotated/cropped/filtered
    //Will not hold image title, path
    private MultiSizeImage originalImage = new MultiSizeImage();
    private MultiSizeImage filteredImage = new MultiSizeImage();
    private MultiSizeImage iconImage = new MultiSizeImage();
    FilterState filter = new FilterState();
    volatile ImageType fullType = ImageType.None;
    volatile ImageType thumbType = ImageType.None;
    //TransFormstate transform;
    //Orientation iOri

    ImageItem(){
//        iconImage.fullDimensions = new Dimension(ErrorImages.unknownError.getWidth(),ErrorImages.unknownError.getHeight());
//        iconImage.fullImage=ErrorImages.unknownError;
//        iconImage.thumbImage=iconImage.fullImage;
//        iconImage.thumbDimensions=iconImage.fullDimensions;
    }

    Dimension getCurrentFullDimensions(){//output of this is checked for null at geyDimensionsWithMake
        switch(fullType){
            case Original:
                return originalImage.fullDimensions;
            case Filtered:
                return filteredImage.fullDimensions;
            default:
                return iconImage.fullDimensions;
        }
    }
    Dimension getThumbDimensions(){
        Dimension returnDim = null;
        switch(fullType){
            case Original:
                returnDim = originalImage.thumbDimensions;
            case Filtered:
                returnDim = filteredImage.thumbDimensions;
            case Icon:
                returnDim = iconImage.thumbDimensions;
            default:
                returnDim = originalImage.thumbDimensions;
        }
        if(returnDim==null) returnDim = new Dimension(0,0);
        return returnDim;
    }
    Dimension getFullDimensions(){
        return originalImage.fullDimensions;
    }
    void setFullDimensions(Dimension in){
        originalImage.fullDimensions = in;
        filteredImage.fullDimensions = in;
    }

    int getNoPixels(){
        Dimension temp = getFullDimensions();
        return temp.width*temp.height;
    }

    boolean hasNoCurrentFullImage(){
        if(fullType.isNone()) return true;
        return false;
    }
    boolean hasNoCurrentThumbImage(){
        if(thumbType.isNone()) return true;
        return false;
    }
    boolean hasNoGoodFullImage(){
        if(fullType.isBad()) return true;
        return false;
    }
    boolean hasNoGoodThumbImage(){
        if(thumbType.isBad()) return true;
        return false;
    }
    boolean hasGoodFullImage(){
        if(fullType.isBad()) return false;
        return true;
    }
    boolean hasGoodThumbImage(){
        if(thumbType.isBad()) return false;
        return true;
    }
    boolean hasAnyFullImage(){
        if(fullType.isNone()) return false;
        return true;
    }
    boolean hasAnyThumbImage(){
        if(thumbType.isNone()) return false;
        return true;
    }

    synchronized void setFullImage(BufferedImage img,ImageType type){
        originalImage.fullImage = img;
        if(img!=null) originalImage.fullDimensions=new Dimension(img.getWidth(),img.getHeight());
        else {
            Log.Print(LogType.Error, "trying to set full image to null");
            Thread.dumpStack();
        }
        if(type.isBad()) fullType = type;
        else if(type.isFiltered()) {
            filterImage(ImgSize.Full);
            fullType = ImageType.Filtered;
        }
        fullType = type;
    }
    synchronized void setThumbImage(BufferedImage img,ImageType type){
        if((img!=null)) originalImage.thumbDimensions=new Dimension(img.getWidth(),img.getHeight());
        //if(img==null) {Log.Print(LogType.Error, "Setting icon to null");Thread.dumpStack();return;}
        originalImage.thumbImage = img;
        if(type.isBad()) thumbType = type;
        else if(type.isFiltered()) {
            filterImage(ImgSize.Thumb);
            thumbType = ImageType.Filtered;
        }
        else thumbType = type;//original and not to be filtered
    }

//    void setAllToIconImage(BufferedImage icon){
//        setToIconImage(icon,ImgSize.Full);
//        setToIconImage(icon,ImgSize.Thumb);
//    }
    synchronized void setToIconImage(BufferedImage icon, ImgSize size) {
        //if(icon==null) {Log.Print(LogType.Error, "Setting icon to null");Thread.dumpStack();return;}
        if (size == ImgSize.Full) {
            fullType = ImageType.Icon;
            iconImage.fullDimensions=new Dimension(icon.getWidth(),icon.getHeight());
            iconImage.fullImage = icon;
        } else if (size == ImgSize.Thumb) {
            thumbType = ImageType.Icon;
            iconImage.thumbDimensions=new Dimension(icon.getWidth(),icon.getHeight());
            iconImage.thumbImage = icon;
        }
    }

    BufferedImage getCurrentFullImage(){
        switch(fullType){
            case Icon:
                return iconImage.fullImage;
            case Filtered:
                return filteredImage.fullImage;
            case Original:
                return originalImage.fullImage;
            default:
                return ErrorImages.unknownError;
        }
    }
    BufferedImage getCurrentThumbImage(){
        switch(thumbType){
            case Icon:
                return iconImage.thumbImage;
            case Filtered:
                return filteredImage.thumbImage;
            case Original:
                return originalImage.thumbImage;
            default:
                return ErrorImages.unknownError;
        }
    }
    private void filterImage(ImgSize size){
        int i;
        BufferedImage[] srcs = {originalImage.fullImage,originalImage.thumbImage};
        BufferedImage destination;
        if(size==ImgSize.Full) i=0;
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
        if ((((size==ImgSize.Full)&&(filteredImage.fullImage== null)))||(((size==ImgSize.Thumb)&&(filteredImage.thumbImage== null))))
            destination = new BufferedImage(srcs[i].getWidth(), srcs[i].getHeight(), BufferedImage.TYPE_INT_RGB);//*/op.createCompatibleDestImage(srcImg, null);
        else{
            destination = new BufferedImage[]{filteredImage.fullImage,filteredImage.thumbImage}[i];
        }
        op.filter(srcs[i], destination);
        if(size==ImgSize.Full) {
            filteredImage.fullDimensions = new Dimension(destination.getWidth(),destination.getHeight());
            filteredImage.fullImage = destination;
        } else{
            filteredImage.thumbImage = destination;
            filteredImage.thumbDimensions = new Dimension(destination.getWidth(),destination.getHeight());
        }
    }

    void resetFilters(){//set to original values
        if(fullType==ImageType.Filtered) fullType=ImageType.Original;
        if(thumbType==ImageType.Filtered) thumbType=ImageType.Original;
        filter = new FilterState();
    }
    private void updateFilters(){
        filterImage(ImgSize.Full);
        filterImage(ImgSize.Thumb);
        if(filteredImage.fullImage!=null) fullType = ImageType.Filtered;
        if(filteredImage.thumbImage!=null) thumbType = ImageType.Filtered;
    }
    void refreshFilters(){
        if((filter.contrast==50)&&(filter.brightness==50)&&(!filter.isInverted)) {
            resetFilters();
            return;
        }
        else updateFilters();
    }
    void clearMem(){
        //clears the full size image.
        if(fullType!=ImageType.Icon){
            fullType = ImageType.None;
        }
        originalImage.fullImage = null;
        filteredImage.fullImage=null;
    }
}