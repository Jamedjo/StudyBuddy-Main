import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

//TODO: add dragmodes to a menu: pan,link,note ...... add bluetooth to a menu

// <editor-fold defaultstate="collapsed" desc="ToolBar">
enum ToolBar{
    mImport(false,"Import Image","mImport", SysIcon.Import.Icon),
    mImportD(false, "Import Directory", "mImportD", SysIcon.ImportDir.Icon),
    mExprtImg(false, "Export Current Image", "ExportCurrentImg", SysIcon.Export.Icon),
    bPrev(true, "Prev", "Prev", SysIcon.Prev.Icon),
    bNext(false, "Next", "Next", SysIcon.Next.Icon),
    bSlideP(false, "Slideshow play", "SlideP", SysIcon.Play.Icon),
    bSlideS(false, "Slideshow stop", "SlideS", SysIcon.Stop.Icon, false),
    bThumbsS(true, "Show Thumbnails", "ThumbsS", SysIcon.ShowThumbs.Icon, false),
    bThumbsH(false, "Hide Thumbnails", "ThumbsH", SysIcon.HideThumbs.Icon),
    bTagTree(false, "Show/Hide Tag Tree", "TagTree", SysIcon.JTree.Icon),
    bAddTag(true, "Create Tag", "AddTag", SysIcon.AddTag.Icon),
    bTagThis(false, "Tag This Image", "TagThis", SysIcon.TagThis.Icon),
    bQuickTag(false, "Tag many images", "QuickTag", SysIcon.QuickTag.Icon),
    bTagTag(false, "Tag A Tag", "TagTag", SysIcon.TagTag.Icon),
    bTagFilter(false, "Filter By Tag", "TagFilter", SysIcon.TagFilter.Icon),
    bDragPan(true, "Drag Mode: Pan", "DragPan", SysIcon.DragPan.Icon),
    bDragLink(false, "Drag Mode: Add Link", "DragLink", SysIcon.DragLink.Icon),
    bDragNote(false, "Drag Mode: Add Note", "DragNote", SysIcon.DragNote.Icon),
    bImageToolBar(true, "Toggle Image ToolBar", "ImageBar", SysIcon.ImageBar.Icon),
    bBlueDemo(true, "Bluetooth", "BlueT", SysIcon.BlueTooth.Icon),
    bZoomToX(true, "Zoom Dialog", "ZoomX", SysIcon.ZoomToX.Icon),
    bZoomFit(false, "Zoom: Fit     ", "ZoomFit", SysIcon.ZoomFit.Icon, false),
    bZoomMax(false, "Zoom: 100%", "Zoom100", SysIcon.Zoom100.Icon),
    bOptions(false, "Options", "Options", SysIcon.Options.Icon);

    JButton button;
    ImageIcon icon;
    boolean isSeperatorHere;
    static final boolean putSeperatorAtEnd = false;
    static final int sliderPosFromEnd = 0;//Hom many icons should come after the slider
    
