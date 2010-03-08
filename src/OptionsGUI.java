
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

enum OptionType{
    CheckBox,
    Slider,
    TextBox,
    Default;
}

enum  UserOptions{//Need to add option for default options
    //Name(Label,Type,SettingName),
    SlideshowTime("Slideshow Time",OptionType.TextBox,"slideShowTime"),
    showLinks("Show Links",OptionType.CheckBox,"showLinks"),
    showNotes("Show Notes",OptionType.CheckBox,"showNotes"),
    SliderExample("Slider Example",OptionType.Slider),
    TextBoxExample("TextBox Example");
    private String text;
    private String settingName;
    private JComponent component;
    private OptionType type;

    UserOptions(String label){
        this(label, OptionType.Default);
    }
    UserOptions(String label, OptionType t){
        this(label, t,"");
    }
    UserOptions(String label, OptionType t, String setting){
        build(label,t,setting);
    }

    private void build(String label,OptionType t,String setting){
        if(label.equals("")) label = toString();
        text = label;
        type = t;
        settingName = setting;

        switch(type){
            case CheckBox:
                component = new JCheckBox();
                break;
            case Slider:
                component = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
                ((JSlider) component).setMajorTickSpacing(25);
                ((JSlider) component).setMinorTickSpacing(5);
                ((JSlider) component).setPaintLabels(true);
                ((JSlider) component).setPaintTicks(true);
                break;
            case TextBox:
            default:
                component = new JTextField(10);
        }
    }

    void initValue(Settings settings){
        if(settingName==null||settingName.equals("")) return;
        switch(type){
            case CheckBox:
                Boolean bVal = settings.getSettingAsBooleanObject(settingName);
                if (bVal == null) return;
                if(bVal.booleanValue()==false) ((JCheckBox)component).setSelected(false);
                else if(bVal.booleanValue()==true) ((JCheckBox)component).setSelected(true);
                break;
            case Slider:
                int iVal = settings.getSettingAsInt(settingName);
                ((JSlider) component).setValue(iVal);
                break;
            case TextBox:
            default:
                String sVal = settings.getSetting(settingName);
                if(sVal!=null) ((JTextField)component).setText(sVal);
        }
    }

    void saveValue(Settings settings){
        if(settingName==null||settingName.equals("")) return;
        settings.setSettingAndSave(settingName, getValue());
    }

    String getLabel(){
        return text+": ";
    }

    JComponent getComponent(){
        return component;
    }

    String getValue(){
        String temp;
        temp = component.getToolTipText();
        switch(type){
            case CheckBox:
                temp = (new Boolean(((JCheckBox)component).isSelected())).toString();
                break;
            case Slider:
                temp = ((Integer)((JSlider)component).getValue()).toString();
                break;
            case TextBox:
            default:
                temp = ((JTextField)component).getText();
        }
        if(temp==null) temp="";
        return temp;
    }
}

//Constructs a model Dialog with ok/cancel from UserOptions enum.
//Similar to toolbar
public class OptionsGUI extends JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    private static final int padding = 6;
    private javax.swing.JLabel Logo;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private int returnStatus = RET_CANCEL;
    private JPanel optsPanel = new JPanel();
    UserOptions[] values = UserOptions.values();

    /** Creates new form TagTagger */
    public OptionsGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"CloseWindow");
        getRootPane().getActionMap().put("CloseWindow", new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                setVisible(false);
            }
        });
        initComponents();
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    void setAllValues(Settings setting){
        for(UserOptions option : values){
            option.initValue(setting);
        }
    }
    void saveAllValues(Settings setting){
        for(UserOptions option : values){
            option.saveValue(setting);
        }
    }

    void initComponents(){
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        Logo = new javax.swing.JLabel();

        setTitle("Options");
        setIconImage(null);
        setLocationByPlatform(true);
        setModal(true);
        //setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        Container  pane = getContentPane();
        pane.setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

        JPanel topBox = new JPanel();
        topBox.setBorder(BorderFactory.createEmptyBorder(padding, padding, 0, 0));
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.X_AXIS));
        topBox.add(Logo);
        topBox.add(Box.createHorizontalGlue());
        pane.add(topBox);

        pane.add(optsPanel);

        JPanel buttonsGroup = new JPanel();
        buttonsGroup.setBorder(BorderFactory.createEmptyBorder(0, 0, padding, padding));
        buttonsGroup.setLayout(new BoxLayout(buttonsGroup ,BoxLayout.LINE_AXIS));
        buttonsGroup.add(Box.createHorizontalGlue());
        buttonsGroup.add(okButton);
        buttonsGroup.add(Box.createRigidArea(new Dimension(padding,padding)));
        buttonsGroup.add(cancelButton);
        pane.add(buttonsGroup);

        Logo.setIcon(SysIcon.Options.Icon);

        rebuildOpts();
    }

    void rebuildOpts(){
        //For each value in enum create and add a setting to layout.
        optsPanel.setLayout(new SpringLayout());
        for (int i=0;i<values.length;i++){
            JLabel l = new JLabel(values[i].getLabel(),JLabel.TRAILING);
            optsPanel.add(l);
            JComponent c = values[i].getComponent();
            l.setLabelFor(c);
            optsPanel.add(c);
        }
        SpringUtilities.makeCompactGrid(optsPanel, values.length, padding);
        pack();
        setMinimumSize(getSize());
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(RET_OK);
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        //dispose();
    }

}
