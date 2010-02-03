import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.*;

class TagTreeListener implements TreeSelectionListener
{
  GUI MainGUI;
  
  TagTreeListener(GUI NewMainGUI)
  {
    MainGUI = NewMainGUI;
  }
  
  public void valueChanged(TreeSelectionEvent Event)
  {
    DefaultMutableTreeNode CurrentNode = (DefaultMutableTreeNode)MainGUI.TagTree.getLastSelectedPathComponent();
    IDTitle NodeObject = null;
    if (CurrentNode != null)
    {
      NodeObject = (IDTitle)CurrentNode.getUserObject();
      if (NodeObject.getID().equals("-1"))
		    MainGUI.state = new ProgramState(LoadType.Refresh, MainGUI);
      else
		    MainGUI.state = new ProgramState(LoadType.Filter, MainGUI, NodeObject.getID());
		}
  }

}