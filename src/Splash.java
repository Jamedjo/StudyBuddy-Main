import java.awt.*;
import java.awt.event.*;

public class Splash extends Frame {
    Log log = new Log();

    public Splash() {

        this.addWindowListener(
                new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            e.getWindow().dispose();
        }  });
        final SplashScreen splash = SplashScreen.getSplashScreen();
        log.print(LogType.Debug,"splashed");
        splash.close();
    }
    
    public static void main (String args[]) {
        Splash test = new Splash();
    }
}
