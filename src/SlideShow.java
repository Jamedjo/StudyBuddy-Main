public class SlideShow implements Runnable {
    GUI mainGUI;
    int t;
    
    SlideShow(GUI gui,int time){
        mainGUI = gui;
        t = time;
    }

    @Override public void run(){
        while(true){
            try{
                Thread.sleep(t);
                mainGUI.getState().nextImage();
            } catch (InterruptedException e){
                return;
                //remember when you 'stop' thread, to create a new one to allow thread to be started again
            }
        }
    }
}
