
import java.awt.Dimension;
import java.awt.RenderingHints;
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


class MultiSizeImage{
    //make these private to some extent. Or make each multiSize image private.
    BufferedImage fullImage = null;
    //BufferedImage mediumImage = null;
    BufferedImage thumbImage = null;
    Dimension dimensions = null;

}
class FilterState{
    int contrast;
    int brightness;
    boolean isInverted;

    FilterState(){
        constructor(false,50,50);
    }
    FilterState(boolean isInvert,int contrst, int bright){
        constructor(isInvert,contrst, bright);
    }
    void constructor(boolean isInvert,int contrst, int bright){
        isInverted = isInvert;
        contrast=contrst;
        brightness=bright;
    }
}
//enum ImageQuality{Sampled,Low,Medium,High}
 //</editor-fold>
//<editor-fold desc="ImageItem">
class ImageItem{
    //Will deal with the image being flipped/rotated/cropped/filtered
    //Will not hold image title, path
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
    Dimension getCurrentDimensions(){
        switch(fullType){
            case Icon:
                return iconImage.dimensions;
            case Filtered:
                return filteredImage.dimensions;
            default:
                return originalImage.dimensions;
        }
    }
    Dimension getFullDimension(){
        return originalImage.dimensions;
    }
    void setFullDimensions(Dimension in){
        originalImage.dimensions = in;
        filteredImage.dimensions = in;
    }

    int getNoPixels(){
        Dimension temp = getCurrentDimensions();
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
    boolean hasFullImage(){
        return (!hasNoCurrentFullImage());
    }
    boolean hasThumbImage(){
        return (!hasNoCurrentThumbImage());
    }

    void setFullImage(BufferedImage img,ImageType type){
        originalImage.fullImage = img;
        if(type.isBad()) fullType = type;
        else if(type.isFiltered()) {
            filterImage(ImgSize.Full);
            fullType = ImageType.Filtered;
        }
        fullType = type;
        originalImage.dimensions=new Dimension(img.getWidth(),img.getHeight());
    }
    void setThumbImage(BufferedImage img,ImageType type){
        originalImage.thumbImage = img;
        if(type.isBad()) thumbType = type;
        else if(type.isFiltered()) {
            filterImage(ImgSize.Thumb);
            thumbType = ImageType.Filtered;
        }
        thumbType = type;
    }

//    void setAllToIconImage(BufferedImage icon){
//        setToIconImage(icon,ImgSize.Full);
//        setToIconImage(icon,ImgSize.Thumb);
//    }
    void setToIconImage(BufferedImage icon,ImgSize size){
        if(size==ImgSize.Full){
        fullType = ImageType.Icon;
        iconImage.fullImage = icon;
        }
        if(size==ImgSize.Thumb){
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
    BufferedImage getCurrentThumbImage(){
        switch(thumbType){
            case Icon:
                return iconImage.thumbImage;
            case Filtered:
                return filteredImage.thumbImage;
            default:
                return originalImage.thumbImage;
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
            filteredImage.dimensions = new Dimension(destination.getWidth(),destination.getHeight());
            filteredImage.fullImage = destination;
        } else{
            filteredImage.thumbImage = destination;
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
        fullType = ImageType.None;
        originalImage.fullImage = null;
        filteredImage.fullImage=null;
    }
}