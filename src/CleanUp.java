
import java.io.File;

public class CleanUp {
    public static void main(String[] args){
        Settings settings = new Settings();
        File propFile =settings.getPropertiesFile();
        boolean outcome = true;
        if(propFile.exists()&&propFile.isFile()){
            try{
            propFile.delete();
            } catch (Exception e){
                System.err.println("Failed to clean up");
                outcome=false;
            }
        }
        InitDemoDB.initDB(settings.getSetting("homeDir")+settings.getSetting("databasePathExt")+settings.getSetting("databaseFileName"));
        if(outcome) System.out.println("Cleanup Complete");
    }
}
