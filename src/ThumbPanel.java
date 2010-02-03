import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import javax.swing.BoxLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


class ThumbButton extends JPanel{
    GUI mainGUI;
    int size;
    int thumbNumber;

    ThumbButton(GUI parentGUI, int squareSize,int im){
        mainGUI = parentGUI;
        size = squareSize;
        thumbNumber = im;

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
	if(thumbNumber<=mainGUI.state.imageIDs.length){
	    //set dimension
	    //currentThumb = mainGUI.state.next(currentThumb);
	    useWH = mainGUI.state.getRelImageWH(ImgSize.Thumb,size,size,thumbNumber);
	    thumbOfsetW= (size - useWH.width)/2;
	    thumbOfsetH= (size - useWH.height)/2;
	    //mainGUI.mainPhoto.setIcon(mainGUI.state.imageList[currentThumb].getIcon(ImgSize.Thumb));
	    g2.drawImage(mainGUI.state.getBImageI(thumbNumber,ImgSize.Thumb), thumbOfsetW, thumbOfsetH,useWH.width,useWH.height, this);
	}
    }

}

class ThumbPanel extends JPanel{
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    //int boardH_start = 100;
    int noTiles = 5;
    int squareSize = 100;
    ThumbButton[] thumbnails;
    GUI mainGUI;

    ThumbPanel(GUI parentGUI) {
	mainGUI = parentGUI;
	gridSize = new Dimension(boardW_start,squareSize);
        this.setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
        boardW = boardW_start;
        this.add(buildThumbHolders(),BorderLayout.CENTER);
        this.validate();
    }

    JPanel buildThumbHolders(){
        int sizeW = squareSize + 6;
        noTiles = (boardW-(boardW % sizeW)) / sizeW; //removes remainder to ensure int
       //**// System.out.println("now showing "+noTiles+" thumbnails");

        thumbnails = new ThumbButton[noTiles];
        JPanel centrePan = new JPanel();
        centrePan.setLayout(new BoxLayout(centrePan,BoxLayout.LINE_AXIS));
        centrePan.setMinimumSize(new Dimension(squareSize,squareSize));
        centrePan.setPreferredSize(new Dimension((squareSize+3)*noTiles,squareSize));//Includes border
        centrePan.setBackground(Color.darkGray);

        for (int i=0;i<thumbnails.length;i++){
            thumbnails[i] = new ThumbButton(mainGUI,squareSize,(i+1));
            centrePan.add(thumbnails[i]);
            //if((i+1)<thumbnails.length) centrePan.add(Box.createRigidArea(new Dimension(2,0)));//gap between thumbnails
        }

        centrePan.validate();
        return centrePan;
    }

    void onResize(){
	//boardW = getParent().getWidth();
	//boardH = getParent().getHeight();
        System.out.println("resized");
	boardW = this.getWidth();
	boardH = this.getHeight();


        this.remove(0);
        this.add(buildThumbHolders(),BorderLayout.CENTER);
        this.validate();

	this.repaint();
	//this.setPreferredSize(new Dimension(boardW,boardH));
	//this.revalidate();
	//getParent().repaint();

        //change number of tiles if needed
    }
}
