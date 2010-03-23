
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

//enum ErrorImageType{FileNotFound,OutOfMemory}
class ErrorImages implements Runnable {
    private static final BufferedImage loadingAnim=SysIcon.LoadingAni1.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage fileNotFound = SysIcon.FileNotFound.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage outOfMemory = SysIcon.OutOfMemory.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage loading = SysIcon.Loading.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage unknownError = SysIcon.Error.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage noNotesFound = SysIcon.NoNotesFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage splashScreen = SysIcon.Splash.getBufferedImage(1.6, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage splashScreenZoom = SysIcon.Splash.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage directoryIcon = SysIcon.Directory.getBufferedImage(1.2, BufferedImage.TYPE_INT_ARGB);
    //improvement: use java graphics to draw without relying on any external files, so GUI won't crash if no external file access
    int t;//milliseconds
    static GUI mainGUI;
    static ArrayList<LoadingAnimationPane> panelAnims=new ArrayList<LoadingAnimationPane>();

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

    static void updateIcons(){
        if (panelAnims != null) {
            for (LoadingAnimationPane panel : panelAnims) {
                if (panel.shouldRepaint()) {
                    panel.updatetAffine();
                    //panel.repaint();
                }
            }
        }
    }
    public static void addPanel(LoadingAnimationPane panel){
        panelAnims.add(panel);
    }

    public static BufferedImage getLoading(){
        return loadingAnim;
    }
}