import java.util.Properties;//Imports need to be fixed here
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.FileNotFoundException;

public class Settings {
    static Log log = new Log();
    //String path;
    static private String appPath;
    static private final String propertiesFile = "StudyBuddy.properties";//Name of the file use to store properties
    static private File propFile;
    static private Properties javaProperties;
    //Default setting values moved to AppDefaults.java and controlled by an enum.

    static void destroy(){
        appPath = null;
        propFile=null;
        javaProperties=null;
    }

    static void init(){
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
            if(AppDefaults.ver.valueDifferent(getSetting("appVersionLast"))){
                setDefaults();
            }
        }   
    }

    static private void setupJavaPropertiesObject() {
        javaProperties = new Properties();
        try {
            FileInputStream inProps = new FileInputStream(propFile);
            javaProperties.load(inProps);
            inProps.close();
        } catch (IOException e) {
            log.print(LogType.Error,"Unable to load properties from file: " + appPath + propertiesFile);
        }
    }

    static public boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win")>=0);
    }

    static private void setDefaults(){
        setSettingDontSaveYet("homeDir",appPath);
        AppDefaults.set();
    }

    //Use this when setting many settings. Use setSettingAndSave for final one.
    static public void setSettingDontSaveYet(String settingName, String settingValue){
        javaProperties.setProperty(settingName, settingValue);
    }
    static public void setSettingAndSave(String settingName, boolean settingValue){
        String val = "false";
        if(settingValue) val = "true";
        setSettingAndSave(settingName,val);
    }
    static public void setSettingAndSave(String settingName, String settingValue){
        javaProperties.setProperty(settingName, settingValue);
        saveSettings();
    }

    static public void saveSettings(){
        try{
        FileOutputStream outProps = new FileOutputStream(propFile);
        javaProperties.store(outProps, "---Empty Comment---");
        outProps.close();
        } catch (IOException e){
            log.print(LogType.Error,"Unable to save settings to file: "+propFile);
        }// catch (FileNotFoundException e){}
    }

    //May return null: returns null if not found.
    static public String getSetting(String settingName){
        return javaProperties.getProperty(settingName);
    }
    static Boolean getSettingAsBooleanObject(String settingName){
        String temp = getSetting(settingName);
        if(temp == null) return null;
        if(temp.toLowerCase().equals("true")) return Boolean.valueOf(true);
        if(temp.toLowerCase().equals("false")) return Boolean.valueOf(false);
        return null;
    }
    static boolean getSettingAsBool(String settingName, boolean defaultVal){
        Boolean b = getSettingAsBooleanObject(settingName);
        if(b==null) return defaultVal;
        return b.booleanValue();
    }
    static public int getSettingAsInt(String settingName) throws NumberFormatException{
        return Integer.parseInt(javaProperties.getProperty(settingName));//catch error
    }
    static public File getPropertiesFile(){
        try{
            return propFile.getCanonicalFile();
        } catch (IOException e) {
            try{
                return propFile.getAbsoluteFile();
            } catch (Exception ex){
                return propFile;
            }
        }
    }

    //public boolean containsSetting(String settingName)
    //containskey

    public static void main(String[] args){
        Settings.init();
        log.print(LogType.Plain,"Os is windows?..."+Settings.isWindows());
        log.print(LogType.Plain,"(private) StudyBudy folder is: "+Settings.appPath);
        log.print(LogType.Plain,"(public) getSetting finds StudyBudy folder as: "+Settings.getSetting("homeDir"));
        Settings.setSettingAndSave("testSaveAllSettigngsHere", "settingsvalue");//Test setting a property in the file and saving all settings to file
        AppDefaults.getAndPrint();
        Settings.setSettingDontSaveYet("numberOfThumbnails", "5");//Test setting a property in the file. Doesn't save Properties to file.
        log.print(LogType.Plain,"Test property set at '5' has value: "+Settings.getSetting("numberOfThumbnails"));

        //Simulate closing StudyBudy and running a second time
        //Any settings which are not saved will no longer exist and return null
        Settings.destroy();Settings.init();
        log.print(LogType.Plain,"\nFirst default setting has value: "+Settings.getSetting(AppDefaults.s1.key));
        log.print(LogType.Plain,"(unsaved) Test property set at '5' has value: "+Settings.getSetting("numberOfThumbnails"));

    }
}
