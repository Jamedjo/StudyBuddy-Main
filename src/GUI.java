import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.imageio.*;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane.*;


//would be useful if database could store both portrait/landscape orientation, and up,down,left,right rotate orientation.
//on second thoughts, storing width and heigh gives you p/l and is needed for scaling anyway
//and u/d/l/r is in exif data.... and these can be stored in ImageObject type anyway.

class GUI implements ActionListener, ComponentListener{
    JFrame w;
    JMenuBar menuBar;
    JMenu imageMenu, viewMenu, helpMenu;
    JMenuItem mRestart, NextImage, PrevImage,ShowThumbs,HideThumbs, Options, Exit, About, Help;
    JButton bPrev, bNext, bImport, bThumbsS, bThumbsH, bSideBar,bZoom, bAddTag;
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JScrollPane boardScroll;
    ProgramState state;
    JOptionPane tagBox;


    public static void main(String[] args){
        GUI mainGUI = new GUI();
        mainGUI.play();
    }

    void play(){
        w = new JFrame();
        w.setTitle("Study Buddy 0.2alpha");
        w.setDefaultCloseOperation(w.EXIT_ON_CLOSE);
        buildMenu();
        w.setJMenuBar(menuBar);
        w.setLocationByPlatform(true);
        w.setIconImage(new ImageIcon("ball2gr.gif").getImage());
	//w.addComponentListener(this);
        //w.setResizable(false);
        quickRestart();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        //while (true) {Thread.sleep(30);}
    }

    void quickRestart(){
	state = new ProgramState(this);        

	mainPanel = new MainPanel(this);
	mainPanel.addComponentListener(this);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(false);

	toolbarMain = new JToolBar("StudyBuddy Toolbar");
	toolbarMain.setFocusable(false);

	//Create GUI elements here

	//tagBox
	//tagBox = new JOptionPane(this);
	//tagBox.setVisible(false);
	
	
			
			

	//boardScroll = new JScrollPane(mainPanel);
	//boardScroll.addComponentListener(this);

        Panel contentSet = new Panel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainPanel,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);

	Panel contentPane = new Panel();
    	contentPane.setLayout(new BorderLayout());

	buildToolbar();

