
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.ImageIcon;

public enum SysIcon {

    Logo("logo.png"),
    Question("question.gif"),
    Error("error.gif"),
    Info("oxygen/dialog-information-3.png"),//info.gif"),
    Help("help.gif"),
    Prev("oxygen/go-previous-6.png"),
    Next("oxygen/go-next-6.png"),
    Play("oxygen/media-playback-start-6.png"),
    Stop("oxygen/media-playback-stop-6.png"),
//    HideThumbs("oxygen/view-right-close.png"),
//    ShowThumbs("oxygen/folder-image.png"),
    Thumbs("oxygen/view-list-icons.png"),
    JTree("oxygen/view-sidetree-4.png"),
    Zoom100("oxygen/zoom-original-4.png"),
    ZoomFit("oxygen/document-preview.png"),
    ZoomToX("oxygen/preferences-system-windows-move.png"),//zoom-fit-best-4.png"),
    AddTag("oxygen/document-edit.png"),
    TagThis("oxygen/list-add-3.png"),
    TagTag("oxygen/feed-subscribe.png"),
    TagFilter("oxygen/edit-find-6.png"),
    BlueTooth("oxygen/preferences-system-bluetooth.png"),//phone-3.png"),
    Adjust("oxygen/color-fill.png"),
    Import("oxygen/application-x-egon.png"),
    ImportDir("oxygen/folder-image.png"); 
    ImageIcon Icon;
    URL imgURL;

    SysIcon(String path) {
        imgURL = getRes(path);
        boolean failed = true;
        if (imgURL != null) {
            try {
                if ((new File(GUI.class.getResource(path).toURI())).isFile()) {
                    Icon = new ImageIcon(imgURL);
                    failed = false;
                } else if (path.equals("logo.png")) {
                    Icon = new ImageIcon(); //Create empty icon if logo missing so GUI still loads.
                    failed = false;
                }
            } catch (URISyntaxException e) {
                System.err.println(e);
            }
        }
        if (failed) {
            System.err.println("Error creating icon: " + path);
            Icon = null;
        }
    }

    //needed due to bug in old versions of JVM
    static URL getRes(String path) {
        URL tempURL = SysIcon.class.getResource(path);
        return tempURL;
    }
}
