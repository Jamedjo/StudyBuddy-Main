//Library of code under lib/
//Various image utilities. needed as default image reader could not read thumbnails from exif
//import org.apache.sanselan.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;

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
    static BufferedImage getThumbFromExif(File pathFile, String absolutePath) {//v.quick, but only works for some images
        BufferedImage tempImage = null;
        try {
            IImageMetadata metadata = Sanselan.getMetadata(pathFile);
            if (metadata instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                tempImage = jpegMetadata.getEXIFThumbnail();
            }
        } catch (ImageReadException e) {
            Log.Print(LogType.Error, "Error reading exif of image " + absolutePath + "\nError was: " + e.toString());
        } catch (IOException e) {
	    Log.Print(LogType.Error,"Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	}
        return tempImage;
    }
    
    static String getFileExtLowercase(File pathFile, String absolutePath) {
        String ext = null;
        int pos = pathFile.getName().lastIndexOf(".");
        if (pos > 0 && pos < (pathFile.getName().length() - 1)) {
            ext = pathFile.getName().substring(pos + 1).toLowerCase();
        }
        if (ext == null) {
            Log.Print(LogType.Error, "Unable to get file extension from " + absolutePath);
        }
        return ext;
    }

    static Dimension getImageDimensionsSanslan(File pathFile, String absolutePath){
        if(pathFile==null) return null;
        Dimension image_d = null;
	try{
	     image_d = Sanselan.getImageSize(pathFile);
	} catch (IOException e) {
	    Log.Print(LogType.Error,"Error reading dimensions of image " + absolutePath + "\nError was: " + e.toString());
	}  catch (ImageReadException e) {
            Log.Print(LogType.Error,"Error reading exif dimensions of image " + absolutePath + "\nError was: " + e.toString());
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

    static Dimension getImageWH(ImgSize size, int MaxW, int MaxH,ImageObject relImage){
        Dimension useWH = new Dimension();
	if(size.isLarge()){
	    useWH= scaleToMax(relImage.getWidthAndMake(),relImage.getHeightAndMake(), MaxW, MaxH);
	}
	else {
	    useWH = scaleToMax(relImage.getWidthForThumb(),relImage.getHeightForThumb(), MaxW, MaxH);
        }
	return useWH;
    }
}
