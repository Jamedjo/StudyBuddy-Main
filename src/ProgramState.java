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
	LoadType lType = LoadType.Init;
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
            ImageDatabase tempImageDB;
	    tempImageDB = new ImageDatabase("mainDB");
	    //If there are no files you get loads of errors

            //Adding an image returns the ImageID of that image.
	    tempImageDB.addImage("Park","///\\\\\\img_2810b_small.jpg");
	    //tempImageDB.addImage("Creates error- not found","///\\\\\\img_monkeys_small.jpg");
	    //tempImageDB.addImage("Creates Error- not an image","///\\\\\\NotAnImage.txt");
	    tempImageDB.addImage("Igloo in Bristol","///\\\\\\img_6088b_small.jpg");
	    tempImageDB.addImage("Pink","///\\\\\\img_5672bp_small.jpg");
	    tempImageDB.addImage("Speed","///\\\\\\img_2926_small.jpg");
	    tempImageDB.addImage("Food","///\\\\\\img_F028c_small.jpg");
	    tempImageDB.addImage("Data Structures&Algorithms note 1","///\\\\\\DSA_1.bmp");
	    //tempImageDB.addImage("Large file- many MegaPixels","///\\\\\\jamaica1730homannsheirs.jpg");
	    tempImageDB.addImage("Graph Notes for C/W","///\\\\\\DSA_7.bmp");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados01.jpg");
	    //tempImageDB.addImage("Barbados","///\\\\\\barbados02.jpg");
	    //tempImageDB.addImage("Barbados","///\\\\\\barbados03.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados04.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados05.jpg");
	    //tempImageDB.addImage("Barbados","///\\\\\\barbados06.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados07.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados08.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados09.jpg");
	    //tempImageDB.addImage("Barbados","///\\\\\\barbados10.jpg");
	    tempImageDB.addImage("Barbados","///\\\\\\barbados-08-046-733284.jpg");

            tempImageDB.save(saveFileName);
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
	    imageIDs = mainGUI.mainImageDB.getImageIDsFromTagTitle(filterTag);
	    break;
	}
	//if imageIDs.length==0
	//then a file should be added first (Construct with Init&imports, then return;)
      	imageList = new ImageObject[imageIDs.length];
	for(int i=0; i<imageIDs.length;i++){
	    imageList[i] = new ImageObject(mainGUI.mainImageDB.getImageFilename(imageIDs[i]));
	}
	lastIndex = (imageIDs.length - 1);
	if(loadType!=LoadType.Init){
	    mainGUI.mainPanel.repaint();
	    mainGUI.thumbPanel.repaint();
	}
    }

    void importImages(File[] files){
	isLocked = true;
	for(File f : files){
	    //System.out.println(f.getPath()+ " is the getPath and the absPath is " +f.getAbsolutePath());//Should be removed later
	    mainGUI.mainImageDB.addImage("Title 1",f.getAbsolutePath());
	}
	try{
	    if(currentFilter.equals("Show All Images")){
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
	if(currentFilter.equals("Show All Images")){
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
	mainGUI.mainPanel.repaint();
	mainGUI.thumbPanel.repaint();
    }
    void prevImage() {
	currentI = prev(currentI);
	mainGUI.mainPanel.repaint();
	mainGUI.thumbPanel.repaint();
    }
    void offsetImage(int by){
	currentI = relItoFixI(by);
	mainGUI.mainPanel.repaint();
	mainGUI.thumbPanel.repaint();
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
