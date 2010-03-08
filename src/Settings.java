import java.util.Properties;//Imports need to be fixed here
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.FileNotFoundException;

public class Settings {
    Log log = new Log();
    //String path;
    private String appPath;
    private final String propertiesFile = "StudyBuddy.properties";//Name of the file use to store properties
    private File propFile;
    private Properties javaProperties;
    //Default setting values moved to AppDefaults.java and controlled by an enum.

    Settings(){
        appPath = (System.getProperty("user.home"));
        if(isWindows()){
            //appPath = appPath + "\\Application Data\\StudyBuddy\\";//Windows uses backslash. Two needed as escape sequence
            appPath = System.getenv("APPDATA") + "\\StudyBuddy\\"; //Above invalid on non default systems (e.g. non english, vista, etc.)
        }
        else{
            appPath = appPath + "/.StudyBuddy/";//put back '.' could someone on linux test this?
        }

        File folder = new File(appPath);
        if (!folder.isDirectory()) {
            //log.print(LogType.Error,"path doesnt exist: "+appPath);
            //Make directory appPath
            boolean success = (new File(appPath)).mkdir();
            if(!success)
                log.print(LogType.Error,"StudyBuddy Directory could not be created");
        }
        //else log.print(LogType.Debug,"path exists :)");

        propFile = new File(appPath+propertiesFile);
        if (!propFile.exists()) {
            //log.print(LogType.Error,"properties files doesnt exist");
            //create propertiesFile file with default values as properties
            try{
                propFile.createNewFile();
                //log.print(LogType.Debug,"New file created");
                setupJavaPropertiesObject();
                setDefaults();
            }
            catch(Exception e){
                log.print(LogType.Error,"Properties file couldnt be created");
                //Throw custom error?
            }
        }
        else {
            //log.print(LogType.Debug,"Properties file already exists");
            setupJavaPropertiesObject();

            //if version is different reset defaults. This is as default settings may have changed causing incompatabilites.
            if(!getSetting("appVersionLast").equals(AppDefaults.ver.value)) setDefaults(); 
        }   
    }

    private void setupJavaPropertiesObject() {
        javaProperties = new Properties();
        try {
            FileInputStream inProps = new FileInputStream(propFile);
            javaProperties.load(inProps);
            inProps.close();
        } catch (IOException e) {
            log.print(LogType.Error,"Unable to load properties from file: " + appPath + propertiesFile);
        }
    }

    public boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win")>=0);
    }

    private void setDefaults(){
        setSettingDontSaveYet("homeDir",appPath);
        AppDefaults.set(this);
    }

    //Use this when setting many settings. Use setSettingAndSave for final one.
    public void setSettingDontSaveYet(String settingName, String settingValue){
        javaProperties.setProperty(settingName, settingValue);
    }
    public void setSettingAndSave(String settingName, boolean settingValue){
        String val = "false";
        if(settingValue) val = "true";
        setSettingAndSave(settingName,val);
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
            log.print(LogType.Error,"Unable to save settings to file: "+propFile);
        }// catch (FileNotFoundException e){}
    }

    //May return null: returns null if not found.
    public String getSetting(String settingName){
        return javaProperties.getProperty(settingName);
    }
    Boolean getSettingAsBooleanObject(String settingName){
        String temp = getSetting(settingName);
        if(temp == null) return null;
        if(temp.toLowerCase().equals("true")) return Boolean.valueOf(true);
        if(temp.toLowerCase().equals("false")) return Boolean.valueOf(false);
        return null;
    }
    boolean getSettingAsBool(String settingName, boolean defaultVal){
        Boolean b = getSettingAsBooleanObject(settingName);
        if(b==null) return defaultVal;
        return b.booleanValue();
    }
    public int getSettingAsInt(String settingName) throws NumberFormatException{
        return Integer.parseInt(javaProperties.getProperty(settingName));//catch error
    }

    //public boolean containsSetting(String settingName)
    //containskey

    public static void main(String[] args){
        Log log = new Log();
        Settings props = new Settings();
        log.print(LogType.Plain,"Os is windows?..."+props.isWindows());
        log.print(LogType.Plain,"(private) StudyBudy folder is: "+props.appPath);
        log.print(LogType.Plain,"(public) getSetting finds StudyBudy folder as: "+props.getSetting("homeDir"));
        props.setSettingAndSave("testSaveAllSettigngsHere", "settingsvalue");//Test setting a property in the file and saving all settings to file
        AppDefaults.getAndPrint(props);
        props.setSettingDontSaveYet("numberOfThumbnails", "5");//Test setting a property in the file. Doesn't save Properties to file.
        log.print(LogType.Plain,"Test property set at '5' has value: "+props.getSetting("numberOfThumbnails"));
        props = null;

        //Simulate closing StudyBudy and running a second time

        //Any settings which are not saved will no longer exist and return null
        props = new Settings();
        log.print(LogType.Plain,"\nFirst default setting has value: "+props.getSetting(AppDefaults.s1.key));
        log.print(LogType.Plain,"(unsaved) Test property set at '5' has value: "+props.getSetting("numberOfThumbnails"));

    }
}
