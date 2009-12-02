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


//Program stucture needs to be redesigned to implement SwingWorker threads to load images
//Each image will be published once loaded and the worker will be done when all are loaded
//This prevents loading large images from freezing/crashing the program,
//allows the GUI to load quicker at startup instead of waiting for all images to load
//and allows for the worker to be cancelled if it is too slow.

//Should be seperated into intial thread, and an event dispatch thread which implements the listeners.
class GUI implements ActionListener, ComponentListener{
    JFrame w;
    JMenuBar menuBar;
    JMenu imageMenu, viewMenu, tagMenu, helpMenu;
    JMenuItem mRestart, mImport, NextImage, PrevImage,ShowThumbs,HideThumbs, AddTag, TagThis, TagFilter, Options, Exit, About, Help;
    JButton bSideBar;
    final JFileChooser fileGetter = new JFileChooser();
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JScrollPane boardScroll;
    ProgramState state;
    JOptionPane tagBox;
    ImageDatabase mainImageDB;

    public static void main(String[] args){
        GUI mainGUI = new GUI();
    }

    GUI(){
        w = new JFrame();
        w.setTitle("Study Buddy 0.5alpha");
        w.setDefaultCloseOperation(w.EXIT_ON_CLOSE);


        menuBar = new JMenuBar();
        buildImageMenu();
	buildTagMenu();
	buildViewMenu();
        buildHelpMenu();
        menuBar.add(imageMenu);
	menuBar.add(viewMenu);
	menuBar.add(tagMenu);
        menuBar.add(helpMenu);


        w.setJMenuBar(menuBar);
        w.setLocationByPlatform(true);
        w.setIconImage(SysIcon.Logo.Icon.getImage());
	//w.addComponentListener(this);
        //w.setResizable(false);
        quickRestart();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        //while (true) {Thread.sleep(30);}
    }

    void quickRestart(){
	state = new ProgramState(LoadType.Init,this);        

	mainPanel = new MainPanel(this);
	mainPanel.addComponentListener(this);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(false);

	toolbarMain = new JToolBar("StudyBuddy Toolbar");
	toolbarMain.setFocusable(false);

	//boardScroll = new JScrollPane(mainPanel);
	//boardScroll.addComponentListener(this);

        Panel contentSet = new Panel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainPanel,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);

	Panel contentPane = new Panel();
    	contentPane.setLayout(new BorderLayout());

	ToolBar.addAll(toolbarMain,(ActionListener)this);

