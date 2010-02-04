import java.awt.image.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.swing.JOptionPane.*;

//Type of load ProgramState does. Respectivly:
//(Creates new DB, loads DB from file, uses existing DB with filter, uses whole existing DB)
enum LoadType{Init,Load,Filter,Refresh}

//Should the program import pdfs? this would allow many more types of notes...
//...but would be alot of work for us

//Should hold data relating to program state and control program state.
//Should hold references to databses and image locations
//Should keep track of whether to flush the curent image and various thumbs based-
//Previous image and 3 next images should be kept, others flushed.
class ProgramState{
    private ImageObject[] imageList;
    String[] imageIDs;
    int lastIndex; //Must be updated when number of images changes
    String currentFilter;
    int currentI = 0;//make private
    GUI mainGUI;
    boolean isLocked = false;//Do not draw if locked.
    final String saveFileName = "savefile.txt";

    ProgramState(LoadType loadType, GUI parentGUI){
	ContructProgramState(loadType,  parentGUI,""); //loadType should not be filter here
    }

    ProgramState(GUI parentGUI){
        //if savefile exists, LoadType.Load, else LoadType.Init
        //use Settings object
	LoadType lType = LoadType.Init;//Use above instead
	ContructProgramState(lType,  parentGUI,""); //loadType should not be filter here
    }
    ProgramState(GUI parentGUI, String filterTag){
	ContructProgramState(LoadType.Filter, parentGUI, filterTag);
    }
    ProgramState(LoadType loadType, GUI parentGUI, String filterTag){
	ContructProgramState(loadType, parentGUI, filterTag);
    }

    void ContructProgramState(LoadType loadType, GUI parentGUI, String filterTag){

	mainGUI = parentGUI;
	switch (loadType){
	case Init:
            ImageDatabase tempDB;
	    tempDB = new ImageDatabase("mainDB");
	    //If there are no files you get loads of errors
            String barbTagID = tempDB.addTag("Barbados");
            String notesTagID = tempDB.addTag("Notes");
            String palmTagID = tempDB.addTag("Palm Tree");
            tempDB.tagTag(palmTagID, barbTagID);
            
            //Adding an image returns the ImageID of that image.
	    addI(tempDB,"Park","///\\\\\\img_2810b_small.jpg");
	    //addI(tempImageDB,"Creates error- not found","///\\\\\\img_monkeys_small.jpg");
	    //addI(tempImageDB,"Creates Error- not an image","///\\\\\\NotAnImage.txt");
	    addI(tempDB,"Igloo in Bristol","///\\\\\\img_6088b_small.jpg");
	    addI(tempDB,"Pink","///\\\\\\img_5672bp_small.jpg");
	    addI(tempDB,"Speed","///\\\\\\img_2926_small.jpg");
	    addI(tempDB,"Food","///\\\\\\img_F028c_small.jpg");
	    addI(tempDB,"Data Structures&Algorithms note 1","///\\\\\\DSA_1.bmp");
	    //addI(tempImageDB,"Large file- many MegaPixels","///\\\\\\jamaica1730homannsheirs.jpg");
	    addI(tempDB,"Graph Notes for C/W","///\\\\\\DSA_7.bmp");


	    addI(tempDB,"Barbados","///\\\\\\barbados01.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados04.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados05.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados07.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados08.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados09.jpg");
            String[] iDs = tempDB.getPossibleIDs("Barbados");
            for (String imageID : iDs){
                tempDB.tagImage(imageID, palmTagID);
            }


	    addI(tempDB,"Barbados","///\\\\\\barbados02.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados03.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados06.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados10.jpg");
	    addI(tempDB,"Barbados","///\\\\\\barbados-08-046-733284.jpg");
            
            tempDB.tagTag(tempDB.getTagIDFromTagTitle("Data Structures&Algorithms note 1") , tempDB.getTagIDFromTagTitle("Notes"));
            tempDB.tagTag(tempDB.getTagIDFromTagTitle("Graph Notes for C/W") , tempDB.getTagIDFromTagTitle("Notes"));

            tempDB.save(saveFileName);
	case Load:
	    mainGUI.mainImageDB = new ImageDatabase("mainDB",saveFileName);
	    //no break as image list must still be passed from DB
	case Refresh:
	    //Create image database by loading database
	    currentFilter = "Show All Images";
	    imageIDs = mainGUI.mainImageDB.getAllImageIDs();
	    break;

	case Filter:
	    //Create image database by loading database
	    currentFilter = filterTag;
	    imageIDs = mainGUI.mainImageDB.getImageIDsFromTagID(filterTag); // Working on TagID not TagTitle
	    break;
	}
	//if imageIDs.length==0
	//then a file should be added first (Construct with Init&imports, then return;)
      	imageList = new ImageObject[imageIDs.length];
	for(int i=0; i<imageIDs.length;i++){
	    imageList[i] = new ImageObject(mainGUI.mainImageDB.getImageFilename(imageIDs[i]));
	}
	lastIndex = (imageIDs.length - 1);
	if((loadType!=LoadType.Init) && (loadType!=LoadType.Load)){
	    mainGUI.mainPanel.onResize();
	    mainGUI.thumbPanel.onResize();
	}
    }
    
