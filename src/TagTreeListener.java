import javax.swing.JTree;
import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.*;

class TagTreeListener implements TreeSelectionListener
{
  GUI Parent;
  
  TagTreeListener(TreeStuff NewParent)
  {
    Parent = NewParent;
  }
  
  public void valueChanged(TreeSelectionEvent Event)
  {
    DefaultMutableTreeNode CurrentNode = (DefaultMutableTreeNode)Parent.TagTree.getLastSelectedPathComponent();
    IDTitle NodeObject;
    if (CurrentNode != null)
    {
      NodeObject = (IDTitle)CurrentNode.getUserObject();
      
    }
  }

}