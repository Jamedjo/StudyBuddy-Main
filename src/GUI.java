import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import javax.swing.JOptionPane.*;
//import javax.swing.ScrollPaneLayout;

//We should use javadoc.

//Refresh image feature?

//Program stucture needs to be redesigned to implement SwingWorker threads to load images
//Each image will be published once loaded and the worker will be done when all are loaded
//This prevents loading large images from freezing/crashing the program,
//allows the GUI to load quicker at startup instead of waiting for all images to load
//and allows for the worker to be cancelled if it is too slow.

//Should be seperated into intial thread, and an event dispatch thread which implements the listeners.
class GUI implements ActionListener, ComponentListener,WindowStateListener {
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
    Thread slideThread;

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
        slideThread = new Thread(new SlideShow(this));
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
	mainPanel = new MainPanel(this);

	thumbPanel = new ThumbPanel(this);
	//thumbPanel.addComponentListener(this);
	thumbPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	thumbPanel.setVisible(true);

	toolbarMain = ToolBar.build((ActionListener)this);

        TagTree = mainImageDB.toTree();
        //TagTree.setMinimumSize(new Dimension(150,0));
        TagTree.addTreeSelectionListener(new TagTreeListener(this));

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.getViewport().setBackground(Color.darkGray);//comment out to see scroll bar bug
        mainScrollPane.setPreferredSize(mainPanel.getPreferredSize());

