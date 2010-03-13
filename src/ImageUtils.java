//Library of code under lib/
//Various image utilities. needed as default image reader could not read thumbnails from exif
//import org.apache.sanselan.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;

class ImageUtils{

    static void saveThumbToFile(Settings settings, BufferedImage bThumb,File pathFile,long fileLength, long modifiedDateTime){
        try{
            File thumbPath = new File(settings.getSetting("homeDir") + settings.getSetting("thumbnailPathExt"));
            File thumbfile = new File(thumbPath,getSaveEncoding(pathFile,fileLength, modifiedDateTime));
            ImageIO.write(bThumb,"jpg",thumbfile);//should use same format as file
        } catch (IOException e){
            Log.Print(LogType.Error,"Error creating thumbnail for image: "+pathFile.toString());
        }
    }

    //Only load thumb if all same.
    static String getSaveEncoding(File pathFile,long fileLength, long modifiedDateTime){
        return getUID(pathFile,fileLength,modifiedDateTime)+".jpg";
    }

    //Get unique id for the given file
    //uses filename, modified date&time, path, and filesizefilesize
    //creates a quick pseudo checksum, which will be unique for that imate
    //and which will be the same for that image even if it is removed from the DB and added again, or added twice.
    static String getUID(File pathFile,long fileLength, long modifiedDateTime){
        String basic = (pathFile.toString()+fileLength+modifiedDateTime);
        byte[] b = (basic.getBytes());
        int l = b.length/2;
        int l2 = b.length - l;
        byte[] first = new byte[l];
        byte[] snd = new byte[l2];
        int i;
        for(i=0;i<l;i++){
            first[i]=b[i];
        }//l2 = Math.min(l2,32);
        for(i=0;i<l2;i++){
            snd[i]=b[b.length-(i+1)];
        }
        int hashA = (new String(first)).hashCode();
        String out = (new String(snd))+hashA;
        return out.replaceAll("[^\\d\\w]", "");
    }
    
    static BufferedImage getThumbFromExif(File pathFile) {//v.quick, but only works for some images
        BufferedImage tempImage = null;
        try {
            IImageMetadata metadata = Sanselan.getMetadata(pathFile);
            if (metadata instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                tempImage = jpegMetadata.getEXIFThumbnail();
            }
        } catch (ImageReadException e) {
            Log.Print(LogType.DebugError, "Error reading exif of image " + pathFile.toString() + "\nError was: " + e.toString());
        } catch (IOException e) {
	    Log.Print(LogType.DebugError,"Error- can not read dimensions of image " + pathFile.toString() + "\nError was: " + e.toString());
	}
        return tempImage;
    }
    
    static String getFileExtLowercase(String name) {
        String ext = null;
        int pos = name.lastIndexOf(".");
        if (pos > 0 && pos < (name.length() - 1)) {
            ext = name.substring(pos + 1).toLowerCase();
        }
        if (ext == null) {
            Log.Print(LogType.DebugError, "Unable to get file extension from " + name);
        }
        return ext;
    }

    static Dimension getImageDimensionsSanslan(File pathFile){
        if(pathFile==null) return null;
        Dimension image_d = null;
	try{
	     image_d = Sanselan.getImageSize(pathFile);
	} catch (IOException e) {
	    Log.Print(LogType.DebugError,"Error; reading dimensions of image " + pathFile.toString() + "\nError was: " + e.toString());
	}  catch (ImageReadException e) {
            Log.Print(LogType.DebugError,"Error reading exif dimensions of image " + pathFile.toString() + "\nError was: " + e.toString());
	}
        return image_d;
    }

        //Finds maximum with and height somthing can be scaled to, without changing aspect ratio
    //Takes the dimensions of the object inW and inH
    //and the dimensions of the box it is to be fitted into maxW and maxH
    //Returns (Width,Height) as an array of two integers.
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

    static Dimension getImageWH(ImgRequestSize size, int MaxW, int MaxH,ImageReference relImage){
        Dimension useWH;// = new Dimension();
        Dimension relImageDimension;
	if(size.isLarge()){
            relImageDimension = relImage.getDimensionsWithMake();
	}
	else {
            relImageDimension = relImage.getThumbDimensionsWithMake();
        }
        useWH= scaleToMax(relImageDimension.width,relImageDimension.height, MaxW, MaxH);
	return useWH;
    }
}
