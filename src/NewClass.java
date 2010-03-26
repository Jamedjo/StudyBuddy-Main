
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Field;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;

// <editor-fold defaultstate="collapsed" desc="comment">
class ComponentRenderer implements ListCellRenderer {
    ComponentRenderer() {
    }
    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (value instanceof javax.swing.Icon) value = new JLabel((Icon) value);
        ((JComponent) value).setOpaque(true);
        if (!(value instanceof JSlider)) {
            if (isSelected) {
                ((JComponent) value).setBackground(list.getSelectionBackground());
                ((JComponent) value).setForeground(list.getSelectionForeground());
            } else {
                ((JComponent) value).setBackground(list.getBackground());
                ((JComponent) value).setForeground(list.getForeground());
            }
        } else {
//            JSlider slider=((JSlider)value);
//            JPanel container=new JPanel(new FlowLayout(FlowLayout.CENTER));
//            container.add(slider);
//            value=container;
        }

        return (JComponent) value;
    }
}

class ZoomEditor implements ComboBoxEditor {// extends BasicComboBoxEditor{//
    static final JLabel showIcon = new JLabel(SysIcon.ZoomToX.Icon);
    ZoomEditor() {
    }
    @Override
    public Component getEditorComponent() {
        return showIcon;
    }
    @Override
    public void addActionListener(ActionListener l) {
    }
    @Override
    public void removeActionListener(ActionListener l) {
    }
    @Override
    public void selectAll() {
    }
    @Override
    public void setItem(Object anObject) {
    }
    @Override
    public Object getItem() {
        return showIcon;
    }
}// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="comment">
class SliderComboBox extends JComboBox {
    @Override
    public void setPopupVisible(boolean v) {
        if ((v == false) && (getSelectedItem() instanceof JSlider)) return;
        getUI().setPopupVisible(this, v);
    }
}// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="comment">
class RefreshableComboBoxModel extends DefaultComboBoxModel {
    RefreshableComboBoxModel(Object[] itmes) {
        super(itmes);
    }
    public void refresh() {
        this.fireContentsChanged(this, 0, 2);
    }
}// </editor-fold>
public class NewClass extends javax.swing.JFrame {
    RefreshableComboBoxModel refresher;
    
    public NewClass() {
        
        initComponents();
        Object[] items = {SysIcon.TagThis.Icon,slider,SysIcon.ZoomFit.Icon,SysIcon.Zoom100.Icon};
        setSliderListeners();
        dropCombo.addPopupMenuListener(null);
        dropCombo.setRenderer(new ComponentRenderer());
        dropCombo.setEditable(true);
        ZoomEditor zoomEditor = new ZoomEditor();
        JLabel proto = new JLabel(SysIcon.ZoomToX.Icon);
        proto.setMaximumSize(new Dimension(slider.getWidth(),proto.getHeight()));
        dropCombo.setEditor(zoomEditor);
        refresher=new RefreshableComboBoxModel(items);
        dropCombo.setModel(refresher);
        dropCombo.setPrototypeDisplayValue(proto);
        //dropCombo.updateUI();
        dropCombo.setMaximumSize(new Dimension(dropCombo.getWidth(),stopButton.getHeight()));
    }

    void setSliderListeners() {
        try {
            Field field = BasicComboBoxUI.class.getDeclaredField("popup");
            field.setAccessible(true);
            Component popup = ((JViewport) ((JScrollPane) ((BasicComboPopup) field.get(dropCombo.getUI())).getComponents()[0]).getComponents()[0]).getComponents()[0];
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

    void dispatchSliderEvent(MouseEvent e,int eventType){
                    Object c = dropCombo.getSelectedItem();//dropCombo.getComponentAt(e.getPoint());
                    if(!(c instanceof JSlider))
                        return;
                    //JSlider popSlider=((JSlider)c);
                    int yOffset=32;
                    System.out.println("presed at:("+e.getX()+","+(e.getY()-yOffset)+")");//-dropCombo.getY()
                    slider.dispatchEvent(new MouseEvent((Component)e.getSource(), eventType, e.getWhen(), e.getModifiers(), e.getX(), e.getY()-yOffset, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
                    refresher.refresh();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton5 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        dropCombo = new SliderComboBox();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        slider = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jToolBar1.setRollover(true);

        jButton5.setText("jButton5");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton5);

        jButton2.setIcon(SysIcon.QuickTag.Icon);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);

        stopButton.setIcon(SysIcon.Stop.Icon);
        stopButton.setFocusable(false);
        stopButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stopButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(stopButton);

        jButton1.setIcon(SysIcon.Play.Icon);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);
        jToolBar1.add(dropCombo);

        jButton6.setIcon(SysIcon.Prev.Icon);
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton7.setIcon(SysIcon.Next.Icon);
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton7);

        jButton4.setText("jButton4");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton4);

        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setOrientation(javax.swing.JSlider.VERTICAL);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(125, 125, 125)
                .addGap(59, 59, 59)
                .addContainerGap(142, Short.MAX_VALUE))
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58))
                )
        ));

        pack();
    }// </editor-fold>

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NewClass().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JComboBox dropCombo;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSlider slider;
    private javax.swing.JButton stopButton;
    // End of variables declaration

}
