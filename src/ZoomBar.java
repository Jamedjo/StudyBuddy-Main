
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicSliderUI;

public class ZoomBar extends JComboBox{
    JSlider zoomSlider;
    RefreshableComboBoxModel refresher;
    ZoomEditor zoomEditor;
    ComponentRenderer renderer;
    GUI mainGUI;

    ZoomBar(GUI gui,ToolBar[] buttons){
        super();
        mainGUI = gui;
        buildZoomBar(mainGUI);
        
        Object[] items = new Object[buttons.length+1];
        items[0]=zoomSlider;
        for(int i=0;i<buttons.length;i++){
            items[i+1]=buttons[i];
        }
        refresher=new RefreshableComboBoxModel(items);
        this.setModel(refresher);
        this.addPopupMenuListener(null);
        renderer =new ComponentRenderer();
        this.setRenderer(renderer);
        this.setEditable(true);
        zoomEditor = new ZoomEditor(mainGUI,zoomSlider);
        JLabel proto = new JLabel(SysIcon.ZoomToX.Icon);
        proto.setMaximumSize(new Dimension(zoomSlider.getWidth(),proto.getHeight()));
        this.setEditor(zoomEditor);
        this.setPrototypeDisplayValue(proto);
        this.updateUI();
        this.setMaximumSize(new Dimension(120,120));
        setSliderListeners();
    }

    void buildZoomBar(GUI mainGUI) {
        zoomSlider = new JSlider(JSlider.VERTICAL, 0, 300, 0);
        zoomSlider.setInverted(true);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setMinorTickSpacing(10);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setPaintTicks(true);
        zoomSlider.setFocusable(false);//Otherwise arrows zoom when they shouldn't
        zoomSlider.addChangeListener(mainGUI.guiListener);
        mainGUI.zoomBar=zoomSlider;
    }
    
    @Override
    public void setPopupVisible(boolean v) {
        if((v==false)&&(getSelectedItem() instanceof JSlider)) return;
        getUI().setPopupVisible(this, v);
    }



    void setSliderListeners() {
        try {
            Field field = BasicComboBoxUI.class.getDeclaredField("popup");
            field.setAccessible(true);
            Component popup = ((JViewport) ((JScrollPane) ((BasicComboPopup) field.get(this.getUI())).getComponents()[0]).getComponents()[0]).getComponents()[0];
            popup.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    dispatchSliderEvent(e,MouseEvent.MOUSE_CLICKED);
                }
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {
                    dispatchSliderEvent(e,MouseEvent.MOUSE_RELEASED);
                }
                public void mousePressed(MouseEvent e) {
                    dispatchSliderEvent(e,MouseEvent.MOUSE_PRESSED);
                }
            });
            popup.addMouseMotionListener(new MouseMotionListener(){
                public void mouseMoved(MouseEvent e) {
                    dispatchSliderEvent(e,MouseEvent.MOUSE_MOVED);
                }
                public void mouseDragged(MouseEvent e) {
                    dispatchSliderEvent(e,MouseEvent.MOUSE_DRAGGED);
                }
            });
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    void dispatchSliderEvent(MouseEvent e, int eventType) {
        if(!renderer.sliderHasFocus) if(eventType!=MouseEvent.MOUSE_RELEASED){
            if(eventType==MouseEvent.MOUSE_PRESSED){
                Object item = this.getItemAt(renderer.selectedIndex);
                if(item instanceof ToolBar) ((ToolBar)item).button.doClick();
            }
            return;
        }
        int yOffset = 0;//32;//Verticle distance from top of popup to start of slder component.
        //System.out.println("event at:("+e.getX()+","+(e.getY()-yOffset)+")");//-dropCombo.getY()
        zoomSlider.dispatchEvent(new MouseEvent((Component) e.getSource(), eventType, e.getWhen(), e.getModifiers(), e.getX(), e.getY() - yOffset, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        refresher.refresh();
    }
}
class DropButton extends JToolBar {
    ToolBar item;
    public DropButton(ToolBar tbItem) {
        item=tbItem;
        setFloatable(false);
        add(Box.createHorizontalGlue());
        add(item.button);
    }
}

class ComponentRenderer implements ListCellRenderer{
    boolean sliderHasFocus=false;
    int selectedIndex=0;

    ComponentRenderer(){
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof javax.swing.Icon) value = new JLabel((Icon) value);
        if (value instanceof ToolBar) value=new DropButton((ToolBar)value);
        ((JComponent)value).setOpaque(true);
        if ((value instanceof JSlider)){
            sliderHasFocus=isSelected;
        }
        if(isSelected){//Alt text?
            selectedIndex = index;
        }
        return (JComponent)value;
    }
}

class ZoomEditor  extends BasicComboBoxEditor{//implements ComboBoxEditor{//
    static final DropButton showIcon=new DropButton(ToolBar.bZoomToX);//replace with text editor
    JSlider slider;
    GUI mainGUI;

    ZoomEditor(GUI gui,JSlider zoomSlider){
        super();
        slider=zoomSlider;
        mainGUI=gui;
    }

    public void update(){
        setItem(null);
    }

    @Override
    public JTextField createEditorComponent() {
        editor = new JTextField("",4);
        editor.setBorder(null);
        return editor;
    }
    @Override
    public void setItem(Object anObject){
        String setText="Zoom: Fit";
        int sliderVal =slider.getValue();
        if(sliderVal>=300) sliderVal = Math.max(300,(int)(mainGUI.mainPanel.getZoomMult()*100));
        if (sliderVal!=0) setText=Integer.toString(sliderVal)+"%";
        editor.setText(setText);
    }
    @Override
    public Object getItem(){
        slider.setValueIsAdjusting(true);
        String editorText = editor.getText();
        int newVal =0;
        if(editorText!=null){
            if(editorText.toLowerCase().endsWith("fit")) slider.setValue(0);
            else{
                StringBuffer numbers = new StringBuffer();
                char c;
                for (int i=0;i<editorText.length();i++) {
                    c = editorText.charAt(i);
                    if (Character.isDigit(c)) {
                        numbers.append(c);
                    }
                }
                try{
                    newVal=Integer.parseInt(numbers.toString());
                    slider.setValue(newVal);
                } catch (Exception e){
                    //fail silently if invalid
                }
            }
        }
        if(slider.getValue()<=300) {
            slider.setValueIsAdjusting(false);
        return slider.getValue();
        }
        mainGUI.zoomTo(newVal);
        return newVal;
    }
}
class RefreshableComboBoxModel extends DefaultComboBoxModel{
    RefreshableComboBoxModel(Object[] itmes){
        super(itmes);
    }
    public void refresh(){
        this.fireContentsChanged(this, 0, 2);
    }
}