    ToolBar(boolean isNewGroup, String label, String command, ImageIcon ic, boolean visible) {
        icon=ic;
        isSeperatorHere = isNewGroup;
        if (icon != null) {
            button = new JButton(icon);
            button.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            button.setToolTipText(label);
            //button.set text to hidden;
        } else {
            button = new JButton(label);
        }
        button.setActionCommand(command);
        button.setVisible(visible);
    }
    ToolBar(boolean isNewGroup, String label, String command) {
        this(isNewGroup, label, command, true);
    }
    ToolBar(boolean isNewGroup, String label, String command, boolean visible) {
        this(isNewGroup, label, command, null, visible);
    }
    ToolBar(boolean isNewGroup, String label, String command, ImageIcon icon) {
        this(isNewGroup, label, command, icon, true);
    }
    void hide() {
        button.setVisible(false);
    }
    void show() {
        button.setVisible(true);
    }
    void setVisible(boolean value) {
        button.setVisible(value);
    }
    JButton create(ActionListener l) {
        button.addActionListener(l);
        return button;
    }
    static JToolBar build(GUI mainGUI) {
        JToolBar bar = new JToolBar("StudyBuddy Toolbar");
        bar.setFocusable(false);

        int i = 0;
        for (ToolBar b : ToolBar.values()) {
            JButton bt = b.create(mainGUI.guiListener);
            if (b.isSeperatorHere) {
                bar.addSeparator();
            }
            bar.add(bt);
            if (i == ToolBar.values().length - (1 + sliderPosFromEnd)){
                bar.addSeparator();
                bar.add(Box.createHorizontalGlue());
                ToolBar[] zoomButtons= {bZoomFit,bZoomMax};
                bar.add(new ZoomBar(mainGUI,zoomButtons));
            }
            i++;
        }
        if (putSeperatorAtEnd) {
            bar.addSeparator();
        }

        //workaround to prevent toolbar from steeling focus
        for (i = 0; i < bar.getComponentCount(); i++) {
            if (bar.getComponent(i) instanceof JButton) {
                ((JButton) bar.getComponent(i)).setFocusable(false);
            }
        }
        return bar;
    }

}// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="ImageToolBar">
enum ImageToolBar {
    //bDragPan(true, "Drag Mode: Pan", "DragPan", SysIcon.DragPan.Icon),
    //bDragLink(false, "Drag Mode: Add Link", "DragLink", SysIcon.DragLink.Icon),
    //bDragNote(false, "Drag Mode: Add Note", "DragNote", SysIcon.DragNote.Icon),

    bAdjustImage(true, "Adjust Image Colours", "AdjustImage", SysIcon.Adjust.Icon),
    bMirror(true, "Mirror Image", "Mirror", SysIcon.Mirror.Icon),
    bFlip(true, "Flip Image Horizontaly", "Flip", SysIcon.Flip.Icon),
    bRotate(true, "Rotate 90* clockwise", "Rotate", SysIcon.Rotate.Icon);
    JButton button;
    boolean isSeperatorHere;
    static final boolean putSeperatorAtEnd = false;

    ImageToolBar(boolean isNewGroup, String label, String command, ImageIcon icon, boolean visible) {
        isSeperatorHere = isNewGroup;
        if (icon != null) {
            button = new JButton(icon);
            button.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            button.setToolTipText(label);
            //button.set text to hidden;
        } else {
            button = new JButton(label);
        }
        button.setActionCommand(command);
        button.setVisible(visible);
    }

    ImageToolBar(boolean isNewGroup, String label, String command) {
        this(isNewGroup, label, command, true);
    }

    ImageToolBar(boolean isNewGroup, String label, String command, boolean visible) {
        this(isNewGroup, label, command, null, visible);
    }

    ImageToolBar(boolean isNewGroup, String label, String command, ImageIcon icon) {
        this(isNewGroup, label, command, icon, true);
    }

    void hide() {
        button.setVisible(false);
    }

    void show() {
        button.setVisible(true);
    }

    void setVisible(boolean value) {
        button.setVisible(value);
    }

    JButton create(ActionListener l) {
        button.addActionListener(l);
        return button;
    }

