
import java.awt.Rectangle;
import javax.swing.JViewport;

public class DragUpdate implements Runnable {
    MainPanel mainPanel;
    int t;
    
    DragUpdate(MainPanel mPanel,int updatePeriod){
        mainPanel = mPanel;
        t = updatePeriod;
    }

    @Override public void run(){
        while(true){
            try{
                Thread.sleep(t);
                //The user is dragging us, so scroll!
		Rectangle r = ((JViewport)mainPanel.getParent()).getViewRect();
                    r.translate(mainPanel.pressX-mainPanel.nowX, mainPanel.pressY-mainPanel.nowY);//TODO: ensure not translating out of range.
                    mainPanel.scrollRectToVisible(r);
            } catch (InterruptedException e){
                return;
                //remember when you 'stop' thread, to create a new one to allow thread to be started again
            } catch (Exception e){
                System.out.println("Caught drag error:\n"+e);
            }
        }
    }
}
