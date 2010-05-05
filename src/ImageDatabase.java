import java.io.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.awt.Rectangle;

class ImageDatabase
{
  Log log = new Log();
  private IndexedTable ImageTable; // Table 1
  private IndexedTable TagTable; // Table 2
  private IndexedTable ImageToTagTable; // Table 3
  private IndexedTable TagToTagTable; // Table 4
  private IndexedTable ImageToImageTable; // Table 5
  private IndexedTable ImageToNoteTable; // Table 6
  private IndexedTable ImageToSpecialTable; // Table 7
  private IndexedTable UpdateTable;
  private String Name;
  private String StoredFilename;
  private boolean DoAutosave;
  private int NextImageID;
  private int NextTagID;
  private int NextNoteID;
  private int NextLinkID;

  //Change this when database structure changes, e.g. after new table created
  //Used to check for database incompatabilities
  static String getDatabaseVersion(){
      return "gamma_r276";
  }

  // Create an image database with the supplied name
  ImageDatabase(String NewName)
  {
      Name = NewName;
      NextTagID = 0;
      NextImageID = 0;
	  NextNoteID = 0;
	  NextLinkID = 0;
      DoAutosave = true;
	  StoredFilename = NewName;
          BuildImageTable();
          BuildTagTable();
          BuildImageToTagTable();
          BuildTagToTagTable();
          BuildImageToImageTable();
          BuildImageToNoteTable();
		  BuildImageToSpecialTable();
  }

  private void BuildImageTable() {
        String[] ImageHeader = {"ImageID", "Title", "Filename"};
        boolean[] ImageKeys = {true, false, false};
        ImageTable = new IndexedTable("ImageTable", new Record(ImageHeader), ImageKeys);
    }

    private void BuildTagTable() {
        String[] TagHeader = {"TagID", "Title"};
        boolean[] TagKeys = {true, false, false};
        TagTable = new IndexedTable("TagTable", new Record(TagHeader), TagKeys);
    }

    private void BuildImageToTagTable() {
        String[] ImageToTagHeader = {"ImageID", "TagID"};
        boolean[] ImageToTagKeys = {true, true};
        ImageToTagTable = new IndexedTable("ImageToTagTable", new Record(ImageToTagHeader), ImageToTagKeys);
    }

    private void BuildTagToTagTable() {
        String[] TagToTagHeader = {"TagID", "TagID"};
        boolean[] TagToTagKeys = {true, true};
        TagToTagTable = new IndexedTable("TagToTagTable", new Record(TagToTagHeader), TagToTagKeys);
    }

    private void BuildImageToImageTable() {
        String[] ImageToImageHeader = {"LinkID", "FromImageID", "ToImageID", "X", "Y", "Width", "Height"};
        boolean[] ImageToImageKeys = {true, false, false, false, false, false, false};
        ImageToImageTable = new IndexedTable("ImageToImageTable", new Record(ImageToImageHeader), ImageToImageKeys);
    }

    private void BuildImageToNoteTable() {
        String[] ImageToNoteHeader = {"NoteID", "ImageID", "Note", "X", "Y", "Width", "Height"};
        boolean[] ImageToNoteKeys = {true, false, false, false, false, false, false};
        ImageToNoteTable = new IndexedTable("ImageToNoteTable", new Record(ImageToNoteHeader), ImageToNoteKeys);
    }
	
	private void BuildImageToSpecialTable() {
        String[] ImageToSpecialHeader = {"ImageID", "SpecialType", "SpecialString"};
        boolean[] ImageToSpecialKeys = {true, true, false};
        ImageToSpecialTable = new IndexedTable("ImageToSpecialTable", new Record(ImageToSpecialHeader), ImageToSpecialKeys);
    }
	
	private void BuildUpdateTable() {
		String[] UpdateHeader = {"TableNum", "UpdateType", "RecordString"};
        boolean[] UpdateKeys = {true, true, true};
        UpdateTable = new IndexedTable("UpdateTable", new Record(UpdateHeader), UpdateKeys);
	}

  // Loads the image database from the files it's stored in
  ImageDatabase(String NewName, String Filename)
  {
      BufferedReader FileInput;
	  DoAutosave = true;
	  StoredFilename = Filename;
      try
      {
        FileInput = new BufferedReader(new FileReader(Filename + "_ID"));
        Name = FileInput.readLine();
        NextImageID = Integer.parseInt(FileInput.readLine());
        NextTagID = Integer.parseInt(FileInput.readLine());
		NextNoteID = Integer.parseInt(FileInput.readLine());
	    NextLinkID = Integer.parseInt(FileInput.readLine());
      }
      catch (Exception TheError)
      {
          log.print(LogType.DebugError, "Error Parsing int from file");
          Name = NewName;
          NextTagID = 0;
          NextImageID = 0;
          NextNoteID = 0;
          NextLinkID = 0;
      }
      try {
          ImageTable = new IndexedTable(Filename + "_ImageTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load ImageTable");
          BuildImageTable();
      }
      try {
          TagTable = new IndexedTable(Filename + "_TagTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load TagTable");
          BuildTagTable();
      }
      try {
          ImageToTagTable = new IndexedTable(Filename + "_ImageToTagTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load ImageToTagTable");
          BuildImageToTagTable();
      }
      try {
          TagToTagTable = new IndexedTable(Filename + "_TagToTagTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load TagToTagTable");
          BuildImageToTagTable();
      }
      try {
          ImageToImageTable = new IndexedTable(Filename + "_ImageToImageTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load ImageToImageTable");
          BuildImageToImageTable();
      }
      try {
          ImageToNoteTable = new IndexedTable(Filename + "_ImageToNoteTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load ImageToNoteTable: "+TheError.toString());
          BuildImageToNoteTable();
      }
	  try {
          ImageToSpecialTable = new IndexedTable(Filename + "_ImageToSpecialTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load ImageToSpecialTable: "+TheError.toString());
          BuildImageToSpecialTable();
      }
	  try {
          UpdateTable = new IndexedTable(Filename + "_UpdateTable");
      } catch (Exception TheError) {
          log.print(LogType.DebugError, "Unable to load UpdateTable: "+TheError.toString());
          BuildUpdateTable();
      }
  }
  
