
import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

enum BlueTthOp{GetDevices,CheckServices}//Type of bluetooth operation toe perform

/**
 *
 * Class that discovers all bluetooth devices in the neighbourhood,
 *
 * Connects to the chosen device and checks for the presence of OBEX push service in it.
 * and displays their name and bluetooth address.
 *
 * 
 */
public class BlueDemo implements DiscoveryListener,Runnable {
    //object used for waiting

    private static Object lock = new Object();
    //vector containing the devices discovered
    private static Vector vecDevices = new Vector();
    private static String connectionURL = null;
    DiscoveryAgent agent;
    String[] devicelist = null;
    BluetoothGUI blueGUI=null;
    BlueTthOp runType=null;
    int devNo;
    boolean devNoIsSet = false;

    /**
     * Called when a bluetooth device is discovered.
     * Used for device search.
     */
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        //add the device to the vector
        if (!vecDevices.contains(btDevice)) {
            vecDevices.addElement(btDevice);
        }
    }

    /**
     * Called when a bluetooth service is discovered.
     * Used for service search.
     */
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        if (servRecord != null && servRecord.length > 0) {
            connectionURL = servRecord[0].getConnectionURL(0, false);
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Called when the service search is over.
     */
    public void serviceSearchCompleted(int transID, int respCode) {
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Called when the device search is over.
     */
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
            for (int i = 0; i < deviceCount; i++) {
                RemoteDevice remoteDevice = (RemoteDevice) vecDevices.elementAt(i);
                try {
                    devicelist[i] = "" + remoteDevice.getBluetoothAddress() + ": " + remoteDevice.getFriendlyName(true);
                } catch (IOException e) {
                    //
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
        uuidSet[0] = new UUID("1105", true);
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
            blueGUI.message("Device does not support Object Push,\nor is no longer reachable");
            //return false;
        } else {
            blueGUI.message("Device supports OBEX Push.");
            //return true;
        }
    }
}
// <editor-fold defaultstate="collapsed" desc="main class from example usage">
//	public static void mains(String[] args){
//		BlueDemo bluetoothServiceDiscovery=new BlueDemo();
//		//display local device address and name
//		LocalDevice localDevice = LocalDevice.getLocalDevice();
//		System.out.println("Address: "+localDevice.getBluetoothAddress());
//		System.out.println("Name: "+localDevice.getFriendlyName());
//		//find devices
//		DiscoveryAgent agent = localDevice.getDiscoveryAgent();
//		System.out.println("Starting device inquiry...");
//		agent.startInquiry(DiscoveryAgent.GIAC, bluetoothServiceDiscovery);
//		try {
//			synchronized(lock){
//				lock.wait();
//			}
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("Device Inquiry Completed. ");
//		//print all devices in vecDevices
//		int deviceCount=vecDevices.size();
//		if(deviceCount <= 0){
//			System.out.println("No Devices Found .");
//		}
//		else{
//			//print bluetooth device addresses and names in the format [ No. address (name) ]
//			System.out.println("Bluetooth Devices: ");
//			for (int i = 0; i <deviceCount; i++) {
//				RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(i);
//				System.out.println((i+1)+". "+remoteDevice.getBluetoothAddress()+" ("+remoteDevice.getFriendlyName(true)+")");
//			}
//		}
//		System.out.print("Choose the device to search for Obex Push service : ");
//		BufferedReader bReader=new BufferedReader(new InputStreamReader(System.in));
//		String chosenIndex=bReader.readLine();
//		int index=Integer.parseInt(chosenIndex.trim());
//		//check for obex service
//		RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(index-1);
//		UUID[] uuidSet = new UUID[1];
//		uuidSet[0]=new UUID("1105",true);
//		System.out.println("\nSearching for service...");
//		agent.searchServices(null,uuidSet,remoteDevice,bluetoothServiceDiscovery);
//		try {
//			synchronized(lock){
//				lock.wait();
//			}
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		if(connectionURL==null){
//			System.out.println("Device does not support Object Push.");
//		}
//		else{
//			System.out.println("Device supports Object Push.");
//		}
//	}// </editor-fold>
