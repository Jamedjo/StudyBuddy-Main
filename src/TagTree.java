import java.util.Hashtable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class TagTree extends JTree implements TreeSelectionListener {
    ImageDatabase mainImageDB;
    GUI mainGUI;

    TagTree(ImageDatabase mainDB, GUI gui) {
        super(gui.addTreeTags(new DefaultMutableTreeNode(new IDTitle("-1", "All Tags")), new Hashtable<String, IDTitle>()));
        mainGUI = gui;
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.addTreeSelectionListener(this);
    }

    public void valueChanged(TreeSelectionEvent Event) {
        DefaultMutableTreeNode CurrentNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
        IDTitle NodeObject = null;
        if (CurrentNode != null) {
            NodeObject = (IDTitle) CurrentNode.getUserObject();
            if (NodeObject.getID().equals("-1")) {
                mainGUI.state = new ProgramState(LoadType.Refresh, mainGUI);
                mainGUI.state.imageChanged();
            } else {
                mainGUI.state = new ProgramState(LoadType.Filter, mainGUI, NodeObject.getID());
                mainGUI.state.imageChanged();
            }
        }
    }
}

