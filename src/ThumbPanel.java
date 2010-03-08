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
	//Use icons for thumbnails, populate icons in loop and then position icons.

	//int currentThumb = mainGUI.state.currentI;
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	if(thumbNumber<=mainGUI.state.numberOfImages){
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

//    synchronized JPanel buildThumbHolders(){
      JPanel buildThumbHolders(){
        int sizeW = squareSize + hBorder;
        maxNoTiles = (boardW-(boardW % sizeW)) / sizeW; //removes remainder to ensure int
//        while(mainGUI.isChangingState){
//            try{
//                wait();
//            } catch (InterruptedException e){}
//        }
        noTiles = Math.min(maxNoTiles,mainGUI.state.numberOfImages);
       //**// log.print(LogType.Debug,"now showing "+noTiles+" thumbnails");

        thumbnails = new ThumbButton[noTiles];
        JPanel centrePan = new JPanel();
        centrePan.setLayout(new BoxLayout(centrePan,BoxLayout.LINE_AXIS));
        centrePan.setMinimumSize(new Dimension(squareSize,squareSize));
        centrePan.setPreferredSize(new Dimension((squareSize+hBorder)*noTiles,squareSize));//Includes border
        centrePan.setBackground(Color.darkGray);

        for (int i=0;i<thumbnails.length;i++){
            thumbnails[i] = new ThumbButton(mainGUI,squareSize,(i+1),hBorder);
            centrePan.add(thumbnails[i]);
            //if((i+1)<thumbnails.length) centrePan.add(Box.createRigidArea(new Dimension(2,0)));//gap between thumbnails
        }
        //centrePan.setMaximumSize(new Dimension(squareSize*thumbnails.length,squareSize*1));

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
        JPanel thumbHolder = buildThumbHolders();
        //thumbHolder.setAlignmentY(Component.TOP_ALIGNMENT);
        this.add(thumbHolder);
        this.validate();

	this.repaint();
	//this.setPreferredSize(new Dimension(boardW,boardH));
	//this.revalidate();
	//getParent().repaint();

        
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e){
        mainGUI.state.offsetImage(-e.getWheelRotation());//not sure which direction is better
    }
}