    void addI(ImageDatabase DB,String title,String filename){
        if(DB.getTagIDFromTagTitle(title)==null) DB.addTag(title);
        DB.tagImage(DB.addImage(title, filename), DB.getTagIDFromTagTitle(title));
    }

    void importImages(File[] files){
	isLocked = true;
	for(File f : files){
	    //System.out.println(f.getPath()+ " is the getPath and the absPath is " +f.getAbsolutePath());//Should be removed later
	    mainGUI.mainImageDB.addImage("Title 1",f.getAbsolutePath());
	}
	try{
	    if(currentFilter.equals("-1")){ // "-1" is now show all (working on TagID rather than Tag Title)
		mainGUI.state = new ProgramState(LoadType.Refresh,mainGUI);
		//Sets image to last image
		//mainGUI.state.currentI = mainGUI.state.imageIDs.length - 1;
		//Sets current image to first loaded image.
		//mainGUI.state.currentI = this.lastIndex + 1;//Bad code as if load fails the this is out of bounds
		//Will be replaced by feeding lastIndex+1 into constructor above
	    }
	    else {
		mainGUI.state = new ProgramState(LoadType.Filter,mainGUI,currentFilter);
	    }
	} catch(java.lang.OutOfMemoryError e){
            JOptionPane.showMessageDialog(mainGUI.w,"Out of memory","Fatal Error",JOptionPane.ERROR_MESSAGE);
	} finally {
	    isLocked = false;
	    safelyDestruct();
	}
    }

    void importImage(String absolutePath){//should make a file and call importImages(new File[])
	isLocked = true;
	mainGUI.mainImageDB.addImage("Title 1",absolutePath);
	if(currentFilter.equals("-1")){ // "-1" means show all (using TagID not Tag Title)
	    imageIDs = mainGUI.mainImageDB.getAllImageIDs();
	    if(lastIndex != (imageIDs.length - 1)) lastIndex = imageIDs.length - 1;
	    else return; //If there are no more images than before import, then failure
	    currentI = lastIndex;
	    imageList[lastIndex] = new ImageObject(mainGUI.mainImageDB.getImageFilename(imageIDs[lastIndex]));
	    mainGUI.mainPanel.repaint();
	    mainGUI.thumbPanel.repaint();
	}
	else {
	    mainGUI.state = new ProgramState(LoadType.Filter,mainGUI,currentFilter);
	    safelyDestruct();
	}
	isLocked = false;
    }

    //flushes all images and thumbs
    void safelyDestruct(){
	//might check if mainGUI.state==this, as this would imply no need to distruct yet.
	for(ImageObject imgObj : imageList){
	    imgObj.destroy();
	    imgObj = null;
	}
	imageList = null;
	imageIDs = null;
	//call garbage collect?
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
	mainGUI.mainPanel.onResize();
	mainGUI.thumbPanel.onResize();
    }
    void prevImage() {
	currentI = prev(currentI);
	mainGUI.mainPanel.onResize();
	mainGUI.thumbPanel.onResize();
    }
    void offsetImage(int by){
	currentI = relItoFixI(by);
	mainGUI.mainPanel.onResize();
	mainGUI.thumbPanel.onResize();
    }

    int relItoFixI(int in){
	int c;
	int outI = currentI;
	if(in==0) return outI;
	if(in>0){
	    for(c=0;c!=in;c++){//>=
		outI = next(outI);
	    }
	}
	else for(c=0;c!=in;c--){//<=
		outI = prev(outI);
	    }
	return outI;
    }

    // Must be edited so empty DB/imageList does not cause error
    ImageObject getCurrentImage(){
	return imageList[currentI];
    }

    Dimension getRelImageWH(ImgSize size, int MaxW, int MaxH, int relativeImage){
	int imageIndex = relItoFixI(relativeImage);
	Dimension useWH = new Dimension();
	//int[] useWH;
	if(size.isLarge()){
	    useWH= ImageObject.scaleToMax(getImageI(imageIndex).getWidthAndMake(),getImageI(imageIndex).getHeightAndMake(), MaxW, MaxH);
	}
	else {
	    useWH = ImageObject.scaleToMax(getImageI(imageIndex).getWidthForThumb(),getImageI(imageIndex).getHeightForThumb(), MaxW, MaxH);
	}
	return useWH;
    }

    ImageObject getImageI(int i){
        if(imageList.length<1){
            System.err.println("Error: There are no images loaded under current search.\nEnsure filter has some images.");
            return null;
        }
	return imageList[i];//will be changed later to keep track of images in memory
    }

    BufferedImage getBImageI(int relativeImage, ImgSize size){
	if((size==ImgSize.Thumb)&&(relativeImage<=3)&&(relativeImage>=-1)) size = ImgSize.ThumbFull;
	BufferedImage returnImage = imageList[relItoFixI(relativeImage)].getImage(size);
	for(int i=4;i<lastIndex;i++){
	    imageList[relItoFixI(relativeImage)].flush();
	}
	return returnImage;
    }
}
