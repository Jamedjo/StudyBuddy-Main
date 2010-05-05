
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class BluetoothGUI extends javax.swing.JDialog {
    Log log = new Log(false);
    GUI mainGUI;
    BlueDemo blD;
    Object[] DevIDs;

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form TagTagger */
    public BluetoothGUI(java.awt.Frame parent, boolean modal, GUI mainGui) {
        super(parent, modal);
        mainGUI=mainGui;
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"CloseWindow");
        getRootPane().getActionMap().put("CloseWindow", new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                setVisible(false);
            }
        });
        initComponents();
        Logo.setText(null);
        message("Click Search to find Bluetooh devices.\n" +"This may take some time to respond");
        setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        leftPanel1 = new javax.swing.JPanel();
        loadingAnimation = new LoadingAnimationPane(false);
        phoneSearch = new javax.swing.JButton();
        existingPhone = new javax.swing.JButton();
        deviceNames = new javax.swing.JComboBox();
        connectButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();
        cancelButton = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        Logo = new javax.swing.JLabel();

        setTitle("Bluetooth");
        setIconImage(SysIcon.BlueTooth.Icon.getImage());
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        javax.swing.GroupLayout loadingAnimationLayout = new javax.swing.GroupLayout(loadingAnimation);
        loadingAnimation.setLayout(loadingAnimationLayout);
        loadingAnimationLayout.setHorizontalGroup(
            loadingAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 208, Short.MAX_VALUE)
        );
        loadingAnimationLayout.setVerticalGroup(
            loadingAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 130, Short.MAX_VALUE)
        );

        phoneSearch.setText("Phone Search");
        phoneSearch.setActionCommand("FindNew");
        phoneSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneSearchActionPerformed(evt);
            }
        });

        existingPhone.setText("Existing phone");
        existingPhone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingPhoneActionPerformed(evt);
            }
        });

        deviceNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Device Names" }));
        deviceNames.setEnabled(false);

        connectButton.setText("Connect");
        connectButton.setEnabled(false);
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftPanel1Layout = new javax.swing.GroupLayout(leftPanel1);
        leftPanel1.setLayout(leftPanel1Layout);
        leftPanel1Layout.setHorizontalGroup(
            leftPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loadingAnimation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftPanel1Layout.createSequentialGroup()
                .addComponent(phoneSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(existingPhone))
            .addComponent(deviceNames, 0, 208, Short.MAX_VALUE)
            .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );
        leftPanel1Layout.setVerticalGroup(
            leftPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(leftPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(existingPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deviceNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadingAnimation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        logArea.setColumns(20);
        logArea.setEditable(false);
        logArea.setRows(5);
        jScrollPane1.setViewportView(logArea);

        cancelButton.setText("Done");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        progressBar.setFocusable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(145, Short.MAX_VALUE)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );

        Logo.setIcon(SysIcon.BlueTooth.Icon);
        Logo.setText("Icon");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(Logo)
                .addContainerGap(6, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(Logo)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(leftPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(leftPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void message(final String newMsg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logArea.append(newMsg + "\n");
            }
        });
    }

    public void setProgress(int value){
        progressBar.setValue(value);
    }

    private void existingPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingPhoneActionPerformed
        this.getDevicesBluetooth(false);//badly named 'searchButton'
    }
    
    private void getDevicesBluetooth(boolean shouldSearch){
        existingPhone.setEnabled(false);
        phoneSearch.setEnabled(false);
        ((LoadingAnimationPane)loadingAnimation).startAnimation();
        blD = BlueDemo.setup(this);
        blD.threadGetDevices(shouldSearch);
    }

    //Called from seperate thread, updates list of devices
    public void bluetoothStartError() {
        ((LoadingAnimationPane)loadingAnimation).stopAnimation();
        existingPhone.setEnabled(true);
    }

    //Called from seperate thread, updates list of devices
    public void updateDevices(final Object[] devicelist) {
        ((LoadingAnimationPane)loadingAnimation).stopAnimation();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DevIDs = devicelist;
                if (DevIDs == null || DevIDs.length == 0) {
                    message("No Bluetooth devices could be found,\nPlease ensure phone is on and near by.");
                } else {
                    message("Bluetooth Devices Found");
                    message("Which device would you like to use?");
                    deviceNames.setModel(new DefaultComboBoxModel((String[]) DevIDs));
                    deviceNames.setEnabled(true);
                    connectButton.setEnabled(true);
                }
            }
        });
    }//GEN-LAST:event_existingPhoneActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        ((LoadingAnimationPane)loadingAnimation).startAnimation();
        deviceNames.setEnabled(false);
        connectButton.setEnabled(false);
        int chosenDevId = -1;
        for (int i = 0; i < DevIDs.length; i++) {
            if (((String) deviceNames.getSelectedItem()).equals(DevIDs[i])) {
                chosenDevId = i;
                i = (Integer.MAX_VALUE - 1);
            }
        }
        blD.threadCheckServices(chosenDevId);
    }//GEN-LAST:event_connectButtonActionPerformed

    public void deviceConnected(String connectionURL){
        blD.RFCOMM_Start(connectionURL,mainGUI.mainImageDB);
    }

    private void phoneSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneSearchActionPerformed
        this.getDevicesBluetooth(true);//badly named 'searchButton'
    }//GEN-LAST:event_phoneSearchActionPerformed

    public void serviceCheckFinished(boolean serviceSupported){
        ((LoadingAnimationPane)loadingAnimation).stopAnimation();
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        //dispose();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                BluetoothGUI dialog = new BluetoothGUI(new javax.swing.JFrame(), true,new GUI());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Logo;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JComboBox deviceNames;
    private javax.swing.JButton existingPhone;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel1;
    private javax.swing.JPanel loadingAnimation;
    private javax.swing.JTextArea logArea;
    private javax.swing.JButton phoneSearch;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}
