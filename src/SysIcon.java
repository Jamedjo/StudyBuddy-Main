
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public enum SysIcon {

    Logo("logo.png"){@Override ImageIcon fail(){return new ImageIcon();}}//Create empty icon if logo missing so GUI still loads.
    ,Splash("splash.png")
    ,Question("question.gif")
    ,Error("error.gif")
    ,Info("oxygen/dialog-information-3.png")//info.gif")
    ,Help("help.gif")
    ,Export("oxygen/document-save-as-3.png")
    ,Prev("oxygen/go-previous-6.png")
    ,Next("oxygen/go-next-6.png")
    ,Play("oxygen/media-playback-start-6.png")
    ,Stop("oxygen/media-playback-stop-6.png")
    ,Directory("oxygen/folder.png")
    ,Tick("oxygenbig/dialog-ok-4.png")
    ,HideThumbs("oxygen/list-remove-4.png","oxygencustom/thumbnailbar-32.png"){@Override void drawIt(){drawBottomRight();}}//document-edit.png")
    ,ShowThumbs("oxygen/list-add-3.png","oxygencustom/thumbnailbar-32.png"){@Override void drawIt(){drawBottomRight();}}//document-edit.png")
//    ,Thumbs("oxygen/view-list-icons.png")
    ,JTree("oxygen/view-sidetree-4.png")
    ,ImageBar("oxygen/applications-graphics-3.png","oxygencustom/toolbar-32.png"){@Override void drawIt(){drawMidRight();}}
    ,Zoom100("oxygen/zoom-original-4.png")
    ,ZoomFit("oxygen/document-preview.png")
    ,ZoomBig("oxygencustom/zoomBig32.png")
    ,ZoomSmall("oxygencustom/zoomSmall32.png")
    ,ZoomToX("oxygencustom/zoom-32.png")//preferences-system-windows-move.png"),//zoom-fit-best-4.png")
    ,AddTag("oxygen/list-add-3.png","oxygen/edit-rename.png"){@Override void drawIt(){drawBottomRight();}}//document-edit.png")
    ,TagThis("oxygen/feed-subscribe.png","oxygen/knotes-4.png"){@Override void drawIt(){drawBottomRight();}}
    ,QuickTag("oxygen/feed-subscribe.png","oxygen/view-pim-notes.png"){@Override void drawIt(){drawBottomRight();}}
    ,TagTag("oxygen/feed-subscribe.png","oxygen/feed-subscribe.png"){@Override void drawIt(){drawBottomRight();}}
    ,TagFilter("oxygencustom/zoom-32.png","oxygen/feed-subscribe.png")//("oxygen/strigi.png")
    ,DragPan("oxygen/transform-move.png")
    ,DragNote("oxygen/insert-text-2.png")//knotes-4.png")
    ,DragLink("oxygen/insert-link-2.png")
    ,BlueTooth("oxygen/preferences-system-bluetooth.png")//phone-3.png")
    ,Adjust("oxygen/color-fill.png")
    ,BigAdjust("oxygenbig/color-fill.png")
    ,LinkCursor("oxygen/quickopen.png")
    ,NoteCursor("oxygen/transform-crop.png")
    ,Import("oxygen/application-x-egon.png")
    ,ImportDir("oxygen/folder-image.png")
    ,Options("oxygen/preferences-system-3.png")
    ,Loading("oxygenbig/view-refresh-6.png"){@Override ImageIcon fail(){return new ImageIcon(new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB));}}//Create empty icon if png missing so GUI still loads.//edit-clear-history-3.png
    ,OutOfMemory("oxygenbig/dialog-warning-3.png"){@Override ImageIcon fail(){return new ImageIcon(new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB));}}//Create empty icon if png missing so GUI still loads.//edit-clear-history-3.png
    ,FileNotFound("oxygenbig/dialog-cancel-4.png"){@Override ImageIcon fail(){return new ImageIcon(new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB));}}
    ,NoNotesFound("oxygenbig/dialog-cancel-4.png","oxygenbig/view-pim-notes.png"){@Override ImageIcon fail(){return new ImageIcon(new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB));}}
    ,Mirror("oxygen/object-flip-horizontal-2.png")
    ,Flip("oxygen/object-flip-vertical-2.png")
    ,Rotate("oxygen/transform-rotate.png")

    ,LoadingAni1("oxygencustom/view-refresh-6-HQ-01_s.png"){@Override ImageIcon fail(){return Loading.Icon;}}

    ;
    ImageIcon Icon;
    URL imgURL;
    Log log = new Log();
    private Graphics2D g2;

    SysIcon(String path) {
        imgURL = getImgURL(path);
        Icon = buildIcon(path);
    }
    SysIcon(String path1, String path2){
        imgURL = getImgURL(path1);
        Icon = buildIcon(path1);

        URL i2URL  = getImgURL(path2);
        if(i2URL==null) return;
        
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        try{
        BufferedImage b = ImageIO.read(i2URL);
        g2 = b.createGraphics();
        g2.setComposite(ac);
        drawIt();
        Icon = new ImageIcon(b);
        } catch(IOException e){
            log.print(LogType.Error, "Error loading second image for icon: "+path2);
        } catch(Exception e){
            log.print(LogType.Error, "Error loading second image for icon: "+path2);
        }
    }
    void drawIt(){
        g2.drawImage(Icon.getImage(), 0, 0, null);
    }

    void drawBottomRight(){
        g2.drawImage(Icon.getImage(), Icon.getIconWidth()/2, Icon.getIconWidth()/2, Icon.getIconWidth()/2, Icon.getIconHeight()/2, null);
    }
    void drawMidLeft(){
        g2.drawImage(Icon.getImage(), 0, Icon.getIconWidth()/4, Icon.getIconWidth()/2, Icon.getIconHeight()/2, null);
    }
    void drawMidRight(){
        g2.drawImage(Icon.getImage(), Icon.getIconWidth()/2, Icon.getIconWidth()/4, Icon.getIconWidth()/2, Icon.getIconHeight()/2, null);
    }

    ImageIcon buildIcon(String path){
        ImageIcon tempIcon=fail();
        if (imgURL!=null) tempIcon = new ImageIcon(imgURL);
        return tempIcon;
    }
    URL getImgURL(String path){
        URL newURL = getRes(path);
        boolean failed = true;
        if (newURL != null) {
            try {
                if ((new File(GUI.class.getResource(path).toURI())).isFile()) {
                    failed = false;
                } else{
                    newURL = null;
                }
            } catch (URISyntaxException e) {
                log.print(LogType.Error, e);
                    newURL = null;
            } catch(IllegalArgumentException e){
                log.print(LogType.DebugError,"Error creating icon, or StudyBuddy is running from a .jar");
                failed=false;
            }
        }
        if (failed) {
            log.print(LogType.Error,"Error creating icon: " + path);
            //tempIcon = null;
        }
        return newURL;
    }

    ImageIcon fail(){return null;};
    
    //needed due to bug in old versions of JVM
    static URL getRes(String path) {
        URL tempURL = SysIcon.class.getResource(path);
        return tempURL;
    }

    //Returns a BufferedImage which is shinkFactor times the size of the Icon
    //so that the Icon shinks when the BufferedImage is displayed at the same size
    //Use 1 for no change. Less than one will result in an error due to drawing at negative coordinates
    // and 0 will result infinite coordinates in a zero size image
    BufferedImage getBufferedImage(double shrinkFactor,int imgType){
        if(shrinkFactor<1){
            log.print(LogType.DebugError,"Bad argument to SysIcon getBufferedImage");
            return null;
        }
        try{
        int newW =(int)(Icon.getIconWidth()*shrinkFactor);
        int newH =(int)(Icon.getIconHeight()*shrinkFactor);
        BufferedImage tempB = new BufferedImage(newW,newH,imgType);
        Icon.paintIcon(null, tempB.createGraphics(), (newW-Icon.getIconWidth())/2, (newH-Icon.getIconHeight())/2);
        return tempB;
        } catch(NullPointerException e){
            log.print(LogType.Error,"Unable to find SysIcon "+this.toString()+" for getBufferedImage");
            return new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB);
        }
    }

    ImageIcon smallIcon(int size){
        return new ImageIcon(getBufferedImage(1,BufferedImage.TYPE_INT_ARGB).getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }
}
