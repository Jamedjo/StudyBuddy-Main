import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

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
    bAdjustImage(true, "Adjust Image Colours", "AdjustImage", SysIcon.Adjust.Icon),
    bBlueDemo(true, "Bluetooth", "BlueT", SysIcon.BlueTooth.Icon),
    bZoomToX(true, "Zoom Dialog", "ZoomX", SysIcon.ZoomToX.Icon),
    bZoomFit(false, "Zoom: Fit     ", "ZoomFit", SysIcon.ZoomFit.Icon, false),
    bZoomMax(false, "Zoom: 100%", "Zoom100", SysIcon.Zoom100.Icon),
    bOptions(false, "Options", "Options", SysIcon.Options.Icon);

    JButton button;
    boolean isSeperatorHere;
    static final boolean putSeperatorAtEnd = false;
    static final int sliderPosFromEnd = 1;//Hom many icons should come after the slider

    ToolBar(boolean isNewGroup,String label, String command, ImageIcon icon, boolean visible) {
        isSeperatorHere = isNewGroup;
        if (icon != null) {
            button = new JButton(icon);
            button.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            button.setToolTipText(label);
            //button.set text to hidden;
        }
        else{
            button = new JButton(label);
        }
        button.setActionCommand(command);
        button.setVisible(visible);
    }
    ToolBar(boolean isNewGroup,String label, String command) {
        this(isNewGroup,label, command, true);
    }
    ToolBar(boolean isNewGroup,String label, String command, boolean visible) {
        this(isNewGroup,label, command, null, visible);
    }
    ToolBar(boolean isNewGroup,String label, String command, ImageIcon icon) {
        this(isNewGroup,label, command, icon, true);
    }

    void hide(){
	button.setVisible(false);
    }

    void show(){
	button.setVisible(true);
    }

    void setVisible(boolean value){
        button.setVisible(value);
    }

    JButton create(ActionListener l){
	button.addActionListener(l);
	return button;
    }

    static JToolBar build(GUI mainGUI){
	JToolBar bar = new JToolBar("StudyBuddy Toolbar");
	bar.setFocusable(false);

	int i=0;
	for (ToolBar b : ToolBar.values()){
	    JButton bt = b.create((ActionListener)mainGUI);
	    if(b.isSeperatorHere){
		bar.addSeparator();//add seperator before positions 0,2&4 in the menu
	    }
	    bar.add(bt);
            if(i==ToolBar.values().length-(1+sliderPosFromEnd)) bar.add(mainGUI.buildZoomBar());
	    i++;
	}
        if (putSeperatorAtEnd) {
            bar.addSeparator();
        }
        
        //workaround to prevent toolbar from steeling focus
	for(i=0; i<bar.getComponentCount();i++){
	    if(bar.getComponent(i) instanceof JButton){
		((JButton)bar.getComponent(i)).setFocusable(false);
	    }
	}
	return bar;
    }

}

enum ImageMenu{
    mImport("Import Image(s)",KeyEvent.VK_I,KeyEvent.VK_I, ActionEvent.CTRL_MASK,"mImport"),
    mImportD("Import Folder",KeyEvent.VK_F,KeyEvent.VK_I, ActionEvent.CTRL_MASK+ActionEvent.SHIFT_MASK,"mImportD"),
    mRestart("Restart Viewer",KeyEvent.VK_R,KeyEvent.VK_N,  ActionEvent.CTRL_MASK,"mRestart"),
    mExit("Exit",KeyEvent.VK_X,KeyEvent.VK_W, ActionEvent.CTRL_MASK,"Exit");
    
    JMenuItem item;
    ImageMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command){
        item = new JMenuItem(label,mnemonic);
	if(acceleratorKey!=-1) {
	    item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,acceleratorMask));
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


    JMenuItem create(ActionListener l){
	item.addActionListener(l);
	return item;
    }

    static JMenu build(ActionListener l){
	JMenu menu = new JMenu("Image");
	menu.setMnemonic(KeyEvent.VK_I);
	int i=0;
	for (ImageMenu iTM : ImageMenu.values()){
	    JMenuItem itm = iTM.create(l);
	    menu.add(itm);
	    i++;
	}
	return menu;
    }
}

enum TagMenu{
    AddTag("Create new tag",KeyEvent.VK_N,-1,-1,"AddTag"),
	TagThis("Tag this Image",KeyEvent.VK_T,KeyEvent.VK_T,0,"TagThis"),
	TagTag("Tag a Tag",KeyEvent.VK_A,-1,-1,"TagTag"),
	TagFilter("Filter Images by Tag",KeyEvent.VK_F,-1,-1,"TagFilter");
    