        //contentPane.add(boardScroll, BorderLayout.CENTER);
	contentPane.add(contentSet, BorderLayout.CENTER);//contentPane.add(mainPanel);
	contentPane.add(toolbarMain, BorderLayout.PAGE_START);
        w.setContentPane(contentPane);
        w.pack();
    }

    void buildToolbar(){
        bPrev = new JButton("Prev");
        //button.setActionCommand(Prev);
        //button.setToolTipText(toolTipText);
        bPrev.addActionListener(this);

	bNext = new JButton("Next");
        bNext.addActionListener(this);

	bThumbsS = new JButton("Show Thumbnails");
        bThumbsS.addActionListener(this);

	bAddTag = new JButton("Add Tag");
        bAddTag.addActionListener(this);

	bThumbsH = new JButton("HIde Thumbnails");
        bThumbsH.addActionListener(this);
	bThumbsH.setVisible(false);

	//bZoom = new JButton("Zoom");
        //bZoom.addActionListener(this);

	toolbarMain.addSeparator(); 
	toolbarMain.add(bPrev);        
	toolbarMain.add(bNext); 
	toolbarMain.addSeparator();       
	toolbarMain.add(bThumbsS);        
	toolbarMain.add(bThumbsH); 
	toolbarMain.addSeparator();
	toolbarMain.add(bAddTag);        
	//toolbarMain.add(bZoom); 

	//workaround to prevent toolbar from steeling focus
	for( int i=0; i<toolbarMain.getComponentCount(); i++ ){
	    if( toolbarMain.getComponent(i) instanceof JButton ){
		((JButton)toolbarMain.getComponent(i)).setFocusable(false);
	    }
	}
    }

    void buildMenu(){
        menuBar = new JMenuBar();
        buildImageMenu();
	buildViewMenu();
        buildHelpMenu();
        menuBar.add(imageMenu);
	menuBar.add(viewMenu);
        menuBar.add(helpMenu);
    }
    
    void buildImageMenu(){
        imageMenu = new JMenu("Image");
        imageMenu.setMnemonic(KeyEvent.VK_I);

        mRestart = new JMenuItem("Restart Viewer",KeyEvent.VK_R);
        mRestart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        mRestart.addActionListener(this);

        Exit = new JMenuItem("Exit",KeyEvent.VK_X);
        Exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        Exit.addActionListener(this);

        imageMenu.add(mRestart);
        imageMenu.add(Exit);
    }
    void buildViewMenu(){
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        NextImage = new JMenuItem("Next Image",KeyEvent.VK_N);
	NextImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
        NextImage.addActionListener(this);
        //NextImage.setEnabled(false);

        PrevImage = new JMenuItem("Previous Image",KeyEvent.VK_P);
	PrevImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
        PrevImage.addActionListener(this);
        //PrevImage.setEnabled(false);

        ShowThumbs = new JMenuItem("Show Thumbnails Bar",KeyEvent.VK_T);
	ShowThumbs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK));
        ShowThumbs.addActionListener(this);
        //ShowThumbs.setEnabled(false);

        HideThumbs = new JMenuItem("Hide Thumbnails Bar",KeyEvent.VK_T);
	HideThumbs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK));
        HideThumbs.addActionListener(this);
	HideThumbs.setVisible(false);
        //HideThumbs.setEnabled(false);

        viewMenu.add(NextImage);
	viewMenu.add(PrevImage);
	viewMenu.addSeparator();
	viewMenu.add(ShowThumbs);
	viewMenu.add(HideThumbs);
    }
    
    void buildHelpMenu(){
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        About = new JMenuItem("About",KeyEvent.VK_A);
        About.addActionListener(this);

        Help = new JMenuItem("StudyBuddy Help!",KeyEvent.VK_H);
        Help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        Help.addActionListener(this);

        helpMenu.add(Help);
        helpMenu.add(About);        
    }

    public void actionPerformed(ActionEvent e){
          if(e.getSource()==mRestart) {
            quickRestart();
        }
        if(e.getSource()==NextImage || e.getSource()==bNext) {
            state.nextImage();
        }
        if(e.getSource()==PrevImage || e.getSource()==bPrev) {
            state.prevImage();
        }
        if(e.getSource()==ShowThumbs || e.getSource()==bThumbsS) {
	    thumbPanel.setVisible(true);
	    ShowThumbs.setVisible(false);
	    bThumbsS.setVisible(false);
	    HideThumbs.setVisible(true);
	    bThumbsH.setVisible(true);
        }

	if(e.getSource()==bAddTag){
	    String s = JOptionPane.showInputDialog(
			w, 
			"What Tag would you like to add?\n", 
			"Create Tag\n", 
			JOptionPane.PLAIN_MESSAGE);
	    
	}

        if(e.getSource()==HideThumbs || e.getSource()==bThumbsH) {
	    thumbPanel.setVisible(false);
	    ShowThumbs.setVisible(true);
	    bThumbsS.setVisible(true);
	    HideThumbs.setVisible(false);
	    bThumbsH.setVisible(false);
        }
        if(e.getSource()==Exit) {
            System.exit(0);
        }
        if(e.getSource()==Help) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w,"Visit http://www.studybuddy.com for help and tutorials","Study Help",JOptionPane.INFORMATION_MESSAGE);
        }
        if(e.getSource()==About) {
            JOptionPane.showMessageDialog(w,"StudyBuddy by Team StudyBuddy","About StudyBuddy",JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void componentResized(ComponentEvent e) {
	// if(e.getSource()==boardScroll) {
 if(e.getSource()==mainPanel) {
            mainPanel.onResize();
	    //thumbPanel.onResize();
        }
// 	if(e.getSource()==w){
// 	    int newWidth = w.getWidth();
// 	    int newHeight = w.getHeight();
// 	    if(newWidth<200) newWidth = 200;
// 	    if(newHeight<200) newHeight = 200;
// 	    w.setSize(newWidth,newHeight);
// 	}
    }

    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentShown(ComponentEvent e){}

}



enum Orientation {Landscape,Portrait}

class ImageObject {
    BufferedImage bImage;
    Orientation iOri;
    int width,height;

    ImageObject(String relativeURL){//,MainPanel parentPane) {
        try {
            URL urlAddress = GUI.class.getResource(relativeURL);
            bImage = ImageIO.read(urlAddress);
            //File fileAddress = new File(relativeURL);
            //img = ImageIO.read(fileAddress)
;
            width = bImage.getWidth(null);
            height = bImage.getHeight(null);
	    if(height<width) iOri = Orientation.Landscape;
	    else iOri = Orientation.Portrait;

            //img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        } catch (IOException e) {
	    System.err.println("Error Loading Image" + e.toString());
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}

//Should hold data relating to program state and control program state
//Should hold references to databses and image locations
class ProgramState{
    ImageDatabase mainImageDatabase;
    ImageObject[] imageList;
    String[] imageFiles;
    int lastIndex = 4;
    int currentI = 0;
    GUI mainGUI;

    ProgramState(GUI parentGUI){
	//Create image database by loading database
	mainImageDatabase = new ImageDatabase("mainDB");
	mainImageDatabase.addImage("Title 1","img_2810b_small.jpg");
	mainImageDatabase.addImage("Title 1","img_6088b_small.jpg");
	mainImageDatabase.addImage("Title 1","img_5672bp_small.jpg");
	mainImageDatabase.addImage("Title 1","img_2926_small.jpg");
	mainImageDatabase.addImage("Title 1","img_F028c_small.jpg");
	imageFiles = mainImageDatabase.getAllFilenames();
	imageList = new ImageObject[imageFiles.length];
	for(int i=0; i<imageFiles.length;i++){
	    imageList[i] = new ImageObject(imageFiles[i]);
	}
	mainGUI = parentGUI;
    }

    int next(int val){
	if(val==lastIndex) return 0;
	else return (val +1);
    }

    int prev(int val){
	if(val==0) return lastIndex;
	else return (val -1);
    }

    void nextImage() {
	currentI = next(currentI);
	mainGUI.mainPanel.repaint();
	mainGUI.thumbPanel.repaint();
    }
    void prevImage() {
	currentI = prev(currentI);
	mainGUI.mainPanel.repaint();
	mainGUI.thumbPanel.repaint();
    }

    ImageObject getCurrentImage(){
	return imageList[currentI];
    }

    //Finds maximum with and height somthing can be scaled to, without changing aspect ratio
    //Takes the dimensions of the object inW and inH
    //and the dimensions of the box it is to be fitted into maxW and maxH
    //Returns (Width,Height) as an array of two integers.
    //Not sure which class it belongs in.
    int[] scaleToMax(int inW, int inH, int maxW, int maxH) {
	float f_inW,f_inH,f_maxW,f_maxH;
	f_inW = inW;
	f_inH = inH;
	f_maxW = maxW;
	f_maxH = maxH;
	int[] outWH = new int[2];
	if ( (f_inW/f_inH)<(f_maxW/f_maxH) ) {
	    //narrower at same scale
	    outWH[1] = maxH;
	    outWH[0] = Math.round((f_maxH / f_inH)* f_inW);
	}
	else {
	    //wider at same scale
	    outWH[0] = maxW;
	    outWH[1] = Math.round(( f_maxW / f_inW)* f_inH);
	}
	return outWH;
    }
}

class MainPanel extends JPanel {
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    int boardH_start = 350;
    GUI mainGUI; //could be passed in contructor, it could be useful to know parent.
    ProgramState state;

    MainPanel(GUI parentGUI) {
	mainGUI = parentGUI;
	state = mainGUI.state;
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
	int[] useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Set dimensions
	useWH = state.scaleToMax(state.getCurrentImage().width,state.getCurrentImage().height, boardW, boardH);
	int leftOfset = (boardW - useWH[0]) / 2;
	int topOfset = (boardH - useWH[1]) / 2;
	g2.drawImage(state.getCurrentImage().bImage, leftOfset, topOfset,useWH[0],useWH[1], this);
    }
}

class ThumbPanel extends JPanel {
    Dimension gridSize;
    int boardW,boardH;
    int boardW_start = 550;
    int boardH_start = 100;
    int tileW = 5;
    int tileH = 1;
    int squareSize;
    GUI mainGUI; //could be passed in contructor, it could be useful to know parent.
    ProgramState state;

    ThumbPanel(GUI parentGUI) {
	mainGUI = parentGUI;
	state = mainGUI.state;
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
	int[] useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Use icons for thumbnails, populate icons in loop and then position icons.

	int leftOfset = (boardW - tileW*(squareSize+2)) /2;
	int topOfset = 0;
	int currentThumb = state.currentI;
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	for(int i = 0; i<tileW;i++){
	    //set dimension
	    useWH = state.scaleToMax(state.imageList[currentThumb].width,state.imageList[currentThumb].height, squareSize, squareSize);
	    thumbOfsetW= (squareSize - useWH[0])/2;
	    thumbOfsetH= (squareSize - useWH[1])/2;
	    g2.drawImage(state.imageList[currentThumb].bImage, leftOfset+thumbOfsetW, topOfset+thumbOfsetH,useWH[0],useWH[1], this);
	leftOfset+=(squareSize + 2);
	currentThumb = state.next(currentThumb);
	}
    }
}
