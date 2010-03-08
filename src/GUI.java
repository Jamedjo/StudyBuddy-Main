
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.ScrollPaneLayout;

//We should use javadoc.


//TODO: make amount scrolled by scroll wheel relative to current size.
//At the moment it is linear i.e. zoooming out from 500% goes to
//480% and zooming out from 40% goes to 20% both with same difference

//Refresh image feature?

//Should be seperated into intial thread, and an event dispatch thread which implements the listeners.
class GUI implements ActionListener, ComponentListener, WindowStateListener, ChangeListener {
    Log log;
    Settings settings;
    JFrame w;
    JMenuBar menuBar;
    JMenu imageMenu, viewMenu, tagMenu, helpMenu;
    JMenuItem Options;
    JButton bSideBar;
    final JFileChooser fileGetter = new JFileChooser();
    final JFileChooser folderGetter = new JFileChooser();
    final JFileChooser jpgExporter = new JFileChooser();
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JScrollPane boardScroll;
    ImageAdjuster adjuster;
    OptionsGUI optionsGUI;
    TagTagger tagTagger;
    QuickTagger quickTagger;
    volatile ProgramState state;
    JOptionPane tagBox;
    ImageDatabase mainImageDB;
    TagTree tagTree;
    Thread slideThread;
    JScrollPane mainScrollPane;
    JSplitPane splitpane;
	JPanel contentPane;
    JPanel imageAreas;
	JScrollPane notePane;
    JSlider zoomBar;
    File thumbPath;
    final int tagTreeStartSize = 150;
    final int tagTreeMaxSize = 350;
    //boolean isChangingState = false;

    public static void main(String[] args) {
        GUI mainGUI = new GUI();
    }

    void setTitle() {
        setTitle(null);
    }

    void setTitle(String suffix) {
        String prefix = "Study Buddy 0.9gamma";
        if (suffix == null) suffix = "";
        else prefix = prefix.concat("- ");
        w.setTitle(prefix + suffix);

    }

    GUI() {
        log = new Log();
        w = new JFrame();
        setTitle();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildMenuBar();
        w.setJMenuBar(menuBar);
        w.setLocationByPlatform(true);
        w.setIconImage(SysIcon.Logo.Icon.getImage());
        //w.addComponentListener(this);
        buildFileGetter();
        buildFolderGetter();
        buildJpgExporter();
        quickRestart();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        slideThread = new Thread(new SlideShow(this,settings.getSettingAsInt("slideShowTime")));
    }

