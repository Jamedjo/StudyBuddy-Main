
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;

public class MainPanel extends JPanel{// implements Scrollable, MouseMotionListener {
//parent is JViewport parent of parent is JScrollPane so use getParent().getParent()
    Dimension gridSize;
    int boardW, boardH;
    int boardW_start = 550;
    int boardH_start = 350;
    final GUI mainGUI;
    boolean isZoomed = false;
    double zoomMultiplier = 1;//1 is 100%, 0.5 is 50% 3 is 300% etc.

    MainPanel(GUI parentGUI) {
        mainGUI = parentGUI;
        gridSize = new Dimension(boardW_start, boardH_start);
        boardW = boardW_start;
        boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.red);//darkGray);
//        setAutoscrolls(true); //enable synthetic drag events
//        addMouseMotionListener(this); //handle mouse drags
    }

    void onResize() {
        //System.out.println("old Pref w: "+this.getPreferredSize().width+"   old Pref h: "+this.getPreferredSize().height);
            //System.out.println("old Brd w: "+boardW+"   old Brd h: "+boardH);
        if( isZoomed ) {
            //use bordW&H instead
            this.setPreferredSize(ImageObject.useMaxMax((int)(mainGUI.state.getCurrentImage().getWidthAndMakeBig()*zoomMultiplier),(int)(mainGUI.state.getCurrentImage().getHeightAndMakeBig()*zoomMultiplier),this.getParent().getWidth(),this.getParent().getHeight()));
            //((JScrollPane)(this.getParent().getParent())).scrollRectToVisible(new Rectangle(500,500,501,501));
        } else {
            ((JViewport)getParent()).revalidate();
        getParent().getParent().validate();
            boardW = getParent().getWidth() - 3;
            boardH = getParent().getHeight() - 3;
            getParent().getParent().setPreferredSize(new Dimension(boardW,boardH ));
        }
        this.repaint();
        //boardW = this.getWidth();
        //boardH = this.getHeight();
        this.revalidate();
        getParent().validate();
        getParent().repaint();
        this.repaint();
        //System.out.println("Pref w: "+this.getPreferredSize().width+"   Pref h: "+this.getPreferredSize().height);
        //System.out.println("Brd w: "+boardW+"   Brd h: "+boardH);
    }

    //all scaling in terms of height. max size is 20 times minimum.
    @Override public void paintComponent(java.awt.Graphics g) {
        if (mainGUI.state.isLocked) {
            return;
        }
        Dimension useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        ImgSize cSize;
        int leftOfset;
        int topOfset;
        if(isZoomed) {
            cSize = ImgSize.Max;
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), this.getParent().getWidth(), this.getParent().getHeight()));
            useWH = mainGUI.state.getRelImageWH(cSize, (int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), 0);
            leftOfset = 0;//should improve on this so that image centres when zoom is small
            topOfset = 0;
        } else {
            cSize = ImgSize.Screen;
            useWH = mainGUI.state.getRelImageWH(cSize, boardW, boardH, 0);
            leftOfset = (boardW - useWH.width) / 2;
            topOfset = (boardH - useWH.height) / 2;
        }
        //boardW = this.getWidth();
        //boardH = this.getHeight();

        //mainGUI.mainPhoto.setIcon(mainGUI.state.getCurrentImage().getIcon(ImgSize.Screen));

        g2.drawImage(mainGUI.state.getBImageI(0, cSize), leftOfset, topOfset, useWH.width, useWH.height, this);
    }


//    @Override public void mouseMoved(MouseEvent e) { }
//    @Override public void mouseDragged(MouseEvent e) {
//        //The user is dragging us, so scroll!
//        Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
//        scrollRectToVisible(r);
//    }


//    @Override public Dimension getPreferredScrollableViewportSize() {
//        return this.getPreferredSize();
//    }
//
//    @Override public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction) {
//        return 5;
//    }
//
//    @Override public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction) {
//	return 5;
//    }
//
//    @Override public boolean getScrollableTracksViewportWidth() {
//        return false;
//    }
//
//    @Override public boolean getScrollableTracksViewportHeight() {
//        return false;
//    }
}
