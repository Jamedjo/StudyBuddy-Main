import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;

class MainPanel extends JPanel {
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    int boardH_start = 350;
    GUI mainGUI; //could be passed in contructor, it could be useful to know parent.

    MainPanel(GUI parentGUI) {
	mainGUI = parentGUI;
	gridSize = new Dimension(boardW_start,boardH_start);
	boardW = boardW_start;
	boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
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
    }

    //all scaling in terms of height. max size is 20 times minimum.

    public void paintComponent(java.awt.Graphics g) {
	if(mainGUI.state.isLocked) return;
	Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Set dimensions
	useWH = mainGUI.state.getRelImageWH(ImgSize.Screen,boardW,boardH,0);
	int leftOfset = (boardW - useWH.width) / 2;
	int topOfset = (boardH - useWH.height) / 2;

	//mainGUI.mainPhoto.setIcon(mainGUI.state.getCurrentImage().getIcon(ImgSize.Screen));

	g2.drawImage(mainGUI.state.getBImageI(0,ImgSize.Screen), leftOfset, topOfset,useWH.width,useWH.height, this);
    }
}
