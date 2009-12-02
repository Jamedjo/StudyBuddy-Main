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

//Type of load ProgramState does. Respectivly:
//(Creates new DB, loads DB from file, uses existing DB with filter, uses whole existing DB)
enum LoadType{Init,Load,Filter,Refresh}

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
	buildMenuBar();
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

    void buildMenuBar(){
        menuBar = new JMenuBar();
        imageMenu = ImageMenu.build((ActionListener)this);
        tagMenu = TagMenu.build((ActionListener)this);
        viewMenu = ViewMenu.build((ActionListener)this);
        helpMenu = HelpMenu.build((ActionListener)this);
        menuBar.add(imageMenu);
	menuBar.add(viewMenu);
	menuBar.add(tagMenu);
        menuBar.add(helpMenu);
    }

    void quickRestart(){
	state = new ProgramState(LoadType.Init,this);        

	mainPanel = new MainPanel(this);
	mainPanel.addComponentListener(this);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(false);

	toolbarMain = ToolBar.build((ActionListener)this);

	//boardScroll = new JScrollPane(mainPanel);
	//boardScroll.addComponentListener(this);

        Panel contentSet = new Panel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainPanel,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);

	Panel contentPane = new Panel();
    	contentPane.setLayout(new BorderLayout());

        //contentPane.add(boardScroll, BorderLayout.CENTER);
	contentPane.add(contentSet, BorderLayout.CENTER);//contentPane.add(mainPanel);
	contentPane.add(toolbarMain, BorderLayout.PAGE_START);
        w.setContentPane(contentPane);
        w.pack();
    }
    
    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand()=="mImport") {
	    int wasGot = fileGetter.showOpenDialog(w);
	    if(wasGot==JFileChooser.APPROVE_OPTION){
		state.importImage(fileGetter.getSelectedFile().getAbsolutePath());
	    }
	    return;
	}
	if(e.getActionCommand()=="mRestart") {
            quickRestart();
	    return;
        }
        if(e.getActionCommand()=="Next") {
            state.nextImage();
	    return;
        }
        if(e.getActionCommand()=="Prev") {
            state.prevImage();
	    return;
        }
        if(e.getActionCommand()=="ThumbsS") {
	    thumbPanel.setVisible(true);
	    
	    ViewMenu.ShowThumbs.hide();	    
	    ViewMenu.HideThumbs.show();
	    ToolBar.bThumbsS.hide();	    
	    ToolBar.bThumbsH.show();
	    return;
        }
	if(e.getActionCommand()=="AddTag"){
	    String newTag = (String)JOptionPane.showInputDialog(w,"Please enter the tag you wish to create?","Create Tag",
								JOptionPane.PLAIN_MESSAGE,SysIcon.Question.Icon,null,"");
	    if ((newTag != null) && (newTag.length() > 0)) {
                mainImageDB.addTag(newTag);
		//mainImageDB.print();
                return;
            }
	    //Was cancelled
	    return;
	}
	if(e.getActionCommand()=="TagThis"){
	    Object[] foundTags = mainImageDB.getAllTagTitles();
	    String newTag = (String)JOptionPane.showInputDialog(w,"Which tag would you like to add to this image?", "Add Tag to image", 
								JOptionPane.PLAIN_MESSAGE,SysIcon.Question.Icon,foundTags,"");
	    if ((newTag != null) && (newTag.length() > 0)) {
		mainImageDB.tagImage(state.imageIDs[state.currentI],mainImageDB.getTagIDFromTagTitle(newTag));
		//mainImageDB.print();
                return;
            }
	    return;
	}
	if(e.getActionCommand()=="TagFilter"){
	    String[] foundTags = mainImageDB.getAllTagTitles();
	    String[] tagFilters = new String[(foundTags.length + 1)];
	    tagFilters[0] = "Show All Images";
	    System.arraycopy(foundTags,0,tagFilters,1,foundTags.length);

	    String filterTag = (String)JOptionPane.showInputDialog(w,"Which tag do you want to search for?","Add Tag to image", 
								   JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, tagFilters, "Show All Images");
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
        if(e.getActionCommand()=="ThumbsH") {
	    thumbPanel.setVisible(false);
	    
	    ViewMenu.ShowThumbs.show();	    
	    ViewMenu.HideThumbs.hide();
	    ToolBar.bThumbsS.show();	    
	    ToolBar.bThumbsH.hide();
	    return;
        }
        if(e.getActionCommand()=="Exit") {
            System.exit(0);
        }
        if(e.getActionCommand()=="Help") {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w,"Visit http://www.studybuddy.com for help and tutorials","Study Help",JOptionPane.INFORMATION_MESSAGE);
	    return;
        }
        if(e.getActionCommand()=="About") {
            JOptionPane.showMessageDialog(w,"StudyBuddy by Team StudyBuddy","About StudyBuddy",JOptionPane.INFORMATION_MESSAGE);
	    return;
        }
	System.err.println("ActionEvent " + e.getActionCommand() + " was not dealt with,\nand had prameter string "+ e.paramString()); 
	//+ ",\nwith source:\n\n " + e.getSource());
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
