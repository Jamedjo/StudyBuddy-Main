import java.net.URL;
import javax.swing.ImageIcon;

public enum SysIcon{
    Logo("logo.png"),
    Question("question.gif"),
    Error("error.gif"),
    Info("info.gif"),
    Help("help.gif");

    ImageIcon Icon;
    URL imgURL;

    SysIcon(String path){
	imgURL = getRes(path);
        if (imgURL != null) {
            Icon = new ImageIcon(imgURL);
        } else {
            System.err.println("Error creating icon: " + path);
        }
    }

    //needed due to bug in old versions of JVM
    static URL getRes(String path){
	URL tempURL = SysIcon.class.getResource(path);
	return tempURL;
    }

}
