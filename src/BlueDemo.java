import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
/**
 *
 * Class that discovers all bluetooth devices in the neighbourhood,
 *
 * Connects to the chosen device and checks for the presence of OBEX push service in it.
 * and displays their name and bluetooth address.
 *
 *
 */
public class BlueDemo implements DiscoveryListener{
	//object used for waiting
	private static Object lock=new Object();
	//vector containing the devices discovered
	private static Vector vecDevices=new Vector();
	private static String connectionURL=null;
        DiscoveryAgent agent;
        String[] devicelist = null;

	public static void mains(String[] args){
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
	}
	/**
	 * Called when a bluetooth device is discovered.
	 * Used for device search.
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		//add the device to the vector
		if(!vecDevices.contains(btDevice)){
			vecDevices.addElement(btDevice);
		}
	}
	/**
	 * Called when a bluetooth service is discovered.
	 * Used for service search.
	 */
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		if(servRecord!=null && servRecord.length>0){
			connectionURL=servRecord[0].getConnectionURL(0,false);
		}
		synchronized(lock){
			lock.notify();
		}
	}
	/**
	 * Called when the service search is over.
	 */
	public void serviceSearchCompleted(int transID, int respCode) {
		synchronized(lock){
			lock.notify();
		}
	}
	/**
	 * Called when the device search is over.
	 */
	public void inquiryCompleted(int discType) {
		synchronized(lock){
			lock.notify();
		}
	}//end method

    static BlueDemo BlueTester() throws IOException {
        BlueDemo blSeDi =new BlueDemo();
		//display local device address and name
		LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: "+localDevice.getBluetoothAddress());
		System.out.println("Name: "+localDevice.getFriendlyName());
		//find devices
		blSeDi.agent = localDevice.getDiscoveryAgent();
		System.out.println("Starting device inquiry...");
		blSeDi.agent.startInquiry(DiscoveryAgent.GIAC, blSeDi);
		try {
			synchronized(lock){
				lock.wait();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Device Inquiry Completed. ");
		//print all devices in vecDevices
		int deviceCount=vecDevices.size();
		if(deviceCount <= 0){
			System.out.println("No Devices Found .");
		}
		else{
			//print bluetooth device addresses and names in the format [ No. address (name) ]
			System.out.println("Bluetooth Devices: ");
                        blSeDi.devicelist = new String[deviceCount];
			for (int i = 0; i <deviceCount; i++) {
				RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(i);
				try{
                                blSeDi.devicelist[i]= ""+remoteDevice.getBluetoothAddress()+": "+remoteDevice.getFriendlyName(true);
                                } catch (IOException e){
                                    //
                                }
			}
		}
                return blSeDi;
		
    }

    static boolean probeProtocol(BlueDemo blSeDi,int devNo) throws IOException{
        
		int index=devNo+1;
		//check for obex service
		RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(index-1);
		UUID[] uuidSet = new UUID[1];
		uuidSet[0]=new UUID("1105",true);
		System.out.println("\nSearching for service...");
		blSeDi.agent.searchServices(null,uuidSet,remoteDevice,blSeDi);
		try {
			synchronized(lock){
				lock.wait();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(connectionURL==null){
			System.out.println("Device does not support Object Push.");
                        return false;
		}
		else{
			System.out.println("Device supports Object Push.");
                        return true;
		}
    }

}