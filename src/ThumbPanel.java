import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import javax.swing.BoxLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
//import javax.swing.border.EtchedBorder;


class ThumbButton extends JPanel{
    Log log = new Log();
    private DragMode dragMode=DragMode.Click;
    GUI mainGUI;
    int size;
    int thumbOffset;
    int currentOffset;
    int hOffset;
    ThumbPanel parent;

    ThumbButton(GUI parentGUI,ThumbPanel parentTP, int squareSize,int im,int hBorder){
        mainGUI = parentGUI;
        size = squareSize;
        thumbOffset = im;
        hOffset = ((int)(hBorder/2));
        parent=parentTP;
        
        addMouseListener(new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
             if(thumbOffset!=0) parent.setToOffsetImage(thumbOffset);
             parent.lastPressed=thumbOffset;
             parent.mousePressed = true;
             updateCursor();
             repaint();
          }
          @Override
          public void mouseReleased(MouseEvent e) {
            parent.mousePressed = false;
            if(parent.lastPressed==0) parent.setToOffsetImage(0);
            updateCursor();
            repaint();
          }
          @Override
          public void mouseEntered(MouseEvent e){
              if((thumbOffset==0)&&(parent.lastPressed!=0)){
                  parent.mousePressed=false;
                  updateCursor();
                  repaint();
              }
          }
        });
        addMouseMotionListener(new MouseMotionListener(){
          @Override
          public void mouseDragged(MouseEvent e){
              if(parent.lastPressed==0){
                  int relative = (hOffset+(size/2)-e.getX());
                  if(relative>0){
                      System.out.println("Left Drag, relative "+relative);
                  }
                  else{
                      System.out.println("Right Drag, relative "+relative);
                  }
                  if(Math.random()<0.1)//bad code to ignore 9 out of 10 drag events.
                      parent.updateOffsetRelative(relative/10);
              }
          }
          @Override
          public void mouseMoved(MouseEvent e){
          }
        });

        this.setMinimumSize(new Dimension(size,size));
        this.setPreferredSize(new Dimension(size,size));
        this.setMaximumSize(new Dimension(size,size));
        this.setBackground(Color.darkGray);
        if(thumbOffset==0) {
            this.setBackground(Color.gray);
            this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.lightGray, Color.yellow));
            dragMode=DragMode.Scroll;
        }
        updateCursor();
    }
    DragMode getCursorMode(){
        return dragMode;
    }
    void setCursorMode(DragMode mode){
        dragMode = mode;
        updateCursor();
    }
    void updateCursor(){
        if(parent.mousePressed){
            setCursor(getCursorMode().closed);
        } else{
            setCursor(getCursorMode().open);
        }
    }

    //all scaling in terms of height. max size is 20 times minimum.????
    public void paintComponent(java.awt.Graphics g) {
	//if(mainGUI.getState().isLocked) return;

        updateCursor();

	Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);


	//Use icons for thumbnails, populate icons in loop and then position icons.

	//int currentThumb = mainGUI.state.currentI;
        currentOffset=thumbOffset+parent.thumbNoOffset;

	//if(currentOffset<mainGUI.getState().numberOfImages){//should allow large positives and negatives
	    //set dimension
	    //currentThumb = mainGUI.state.next(currentThumb);
	    useWH = mainGUI.getState().getRelImageWH(ImgRequestSize.Thumb,size,size,currentOffset);
	    int thumbOfsetW= (size - useWH.width)/2;
	    int thumbOfsetH= (size - useWH.height)/2;
	    //mainGUI.mainPhoto.setIcon(mainGUI.state.imageList[currentThumb].getIcon(ImgRequestSize.Thumb));
	    g2.drawImage(mainGUI.getState().getBImageI(currentOffset,ImgRequestSize.Thumb), thumbOfsetW+hOffset, thumbOfsetH,useWH.width,useWH.height, this);
	//}
    }

}

class ThumbPanel extends JPanel implements MouseWheelListener{
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    //int boardH_start = 100;
    int noTiles;
    int maxNoTiles;
    int squareSize = 110;
    int hBorder = 3;
    int tilesHigh = 1;
    ThumbButton[] thumbnails;
    final GUI mainGUI;
    int thumbNoOffset=0;
    Thread timerThread= new Thread(new ScrollTimer(this,0));
    boolean mousePressed = false;
    int lastPressed=0;

