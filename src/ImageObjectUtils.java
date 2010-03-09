
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

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
