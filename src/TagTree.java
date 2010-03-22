import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

public class TagTree extends JTree implements TreeSelectionListener,ActionListener{
    ImageDatabase mainImageDB;
    GUI mainGUI;
    DefaultMutableTreeNode treeNode;
    JPopupMenu rightClickMenu = new JPopupMenu();
    JMenuItem mTagThis;
    int lastX,lastY;

    TagTree(ImageDatabase mainDB, GUI gui) {
        super(new DefaultTreeModel(new DefaultMutableTreeNode(), false));
        mainGUI = gui;
        mainImageDB = mainDB;
        //last x any y = 0; ?
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setFocusable(false);//Prevents other keyboard actions in rest of GUI. May be able to allow up/down only here.
        updateTags();
        this.expandRow(0);
        this.addTreeSelectionListener(this);
        this.addMouseListener(
                new MouseAdapter() {
                    // Also for mouse pressed? different platforms/os trigger popups differently
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            lastX=e.getX();
                            lastY=e.getY();
                            rightClickMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                        }
                    }
                });


        mTagThis = new JMenuItem("Tag current image with this");
        mTagThis.addActionListener(this);
        mTagThis.setActionCommand("rTagThis");
        rightClickMenu.add(mTagThis);
    }

    public void updateTags(){
        treeNode = new DefaultMutableTreeNode(new IDTitle("-1", "All Tags"));
        ((DefaultTreeModel) this.getModel()).setRoot(treeNode);
        addTreeTags(treeNode, new Hashtable<String, IDTitle>());
        this.expandRow(0);
        // TODO: before update, check for expanded rows and remember them?
        this.repaint();
    }

    public void valueChanged(TreeSelectionEvent Event) {
        DefaultMutableTreeNode CurrentNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
        IDTitle NodeObject = null;
        if (CurrentNode != null) {
            NodeObject = (IDTitle) CurrentNode.getUserObject();
            if (NodeObject.getID().equals("-1")) {
                mainGUI.setState(new ProgramState(LoadType.Refresh, mainGUI));
                mainGUI.getState().imageChanged();
            } else {
                mainGUI.setState(new ProgramState(LoadType.Filter, mainGUI, NodeObject.getID()));
                mainGUI.getState().imageChanged();
            }
        }
    }

public void actionPerformed(ActionEvent ae) {//ensue x and y are from first click and not menu click
        if (ae.getActionCommand().equals("rTagThis")){
            IDTitle idT =(IDTitle)((DefaultMutableTreeNode)this.getPathForLocation(lastX,lastY).getLastPathComponent()).getUserObject();
            mainImageDB.tagImage(mainGUI.getState().getCurrentImageID(), idT.getID());
        }
        else {
            System.err.println("ActionEvent " + ae.getActionCommand() + " was not dealt with,\nand had prameter string " + ae.paramString());
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
