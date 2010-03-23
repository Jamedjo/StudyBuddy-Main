import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.JOptionPane.*;

//Type of load ProgramState does. Respectivly:
//(Creates new DB, loads DB from file, uses existing DB with filter, uses whole existing DB)
enum LoadType{Init,Load,Filter,Refresh,LoadLast}

//Should the program import pdfs? this would allow many more types of notes...
//...but would be alot of work for us

//Should hold data relating to program state and control program state.
//Should hold references to databses and image locations
//Should keep track of whether to flush the curent image and various thumbs based-
//Previous image and 3 next images should be kept, others flushed.
class ProgramState{
    Log log = new Log(false);
    private ImageReference[] imageList;
    private String[] imageIDs;
    int lastIndex; //Must be updated when number of images changes
    String currentFilter;//replace with private IDTitle and use getters and setters to access if ever needed.
    int numberOfImages;
    int currentI = 0;//make private
    final GUI mainGUI;
    boolean isLocked = false;//Do not draw if locked.

    // IMPORTANT NOTE: WHEN CONSTRUCTING NEW PROGRAM STATE. OTHER THREADS WILL SEE OLD STATE UNTIL CONTSRUCTOR RETURNS.
    // THIS MEANS METHODS CAN ACCIDENTALLY USE VALUES FROM THE OLD STATE.
    // BE CAREFULL TO CHECK STATE IS 'CHANGING' WHEN USING ITS VALUES
    // CALL mainGUI.state.imageChanged after constructing a ne state

    ProgramState(LoadType loadType, GUI parentGUI){
	mainGUI = parentGUI;
	ConstructProgramState(loadType,  parentGUI,""); //loadType should not be filter here
    }

    ProgramState(GUI parentGUI){
	mainGUI = parentGUI;
        String temp;
        temp = Settings.getSetting("databaseFilePathAndName");
        if(temp==null) ConstructProgramState(LoadType.Init,  parentGUI,"");
        else ConstructProgramState(LoadType.LoadLast,  parentGUI,Settings.getSetting("lastFilterUsed"));
    }
    ProgramState(GUI parentGUI, String filterTag){
	mainGUI = parentGUI;
	ConstructProgramState(LoadType.Filter, parentGUI, filterTag);
    }
    ProgramState(LoadType loadType, GUI parentGUI, String filterTag){
	mainGUI = parentGUI;
	ConstructProgramState(loadType, parentGUI, filterTag);
    }
    String getSetting(String name){
        return Settings.getSetting(name);
    }
    void ConstructProgramState(LoadType loadType, GUI parentGUI, String filterTag){
        //mainGUI.isChangingState = true;
	switch (loadType){
	case Init:
            Settings.setSettingAndSave("databaseFilePathAndName", getSetting("homeDir")+getSetting("databasePathExt")+getSetting("databaseFileName"));
            InitDemoDB.initDB(Settings.getSetting("databaseFilePathAndName"));//Resets database
            Settings.setSettingAndSave("databaseVersion", ImageDatabase.getDatabaseVersion());
	case Load:
	    mainGUI.mainImageDB = new ImageDatabase(getSetting("databaseFileName"),getSetting("databaseFilePathAndName"));
            checkDBVersion();
            //no break as image list must still be passed from DB
	case Refresh:
	    //Create image database by loading database
	    currentFilter = "-1";
	    imageIDs = mainGUI.mainImageDB.getAllImageIDs();
	    break;
        case LoadLast:
            mainGUI.mainImageDB = new ImageDatabase(getSetting("databaseFileName"),getSetting("databaseFilePathAndName"));
            checkDBVersion();
            //change currentI by maingGUI.settings.getSetting("LastCurrentI") but be careful of null, etc.
	case Filter:
	    //Create image database by loading database
            if(filterTag==null) {
                log.print(LogType.Error,"Error: Tried to filter by tag without a filter.");
                ConstructProgramState(LoadType.Load, parentGUI, "");
                return;
            } else if (filterTag.equals("-1")){
                ConstructProgramState(LoadType.Refresh, parentGUI, filterTag);
                return;
            }
	    currentFilter = filterTag;
	    imageIDs = mainGUI.mainImageDB.getImageIDsFromTagIDChildren(filterTag); // Working on TagID not TagTitle
	    break;
	}
        Settings.setSettingAndSave("lastFilterUsed", currentFilter);
	//if imageIDs.length==0
	//then a file should be added first (Construct with Init&imports, then return;)
      	imageList = new ImageReference[imageIDs.length];
        numberOfImages = imageList.length;
	for(int i=0; i<imageIDs.length;i++){
	    imageList[i] = new ImageReference(mainGUI.mainImageDB.getImageFilename(imageIDs[i]));
	}
	lastIndex = (imageIDs.length - 1);

        //mainGUI.isChangingState = false; //Set false before calling imageChanged thumbPanel.onResize() waits for this to be false
        //Needed as GUI components not cfreated yet
	//if((loadType!=LoadType.Init) && (loadType!=LoadType.Load)){
            //imageChanged();//Will cause deadlock or bugs if uncommented. Call after constructing
	//}
    if(imageList.length<1){
            log.print(LogType.DebugError,"Error: There are no images loaded under current search.\nEnsure filter has some images.");
            imageIDs = new String[1];
            imageIDs[0] = "-1";
            imageList = new ImageReference[1];
            imageList[0] = new ImageReference("NoExistingFiles:a:b:c:d:e:f:g:h.i.j.k.l.m.n:o:p:non.ex");
        }

        imageChanged();
    }

