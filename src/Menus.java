import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

enum ToolBar{
    bPrev("Prev",			"Prev"),
	bNext("Next",			"Next"),
	bThumbsS("Show Thumbnails",		"ThumbsS"),
	bThumbsH("Hide Thumbnails",		"ThumbsH",false),
	//bZoom("Zoom",			"bZoom"),//ensure does not affect seperators
	bAddTag("Add Tag",			"AddTag"),
	bTagThis("Tag This Image",		"TagThis"),
	bTagFilter("Filter By Tag",		"TagFilter");

    JButton button;
    ToolBar(String label,String command){
        button = new JButton(label);
	button.setActionCommand(command);
        //button.setToolTipText(toolTipText);
    }
    ToolBar(String label, String command, boolean visible){
	this(label,command);
	button.setVisible(visible);
    }

    void hide(){
	button.setVisible(false);
    }

    void show(){
	button.setVisible(true);
    }

    JButton create(ActionListener l){
	button.addActionListener(l);
	return button;
    }

    static JToolBar build(ActionListener l){

	JToolBar bar = new JToolBar("StudyBuddy Toolbar");
	bar.setFocusable(false);

	int i=0;
	for (ToolBar b : ToolBar.values()){
	    if(i==0||i==2||i==4){
		bar.addSeparator();//add seperator before positions 0,2&4 in the menu
	    }
	    JButton bt = b.create(l);
	    bar.add(bt);
	    i++;
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
    mImport("Import Image",KeyEvent.VK_I,KeyEvent.VK_I, ActionEvent.CTRL_MASK,"mImport"),
	//mImportImages
	//mImportDirectory
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
	ShowThumbs("Show Thumbnails Bar",KeyEvent.VK_T,KeyEvent.VK_T,ActionEvent.CTRL_MASK,"ThumbsS"),
	HideThumbs("Hide Thumbnails Bar",KeyEvent.VK_T,KeyEvent.VK_T,ActionEvent.CTRL_MASK,"ThumbsH",false);
    
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

    JMenuItem create(ActionListener l){
	item.addActionListener(l);
	return item;
    }

    static JMenu build(ActionListener l){
	JMenu menu = new JMenu("View");
	menu.setMnemonic(KeyEvent.VK_V);
	int i=0;
	for (ViewMenu iTM : ViewMenu.values()){
	    if(i==2){
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