        JPanel contentSet = new JPanel();
    	contentSet.setLayout(new BorderLayout());
	contentSet.add(mainScrollPane,BorderLayout.CENTER);
	contentSet.add(thumbPanel,BorderLayout.PAGE_END);
        
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,TagTree,contentSet);
        splitpane.setOneTouchExpandable(true);
        splitpane.setDividerLocation(150 + splitpane.getInsets().left);
        //splitpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

	JPanel contentPane = new JPanel();
    	contentPane.setLayout(new BorderLayout());
	contentPane.add(splitpane, BorderLayout.CENTER);//contentPane.add(mainPanel);
	contentPane.add(toolbarMain, BorderLayout.PAGE_START);

        w.setContentPane(contentPane);
        w.addWindowStateListener(this);
        w.pack();
        mainPanel.onResize();
        thumbPanel.onResize();
	contentPane.addComponentListener(this);//don't want it to trigger while building
    }
    
    @Override public void actionPerformed(ActionEvent ae){
        if(ae.getActionCommand().equals("mRestart")){
            toggleThumbs(true);
	    toggleZoomed(true);
            quickRestart();
        }
        else if(ae.getActionCommand().equals("mImport")) importDo();
	else if(ae.getActionCommand().equals("ThumbsS")) toggleThumbs(true);
        else if(ae.getActionCommand().equals("ThumbsH")) toggleThumbs(false);
	else if(ae.getActionCommand().equals("SlideP")) toggleSlide(true);
        else if(ae.getActionCommand().equals("SlideS")) toggleSlide(false);
        else if(ae.getActionCommand().equals("ZoomFit")) toggleZoomed(true);
	else if(ae.getActionCommand().equals("Zoom100")) toggleZoomed(false);
        else if(ae.getActionCommand().equals("Next")) state.nextImage();
        else if(ae.getActionCommand().equals("Prev")) state.prevImage();
        else if (ae.getActionCommand().equals( "AddTag")) {
            TagTree = mainImageDB.addTagFromTree(TagTree, w);
            TagTree.repaint();
        }
        else if (ae.getActionCommand().equals("TagThis")) tagThis();
        else if (ae.getActionCommand().equals("TagFilter")) tagFilter();
        else if (ae.getActionCommand().equals("BlueT")) bluetoothDo();
        else if (ae.getActionCommand().equals("Exit")) {
            System.exit(0);
        }
        else if(ae.getActionCommand().equals("Help")) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w,"Visit http://www.studybuddy.com for help and tutorials","Study Help",JOptionPane.INFORMATION_MESSAGE,SysIcon.Info.Icon);
	}
        else if(ae.getActionCommand().equals("About")) {
            JOptionPane.showMessageDialog(w,"StudyBuddy by Team StudyBuddy","About StudyBuddy",JOptionPane.INFORMATION_MESSAGE,SysIcon.Help.Icon);
	}
	else System.err.println("ActionEvent " + ae.getActionCommand() + " was not dealt with,\nand had prameter string "+ ae.paramString());
	//+ ",\nwith source:\n\n " + e.getSource());
    }

    @Override public void windowStateChanged(WindowEvent e){
            mainPanel.onResize();
	    thumbPanel.onResize();
    }

    @Override public void componentResized(ComponentEvent e) {
	// if(e.getSource()==boardScroll) {
	//if(e.getSource()==mainPanel) {
        //**//System.out.println(e.paramString());
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
    @Override public void componentHidden(ComponentEvent e){}
    @Override public void componentMoved(ComponentEvent e){}
    @Override public void componentShown(ComponentEvent e){}

    void toggleThumbs(boolean makeVisible){//true to show
        thumbPanel.setVisible(makeVisible);

        ViewMenu.ShowThumbs.setVisible(!makeVisible);
        ViewMenu.HideThumbs.setVisible(makeVisible);
        ToolBar.bThumbsS.setVisible(!makeVisible);
        ToolBar.bThumbsH.setVisible(makeVisible);

        mainPanel.onResize();
    }
    void toggleZoomed(boolean makeFit){//true to set zoom to fit
        mainPanel.isZoomed = (!makeFit);

        ViewMenu.ZoomToFit.setVisible(!makeFit);
        ViewMenu.ZoomTo100.setVisible(makeFit);
        ToolBar.bZoomFit.setVisible(!makeFit);
        ToolBar.bZoomMax.setVisible(makeFit);

        mainPanel.onResize();
    }
    void toggleSlide(boolean setPlaying){//true to start playing
        if(setPlaying){
            slideThread.start();
        } else {
            slideThread.interrupt();
            slideThread = new Thread(new SlideShow(this));
        }

        ViewMenu.SlidePlay.setVisible(!setPlaying);
        ViewMenu.SlideStop.setVisible(setPlaying);
        ToolBar.bSlideP.setVisible(!setPlaying);
        ToolBar.bSlideS.setVisible(setPlaying);

        mainPanel.onResize();
    }

    void bluetoothDo() {
        try {
            String outcome = "Device does not support sellected protocol";

            JOptionPane.showMessageDialog(w, "Click OK to procede with Bluetooh.\nThis may take some time to respond", "Bluetooth", JOptionPane.INFORMATION_MESSAGE, SysIcon.Info.Icon);

            BlueDemo blD = BlueDemo.BlueTester();
            Object[] DevIDs = blD.devicelist;
            if (DevIDs == null || DevIDs.length == 0) {
                outcome = "No Bluetooth devices could be found,\nPlease ensure phone is on and near by.";
            } else {
                String DevString = (String) JOptionPane.showInputDialog(w, "Which device would you like to use?", "Bluetooth Devices Found",
                        JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, DevIDs, null);

                int chosenDevId = -1;
                for (int i = 0; i < DevIDs.length; i++) {
                    if (DevString.equals(DevIDs[i])) {
                        chosenDevId = i;
                        i = (Integer.MAX_VALUE - 1);
                    }
                }

                if (BlueDemo.probeProtocol(blD, chosenDevId)) {
                    outcome = "Device supports OBEX push";
                }
            }
            JOptionPane.showMessageDialog(w, outcome, "Bluetooth service discovery", JOptionPane.INFORMATION_MESSAGE, SysIcon.Info.Icon);
        } catch (IOException er) {
            er.printStackTrace();
        }
    }

    void tagFilter() {
        Object[] AllTags = mainImageDB.getTagIDTitles();
        Object[] TagFilters = new IDTitle[(AllTags.length + 1)];
        TagFilters[0] = new IDTitle("-1", "Show All Images");
        System.arraycopy(AllTags, 0, TagFilters, 1, AllTags.length);

        Object FilterTag = JOptionPane.showInputDialog(w, "Which tag do you want to search for?", "Filter images",
                JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, TagFilters, null);
        if ((FilterTag != null) && (FilterTag instanceof IDTitle)) {
            IDTitle FilterTagIDTitle = (IDTitle) FilterTag;
            if (FilterTagIDTitle.getID().equals("-1")) {
                state = new ProgramState(LoadType.Refresh, this); //flush first?
            } else {
                state = new ProgramState(LoadType.Filter, this, FilterTagIDTitle.getID()); //flush first?
            }
        }
    }
    void tagThis() {
        Object[] AllTags = mainImageDB.getTagIDTitles();
        Object NewTag = JOptionPane.showInputDialog(w, "Which tag would you like to add to this image?", "Add Tag to image",
                JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, AllTags, null);
        if ((NewTag != null) && (NewTag instanceof IDTitle)) {
            IDTitle NewTagIDTitle = (IDTitle) NewTag;
            mainImageDB.tagImage(state.imageIDs[state.currentI], NewTagIDTitle.getID());
        }
    }
    void importDo(){
        int wasGot = fileGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            state.importImages(fileGetter.getSelectedFiles());
        }
    }
}
