
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

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
    //Thread dragThread;
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
        //dragThread = new Thread(new DragUpdate(this,dragPeriod));
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
            useWH = mainGUI.getState().getRelImageWH(ImgRequestSize.Max, boardW, boardH, 0);
        //}
        //Potentially inefficient as forces full size image to load
        //log.print(LogType.Debug,"old zoomMultiplier- " + getZoomMult());
        setZoomMult((double)((double) useWH.width) / ((double) mainGUI.getState().getImageWidthFromBig()));
        //log.print(LogType.Debug,"new zoomMultiplier- " + getZoomMult());
        //log.print(LogType.Debug,"boardW: "+boardW+" boardH: "+boardH+"\nuseWH.width: "+useWH.width+" useWH.height: "+useWH.height);
    }

    void onResize() {
        //boolean oldScr=mainGUI.mainScrollPane.getHorizontalScrollBar().isVisible();;
        if ( isZoomed() ) {
            this.setPreferredSize(ImageUtils.useMaxMax((int) (mainGUI.getState().getImageWidthFromBig() * getZoomMult()), (int) (mainGUI.getState().getImageHeightFromBig() * getZoomMult()), this.getParent().getWidth(), this.getParent().getHeight()));
            if((getCursorMode()==DragMode.Drag)||(getCursorMode()==DragMode.None)){
                setCursorMode(getCurrentDrag());
            }
            //updateCursor();
        } else {
            mainGUI.imageAreas.getParent().validate();
            getParent().validate();
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
        if (mainGUI.getState().isLocked) {
            return;
        }
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        ImgRequestSize cSize;
        if (isZoomed())  cSize = ImgRequestSize.Max;
        else cSize = ImgRequestSize.Max;
        BufferedImage img = mainGUI.getState().getBImageI(0, cSize);
        boolean loading=false;BufferedImage b=null;boolean firstStart=false;
        if(img==ErrorImages.loading){
            img=mainGUI.getState().getBImageI(0,ImgRequestSize.Thumb);
            b =  ErrorImages.getMainLoading();
            loading=true;
        } else ErrorImages.stopMainAnim();
        if(img==ErrorImages.noNotesFound){
            if(mainGUI.getState().currentFilter.equals("-1")){
                img=ErrorImages.splashScreen;
                firstStart=true;
            }
        }
        
        if (isZoomed()) {
            int w,h;
            if(loading){
                w=img.getWidth();
                h=img.getHeight();
            }else{
                w= mainGUI.getState().getImageWidthFromBig();
                h= mainGUI.getState().getImageHeightFromBig();
            }
            this.setPreferredSize(ImageUtils.useMaxMax((int) (w * getZoomMult()),(int) (h * getZoomMult()),this.getParent().getWidth(),this.getParent().getHeight()));
            useWH = new Dimension((int) (w * getZoomMult()),(int) (h * getZoomMult()));
        } else {
            if(loading||firstStart) useWH= ImageUtils.scaleToMax(img.getWidth(),img.getHeight(), boardW, boardH);
            else mainGUI.getState().getRelImageWH(cSize, boardW, boardH, 0);
        }
        setOffsets();
        AffineTransform originalAffine = g2.getTransform();
        g2.setTransform(mainGUI.getState().getCurrentImage().img.transform.getAffine(originalAffine,(leftOffset*2)+useWH.width,(topOffset*2)+useWH.height));//offset+(w/2)
        g2.drawImage(img, leftOffset, topOffset, useWH.width, useWH.height, this);
        g2.setTransform(originalAffine);
        if(loading) {
            int leftLoadOS = (isZoomed())? ((JViewport) this.getParent()).getViewPosition().x : 0 ;
            int topLoadOS = (isZoomed())? ((JViewport) this.getParent()).getViewPosition().y : 0 ;
            Dimension loadingWH = ImageUtils.scaleToMax(b.getWidth(), b.getHeight(), boardW, boardH);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
            g2.setComposite(ac);
            g2.drawImage(b, ((boardW - loadingWH.width) / 2)+leftLoadOS, ((boardH - loadingWH.height) / 2)+topLoadOS, loadingWH.width, loadingWH.height, this);
        }
        drawLinkBoxes(g2, mainGUI.settings.getSettingAsBool("showNotes",true), mainGUI.settings.getSettingAsBool("showLinks",true));
        //g2.dispose();?
    }

    final static float dashA[] = {16.0f};
    final static BasicStroke dashLine = new BasicStroke(3.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10.0f, dashA, 0.0f);

    // Retreive the boxes for notes and links and draw them on the image
    private void drawLinkBoxes(Graphics2D g2, boolean showNotes, boolean showImageLinks) {
        Rectangle[] linksRect;
        String CurrentImageID = mainGUI.getState().getCurrentImageID();
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
            setZoomMult(((double)useWH.width) / ((double)mainGUI.getState().getImageWidthFromBig()));
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
        nowX = e.getX();
        nowY = e.getY();
//        dragThread.start();
    }
    @Override public void mouseDragged(MouseEvent e) {
        nowX = e.getX();
        nowY = e.getY();
        Point p = ((JViewport) getParent()).getViewPosition();
        int cX = pressX - e.getX();
        int cY = pressY - e.getY();
        p.x += cX;
        p.y += cY;//TODO: ensure not translating out of range.
        p.x = Math.max(p.x, 0);
        p.y = Math.max(p.y, 0);
        p.x = Math.min(p.x,this.getPreferredSize().width-((JViewport) getParent()).getExtentSize().width);//width
        p.y = Math.min(p.y,this.getPreferredSize().height-((JViewport) getParent()).getExtentSize().height);//hight
        EventQueue.invokeLater(new DragUpdate(this,((JViewport) getParent()), p));
        repaint();
                    //onResize();
    }
    public void mouseReleased(MouseEvent e) {
//        dragThread.interrupt();
//        dragThread = new Thread(new DragUpdate(this, dragPeriod));
        mousePressed = false;
        updateCursor();
        DragMode mode = getCursorMode();
        if ((mode == DragMode.Note) || (mode == DragMode.Link)) {
            setOffsets();
            Rectangle rec = getBoxFromPress(e.getX(), e.getY(), true);
            if (mode == DragMode.Note) {
                mainGUI.mainImageDB.addImageNote(mainGUI.getState().getCurrentImageID(), "", rec.x, rec.y, rec.width, rec.height);
            } else if (mode == DragMode.Link) {
                ImageLinker.setSelectingImage(true);
                ImageLinker.setDummyLinkID(mainGUI.mainImageDB.linkImage(mainGUI.getState().getCurrentImageID(), mainGUI.getState().getCurrentImageID(), rec.x, rec.y, rec.width, rec.height));
                JOptionPane.showMessageDialog(mainGUI.w, "Navigate to the image to link to and repress the link button");
            }
            setCursorMode(getCurrentDrag());
            this.repaint();
        }
    }
    public void mouseClicked(MouseEvent e) {
        NotePanel PointNotes = new NotePanel(mainGUI, mainGUI.getState().getCurrentImageID(), e.getX(), e.getY(), leftOffset, topOffset, getZoomMult());
        String[] LinkedImageIDs = mainGUI.mainImageDB.getImageIDsFromImagePoint(mainGUI.getState().getCurrentImageID(), e.getX(), e.getY(), leftOffset, topOffset, getZoomMult());
        //String TempString = "";
        if (PointNotes.isEmpty() == false) {
            mainGUI.contentPane.remove(mainGUI.notePane);
            mainGUI.notePane = new JScrollPane(PointNotes);
            mainGUI.notePane.setVisible(true);
            mainGUI.contentPane.add(mainGUI.notePane, BorderLayout.LINE_END);
            mainGUI.contentPane.validate();
            mainGUI.mainPanel.onResize();
        } else {
            mainGUI.contentPane.remove(mainGUI.notePane);
            mainGUI.contentPane.validate();
            mainGUI.mainPanel.onResize();
        }
        if (LinkedImageIDs.length > 0) {
            //One hyperlink should only link to one image
            mainGUI.getState().goToImageByID(LinkedImageIDs[0]);
            //This could be improved by allowing hyperlinks to websites if we wanted.

            //TempString = TempString + LinkedImageIDs[0];
            //for (int i = 1; i < LinkedImageIDs.length; i++) {
                //TempString = TempString + "," + LinkedImageIDs[i];
            //}
            //JOptionPane.showMessageDialog(mainGUI.w, "Links to images " + TempString);
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
