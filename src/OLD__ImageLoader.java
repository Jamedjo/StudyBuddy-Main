
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CancellationException;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

class OLD__ImageLoader extends SwingWorker<BufferedImage, Void> {
    Log log = new Log(false);
    double maxFilesizeToLoadThumb = 3;//Megabytes
    BufferedImage loadBImage,loadBThumb;
    File pathFile;
    ImgRequestSize size;
    int imgType;
    int screenWidth,screenHeight;
    static final int thumbMaxW = 200;
    static final int thumbMaxH = 200;
    long fileLength,modifiedDateTime;
    ImageReference parent;//needed to publish result
    boolean success = false;
    boolean outOfMemory=false;

    OLD__ImageLoader(ImageReference p,File pF, ImgRequestSize sz,
            int iT, BufferedImage lBT,long fL,long mDT) {
        pathFile = pF;
        size = sz;
        imgType = iT;
        Dimension scrD = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	screenWidth = scrD.width;
	screenHeight = scrD.height;
        parent=p;
        loadBThumb=lBT;
        fileLength = fL;
        modifiedDateTime = mDT;
    }

    public BufferedImage doInBackground() {
        if((!isCancelled())||size.isThumb()) try {

            if(size.isThumb()&&(pathFile.length()>(maxFilesizeToLoadThumb*1024*1024))){
                outOfMemory=true;
                throw new OutOfMemoryError("Image to large");
            }
            
            long start = Calendar.getInstance().getTimeInMillis();
            
            loadBImage = ImageIO.read(pathFile);
            log.print(LogType.Debug, "Loading image " + pathFile.toString() + "\n      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to read image to memory");
            start = Calendar.getInstance().getTimeInMillis();

            log.print(LogType.Debug, "      -Took " + (Calendar.getInstance().getTimeInMillis() - start) + " milliseconds to process image");
           
            success = true;

        } catch (IOException e) {
            if(pathFile.toString().equals("NoExistingFiles:a:b:c:d:e:f:g:h.i.j.k.l.m.n:o:p:non.ex")){
                loadBImage = SysIcon.NoNotesFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
                if(loadBImage!=null) success = true;
            } else
            log.print(LogType.Error, "Error loading image " + pathFile.toString() + "\nError was: " + e.toString());
        } catch (IllegalArgumentException e) {
            log.print(LogType.Error, "Image file " + pathFile.toString() + " could not be found " + "\nError was: " + e.toString());
        } catch (NullPointerException e) {
            log.print(LogType.Error, "Could not load image from file " + pathFile.toString() + "\nError was: " + e.toString());
        } catch (java.lang.OutOfMemoryError e) {
            if(!outOfMemory) {
                log.print(LogType.Error, "Fatal Error. Out of heap memory.\nImage "+pathFile.toString()+" is probably too large to load");
                outOfMemory=true;
            } else log.print(LogType.Error, "Error: requested thumbnail for "+(pathFile.length()/(1024*1024))+"MB when the max is "+maxFilesizeToLoadThumb+"MB.");
        } finally{
            if(outOfMemory){
                loadBImage = null;
                if(loadBThumb==null) loadBThumb =  getOutOfMemoryImage();
            } else if(!success){
                loadBImage = getFileNotFoundImage();
                if(loadBThumb==null) loadBThumb = getFileNotFoundImage();
            } else makeThumb(loadBImage);//Will make thumb even if cancelled if already done that hard part
        }
        return loadBImage;
    }

    protected void done(){
        try {
               parent.OLD_setImageFromLoader(loadBImage,loadBThumb,size,isCancelled(),outOfMemory);
           } catch (CancellationException e) {
               //Hmm, thrown by get()
               log.print(LogType.Error,"Cancellation Exception");
           }catch (Exception e) {
               log.print(LogType.Debug, e);
           }

    }

    BufferedImage getFileNotFoundImage() {
        //improvement: set the buffered image to a java graphics drawn X icon
        return SysIcon.FileNotFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    }
    BufferedImage getOutOfMemoryImage() {
        //improvement: set the buffered image to a java graphics drawn X icon
        return SysIcon.OutOfMemory.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    }

    void makeThumb(BufferedImage bigImg) {
        long start = Calendar.getInstance().getTimeInMillis();
        Dimension iconWH = ImageUtils.scaleDownToMax(bigImg.getWidth(), bigImg.getHeight(), thumbMaxW, thumbMaxH);
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
        ImageUtils.saveThumbToFile(parent.mainGUI.settings, loadBThumb,pathFile, fileLength,  modifiedDateTime);
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
