
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.awt.Rectangle;

public class MainPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {//,Scrollable {
//parent is JViewport parent of parent is JScrollPane so use getParent().getParent()

    Log log = new Log();
    Dimension gridSize;
    int boardW, boardH;
    Dimension useWH;
    final int boardW_start = 550;
    final int boardH_start = 350;
    int leftOffset;
    int topOffset;
    final double wheelZoomIncrement = 0.2;//affect zoom by 20 percent on wheel rotate
    final double minimumZoomLevel = 0.025; //minium zoom so image does not dissapear of negative zoom
    final GUI mainGUI;
    private boolean bIsZoomed = false;
    private double zoomMultiplier = 1;//1 is 100%, 0.5 is 50% 3 is 300% etc.
    int pressX,pressY,nowX,nowY;
    Thread dragThread;
    final int dragPeriod = 40;// 25fps is equivalent to every 40ms
    private DragMode dragMode;//Change if zoomed in by default
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
        mainGUI.mainImageDB.linkImage(mainGUI.state.getRelativeImageID(1),mainGUI.state.getCurrentImageID(), 300, 200, 100, 100);
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
        //log.print(LogType.Debug,"old zoomMultiplier- " + getZoomMult());
        setZoomMult((double)((double) useWH.width) / ((double) mainGUI.state.getImageWidthFromBig()));
        //log.print(LogType.Debug,"new zoomMultiplier- " + getZoomMult());
        //log.print(LogType.Debug,"boardW: "+boardW+" boardH: "+boardH+"\nuseWH.width: "+useWH.width+" useWH.height: "+useWH.height);
    }

    void onResize() {
        //boolean oldScr=mainGUI.mainScrollPane.getHorizontalScrollBar().isVisible();;
        if ( isZoomed() ) {
            this.setPreferredSize(ImageObjectUtils.useMaxMax((int) (mainGUI.state.getImageWidthFromBig() * getZoomMult()), (int) (mainGUI.state.getImageWidthFromBig() * getZoomMult()), this.getParent().getWidth(), this.getParent().getHeight()));
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
        //if (oldScr!=mainGUI.mainScrollPane.getHorizontalScrollBar().isVisible()) log.print(LogType.Debug,"Horizontal Scroll bar toggled");
    }

    void setOffsets() {
        if (isZoomed()) {
            leftOffset = (this.getPreferredSize().width - useWH.width) / 2;
            topOffset = (this.getPreferredSize().height - useWH.height) / 2;
        } else {
            leftOffset = (boardW - useWH.width) / 2;
            topOffset = (boardH - useWH.height) / 2;// plus 2 due to weird borders
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
            this.setPreferredSize(ImageObjectUtils.useMaxMax((int) (mainGUI.state.getImageWidthFromBig() * getZoomMult()), (int) (mainGUI.state.getImageHeightFromBig() * getZoomMult()), this.getParent().getWidth(), this.getParent().getHeight()));
            useWH = new Dimension((int) (mainGUI.state.getImageWidthFromBig() * getZoomMult()), (int) (mainGUI.state.getImageHeightFromBig() * getZoomMult()));
        } else {
            cSize = ImgSize.Screen;
            useWH = mainGUI.state.getRelImageWH(cSize, boardW, boardH, 0);
        }
        setOffsets();
        g2.drawImage(mainGUI.state.getBImageI(0, cSize), leftOffset, topOffset, useWH.width, useWH.height, this);
        drawLinkBoxes(g2, mainGUI.settings.getSettingAsBool("showNotes",true), mainGUI.settings.getSettingAsBool("showLinks",true));
    }

    final static float dashA[] = {16.0f};
    final static BasicStroke dashLine = new BasicStroke(3.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10.0f, dashA, 0.0f);

    // Retreive the boxes for notes and links and draw them on the image
    private void drawLinkBoxes(Graphics2D g2, boolean showNotes, boolean showImageLinks) {
        Rectangle[] linksRect;
        String CurrentImageID = mainGUI.state.getCurrentImageID();
        g2.setStroke(dashLine);
        //log.print(LogType.Debug,"zoomMultiplier- " + getZoomMult());
        if (CurrentImageID != null) {
            if (showNotes) {
                g2.setColor(Color.red);
                linksRect = mainGUI.mainImageDB.getNoteRectanglesFromImageID(CurrentImageID, leftOffset, topOffset, getZoomMult());
                if (linksRect != null) {
                    for (int i = 0; i < linksRect.length; i++) {
                        //log.print(LogType.Debug,"Notes[i]- " + linksRect[i]);
                        g2.draw(linksRect[i]);
                    }
                }
                linksRect = null;
            }
            if (showImageLinks) {
                g2.setColor(Color.blue);
                linksRect = mainGUI.mainImageDB.getLinkRectanglesFromImageID(CurrentImageID, leftOffset, topOffset, getZoomMult());
                if (linksRect != null) {
                    for (int j = 0; j < linksRect.length; j++) {
                        //log.print(LogType.Debug,"Links[j]- " + linksRect[j]);
                        g2.draw(linksRect[j]);
                    }
                }
                linksRect = null;
            }
            //If dragging rectagle in relevent mode, draw it.
            if(mousePressed&&(getCursorMode()==DragMode.Link||getCursorMode()==DragMode.Note)){
                if(getCursorMode()==DragMode.Link) g2.setColor(Color.blue);
                else g2.setColor(Color.red);
                g2.draw(getBoxFromPress(nowX,nowY,false));
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        double oldZoom = getZoomMult();
        if (isZoomFit()) {
            //get multiplier from fit so zoom doesnt jump
            setZoomMult(((double)useWH.width) / ((double)mainGUI.state.getImageWidthFromBig()));
        }
        //Does not look at offsets as these imply image is not enlarged in that dimension.
        //If the image is zoomed out and has offsets then no need to change JViewport position
        int xpos = e.getPoint().x;
        int ypos = e.getPoint().y;
        setZoomed(true);
        setZoomMult(getZoomMult()*(1-(((double) e.getWheelRotation()) * wheelZoomIncrement)));
        mainGUI.toggleZoomed(false);
        double zoomFactor = getZoomMult()/oldZoom;
        int newX = (int)(xpos*zoomFactor);
        int newY = (int)(ypos*zoomFactor);
        Rectangle r = ((JViewport) this.getParent()).getViewRect();
        r.translate(newX-xpos,newY-ypos);
        this.scrollRectToVisible(r);
    }
    @Override public void mouseMoved(MouseEvent e) { }

    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        updateCursor();
        pressX = e.getX();
        pressY = e.getY();
        nowX = pressX;
        nowY = pressY;
        dragThread.start();
    }
    @Override public void mouseDragged(MouseEvent e) {
        nowX = e.getX();
        nowY = e.getY();
        getParent().repaint();
        repaint();
    }
    public void mouseReleased(MouseEvent e){
        dragThread.interrupt();
        dragThread = new Thread(new DragUpdate(this, dragPeriod));
        mousePressed = false;
        updateCursor();
        DragMode mode = getCursorMode();
        if ((mode == DragMode.Note) || (mode == DragMode.Link)) {
            setOffsets();
            Rectangle rec = getBoxFromPress(e.getX(),e.getY(),true);
            if (mode == DragMode.Note) {
                mainGUI.mainImageDB.addImageNote(mainGUI.state.getCurrentImageID(), "", rec.x, rec.y ,rec.width ,rec.height);
            } else if (mode == DragMode.Link) {
				mainGUI.state.setSelectingImage(true);
                mainGUI.state.setDummyLinkID(mainGUI.mainImageDB.linkImage(mainGUI.state.getCurrentImageID(), mainGUI.state.getCurrentImageID(), rec.x, rec.y, rec.width,rec.height));
				JOptionPane.showMessageDialog(mainGUI.w, "Navigate to the image to link to and repress the link button");
            }
            setCursorMode(getCurrentDrag());
            this.repaint();
        }
    }
    public void mouseClicked(MouseEvent e)
	{
		NotePanel PointNotes = new NotePanel(mainGUI, mainGUI.state.getCurrentImageID(), e.getX(), e.getY(), leftOffset, topOffset, getZoomMult());
		String[] LinkedImageIDs = mainGUI.mainImageDB.getImageIDsFromImagePoint(mainGUI.state.getCurrentImageID(), e.getX(), e.getY(), leftOffset, topOffset, getZoomMult());
		String TempString = "";
		if (PointNotes.isEmpty() == false)
		{
			mainGUI.contentPane.remove(mainGUI.notePane);
			mainGUI.notePane = new JScrollPane(PointNotes);
			mainGUI.notePane.setVisible(true);
			mainGUI.contentPane.add(mainGUI.notePane, BorderLayout.LINE_END);
			mainGUI.contentPane.validate();
			mainGUI.mainPanel.onResize();
		}
		else
		{
			mainGUI.contentPane.remove(mainGUI.notePane);
			mainGUI.contentPane.validate();
			mainGUI.mainPanel.onResize();
		}
		if (LinkedImageIDs.length > 0)
		{
			TempString = TempString + LinkedImageIDs[0]; 
			for (int i=1; i< LinkedImageIDs.length; i++)
			{
				TempString = TempString + "," + LinkedImageIDs[i];
			}
			JOptionPane.showMessageDialog(mainGUI.w, "Links to images " + TempString);
		}
	}
    public void mouseEntered(MouseEvent e){ }
    public void mouseExited(MouseEvent e){ }

    Rectangle getBoxFromPress(int currentMouseX, int currentMouseY,boolean useScale) {
        double scale = 1;
        int xTranslate = 0;
        int yTranslate = 0;

        if (useScale){
            scale = getZoomMult();
            xTranslate = leftOffset;
            yTranslate = topOffset;
        }
        // x and y values scaled apropriately, and tranlated if image is centred on mainPanel.
        double scaledXstart, scaledYstart, scaledXstop, scaledYstop;
        scaledXstart = (pressX - xTranslate) / scale;
        scaledYstart = (pressY - yTranslate) / scale;
        scaledXstop = (currentMouseX - xTranslate) / scale;
        scaledYstop = (currentMouseY - yTranslate) / scale;

        int boxWidth, boxHeight, boxXleft, boxYtop;
        boxWidth = (int) Math.abs(scaledXstart - scaledXstop);
        boxHeight = (int) Math.abs(scaledYstart - scaledYstop);
        boxXleft = (int) Math.min(scaledXstart, scaledXstop);
        boxYtop = (int) Math.min(scaledYstart, scaledYstop);

        return new Rectangle(boxXleft, boxYtop, boxWidth, boxHeight);
    }
}
