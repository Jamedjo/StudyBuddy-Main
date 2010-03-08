
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.KeyStroke;

enum  UserOptions{
    SlideshowTime();

}

//Constructs a model Dialog with ok/cancel from UserOptions enum.
//Similar to toolbar
public class OptionsGUI extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    private javax.swing.JLabel Logo;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton okButton;
    private int returnStatus = RET_CANCEL;

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

    void initComponents(){
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        Logo = new javax.swing.JLabel();

        setTitle("Options");
        setIconImage(null);
        setModal(true);
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

        buildMain();

        Logo.setIcon(SysIcon.Options.Icon);

        pack();
    }

    void buildMain(){
        //For each value in enum create and add a setting to layout.
        //cancelButton,okButton,Logo must be added.
        Container  pane = getContentPane();
        pane.setLayout(new BoxLayout(getContentPane(),BoxLayout.PAGE_AXIS));
        pane.add(Logo);
        pane.add(okButton);
        pane.add(cancelButton);
        
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
//        returnStatus = retStatus;
        setVisible(false);
        //dispose();
    }

}

enum OptionType{
    Checkbox,
    Slider,
    TextBox;

}