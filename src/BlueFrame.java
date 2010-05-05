
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

enum FrameTypeGroup{Command,String,Data}

enum FrameType{
    Text(FrameTypeGroup.String,                     (byte) 0x00)
    ,Image(FrameTypeGroup.Data,                     (byte) 0x01)
    ,ImagesStart(FrameTypeGroup.Command,            (byte) 0x02)
    ,ImagesDone(FrameTypeGroup.Command,             (byte) 0x03)
    ,ImageFileName(FrameTypeGroup.String,           (byte) 0x04)
    ,FinishedSending(FrameTypeGroup.Command,        (byte) 0x05)
    ,NewDBValues(FrameTypeGroup.String,             (byte) 0x06)

    ,CommunicationsFinished(FrameTypeGroup.Command, (byte) 0xEE)
    ,ErrorValue(FrameTypeGroup.Command,             (byte) 0xFF)
            ;

    byte val;
    FrameTypeGroup group;

    FrameType(FrameTypeGroup frameGroup, byte value){
        val=value;
        group=frameGroup;
    }
    public boolean isCommand(){
        if(group==FrameTypeGroup.Command) return true;
        return false;
    }
    public boolean isString(){
        if(group==FrameTypeGroup.String) return true;
        return false;
    }
    //hasLength
    //isImage
    public static FrameType byteToType(byte t){
        //should use switch statement for speed later
        for(FrameType type : FrameType.values()){
            if(type.val==t) return type;
        }
        return ErrorValue;
    }
}