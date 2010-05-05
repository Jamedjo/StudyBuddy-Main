
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
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
    private static Vector<KnownDevice> deviceList = new Vector<KnownDevice>();

    private static Object lock = new Object();
    private static String connectionURL = null;
    DiscoveryAgent agent;
    String[] deviceNames = null;
    BluetoothGUI blueGUI=null;
    BlueTthOp runType=null;
    int devNo;
    boolean devNoIsSet = false;

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        addDevice(btDevice);
    }
    
    void addDevice(RemoteDevice btDevice){
        Iterator<KnownDevice> it = deviceList.iterator();
        boolean contains = false;
        while(it.hasNext()&&(contains==false)){
            if(it.next().equalsDev(btDevice)) contains=true;
        }
        if (!contains) {
            deviceList.addElement(new KnownDevice(btDevice,blueGUI));
        }
    }
    
    String[] getDeviceNames(){
        Iterator<KnownDevice> it = deviceList.iterator();
        String[]devNames = new String[deviceList.size()];
        int i=0;
        while(it.hasNext()){
            KnownDevice kDev= it.next();
            //devNames[i] = kDev.getAddress()+": "+kDev.getName();
            devNames[i] = kDev.getName();
            i++;
        }
        return devNames;
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
        boolean shouldSearch=true;//Set false to only use Preknown and cached devices

        try{
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        //blueGUI.message("Address: " + localDevice.getBluetoothAddress());
        blueGUI.message("Computer Name: " + localDevice.getFriendlyName());
        agent = localDevice.getDiscoveryAgent();
        blueGUI.message("Searching for devices...");


        RemoteDevice[] list = agent.retrieveDevices(DiscoveryAgent.PREKNOWN);
        for(RemoteDevice d :list){
            addDevice(d);
        }
        list = agent.retrieveDevices(DiscoveryAgent.CACHED);
        for(RemoteDevice d :list){
            addDevice(d);
        }
        if(shouldSearch) agent.startInquiry(DiscoveryAgent.GIAC, this);


        } catch(javax.bluetooth.BluetoothStateException er){
            blueGUI.message("Unable to find bluetooth installed on this PC");
            blueGUI.bluetoothStartError();
            return;
        } catch (IOException er) {
            er.printStackTrace();
        }
        if(shouldSearch) {
                try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //blueGUI.message("Device Search Finished.");
        if (deviceList.size() > 0) {
            //blueGUI.message("Bluetooth Devices: ");
            blueGUI.message("Found "+deviceList.size()+" devices. Getting device names.");
            deviceNames = getDeviceNames();
        } else {
            blueGUI.message("No Bluetooth Devices Found.");
        }
        blueGUI.updateDevices(deviceNames);
    }

    void checkServices(){
        int index = devNo + 1;
        //check for custom service
        RemoteDevice remoteDevice = deviceList.elementAt(index - 1).getDevice();
        UUID[] uuidSet = new UUID[1];
        uuidSet[0] = new UUID("7e1e6390578211df98790800200c9a66", false);
        blueGUI.message("\nSearching for StudyBuddy on phone...");
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
       //blueGUI.message("Looking for StudyBuddy on phone:");
        if (connectionURL == null) {
            blueGUI.message("StudyBuddy is not running on chosen phone,\nor phone has disconnected");
            blueGUI.serviceCheckFinished(false);
        } else {
            blueGUI.message("Found StudyBuddy on remote phone.");
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

        BlueFrame frameSender = new BlueFrame(portOut);

        frameSender.sendString(FrameType.Text, "Hello Android!!");
        
        frameSender.sendCommand(FrameType.ImagesStart);
        frameSender.sendImage(FrameType.Image, "zoomSmall32.png",(new File("D:\\Users\\Student\\Documents\\NetBeansProjects\\StudyBuddyMarch\\etc\\icons\\oxygencustom\\zoomSmall32.png")));
        frameSender.sendImage(FrameType.Image, "img_6088b_small.jpg",(new File("D:\\Users\\Student\\Documents\\NetBeansProjects\\StudyBuddyMarch\\etc\\img\\img_6088b_small.jpg")));

        frameSender.sendCommand(FrameType.ImagesDone);

        frameSender.sendString(FrameType.Text, "Hello Android!!!!!!!!!\nThis is multi-line!!!!!!!!");

        portOut.flush();

        blueGUI.message("Hello sent");
        BufferedReader inReader = new BufferedReader(new InputStreamReader(portIn));
                        String in;
                        if ((in = inReader.readLine()) != null) {
                            blueGUI.message("Read: '" + in + "'");
                        }


        portIn.close();
        portOut.close();
        port.close();

        } catch (IOException e){
            //Do somthing more here
            e.printStackTrace();
        }
        finally{
        }

        //Connector.open(connectionURL, READ_WRITE, timeout);

    }
}

class KnownDevice{
    BluetoothGUI blueGUI;
    private RemoteDevice device;
    private String devName = null;
    private String address = null;

    KnownDevice(RemoteDevice d,BluetoothGUI bGUI){
        device = d;
        blueGUI=bGUI;
    }

    String getName(){
        if(devName==null){
            try{
                devName = device.getFriendlyName(true);
                blueGUI.message("Found: "+devName);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return devName;
    }

    String getAddress(){
        if(address==null){
            address = device.getBluetoothAddress();
        }
        return address;
    }

    boolean equalsDev(RemoteDevice d){
        return (device==d);
    }

    RemoteDevice getDevice(){
        return device;
    }
}