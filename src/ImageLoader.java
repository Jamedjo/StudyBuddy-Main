import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CancellationException;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

class ImageLoader extends SwingWorker<BufferedImage, Void> {
    Log log = new Log(false);
    BufferedImage returnImage = null;
    File pathFile;
    int screenWidth, screenHeight;
    ImageReference parent;//needed to publish result
    boolean outOfMemory = false;
    ImageType returnImageType = ImageType.None;

    ImageLoader(ImageReference p, File pF) {
        pathFile = pF;
        parent = p;
    }
    public BufferedImage doInBackground() {
        try {
            if (!isCancelled()) {
                long start = Calendar.getInstance().getTimeInMillis();

                if(parent.getNoPixels()>(6 * 1024 * 1024))System.gc();//If greater than 6megapixels, or unknown, hint at garbage collection
                returnImage = ImageIO.read(pathFile);

                log.print(LogType.Debug, "Loading image " + pathFile.toString() + "\n      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to read image to memory");
                start = Calendar.getInstance().getTimeInMillis();

                log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to process image");

                returnImageType = ImageType.Original;
            }
        } catch (IOException e) {
            if (pathFile.toString().equals("NoExistingFiles:a:b:c:d:e:f:g:h.i.j.k.l.m.n:o:p:non.ex")) {
                returnImage = ErrorImages.noNotesFound;
                if (returnImage != null)//Needed if SysIcon can fail- e.g compound icon.
                    returnImageType = ImageType.Icon;
            } else
                log.print(LogType.Error, "Error loading image " + pathFile.toString() + "\nError was: " + e.toString());
        } catch (IllegalArgumentException e) {
            log.print(LogType.Error, "Image file " + pathFile.toString() + " could not be found " + "\nError was: " + e.toString());
        } catch (NullPointerException e) {
            log.print(LogType.Error, "Could not load image from file " + pathFile.toString() + "\nError was: " + e.toString());
        } catch (java.lang.OutOfMemoryError e) {
            log.print(LogType.Error, "Fatal Error. Out of heap memory.\nImage " + pathFile.toString() + " is probably too large to load");
            outOfMemory = true;
            returnImage = null;
        } finally {
            return returnImage;
        }
    }
    protected void done() {
        try {
            if (outOfMemory) {
                returnImage = ErrorImages.outOfMemory;
                returnImageType = ImageType.Icon;
            } else if (returnImageType == ImageType.None) {
                returnImage = ErrorImages.fileNotFound;
                returnImageType = ImageType.Icon;
                if(isCancelled()) outOfMemory = true;//analagous to having been cancelled for memory reasons
            }
            parent.setImageFromLoader(returnImage, returnImageType, outOfMemory);
        } catch (CancellationException e) {
            //Hmm, thrown by get()
            log.print(LogType.Error, "Cancellation Exception");
        } catch (Exception e) {
            //log.print(LogType.Error, e);
            e.printStackTrace();
        } finally {
            returnImage = null;
        }
    }

    //could merge two functions
//    BufferedImage makeScreenImg(BufferedImage bigImg) {
//        Dimension iconWH = ImageUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), screenWidth, screenHeight);
//        if (!(iconWH.width < bigImg.getWidth())) {
//            return bigImg;
//        }
//        //Image tempimage = bigIcon.getImage();
//        BufferedImage tempimage = (new BufferedImage(iconWH.width, iconWH.height, imgType));
//
//        Graphics2D g2 = tempimage.createGraphics();//TYPE_INT_RGB takes 4bytes, this takes 3. Less memory used
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);//might not be avaliable on all systems // might not have spelt biquibic right
//        //g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2.drawImage(bigImg, 0, 0, iconWH.width, iconWH.height, null);
//        g2.dispose();
//
//        return tempimage;
//    }
}
