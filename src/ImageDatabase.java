import java.io.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Rectangle;

class ImageDatabase
{
  private IndexedTable ImageTable;
  private IndexedTable TagTable;
  private IndexedTable ImageToTagTable;
  private IndexedTable TagToTagTable;
  private IndexedTable ImageToImageTable;
  private IndexedTable ImageToNoteTable;
  private String Name;
  private int NextImageID;
  private int NextTagID;
  private int NextNoteID;
  private int NextLinkID;
  
  // Create an image database with the supplied name
  ImageDatabase(String NewName)
  {
      Name = NewName;
      NextTagID = 0;
      NextImageID = 0;
	  NextNoteID = 0;
	  NextLinkID = 0;
      String[] ImageHeader = {"ImageID", "Title", "Filename"};
      boolean[] ImageKeys = {true, false, false};
      ImageTable = new IndexedTable("ImageTable", new Record(ImageHeader), ImageKeys);
      
      String[] TagHeader = {"TagID", "Title"};
      boolean[] TagKeys = {true, false, false};
      TagTable = new IndexedTable("TagTable", new Record(TagHeader), TagKeys);
      
      String[] ImageToTagHeader = {"ImageID", "TagID"};
      boolean[] ImageToTagKeys = {true, true};
      ImageToTagTable = new IndexedTable("ImageToTagTable", new Record(ImageToTagHeader), ImageToTagKeys);
      
      String[] TagToTagHeader = {"TagID", "TagID"};
      boolean[] TagToTagKeys = {true, true};
      TagToTagTable = new IndexedTable("TagToTagTable", new Record(TagToTagHeader), TagToTagKeys);
	  
	  String[] ImageToImageHeader = {"LinkID", "FromImageID", "ToImageID", "X", "Y", "Width", "Height"};
      boolean[] ImageToImageKeys = {true, false, false, false, false, false, false};
      ImageToImageTable = new IndexedTable("ImageToImageTable", new Record(ImageToImageHeader), ImageToImageKeys);
	  
	  String[] ImageToNoteHeader = {"NoteID", "ImageID", "Note", "X", "Y", "Width", "Height"};
      boolean[] ImageToNoteKeys = {true, false, false, false, false, false, false};
      ImageToImageTable = new IndexedTable("ImageToNoteTable", new Record(ImageToNoteHeader), ImageToNoteKeys);
  }
  
