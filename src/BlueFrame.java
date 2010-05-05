
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BlueFrame {
    private OutputStream outPort;
    BluetoothGUI blueGUI;

    public BlueFrame(BluetoothGUI blueGui, OutputStream portOut){
        outPort=portOut;
        blueGUI=blueGui;
    }
    
    //Need to make sure FrameType is valid for command
    public boolean sendString(FrameType type,String text){
            //byte[] message = (text+"\n").getBytes();
            byte[] message = (text).getBytes();
            
        try{
            if(writeMessageHeader(type.val,message.length)){
            outPort.write(message);
                return true;
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean sendCommand(FrameType type){
        try{
            outPort.write(type.val);
            return true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }
    public boolean sendImage(FrameType type,String filename,File imageFile){
        try{
            if(sendString(FrameType.Text,filename)){
                Log.Print(LogType.Log, "Sending file: "+filename);
                if(writeMessageHeader(type.val, (int)imageFile.length())){
                    FileInputStream is = new FileInputStream(imageFile);
                    blueGUI.setProgress(0);
                    for(int i=0;i<imageFile.length();i++){
                        outPort.write(is.read());
                        //System.out.print(".");
                        //System.out.flush();
                        blueGUI.setProgress((100/(int)imageFile.length())*i);
                    }
                    blueGUI.setProgress(100);
                Log.Print(LogType.Log, "Sent"+filename);
                    return true;
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean writeMessageHeader(byte typeByte, int l){
        try{
            outPort.write(typeByte);
            byte[] len = new byte[] {(byte)l, (byte)(l>>8), (byte)(l>>16), (byte)(l>>24)};//Converts int to byte[]
            outPort.write(len);
            return true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}

enum FrameType{
    Text((byte) 0x00)
    ,Image((byte) 0x01)
    ,ImagesStart((byte) 0x02)
    ,ImagesDone((byte) 0x03)
    //,ImageFileName((byte) 0x04)

            ;

    byte val;

    FrameType(byte value){
        val=value;
    }
}