  String getName() { return Name; }
  IndexedTable getImageTable() { return ImageTable; }
  IndexedTable getTagTable() { return TagTable; }
  IndexedTable getImageToTagTable() { return ImageToTagTable; }
  IndexedTable getTagToTagTable() { return TagToTagTable; }
  IndexedTable getImageToImageTable() { return ImageToImageTable; }
  IndexedTable getImageToNoteTable() { return ImageToNoteTable; }
  IndexedTable getImageToSpecialTable() { return ImageToSpecialTable; }
  IndexedTable getUpdateTable() { return UpdateTable; }
  
  void Autosave()
  {
	if (DoAutosave == true)
		save(StoredFilename);
  }
  
  void setAutosave(boolean Set) { DoAutosave = Set; }
  boolean getAutosave() { return DoAutosave; }
  void setStoredFilename(String Filename) { StoredFilename = Filename; }
  String getStoredFilename() { return StoredFilename; }
  
  
  // Prints out a representation of the different tables
  void print()
  {
      System.out.println(ImageTable.toString());
      System.out.println(TagTable.toString());
      System.out.println(ImageToTagTable.toString());
      System.out.println(TagToTagTable.toString());
      System.out.println(ImageToImageTable.toString());
      System.out.println(ImageToNoteTable.toString());
      System.out.println(UpdateTable.toString());
  }
  
  // Save the entire database to the desired filename
  void save(String Filename)
  {
    PrintStream FileOutput;
    try
    {
      FileOutput = new PrintStream(new FileOutputStream(Filename + "_ID"));
      FileOutput.println(Name);
      FileOutput.println(Integer.toString(NextImageID));
      FileOutput.println(Integer.toString(NextTagID));
      FileOutput.println(Integer.toString(NextNoteID));
      FileOutput.println(Integer.toString(NextLinkID));
    }
    catch (Exception TheError)
    {
      throw new Error(TheError);    
    } 
    ImageTable.save(Filename + "_ImageTable");
    TagTable.save(Filename + "_TagTable");
    ImageToTagTable.save(Filename + "_ImageToTagTable");
    TagToTagTable.save(Filename + "_TagToTagTable");
    ImageToImageTable.save(Filename + "_ImageToImageTable");
    ImageToNoteTable.save(Filename + "_ImageToNoteTable");
    ImageToSpecialTable.save(Filename + "_ImageToSpecialTable");
    UpdateTable.save(Filename + "_UpdateTable");
  }
  