    void checkDBVersion() {
        try {
            if (!Settings.getSetting("databaseVersion").equals(ImageDatabase.getDatabaseVersion())) {
                log.print(LogType.Error, "Database version missmatch");
            }
        } catch (NullPointerException e) {
            log.print(LogType.Error, "Database version missmatch- old version not set");
            log.print(LogType.DebugError, "Try running: ProgramState(LoadType.Init, GUI parentGUI)");
        }
    }

    void imageDeleted(){
         mainGUI.setState(new ProgramState(LoadType.Filter, mainGUI, currentFilter));
         mainGUI.getState().imageChanged();
    }

    void importImages(File[] files) {// redo if hierarchy. Work out why GUI freezes when importing large files. Ensure import folder works.
        isLocked = true;
        try {
            if (currentFilter.equals("-1")) { // "-1" is now show all (working on TagID rather than Tag Title)
                ArrayList<String> tempImageIDs = new ArrayList<String>(Arrays.asList(imageIDs));
                ArrayList<ImageReference> tempImageList = new ArrayList<ImageReference>(Arrays.asList(imageList));
                if (((String) tempImageIDs.get(0)).equals("-1")) {
                    tempImageIDs.remove(0);
                    tempImageList.remove(0);
                }
                ArrayList<File> foldersList = new ArrayList<File>();
                for (File f : files) {
                    if (f.isDirectory()) foldersList.add(f);
                    else {
                        //log.print(LogType.Debug,f.getPath()+ " is the getPath and the absPath is " +f.getAbsolutePath());//Should be removed later
                        String currentImID = mainGUI.mainImageDB.addImage("Title 1", f.getAbsolutePath());//f.getName()?
                        if (currentImID != null) {
                            tempImageIDs.add(currentImID);
                            //tempImageList.add(new ImageReference(mainGUI.mainImageDB.getImageFilename(currentImID) ,currentImID ));
                            tempImageList.add(new ImageReference(f.getAbsolutePath()));
                        }
                    }
                }
                for (File dir : foldersList.toArray(new File[0])) {
                    File[] children = dir.listFiles(new FileFilter() {
                        public boolean accept(File g) {
                            if (g.isDirectory()) {
                                return false;
                            }
                            return FileDialogs.isImage(g);
                        }
                    });
                    for (File c : children) {
                        if (c.isDirectory()) foldersList.add(c);
                        String currentImID = mainGUI.mainImageDB.addImage("Title 1", c.getAbsolutePath());//c.getName()?
                        if (currentImID != null) {
                            tempImageIDs.add(currentImID);
                            tempImageList.add(new ImageReference(c.getAbsolutePath()));
                        }
                    }
                }
                imageIDs = new String[tempImageIDs.size()];
                tempImageIDs.toArray(imageIDs);
                imageList = new ImageReference[tempImageList.size()];
                tempImageList.toArray(imageList);
                if (lastIndex >= (imageIDs.length - 1)) {
                    //Print error loading images?
                    return; //If there are no more images than before import, then failure
                }
                currentI = lastIndex + 1;
                lastIndex = imageIDs.length - 1;
                numberOfImages = imageList.length;
            } else {
                for (File f : files) {
                    //log.print(LogType.Debug,f.getPath()+ " is the getPath and the absPath is " +f.getAbsolutePath());//Should be removed later
                    mainGUI.mainImageDB.addImage("Title 1", f.getAbsolutePath());
                }
                mainGUI.setState(new ProgramState(LoadType.Filter, mainGUI, currentFilter));
            }
            mainGUI.getState().imageChanged();
        } catch (java.lang.OutOfMemoryError e) {
            JOptionPane.showMessageDialog(mainGUI.w, "Out of memory", "Fatal Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            isLocked = false;
        }
    }

//    void importImage(String absolutePath){}//should make a file and call importImages(new File[])

    //flushes all images and thumbs
    void safelyDestruct(){
	//might check if mainGUI.state==this, as this would imply no need to distruct yet.
	for(ImageReference imgObj : imageList){
	    imgObj.destroy();
	    imgObj = null;
	}
	imageList = null;
	imageIDs = null;
        System.gc(); //hint at garbage collection
    }

    int next(int val){
	if(val>=lastIndex) return 0;
	return (val+1);
    }

    int prev(int val){
	if(val<=0) return lastIndex;
	return (val-1);
    }

    void nextImage() {
	currentI = next(currentI);
        imageChanged();
    }
    void prevImage() {
	currentI = prev(currentI);
        imageChanged();
    }
    void offsetImage(int by){
	currentI = relItoFixI(by);
        imageChanged();
    }

    void imageChanged(){
        if(mainGUI.mainPanel==null||mainGUI.thumbPanel==null) return;
        String imageName = mainGUI.mainImageDB.getImageFilename(imageIDs[currentI]);
        if(imageName!=null)mainGUI.setTitle("- Image: "+(new File(imageName)).getName().toString());
        else mainGUI.setTitle("");
        RepaintManager.repaint(RepaintType.Window);
        //mainGUI.tagTree.repaint();
    }

    int relItoFixI(int in){
        int outI;
        outI = (currentI + in) % (lastIndex + 1);//If the new posistion is larger than the array, use modulo
        if(outI<0) outI = (lastIndex+outI)+1;//If negative go back outI images
	return outI;
    }

    // Must be edited so empty DB/imageList does not cause error
    ImageReference getCurrentImage(){
	return imageList[currentI];
    }
    String getCurrentImageID(){
	return imageIDs[currentI];
    }

    Dimension getRelImageWH(ImgRequestSize size, int MaxW, int MaxH, int relativeImage){
	ImageReference relImage = getImageI(relItoFixI(relativeImage));
        return ImageUtils.getImageWH(size, MaxW, MaxH, relImage);
    }

    ImageReference getImageI(int i){
        if(imageList.length<1){
            log.print(LogType.Error,"Error: There are no images loaded under current search.\nEnsure filter has some images.");
            return null;
        }
	return imageList[i];//will be changed later to keep track of images in memory
    }

    String getRelativeImageID(int relativeImage){
        return imageIDs[relItoFixI(relativeImage)];
    }
    void goToImageByID(String newID){
        //Seach for image in current list of images
        //Goto if found
        for(int i=0;i<imageIDs.length;i++){
            if(imageIDs[i].equals(newID)){
                currentI = i;
                imageChanged();
                mainGUI.tagTree.repaint();//Needs to update not repaint.
                return;
            }
        }

        String[] possibleTags = mainGUI.mainImageDB.getTagIDsFromImage(newID);
        if (possibleTags.length > 0) {
            if (!currentFilter.equals(possibleTags[0])) {
                mainGUI.setState(new ProgramState(LoadType.Filter, mainGUI, possibleTags[0]));
                mainGUI.getState().goToImageByID(newID);
            }
            return;
        } else {
            if (!currentFilter.equals("-1")) {
                mainGUI.setState(new ProgramState(LoadType.Refresh, mainGUI, "-1"));
                mainGUI.getState().goToImageByID(newID);
                return;
            }
        }

        JOptionPane.showMessageDialog(mainGUI.w, "Unable to jumping to image " + newID);
    }
    BufferedImage getBImageI(int relativeImage, ImgRequestSize size){
            //If getting thumb for an upcoming image, get the full image too.
            //if((size==ImgRequestSize.Thumb)&&(relativeImage<=3)&&(relativeImage>=-1)) size = ImgRequestSize.ThumbFull;
	BufferedImage returnImage = imageList[relItoFixI(relativeImage)].getImage(size);

        if(size.isLarge()&&(relativeImage==0)){
            int i;
            int prev = prev(currentI); //dont preload too large files. If you load a file make sure it does not keep trying to load after failures.(heap mem)
            for(i=1;i<Math.min(4,lastIndex);i++){//Use amount of memory avaliable to determine number to preload
                imageList[relItoFixI(i)].preload(size);//Preloads next three images
            }
            for(;i<lastIndex;i++){
                if(i==prev) imageList[prev].preload(size);//Preloads previous image
                imageList[relItoFixI(i)].flush();//removes from memory all images after the preloaded ones
            }
            //remove thumbnails after 250 thumbnails? //30 or so better but 250 should be max thumbnails stored.
        }
	return returnImage;
    }
    int getImageWidthFromBig(){
        return getCurrentImage().getDimensionsWithMake().width;
    }
    int getImageHeightFromBig(){
        return getCurrentImage().getDimensionsWithMake().height;
    }

    void imageColoursReset(){
        getCurrentImage().img.resetFilters();
        imageColoursUpdated();
    }
    void imageColoursUpdated(){
        //getCurrentImage().filterImage();
        RepaintManager.repaint(RepaintType.ColourChange);
    }
}