  // Loads the image database from the files it's stored in
  ImageDatabase(String NewName, String Filename)
  {
      BufferedReader FileInput;
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
        Name = NewName;
		NextTagID = 0;
		NextImageID = 0;
	    NextNoteID = 0;
	    NextLinkID = 0;		
      }
	  try
	  {
		ImageTable = new IndexedTable(Filename + "_ImageTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] ImageHeader = {"ImageID", "Title", "Filename"};
		boolean[] ImageKeys = {true, false, false};
		ImageTable = new IndexedTable("ImageTable", new Record(ImageHeader), ImageKeys);
	  }
	  try
	  {
		TagTable = new IndexedTable(Filename + "_TagTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] TagHeader = {"TagID", "Title"};
		boolean[] TagKeys = {true, false, false};
		TagTable = new IndexedTable("TagTable", new Record(TagHeader), TagKeys);
	  }
	  try
	  {
		ImageToTagTable = new IndexedTable(Filename + "_ImageToTagTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] ImageToTagHeader = {"ImageID", "TagID"};
		boolean[] ImageToTagKeys = {true, true};
		ImageToTagTable = new IndexedTable("ImageToTagTable", new Record(ImageToTagHeader), ImageToTagKeys);
	  }
	  try
	  {
		TagToTagTable = new IndexedTable(Filename + "_TagToTagTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] TagToTagHeader = {"TagID", "TagID"};
		boolean[] TagToTagKeys = {true, true};
		TagToTagTable = new IndexedTable("TagToTagTable", new Record(TagToTagHeader), TagToTagKeys);
	  }
	  try
	  {
		ImageToImageTable = new IndexedTable(Filename + "_ImageToImageTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] ImageToImageHeader = {"LinkID", "FromImageID", "ToImageID", "X", "Y", "Width", "Height"};
		boolean[] ImageToImageKeys = {true, false, false, false, false, false, false};
		ImageToImageTable = new IndexedTable("ImageToImageTable", new Record(ImageToImageHeader), ImageToImageKeys);
	  }
	  try
	  {
		ImageToNoteTable = new IndexedTable(Filename + "_ImageToNoteTable");
	  }	
	  catch(Exception TheError)
	  {
		String[] ImageToNoteHeader = {"NoteID", "ImageID", "Note", "X", "Y", "Width", "Height"};
		boolean[] ImageToNoteKeys = {true, false, false, false, false, false, false};
		ImageToNoteTable = new IndexedTable("ImageToNoteTable", new Record(ImageToNoteHeader), ImageToNoteKeys);
	  }
  }
  
  String getName() { return Name; }
  IndexedTable getImageTable() { return ImageTable; }
  IndexedTable getTagTable() { return TagTable; }
  IndexedTable getImageToTagTable() { return ImageToTagTable; }
  IndexedTable getTagToTagTable() { return TagToTagTable; }
  IndexedTable getImageToImageTable() { return ImageToImageTable; }
  IndexedTable getImageToNoteTable() { return ImageToNoteTable; }
  
  // Prints out a representation of the different tables
  void print()
  {
      System.out.println(ImageTable.toString());
      System.out.println(TagTable.toString());
      System.out.println(ImageToTagTable.toString());
      System.out.println(TagToTagTable.toString());
      System.out.println(ImageToImageTable.toString());
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
  }
  
  // Add a new image to the database
  String addImage(String Title, String Filename)
  {
      String[] RecordArray = {Integer.toString(NextImageID), Title, Filename};
      NextImageID++;
      if (ImageTable.addRecord(new Record(RecordArray)) == -1)
        return null; // Failed, record already present
      else
        return Integer.toString(NextImageID - 1);
  }
  
  // Delete an image from the database (by fields)
  int deleteImage(String ImageID, String Title, String Filename)
  {
    String[] RecordArray = {ImageID, Title, Filename};
    return deleteImage(new Record(RecordArray));
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
	Matches = ImageToImageTable.getRecords(r.getField(1),0);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToImageTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	Matches = ImageToImageTable.getRecords(r.getField(2),1);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToImageTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	//Delete ImageToNote records containing the image
	Matches = ImageToNoteTable.getRecords(r.getField(1),0);
	Records = Matches.elements();
	while (Records.hasMoreElements())
    {
      if (ImageToNoteTable.deleteRecord((Record)Records.nextElement()) == -1)
        return -1;
    }
	// Delete the image record from the image table
	if (ImageTable.deleteRecord(r) == -1)
      return -1;
    return 1;
  }
  
  // Delete an ImageToImage link from the database (by fields)
  int deleteLink(String LinkID)
  {
    String[] RecordArray = {LinkID, null, null, null, null, null, null};
    return deleteLink(new Record(RecordArray));
  }
  
  // Delete an ImageToImage link from the database (by record)
  int deleteLink(Record r)
  {
    return ImageToImageTable.deleteRecord(r);
  }
  
  // Delete an ImageToNote link from the database (by fields)
  int deleteNote(String NoteID)
  {
    String[] RecordArray = {NoteID, null, null, null, null, null, null};
    return deleteNote(new Record(RecordArray));
  }
  
  // Delete an ImageToImage link from the database (by record)
  int deleteNote(Record r)
  {
    return ImageToNoteTable.deleteRecord(r);
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
    return ImageToTagTable.deleteRecord(r);
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
    return TagToTagTable.deleteRecord(r);
  }
  
  // Delete a tag from the database (by fields)
  int deleteTag(String TagID)
  {
    String[] RecordArray = {TagID, getTagTitleFromTagID(TagID)};
    return deleteTag(new Record(RecordArray));
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
      return Integer.toString(NextTagID - 1);
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
        return 1;
      }
  }
  
  // Link an area of an image to a note
  String addImageNote(String ImageID, String Note, int X, int Y, int Width, int Height)
  {
    String[] RecordString = {Integer.toString(NextNoteID), ImageID, Note, Integer.toString(X), Integer.toString(Y), Integer.toString(Width), Integer.toString(Height)};
    NextNoteID++;
	if (ImageTable.getRecord(ImageID, 0) == null)
      return null;
    else
	{
      ImageToNoteTable.addRecord(new Record(RecordString));
      return Integer.toString(NextNoteID-1);
	}
  }
  
  // Link an area of an image to another image
  int linkImage(String ToImageID, String FromImageID, int X, int Y, int Width, int Height)
  {
    String[] RecordString = {Integer.toString(NextLinkID), ToImageID, FromImageID, Integer.toString(X), Integer.toString(Y), Integer.toString(Width), Integer.toString(Height)};
    NextLinkID++;
	if (ImageTable.getRecord(ToImageID, 0) == null)
      return 0;
    else
      if (ImageTable.getRecord(FromImageID, 0) == null)
        return -1;
      else
      {
        ImageToImageTable.addRecord(new Record(RecordString));
        return 1;
      }
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
          return 1;
      }
  }
  
  // Produce a sub table of Notes for a certain ImageID
  IndexedTable getNotesFromImageID(String ImageID)
  {
	return ImageToNoteTable.getRecords(ImageID, 1);
  }
  
  // Produce a sub table of links for a certain ImageID
  IndexedTable getLinksFromImageIDPoint(String ImageID)
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
	IndexedTable TempTable = ImageToImageTable.getRecords(ImageID, 0);
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
		{
			PointNotes.addRecord(TempRecord);
			System.out.println("inside note");
		}
			
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