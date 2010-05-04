
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

enum BlueTthOp{GetDevices,CheckServices}//Type of bluetooth operation toe perform

public class BlueDemo implements DiscoveryListener,Runnable {
    //object used for waiting

    private static Object lock = new Object();
    //vector containing the devices discovered
    private static Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
    private static String connectionURL = null;
    DiscoveryAgent agent;
    String[] devicelist = null;
    BluetoothGUI blueGUI=null;
    BlueTthOp runType=null;
    int devNo;
    boolean devNoIsSet = false;

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        //add the device to the vector
        if (!vecDevices.contains(btDevice)) {
            vecDevices.addElement(btDevice);
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (servRecord != null && servRecord.length > 0) {
            connectionURL = servRecord[0].getConnectionURL(0, false);
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        synchronized (lock) {
            lock.notify();
        }
    }

    public void inquiryCompleted(int discType) {
        synchronized (lock) {
            lock.notify();
        }
    }//end method

    static BlueDemo setup(BluetoothGUI BlueGUI) {
        BlueDemo blSeDi = new BlueDemo();
        blSeDi.blueGUI = BlueGUI;
        return blSeDi;
    }
    void setChosenDevice(int device){
        devNo = device;
        devNoIsSet=true;
    }
    void threadGetDevices(){
        runType = BlueTthOp.GetDevices;
        (new Thread(this)).start();
    }
    void threadCheckServices(int deviceNumber){
        setChosenDevice(deviceNumber);
        runType = BlueTthOp.CheckServices;
        (new Thread(this)).start();
    }
    @Override
    public void run(){
        if(blueGUI==null) return;
        if(runType==null) return;
        switch(runType){
            case GetDevices:
                getDevices();
                break;
            case CheckServices:
                if(devNoIsSet) checkServices();
                break;
            default:
                return;
        }

    }

    void getDevices(){
        //display local device address and name
        try{
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        blueGUI.message("Address: " + localDevice.getBluetoothAddress());
        blueGUI.message("Name: " + localDevice.getFriendlyName());
        //find devices
        agent = localDevice.getDiscoveryAgent();
        blueGUI.message("Starting device inquiry...");
        agent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch(javax.bluetooth.BluetoothStateException er){
            blueGUI.message("Unable to find bluetooth installed on this PC");
            blueGUI.bluetoothStartError();
            return;
        } catch (IOException er) {
            er.printStackTrace();
        }
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        blueGUI.message("Device Inquiry Completed. ");
        //print all devices in vecDevices
        int deviceCount = vecDevices.size();
        if (deviceCount <= 0) {
            blueGUI.message("No Devices Found .");
        } else {
            //print bluetooth device addresses and names in the format [ No. address (name) ]
            blueGUI.message("Bluetooth Devices: ");
            devicelist = new String[deviceCount];
            blueGUI.message("Found "+deviceCount+" devices. Getting device names.");
            for (int i = 0; i < deviceCount; i++) {
                RemoteDevice remoteDevice = (RemoteDevice) vecDevices.elementAt(i);
                try {
                    devicelist[i] = "" + remoteDevice.getBluetoothAddress() + ": " + remoteDevice.getFriendlyName(true);
                    blueGUI.message("Found: "+devicelist[i]);
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                finally {
                    blueGUI.message("...");
                }
            }
        }
        blueGUI.updateDevices(devicelist);
    }

    void checkServices(){
        int index = devNo + 1;
        //check for obex service
        RemoteDevice remoteDevice = (RemoteDevice) vecDevices.elementAt(index - 1);
        UUID[] uuidSet = new UUID[1];
        uuidSet[0] = new UUID("7e1e6390578211df98790800200c9a66", false);
        blueGUI.message("\nSearching for service...");
        try {
            agent.searchServices(null, uuidSet, remoteDevice, this);
        } catch (IOException er) {
            er.printStackTrace();
        }
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        blueGUI.message("Bluetooth service discovery:");
        if (connectionURL == null) {
            blueGUI.message("Device does not support required connection,\nor is no longer reachable");
            blueGUI.serviceCheckFinished(false);
        } else {
            blueGUI.message("Device supports RFCOMM bluetooth.");
            blueGUI.serviceCheckFinished(true);//Stops animation for now
            RFCOMM_Start(connectionURL);
        }
    }

    void RFCOMM_Start(String connectionURL){
        //String thisServerUUID="12358934876238497239847328957152";
        //LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
        try{
        blueGUI.message("Conection URL :"+connectionURL);
        StreamConnection port = (StreamConnection) Connector.open(connectionURL);
        InputStream portIn = port.openInputStream();
        OutputStream portOut = port.openOutputStream();

        portOut.write("Hello Android!!".getBytes());
        portOut.flush();

        portIn.close();
        portOut.close();
        port.close();
        blueGUI.message("Hello sent");
        } catch (IOException e){
            //Do somthing more here
            e.printStackTrace();
        }
        finally{
        }

        //Connector.open(connectionURL, READ_WRITE, timeout);

    }
}