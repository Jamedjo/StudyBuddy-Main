public class RepaintManager {
    static Log log = new Log();
    static GUI mainGUI;
    static ThumbPreview preview;

    static void initMain(GUI gui){
        mainGUI=gui;
    }
    static void initPrevew(ThumbPreview prevThmb){
        preview = prevThmb;
    }

    static void repaint(RepaintType type){
        try{
        switch(type){
            case Preview:
                preview.repaint();
                break;
            case NewThumb:
                mainGUI.thumbPanel.repaint();//not onResize
                break;
            case MainPanel:
                mainGUI.mainPanel.onResize();
                break;
            case ImageUpdated:
                mainGUI.mainPanel.onResize();//resize as image have changed dimensions
                mainGUI.thumbPanel.repaint();//repaint as no need to re-layout components
                break;
            case ColourChange:
                mainGUI.mainPanel.repaint();
                mainGUI.thumbPanel.repaint();
                break;
            case Window:
                mainGUI.mainPanel.onResize();
                mainGUI.thumbPanel.onResize();
                break;
            default:

                break;
        }
        } catch(NullPointerException e){
            log.print(LogType.Error,"Unable to update GUI panel before it is initialized");
        }
    }
}

enum RepaintType{ImageUpdated,NewThumb,ThumbResize,MainPanel,Window,Preview,ColourChange}