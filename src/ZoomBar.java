
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
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
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicSliderUI;

public class ZoomBar extends JComboBox{
    JSlider zoomSlider;
    RefreshableComboBoxModel refresher;

    ZoomBar(GUI mainGUI){
        super();
        buildZoomBar(mainGUI);
        init();
    }
    void init(){
        Object[] items = {SysIcon.TagThis.Icon,zoomSlider,SysIcon.ZoomFit.Icon,SysIcon.Zoom100.Icon};
        refresher=new RefreshableComboBoxModel(items);
        this.setModel(refresher);
        setSliderListeners();
        this.addPopupMenuListener(null);
        this.setRenderer(new ComponentRenderer());
        this.setEditable(true);
        ZoomEditor zoomEditor = new ZoomEditor();
        JLabel proto = new JLabel(SysIcon.ZoomToX.Icon);
        proto.setMaximumSize(new Dimension(zoomSlider.getWidth(),proto.getHeight()));
        this.setEditor(zoomEditor);
        this.setPrototypeDisplayValue(proto);
        //dropCombo.updateUI();
        //this.setMaximumSize(new Dimension(200,200));
    }

    void buildZoomBar(GUI mainGUI) {
        zoomSlider = new JSlider(JSlider.VERTICAL, 0, 300, 0);
        zoomSlider.setMajorTickSpacing(100);
        zoomSlider.setMinorTickSpacing(20);
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
        Object c = this.getSelectedItem();//dropCombo.getComponentAt(e.getPoint());//Should not use selected item without first making this the selected item
        if (!(c instanceof JSlider))
            return;
        //JSlider popSlider=((JSlider)c);
        int yOffset = 32;
        //System.out.println("event at:("+e.getX()+","+(e.getY()-yOffset)+")");//-dropCombo.getY()
        zoomSlider.dispatchEvent(new MouseEvent((Component) e.getSource(), eventType, e.getWhen(), e.getModifiers(), e.getX(), e.getY() - yOffset, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        refresher.refresh();
    }
}

class ComponentRenderer implements ListCellRenderer{
    ComponentRenderer(){
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof javax.swing.Icon) value = new JLabel((Icon) value);
        ((JComponent)value).setOpaque(true);
        if (!(value instanceof JSlider)){
            if (isSelected) {
                ((JComponent) value).setBackground(list.getSelectionBackground());
                ((JComponent) value).setForeground(list.getSelectionForeground());
            } else {
                ((JComponent) value).setBackground(list.getBackground());
                ((JComponent) value).setForeground(list.getForeground());
            }
        } else{
//            JSlider slider=((JSlider)value);
//            JPanel container=new JPanel(new FlowLayout(FlowLayout.CENTER));
//            container.add(slider);
//            value=container;
        }

        return (JComponent)value;
    }
}

class ZoomEditor implements ComboBoxEditor{// extends BasicComboBoxEditor{//
    static final JLabel showIcon=new JLabel(SysIcon.ZoomToX.Icon);

    ZoomEditor(){
    }

    @Override
    public Component getEditorComponent(){
        return showIcon;
    }
    @Override
    public void addActionListener(ActionListener l){}
    @Override
    public void removeActionListener(ActionListener l){}
    @Override
    public void selectAll(){}
    @Override
    public void setItem(Object anObject){}
    @Override
    public Object getItem(){
        return showIcon;
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
