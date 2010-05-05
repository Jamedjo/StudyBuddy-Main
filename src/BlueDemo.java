
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final Object lock = new Object();
    private static String connectionURL = null;
    DiscoveryAgent agent;
    String[] deviceNames = null;
    BluetoothGUI blueGUI=null;
    BlueTthOp runType=null;
    boolean shouldSearch=true;
    int devNo;
    boolean devNoIsSet = false;
    String newMobileDBValues;
    String nextFileName;
    String imageStorePath="C:\\sdjlfkasklfjskladsjfioewtuierwhgks";

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
        blueGUI.setProgress(0);
        while(it.hasNext()){
            KnownDevice kDev= it.next();
            //devNames[i] = kDev.getAddress()+": "+kDev.getName();
            devNames[i] = kDev.getName();
            i++;
            blueGUI.setProgress((100/deviceList.size())*i);
        }
        return devNames;
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (servRecord != null && servRecord.length > 0) {
            connectionURL = servRecord[0].getConnectionURL(0, false);
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
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
    void threadGetDevices(boolean search){
        runType = BlueTthOp.GetDevices;
        shouldSearch=search;
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
            blueGUI.deviceConnected(connectionURL);
        }
    }

    void RFCOMM_Start(String connectionURL, ImageDatabase mainDB){
        try{
        blueGUI.message("Conection URL :"+connectionURL);
        StreamConnection port = (StreamConnection) Connector.open(connectionURL);
        //StreamConnection port = (StreamConnection) Connector.open(connectionURL, READ_WRITE, timeout);
        InputStream portIn = port.openInputStream();
        OutputStream portOut = port.openOutputStream();

        BlueFrame frameSender = new BlueFrame(blueGUI,portOut);

        recieveFrames(portIn);
        //call assignMobileItemsIDs with string recieved (and place to store images)
        //send return value back
        //call make changes from moblile and print return value

        System.out.println("Calling mainDB.assignMobileItemsIDs(newMobileDBValues, imageStorePath)\nRecieved: "+newMobileDBValues);
        frameSender.sendString(FrameType.NewDBValues, mainDB.assignMobileItemsIDs(newMobileDBValues, imageStorePath));
        frameSender.sendCommand(FrameType.FinishedSending);
        mainDB.print();
        recieveFrames(portIn);

//        File[] files = mainDB.imageFilenamesForMobile();
//        String updateString=mainDB.makeUpdateString();
//
//        frameSender.sendString(FrameType.Text, "Hello Android!!");
//
//        frameSender.sendCommand(FrameType.ImagesStart);
//        frameSender.sendImage(FrameType.Image, "zoomSmall32.png",(new File("D:\\Users\\Student\\Documents\\NetBeansProjects\\StudyBuddyMarch\\etc\\icons\\oxygencustom\\zoomSmall32.png")));
//        frameSender.sendImage(FrameType.Image, "img_6088b_small.jpg",(new File("D:\\Users\\Student\\Documents\\NetBeansProjects\\StudyBuddyMarch\\etc\\img\\img_6088b_small.jpg")));
//
//        frameSender.sendCommand(FrameType.ImagesDone);
//
//        frameSender.sendString(FrameType.Text, "Hello Android!!!!!!!!!\nThis is multi-line!!!!!!!!");
//        frameSender.sendString(FrameType.Text, updateString);
//        frameSender.sendCommand(FrameType.FinishedSending);
//
//        portOut.flush();
//        blueGUI.message("Hello sent");
//
//        recieveFrames(portIn);

        portIn.close();
        portOut.close();
        port.close();

        } catch (IOException e){
            //Do somthing more here
            e.printStackTrace();
        }
        finally{
        }
    }


    void recieveFrames(InputStream portIn){
        try{
            while(true){
                FrameType type = readFrameType(portIn);
                if(type==FrameType.FinishedSending) {
                    System.out.println("Finished recieving");
                    break;
                }
                if(type.isCommand()) {
                    System.out.println("Got command: "+type.toString());

                    continue;//Dosn't need to read length, does nothing for now.
                }

                System.out.println("Got frame type: "+type.toString());
                int length = readFrameLength(portIn);

                switch(type){
                    case Text:
                        System.out.println(readFrameText(portIn, length));
                    case Image:
                        FileOutputStream fileOut = new FileOutputStream(imageStorePath+nextFileName);
                        this.readFrameImage(portIn, fileOut, length);
                        break;
                    //case ImagesStart:
                    //case ImagesDone:
                    case ImageFileName:
                        nextFileName=readFrameText(portIn,length);
                        break;
                    case NewDBValues:
                        newMobileDBValues=readFrameText(portIn, length);
                        break;
                    //case FinishedSending:
                    //case ErrorValue:
                    //default:
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    FrameType readFrameType(InputStream portIn) throws IOException{
        return FrameType.byteToType((byte)portIn.read());
    }
    int readFrameLength(InputStream portIn) throws IOException{
        byte[] b = new byte[4];
        portIn.read(b);//should throw error if failed
        int l = (b[0] & 0xff) + ((b[1]&0xff)<<8) + ((b[2]&0xff)<<16) + ((b[3]&0xff)<<24);
        return l;
    }
    String readFrameText(InputStream portIn,int length) throws IOException{
        return new String(readFrameData(portIn, length));
    }
    byte[] readFrameData(InputStream portIn,int length) throws IOException{
        byte[] data = new byte[length];
        portIn.read(data);
        return data;
    }
    void readFrameImage(InputStream portIn,FileOutputStream fileOut,int length){
        
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
                Log.Print(LogType.Error, "Unable to resolve device name");
                //e.printStackTrace();
            }
        }
        if (devName==null) return "Unknown Device";
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