
import java.io.File;


enum AppDefaults {

    //study buddy version used? (if different reset defaults?
    //seperate properties version, which should be incremented if defaults need to be reset?
    //date settings changed?
    //thumbnails directory
    //might change this to an array so a loop can be used to set values. Or an enum.
    s1("importPathExt", "importedimage"+File.separator),
    s2("slideShowTime", "2000"),
    //example s99("nestedDirectoryTest_PathExt","topDir"+File.separator+"subDir"+File.separator),//just using the subDir will not create the dir
    s3("thumbnailPathExt", "thumbnails"+File.separator),//must deal with OS dependant path seperator
    s4("databaseFileName","mainDB"),
    s5("databasePathExt","database"+File.separator),
    s6("appVersionLast","0.7beta");


    String key, value;

    AppDefaults(String k, String v) {
        key = k;
        value = v;
    }

    static void set(Settings settings) {
        makeDirs(settings);
        for (AppDefaults setting : AppDefaults.values()) {
            settings.setSettingDontSave(setting.key, setting.value);
        }
        settings.saveSettings();
    }

    static void makeDirs(Settings seTTings){
        for (AppDefaults setting : AppDefaults.values()) {
            if(setting.key.endsWith("PathExt")) {
                String path = seTTings.getSetting("homeDir") +  setting.value;
                File folder = new File(path);
                if (!folder.isDirectory()) {
                    boolean success = (new File(path)).mkdir();
                    if (!success) {
                        System.err.println("StudyBuddy Directory could not be created");
                    }
                }
            }
        }
    }

    static void getAndPrint(Settings settings){
        for (AppDefaults setting : AppDefaults.values()) {
            System.out.println("Default key: "+setting.key+" has value: "+settings.getSetting(setting.key));
        }        
    }

    public static void main(String[] args) {
        for (AppDefaults setting : AppDefaults.values()) {
            System.out.println(setting.key + " :key||| <-     -> |||value:" + setting.value);
        }
    }
}
