/*
 * Running this class should move images to userdir
 * and add them to the DB with some tags.
 * Not runing this file before running GUI could result in app with no images.
 */
public class InitDemoDB {
    static void initDB(String name){
        ImageDatabase tempDB;
	    tempDB = new ImageDatabase(AppDefaults.DBname.value);
            
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
	    addI(tempDB,"Pineapple png","///\\\\\\pineapple.png");
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
	    String anID = addI(tempDB,"Barbados","///\\\\\\barbados-08-046-733284.jpg");

            tempDB.addImageNote(anID, "This is a test test Note!!!\nIts Multilined.", 300, 300, 250, 200);
            tempDB.linkImage(anID,"0", 300, 200, 100, 100);


	    addI(tempDB,"hasJpegThumb","///\\\\\\IMG_0944_Corrected.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\img_2810b_small.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\img_2926_small.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\img_5672bp_small.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\img_6088b_small.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\img_F028c_small.jpg");
	    addI(tempDB,"hasJpegThumb","///\\\\\\PSD_0843.jpg");


            tempDB.tagTag(tempDB.getTagIDFromTagTitle("Data Structures&Algorithms note 1") , tempDB.getTagIDFromTagTitle("Notes"));
            tempDB.tagTag(tempDB.getTagIDFromTagTitle("Graph Notes for C/W") , tempDB.getTagIDFromTagTitle("Notes"));

            tempDB.save(name);
            //copy all images to user dir
    }

    public static void main(String[] args){
        String name;
        if (args.length!=1){
            System.out.println("Usage: java InitDemoDB databasename");
            Settings settings = new Settings();
            name = settings.getSetting("homeDir")+settings.getSetting("databasePathExt")+settings.getSetting("databaseFileName");
        } else name = args[0];
        System.out.println("Using: "+name);
        initDB(name);
    }

    static String addI(ImageDatabase DB,String title,String filename){
        if(DB.getTagIDFromTagTitle(title)==null) DB.addTag(title);
        String id = DB.addImage(title, filename);
        DB.tagImage(id, DB.getTagIDFromTagTitle(title));
        return id;
    }
}
