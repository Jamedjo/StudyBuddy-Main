import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import javax.swing.JOptionPane.*;

//We should use javadoc.

//Refresh image feature?

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
    JTree TagTree;
    //JLabel mainPhoto;

    public static void main(String[] args){
        GUI mainGUI = new GUI();
    }

    GUI(){
        w = new JFrame();
        w.setTitle("Study Buddy 0.6beta");
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
	state = new ProgramState(this);        

	//mainPhoto = new JLabel();
        //mainPhoto.setVerticalTextPosition(JLabel.BOTTOM);
        //mainPhoto.setHorizontalTextPosition(JLabel.CENTER);
        //mainPhoto.setHorizontalAlignment(JLabel.CENTER);


	mainPanel = new MainPanel(this);
	//mainPanel.add(mainPhoto);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(true);

	toolbarMain = ToolBar.build((ActionListener)this);

	//boardScroll = new JScrollPane(mainPanel);
	//boardScroll.addComponentListener(this);

        TagTree = mainImageDB.toTree();
        //TagTree.setMinimumSize(new Dimension(150,0));
        TagTree.addTreeSelectionListener(new TagTreeListener(this));

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        //mainScrollPane.setBackground(Color.darkGray);

        JPanel contentSet = new JPanel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainScrollPane,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);
        
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,TagTree,contentSet);
        splitpane.setOneTouchExpandable(true);
        splitpane.setDividerLocation(150 + splitpane.getInsets().left);


	JPanel contentPane = new JPanel();
    	contentPane.setLayout(new BorderLayout());

        //contentPane.add(boardScroll, BorderLayout.CENTER);
	contentPane.add(splitpane, BorderLayout.CENTER);//contentPane.add(mainPanel);
	contentPane.add(toolbarMain, BorderLayout.PAGE_START);

	contentPane.addComponentListener(this);
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

        if(e.getActionCommand()=="ThumbsH"||e.getActionCommand()=="mRestart") {
	    thumbPanel.setVisible(false);
	    
	    ViewMenu.ShowThumbs.show();	    
	    ViewMenu.HideThumbs.hide();
	    ToolBar.bThumbsS.show();	    
	    ToolBar.bThumbsH.hide();
	    if(e.getActionCommand()=="ThumbsH") return;
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
	if (e.getActionCommand() == "AddTag")
  {
    TagTree = mainImageDB.addTagFromTree(TagTree, w);
    TagTree.repaint();
    return;
	}
	if (e.getActionCommand() == "TagThis")
  {
    Object[] AllTags = mainImageDB.getTagIDTitles();
    Object NewTag = JOptionPane.showInputDialog(w, "Which tag would you like to add to this image?", "Add Tag to image", 
              JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, AllTags, null);
    if ((NewTag != null) && (NewTag instanceof IDTitle))
    {
      IDTitle NewTagIDTitle = (IDTitle) NewTag;
      mainImageDB.tagImage(state.imageIDs[state.currentI], NewTagIDTitle.getID());
    }
    return;
	}
	if (e.getActionCommand() == "TagFilter")
  {
    Object[] AllTags = mainImageDB.getTagIDTitles();
    Object[] TagFilters = new IDTitle[(AllTags.length + 1)];
    TagFilters[0] = new IDTitle("-1", "Show All Images");
    System.arraycopy(AllTags,0,TagFilters,1,AllTags.length);
    
    Object FilterTag = JOptionPane.showInputDialog(w,"Which tag do you want to search for?","Filter images", 
                 JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, TagFilters, null);
    if ((FilterTag != null) && (FilterTag instanceof IDTitle))
    {
      IDTitle FilterTagIDTitle = (IDTitle) FilterTag;
      if (FilterTagIDTitle.getID().equals("-1"))
        state = new ProgramState(LoadType.Refresh, this); //flush first?
      else
        state = new ProgramState(LoadType.Filter, this, FilterTagIDTitle.getID()); //flush first?
    }
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
	//if(e.getSource()==mainPanel) {
        System.out.println(e.paramString());
            mainPanel.onResize();
	    thumbPanel.onResize();
        //}
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

