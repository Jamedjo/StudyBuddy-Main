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

//We should use javadoc.

//Refresh image feature?

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
    JMenuItem  Options;
    JButton bSideBar;
    final JFileChooser fileGetter = new JFileChooser();
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JScrollPane boardScroll;
    ProgramState state;
    JOptionPane tagBox;
    ImageDatabase mainImageDB;
    //JLabel mainPhoto;

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
	buildFileGetter();
        quickRestart();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        //while (true) {Thread.sleep(30);}
    }
    void buildFileGetter(){
	fileGetter.addChoosableFileFilter( new javax.swing.filechooser.FileFilter(){
		public boolean accept(File f){
		    if(f.isDirectory()) return true;
		    String[] exts = {"jpeg","jpg","gif","bmp","png","tiff","tif","tga","pcx","xbm","svg"};
		    String ext = null;
		    String name = f.getName();
		    int pos = name.lastIndexOf(".");
		    if(pos>0 && pos<(name.length() - 1)){
			    ext = name.substring(pos+1).toLowerCase();
			    for(String imgExt : exts){
				if(ext.equals(imgExt)){
				    return true;
				}
			    }
			} 
		    return false;
		}
		public String getDescription() {
		    return "All Images";
		}
	    }
	    );
	fileGetter.setDialogTitle("Import Image");
	fileGetter.setMultiSelectionEnabled(true);

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

	//mainPhoto = new JLabel();
        //mainPhoto.setVerticalTextPosition(JLabel.BOTTOM);
        //mainPhoto.setHorizontalTextPosition(JLabel.CENTER);
        //mainPhoto.setHorizontalAlignment(JLabel.CENTER);


	mainPanel = new MainPanel(this);
	mainPanel.addComponentListener(this);
	//mainPanel.add(mainPhoto);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(false);

	toolbarMain = ToolBar.build((ActionListener)this);

	//boardScroll = new JScrollPane(mainPanel);
	//boardScroll.addComponentListener(this);

        JPanel contentSet = new JPanel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainPanel,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);

	JPanel contentPane = new JPanel();
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
		state.importImages(fileGetter.getSelectedFiles());
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
		    state = new ProgramState(LoadType.Refresh,this);//flush first?
		}
		else {
		    state = new ProgramState(LoadType.Filter,this,filterTag);//flush first?
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
            JOptionPane.showMessageDialog(w,"Visit http://www.studybuddy.com for help and tutorials","Study Help",JOptionPane.INFORMATION_MESSAGE,SysIcon.Info.Icon);
	    return;
        }
        if(e.getActionCommand()=="About") {
            JOptionPane.showMessageDialog(w,"StudyBuddy by Team StudyBuddy","About StudyBuddy",JOptionPane.INFORMATION_MESSAGE,SysIcon.Help.Icon);
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
//Should keep track of whether to flush the curent image and various thumbs based-
//Previous image and 3 next images should be kept, others flushed.
class ProgramState{
    private ImageObject[] imageList;
    String[] imageIDs;
    int lastIndex; //Must be updated when number of images changes
    String currentFilter;
    int currentI = 0;//make private
    GUI mainGUI;
    boolean isLocked = false;//Do not draw if locked.

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

	    mainGUI.mainImageDB.addImage("Park","///\\\\\\img_2810b_small.jpg");
	    
	    mainGUI.mainImageDB.addImage("Creates error- not found","///\\\\\\img_monkeys_small.jpg");

	    mainGUI.mainImageDB.addImage("Creates Error- not an image","///\\\\\\NotAnImage.txt");
	    mainGUI.mainImageDB.addImage("Igloo in Bristol","///\\\\\\img_6088b_small.jpg");
	    mainGUI.mainImageDB.addImage("Pink","///\\\\\\img_5672bp_small.jpg");
	    mainGUI.mainImageDB.addImage("Speed","///\\\\\\img_2926_small.jpg");
	    mainGUI.mainImageDB.addImage("Food","///\\\\\\img_F028c_small.jpg");
	    mainGUI.mainImageDB.addImage("Data Structures&Algorithms note 1","///\\\\\\DSA_1.bmp");
	    mainGUI.mainImageDB.addImage("Graph Notes for C/W","///\\\\\\DSA_7.bmp");

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

    void importImages(File[] files){
	isLocked = true;
	for(File f : files){
	    System.out.println(f.getPath()+ " is the getPath and the absPath is " +f.getAbsolutePath());//Should be removed later
	    mainGUI.mainImageDB.addImage("Title 1",f.getAbsolutePath());
	}
	try{
	    if(currentFilter.equals("Show All Images")){
		mainGUI.state = new ProgramState(LoadType.Refresh,mainGUI);
		//Sets image to last image
		//mainGUI.state.currentI = mainGUI.state.imageIDs.length - 1;
		//Sets current image to first loaded image.
		//mainGUI.state.currentI = this.lastIndex + 1;//Bad code as if load fails the this is out of bounds
		//Will be replaced by feeding lastIndex+1 into constructor above
	    }
	    else {
		mainGUI.state = new ProgramState(LoadType.Filter,mainGUI,currentFilter);
	    }
	} catch(java.lang.OutOfMemoryError e){
            JOptionPane.showMessageDialog(mainGUI.w,"Out of memory- over 128MB was needed\n"
					  +"SwingWorker should be used in code,\n"
					  +"and not all images should be buffered","Fatal Error",JOptionPane.ERROR_MESSAGE);
	} finally {
	    isLocked = false;
	    safelyDestruct();
	}
    }

    void importImage(String absolutePath){//should make a file and call importImages(new File[])
	isLocked = true;
	mainGUI.mainImageDB.addImage("Title 1",absolutePath);
	if(currentFilter.equals("Show All Images")){
	    imageIDs = mainGUI.mainImageDB.getAllImageIDs();
	    if(lastIndex != (imageIDs.length - 1)) lastIndex = imageIDs.length - 1;
	    else return; //If there are no more images than before import, then failure
	    currentI = lastIndex;
	    imageList[lastIndex] = new ImageObject(mainGUI.mainImageDB.getImageFilename(imageIDs[lastIndex]));
	    mainGUI.mainPanel.repaint();
	    mainGUI.thumbPanel.repaint();
	}
	else {
	    mainGUI.state = new ProgramState(LoadType.Filter,mainGUI,currentFilter);
	    safelyDestruct();
	}
	isLocked = false;
    }

    //flushes all images and thumbs
    void safelyDestruct(){
	//might check if mainGUI.state==this, as this would imply no need to distruct yet.
	for(ImageObject imgObj : imageList){
	    imgObj.destroy();
	    imgObj = null;
	}
	imageList = null;
	imageIDs = null;
	//call garbage collect?
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

    // Must be edited so empty DB/imageList does not cause error
    ImageObject getCurrentImage(){
	return imageList[currentI];
    }

    ImageObject getImageI(int i){
	return imageList[i];//will be changed later to keep track of images in memory
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
	if(mainGUI.state.isLocked) return;
	int[] useWH;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
	//Set dimensions
	useWH = ImageObject.scaleToMax(mainGUI.state.getCurrentImage().getWidthAndMake(),mainGUI.state.getCurrentImage().getHeightAndMake(), boardW, boardH);
	int leftOfset = (boardW - useWH[0]) / 2;
	int topOfset = (boardH - useWH[1]) / 2;

	//mainGUI.mainPhoto.setIcon(mainGUI.state.getCurrentImage().getIcon(ImgSize.Screen));

	g2.drawImage(mainGUI.state.getCurrentImage().getImage(ImgSize.Screen), leftOfset, topOfset,useWH[0],useWH[1], this);
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
	if(mainGUI.state.isLocked) return;
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
	    useWH = ImageObject.scaleToMax(mainGUI.state.getImageI(currentThumb).getWidthForThumb(),mainGUI.state.getImageI(currentThumb).getHeightForThumb(), squareSize, squareSize);
	    thumbOfsetW= (squareSize - useWH[0])/2;
	    thumbOfsetH= (squareSize - useWH[1])/2;
	    //mainGUI.mainPhoto.setIcon(mainGUI.state.imageList[currentThumb].getIcon(ImgSize.Thumb));
	    g2.drawImage(mainGUI.state.getImageI(currentThumb).getImage(ImgSize.Thumb), leftOfset+thumbOfsetW, topOfset+thumbOfsetH,useWH[0],useWH[1], this);
	    leftOfset+=(squareSize + 2);
	}
    }
}
