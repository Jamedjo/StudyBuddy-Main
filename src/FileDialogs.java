
import java.awt.FileDialog;
import java.io.File;
import javax.swing.JFileChooser;
import net.tomahawk.XFileDialog;

/*
 * Contains static classes for buiding and useing file choosers.
 */
public class FileDialogs {
    static Log log = new Log();
    static GUI mainGUI;
    static boolean isWindows;
    static JFileChooser fileGetter=null;
    static JFileChooser folderGetter=null;
    static JFileChooser jpgExporter=null;
    static XFileDialog winFileGetter=null;
    static XFileDialog winFolderGetter=null;
    static final String[] exts = {"jpeg", "jpg", "gif", "bmp", "png", "tiff", "tif", "tga", "pcx", "xbm", "svg","wbmp"};

    static void init(GUI gui){
        mainGUI = gui;
        isWindows = mainGUI.settings.isWindows();
    }

    static void exportCurrentImage() {
        if(jpgExporter==null) buildJpgExporter(mainGUI);
        int destReady = jpgExporter.showOpenDialog(mainGUI.w);
        if (destReady == JFileChooser.APPROVE_OPTION) {
            String filePathAndName = jpgExporter.getSelectedFile().toString();
            String ext = ImageUtils.getFileExtLowercase(filePathAndName);
            if((ext==null)||(!(ext.equals("jpg")||ext.equals("jpeg")))) filePathAndName = filePathAndName + ".jpg";
            mainGUI.settings.setSettingAndSave("lastOpenDirectory",jpgExporter.getCurrentDirectory().toString());
            mainGUI.getState().getCurrentImage().saveFullToPath(filePathAndName);
        }
    }
    static void importDo() {
        boolean success = false;
        String lastDir = null;
        File[] selectedFiles = null;

        if (!isWindows) {
            if (fileGetter == null) buildFileGetter();
            int wasGot = fileGetter.showOpenDialog(mainGUI.w);
            if (wasGot == JFileChooser.APPROVE_OPTION) {
                success = true;
                lastDir = fileGetter.getCurrentDirectory().toString();
                selectedFiles = fileGetter.getSelectedFiles();

            }
        } else {
            if (winFileGetter == null) buildWinFileGetter();
            winFileGetter.show();
            String[] filenames = winFileGetter.getFiles();
            String foldername = winFileGetter.getDirectory();
            if (filenames != null) {
                success = true;
                selectedFiles = new File[filenames.length];
                for (int i = 0; i < filenames.length; i++) {
                    selectedFiles[i] = new File(foldername,filenames[i]);
                }
                lastDir=foldername;
            }
            //winFileGetter.dispose();
        }

        if (success) {
            if (lastDir != null) mainGUI.settings.setSettingAndSave("lastOpenDirectory", lastDir);
            if (selectedFiles != null) mainGUI.getState().importImages(selectedFiles);
        }


    }
    static void importDirDo() {
        boolean success = false;
        String lastDir = null;
        File[] selectedFiles = null;

        if (!isWindows) {
            if(folderGetter==null) buildFolderGetter(mainGUI);
                int wasGot = folderGetter.showOpenDialog(mainGUI.w);
                if (wasGot == JFileChooser.APPROVE_OPTION) {
                success = true;
                lastDir = folderGetter.getCurrentDirectory().toString();
                selectedFiles = folderGetter.getSelectedFiles();

            }
        } else {
            if (winFolderGetter == null) buildWinFolderGetter();
            winFolderGetter.show();
            String folder = winFolderGetter.getFolder();
            //String foldername = winFolderGetter.getDirectory();//parent
            if (folder != null) {
                success = true;
                selectedFiles = new File[1];
                selectedFiles[0] = new File(folder);
                lastDir=folder;//foldername;
            }
        }

        if (success) {
            if (lastDir != null) mainGUI.settings.setSettingAndSave("lastOpenDirectory", lastDir);
            if (selectedFiles != null) mainGUI.getState().importImages(selectedFiles);
        }
    }

