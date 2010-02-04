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
    //Default setting values moved to AppDefaults.java and controlled by an enum.

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
            System.err.println("path doesnt exist: "+appPath);
            //Make directory appPath
            boolean success = (new File(appPath)).mkdir();
            if(!success)
                System.err.println("StudyBuddy Directory could not be created");
        }
        //else System.out.println("path exists :)");

        propFile = new File(appPath+propertiesFile);
        if (!propFile.exists()) {
            System.err.println("properties files doesnt exist");
            //create propertiesFile file with default values as properties
            try{
                propFile.createNewFile();
                //System.out.println("New file created");
                setupJavaPropertiesObject();
                setDefaults();
            }
            catch(Exception e){
                System.err.println("File couldnt be created");
                //Throw custom error?
            }
        }
        else {
            //System.out.println("Properties file already exists");
            setupJavaPropertiesObject();
            setDefaults(); //if version is different (as this means new defaults may have been added)
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
        AppDefaults.set(this);
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

    //public boolean containsSetting(String settingName)
    //containskey

    public static void main(String[] args){
        Settings props = new Settings();
        System.out.println("Os is windows?..."+props.isWindows());
        System.out.println("(private) StudyBudy folder is: "+props.appPath);
        System.out.println("(public) getSetting finds StudyBudy folder as: "+props.getSetting("homeDir"));
        props.setSettingAndSave("testSaveAllSettigngsHere", "settingsvalue");//Test setting a property in the file and saving all settings to file
        AppDefaults.getAndPrint(props);
        props.setSettingDontSave("numberOfThumbnails", "5");//Test setting a property in the file. Doesn't save Properties to file.
        System.out.println("Test property set at '5' has value: "+props.getSetting("numberOfThumbnails"));
        props = null;

        //Simulate closing StudyBudy and running a second time

        //Any settings which are not saved will no longer exist and return null
        props = new Settings();
        System.out.println("\nFirst default setting has value: "+props.getSetting(AppDefaults.s1.key));
        System.out.println("(unsaved) Test property set at '5' has value: "+props.getSetting("numberOfThumbnails"));

    }
}