    ThumbPanel(GUI parentGUI) {
	mainGUI = parentGUI;
        //this.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        this.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
	gridSize = new Dimension(boardW_start,squareSize);
        this.setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
        boardW = boardW_start;
        this.addMouseWheelListener(this);
        //this.add(buildThumbHolders(),BorderLayout.WEST);
        this.validate();
    }

//     JPanel buildThumbHolders(){
      synchronized JPanel buildThumbHolders(){
        final int sizeDiff=20;
        int sizeW = squareSize + hBorder;
        if(boardW<=sizeW) maxNoTiles=1;
        else maxNoTiles= (((boardW-sizeW)-(boardW % (sizeW-sizeDiff)))/(sizeW-sizeDiff) )+1;//finds space left after first (big) tile, divides this by size of smaller tiles. has modulo to help with rounding
        if((maxNoTiles>3)&&((maxNoTiles%2)==0)) maxNoTiles--;

        noTiles = Math.min(maxNoTiles,(mainGUI.getState().numberOfImages));//-1));//remove -1 to show currentI too
       //**// log.print(LogType.Debug,"now showing "+noTiles+" thumbnails");
          JPanel centrePan = new JPanel();
        if (noTiles < 0) noTiles=0;
        centrePan.setLayout(new BoxLayout(centrePan, BoxLayout.LINE_AXIS));
        centrePan.setBackground(Color.darkGray);
        thumbnails = new ThumbButton[noTiles];
        int thumbNumber,useSize;
        for (int i=0;i<thumbnails.length;i++){
            thumbNumber=i-(noTiles/2);//(i+1)
            useSize = (thumbNumber==0)? squareSize : squareSize-sizeDiff;//make non-current thumbs 3pixels smaller
            thumbnails[i] = new ThumbButton(mainGUI,this,useSize,thumbNumber,hBorder);
            centrePan.add(thumbnails[i]);
            //if((i+1)<thumbnails.length) centrePan.add(Box.createRigidArea(new Dimension(2,0)));//gap between thumbnails
        }
        //centrePan.setMaximumSize(new Dimension(squareSize*thumbnails.length,squareSize*1));

        if(noTiles>=2){
            tilesHigh = 1;
            centrePan.setMinimumSize(new Dimension(squareSize,squareSize));
            centrePan.setPreferredSize(new Dimension(((sizeW-sizeDiff)*(noTiles-1))+sizeW,squareSize));//Includes border
        } else {
            tilesHigh = 0;
            centrePan.setMinimumSize(new Dimension(0,0));
            centrePan.setPreferredSize(new Dimension(0,0));
        }
        centrePan.validate();
        return centrePan;
    }

    void onResize(){
	//boardW = getParent().getWidth();
	//boardH = getParent().getHeight();
	boardW = this.getWidth();
	boardH = this.getHeight();

//change number of tiles if needed
        this.removeAll();
        //this.remove(0);
        int oldTilesHigh= tilesHigh;
        JPanel thumbHolder = buildThumbHolders();
        //thumbHolder.setAlignmentY(Component.TOP_ALIGNMENT);
        this.add(thumbHolder);
        this.setPreferredSize(thumbHolder.getPreferredSize());
        this.setMinimumSize(thumbHolder.getMinimumSize());
        if(oldTilesHigh!=tilesHigh) RepaintManager.repaint(RepaintType.MainPanel);
        getParent().validate();
        this.validate();

	getParent().repaint();
	this.repaint();
	//this.setPreferredSize(new Dimension(boardW,boardH));
	//this.revalidate();       
    }

    int getOffsetNo(){
        return thumbNoOffset;
    }
    void setToOffsetImage(int thumbNum){
        timerThread.interrupt();
        timerThread = new Thread(new ScrollTimer(this, 300));
        mainGUI.getState().offsetImage(thumbNoOffset + thumbNum);
        thumbNoOffset = 0;
        mousePressed=false;
        repaint();
    }

    void updateOffsetRelative(int by){
        thumbNoOffset+=by;
        if(Math.abs(thumbNoOffset)>mainGUI.getState().lastIndex)
            thumbNoOffset%=mainGUI.getState().numberOfImages;
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        updateOffsetRelative(e.getWheelRotation());//-e.getWheelRotation());//not sure which direction is better

        timerThread.interrupt();
        timerThread = new Thread(new ScrollTimer(this, 300));
        timerThread.start();
    }
    
    @Override
    public void paintComponent(java.awt.Graphics g) {
        try {
            for (int i = 0; i < thumbnails.length; i++) {//Draw thumbs in correct order, so swingworker loads in order
                thumbnails[i].paintComponent(g);
            }
        } catch (NullPointerException e) {
            Log.Print(LogType.Error, "Error painting thumnail");
        }
        super.paintComponent(g);
    }
}

class ScrollTimer implements Runnable {
    ThumbPanel parent;
    int t;

    ScrollTimer(ThumbPanel parnt,int time){
        parent=parnt;
        t = time;
    }
    @Override public void run(){
        while(true){
            try{
                Thread.sleep(t);
                parent.mainGUI.getState().offsetImage(parent.getOffsetNo());
                parent.thumbNoOffset=0;
            } catch (InterruptedException e){
                return;
                //remember when you 'stop' thread, to create a new one to allow thread to be started again
            }
        }
    }
}
