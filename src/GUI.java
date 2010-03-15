
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
    Settings settings = new Settings();;
    JFrame w;
    private volatile ProgramState state;
    ImageDatabase mainImageDB;
    
    ImageAdjuster adjuster;
    OptionsGUI optionsGUI;
    TagTagger tagTagger;
    QuickTagger quickTagger;
    Thread slideThread;

    final JFileChooser fileGetter = new JFileChooser();
    final JFileChooser folderGetter = new JFileChooser();
    final JFileChooser jpgExporter = new JFileChooser();
    JMenuBar menuBar;
    JMenu imageMenu, viewMenu, tagMenu, helpMenu;
    JMenuItem Options;
    JButton bSideBar;
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JToolBar imageToolbar;
    JScrollPane boardScroll;
    JOptionPane tagBox;
    TagTree tagTree;
    JScrollPane mainScrollPane;
    JSplitPane splitpane;
    //JPanel rightArea;
    JPanel contentPane;
    JPanel imageAreas;
    JScrollPane notePane;
    JSlider zoomBar;
    //File thumbPath;
    final int tagTreeStartSize = 150;
    final int tagTreeMaxSize = 350;
    //boolean isChangingState = false;

    ProgramState getState(){
        return state;
    }
    void setState(ProgramState newstate){
        ProgramState tempstate = state;
        state = newstate;
        tempstate.safelyDestruct();
    }

    public static void main(String[] args) {
        //GUI mainGUI =
        new GUI();
    }

    void setTitle() {
        setTitle(null);
    }

    void setTitle(String suffix) {
        String prefix = "Study Buddy 0.9.2";
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
        setupCache();
        Thread errorImgsThread = new Thread(new ErrorImages(100,this));
        quickRestart();
        errorImgsThread.start();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        w.setMinimumSize(new Dimension(200,200));
        //slideThread = new Thread(new SlideShow(this,settings.getSettingAsInt("slideShowTime")));
    }

    void setupCache() {
        ImageIO.setUseCache(true);
        String cachePath = settings.getSetting("homeDir") + settings.getSetting("cachePathExt");
        if ((cachePath != null) && (cachePath.length() > 1)) {
            File cacheDir = new File(cachePath);
            if (cacheDir.isDirectory()) {
                ImageIO.setCacheDirectory(cacheDir);
            }
        }
    }

    boolean isImage(File f){
        String[] exts = {"jpeg", "jpg", "gif", "bmp", "png", "tiff", "tif", "tga", "pcx", "xbm", "svg","wbmp"};
        //String[] readerNames = ImageIO.getReaderFormatNames();
        //Sanselan.hasImageFileExtension();
        String ext = null;
        ext = ImageUtils.getFileExtLowercase(f.getName());
        if (ext==null) return false;
        for (String imgExt : exts) {
            if (ext.equals(imgExt)) {
                return true;
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
        setGetterDir(fileGetter);
        fileGetter.setMultiSelectionEnabled(true);
    }
    void setGetterDir(JFileChooser getter){
        String lastSetDir = settings.getSetting("lastOpenDirectory");
        if((lastSetDir==null)||lastSetDir.equals("")) return;
        File lastDir = new File(lastSetDir);
        if((lastDir!=null)&&lastDir.exists()&&lastDir.isDirectory()){
            getter.setCurrentDirectory(lastDir);
        }
    }
    void buildFolderGetter() {
        folderGetter.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderGetter.setDialogTitle("Import Folder(s)");
        setGetterDir(folderGetter);
        folderGetter.setMultiSelectionEnabled(true);
    }
    void buildJpgExporter() {
        //jpgExporter.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        jpgExporter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
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
        setGetterDir(jpgExporter);
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
        state = new ProgramState(this);//Also initializes mainImageDB //Only time that state = should be used out side of setState()
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
        imageToolbar = ImageToolBar.build(this);

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

        notePane = new JScrollPane();

//        rightArea = new JPanel();
//        rightArea.setLayout(new BoxLayout(rightArea,BoxLayout.Y_AXIS));
//        rightArea.setMinimumSize(new Dimension(100,100));

        JPanel toolBarArea = new JPanel();
        toolBarArea.setLayout(new BorderLayout());
        toolBarArea.add(toolbarMain,BorderLayout.CENTER);
        toolBarArea.add(imageToolbar,BorderLayout.PAGE_END);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(splitpane, BorderLayout.CENTER);//contentPane.add(mainPanel);
        contentPane.add(toolBarArea, BorderLayout.PAGE_START);
        contentPane.add(notePane, BorderLayout.LINE_END);

        adjuster = new ImageAdjuster(w,true);
        adjuster.addChangeListeners( new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                getState().getCurrentImage().replaceFilter(adjuster.getCurrentInvertBox(), adjuster.getCurrentSliderContrast(),adjuster.getCurrentSliderBright());
                getState().imageColoursUpdated();
            }
        });

        optionsGUI = new OptionsGUI(w,true);
        optionsGUI.setAllValues(settings);

        tagTagger = new TagTagger(w,true);
        quickTagger = new QuickTagger(w,true);

        w.setContentPane(contentPane);
        w.addWindowStateListener(this);
        w.pack();
        getState().imageChanged();
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
        else if (ae.getActionCommand().equals("Next")) getState().nextImage();
        else if (ae.getActionCommand().equals("Prev")) getState().prevImage();
        else if (ae.getActionCommand().equals("AddTag")) addTag();
        else if (ae.getActionCommand().equals("TagThis")) tagThis();
        else if (ae.getActionCommand().equals("QuickTag")) quickTag();
        else if (ae.getActionCommand().equals("ImageBar")) imageToolbarToggle();
        else if (ae.getActionCommand().equals("TagFilter")) tagFilter();
        else if (ae.getActionCommand().equals("TagTag")) tagTag();
        else if (ae.getActionCommand().equals("DragPan")) mainPanel.setCursorMode(mainPanel.getCurrentDrag());
        else if (ae.getActionCommand().equals("DragLink")) dragLink();
        else if (ae.getActionCommand().equals("DragNote")) mainPanel.setCursorMode(DragMode.Note);
        else if (ae.getActionCommand().equals("BlueT")) bluetoothDo();
        else if (ae.getActionCommand().equals("AdjustImage")) showImageAdjuster();
        else if (ae.getActionCommand().equals("Flip")) {state.getCurrentImage().img.transform.flip(); mainPanel.onResize();}
        else if (ae.getActionCommand().equals("Mirror")){ state.getCurrentImage().img.transform.mirror(); mainPanel.onResize();}
        else if (ae.getActionCommand().equals("Rotate")) {state.getCurrentImage().img.transform.rotate90(); mainPanel.onResize();}
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
	
	void dragLink()
	{
		Record TempRecord;
		String DummyLinkID;
		if (ImageLinker.getSelectingImage())
		{
			DummyLinkID = ImageLinker.getDummyLinkID();
			TempRecord = mainImageDB.getLink(DummyLinkID);
			mainImageDB.deleteLink(DummyLinkID);
			mainImageDB.linkImage(TempRecord.getField(1), getState().getCurrentImageID(), Integer.parseInt(TempRecord.getField(3)), Integer.parseInt(TempRecord.getField(4)), Integer.parseInt(TempRecord.getField(5)), Integer.parseInt(TempRecord.getField(6)));
			JOptionPane.showMessageDialog(w, "Images Linked!");
			mainPanel.repaint();
			ImageLinker.setSelectingImage(false);
		}
		else
		{
			mainPanel.setCursorMode(DragMode.Link);
		}
	
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
            slideThread = new Thread(new SlideShow(this,settings.getSettingAsInt("slideShowTime")));
            slideThread.start();
        } else {
            slideThread.interrupt();
        }

        ViewMenu.SlidePlay.setVisible(!setPlaying);
        ViewMenu.SlideStop.setVisible(setPlaying);
        ToolBar.bSlideP.setVisible(!setPlaying);
        ToolBar.bSlideS.setVisible(setPlaying);

        mainPanel.onResize();
    }

    void bluetoothDo() {
        BluetoothGUI blueGUI = new BluetoothGUI(w,true);
        blueGUI.setVisible(true);
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
                setState(new ProgramState(LoadType.Refresh, this));
                getState().imageChanged();
            } else {
                setState(new ProgramState(LoadType.Filter, this, FilterTagIDTitle.getID()));
                getState().imageChanged();
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
            mainImageDB.tagImage(getState().getCurrentImageID(), NewTagIDTitle.getID());
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
        String[] ids = mainImageDB.getAllImageIDs();
        String[][] idTitleTable = new String[ids.length][2];
        for(int i=0;i<ids.length;i++){
            idTitleTable[i][0] = ids[i];
            idTitleTable[i][1] = mainImageDB.getImageFilename(ids[i]);
        }
        quickTagger.loadAllTags(mainImageDB.getTagIDTitles(),idTitleTable);
        quickTagger.setVisible(true);
        if(quickTagger.getReturnStatus()==TagTagger.RET_OK){
            Object[] SelectedImages = quickTagger.getSelctedImageIDs();
            Object NewTag = quickTagger.getSelectedTag();
            if ((NewTag == null) || !(NewTag instanceof IDTitle)) return;
            if(SelectedImages.length<1) return;

                String tagID = ((IDTitle)NewTag).getID();
                for(Object Img: SelectedImages){
                    mainImageDB.tagImage(Img.toString(), tagID);
                }
                if(tagID.equals(getState().currentFilter)){
                    setState(new ProgramState(LoadType.Filter, this, tagID));
                    mainPanel.onResize();
                    thumbPanel.onResize();
                }
        }
        tagTree.updateTags();
    }

    void exportCurrentImage() {
        int destReady = jpgExporter.showOpenDialog(w);
        if (destReady == JFileChooser.APPROVE_OPTION) {
            String filePathAndName = jpgExporter.getSelectedFile().toString();
            String ext = ImageUtils.getFileExtLowercase(filePathAndName);
            if((ext==null)||(!(ext.equals("jpg")||ext.equals("jpeg")))) filePathAndName = filePathAndName + ".jpg";
            settings.setSettingAndSave("lastOpenDirectory",jpgExporter.getCurrentDirectory().toString());
            getState().getCurrentImage().saveFullToPath(filePathAndName);
        }
    }
    void importDo() {
        int wasGot = fileGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            settings.setSettingAndSave("lastOpenDirectory",fileGetter.getCurrentDirectory().toString());
            getState().importImages(fileGetter.getSelectedFiles());
        }
    }
    void importDirDo() {
        int wasGot = folderGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            settings.setSettingAndSave("lastOpenDirectory",folderGetter.getCurrentDirectory().toString());
            getState().importImages(folderGetter.getSelectedFiles());
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
        getState().getCurrentImage().replaceFilter(adjuster.isInverted(), adjuster.getContrast(),adjuster.getBrightness());
        if(adjuster.shouldReset()){
            getState().imageColoursReset();
        }
    }
    void showOptions(){
        optionsGUI.setAllValues(settings);
        optionsGUI.setVisible(true);
        if(optionsGUI.getReturnStatus()==OptionsGUI.RET_OK){
            optionsGUI.saveAllValues(settings);
        }
        mainPanel.onResize();
    }
    void imageToolbarToggle(){
        imageToolbar.setVisible(!imageToolbar.isVisible());
        w.validate();
        mainPanel.onResize();
    }
}
