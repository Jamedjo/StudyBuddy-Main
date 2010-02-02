import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;

class ThumbPanel extends JPanel {
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    int boardH_start = 100;
    int tileW = 5;
    int tileH = 1;
    int squareSize;
    GUI mainGUI;

    ThumbPanel(GUI parentGUI) {
	mainGUI = parentGUI;
	gridSize = new Dimension(boardW_start,boardH_start);
	boardW = boardW_start;
	boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);

	if (boardW/tileW<boardH/tileH){
	    squareSize = boardW/tileW;
	} else squareSize = boardH/tileH;
    }

    void onResize(){
	//boardW = getParent().getWidth();
	//boardH = getParent().getHeight();
	boardW = getWidth();
	boardH = getHeight();
	this.repaint();
	//this.setPreferredSize(new Dimension(boardW,boardH));
	//this.revalidate();
	//getParent().repaint();

	if (boardW/tileW<boardH/tileH){
	    squareSize = boardW/tileW;
	} else squareSize = boardH/tileH;
    }

    //all scaling in terms of height. max size is 20 times minimum. 

    public void paintComponent(java.awt.Graphics g) {
	if(mainGUI.state.isLocked) return;
	Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Use icons for thumbnails, populate icons in loop and then position icons.

	int leftOfset = (boardW - tileW*(squareSize+2)) /2;
	int topOfset = 0;
	//int currentThumb = mainGUI.state.currentI;
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	for(int im = 1; (im<=tileW)&&(im<=mainGUI.state.imageIDs.length);im++){
	    //set dimension
	    //currentThumb = mainGUI.state.next(currentThumb);
	    useWH = mainGUI.state.getRelImageWH(ImgSize.Thumb,squareSize,squareSize,im);
	    thumbOfsetW= (squareSize - useWH.width)/2;
	    thumbOfsetH= (squareSize - useWH.height)/2;
	    //mainGUI.mainPhoto.setIcon(mainGUI.state.imageList[currentThumb].getIcon(ImgSize.Thumb));
	    g2.drawImage(mainGUI.state.getBImageI(im,ImgSize.Thumb), leftOfset+thumbOfsetW, topOfset+thumbOfsetH,useWH.width,useWH.height, this);
	    leftOfset+=(squareSize + 2);
	}
    }
}
