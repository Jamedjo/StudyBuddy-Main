import java.awt.*;
import java.awt.event.*;

public class Splash extends Frame {

    public Splash() {

        this.addWindowListener(
                new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            e.getWindow().dispose();
        }  });
        final SplashScreen splash = SplashScreen.getSplashScreen();
        System.out.println("splashed");
        splash.close();
    }
    
    public static void main (String args[]) {
        Splash test = new Splash();
    }
}
