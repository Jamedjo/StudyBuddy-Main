import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.*;

class TagTreeListener implements TreeSelectionListener
{
  GUI mainGUI;
  
  TagTreeListener(GUI NewMainGUI)
  {
    mainGUI = NewMainGUI;
  }
  
  public void valueChanged(TreeSelectionEvent Event)
  {
    DefaultMutableTreeNode CurrentNode = (DefaultMutableTreeNode)mainGUI.TagTree.getLastSelectedPathComponent();
    IDTitle NodeObject = null;
    if (CurrentNode != null)
    {
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
