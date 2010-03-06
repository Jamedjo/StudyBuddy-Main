
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.awt.Rectangle;

public class MainPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {//,Scrollable {
//parent is JViewport parent of parent is JScrollPane so use getParent().getParent()

    Dimension gridSize;
    int boardW, boardH;
    Dimension useWH;
    final int boardW_start = 550;
    final int boardH_start = 350;
    int leftOfset;
    int topOfset;
    final double wheelZoomIncrement = 0.2;//affect zoom by 20 percent on wheel rotate
    final double minimumZoomLevel = 0.025; //minium zoom so image does not dissapear of negative zoom
    final GUI mainGUI;
    private boolean bIsZoomed = false;
    private double zoomMultiplier = 1;//1 is 100%, 0.5 is 50% 3 is 300% etc.
    int pressX,pressY,nowX,nowY;
    //Cursor plainCursor = Cursor.getDefaultCursor();
    Thread dragThread;
    final int dragPeriod = 40;// 25fps is equivalent to every 40ms
    private DragMode dragMode = DragMode.None;//Change if zoomed in by default
    boolean mousePressed = false;

    MainPanel(GUI parentGUI) {
        mainGUI = parentGUI;
        gridSize = new Dimension(boardW_start, boardH_start);
        boardW = boardW_start;
        boardH = boardH_start;
        setPreferredSize(gridSize);
        this.setBackground(Color.darkGray);
        this.addMouseWheelListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        dragThread = new Thread(new DragUpdate(this,dragPeriod));
    }
    DragMode getCursorMode(){
        return dragMode;
    }
    void setCursorMode(DragMode mode){
        dragMode = mode;
        updateCursor();
    }
    void updateCursor(){
        if(mousePressed){
            setCursor(getCursorMode().closed);
        } else{
            setCursor(getCursorMode().open);
        }
    }
    
    //A bit counterintuitive due to bad naming...
    //This returns NONE or DRAG depending on zoom
    //It refers to DragMode.Drag not current DragMode
    //Checks zoom mode and if image is smaller than panel
    DragMode getCurrentDrag(){
        if(isZoomed()){
            //System.out.printf("prefWidth:%d prefHeight:%d useW:%d useH:%d\n",this.getPreferredSize().width,this.getPreferredSize().height,useWH.width,useWH.height);
            //add 3 to deal with scroll bars?
            if(this.getPreferredSize().width>getParent().getParent().getWidth()||this.getPreferredSize().height>getParent().getParent().getHeight()){
            return DragMode.Drag;
            }
        }
        return DragMode.None;
    }

    boolean isZoomed(){
        return bIsZoomed;
    }
    double getZoomMult(){
        return zoomMultiplier;
    }
    void setZoomMult(double newZoomMultipler){
        zoomMultiplier = newZoomMultipler;
        if (getZoomMult() < minimumZoomLevel) {
            zoomMultiplier = minimumZoomLevel;
        }
    }
    boolean isZoomFit(){
        return (!bIsZoomed);
    }
    void setZoomed(boolean isZoomed){
        bIsZoomed = isZoomed;
        if(isZoomFit()){
            //fixFitZoomMultiplier();
            onResize();
            if(getCursorMode()==DragMode.Drag) setCursorMode(DragMode.None);
        } else{
            if(getCursorMode()==DragMode.None) setCursorMode(DragMode.Drag);
        }
    }
    void fixFitZoomMultiplier(){
        //if(useWH==null){
            useWH = mainGUI.state.getRelImageWH(ImgSize.Screen, boardW, boardH, 0);
        //}
        //Potentially inefficient as forces full size image to load
        System.out.println("old zoomMultiplier- " + getZoomMult());
        setZoomMult((double)((double) useWH.width) / ((double) mainGUI.state.getCurrentImage().getWidthAndMakeBig()));
        System.out.println("new zoomMultiplier- " + getZoomMult());
        System.out.println("boardW: "+boardW+" boardH: "+boardH+"\nuseWH.width: "+useWH.width+" useWH.height: "+useWH.height);
    }

    void onResize() {
        //boolean oldScr=mainGUI.mainScrollPane.getHorizontalScrollBar().isVisible();;
        if ( isZoomed() ) {
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * getZoomMult()), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * getZoomMult()), this.getParent().getWidth(), this.getParent().getHeight()));
            if((getCursorMode()==DragMode.Drag)||(getCursorMode()==DragMode.None)){
                setCursorMode(getCurrentDrag());
            }
            //updateCursor();
        } else {
            mainGUI.imageAreas.getParent().validate();
            //mainGUI.imageAreas.validate();
            boardW = mainGUI.mainScrollPane.getWidth() - 3;
            boardH = mainGUI.mainScrollPane.getHeight() - 3;
            this.setPreferredSize(new Dimension(boardW, boardH));
            this.setCursor(Cursor.getDefaultCursor());
        }
        getParent().validate();
        this.revalidate();
        //getParent().validate();
        repaint();
        if(isZoomFit()){
        fixFitZoomMultiplier();
        }
        //if (oldScr!=mainGUI.mainScrollPane.getHorizontalScrollBar().isVisible()) System.out.println("Horizontal Scroll bar toggled");
    }

    void setOfsets() {
        if (isZoomed()) {
            leftOfset = (this.getPreferredSize().width - useWH.width) / 2;
            topOfset = (this.getPreferredSize().height - useWH.height) / 2;
        } else {
            leftOfset = (boardW - useWH.width) / 2;
            topOfset = (boardH - useWH.height) / 2;
        }
    }

    //all scaling in terms of height. max size is 20 times minimum.
    @Override public void paintComponent(java.awt.Graphics g) {
        if (mainGUI.state.isLocked) {
            return;
        }
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        ImgSize cSize;
        if (isZoomed()) {
            cSize = ImgSize.Max;
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * getZoomMult()), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * getZoomMult()), this.getParent().getWidth(), this.getParent().getHeight()));
            useWH = new Dimension((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * getZoomMult()), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * getZoomMult()));
        } else {
            cSize = ImgSize.Screen;
            useWH = mainGUI.state.getRelImageWH(cSize, boardW, boardH, 0);
        }
        setOfsets();
