
import java.awt.*;
import javax.swing.*;
import javax.swing.JOptionPane.*;
import java.awt.event.*;
import java.awt.Cursor;
import java.util.Calendar;
import java.awt.Rectangle;

public class MainPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {//,Scrollable {
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
    private int lastX,lastY;
    Cursor openHand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    Cursor closedHand = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    Cursor plainCursor = Cursor.getDefaultCursor();
    int tranX =0;
    int tranY =0;
    long lastDrag;

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
    }

    void onResize() {
        if ( isZoomed ) {
            this.setPreferredSize(ImageObject.useMaxMax((int) (mainGUI.state.getCurrentImage().getWidthAndMakeBig() * zoomMultiplier), (int) (mainGUI.state.getCurrentImage().getHeightAndMakeBig() * zoomMultiplier), this.getParent().getWidth(), this.getParent().getHeight()));
            if(this.getPreferredSize().width>getParent().getParent().getWidth()||this.getPreferredSize().height>getParent().getParent().getHeight()) {
                this.setCursor(openHand);
            } else {
                this.setCursor(plainCursor);
            }
        } else {
            mainGUI.imageAreas.validate();
            boardW = mainGUI.mainScrollPane.getWidth() - 3;
            boardH = mainGUI.mainScrollPane.getHeight() - 3;
            this.setPreferredSize(new Dimension(boardW, boardH));
            this.setCursor(Cursor.getDefaultCursor());
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
		DrawLinkBoxes(g2, mainGUI, leftOfset, topOfset, zoomMultiplier, true, true);
    }
	
	// Retreive the boxes for notes and links and draw them on the image
	private void DrawLinkBoxes(Graphics2D DrawWhere, GUI TheGUI, int XOffset, int YOffset, double Scale, boolean Notes, boolean ImageLinks)
	{
		Rectangle[] Links;
		String CurrentImageID = TheGUI.state.getCurrentImageID();
		System.out.println(zoomMultiplier);
		if (CurrentImageID != null)
		{
			if (Notes == true)
			{
				Links = TheGUI.mainImageDB.getNoteRectanglesFromImageID(CurrentImageID, XOffset, YOffset, Scale);
				if (Links != null)
				{
					System.out.println(Links[0]);
					for(int i=0; i<Links.length; i++)
						DrawWhere.draw(Links[i]);
				}
			}
			if (ImageLinks == true)
			{
				Links = TheGUI.mainImageDB.getLinkRectanglesFromImageID(CurrentImageID, XOffset, YOffset, Scale);
				if (Links != null)
					for(int i=0; i<Links.length; i++)
						DrawWhere.draw(Links[i]);
			}
		}
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
        System.out.println(xpos+","+ypos);
        //this.scrollRectToVisible(new Rectangle(xpos,ypos,this.getParent().getWidth(),this.getParent().getHeight()));//improve this to improve zoom.
        ((JViewport)this.getParent()).setViewPosition(new Point(xpos,ypos));
    }
    @Override public void mouseMoved(MouseEvent e) { }
    @Override public void mouseDragged(MouseEvent e) {
        if (mainGUI.state.noteRect == false && mainGUI.state.linkRect == false)
		{
			//The user is dragging us, so scroll!
			Rectangle r = ((JViewport)this.getParent()).getViewRect();
			tranX +=lastX-e.getX();
			tranY +=lastY-e.getY();
	//        if((Calendar.getInstance().getTimeInMillis()-lastDrag)>=40){
			//System.out.println("translate:"+tranX+"x"+tranY);
				r.translate(tranX,tranY);
				tranX =0;
				tranY=0;
				lastDrag = Calendar.getInstance().getTimeInMillis();
	//        } else System.out.println("dropped drag" +tranX);
			scrollRectToVisible(r);
			lastX = e.getX();
			lastY = e.getY();
		}
    }
    public void mousePressed(MouseEvent e){
        if (this.getCursor().equals(openHand)){
            this.setCursor(closedHand);
        }
        lastX = e.getX();
        lastY = e.getY();
        tranX = 0;
        tranY = 0;
        lastDrag = Calendar.getInstance().getTimeInMillis();
    }
    public void mouseReleased(MouseEvent e){
		int leftOfset=0;
		int topOfset=0;
        if (this.getCursor().equals(closedHand)){
            this.setCursor(openHand);
        }
		if (mainGUI.state.noteRect == true || mainGUI.state.linkRect == true)
		{
			if (isZoomed)
			{
				leftOfset = (this.getPreferredSize().width - useWH.width) / 2;
				topOfset = (this.getPreferredSize().height - useWH.height) / 2;
			}
			else 
			{
				leftOfset = (boardW - useWH.width) / 2;
				topOfset = (boardH - useWH.height) / 2;
			}
		}
		if (mainGUI.state.noteRect == true)
		{
			
			mainGUI.mainImageDB.addImageNote(mainGUI.state.getCurrentImageID(), "", (int) ((lastX/zoomMultiplier)-leftOfset), (int) ((lastY/zoomMultiplier)-topOfset), (int) ((e.getX()-lastX)/zoomMultiplier), (int) ((e.getY()-lastY)/zoomMultiplier));
			mainGUI.state.noteRect = false;
			this.repaint();
		}
		if (mainGUI.state.linkRect == true)
		{
			mainGUI.mainImageDB.linkImage(mainGUI.state.getCurrentImageID(), "", lastX-leftOfset, lastY-topOfset, e.getX()-lastX, e.getY()-lastY);
			mainGUI.state.linkRect = false;
			this.repaint();
		}
    }
    public void mouseClicked(MouseEvent e){ }
    public void mouseEntered(MouseEvent e){ }
    public void mouseExited(MouseEvent e){ }

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
