import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;

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
    }

}
