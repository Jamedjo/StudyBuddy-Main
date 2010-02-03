
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;

public class MainPanel extends JPanel implements Scrollable, MouseMotionListener {

    Dimension gridSize;
    int boardW, boardH;
    int boardW_start = 550;
    int boardH_start = 350;
    GUI mainGUI; //could be passed in contructor, it could be useful to know parent.
    boolean isZoomed = false;

    MainPanel(GUI parentGUI) {
        mainGUI = parentGUI;
        gridSize = new Dimension(boardW_start, boardH_start);
        boardW = boardW_start;
        boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
        setAutoscrolls(true); //enable synthetic drag events
        addMouseMotionListener(this); //handle mouse drags
    }

    void onResize() {
        //boardW = getParent().getWidth();
        //boardH = getParent().getHeight();
        if(!isZoomed) {
            this.setPreferredSize(new Dimension(this.getParent().getParent().getWidth() - 3,this.getParent().getParent().getHeight() - 3 ));
            //**//System.out.println("klj"+this.getParent().getWidth());
        } else {
            this.setPreferredSize(ImageObject.useMaxMax(mainGUI.state.getCurrentImage().getWidthAndMakeBig(),mainGUI.state.getCurrentImage().getHeightAndMakeBig(),this.getParent().getWidth(),this.getParent().getHeight()));
        }
        this.revalidate();
        getParent().validate();
        boardW = this.getWidth();
        boardH = this.getHeight();
        getParent().repaint();
        this.repaint();
    }

    //all scaling in terms of height. max size is 20 times minimum.
    public void paintComponent(java.awt.Graphics g) {
        if (mainGUI.state.isLocked) {
            return;
        }
        Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        ImgSize cSize;
        if(isZoomed) {
            cSize = ImgSize.Max;
            this.setPreferredSize(ImageObject.useMaxMax(mainGUI.state.getCurrentImage().getWidthAndMakeBig(),mainGUI.state.getCurrentImage().getHeightAndMakeBig(),this.getParent().getWidth(),this.getParent().getHeight()));
            useWH = mainGUI.state.getRelImageWH(cSize, mainGUI.state.getCurrentImage().getWidthAndMakeBig(),mainGUI.state.getCurrentImage().getHeightAndMakeBig(), 0);
        }
        else {
            cSize = ImgSize.Screen;
        useWH = mainGUI.state.getRelImageWH(cSize,boardW,boardH,0);
        }
        int leftOfset = (boardW - useWH.width) / 2;
        int topOfset = (boardH - useWH.height) / 2;

        //mainGUI.mainPhoto.setIcon(mainGUI.state.getCurrentImage().getIcon(ImgSize.Screen));

        g2.drawImage(mainGUI.state.getBImageI(0, cSize), leftOfset, topOfset, useWH.width, useWH.height, this);
    }


    public void mouseMoved(MouseEvent e) { }
    public void mouseDragged(MouseEvent e) {
        //The user is dragging us, so scroll!
        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
        scrollRectToVisible(r);
    }


    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction) {
        return 5;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction) {
	return 5;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