        //contentPane.add(boardScroll, BorderLayout.CENTER);
	contentPane.add(contentSet, BorderLayout.CENTER);//contentPane.add(mainPanel);
	contentPane.add(toolbarMain, BorderLayout.PAGE_START);
        w.setContentPane(contentPane);
        w.pack();
    }

    void buildImageMenu(){
        imageMenu = new JMenu("Image");
        imageMenu.setMnemonic(KeyEvent.VK_I);

	ImageMenu.addAll(imageMenu,(ActionListener)this);
    }
    
    void buildTagMenu(){
        tagMenu = new JMenu("Tag");
        tagMenu.setMnemonic(KeyEvent.VK_T);

        AddTag = new JMenuItem("Create new tag",KeyEvent.VK_N);
        //AddTag.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        AddTag.addActionListener(this);

        TagThis = new JMenuItem("Tag this Image",KeyEvent.VK_T);
        TagThis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,0));
        TagThis.addActionListener(this);

        TagFilter = new JMenuItem("Filter Images by Tag",KeyEvent.VK_F);
        //TagFilter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        TagFilter.addActionListener(this);

        tagMenu.add(AddTag);
        tagMenu.add(TagThis);
        tagMenu.add(TagFilter);
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
	if(e.getSource()==mImport) {
	    int wasGot = fileGetter.showOpenDialog(w);
	    if(wasGot==JFileChooser.APPROVE_OPTION){
		state.importImage(fileGetter.getSelectedFile().getAbsolutePath());
	    }
	    return;
	}
	if(e.getSource()==mRestart) {
            quickRestart();
	    return;
        }
        if(e.getSource()==NextImage || e.getActionCommand()=="bNext") {
            state.nextImage();
	    return;
        }
        if(e.getSource()==PrevImage || e.getActionCommand()=="bPrev") {
            state.prevImage();
	    return;
        }
        if(e.getSource()==ShowThumbs || e.getActionCommand()=="bThumbsS") {
	    thumbPanel.setVisible(true);
	    
	    ShowThumbs.setVisible(false);
	    HideThumbs.setVisible(true);

	    ToolBar.bThumbsS.hide();	    
	    ToolBar.bThumbsH.show();
	    return;
        }
	if(e.getSource()==AddTag || e.getActionCommand()=="bAddTag"){
	    String newTag = (String)JOptionPane.showInputDialog(
							w, 
							"Please enter the tag you wish to create?",
							"Create Tag",
							JOptionPane.PLAIN_MESSAGE,
							SysIcon.Question.Icon,
							null,
							"");
	    if ((newTag != null) && (newTag.length() > 0)) {
                mainImageDB.addTag(newTag);
		//mainImageDB.print();
                return;
            }
	    //Was cancelled
	    return;
	}
	if(e.getSource()==TagThis || e.getActionCommand()=="bTagThis"){
	    Object[] foundTags = mainImageDB.getAllTagTitles();
	    String newTag = (String)JOptionPane.showInputDialog(
								w, 
								"Which tag would you like to add to this image?", 
								"Add Tag to image", 
								JOptionPane.PLAIN_MESSAGE,
								SysIcon.Question.Icon,
								foundTags,
								"");
	    if ((newTag != null) && (newTag.length() > 0)) {
		mainImageDB.tagImage(state.imageIDs[state.currentI],mainImageDB.getTagIDFromTagTitle(newTag));
		//mainImageDB.print();
                return;
            }
	    return;
	}
	if(e.getSource()==TagFilter || e.getActionCommand()=="bTagFilter"){
	    String[] foundTags = mainImageDB.getAllTagTitles();
	    String[] tagFilters = new String[(foundTags.length + 1)];
	    tagFilters[0] = "Show All Images";
	    System.arraycopy(foundTags,0,tagFilters,1,foundTags.length);

	    String filterTag = (String)JOptionPane.showInputDialog(
								   w, 
								   "Which tag do you want to search for?", 
								   "Add Tag to image", 
								   JOptionPane.PLAIN_MESSAGE,
								   SysIcon.Question.Icon,
								   tagFilters,
								   "Show All Images");
	    if ((filterTag != null) && (filterTag.length() > 0)) {
		if(filterTag.equals("Show All Images")){
		    state = new ProgramState(LoadType.Refresh,this);
		}
		else {
		    state = new ProgramState(LoadType.Filter,this,filterTag);
		}
		//mainImageDB.print();
                return;
            }
	    return;
	}
        if(e.getSource()==HideThumbs || e.getActionCommand()=="bThumbsH") {
	    thumbPanel.setVisible(false);
	    
	    ShowThumbs.setVisible(true);
	    HideThumbs.setVisible(false);

	    ToolBar.bThumbsS.show();	    
	    ToolBar.bThumbsH.hide();
	    return;
        }
        if(e.getSource()==Exit) {
            System.exit(0);
        }
        if(e.getSource()==Help) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w,"Visit http://www.studybuddy.com for help and tutorials","Study Help",JOptionPane.INFORMATION_MESSAGE);
	    return;
        }
        if(e.getSource()==About) {
            JOptionPane.showMessageDialog(w,"StudyBuddy by Team StudyBuddy","About StudyBuddy",JOptionPane.INFORMATION_MESSAGE);
	    return;
        }
	System.err.println("ActionEvent " + e.getActionCommand() + " was not dealt with,\nand had prameter string " + e.paramString()); //+ ",\nwith source:\n\n " + e.getSource());
    }

    public void componentResized(ComponentEvent e) {
	// if(e.getSource()==boardScroll) {
	if(e.getSource()==mainPanel) {
            mainPanel.onResize();
	    thumbPanel.onResize();
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
    BufferedImage bImage = null;
    Orientation iOri;
    int width,height;
    //String imageID;

    ImageObject(String absoluteURL){
	URL urlAddress;
	if(absoluteURL.startsWith("///\\\\\\")){
	    String relativeURL = absoluteURL.substring(6);
	    //System.out.println(relativeURL +  " is relative and absolute is " + absoluteURL);
	    urlAddress = GUI.class.getResource(relativeURL); //could be null
	}
	else {
	    File file = new File(absoluteURL);
	    try{
		urlAddress = file.toURI().toURL();
		//System.out.println(absoluteURL +  " is absolute and file is "+file.toString() +" and URL is " + urlAddress.toString());
	    } catch (MalformedURLException e){
		urlAddress = null;
		System.err.println("Image file " + absoluteURL + " could not be found " + "\nError was: " + e.toString());
	    }
	}
	if(urlAddress==null){
	    System.err.println("File could not be found at " + absoluteURL);
	}
	ImageObjectConstructor(urlAddress,absoluteURL);
    }

    ImageObject(URL urlAddress){
	String absoluteURL = urlAddress.toString();//should find absoluteURL for printing
	ImageObjectConstructor(urlAddress,absoluteURL);
    }

    //ImageObject(URL urlAddress, String absoluteURL){
    //	ImageObjectConstructor(urlAddress, absoluteURL);
    //}

    void ImageObjectConstructor(URL urlAddress, String absoluteURL){
        try {
            bImage = ImageIO.read(urlAddress);
            //File fileAddress = new File(relativeURL);
            //img = ImageIO.read(fileAddress)
	    setVars();

        } catch (IOException e) {
	    System.err.println("Error loading image " + absoluteURL + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	    //JOptionPane.showMessageDialog(parentPane,"Error Loading Image" + e.toString(),"Fatal Error",JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
	    System.err.println("Image file " + absoluteURL + " could not be found " + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	} catch (NullPointerException e) {
	    System.err.println("Could not load image from file " + absoluteURL + "\nError was: " + e.toString());
	    setToXasFileNotFound();
	}
    }

    void setVars(){
	width = bImage.getWidth(null);
	height = bImage.getHeight(null);
	if(height<width) iOri = Orientation.Landscape;
	else iOri = Orientation.Portrait;
    }

    void setToXasFileNotFound(){
	//set the buffered image to a java graphics drawn X icon
	//bImage = ######
	//set width, height and orientation
	//setVars();
    }

}

//Type of load ProgramState does. Respectivly:
//(Creates new DB, loads DB from file, uses existing DB with filter, uses whole existing DB)
enum LoadType{Init,Load,Filter,Refresh}

//Should hold data relating to program state and control program state
//Should hold references to databses and image locations
class ProgramState{
    ImageObject[] imageList;
    String[] imageIDs;
    int lastIndex; //Must be updated when number of images changes
    String currentFilter;
    int currentI = 0;
    GUI mainGUI;

    ProgramState(LoadType loadType, GUI parentGUI){
	ContructProgramState(loadType,  parentGUI,""); //loadType should not be filter here
    }
    ProgramState(GUI parentGUI, String filterTag){
	ContructProgramState(LoadType.Filter, parentGUI, filterTag);
    }
    ProgramState(LoadType loadType, GUI parentGUI, String filterTag){
	ContructProgramState(loadType, parentGUI, filterTag);
    }

    void ContructProgramState(LoadType loadType, GUI parentGUI, String filterTag){
	mainGUI = parentGUI;
	switch (loadType){
	case Init:
	    mainGUI.mainImageDB = new ImageDatabase("mainDB");
	    //If there are no files you get loads of errors

	    mainGUI.mainImageDB.addImage("Title 1","///\\\\\\img_2810b_small.jpg");
	    //mainGUI.mainImageDB.addImage("Creates error- not found","///\\\\\\img_monkeys_small.jpg");
	    //mainGUI.mainImageDB.addImage("Creates Error- not an image","///\\\\\\NotAnImage.txt");
	    mainGUI.mainImageDB.addImage("Title 1","///\\\\\\img_6088b_small.jpg");
	    mainGUI.mainImageDB.addImage("Title 1","///\\\\\\img_5672bp_small.jpg");
	    mainGUI.mainImageDB.addImage("Title 1","///\\\\\\img_2926_small.jpg");
	    mainGUI.mainImageDB.addImage("Title 1","///\\\\\\img_F028c_small.jpg");

	    //no break as image list must still be passed from DB
	case Refresh:
	    //Create image database by loading database
	    currentFilter = "Show All Images";
	    imageIDs = mainGUI.mainImageDB.getAllImageIDs();
	    break;
	case Load: System.exit(1); //Load DB not yet implemented
	    break;
	case Filter:
	    //Create image database by loading database	
	    currentFilter = filterTag;
	    imageIDs = mainGUI.mainImageDB.getImageIDsFromTagTitle(filterTag);
	    break;
	}
	//if imageIDs.length==0
	//then a file should be added first (Construct with Init&imports, then return;)
      	imageList = new ImageObject[imageIDs.length];
	for(int i=0; i<imageIDs.length;i++){
	    imageList[i] = new ImageObject(mainGUI.mainImageDB.getImageFilename(imageIDs[i]));
	}
	lastIndex = (imageIDs.length - 1);
	if(loadType!=LoadType.Init){
	    mainGUI.mainPanel.repaint();
	    mainGUI.thumbPanel.repaint();
	}
    }

    void importImage(String absolutePath){
	mainGUI.mainImageDB.addImage("Title 1",absolutePath);
	if(currentFilter.equals("Show All Images")){
	    mainGUI.state = new ProgramState(LoadType.Refresh,mainGUI);
	    mainGUI.state.currentI = mainGUI.state.imageIDs.length - 1;
	}
	else {
	    mainGUI.state = new ProgramState(LoadType.Filter,mainGUI,currentFilter);
	}
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

    // Must be edited so empty DB / imageList does no cause error
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
	int[] useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Set dimensions
	useWH = mainGUI.state.scaleToMax(mainGUI.state.getCurrentImage().width,mainGUI.state.getCurrentImage().height, boardW, boardH);
	int leftOfset = (boardW - useWH[0]) / 2;
	int topOfset = (boardH - useWH[1]) / 2;
	g2.drawImage(mainGUI.state.getCurrentImage().bImage, leftOfset, topOfset,useWH[0],useWH[1], this);
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
    GUI mainGUI;

    ThumbPanel(GUI parentGUI) {
	mainGUI = parentGUI;
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
	int currentThumb = mainGUI.state.currentI;
	int thumbOfsetW =0;
	int thumbOfsetH = 0;
	for(int i = 0; (i<tileW)&&(i<mainGUI.state.imageIDs.length);i++){
	    //set dimension
	    currentThumb = mainGUI.state.next(currentThumb);
	    useWH = mainGUI.state.scaleToMax(mainGUI.state.imageList[currentThumb].width,mainGUI.state.imageList[currentThumb].height, squareSize, squareSize);
	    thumbOfsetW= (squareSize - useWH[0])/2;
	    thumbOfsetH= (squareSize - useWH[1])/2;
	    g2.drawImage(mainGUI.state.imageList[currentThumb].bImage, leftOfset+thumbOfsetW, topOfset+thumbOfsetH,useWH[0],useWH[1], this);
	    leftOfset+=(squareSize + 2);
	}
    }
}
