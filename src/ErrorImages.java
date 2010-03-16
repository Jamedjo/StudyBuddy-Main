
import java.awt.image.BufferedImage;
import java.util.ArrayList;

//enum ErrorImageType{FileNotFound,OutOfMemory}
class ErrorImages implements Runnable {
    static final BufferedImage[] loadingAnim= {
        SysIcon.LoadingAni1.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni1b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni2.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni2b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni3.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni3b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni4.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni4b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni5.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni5b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni6.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni6b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni7.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni7b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni8.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB),
        SysIcon.LoadingAni8b.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB)
    };
    static final BufferedImage fileNotFound = SysIcon.FileNotFound.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage outOfMemory = SysIcon.OutOfMemory.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage loading = SysIcon.Loading.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage unknownError = SysIcon.Error.getBufferedImage(2, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage noNotesFound = SysIcon.NoNotesFound.getBufferedImage(1, BufferedImage.TYPE_INT_ARGB);
    static final BufferedImage splashScreen = SysIcon.Splash.getBufferedImage(1.6, BufferedImage.TYPE_INT_ARGB);
    //improvement: use java graphics to draw without relying on any external files, so GUI won't crash if no external file access

    static final int angle = 10;
    static int current = 0;//animations go from 1to8, array from 0to7.
    static final int numberOfSprites = 16;
    int t;//milliseconds
    static GUI mainGUI;
    static ArrayList<LoadingAnimationPane> panelAnims=new ArrayList<LoadingAnimationPane>();
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

    static void updateIcons(){
        if(current==(numberOfSprites-1)) current=0;
        else current++;
        mainGUI.mainPanel.repaint();
        if (panelAnims != null) {
            for (LoadingAnimationPane panel : panelAnims) {
                if (panel.shouldRepaint()) {
                    panel.repaint();
                }
            }
        }
    }
    public static void addPanel(LoadingAnimationPane panel){
        panelAnims.add(panel);
    }

    public static BufferedImage getLoading(){
        return loadingAnim[current];
    }
    public static BufferedImage getMainLoading(){
        mainPanelShouldRepaint=true;
        return loadingAnim[current];
    }

    public static void stopMainAnim(){
        mainPanelShouldRepaint=false;
    }

}