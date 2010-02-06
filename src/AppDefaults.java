
enum AppDefaults {

    //study buddy version used? (if different reset defaults?
    //seperate properties version, which should be incremented if defaults need to be reset?
    //date settings changed?
    //thumbnails directory
    //might change this to an array so a loop can be used to set values. Or an enum.
    s1("importDirectory", "/importedimage/"),
    s2("testDefault2", "22222"),
    s3("thumbDir", "/thumbnails/");//must deal with OS dependant path seperator


    String key, value;

    AppDefaults(String k, String v) {
        key = k;
        value = v;
    }

    static void set(Settings settings) {
        for (AppDefaults setting : AppDefaults.values()) {
            settings.setSettingDontSave(setting.key, setting.value);
        }
        settings.saveSettings();
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
