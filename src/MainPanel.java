
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;

public class MainPanel extends JPanel implements Scrollable, MouseMotionListener {
//parent is JViewport parent of parent is JScrollPane so use getParent().getParent()
    Dimension gridSize;
    int boardW, boardH;
    int boardW_start = 550;
    int boardH_start = 350;
    GUI mainGUI; //could be passed in contructor, it could be useful to know parent.
    boolean isZoomed = false;
    double zoomMultiplier = 1;//1 is 100%, 0.5 is 50% 3 is 300% etc.

    MainPanel(GUI parentGUI) {
        mainGUI = parentGUI;
        gridSize = new Dimension(boardW_start, boardH_start);
        boardW = boardW_start;
        boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.red);//darkGray);
        setAutoscrolls(true); //enable synthetic drag events
        addMouseMotionListener(this); //handle mouse drags
    }

    void onResize() {
        System.out.println("Pw: "+this.getPreferredSize().width+"   Ph: "+this.getPreferredSize().height);
            System.out.println("oldBw: "+boardW+"   oldBh: "+boardW);
        //boardW = getParent().getWidth();
        //boardH = getParent().getHeight();
        if( isZoomed ) {
            this.setPreferredSize(ImageObject.useMaxMax((int)(mainGUI.state.getCurrentImage().getWidthAndMakeBig()*zoomMultiplier),(int)(mainGUI.state.getCurrentImage().getHeightAndMakeBig()*zoomMultiplier),this.getParent().getWidth(),this.getParent().getHeight()));
            //((JScrollPane)(this.getParent().getParent())).scrollRectToVisible(new Rectangle(500,500,501,501));
        } else {
            this.setPreferredSize(new Dimension(mainGUI.mainScrollPane.getWidth() - 3,mainGUI.mainScrollPane.getHeight() - 3 ));
        }
        this.revalidate();
        getParent().validate();
        boardW = this.getWidth();
        boardH = this.getHeight();
        getParent().repaint();
        this.repaint();
        System.out.println("Pw: "+this.getPreferredSize().width+"   Ph: "+this.getPreferredSize().height);
        System.out.println("Bw: "+boardW+"   Bh: "+boardW);
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
            this.setPreferredSize(ImageObject.useMaxMax((int)(mainGUI.state.getCurrentImage().getWidthAndMakeBig()*zoomMultiplier),(int)(mainGUI.state.getCurrentImage().getHeightAndMakeBig()*zoomMultiplier),this.getParent().getWidth(),this.getParent().getHeight()));
            useWH = mainGUI.state.getRelImageWH(cSize, (int)(mainGUI.state.getCurrentImage().getWidthAndMakeBig()*zoomMultiplier),(int)(mainGUI.state.getCurrentImage().getHeightAndMakeBig()*zoomMultiplier), 0);
        } else {
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
