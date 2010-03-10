import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import javax.swing.BoxLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
//import javax.swing.border.EtchedBorder;


class ThumbButton extends JPanel{
    Log log = new Log();
    GUI mainGUI;
    int size;
    int thumbNumber;
    int hOffset;

    ThumbButton(GUI parentGUI, int squareSize,int im,int hBorder){
        mainGUI = parentGUI;
        size = squareSize;
        thumbNumber = im;
        hOffset = ((int)(hBorder/2));

        addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent e) {
              //set current image to one clicked
              mainGUI.state.offsetImage(thumbNumber);
          }
        });

        this.setMinimumSize(new Dimension(size,size));
        this.setBackground(Color.darkGray);
    }

    //all scaling in terms of height. max size is 20 times minimum.????
    public void paintComponent(java.awt.Graphics g) {
	if(mainGUI.state.isLocked) return;

	Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);


	//Use icons for thumbnails, populate icons in loop and then position icons.

	//int currentThumb = mainGUI.state.currentI;
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	if(thumbNumber<mainGUI.state.numberOfImages){// use <= to show currentI too
	    //set dimension
	    //currentThumb = mainGUI.state.next(currentThumb);
	    useWH = mainGUI.state.getRelImageWH(ImgSize.Thumb,size,size,thumbNumber);
	    thumbOfsetW= (size - useWH.width)/2;
	    thumbOfsetH= (size - useWH.height)/2;
	    //mainGUI.mainPhoto.setIcon(mainGUI.state.imageList[currentThumb].getIcon(ImgSize.Thumb));
	    g2.drawImage(mainGUI.state.getBImageI(thumbNumber,ImgSize.Thumb), thumbOfsetW+hOffset, thumbOfsetH,useWH.width,useWH.height, this);
	}
    }

}

class ThumbPanel extends JPanel implements MouseWheelListener{
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    //int boardH_start = 100;
    int noTiles;
    int maxNoTiles;
    int squareSize = 100;
    int hBorder = 3;
    int tilesHigh = 1;
    ThumbButton[] thumbnails;
    final GUI mainGUI;

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
        int sizeW = squareSize + hBorder;
        maxNoTiles = (boardW-(boardW % sizeW)) / sizeW; //removes remainder to ensure int
//        while(mainGUI.isChangingState){
//            try{
//                wait();
//            } catch (InterruptedException e){}
//        }
        noTiles = Math.min(maxNoTiles,(mainGUI.state.numberOfImages-1));//remove -1 to show currentI too
       //**// log.print(LogType.Debug,"now showing "+noTiles+" thumbnails");

        thumbnails = new ThumbButton[noTiles];
        JPanel centrePan = new JPanel();
        centrePan.setLayout(new BoxLayout(centrePan,BoxLayout.LINE_AXIS));
        centrePan.setBackground(Color.darkGray);

        for (int i=0;i<thumbnails.length;i++){
            thumbnails[i] = new ThumbButton(mainGUI,squareSize,(i+1),hBorder);
            centrePan.add(thumbnails[i]);
            //if((i+1)<thumbnails.length) centrePan.add(Box.createRigidArea(new Dimension(2,0)));//gap between thumbnails
        }
        //centrePan.setMaximumSize(new Dimension(squareSize*thumbnails.length,squareSize*1));

        if(noTiles>=1){
            tilesHigh = 1;
            centrePan.setMinimumSize(new Dimension(squareSize,squareSize));
            centrePan.setPreferredSize(new Dimension((squareSize+hBorder)*noTiles,squareSize));//Includes border
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
        if(oldTilesHigh!=tilesHigh) mainGUI.mainPanel.onResize();
        getParent().validate();
        this.validate();

	getParent().repaint();
	this.repaint();
	//this.setPreferredSize(new Dimension(boardW,boardH));
	//this.revalidate();       
    }
    
//    public void repaint(){
//        super.repaint();
//        try{
//        for(int i=0;i<thumbnails.length;i++){//Draw thumbs in correct order, so swingworker loads in order
//            thumbnails[i].repaint();
//        }
//        } catch (NullPointerException e){
//            Log.Print(LogType.Error, "Error painting thumnail");
//        }
//    }

    @Override public void mouseWheelMoved(MouseWheelEvent e){
        mainGUI.state.offsetImage(-e.getWheelRotation());//not sure which direction is better
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
