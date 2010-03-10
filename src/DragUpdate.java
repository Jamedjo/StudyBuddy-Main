
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JViewport;

public class DragUpdate implements Runnable {
    MainPanel mP;
    //int t;
    JViewport vP;
    Point p;
    
    DragUpdate(MainPanel mPanel,JViewport viewPort,Point point){//int updatePeriod){
        mP = mPanel;
        //t = updatePeriod;
        vP=viewPort;
        p=point;
    }

    @Override public void run(){
        //while(true){
        //try{
        //Thread.sleep(t);
        //The user is dragging us, so scroll!
                if(mP.getCursorMode() == DragMode.Drag) {
                    vP.setViewPosition(p);
                }
        //} catch (InterruptedException e) {
        //return;
        ////remember when you 'stop' thread, to create a new one to allow thread to be started again
        //} catch (Exception e) {
        //System.out.println("Caught drag error:\n"+e);
        //}
        //}
    }
}