
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;

//We should use javadoc.

//Refresh image feature?

class GUI {
    Log log;
    JFrame w;
    private volatile ProgramState state;
    ImageDatabase mainImageDB;
    
    ImageAdjuster adjuster;
    OptionsGUI optionsGUI;
    TagTagger tagTagger;
    QuickTagger quickTagger;
    Thread slideThread;

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
    GuiListener guiListener = new GuiListener(this);
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
        String prefix = "Study Buddy 1.00";
        if (suffix == null) suffix = "";
        else prefix = prefix.concat("  ");
        w.setTitle(prefix + suffix);

    }

    GUI() {
        log = new Log();
        Settings.init();
        //try {
        //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //} catch (Exception e) {
        //    log.print(LogType.Debug, "Unable to use system look&feel");
        //}
        RepaintManager.initMain(this);
        w = new JFrame();
        setTitle();
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildMenuBar();
        w.setJMenuBar(menuBar);
        w.setLocationByPlatform(true);
        w.setIconImage(SysIcon.Logo.Icon.getImage());
        //w.addComponentListener(this);
        FileDialogs.init(this);
        setupCache();
        Thread errorImgsThread = new Thread(new ErrorImages(100,this));
        quickRestart();
        errorImgsThread.start();
        //w.setDefaultLookAndFeelDecorated(false);
        w.addComponentListener(guiListener);
        w.setVisible(true);
        w.setMinimumSize(new Dimension(200,200));
    }

    void setupCache() {
        ImageIO.setUseCache(true);
        String cachePath = Settings.getSetting("homeDir") + Settings.getSetting("cachePathExt");
        if ((cachePath != null) && (cachePath.length() > 1)) {
            File cacheDir = new File(cachePath);
            if (cacheDir.isDirectory()) {
                ImageIO.setCacheDirectory(cacheDir);
            }
        }
    }


    void buildMenuBar() {
        menuBar = new JMenuBar();
        imageMenu = ImageMenu.build(guiListener);
        tagMenu = TagMenu.build(guiListener);
        viewMenu = ViewMenu.build(guiListener);
        helpMenu = HelpMenu.build(guiListener);
        menuBar.add(imageMenu);
        menuBar.add(viewMenu);
        menuBar.add(tagMenu);
        menuBar.add(helpMenu);
    }

    void quickRestart(){
        state = new ProgramState(this);//Also initializes mainImageDB //Only time that state = should be used out side of setState()
        mainPanel = new MainPanel(this);
        mainPanel.setCursorMode(DragMode.None);

        thumbPanel = new ThumbPanel(this);
        thumbPanel.setVisible(true);

        toolbarMain = ToolBar.build(this);
        toolbarMain.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
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
            @Override
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

        final JTextField newTagBox = new JTextField();
        class textAddTag implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e){
                //newTagBox.text is used
                //if tag doesn't exist add it
                addTag(newTagBox.getText());
                //tag current image with the tag
                mainImageDB.tagImage(getState().getCurrentImageID(), mainImageDB.getTagIDFromTagTitle(newTagBox.getText()));
                tagTree.updateTags();
                newTagBox.setText("");
            }
        }
        newTagBox.addActionListener(new textAddTag());
        JPanel newTagBar = new JPanel();
        newTagBar.setLayout(new BorderLayout());
        newTagBar.add(newTagBox, BorderLayout.CENTER);
        JButton tagButton =new JButton(SysIcon.TagThis.smallIcon(25));//AddTag.Icon);//JButton("Tag");
        tagButton.addActionListener(new textAddTag());
        tagButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        newTagBar.add(tagButton,BorderLayout.EAST);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(newTagBar, BorderLayout.NORTH);
        leftPanel.add(tagTree, BorderLayout.CENTER);

        splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, imageAreas);
        splitpane.setOneTouchExpandable(true);
        splitpane.setDividerLocation(tagTreeStartSize + splitpane.getInsets().left);
        if(getState().getCurrentImageID().equals("-1")) splitpane.setDividerLocation(1);
        splitpane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(splitpane.isValid()){
                    if(evt.getPropertyName().toLowerCase().equals("dividerlocation")){
                        if(splitpane.getDividerLocation()>tagTreeMaxSize) splitpane.setDividerLocation(tagTreeMaxSize);
                    }
                    if (mainPanel.isValid()) {
                        RepaintManager.repaint(RepaintType.Window);
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
            @Override
            public void stateChanged(ChangeEvent e){
                getState().getCurrentImage().replaceFilter(adjuster.getCurrentInvertBox(), adjuster.getCurrentSliderContrast(),adjuster.getCurrentSliderBright());
                getState().imageColoursUpdated();
            }
        });

        optionsGUI = new OptionsGUI(w,true);
        optionsGUI.setAllValues();

        tagTagger = new TagTagger(w,true);

        w.setContentPane(contentPane);
        w.addWindowStateListener(guiListener);
        w.pack();
        getState().imageChanged();
        contentPane.addComponentListener(guiListener);//don't want it to trigger while building

    }
	
    void dragLink() {
        mainPanel.setCursorMode(DragMode.Link);
    }

    void toggleThumbs(boolean makeVisible) {//true to show
        thumbPanel.setVisible(makeVisible);

        ViewMenu.ShowThumbs.setVisible(!makeVisible);
        ViewMenu.HideThumbs.setVisible(makeVisible);
        ToolBar.bThumbsS.setVisible(!makeVisible);
        ToolBar.bThumbsH.setVisible(makeVisible);

        RepaintManager.repaint(RepaintType.MainPanel);
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
        RepaintManager.repaint(RepaintType.Window);
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
        ZoomButtons.bZoomFit.setVisible(!makeFit);
        ZoomButtons.bZoomMax.setVisible(makeFit);

        RepaintManager.repaint(RepaintType.MainPanel);
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
            slideThread = new Thread(new SlideShow(this,Settings.getSettingAsInt("slideShowTime")));
            slideThread.start();
        } else {
            slideThread.interrupt();
        }

        ViewMenu.SlidePlay.setVisible(!setPlaying);
        ViewMenu.SlideStop.setVisible(setPlaying);
        ToolBar.bSlideP.setVisible(!setPlaying);
        ToolBar.bSlideS.setVisible(setPlaying);

        RepaintManager.repaint(RepaintType.MainPanel);
    }

    void bluetoothDo() {
        BluetoothGUI blueGUI = new BluetoothGUI(w,true,this);
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
        tagTagger.setLocationRelativeTo(w);
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

    // TODO: change so image names, or "title:DSA Notes page 73" style name used when image thumbnail not loaded/file not found.
    void quickTag() {
        quickTagger = new QuickTagger(w,true,this);
        quickTagger.loadAllTags(mainImageDB.getTagIDTitles());
        quickTagger.setLocationRelativeTo(w);
        boolean done=false;
        while(!done){
            quickTagger.setVisible(true);
            if(quickTagger.getReturnStatus()==TagTagger.RET_OK){
                Object[] SelectedImages = quickTagger.getSelctedImageIDs();
                Object NewTag = quickTagger.getSelectedTag();
                if ((NewTag != null) && (NewTag instanceof IDTitle) && !(SelectedImages.length<1)){
                    done = true;
                    String tagID = ((IDTitle)NewTag).getID();
                    for(Object Img: SelectedImages){
                        mainImageDB.tagImage(Img.toString(), tagID);
                    }
                    if(tagID.equals(getState().currentFilter)){
                        setState(new ProgramState(LoadType.Filter, this, tagID));
                        RepaintManager.repaint(RepaintType.Window);
                    }
                }
            } else done=true;
            if(!done) JOptionPane.showMessageDialog(w, "Select one tag and at least one image.");
        }
        tagTree.updateTags();
    }


    // Prompt for name of new tag with dialog box
    void addTag() {
        String newTag = (String) JOptionPane.showInputDialog(w, "Name of new Tag", "Create Tag", JOptionPane.PLAIN_MESSAGE, null, null, "");
        addTag(newTag);
    }

    // Add a tag to the database and update tree
    void addTag(String newTag) {
        String newTagID;
        // Check user inputted tag name is valid
        if ((newTag != null) && (newTag.length() > 0)) {
            //if tag already exists return
            String alreadyExists=mainImageDB.getTagIDFromTagTitle(newTag);
            if(alreadyExists!=null) return;
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
        optionsGUI.setAllValues();
        optionsGUI.setLocationRelativeTo(w);
        optionsGUI.setVisible(true);
        if(optionsGUI.getReturnStatus()==OptionsGUI.RET_OK){
            optionsGUI.saveAllValues();
        }
        RepaintManager.repaint(RepaintType.MainPanel);
    }
    void imageToolbarToggle(){
        imageToolbar.setVisible(!imageToolbar.isVisible());
        w.validate();
        RepaintManager.repaint(RepaintType.MainPanel);
    }

    void deleteCurrentImage(){
        mainImageDB.deleteImage(getState().getCurrentImageID());
        getState().imageDeleted();
    }

    void showHelpGuide(){
        //System.out.println("No Start Here Guide Yet");
    }
}