//        Image offScreenImage = createImage(useWH.width,useWH.height);
//        Graphics2D gOffScr = (Graphics2D) offScreenImage.getGraphics();
//        gOffScr.setColor(Color.red);
//        gOffScr.fillRect(0,0,useWH.width,useWH.height);
//        int drawX,drawY,drawW,drawH;
//        drawX=((JViewport)this.getParent()).getViewRect().x;
//        drawY=((JViewport)this.getParent()).getViewRect().y;
//        drawW=((JViewport)this.getParent()).getViewRect().width;
//        drawH=((JViewport)this.getParent()).getViewRect().height;
//        gOffScr.drawImage(mainGUI.state.getBImageI(0, cSize).getSubimage(drawX,drawY,drawW,drawH), drawX, drawY, drawW, drawH, this);
//        gOffScr.dispose();
//        g2.drawImage(offScreenImage,leftOfset,topOfset, this);
        g2.drawImage(mainGUI.state.getBImageI(0, cSize), leftOfset, topOfset, useWH.width, useWH.height, this);
        DrawLinkBoxes(g2, mainGUI, leftOfset, topOfset, getZoomMult(), true, true);
    }
	
    // Retreive the boxes for notes and links and draw them on the image
    private void DrawLinkBoxes(Graphics2D DrawWhere, GUI TheGUI, int XOffset, int YOffset, double Scale, boolean Notes, boolean ImageLinks) {
        Rectangle[] Links;
        String CurrentImageID = TheGUI.state.getCurrentImageID();
        System.out.println("zoomMultiplier- " + getZoomMult());
        if (CurrentImageID != null) {
            if (Notes == true) {
                Links = TheGUI.mainImageDB.getNoteRectanglesFromImageID(CurrentImageID, XOffset, YOffset, Scale);
                if (Links != null) {
                    System.out.println("Links[0]- " + Links[0]);
                    for (int i = 0; i < Links.length; i++) {
                        DrawWhere.draw(Links[i]);
                    }
                }
            }
            if (ImageLinks == true) {
                Links = TheGUI.mainImageDB.getLinkRectanglesFromImageID(CurrentImageID, XOffset, YOffset, Scale);
                if (Links != null) {
                    for (int i = 0; i < Links.length; i++) {
                        DrawWhere.draw(Links[i]);
                    }
                }
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (isZoomFit()) {
            //get multiplier from fit so zoom doesnt jump
            setZoomMult(((double)useWH.width) / ((double)mainGUI.state.getCurrentImage().getWidthAndMakeBig()));
        }
        //System.out.println(e.toString());
        int xpos = e.getPoint().x;
        int ypos = e.getPoint().y;
        setZoomed(true);
        setZoomMult(getZoomMult()-((double) e.getWheelRotation()) * wheelZoomIncrement);
        mainGUI.toggleZoomed(false);
        System.out.println(xpos+","+ypos);
        //this.scrollRectToVisible(new Rectangle(xpos,ypos,this.getParent().getWidth(),this.getParent().getHeight()));//improve this to improve zoom.
        ((JViewport)this.getParent()).setViewPosition(new Point(xpos,ypos));
    }
    @Override public void mouseMoved(MouseEvent e) { }

    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        updateCursor();
        pressX = e.getX();
        pressY = e.getY();
        nowX = pressX;
        nowY = pressY;
        //lastDrag = Calendar.getInstance().getTimeInMillis();
        dragThread.start();
    }
    @Override public void mouseDragged(MouseEvent e) {
        if (getCursorMode() == DragMode.Drag) {
            nowX = e.getX();
            nowY = e.getY();
        }
        getParent().repaint();
    }
    public void mouseReleased(MouseEvent e){
        dragThread.interrupt();
        dragThread = new Thread(new DragUpdate(this, dragPeriod));
        int leftOfset = 0;
        int topOfset = 0;
        mousePressed = false;
        updateCursor();
        if (getCursorMode() == DragMode.Note) {
            setOfsets();
            mainGUI.mainImageDB.addImageNote(mainGUI.state.getCurrentImageID(), "", (int) ((nowX / getZoomMult()) - leftOfset), (int) ((nowY / getZoomMult()) - topOfset), (int) ((e.getX() - nowX) / getZoomMult()), (int) ((e.getY() - nowY) / getZoomMult()));
            setCursorMode(DragMode.Drag);
            this.repaint();
        }else if (getCursorMode() == DragMode.Link) {
            setOfsets();
            mainGUI.mainImageDB.linkImage(mainGUI.state.getCurrentImageID(), "", nowX - leftOfset, nowY - topOfset, e.getX() - nowX, e.getY() - nowY);
            setCursorMode(DragMode.Drag);
            this.repaint();
        }
    }
    public void mouseClicked(MouseEvent e){ }
    public void mouseEntered(MouseEvent e){ }
    public void mouseExited(MouseEvent e){ }
}