  // Merge Tags of the same title
  int mergeCommonTagTitles()
  {
  	Enumeration AllTags;
  	Enumeration TagsWithTitle;
  	Enumeration TempRecords;
  	Record TempRecord;
  	String MergeToID;
  	String OldID;
  	HashSet<String> UniqueTitles = new HashSet<String>();
  	String[] TagTitles;
  	AllTags = ImageTable.elements();
  	int Result=1;
  	int TempResult;
  	while (AllTags.hasMoreElements())
  	{
  		TempRecord = (Record) AllTags.nextElement();
  		UniqueTitles.add(TempRecord.getField(1));
  	}
  	TagTitles = new String[UniqueTitles.size()];
  	TagTitles = UniqueTitles.toArray(TagTitles);
  	for (int i=0; i<TagTitles.length; i++)
  	{
  		TagsWithTitle = TagTable.getRecords(TagTitles[i], 1).elements();
  		if (TagsWithTitle.hasMoreElements())
  		{
	  		TempRecord = (Record) TagsWithTitle.nextElement();
	  		MergeToID = TempRecord.getField(0);
	  		while (TagsWithTitle.hasMoreElements())
	  		{
	  			TempRecord = (Record) TagsWithTitle.nextElement();
	  			OldID = TempRecord.getField(0);
	  			TempResult = TagTable.deleteRecord(TempRecord);
	  			if (TempResult == -1)
	  				Result = -1;
	  			else
	  				addChange(2, "Delete", TempRecord);
	  			// Update ImageToTagTable
	  			TempRecords = ImageToTagTable.getRecords(OldID, 1).elements();
	  			while (TempRecords.hasMoreElements())
		  		{
		  			TempRecord = (Record) TempRecords.nextElement();
		  			TempResult = ImageToTagTable.deleteRecord(TempRecord);
		  			if (TempResult == -1)
	  					Result = -1;
	  				else
		  				addChange(3, "Delete", TempRecord);
		  			TempRecord.setField(0, MergeToID);
		  			TempResult = ImageToTagTable.addRecord(TempRecord);
		  			if (TempResult == -1)
	  					Result = -1;
	  				else
		  				addChange(3, "Add", TempRecord);
		  		}
		  		// Update TagToTagTable
		  		TempRecords = TagToTagTable.getRecords(OldID, 0).elements();
	  			while (TempRecords.hasMoreElements())
		  		{
		  			TempRecord = (Record) TempRecords.nextElement();
		  			TempResult = TagToTagTable.deleteRecord(TempRecord);
		  			if (TempResult == -1)
	  					Result = -1;
	  				else
		  				addChange(4, "Delete", TempRecord);
		  			TempRecord.setField(0, MergeToID);
		  			if (!(TempRecord.getField(0).equals(TempRecord.getField(1))))
		  			{
		  				TempResult = TagToTagTable.addRecord(TempRecord);
		  				if (TempResult == -1)
	  						Result = -1;
	  					else
	  						addChange(4, "Add", TempRecord);
		  			}
		  			
		  		}
		  		TempRecords = TagToTagTable.getRecords(OldID, 1).elements();
	  			while (TempRecords.hasMoreElements())
		  		{
		  			TempRecord = (Record) TempRecords.nextElement();
		  			TempResult = TagToTagTable.deleteRecord(TempRecord);
		  			if (TempResult == -1)
	  						Result = -1;
	  				else
	  					addChange(4, "Delete", TempRecord);
		  			TempRecord.setField(1, MergeToID);
		  			if (!(TempRecord.getField(0).equals(TempRecord.getField(1))))
		  			{
		  				TempResult = TagToTagTable.addRecord(TempRecord);
		  				if (TempResult == -1)
	  						Result = -1;
	  					else
	  						addChange(4, "Add", TempRecord);
		  			}
		  		}
	  		}
  		}
  	}
  	return Result;
  }
  
  // Add a change to the database
  int addChange(int TableNum, String UpdateType, Record RecordChanged)
  {
		Record TempRecord;
		String RecordString = "";
		for (int f=0; f<RecordChanged.getNumFields(); f++)
		{
			// If doing image filename, just add filename not path
			if ((f==2) && (TableNum == 1))
			{
				String PathString = RecordChanged.getField(f);
				File PathFile = new File(PathString);
				RecordString = RecordString + FileUtils.escape(PathFile.getName());
			}
			else
				RecordString = RecordString + FileUtils.escape(RecordChanged.getField(f));
			if (f < RecordChanged.getNumFields() - 1)
				RecordString = RecordString + ',';
		}
		String[] ChangeRecordArray = {Integer.toString(TableNum), UpdateType, RecordString};
		String[] AddRecordArray = {Integer.toString(TableNum), "Add", RecordString};
		// If deleting from a table, and addition was only since last update, just remove the addition from the changes table
		if (UpdateType.equals("Delete"))
		{
			TempRecord = UpdateTable.getRecord(new Record(AddRecordArray));
			if (TempRecord != null)
				return UpdateTable.deleteRecord(TempRecord);
			else
				return -1;
		}
		else
			return UpdateTable.addRecord(new Record(ChangeRecordArray));
  }
  
  // Produce the string of update tables
  String makeUpdateString()
  {
		Enumeration UpdateRecords = UpdateTable.elements();
		Record TempRecord;
		String ResultString = "";
		while (UpdateRecords.hasMoreElements())
		{
			TempRecord = (Record) UpdateRecords.nextElement();
			ResultString = ResultString + TempRecord.getField(0) + "," + TempRecord.getField(1) + "," + TempRecord.getField(2) + "\n";
		}
		return ResultString;
  }
  
  // Produce a list of images to send to the Mobile()
  String[] imageFilenamesForMobile()
  {
  	String[] SearchArray = {"1", "Add", null};
  	ArrayList<String> Filenames = new ArrayList<String>();
  	Record AddRecord;
  	String ImageRecordString;
  	String[] ImageRecordFields;
  	IndexedTable AddedImagesTable = ImageTable.getRecords(new Record(SearchArray));
  	Enumeration AddedImages = AddedImagesTable.elements();
  	while(AddedImages.hasMoreElements())
  	{
  			AddRecord = (Record) AddedImages.nextElement();
  			ImageRecordString = AddRecord.getField(2);
  			ImageRecordFields = ImageRecordString.split(",");
  			// ImageRecordFields[0] is the ImageID
  			Filenames.add(getImageFilename(ImageRecordFields[0]));
  	}
  	return Filenames.toArray(new String[Filenames.size()]);
  }
  
