import java.net.URL;
import javax.swing.ImageIcon;

public enum SysIcon{
    Logo("logo.gif"),
    Question("question.gif"),
    Error("error.gif"),
    Info("info.gif"),
    Help("help.gif");

    ImageIcon Icon;
    SysIcon(String path){
        URL imgURL = SysIcon.class.getResource(path);
        if (imgURL != null) {
            Icon = new ImageIcon(imgURL);
        } else {
            System.err.println("Error creating icon: " + path);
        }
    }
}
