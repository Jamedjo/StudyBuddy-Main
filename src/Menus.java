import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

enum ToolBar{
    bPrev("Prev",			"bPrev"),
    bNext("Next",			"bNext"),
    bThumbsS("Show Thumbnails",		"bThumbsS"),
    bThumbsH("Hide Thumbnails",		"bThumbsH",false),
    //bZoom("Zoom",			"bZoom"),//ensure does not affect seperators
    bAddTag("Add Tag",			"bAddTag"),
    bTagThis("Tag This Image",		"bTagThis"),
    bTagFilter("Filter By Tag",		"bTagFilter");

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

    static void addAll(JToolBar bar,ActionListener l){
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
	item.setAccelerator(KeyStroke.getKeyStroke(acceleratorKey,acceleratorMask));
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

    static void addAll(JMenu menu,ActionListener l){
     int i=0;
	for (ImageMenu iTM : ImageMenu.values()){
	    JMenuItem itm = iTM.create(l);
	    menu.add(itm);
	    i++;
	}
    }
}
