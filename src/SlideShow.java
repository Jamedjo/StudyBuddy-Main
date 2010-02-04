/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Student
 */
public class SlideShow implements Runnable {
    GUI mainGUI;
    
    SlideShow(GUI gui){
        mainGUI = gui;
    }

    @Override public void run(){
        while(true){
            try{
                Thread.sleep(2000);
                mainGUI.state.nextImage();
            } catch (InterruptedException e){
                return;
                //remember when you 'stop' thread, to create a new one to allow thread to be started again
            }
        }
    }
}
