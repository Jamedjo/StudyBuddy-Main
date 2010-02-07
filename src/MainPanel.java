
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;

public class MainPanel extends JPanel implements MouseWheelListener {//,Scrollable, MouseMotionListener {
//parent is JViewport parent of parent is JScrollPane so use getParent().getParent()

    Dimension gridSize;
    int boardW, boardH;
    Dimension useWH;
    final int boardW_start = 550;
    final int boardH_start = 350;
    final double wheelZoomIncrement = 0.2;//affect zoom by 20 percent on wheel rotate
    final double minimumZoomLevel = 0.025; //minium zoom so image does not dissapear of negative zoom
    final GUI mainGUI;
    boolean isZoomed = false;
    double zoomMultiplier = 1;//1 is 100%, 0.5 is 50% 3 is 300% etc.

    MainPanel(GUI parentGUI) {
        mainGUI = parentGUI;
        gridSize = new Dimension(boardW_start, boardH_start);
        boardW = boardW_start;
        boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
        this.addMouseWheelListener(this);
//        setAutoscrolls(true); //enable synthetic drag events
//        addMouseMotionListener(this); //handle mouse drags
    }

    void onResize() {
        if ( isZoomed ) {
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), this.getParent().getWidth(), this.getParent().getHeight()));
        } else {
            mainGUI.imageAreas.validate();
            boardW = mainGUI.mainScrollPane.getWidth() - 3;
            boardH = mainGUI.mainScrollPane.getHeight() - 3;
            this.setPreferredSize(new Dimension(boardW, boardH));
        }
        getParent().validate();
        this.revalidate();
        //getParent().validate();
        this.repaint();
    }

    //all scaling in terms of height. max size is 20 times minimum.
    @Override public void paintComponent(java.awt.Graphics g) {
        if (mainGUI.state.isLocked) {
            return;
        }
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        ImgSize cSize;
        int leftOfset;
        int topOfset;
        if (isZoomed) {
            cSize = ImgSize.Max;
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), this.getParent().getWidth(), this.getParent().getHeight()));
            useWH = mainGUI.state.getRelImageWH(cSize, (int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), 0);
            leftOfset = (this.getPreferredSize().width - useWH.width) / 2;
            topOfset = (this.getPreferredSize().height - useWH.height) / 2;
        } else {
            cSize = ImgSize.Screen;
            useWH = mainGUI.state.getRelImageWH(cSize, boardW, boardH, 0);
            leftOfset = (boardW - useWH.width) / 2;
            topOfset = (boardH - useWH.height) / 2;
        }

        g2.drawImage(mainGUI.state.getBImageI(0, cSize), leftOfset, topOfset, useWH.width, useWH.height, this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!isZoomed) {
            //get multiplier from fit so zoom doesnt jump
            zoomMultiplier = ((double)useWH.width) / ((double)mainGUI.state.getCurrentImage().getWidthAndMakeBig());
        }
        //System.out.println(e.toString());
        int xpos = e.getPoint().x;
        int ypos = e.getPoint().y;
        isZoomed = true;
        zoomMultiplier -= ((double) e.getWheelRotation()) * wheelZoomIncrement;
        if (zoomMultiplier < minimumZoomLevel) {
            zoomMultiplier = minimumZoomLevel;
        }
        mainGUI.toggleZoomed(false);
        this.scrollRectToVisible(new Rectangle(xpos,ypos,xpos+1,ypos+1));//improve this to improve zoom.
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
