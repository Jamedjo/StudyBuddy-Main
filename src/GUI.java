import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
//import javax.swing.ScrollPaneLayout;

//We should use javadoc.


//TODO: make amount scrolled by scroll wheel relative to current size.
//At the moment it is linear i.e. zoooming out from 500% goes to
//480% and zooming out from 40% goes to 20% both with same difference

//Refresh image feature?

//Should be seperated into intial thread, and an event dispatch thread which implements the listeners.
class GUI implements ActionListener, ComponentListener, WindowStateListener, ChangeListener {
    Settings settings;
    JFrame w;
    JMenuBar menuBar;
    JMenu imageMenu, viewMenu, tagMenu, helpMenu;
    JMenuItem Options;
    JButton bSideBar;
    final JFileChooser fileGetter = new JFileChooser();
    MainPanel mainPanel;
    ThumbPanel thumbPanel;
    JToolBar toolbarMain;
    JScrollPane boardScroll;
    volatile ProgramState state;
    JOptionPane tagBox;
    ImageDatabase mainImageDB;
    JTree TagTree;
    Thread slideThread;
    JScrollPane mainScrollPane;
    JPanel imageAreas;
    JSlider zoomBar;
    //boolean isChangingState = false;

    public static void main(String[] args) {
        GUI mainGUI = new GUI();
    }

    void setTitle() {
        setTitle(null);
    }

    void setTitle(String suffix) {
        String prefix = "Study Buddy 0.7beta";
        if (suffix == null) suffix = "";
        else prefix = prefix.concat("- ");
        w.setTitle(prefix + suffix);

    }

    GUI() {
        w = new JFrame();
        setTitle();
        w.setDefaultCloseOperation(w.EXIT_ON_CLOSE);
        buildMenuBar();
        w.setJMenuBar(menuBar);
        w.setLocationByPlatform(true);
        w.setIconImage(SysIcon.Logo.Icon.getImage());
        //w.addComponentListener(this);
        buildFileGetter();
        quickRestart();
        //w.setDefaultLookAndFeelDecorated(false);
        w.setVisible(true);
        slideThread = new Thread(new SlideShow(this,settings.getSettingAsInt("slideShowTime")));
    }

    void buildFileGetter() {
        fileGetter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String[] exts = {"jpeg", "jpg", "gif", "bmp", "png", "tiff", "tif", "tga", "pcx", "xbm", "svg"};
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

            public String getDescription() {
                return "All Images";
            }
        }
        );
        fileGetter.setDialogTitle("Import Image");
        fileGetter.setMultiSelectionEnabled(true);

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
        zoomBar.addChangeListener(this);
        return zoomBar;
    }

    void quickRestart(){
        settings = new Settings();
	state = new ProgramState(this);
	mainPanel = new MainPanel(this);

        thumbPanel = new ThumbPanel(this);
        thumbPanel.setVisible(true);

        toolbarMain = ToolBar.build(this);

        TagTree = mainImageDB.toTree();
        //TagTree.setMinimumSize(new Dimension(150,0));
        TagTree.addTreeSelectionListener(new TagTreeListener(this));

        mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.getViewport().setBackground(Color.darkGray);//comment out to see scroll bar bug
        mainScrollPane.setPreferredSize(mainPanel.getPreferredSize());
        mainScrollPane.setWheelScrollingEnabled(false);

        imageAreas = new JPanel();
        imageAreas.setLayout(new BorderLayout());
        imageAreas.add(mainScrollPane, BorderLayout.CENTER);
        imageAreas.add(thumbPanel, BorderLayout.PAGE_END);

        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, TagTree, imageAreas);
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
        state.imageChanged();
        contentPane.addComponentListener(this);//don't want it to trigger while building
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("mRestart")) {
            toggleThumbs(true);
            toggleZoomed(true);
            quickRestart();
        } else if (ae.getActionCommand().equals("mImport")) importDo();
        else if (ae.getActionCommand().equals("ThumbsS")) toggleThumbs(true);
        else if (ae.getActionCommand().equals("ThumbsH")) toggleThumbs(false);
        else if (ae.getActionCommand().equals("SlideP")) toggleSlide(true);
        else if (ae.getActionCommand().equals("SlideS")) toggleSlide(false);
        else if (ae.getActionCommand().equals("ZoomFit")) toggleZoomed(true);
        else if (ae.getActionCommand().equals("Zoom100")) zoomTo(100);
        else if (ae.getActionCommand().equals("ZoomX")) zoomBox();
        else if (ae.getActionCommand().equals("Next")) state.nextImage();
        else if (ae.getActionCommand().equals("Prev")) state.prevImage();
        else if (ae.getActionCommand().equals("AddTag")) {
            TagTree = mainImageDB.addTagFromTree(TagTree, w);
            TagTree.repaint();
        } else if (ae.getActionCommand().equals("TagThis")) tagThis();
        else if (ae.getActionCommand().equals("TagFilter")) tagFilter();
        else if (ae.getActionCommand().equals("BlueT")) bluetoothDo();
        else if (ae.getActionCommand().equals("Exit")) {
            System.exit(0);
        } else if (ae.getActionCommand().equals("Help")) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(w, "Visit http://www.studybuddy.com for help and tutorials", "Study Help", JOptionPane.INFORMATION_MESSAGE, SysIcon.Info.Icon);
        } else if (ae.getActionCommand().equals("About")) {
            JOptionPane.showMessageDialog(w, "StudyBuddy by Team StudyBuddy", "About StudyBuddy", JOptionPane.INFORMATION_MESSAGE, SysIcon.Help.Icon);
        } else
            System.err.println("ActionEvent " + ae.getActionCommand() + " was not dealt with,\nand had prameter string " + ae.paramString());
        //+ ",\nwith source:\n\n " + e.getSource());
    }

    public void windowStateChanged(WindowEvent e) {
        mainPanel.onResize();
        thumbPanel.onResize();
    }

    public void componentResized(ComponentEvent e) {
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

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void stateChanged(ChangeEvent e) {
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

    void toggleZoomed(boolean makeFit) {//true to set zoom to fit
        mainPanel.isZoomed = (!makeFit);
        if (makeFit) {
            zoomBar.setValueIsAdjusting(true);//dont want to fire event
            zoomBar.setValue(0);
            //zoomBar.setValueIsAdjusting(false);//Setting this false also fires event
        } else {
            zoomBar.setValueIsAdjusting(true);//dont want to fire event though
            zoomBar.setValue((int) (mainPanel.zoomMultiplier * 100));
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

    void zoomTo(double percent) {
        mainPanel.zoomMultiplier = (percent / 100);
        toggleZoomed(false);
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
    }

    void tagThis() {
        Object[] AllTags = mainImageDB.getTagIDTitles();
        Object NewTag = JOptionPane.showInputDialog(w, "Which tag would you like to add to this image?", "Add Tag to image",
                JOptionPane.PLAIN_MESSAGE, SysIcon.Question.Icon, AllTags, null);
        if ((NewTag != null) && (NewTag instanceof IDTitle)) {
            IDTitle NewTagIDTitle = (IDTitle) NewTag;
            mainImageDB.tagImage(state.getCurrentImageID(), NewTagIDTitle.getID());
        }
    }

    void importDo() {
        int wasGot = fileGetter.showOpenDialog(w);
        if (wasGot == JFileChooser.APPROVE_OPTION) {
            state.importImages(fileGetter.getSelectedFiles());
        }
    }
}