  // Add items from the mobile and assign IDs
  String assignMobileItemsIDs(String StringFromMobile, String PathForImages)
  {
	String[] Updates = StringFromMobile.split("\n");
	String[] Fields;
	String[] RecordArray;
	String[] ImageRecordArray;
	String Result = "";
	int TableNum;
	int ComputerID;
	for (int u=0; u<Updates.length; u++)
	{
		Fields = Updates[u].split(",");
		TableNum = Integer.parseInt(Fields[0]);
		RecordArray = new String[Fields.length - 2];
		for (int f=0; f<RecordArray.length; f++)
			RecordArray[f] = FileUtils.unEscape(Fields[f+2]);
		switch (TableNum)
		{
			case 1:
				ImageRecordArray = new String[3];
				ImageRecordArray[0] = Fields[0];
				ImageRecordArray[1] = "Image" + Fields[0];
				ImageRecordArray[2] = PathForImages + Fields[1];
				ComputerID = ImageTable.addRecord(new Record(ImageRecordArray));
				if (ComputerID != -1)
					Result = Result + Fields[0] + "," + Fields[1] + "," + Integer.toString(ComputerID) + "\n";
				break;
			case 2:
				ComputerID = TagTable.addRecord(new Record(RecordArray));
				if (ComputerID != -1)
					Result = Result + Fields[0] + "," + Fields[1] + "," + Integer.toString(ComputerID) + "\n";
				break;
		}
	}
	return Result;
  }
  
  // Add items from the mobile and assign IDs
  int makeChangesFromMobile(String StringFromMobile)
  {
		String[] Updates = StringFromMobile.split("\n");
		String[] Fields;
		String[] RecordArray;
		int TempResult = 1;
		int Result = 1;
		int TableNum;
		String ComputerID;
		for (int u=0; u<Updates.length; u++)
		{
			Fields = Updates[u].split(",");
			TableNum = Integer.parseInt(Fields[0]);
			RecordArray = new String[Fields.length - 2];
			for (int f=0; f<RecordArray.length; f++)
				RecordArray[f] = FileUtils.unEscape(Fields[f+2]);
			switch (TableNum)
			{
				case 1:
					if (Fields[1].equals("Add"))
					{
						TempResult = -1;
					}
					if (Fields[1].equals("Delete"))
						TempResult = ImageTable.deleteRecord(new Record(RecordArray));
					if (TempResult < Result)
						Result = TempResult;
					break;
				case 2:
					if (Fields[1].equals("Add"))
						TempResult = -1;
					if (Fields[1].equals("Delete"))
						TempResult = TagTable.deleteRecord(new Record(RecordArray));
					if (TempResult < Result)
						Result = TempResult;
				case 3:
					if (Fields[1].equals("Add"))
						TempResult = ImageToTagTable.addRecord(new Record(RecordArray));
					if (Fields[1].equals("Delete"))
						TempResult = ImageToTagTable.deleteRecord(new Record(RecordArray));
					if (TempResult < Result)
						Result = TempResult;
				case 4:
					if (Fields[1].equals("Add"))
						TempResult = TagToTagTable.addRecord(new Record(RecordArray));
					if (Fields[1].equals("Delete"))
						TempResult = TagToTagTable.deleteRecord(new Record(RecordArray));
					if (TempResult < Result)
						Result = TempResult;
			}
		}
		return Result;
  }
  
  void refreshChanges()
  {
      BuildUpdateTable();
      save(StoredFilename);
  }
  
  // Add a new image to the database
  String addImage(String Title, String Filename)
  {
      String[] RecordArray = {Integer.toString(NextImageID), Title, Filename};
      NextImageID++;
      if (ImageTable.addRecord(new Record(RecordArray)) == -1)
        return null; // Failed, record already present
      else
	  {
        Autosave();
		return Integer.toString(NextImageID - 1);
	  }
  }
  
  // Add a new special tag for an image
  int addSpecial(String ImageID, String SpecialType, String Special)
  {
	int Result;
	String[] RecordArray = {ImageID, SpecialType, Special};
	Result = ImageToSpecialTable.addRecord(new Record(RecordArray));
	if (Result == 1)
		Autosave();
	return Result;
  }
  
  // Delete special tag for an image
  int deleteSpecial(String ImageID, String SpecialType)
  {
	int Result;
	String[] RecordArray = {ImageID, SpecialType, null};
	Record TempRecord = ImageToSpecialTable.getRecord(new Record(RecordArray));
	if (TempRecord == null)
		return -1;
	else
	{
		Result = ImageToSpecialTable.deleteRecord(TempRecord);
		if (Result == 1)
			Autosave();
		return Result; 
	}
  }
  
  // Return all special tags for an image
  IndexedTable getImageSpecials(String ImageID)
  {
	return ImageToSpecialTable.getRecords(ImageID, 0);
  }
  
  // Return special string of a certain type
  String getImageSpecial(String ImageID, String SpecialType)
  {
	String[] RecordArray = {ImageID, SpecialType, null};
	Record TempRecord = ImageToSpecialTable.getRecord(new Record(RecordArray));
	if (TempRecord == null)
		return null;
    else
		return TempRecord.getField(2);
  }
  
  // Delete an image from the database (by fields)
  int deleteImage(String ImageID)
  {
	Record TempRecord = ImageTable.getRecord(ImageID, 0);
	if (TempRecord == null)
		return -1;
	else
		return deleteImage(TempRecord);
  }
  
