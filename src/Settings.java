import java.util.Properties.*;//Imports need to be fixed here
import java.io.File;

public class Settings {
    //String path;
    public String appPath;
    final String propertiesFile = "StudyBuddy.properties";
    File propFile;

    Settings(){
        appPath = (System.getProperty("user.home"));
        if(isWindows()){
            appPath = appPath + "\\Application Data\\StudyBuddy\\";//Windows uses backslash. Two needed as escape sequence
        }
        else{
            appPath = appPath + ".StudyBuddy/";
        }

        // attempt at finding if the directory exists, not sure if this is what was needed?
        // james: I have not checked if it works but it looks right
        File folder = new File(appPath);
        if (!folder.isDirectory()) {
            System.out.println("path doesnt exist");
            //Make directory appPath
        }

        //
        propFile = new File(appPath+propertiesFile);
       // if (! file exists and is properties file) {
            System.out.println("properties files doesnt exist");
            //create propertiesFile file with default values as properties
        //}
        
    }

    public static boolean isWindows(){
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("win")>=0);
    }

    //Use properties API to set and get settings

    //see http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
    public void setSetting(String settingName, String settingValue){
        //setProperty(settingName, settingValue);
    }

    //see http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
    public String getSetting(String settingName){
        //String value = getProperty(settingName);
        return "";// value;
    }


    public static void main(String[] args){
        Settings props = new Settings();
        System.out.println(props.appPath);
        System.out.println(props.getSetting("homeDir"));
        props.setSetting("numberOfThumbnails", "5");
        System.out.println(props.getSetting("numberOfThumnails"));
    }
}