    static boolean isImage(File f){
        //String[] readerNames = ImageIO.getReaderFormatNames();
        //Sanselan.hasImageFileExtension();
        String ext = null;
        ext = ImageUtils.getFileExtLowercase(f.getName());
        if (ext==null) return false;
        for (String imgExt : exts) {
            if (ext.equals(imgExt)) {
                return true;
            }
        }
        return false;
    }

   static void buildFileGetter() {
        fileGetter = new JFileChooser();
        fileGetter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return isImage(f);
            }
            public String getDescription() {
                return "All Images";
            }
        });
        fileGetter.setDialogTitle("Import Image(s)");
        setGetterDir(fileGetter);
        fileGetter.setMultiSelectionEnabled(true);
    }
    static void buildWinFileGetter() {
        winFileGetter = new XFileDialog(mainGUI.w);
        winFileGetter.setMode(FileDialog.LOAD);
        winFileGetter.setMultiSelectionEnabled(true);
        winFileGetter.setThumbnail(true);
        winFileGetter.setTitle("Import Images(s)");
        File lastDir = getLastDir();
        if(lastDir!=null) winFileGetter.setDirectory(lastDir.toString());
        StringBuilder imageFiltersSB = new StringBuilder();
        for (String ext : exts) {
            imageFiltersSB.append(ext);
            imageFiltersSB.append(";");
        }
        String[] desc = {"All Images"};
        String[] filts = {imageFiltersSB.toString()};
        winFileGetter.setFilters(desc, filts);
    }
    static void buildWinFolderGetter() {
        winFolderGetter = new XFileDialog(mainGUI.w);
        winFolderGetter.setMode(FileDialog.LOAD);
        winFolderGetter.setMultiSelectionEnabled(false);
        winFolderGetter.setThumbnail(true);
        winFolderGetter.setTitle("Import Images(s)");
        File lastDir = getLastDir();
        if(lastDir!=null) winFolderGetter.setDirectory(lastDir.toString());
        StringBuilder imageFiltersSB = new StringBuilder();
        for (String ext : exts) {
            imageFiltersSB.append(ext);
            imageFiltersSB.append(";");
        }
        String[] desc = {"All Images"};
        String[] filts = {imageFiltersSB.toString()};
        winFolderGetter.setFilters(desc, filts);
    }
    static File getLastDir() {
        String lastSetDir = mainGUI.settings.getSetting("lastOpenDirectory");
        if ((lastSetDir == null) || lastSetDir.equals("")) return null;
        File lastDir = new File(lastSetDir);
        if (lastDir.exists() && lastDir.isDirectory()) return lastDir;
        return null;
    }
    static void setGetterDir(JFileChooser getter) {
        File lastDir = getLastDir();
        if((lastDir!=null)){
            try{
            getter.setCurrentDirectory(lastDir);
            } catch (IndexOutOfBoundsException e){
                log.print(LogType.Error,"Error creating open/save dialog at: "+lastDir.toString());
                e.printStackTrace();
            }
        }
    }
    static void buildFolderGetter(GUI mainGUI) {
        folderGetter = new JFileChooser();
        folderGetter.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderGetter.setDialogTitle("Import Folder(s)");
        setGetterDir(folderGetter);
        folderGetter.setMultiSelectionEnabled(true);
    }
    static void buildJpgExporter(GUI mainGUI) {
        jpgExporter = new JFileChooser();
        //jpgExporter.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        jpgExporter.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;//Still want to show directories to browse
                }
                if(f.toString().toLowerCase().endsWith(".jpg")||f.toString().toLowerCase().endsWith(".jpeg")) return true;
                return false;
            }
            public String getDescription() {
                return "Jpg File";
            }
        });
        jpgExporter.setDialogTitle("Export Image as JPG");
        jpgExporter.setSelectedFile(new File("image.jpg"));
        setGetterDir(jpgExporter);
        jpgExporter.setMultiSelectionEnabled(false);
    }
}