    boolean isImage(File f){
        String[] exts = {"jpeg", "jpg", "gif", "bmp", "png", "tiff", "tif", "tga", "pcx", "xbm", "svg","wbmp"};
        //String[] readerNames = ImageIO.getReaderFormatNames();
        String ext = null;
        String name = f.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0 && pos < (name.length() - 1)) {
            ext = name.substring(pos + 1).toLowerCase();
            for (String imgExt : exts) {
                if (ext.equals(imgExt)) {
                    return true;
                }
            }
        }
        return false;
    }

    void buildFileGetter() {
        fileGetter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return isImage(f);
            }
            public String getDescription() {
                return "All Images";
            }
        });
        fileGetter.setDialogTitle("Import Image");
        fileGetter.setMultiSelectionEnabled(true);
    }
    void buildFolderGetter() {
        folderGetter.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderGetter.setDialogTitle("Import Folder(s)");
        folderGetter.setMultiSelectionEnabled(true);
    }
    void buildJpgExporter() {
        jpgExporter.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        fileGetter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;//Still want to show directories to browse
                }
                if(f.toString().toLowerCase().endsWith(".jpg")||f.toString().toLowerCase().endsWith(".jpeg")) return true;
                return false;
            }
            public String getDescription() {
                return "Jpg File";
            }
        });
        jpgExporter.setDialogTitle("Export Image as JPG");
        jpgExporter.setSelectedFile(new File("image.jpg"));
        jpgExporter.setMultiSelectionEnabled(false);
    }

    void buildMenuBar() {
        menuBar = new JMenuBar();
        imageMenu = ImageMenu.build((ActionListener) this);
        tagMenu = TagMenu.build((ActionListener) this);
        viewMenu = ViewMenu.build((ActionListener) this);
        helpMenu = HelpMenu.build((ActionListener) this);
        menuBar.add(imageMenu);
        menuBar.add(viewMenu);
        menuBar.add(tagMenu);
        menuBar.add(helpMenu);
    }

    JSlider buildZoomBar() {
        zoomBar = new JSlider(JSlider.HORIZONTAL, 0, 300, 0);
        zoomBar.setMajorTickSpacing(100);
        zoomBar.setMinorTickSpacing(20);
        zoomBar.setPaintLabels(true);
        zoomBar.setPaintTicks(true);
        zoomBar.setFocusable(false);//Otherwise arrows zoom when they shouldn't
        zoomBar.addChangeListener(this);
        return zoomBar;
    }

    void quickRestart(){
        settings = new Settings();
        thumbPath = new File(settings.getSetting("homeDir") + settings.getSetting("thumbnailPathExt"));
        state = new ProgramState(this);//Also initializes mainImageDB
        mainPanel = new MainPanel(this);
        mainPanel.setCursorMode(DragMode.None);

        thumbPanel = new ThumbPanel(this);
        thumbPanel.setVisible(true);

        toolbarMain = ToolBar.build(this);
        toolbarMain.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().toLowerCase().equals("orientation")){
                    zoomBar.setOrientation((Integer)(evt.getNewValue()));
                }
            }
        });

        tagTree = new TagTree(mainImageDB,this);
        //TagTree.setMinimumSize(new Dimension(150,0));
        //Put tagTree in a scrollPane for when more tags exist than it can vertically handle.

        mainScrollPane = new JScrollPane(mainPanel);
        //mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getViewport().setBackground(Color.darkGray);//comment out to see scroll bar bug
        mainScrollPane.getViewport().addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                mainScrollPane.getViewport().repaint();
            }
        });
        //mainScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        mainScrollPane.setPreferredSize(mainPanel.getPreferredSize());
        mainScrollPane.setWheelScrollingEnabled(false);

        imageAreas = new JPanel();
        imageAreas.setLayout(new BorderLayout());
        imageAreas.add(mainScrollPane, BorderLayout.CENTER);
        imageAreas.add(thumbPanel, BorderLayout.PAGE_END);

        splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tagTree, imageAreas);
        splitpane.setOneTouchExpandable(true);
        splitpane.setDividerLocation(tagTreeStartSize + splitpane.getInsets().left);
        splitpane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(splitpane.isValid()){
                    if(evt.getPropertyName().toLowerCase().equals("dividerlocation")){
                        if(splitpane.getDividerLocation()>tagTreeMaxSize) splitpane.setDividerLocation(tagTreeMaxSize);
                    }
                    if (mainPanel.isValid()) {
                        mainPanel.onResize();
                        thumbPanel.onResize();
                    }
                }
            }
        });
        //splitpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		notePane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
		contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitpane, BorderLayout.CENTER);//contentPane.add(mainPanel);
        contentPane.add(toolbarMain, BorderLayout.PAGE_START);
		contentPane.add(notePane, BorderLayout.EAST);

        adjuster = new ImageAdjuster(w,true);
        adjuster.addChangeListeners( new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                state.getCurrentImage().brightness = adjuster.getCurrentSliderBright();
                state.getCurrentImage().contrast = adjuster.getCurrentSliderContrast();
                state.getCurrentImage().isInverted = adjuster.getCurrentInvertBox();
                state.imageColoursUpdated();
            }
        });

        optionsGUI = new OptionsGUI(w,true);
        optionsGUI.setAllValues(settings);

        tagTagger = new TagTagger(w,true);
        quickTagger = new QuickTagger(w,true);

        w.setContentPane(contentPane);
        w.addWindowStateListener(this);
        w.pack();
        state.imageChanged();
        contentPane.addComponentListener(this);//don't want it to trigger while building

        //Set positions. Should be done upon popup,show or set visable instead.
        adjuster.setLocationRelativeTo(w);
        optionsGUI.setLocationRelativeTo(w);
        quickTagger.setLocationRelativeTo(w);
        tagTagger.setLocationRelativeTo(w);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("mRestart")) {
            toggleThumbs(true);
            toggleZoomed(true);
            quickRestart();
        } else if (ae.getActionCommand().equals("mImport")) importDo();
        else if (ae.getActionCommand().equals("mImportD")) importDirDo();
        else if (ae.getActionCommand().equals("ThumbsS")) toggleThumbs(true);
        else if (ae.getActionCommand().equals("ThumbsH")) toggleThumbs(false);
        else if (ae.getActionCommand().equals("TagTree")) toggleTagTree();
        else if (ae.getActionCommand().equals("SlideP")) toggleSlide(true);
        else if (ae.getActionCommand().equals("SlideS")) toggleSlide(false);
        else if (ae.getActionCommand().equals("ZoomFit")) toggleZoomed(true);
        else if (ae.getActionCommand().equals("Zoom100")) zoomTo(100);
        else if (ae.getActionCommand().equals("ZoomX")) zoomBox();
        else if (ae.getActionCommand().equals("Next")) state.nextImage();
        else if (ae.getActionCommand().equals("Prev")) state.prevImage();
        else if (ae.getActionCommand().equals("AddTag")) addTag();
        else if (ae.getActionCommand().equals("TagThis")) tagThis();
        else if (ae.getActionCommand().equals("QuickTag")) quickTag();
        else if (ae.getActionCommand().equals("TagFilter")) tagFilter();
        else if (ae.getActionCommand().equals("TagTag")) tagTag();
        else if (ae.getActionCommand().equals("DragPan")) mainPanel.setCursorMode(mainPanel.getCurrentDrag());
        else if (ae.getActionCommand().equals("DragLink")) mainPanel.setCursorMode(DragMode.Link);
        else if (ae.getActionCommand().equals("DragNote")) mainPanel.setCursorMode(DragMode.Note);
        else if (ae.getActionCommand().equals("BlueT")) bluetoothDo();
        else if (ae.getActionCommand().equals("AdjustImage")) showImageAdjuster();
        else if (ae.getActionCommand().equals("ExportCurrentImg")) exportCurrentImage();
        else if (ae.getActionCommand().equals("Options")) showOptions();
        else if (ae.getActionCommand().equals("Exit")) {
            System.exit(0);
        } else if (ae.getActionCommand().equals("Help")) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w, "Visit http://www.studybuddy.com for help and tutorials", "Study Help", JOptionPane.INFORMATION_MESSAGE, SysIcon.Info.Icon);
        } else if (ae.getActionCommand().equals("About")) {
            JOptionPane.showMessageDialog(w, "StudyBuddy by Team StudyBuddy", "About StudyBuddy", JOptionPane.INFORMATION_MESSAGE, SysIcon.Help.Icon);
        } else{
            log.print(LogType.Error,"ActionEvent " + ae.getActionCommand() + " was not dealt with,\nand had prameter string " + ae.paramString());
        }
        //+ ",\nwith source:\n\n " + e.getSource());
    }

    public void windowStateChanged(WindowEvent e) {
        mainPanel.onResize();
        thumbPanel.onResize();
    }

    public void componentResized(ComponentEvent e) {
        // if(e.getSource()==boardScroll) {
        //if(e.getSource()==mainPanel) {
        //**//log.print(LogType.Error,e.paramString());
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

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void stateChanged(ChangeEvent e) {
        //ZoomBar
        JSlider src = (JSlider) e.getSource();
        if (!src.getValueIsAdjusting()) {
            int zoom = (int) src.getValue();
            if (zoom == 0) {
                toggleZoomed(true);
            } else {
                zoomTo(zoom);
            }
        }
    }

    void toggleThumbs(boolean makeVisible) {//true to show
        thumbPanel.setVisible(makeVisible);

        ViewMenu.ShowThumbs.setVisible(!makeVisible);
        ViewMenu.HideThumbs.setVisible(makeVisible);
        ToolBar.bThumbsS.setVisible(!makeVisible);
        ToolBar.bThumbsH.setVisible(makeVisible);

        mainPanel.onResize();
    }
    void toggleTagTree() {//true to show
        //boolean makeVisible =
        if (splitpane.getDividerLocation() < 2) {
            int x = splitpane.getLastDividerLocation();
            if (x < 50) {
                x = tagTreeStartSize;
            }
            splitpane.setDividerLocation(x);
        } else {
            splitpane.setDividerLocation(1);
        }
        mainPanel.onResize();
        thumbPanel.onResize();
    }

    void toggleZoomed(boolean makeFit) {//true to set zoom to fit
        mainPanel.setZoomed(!makeFit);
        if (makeFit) {
            zoomBar.setValueIsAdjusting(true);//dont want to fire event
            zoomBar.setValue(0);
            //zoomBar.setValueIsAdjusting(false);//Setting this false also fires event
        } else {
            zoomBar.setValueIsAdjusting(true);//dont want to fire event though
            zoomBar.setValue((int) (mainPanel.getZoomMult() * 100));
            //zoomBar.setValueIsAdjusting(false);//Setting this false also fires event
        }

        ViewMenu.ZoomToFit.setVisible(!makeFit);
        ViewMenu.ZoomTo100.setVisible(makeFit);
        ToolBar.bZoomFit.setVisible(!makeFit);
        ToolBar.bZoomMax.setVisible(makeFit);

        mainPanel.onResize();
    }

    void zoomBox() {
        double percent = 100;
        String[] options = {"Fit", "25", "50", "75", "100", "200", "500"};
        String value = (String) JOptionPane.showInputDialog(w, "Enter percentage zoom:", "Set Zoom",
                JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, options, null);
        if (value!=null){
            if (value.toLowerCase().equals("Fit".toLowerCase())) {
                toggleZoomed(true);
            } else {
                percent = Double.parseDouble(value);
                //catch num format exception
                //deal with non number characters? e.g. '%'
                // deal with blank input
                //make editable
                zoomTo(percent);
            }
        }
    }

    void zoomTo(double percent) {
        double oldZoom = mainPanel.getZoomMult();
        int width = mainScrollPane.getViewport().getExtentSize().width;
        int hight = mainScrollPane.getViewport().getExtentSize().height;
        int xpos = mainScrollPane.getViewport().getViewPosition().x+(width/2);
        int ypos = mainScrollPane.getViewport().getViewPosition().y+(hight/2);

        mainPanel.setZoomMult(percent / 100);
        toggleZoomed(false);

        double zoomFactor = mainPanel.getZoomMult()/oldZoom;
        int newX = (int)(xpos*zoomFactor);
        int newY = (int)(ypos*zoomFactor);
        Rectangle r = mainScrollPane.getViewport().getViewRect();
        r.translate(newX-xpos,newY-ypos);
        mainPanel.scrollRectToVisible(r);
    }

    void toggleSlide(boolean setPlaying) {//true to start playing
        if (setPlaying) {
            slideThread.start();
        } else {
            slideThread.interrupt();
            slideThread = new Thread(new SlideShow(this,settings.getSettingAsInt("slideShowTime")));
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
                state.imageChanged();
            } else {
                state = new ProgramState(LoadType.Filter, this, FilterTagIDTitle.getID()); //flush first?
                state.imageChanged();
            }
        }
        tagTree.updateTags();
    }

    void tagThis() {
        Object[] AllTags = mainImageDB.getTagIDTitles();
        Object NewTag = JOptionPane.showInputDialog(w, "Which tag would you like to add to this image?", "Add Tag to image",
                JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, AllTags, null);
        if ((NewTag != null) && (NewTag instanceof IDTitle)) {
            IDTitle NewTagIDTitle = (IDTitle) NewTag;
            mainImageDB.tagImage(state.getCurrentImageID(), NewTagIDTitle.getID());
        }
        tagTree.updateTags();
    }
    void tagTag() {
        tagTagger.loadAllTags(mainImageDB.getTagIDTitles());
        tagTagger.setVisible(true);
        if(tagTagger.getReturnStatus()==TagTagger.RET_OK){
            Object ChildTag = tagTagger.getChildT();
            Object ParentTag = tagTagger.getParentT();
            if ((ChildTag != null) && (ChildTag instanceof IDTitle) && (ParentTag != null) && (ParentTag instanceof IDTitle)) {
                String ParentTagID = ((IDTitle) ParentTag).getID();
                String ChildTagID= ((IDTitle) ChildTag).getID();
                if(!ParentTagID.equals(ChildTagID)) mainImageDB.tagTag(ChildTagID,ParentTagID);

            }
        }
        tagTree.updateTags();
    }

    // TODO: change so image names, or "title:DSA Notes page 73" style name used, but ID returned. Use IDTitle?
    // TODO: add option to show thumbnails instad of text list.
    void quickTag() {
        quickTagger.loadAllTags(mainImageDB.getTagIDTitles(),mainImageDB.getAllImageIDs());
        quickTagger.setVisible(true);
        if(quickTagger.getReturnStatus()==TagTagger.RET_OK){
            Object[] SelectedImages = quickTagger.getSelctedImages();
            Object NewTag = quickTagger.getSelectedTag();
            if ((NewTag != null) && (NewTag instanceof IDTitle)) {
                IDTitle NewTagIDTitle = (IDTitle) NewTag;
                for(Object Img: SelectedImages){
                    mainImageDB.tagImage(Img.toString(), NewTagIDTitle.getID());
                }
            }
        }
        tagTree.updateTags();
    }

    void exportCurrentImage() {
        int destReady = fileGetter.showOpenDialog(w);
        if (destReady == JFileChooser.APPROVE_OPTION) {
            state.getCurrentImage().saveFullToPath(((fileGetter.getSelectedFiles())[0]).toString());
        }
    }
    void importDo() {
        int wasGot = fileGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            state.importImages(fileGetter.getSelectedFiles());
        }
    }
    void importDirDo() {
        int wasGot = folderGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            state.importImages(folderGetter.getSelectedFiles());
        }
    }

    // Add a tag to the database and update tree
    void addTag() {
        String newTag = (String) JOptionPane.showInputDialog(w, "Name of new Tag", "Create Tag", JOptionPane.PLAIN_MESSAGE, null, null, "");
        String newTagID;
        // Check user inputted tag name is valid
        if ((newTag != null) && (newTag.length() > 0)) {
            // Add the new tag into the tag table
            newTagID = mainImageDB.addTag(newTag);
            if (newTagID != null) {
                tagTree.addTagToTree(newTagID,newTag);
            }
        }
        tagTree.updateTags();
    }

    // Delete a tag from the database from a selection on a tag tree
    void deleteTagFromTree() {
        DefaultMutableTreeNode NodeToDel;
        IDTitle NodeToDelObject = null;
        boolean IsRoot = false;
        // Find the currently selected node in the tree
        if (tagTree.getSelectionPath() == null) {
            IsRoot = true;
        } else {
            NodeToDel = (DefaultMutableTreeNode) tagTree.getLastSelectedPathComponent();
            NodeToDelObject = (IDTitle) NodeToDel.getUserObject();
            if (NodeToDelObject.getID().equals("-1")) {
                IsRoot = true;
            }
        }
        if (IsRoot == false) {
            mainImageDB.deleteTag(NodeToDelObject.getID());
        }
        tagTree.updateTags();
    }

    void showImageAdjuster(){
//        int oldBr = state.getCurrentImage().brightness;
//        int oldCr = state.getCurrentImage().contrast;
//        boolean oldInv = state.getCurrentImage().isInverted;
        adjuster.popup();
        state.getCurrentImage().brightness = adjuster.getBrightness();
        state.getCurrentImage().contrast = adjuster.getContrast();
        state.getCurrentImage().isInverted = adjuster.isInverted();
        if(adjuster.shouldReset()){
            state.imageColoursReset();
        }// else if((state.getCurrentImage().brightness !=oldBr)||(state.getCurrentImage().contrast!=oldCr)||(state.getCurrentImage().isInverted!=oldInv)){
            state.imageColoursUpdated();//Now always needed as preview may have changed values
        //}
    }
    void showOptions(){
        optionsGUI.setAllValues(settings);
        optionsGUI.setVisible(true);
        if(optionsGUI.getReturnStatus()==OptionsGUI.RET_OK){
            optionsGUI.saveAllValues(settings);
        }

    }
}