    static JToolBar build(GUI mainGUI) {
        JToolBar bar = new JToolBar("Image Toolbar");
        bar.setFocusable(false);
        bar.setFloatable(false);
        bar.setVisible(false);

        int i = 0;
        for (ImageToolBar b : ImageToolBar.values()) {
            JButton bt = b.create(mainGUI.guiListener);
            if (b.isSeperatorHere) {
                bar.addSeparator();//add seperator before positions 0,2&4 in the menu
            }
            bar.add(bt);
            i++;
        }
        if (putSeperatorAtEnd) {
            bar.addSeparator();
        }

        //workaround to prevent toolbar from steeling focus
        for (i = 0; i < bar.getComponentCount(); i++) {
            if (bar.getComponent(i) instanceof JButton) {
                ((JButton) bar.getComponent(i)).setFocusable(false);
            }
        }
        return bar;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="ImageMenu">
enum ImageMenu {

    mImport("Import Image(s)", KeyEvent.VK_I, KeyEvent.VK_I, ActionEvent.CTRL_MASK, "mImport"),
    mImportD("Import Folder", KeyEvent.VK_F, KeyEvent.VK_I, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK, "mImportD"),
    mExportImg("Export Current Image", KeyEvent.VK_E, -1, -1, "ExportCurrentImg"),
    mRemoveI("Remove Image from StudyBuddy", KeyEvent.VK_R, -1, -1, "mRemoveI"),
    mOptions("StudyBuddy Options",KeyEvent.VK_O,-1,-1,"Options"),
    mRestart("Restart Viewer", KeyEvent.VK_V, KeyEvent.VK_N, ActionEvent.CTRL_MASK, "mRestart"),
    mExit("Exit", KeyEvent.VK_X, KeyEvent.VK_W, ActionEvent.CTRL_MASK, "Exit");
    JMenuItem item;

    ImageMenu(String label, int mnemonic, int acceleratorKey, int acceleratorMask, String command) {
        item = new JMenuItem(label, mnemonic);
        if (acceleratorKey != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, acceleratorMask));
        }
        item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    //ImageMenu(String label, String command, boolean visible){
    //this(label,command);
    //item.setVisible(visible);
    //}
    //void hide(){
    //item.setVisible(false);
    //}
    //void show(){
    //item.setVisible(true);
    //}
//    void setVisible(boolean value){
//        item.setVisible(value);
//    }

    JMenuItem create(ActionListener l) {
        item.addActionListener(l);
        return item;
    }

    static JMenu build(ActionListener l) {
        JMenu menu = new JMenu("Image");
        menu.setMnemonic(KeyEvent.VK_I);
        int i = 0;
        for (ImageMenu iTM : ImageMenu.values()) {
            JMenuItem itm = iTM.create(l);
            menu.add(itm);
            i++;
        }
        return menu;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="TagMenu">
enum TagMenu {
    AddTag("Create new tag", KeyEvent.VK_N, -1, -1, "AddTag"),
    DeleteTag("Delete a tag", KeyEvent.VK_D, -1, -1, "DeleteTag"),
    TagThis("Tag this Image", KeyEvent.VK_T, KeyEvent.VK_T, 0, "TagThis"),
    QuickTag("QuickTag Images", KeyEvent.VK_Q, KeyEvent.VK_Q, 0, "QuickTag"),
    TagTag("Tag a Tag", KeyEvent.VK_A, -1, -1, "TagTag"),
    TagFilter("Filter Images by Tag", KeyEvent.VK_F, -1, -1, "TagFilter");
    JMenuItem item;
    TagMenu(String label, int mnemonic, int acceleratorKey, int acceleratorMask, String command) {
        item = new JMenuItem(label, mnemonic);
        if (acceleratorKey != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, acceleratorMask));
        }
        item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    //TagMenu(String label, String command, boolean visible){
    //this(label,command);
    //item.setVisible(visible);
    //}
    //void hide(){
    //item.setVisible(false);
    //}
    //void show(){
    //item.setVisible(true);
    //}
    JMenuItem create(ActionListener l) {
        item.addActionListener(l);
        return item;
    }
    static JMenu build(ActionListener l) {
        JMenu menu = new JMenu("Tag");
        menu.setMnemonic(KeyEvent.VK_T);
        int i = 0;
        for (TagMenu iTM : TagMenu.values()) {
            JMenuItem itm = iTM.create(l);
            menu.add(itm);
            i++;
        }
        return menu;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="ViewMenu">
enum ViewMenu {
    NextImage("Next Image", KeyEvent.VK_N, KeyEvent.VK_RIGHT, 0, "Next"),
    PrevImage("Previous Image", KeyEvent.VK_P, KeyEvent.VK_LEFT, 0, "Prev"),
    ShowThumbs("Show Thumbnails Bar", KeyEvent.VK_T, KeyEvent.VK_T, ActionEvent.CTRL_MASK, "ThumbsS", false),
    HideThumbs("Hide Thumbnails Bar", KeyEvent.VK_T, KeyEvent.VK_T, ActionEvent.CTRL_MASK, "ThumbsH"),
    ToggleTree("Show/Hide Tag Tree", KeyEvent.VK_R, -1, -1, "TagTree"),
    ToggleImgToolBar("Toggle Image ToolBar", KeyEvent.VK_I,-1, -1, "ImageBar"),
    SlidePlay("Play Slideshow", KeyEvent.VK_S, KeyEvent.VK_SPACE, 0, "SlideP"),
    SlideStop("Stop Slideshow", KeyEvent.VK_S, KeyEvent.VK_SPACE, 0, "SlideS", false),
    ZoomToFit("Zoom: Fit Image", KeyEvent.VK_Z, KeyEvent.VK_Z, ActionEvent.ALT_MASK, "ZoomFit", false),
    ZoomTo100("Zoom: 100%", KeyEvent.VK_Z, KeyEvent.VK_Z, ActionEvent.ALT_MASK, "Zoom100"),
    ZoomToX("Zoom: Custom", KeyEvent.VK_C, KeyEvent.VK_Z, ActionEvent.SHIFT_MASK, "ZoomX");
    JMenuItem item;
    ViewMenu(String label, int mnemonic, int acceleratorKey, int acceleratorMask, String command) {
        item = new JMenuItem(label, mnemonic);
        if (acceleratorKey != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, acceleratorMask));
        }
        item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    ViewMenu(String label, int mnemonic, int acceleratorKey, int acceleratorMask, String command, boolean visible) {
        this(label, mnemonic, acceleratorKey, acceleratorMask, command);
        item.setVisible(visible);
    }
    void hide() {
        item.setVisible(false);
    }
    void show() {
        item.setVisible(true);
    }
    void setVisible(boolean value) {
        item.setVisible(value);
    }
    JMenuItem create(ActionListener l) {
        item.addActionListener(l);
        return item;
    }
    static JMenu build(ActionListener l) {
        JMenu menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        int i = 0;
        for (ViewMenu iTM : ViewMenu.values()) {
            if (i == 2 || i == 6) {
                menu.addSeparator();//add seperator before positions 2 in the menu
            }
            JMenuItem itm = iTM.create(l);
            menu.add(itm);
            i++;
        }
        return menu;
    }
}// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="HelpMenu">
enum HelpMenu {
    About("About", KeyEvent.VK_A, -1, -1, "About"),
    Start("StartHere Guide", KeyEvent.VK_S, -1, -1, "StartHelp"),
    Help("StudyBuddy Help!", KeyEvent.VK_H, KeyEvent.VK_F1, 0, "Help");
    JMenuItem item;
    HelpMenu(String label, int mnemonic, int acceleratorKey, int acceleratorMask, String command) {
        item = new JMenuItem(label, mnemonic);
        if (acceleratorKey != -1) {
            item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey, acceleratorMask));
        }
        item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    //HelpMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command, boolean visible){
    //this(label,command);
    //item.setVisible(visible);
    //}
    JMenuItem create(ActionListener l) {
        item.addActionListener(l);
        return item;
    }
    static JMenu build(ActionListener l) {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        int i = 0;
        for (HelpMenu iTM : HelpMenu.values()) {
            //if(i==2){
            //bar.addSeparator();//add seperator before positions 2 in the menu
            //}
            JMenuItem itm = iTM.create(l);
            menu.add(itm);
            i++;
        }
        return menu;
    }// </editor-fold>
}
