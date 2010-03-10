
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CancellationException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

class ImageLoader extends SwingWorker<BufferedImage, Void> {
    BufferedImage loadBImage,loadBThumb;
    Log log = new Log(false);
    File pathFile,thumbPath;
    String absolutePath,imageID;
    ImgSize size;
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
        if((!isCancelled())||size.isThumb()) try {
            long start = Calendar.getInstance().getTimeInMillis();
            //ImageIO.setUseCache(true);
            //ImageIO.setCacheDirectory(pathFile);
            
            loadBImage = ImageIO.read(pathFile);
            log.print(LogType.Debug, "Loading image " + absolutePath + "\n      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to read image to memory");
            start = Calendar.getInstance().getTimeInMillis();


            if(!isCancelled()){
                if ((size == ImgSize.Screen) /*|| (size == ImgSize.ThumbFull)*/) {//&& not thumb only (as this would be extra work)
                    loadBImage = makeScreenImg(loadBImage);
                }
            }

            log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to process image");
           
            //start = Calendar.getInstance().getTimeInMillis();
            //log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to get width,height&orientation");
            //start = Calendar.getInstance().getTimeInMillis();
            success = true;

        } catch (IOException e) {
            if(absolutePath.equals("///\\\\///\\\\\\NonExistingFile")){
                loadBImage = SysIcon.NoNotesFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
                if(loadBImage!=null) success = true;
            } else
            log.print(LogType.Error, "Error loading image " + absolutePath + "\nError was: " + e.toString());
        } catch (IllegalArgumentException e) {
            log.print(LogType.Error, "Image file " + absolutePath + " could not be found " + "\nError was: " + e.toString());
        } catch (NullPointerException e) {
            log.print(LogType.Error, "Could not load image from file " + absolutePath + "\nError was: " + e.toString());
        } catch (java.lang.OutOfMemoryError e) {
            log.print(LogType.Error, "Fatal Error. Out of heap memory.\nSwingWorker should be used in code, and not all images should be buffered");
        } finally{
            if(!success){
                loadBImage = setToXasFileNotFound();
            }
            makeThumb(loadBImage);//Will make thumb even if cancelled if already done that hard part
        }
        return loadBImage;
    }

    protected void done(){
        try {
               parent.setImageFromLoader(loadBImage,loadBThumb,size,isCancelled());
           } catch (CancellationException e) {
               //Hmm, thrown by get()
               log.print(LogType.Error,"Cancellation Exception");
           }catch (Exception e) {
               log.print(LogType.Debug, e);
           }

    }

    BufferedImage setToXasFileNotFound() {
        //improvement: set the buffered image to a java graphics drawn X icon
        return SysIcon.FileNotFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    }

    void makeThumb(BufferedImage bigImg) {//quick one image read to bimage
        long start = Calendar.getInstance().getTimeInMillis();
        Dimension iconWH = ImageObjectUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), thumbMaxW, thumbMaxH);
        if (!(iconWH.width < bigImg.getWidth())) {
            loadBThumb = bigImg;
        } else {

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
        }
        ImageObjectUtils.saveThumbToFile(thumbPath, absolutePath, loadBThumb, imageID);
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
