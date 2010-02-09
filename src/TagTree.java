
import java.util.Hashtable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class TagTree extends JTree implements TreeSelectionListener {//Will implement ActionListener for JPopupMenu

    ImageDatabase mainImageDB;
    GUI mainGUI;
    DefaultMutableTreeNode treeNode;

    TagTree(ImageDatabase mainDB, GUI gui) {
        super(new DefaultTreeModel(new DefaultMutableTreeNode(), false));
        mainGUI = gui;
        mainImageDB = mainDB;
        treeNode = new DefaultMutableTreeNode(new IDTitle("-1", "All Tags"));
        ((DefaultTreeModel) this.getModel()).setRoot(treeNode);
        addTreeTags(treeNode, new Hashtable<String, IDTitle>());
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setFocusable(false);//Prevents other keyboard actions in rest of GUI. May be able to allow up/down only here.
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

//    public void onChange() {
//        ((DefaultTreeModel) this.getModel()).nodeStructureChanged((TreeNode) defMutTreeNode);
//        this.repaint();
//    }
    public void addTagToTree(String newTagID, String newTag) {
        boolean addToRoot = false;
        DefaultTreeModel model;
        DefaultMutableTreeNode nodeAddTo = null;
        DefaultMutableTreeNode nodeToAdd = null;
        IDTitle nodeAddToObject = null;
        // Find the currently selected node in the tree
        if (this.getSelectionPath() == null) {
            addToRoot = true;
        } else {
            nodeAddTo = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
            // If root node selected then add to root node
            nodeAddToObject = (IDTitle) nodeAddTo.getUserObject();
            if (nodeAddToObject.getID().equals("-1")) {
                addToRoot = true;
            }
            if (addToRoot == false) {
                mainImageDB.tagTag(newTagID, nodeAddToObject.getID());
            }
        }
        if (addToRoot == true) {
            nodeAddTo = (DefaultMutableTreeNode) this.getModel().getRoot();
        }
        nodeToAdd = new DefaultMutableTreeNode(new IDTitle(newTagID, newTag));
        model = (DefaultTreeModel) this.getModel();
        model.insertNodeInto(nodeToAdd, nodeAddTo, nodeAddTo.getChildCount());
        //model.nodeStructureChanged(nodeAddTo);
        this.repaint();
    }

    // Adds all tags tagged by a node to that node (in a tree)
    DefaultMutableTreeNode addTreeTags(DefaultMutableTreeNode NodeAddTo, Hashtable<String, IDTitle> PathTags) {
        String[] TagIDs;
        DefaultMutableTreeNode TempTreeNode;
        IDTitle TempIDTitle;
        // Gets a list of tags tagged with this tag (or all if root node)
        TempIDTitle = (IDTitle) NodeAddTo.getUserObject();
        if (TempIDTitle.getID().equals("-1")) {
            TagIDs = mainImageDB.getAllTagIDs();
        } else {
            TagIDs = mainImageDB.getTagIDsFromTagID(TempIDTitle.getID());
        }
        // For all of those tags, add them to the node as branches and recurse
        if (TagIDs != null) {
            for (int i = 0; i < TagIDs.length; i++) //for (int i=TagIDs.length-1; i>=0; i--)//Reverse add
            {
                if (PathTags.containsKey(TagIDs[i]) == false) {
                    TempIDTitle = new IDTitle(TagIDs[i], mainImageDB.getTagTitleFromTagID(TagIDs[i]));
                    TempTreeNode = new DefaultMutableTreeNode(TempIDTitle);
                    PathTags.put(TagIDs[i], TempIDTitle);
                    TempTreeNode = addTreeTags(TempTreeNode, PathTags);
                    PathTags.remove(TagIDs[i]);
                    NodeAddTo.add(TempTreeNode);
                }
            }
        }
        return NodeAddTo;
    }
}
