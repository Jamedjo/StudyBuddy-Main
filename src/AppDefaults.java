
import java.io.File;


enum AppDefaults {
    //date settings changed?
    s1("importPathExt", "importedimage"+File.separator),
    s2("slideShowTime", "2000"),
    //example s99("nestedDirectoryTest_PathExt","topDir"+File.separator+"subDir"+File.separator),//just using the subDir will not create the dir
    s3("thumbnailPathExt", "thumbnails"+File.separator),
    DBname("databaseFileName","gammaDB"),
    s5("databasePathExt","database"+File.separator),
    ver("appVersionLast","0.9.1_r246"),//change this whenever you change this enum.
    s7("lastFilterUsed","Show All Images"),
    s8("lastCurrentI","0"),
    s9("showLinks","true"),
    s10("showNotes","true");
    //use , to seperate but ; after last

    String key, value;

    AppDefaults(String k, String v) {
        key = k;
        value = v;
    }

    boolean valueDifferent(String b){
        String a = value;
        boolean bool =  ((String)a).equals(b);
        return !bool;
    }

    static void set(Settings settings) {
        makeDirs(settings);
        for (AppDefaults setting : AppDefaults.values()) {
            settings.setSettingDontSaveYet(setting.key, setting.value);
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
