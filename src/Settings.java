import java.util.Properties;//Imports need to be fixed here
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.FileNotFoundException;

public class Settings {
    //String path;
    private String appPath;
    private final String propertiesFile = "StudyBuddy.properties";//Name of the file use to store properties
    private File propFile;
    private Properties javaProperties;

    //Default values
    private final String testDefault1val = "testval1";
    private final String testDefault2val = "22222";
    private final String testDefault3val = "/thumbnaildir/small/test/";

    Settings(){
        appPath = (System.getProperty("user.home"));
        if(isWindows()){
            appPath = appPath + "\\Application Data\\StudyBuddy\\";//Windows uses backslash. Two needed as escape sequence
        }
        else{
            appPath = appPath + "/.StudyBuddy/";//put back '.' could someone on linux test this?
        }

        File folder = new File(appPath);
        if (!folder.isDirectory()) {
            System.out.println("path doesnt exist");
            //Make directory appPath
            boolean success = (new File(appPath)).mkdir();
            if(success)
                System.out.println("Directory made");
            else
                System.out.println("Directory not made");
        }
        else 
            System.out.println("path exists :)");

        propFile = new File(appPath+propertiesFile);
        if (!propFile.exists()) {
            System.out.println("properties files doesnt exist");
            //create propertiesFile file with default values as properties
            try{
                propFile.createNewFile();
                System.out.println("New file created");
                setupJavaPropertiesObject();
                setDefaults();
            }
            catch(Exception e){
                System.out.println("File couldnt be created");
                //Throw custom error?
            }
        }
        else {
            System.out.println("Properties file already exists");
            setupJavaPropertiesObject();
        }

        
    }

    private void setupJavaPropertiesObject() {
        javaProperties = new Properties();
        try {
            FileInputStream inProps = new FileInputStream(propFile);
            javaProperties.load(inProps);
            inProps.close();
        } catch (IOException e) {
            System.err.println("Unable to load properties from file: " + appPath + propertiesFile);
        }
    }

    public boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win")>=0);
    }

    private void setDefaults(){
        setSettingDontSave("homeDir",appPath);
        setSettingDontSave("testDefault1",testDefault1val);
        setSettingDontSave("testDefault2",testDefault2val);
        setSettingAndSave("testDefault3",testDefault3val);
    }

    //Use this when setting many settings. Use setSettingAndSave for final one.
    public void setSettingDontSave(String settingName, String settingValue){
        javaProperties.setProperty(settingName, settingValue);
    }
    
    public void setSettingAndSave(String settingName, String settingValue){
        javaProperties.setProperty(settingName, settingValue);
        saveSettings();
    }

    public void saveSettings(){
        try{
        FileOutputStream outProps = new FileOutputStream(propFile);
        javaProperties.store(outProps, "---Empty Comment---");
        outProps.close();
        } catch (IOException e){
            System.err.println("Unable to save settings to file: "+propFile);
        }// catch (FileNotFoundException e){}
    }

    //see http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
    public String getSetting(String settingName){
        return javaProperties.getProperty(settingName);
    }


    public static void main(String[] args){
        Settings props = new Settings();
        System.out.println("(private) StudyBudy folder is:\n"+props.appPath);
        System.out.println("\n(public) getSetting finds StudyBudy folder as:\n"+props.getSetting("homeDir"));
        props.setSettingAndSave("testSaveAllSettigngsHere", "value");//Test setting a property in the file and saving all settings to file
        props.setSettingDontSave("numberOfThumbnails", "5");//Test setting a property in the file. Doesn't save Properties to file.
        System.out.println("\n test property set at '5' has value: "+props.getSetting("numberOfThumnails"));
        System.out.println("default property test value 1: "+props.getSetting("testDefault1"));
        System.out.println("default property test value 2: "+props.getSetting("testDefault1"));
        System.out.println("default property test value 3: "+props.getSetting("testDefault1"));
        System.out.println("Os is windows?..."+props.isWindows());
    }
}
