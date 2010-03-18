//import bartelo.javax.microedition.io.HttpConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.microedition.io.*;
import java.io.*;
import javax.bluetooth.*; //comes from bluecove
//import javax.obex.*;
// uses the de.avetana OBEX library


public class OBEX_Demo  implements ActionListener, Runnable{
     // Bluetooth singleton object
       LocalDevice device;
       DiscoveryAgent agent;
       String HTBTurl = null;
       Boolean mServerState = false; // stop is default state

       Thread mServer = null;
       String msgOut = "srv out msg";
       String msgIn = "no msg rcv";
       StreamConnectionNotifier btServerNotifier     ;
       UUID uuid = new UUID("9106", true);
       JLabel spacerlabel = new JLabel(" ");
	JButton startButton = new JButton("Start Server");
	JTextArea textarea = new JTextArea("",20, 40);
       JButton endButton = new JButton("End Server");

 	public OBEX_Demo(){

		//Give it the Java look and feel
		JFrame.setDefaultLookAndFeelDecorated(true);

		JFrame frame = new JFrame("FileServer ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane(textarea);
		textarea.setEditable(false);

		Container cp = frame.getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

		startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		startButton.addActionListener(this);
               cp.add(startButton);

		endButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		endButton.addActionListener(this);
               cp.add(endButton);


		spacerlabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		cp.add(spacerlabel);

		scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		cp.add(scrollPane);

		frame.pack();
		frame.setVisible(true);

		updateStatus("[server:] FileServer Application started");
		updateStatus("[server:] Press the \"Start Server\" button to await for client devices");


	}

       private void startServer() {
           if (mServer !=null)
               return;
           //start the server and receiver
           mServer = new Thread(this);
           mServer.start();
       }

       private void endServer() {
           if (mServer == null)
               return;
           try {
               mServer.join();
           } catch (Exception ex) {};
           mServer = null;

       }




       public void run(){
		try {
			//UUID uuid = new UUID("1106", true);
                       UUID uuid = new UUID("9106", true);
                       device = LocalDevice.getLocalDevice(); // obtain reference to singleton
                       device.setDiscoverable(DiscoveryAgent.GIAC); // set Discover mode to LIAC
               }catch (Exception e)
                          { System.err.println("Cant init set discvover");
                            e.printStackTrace();
               }
			String url = "btspp://localhost:" + uuid + ";name=BTTP;authenticate=false;master=false;encrypt=false";
//Bluecove does not support btgeop on server
//			String url = "btgoep://localhost:" + uuid + ";name=BTTP;authenticate=false;master=false;encrypt=false";

		try{

                 // obtain connection and stream to this service
                   btServerNotifier = (StreamConnectionNotifier) Connector.open( url );
               } catch ( Exception e) {
                   e.printStackTrace();
               }


               while (mServerState )
                 {
                      StreamConnection btConn = null;
                      try {
                         updateStatus("[server:] Now waiting for a client to connect");

                         btConn = btServerNotifier.acceptAndOpen();
                     } catch (IOException ioe) { }
                     if (btConn != null) processConnection(btConn);
               }
       }

       void processConnection(StreamConnection conn) {
	    updateStatus("[server:] A client is now connected");

           try {
                 DataInputStream in = conn.openDataInputStream();

//                  // write data into serial stream
//                  msgIn = in.readUTF();

               char s ='a';

               String h =null;
               try {
                   while (s!='\n'){
                       s = in.readChar();
                       System.out.println(s);
                       h += s;
                   }
                   System.out.println(s); // typing the result
               } catch (IOException ex) {
                   System.out.println("unable to handle incoming data");
                   ex.printStackTrace();
               }
                 msgIn = h;
                 in.close();
                 System.out.print("The receive message is '" + msgIn + "'");

                  Thread.sleep(1000);

                  DataOutputStream out = conn.openDataOutputStream();
                  msgOut = msgIn +" srv reply";
                  System.out.print("Sending the message is '" + msgOut + "'");

                 // write data into serial stream
                 out.writeUTF( msgOut );
                 out.flush();


                  Thread.sleep(1000);

                 // finish, close output stream
                 out.close();

               } catch (Exception e)
               {
                 e.printStackTrace();

               }
               try {
                   conn.close();
 		    updateStatus("[server:] Finished connection");

               }catch (Exception e ){ }


       }

	public void actionPerformed(ActionEvent e) {
               if ((e.getActionCommand()).equals("Start Server")    ) {
                   startButton.setEnabled(false);
                   mServerState = true; // set server state started
                   startServer();
               }
               if ((e.getActionCommand()).equals("End Server")    ) {
                   endButton.setEnabled(false);
                   startButton.setEnabled(true);
                   mServerState = false;
                   endServer();

                }

	}



	public void updateStatus(String message){
		textarea.append("\n" + message);

	}


	public static void main(String[] args) {
		new OBEX_Demo();
	}
}
