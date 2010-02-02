import java.util.Properties;

public class Settings {
    //String path;

    Settings(){
        //if properties file exists in StudyBuddy folder in user home directory
        //e.g. C:\Users\James\Application Data\StudyBuddy\
        //or e.g. /home/James/.StudyBudy/
        //use System.getProperty("user.home") to find the main path like C:\Users\James or /home/James
        //use somthing like http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
            //to find the OS and if windows use path+"\Application Data\StudyBuddy\" if other OS use path+".StudyBuddy\"
        //
            // path = path to file
        //else
            //create it with default values as properties
            //path = path to created settings file
    }

    //Duplicate this for "public void setSetting(String settingName,String settingValue){}
//    String getSetting(String settingName){
//        return the string value of the property given
          //see http://java.sun.com/docs/books/tutorial/essential/environment/properties.html
          //Use properties API to set and get settings
//    }


    public static void main(String[] args){
        Settings props = new Settings();
        //print props.path;
        //print props.getSetting("homeDir");
        //props.setSetting("numberOfThumbnails","5");
        //print props.getSetting("numberOfThumbnails");
    }
}
