
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GuiListener  implements ComponentListener, WindowStateListener, ChangeListener, ActionListener {
    GUI mainGUI;
    Log log = new Log();

    GuiListener(GUI gui){
        mainGUI = gui;
    }

    @Override
  public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("mRestart")) {
            mainGUI.toggleThumbs(true);
            mainGUI.toggleZoomed(true);
            mainGUI.quickRestart();
        } else if (ae.getActionCommand().equals("mImport")) FileDialogs.importDo();
        else if (ae.getActionCommand().equals("mImportD")) FileDialogs.importDirDo();
        else if (ae.getActionCommand().equals("ThumbsS")) mainGUI.toggleThumbs(true);
        else if (ae.getActionCommand().equals("ThumbsH")) mainGUI.toggleThumbs(false);
        else if (ae.getActionCommand().equals("TagTree")) mainGUI.toggleTagTree();
        else if (ae.getActionCommand().equals("SlideP")) mainGUI.toggleSlide(true);
        else if (ae.getActionCommand().equals("SlideS")) mainGUI.toggleSlide(false);
        else if (ae.getActionCommand().equals("ZoomFit")) mainGUI.toggleZoomed(true);
        else if (ae.getActionCommand().equals("Zoom100")) mainGUI.zoomTo(100);
        else if (ae.getActionCommand().equals("ZoomX")) mainGUI.zoomBox();
        else if (ae.getActionCommand().equals("mRemoveI")) mainGUI.deleteCurrentImage();
//        else if (ae.getActionCommand().equals("DeleteTag")) deleteTag();
        else if (ae.getActionCommand().equals("Next")) mainGUI.getState().nextImage();
        else if (ae.getActionCommand().equals("Prev")) mainGUI.getState().prevImage();
        else if (ae.getActionCommand().equals("AddTag")) mainGUI.addTag();
        else if (ae.getActionCommand().equals("TagThis")) mainGUI.tagThis();
        else if (ae.getActionCommand().equals("QuickTag")) mainGUI.quickTag();
        else if (ae.getActionCommand().equals("ImageBar")) mainGUI.imageToolbarToggle();
        else if (ae.getActionCommand().equals("TagFilter")) mainGUI.tagFilter();
        else if (ae.getActionCommand().equals("TagTag")) mainGUI.tagTag();
        else if (ae.getActionCommand().equals("DragPan")) mainGUI.mainPanel.setCursorMode(mainGUI.mainPanel.getCurrentDrag());
        else if (ae.getActionCommand().equals("DragLink")) mainGUI.dragLink();
        else if (ae.getActionCommand().equals("DragNote")) mainGUI.mainPanel.setCursorMode(DragMode.Note);
        else if (ae.getActionCommand().equals("BlueT")) mainGUI.bluetoothDo();
        else if (ae.getActionCommand().equals("AdjustImage")) mainGUI.showImageAdjuster();
        else if (ae.getActionCommand().equals("Flip")) {mainGUI.getState().getCurrentImage().img.transform.flip(); RepaintManager.repaint(RepaintType.MainPanel);}
        else if (ae.getActionCommand().equals("Mirror")){ mainGUI.getState().getCurrentImage().img.transform.mirror(); RepaintManager.repaint(RepaintType.MainPanel);}
        else if (ae.getActionCommand().equals("Rotate")) {mainGUI.getState().getCurrentImage().img.transform.rotate90(); RepaintManager.repaint(RepaintType.MainPanel);}
        else if (ae.getActionCommand().equals("ExportCurrentImg")) FileDialogs.exportCurrentImage();
        else if (ae.getActionCommand().equals("StartHelp")) mainGUI.showHelpGuide();
        else if (ae.getActionCommand().equals("Options")) mainGUI.showOptions();
        else if (ae.getActionCommand().equals("Exit")) {
            System.exit(0);
        } else if (ae.getActionCommand().equals("Help")) {
            //Not final help- needs improving
            JOptionPane.showMessageDialog(mainGUI.w, "Visit http://www.studybuddy.com for help and tutorials", "Study Help", JOptionPane.INFORMATION_MESSAGE, SysIcon.Info.Icon);
        } else if (ae.getActionCommand().equals("About")) {
            JOptionPane.showMessageDialog(mainGUI.w, "StudyBuddy by Team StudyBuddy", "About StudyBuddy", JOptionPane.INFORMATION_MESSAGE, SysIcon.Help.Icon);
        } else{
            log.print(LogType.Error,"ActionEvent " + ae.getActionCommand() + " was not dealt with,\nand had prameter string " + ae.paramString());
        }
        //+ ",\nwith source:\n\n " + e.getSource());
    }





    @Override
    public void windowStateChanged(WindowEvent e) {
        RepaintManager.repaint(RepaintType.Window);
    }

    static final int maxWindowWidth=400;
    static final int maxWindowHeight=300;
    @Override
    public void componentResized(ComponentEvent e) {
        if (e.getSource() == mainGUI.w) {
            int newWidth = mainGUI.w.getWidth();
            int newHeight = mainGUI.w.getHeight();
            if (newWidth < maxWindowWidth) newWidth = maxWindowWidth;
            if (newHeight < maxWindowHeight) newHeight = maxWindowHeight;
            mainGUI.w.setSize(newWidth, newHeight);
        }
        // if(e.getSource()==boardScroll) {
        //if(e.getSource()==mainPanel) {
        //**//log.print(LogType.Error,e.paramString());
        RepaintManager.repaint(RepaintType.Window);
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //ZoomBar
        JSlider src = (JSlider) e.getSource();
        if (!src.getValueIsAdjusting()) {
            int zoom = (int) src.getValue();
            if (zoom == 0) {
                mainGUI.toggleZoomed(true);
            } else {
                mainGUI.zoomTo(zoom);
            }
        }
    }
}
