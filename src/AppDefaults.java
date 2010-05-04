
import java.io.File;


enum AppDefaults {
    //date settings changed?
    s1("importPathExt", "importedimage"+File.separator),
    cacheDir("cachePathExt","imageCache"+File.separator),
    s2("slideShowTime", "2000"),
    //example s99("nestedDirectoryTest_PathExt","topDir"+File.separator+"subDir"+File.separator),//just using the subDir will not create the dir
    s3("thumbnailPathExt", "thumbnails"+File.separator),
    DBname("databaseFileName","gammaDB"),
    s5("databasePathExt","database"+File.separator),
    ver("appVersionLast","0.9.4_r337"),//change this whenever you change this enum.
    s7("lastFilterUsed","-1"),
    s8("lastCurrentI","0"),
    s9("showLinks","true"),
    s10("showNotes","true"),
    s11("newImagePathExt","mobileImages");
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

    static void set() {
        makeDirs();
        for (AppDefaults setting : AppDefaults.values()) {
            Settings.setSettingDontSaveYet(setting.key, setting.value);
        }
        Settings.saveSettings();
    }

    static void makeDirs(){
        for (AppDefaults setting : AppDefaults.values()) {
            if(setting.key.endsWith("PathExt")) {
                String path = Settings.getSetting("homeDir") +  setting.value;
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

    static void getAndPrint(){
        for (AppDefaults setting : AppDefaults.values()) {
            System.out.println("Default key: "+setting.key+" has value: "+Settings.getSetting(setting.key));
        }        
    }

    public static void main(String[] args) {
        for (AppDefaults setting : AppDefaults.values()) {
            System.out.println(setting.key + " :key||| <-     -> |||value:" + setting.value);
        }
    }
}