    JMenuItem item;
    TagMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command){
        item = new JMenuItem(label,mnemonic);
	if(acceleratorKey!=-1) {
	    item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,acceleratorMask));
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

    JMenuItem create(ActionListener l){
	item.addActionListener(l);
	return item;
    }

    static JMenu build(ActionListener l){
	JMenu menu = new JMenu("Tag");
	menu.setMnemonic(KeyEvent.VK_T);
	int i=0;
	for (TagMenu iTM : TagMenu.values()){
	    JMenuItem itm = iTM.create(l);
	    menu.add(itm);
	    i++;
	}
	return menu;
    }
}

enum ViewMenu{
    NextImage("Next Image",KeyEvent.VK_N,KeyEvent.VK_RIGHT,0,"Next"),
	PrevImage("Previous Image",KeyEvent.VK_P,KeyEvent.VK_LEFT,0,"Prev"),
	ShowThumbs("Show Thumbnails Bar",KeyEvent.VK_T,KeyEvent.VK_T,ActionEvent.CTRL_MASK,"ThumbsS",false),
	HideThumbs("Hide Thumbnails Bar",KeyEvent.VK_T,KeyEvent.VK_T,ActionEvent.CTRL_MASK,"ThumbsH"),
	SlidePlay("Play Slideshow",KeyEvent.VK_S,KeyEvent.VK_SPACE,0,"SlideP"),
	SlideStop("Stop Slideshow",KeyEvent.VK_T,KeyEvent.VK_SPACE,0,"SlideS",false),
	ZoomToFit("Zoom: Fit Image",KeyEvent.VK_Z,KeyEvent.VK_Z,ActionEvent.ALT_MASK,"ZoomFit",false),
	ZoomTo100("Zoom: 100%",KeyEvent.VK_Z,KeyEvent.VK_Z,ActionEvent.ALT_MASK,"Zoom100"),
	ZoomToX("Zoom: Custom",KeyEvent.VK_C,KeyEvent.VK_Z,ActionEvent.SHIFT_MASK,"ZoomX");
    
    JMenuItem item;
    ViewMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command){
        item = new JMenuItem(label,mnemonic);
	if(acceleratorKey!=-1) {
	    item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,acceleratorMask));
	}
	item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    ViewMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command, boolean visible){
	this(label, mnemonic, acceleratorKey, acceleratorMask, command);
	item.setVisible(visible);
    }

    void hide(){
	item.setVisible(false);
    }
    void show(){
	item.setVisible(true);
    }
    void setVisible(boolean value){
        item.setVisible(value);
    }

    JMenuItem create(ActionListener l){
	item.addActionListener(l);
	return item;
    }

    static JMenu build(ActionListener l){
	JMenu menu = new JMenu("View");
	menu.setMnemonic(KeyEvent.VK_V);
	int i=0;
	for (ViewMenu iTM : ViewMenu.values()){
	    if(i==2||i==4){
		menu.addSeparator();//add seperator before positions 2 in the menu
	    }
	    JMenuItem itm = iTM.create(l);
	    menu.add(itm);
	    i++;
	}
	return menu;
    }
}

enum HelpMenu{
    About("About",KeyEvent.VK_A,-1,-1,"About"),
	Help("StudyBuddy Help!",KeyEvent.VK_H,KeyEvent.VK_F1,0,"Help");
    
    JMenuItem item;
    HelpMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command){
        item = new JMenuItem(label,mnemonic);
	if(acceleratorKey!=-1) {
	    item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,acceleratorMask));
	}
	item.setActionCommand(command);
        //item.setToolTipText(toolTipText);
    }
    //HelpMenu(String label,int mnemonic,int acceleratorKey,int acceleratorMask,String command, boolean visible){
    //this(label,command);
    //item.setVisible(visible);
    //}

    JMenuItem create(ActionListener l){
	item.addActionListener(l);
	return item;
    }

    static JMenu build(ActionListener l){
	JMenu menu = new JMenu("Help");
	menu.setMnemonic(KeyEvent.VK_H);
	int i=0;
	for (HelpMenu iTM : HelpMenu.values()){
	    //if(i==2){
	    //bar.addSeparator();//add seperator before positions 2 in the menu
	    //}
	    JMenuItem itm = iTM.create(l);
	    menu.add(itm);
	    i++;
	}
	return menu;
    }
}