  // Delete an image from the database (by record)
  int deleteImage(Record r)
  {
    IndexedTable Matches;
    Enumeration Records;	
	// Delete ImageToTag records containing the image
    Matches = ImageToTagTable.getRecords(r.getField(0), 0);
    Records = Matches.elements();
    while (Records.hasMoreElements())
    {
      if (ImageToTagTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	// Delete ImageToImage records containing the image
	Matches = ImageToImageTable.getRecords(r.getField(0),1);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToImageTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	Matches = ImageToImageTable.getRecords(r.getField(0),2);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToImageTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	//Delete ImageToNote records containing the image
	Matches = ImageToNoteTable.getRecords(r.getField(0),1);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToNoteTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	//Delete ImageToSpecial records containing the image
	Matches = ImageToSpecialTable.getRecords(r.getField(0),0);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToSpecialTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	// Delete the image record from the image table
	if (ImageTable.deleteRecord(r) == -1)
      return -1;
	Autosave();
    return 1;
  }
  
  // Delete an ImageToImage link from the database (by fields)
  int deleteLink(String LinkID)
  {
	Record TempRecord = ImageToImageTable.getRecord(LinkID, 0);
	if (TempRecord == null)
		return -1;
	else
		return deleteLink(TempRecord);
  }
  
  // Delete an ImageToImage link from the database (by record)
  int deleteLink(Record r)
  {
    int Result;
	Result = ImageToImageTable.deleteRecord(r);
	if (Result == 1)
		Autosave();
	return Result;
  }
  
  // Delete an ImageToNote link from the database (by fields)
  int deleteNote(String NoteID)
  {
	Record TempRecord = ImageToNoteTable.getRecord(NoteID, 0);
	if (TempRecord == null)
		return -1;
	else
		return deleteNote(TempRecord);
  }
  
  // Delete an ImageToImage link from the database (by record)
  int deleteNote(Record r)
  {
    int Result = ImageToNoteTable.deleteRecord(r);
	if (Result == 1)
		Autosave();
	return Result;
  }
  
  // Delete an ImageToTag link from the database (by fields)
  int deleteImageTag(String ImageID, String TagID)
  {
	String[] RecordArray = {ImageID, ImageID};
    return deleteImageTag(new Record(RecordArray));
  }
  
  // Delete an ImageToTag link from the database (by record)
  int deleteImageTag(Record r)
  {
    int Result = ImageToTagTable.deleteRecord(r);
	if (Result == 1)
		Autosave();
	return Result;
  }
  
  // Delete an TagToTag link from the database (by fields)
  int deleteTagTag(String ImageID, String TagID)
  {
	String[] RecordArray = {ImageID, ImageID};
	return deleteTagTag(new Record(RecordArray));
  }
  
  // Delete an TagToTag link from the database (by record)
  int deleteTagTag(Record r)
  {
    int Result = TagToTagTable.deleteRecord(r);
	if (Result == 1)
		Autosave();
	return Result;
  }
  
  // Delete a tag from the database (by fields)
  int deleteTag(String TagID)
  {
    Record TempRecord = TagTable.getRecord(TagID, 0);
	if (TempRecord == null)
		return -1;
	else
		return deleteTag(TempRecord);
  }
  
  // Delete a tag from the database (by record)
  int deleteTag(Record r)
  {
    IndexedTable TagMatches;
    Enumeration TagRecords;
    // Get tag to image records including the tag and delete them
    TagMatches = ImageToTagTable.getRecords(r.getField(0), 1);
    TagRecords = TagMatches.elements();
    while (TagRecords.hasMoreElements())
    {
      if (ImageToTagTable.deleteRecord((Record) TagRecords.nextElement()) == -1)
        return -1;
    }
    // Get tag to tag records including the tag (as tagee) and delete them
    TagMatches = TagToTagTable.getRecords(r.getField(0), 0);
    TagRecords = TagMatches.elements();
    while (TagRecords.hasMoreElements())
    {
      System.out.println("deleting as tagee");
      if (TagToTagTable.deleteRecord((Record) TagRecords.nextElement()) == -1)
        return -1;
    }
    // Get tag to tag records including the tag (as tagger) and delete them
    TagMatches = TagToTagTable.getRecords(r.getField(0), 1);
    TagRecords = TagMatches.elements();
    while (TagRecords.hasMoreElements())
    {
      if (TagToTagTable.deleteRecord((Record) TagRecords.nextElement()) == -1)
        return -1;
    }
    // Delete tag from tagtable
    if (TagTable.deleteRecord(r) == -1)
      return -1;
	Autosave();
    return 1;
  }
  
  // Add a new tag to the database
  String addTag(String Title)
  {
    String[] RecordArray = {Integer.toString(NextTagID), Title};
    NextTagID++;
    if (TagTable.addRecord(new Record(RecordArray)) == -1)
    {
      return null;
    }
    else
	{
      Autosave();
	  return Integer.toString(NextTagID - 1);
	}
  }
  
  // Link an image with a tag
  int tagImage(String ImageID, String TagID)
  {
    String[] RecordString = {ImageID, TagID};
    if (ImageTable.getRecord(ImageID, 0) == null)
      return 0;
    else
      if (TagTable.getRecord(TagID, 0) == null)
        return -1;
      else
      {
        ImageToTagTable.addRecord(new Record(RecordString));
		Autosave();
        return 1;
      }
  }
  
  // Link an area of an image to a note
  String addImageNote(String ImageID, String Note, int X, int Y, int Width, int Height) {
        String[] RecordString = {Integer.toString(NextNoteID), ImageID, Note, Integer.toString(X), Integer.toString(Y), Integer.toString(Width), Integer.toString(Height)};
        NextNoteID++;
        if (ImageTable.getRecord(ImageID, 0) == null)
            return null;
        else
		{
            ImageToNoteTable.addRecord(new Record(RecordString));
			Autosave();
            return Integer.toString(NextNoteID-1);
		}
  }
  
  // Link an area of an image to another image
  String linkImage(String ToImageID, String FromImageID, int X, int Y, int Width, int Height)
  {
    String[] RecordString = {Integer.toString(NextLinkID), ToImageID, FromImageID, Integer.toString(X), Integer.toString(Y), Integer.toString(Width), Integer.toString(Height)};
    NextLinkID++;
	if (ImageTable.getRecord(ToImageID, 0) == null)
      return null;
    else
      if (ImageTable.getRecord(FromImageID, 0) == null)
        return null;
      else
      {
        ImageToImageTable.addRecord(new Record(RecordString));
        Autosave();
		return Integer.toString(NextLinkID-1);
      }
  }
  
  // Link an area of an image to another image
  Record getLink(String LinkID)
  {
    return ImageToImageTable.getRecord(LinkID, 0);
  }
  
  // Link a tag with another tag, tagee is tagged with tagger
  int tagTag(String TageeID, String TaggerID)
  {
    String[] RecordString = { TageeID, TaggerID }; 
    if (TagTable.getRecord(TageeID, 0) == null)
      return -1;
    else
      if (TagTable.getRecord(TaggerID, 0) == null)
        return 0;
      else
      {
		  TagToTagTable.addRecord(new Record(RecordString));
		  Autosave();
          return 1;
      }
  }
  
  // Produce a sub table of Notes for a certain ImageID
  IndexedTable getNotesFromImageID(String ImageID)
  {
	return ImageToNoteTable.getRecords(ImageID, 1);
  }
  
  // Produce a sub table of links for a certain ImageID
  IndexedTable getLinksFromImageID(String ImageID)
  {
	return ImageToImageTable.getRecords(ImageID, 1);
  }
  
  // Produce the rectangles of note links for a certain ImageID
  Rectangle[] getNoteRectanglesFromImageID(String ImageID, int XOffset, int YOffset, double Scale)
  {
	IndexedTable TempTable = ImageToNoteTable.getRecords(ImageID, 1);
	Enumeration Records;
	Record TempRecord;
	Rectangle[] Result;
    int outX,outY,outW,outH;
	int i=0;
	if (TempTable.getNumRecords() == 0)
		return null;
	else
	{
		Result = new Rectangle[TempTable.getNumRecords()];
		Records = TempTable.elements();
		while(Records.hasMoreElements())
		{
		  TempRecord = (Record) Records.nextElement();
		  outX = (int) (XOffset + (Scale*Integer.parseInt(TempRecord.getField(3))));
		  outY = (int) (YOffset + (Scale*Integer.parseInt(TempRecord.getField(4))));
		  outW = (int) (Scale*Integer.parseInt(TempRecord.getField(5)));
		  outH = (int) (Scale*Integer.parseInt(TempRecord.getField(6)));
		  Result[i] = new Rectangle(outX , outY, outW, outH);
		  i++;
		}
		return Result;
	}
  }
  
  // Produce the rectangles of image links for a certain ImageID
  Rectangle[] getLinkRectanglesFromImageID(String ImageID, int XOffset, int YOffset, double Scale)
  {
	IndexedTable TempTable = ImageToImageTable.getRecords(ImageID, 1);
	Enumeration Records;
	Record TempRecord;
	Rectangle[] Result;
	int outX,outY,outW,outH;
	int i=0;
	if (TempTable.getNumRecords() == 0)
		return null;
	else
	{
		Result = new Rectangle[TempTable.getNumRecords()];
		Records = TempTable.elements();
		while(Records.hasMoreElements())
		{
		  TempRecord = (Record) Records.nextElement();
		  outX = (int) (XOffset + (Scale*Integer.parseInt(TempRecord.getField(3))));
		  outY = (int) (YOffset + (Scale*Integer.parseInt(TempRecord.getField(4))));
		  outW = (int) (Scale*Integer.parseInt(TempRecord.getField(5)));
		  outH = (int) (Scale*Integer.parseInt(TempRecord.getField(6)));
		  Result[i] = new Rectangle(outX , outY, outW, outH);
		  i++;
		}
		return Result;
	}
  }
  
  // Produce the notes for a certain point in an image
  IndexedTable getNotesFromImagePoint(String ImageID, int X, int Y, int XOffset, int YOffset, double Scale)
  {
	Enumeration Records;
	Record TempRecord;
	Rectangle TempRectangle;
	IndexedTable ImageNotes = ImageToNoteTable.getRecords(ImageID, 1);
	IndexedTable PointNotes = new IndexedTable("Result", ImageToNoteTable.getHeader(), ImageToNoteTable.getKeyFields());
	Records = ImageNotes.elements();
	while(Records.hasMoreElements())
	{
		TempRecord = (Record) Records.nextElement();
		TempRectangle = new Rectangle((int) (XOffset + (Scale*Integer.parseInt(TempRecord.getField(3)))), (int)(YOffset + (Scale*Integer.parseInt(TempRecord.getField(4)))), (int) (Scale*Integer.parseInt(TempRecord.getField(5))), (int) (Scale*Integer.parseInt(TempRecord.getField(6))));
		if (TempRectangle.contains(X, Y))
			PointNotes.addRecord(TempRecord);
	}
	return PointNotes;
  }
  
  // Produce an array of String that describe the point in the image
  IDTitle[] getNoteStringsFromImagePoint(String ImageID, int X, int Y, int XOffset, int YOffset, double Scale)
  {
	Record TempRecord;
	IndexedTable TempNotes = getNotesFromImagePoint(ImageID, X, Y, XOffset, YOffset, Scale);
	IDTitle[] Result = new IDTitle[TempNotes.getNumRecords()];
	int i = 0;
	Enumeration NoteRecords = TempNotes.elements();
	while(NoteRecords.hasMoreElements())
	{
		TempRecord = (Record) NoteRecords.nextElement();
		Result[i] = new IDTitle(TempRecord.getField(0), TempRecord.getField(2));
		i++;
	}
	return Result;
  }
  
  // Produce the links for a certain point in an image
  IndexedTable getLinksFromImagePoint(String ImageID, int X, int Y, int XOffset, int YOffset, double Scale)
  {
	Enumeration Records;
	Record TempRecord;
	Rectangle TempRectangle;
	IndexedTable ImageLinks = ImageToImageTable.getRecords(ImageID, 1);
	IndexedTable PointLinks = new IndexedTable("Result", ImageToImageTable.getHeader(), ImageToImageTable.getKeyFields());
	Records = ImageLinks.elements();
	while(Records.hasMoreElements())
	{
		TempRecord = (Record) Records.nextElement();
		TempRectangle = new Rectangle((int) (XOffset + (Scale*Integer.parseInt(TempRecord.getField(3)))), (int)(YOffset + (Scale*Integer.parseInt(TempRecord.getField(4)))), (int) (Scale*Integer.parseInt(TempRecord.getField(5))), (int) (Scale*Integer.parseInt(TempRecord.getField(6))));
		if (TempRectangle.contains(X, Y))
			PointLinks.addRecord(TempRecord);
	}
	return PointLinks;
  }
  
  // Produce an array of imageIDs pointed to by the point in the image
  String[] getImageIDsFromImagePoint(String ImageID, int X, int Y, int XOffset, int YOffset, double Scale)
  {
	return getLinksFromImagePoint(ImageID, X, Y, XOffset, YOffset, Scale).getColArray(2);
  }
  
  // Produce a sub table of Images that are tagged with the TagID
  IndexedTable getImagesFromTagID(String TagID)
  {
    IndexedTable Result;
    String[] ImageIDs;
    // Get the ImageIDs that are tagged with the TagID
    ImageIDs = ImageToTagTable.getRecords(TagID, 1).getColArray(0);
    // Result is an indexed table in the format of ImageTable
    Result = new IndexedTable("Result_Table", ImageTable.getHeader(), ImageTable.getKeyFields());
    // For all the ImageIDs find the complete image record and add it to the result table
    for (int i=0; i<ImageIDs.length; i++)
      Result.addRecord(ImageTable.getRecord(ImageIDs[i], 0));
    return Result;
  }

  // Produce an array of TagIDs used in an Image
  String[] getTagIDsFromImage(String ImageID)
  {
    String[] TagIDs;
    // Get the TagIDs that are used  by the ImageID
    TagIDs = ImageToTagTable.getRecords(ImageID, 0).getColArray(1);
    return TagIDs;
  }
  
  // Produce a sub table of Tags that are tagged with the TagID
  IndexedTable getTagsFromTagID(String TagID)
  {
    IndexedTable Result;
    String[] TagIDs;
    // Get the TagIDs that are tagged with the TagID
    TagIDs = TagToTagTable.getRecords(TagID, 1).getColArray(0);
    // Result is an indexed table in the format of ImageTable
    Result = new IndexedTable("Result_Table", TagTable.getHeader(), TagTable.getKeyFields());
    // For all the TagIDs find the complete tag record and add it to the result table
    if (TagIDs != null)
      for (int i=0; i<TagIDs.length; i++)
        Result.addRecord(TagTable.getRecord(TagIDs[i], 0));
    return Result;
  }
  
  // Get the title of a tag from its tagID
  String getTagTitleFromTagID(String TagID)
  {
    Record TempRecord = TagTable.getRecord(TagID, 0);
    if (TempRecord == null)
        return null;
    else
        return TempRecord.getField(1);
  }
  
  // Get the tagIDa tag from its title
  String getTagIDFromTagTitle(String TagTitle)
  {
    Record TempRecord = TagTable.getRecord(TagTitle, 1);
    if (TempRecord == null)
        return null;
    else
        return TempRecord.getField(0);
  }  
  
  // Get an array of image IDs tagged with the tag
  String[] getImageIDsFromTagID(String TagID)
  {
    IndexedTable TempTable = getImagesFromTagID(TagID);
    return TempTable.getColArray(0);
  }

  // Get an array of image IDs tagged with the tag
  // Also include any images tagged with an ID tagged by this tag.
  // Should change to hashtable or somthing is used instead of array list.
  //This is so we can check if each image is already going to be returned to prevent duplicates
  String[] getImageIDsFromTagIDRecursively(String TagID)
  {
    IndexedTable tempTable = getImagesFromTagID(TagID);
    String[] tempTagTable = getTagIDsFromTagID(TagID);
    ArrayList<String> imagesSoFar = new ArrayList<String>(Arrays.asList(tempTable.getColArray(0)));
	if(tempTagTable!=null)
	  for(String tag: tempTagTable)
	  {
        imagesSoFar.addAll(Arrays.asList(getImageIDsFromTagIDRecursively(tag)));
      }
    return imagesSoFar.toArray(new String[imagesSoFar.size()]);
  }
  
  // Returns a table of images tagged with the tag, includes children
  IndexedTable getImagesFromTagIDChildren(String TagID)
  {
	IndexedTable TempImageTable;
	Enumeration TempImages;
	IndexedTable ResultTable = getImagesFromTagID(TagID);
	String[] TaggedTags = getTagIDsFromTagID(TagID);
	if (TaggedTags != null)
		for(int i=0; i<TaggedTags.length; i++)
		{
			TempImageTable = getImagesFromTagID(TaggedTags[i]);
			TempImages = TempImageTable.elements();
			while (TempImages.hasMoreElements())
				ResultTable.addRecord((Record) TempImages.nextElement());
		}
	return ResultTable;
  }
  
  // Returns an array of ImageIDs tagged with the tag, includes children
  String[] getImageIDsFromTagIDChildren(String TagID)
  {
	return getImagesFromTagIDChildren(TagID).getColArray(0);
  }
  
  // Get an array of tag IDs tagged with the tag
  String[] getTagIDsFromTagID(String TagID)
  {
    IndexedTable TempTable = getTagsFromTagID(TagID);
    if (TempTable.getNumRecords() == 0)
      return null;
    else
      return TempTable.getColArray(0);
  }
  
  // Get an array of image IDs tagged with the tag title (matches tag title to first tagID)
  String[] getImageIDsFromTagTitle(String TagTitle)
  {
    String TagID = this.getTagIDFromTagTitle(TagTitle);
    if (TagID == null)
      return null;
    else
      return getImageIDsFromTagID(TagID);
  }
  
  // Get an array of image IDs tagged with the tag title (matches tag title to all tagIDs)
  String[] getImageIDsFromTagTitleAll(String TagTitle)
  {
    String[] TempResult;
    String[] TagIDs = TagTable.getRecords(TagTitle, 1).getColArray(0);
    ArrayList<String> ResultList = new ArrayList<String>();
    String[] Result;
    for (int i=0; i<TagIDs.length; i++)
    {
      TempResult = this.getImageIDsFromTagID(TagIDs[i]);
      if (TempResult != null)
        for (int j=0; j<TempResult.length; j++)
        {
          if (ResultList.contains(TempResult[j]) == false)
            ResultList.add(TempResult[j]);
        }
    }
    Result = new String[ResultList.size()];
    ResultList.toArray(Result);
    return Result;
  }
  
  // Get an array of image filenames tagged with the tag
  String[] getFilenamesFromTagID(String TagID)
  {
    IndexedTable TempTable;
    TempTable = getImagesFromTagID(TagID);
    if (TempTable.getNumRecords() == 0)
      return null;
    else
      return TempTable.getColArray(2);
  }
  
  // Get an array of all the image IDs
  String[] getAllImageIDs()
  {
    return ImageTable.getColArray(0);
  }
  
  // Get an array of all the tag IDs
  IDTitle[] getTagIDTitles()
  {
    String[] TagIDs = TagTable.getColArray(0);
    IDTitle[] Result = new IDTitle[TagIDs.length];
    for (int i=0; i<Result.length; i++)
      Result[i] = new IDTitle(TagIDs[i], getTagTitleFromTagID(TagIDs[i]));
    return Result;
  }
  
  // Get an array of TagIDs of all tags
  String[] getAllTagIDs()
  {
    return TagTable.getColArray(0);
  }
  
  // Get an array of all the tag Titles (shouldnt be used - title not key field - titles not unique)
  String[] getAllTagTitles()
  {
    return TagTable.getColArray(1);
  }
  
  // Get the filename of a certain image (by ID)
  String getImageFilename(String ImageID)
  {
    Record FoundRecord = ImageTable.getRecord(ImageID, 0);
    if (FoundRecord == null)
      return null;
    else
      return FoundRecord.getField(2);
  }
  
  // Get a list of possible ImageIDs from the image title (which doesnt have to be unique)
  String[] getPossibleIDs(String Title)
  {
    IndexedTable FoundRecords = ImageTable.getRecords(Title, 1);
    if (FoundRecords.getNumRecords() == 0)
      return null;
    else
      return FoundRecords.getColArray(0);
  }
  
  // Get a particular images record (by ID)
  Record getImageRecord(String ImageID)
  {
    Record FoundRecord = ImageTable.getRecord(ImageID, 0); 
    if (FoundRecord == null)
      return null;
    else
      return FoundRecord;
  }
  
  
  // Get an array of all the image filenames
  String[] getAllFilenames()
  {    
    return ImageTable.getColArray(2);
  }
  
}
