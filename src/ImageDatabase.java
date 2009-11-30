import java.io.*;
class ImageDatabase
{
    private IndexedTable ImageTable;
    private IndexedTable TagTable;
    private IndexedTable ImageToTagTable;
    private IndexedTable TagToTagTable;
    private String Name;
    private int NextImageID;
    private int NextTagID;
    
    ImageDatabase(String name)
    {
        Name = name;
        NextTagID = 0;
        NextImageID = 0;
        String[] ImageHeader = {"ImageID", "Title", "Filename"};
        ImageTable = new IndexedTable("ImageTable", new Record(ImageHeader));
        
        String[] TagHeader = {"TagID", "Title"};
        TagTable = new IndexedTable("TagTable", new Record(TagHeader));
        
        String[] ImageToTagHeader = {"ImageID", "TagID"};
        ImageToTagTable = new IndexedTable("ImageToTagTable", new Record(ImageToTagHeader));
        
        String[] TagToTagHeader = {"TagID", "TagID"};
        TagToTagTable = new IndexedTable("TagToTagTable", new Record(TagToTagHeader));
    }
    
    ImageDatabase(String name, String filename)
    {
        File IDFile;
        FileReader TheStream;
        BufferedReader FileInput;
        Name = name;
        try
        {
            IDFile = new File(filename + "_ID");
            TheStream = new FileReader(IDFile);
            FileInput = new BufferedReader(TheStream);
            NextImageID = Integer.parseInt(FileInput.readLine());
            NextTagID = Integer.parseInt(FileInput.readLine());
        }
        catch (Exception TheError)
        {
            throw new Error(TheError);    
        } 
        ImageTable = new IndexedTable("ImageTable", filename + "_ImageTable");
        TagTable = new IndexedTable("TagTable", filename + "_TagTable");
        ImageToTagTable = new IndexedTable("ImageToTagTable", filename + "_ImageToTagTable");
        TagToTagTable = new IndexedTable("TagToTagTable", filename + "_TagToTagTable");
    }
    
    String getName() { return Name; }
    IndexedTable getImageTable() { return ImageTable; }
    IndexedTable getTagTable() { return TagTable; }
    IndexedTable getImageToTagTable() { return ImageToTagTable; }
    IndexedTable getTagToTagTable() { return TagToTagTable; }
    
    void print()
    {
        ImageTable.print();
        TagTable.print();
        ImageToTagTable.print();
        TagToTagTable.print();
    }
    
    void store(String filename)
    {
        File IDFile;
        FileOutputStream TheStream;
        PrintStream FileOutput;
        try
        {
            IDFile = new File(filename + "_ID");
            TheStream = new FileOutputStream(IDFile);
            FileOutput = new PrintStream(TheStream);
            FileOutput.print(Integer.toString(NextImageID));
            FileOutput.print("\n");
            FileOutput.print(Integer.toString(NextTagID));
            FileOutput.print("\n");
        }
        catch (Exception TheError)
        {
            throw new Error(TheError);    
        } 
        ImageTable.store(filename + "_ImageTable");
        TagTable.store(filename + "_TagTable");
        ImageToTagTable.store(filename + "_ImageToTagTable");
        TagToTagTable.store(filename + "_TagToTagTable");
    }
    
    String addImage(String Title, String Filename)
    {
        String[] RecordString = {Integer.toString(NextImageID), Title, Filename };
        NextImageID++;
        ImageTable.insertRecord(new Record(RecordString));
        return Integer.toString(NextImageID - 1);
    }
    
    int deleteImage(String ImageID, String Title, String Filename)
    {
        int Result1 = 0;
        int Result2 = 0;
        IndexedTable ImageTags = getImageToTagTable().findMultiple(ImageID, 0);
        String[] ImageString = { ImageID, Title, Filename };
        Result1 = ImageTable.deleteRecord(new Record(ImageString));
        if (Result1 == 1)
            System.out.print("Deleted from main table\n");
        for (int i = 0; i < ImageTags.getNumFields(); i++)
        {
            Result2 = ImageToTagTable.deleteRecords(new Record(ImageTags.getRecord(i)));
        }
        if (Result2 == 1)
            System.out.print("Deleted from other tables\n");
        if (Result1 == 1 || Result2 == 1)
            return 1;
        else
            return 0;
    }
    
    int deleteImage(Record r)
    {
        int Result1 = 0;
        int Result2 = 0;
        IndexedTable ImageTags = getImageToTagTable().findMultiple(r.getField(0), 0);
        Result1 = ImageTable.deleteRecord(r);
        if (Result1 == 1)
            System.out.print("Deleted from main table\n");
        for (int i = 0; i < ImageTags.getNumFields(); i++)
        {
            Result2 = ImageToTagTable.deleteRecords(new Record(ImageTags.getRecord(i)));
        }
        if (Result2 == 1)
            System.out.print("Deleted from other tables\n");
        if (Result1 == 1 || Result2 == 1)
            return 1;
        else
            return 0;
    }
    
    String addTag(String Title)
    {
        String[] RecordString = {Integer.toString(NextTagID), Title};
        NextTagID++;
        TagTable.insertRecord(new Record(RecordString));
        return Integer.toString(NextTagID - 1);
    }
    
    void tagImage(String ImageID, String TagID)
    {
        String[] RecordString = { ImageID, TagID };
        if (ImageTable.findSingle(ImageID, 0) == null)
            throw new Error("Image cannot be tagged: image doesnt exist");
        else
            if (TagTable.findSingle(TagID, 0) == null)
                throw new Error("Image cannot be tagged: tag doesnt exist");
            else
                ImageToTagTable.insertRecord(new Record(RecordString));
    }
    
    void tagTag(String TageeID, String TaggerID)
    {
        String[] RecordString = { TageeID, TaggerID }; 
        if (TagTable.findSingle(TageeID, 0) == null)
            throw new Error("Tag cannot be tagged: tagee doesnt exist");
        else
            if (TagTable.findSingle(TaggerID, 0) == null)
                throw new Error("Tag cannot be tagged: tagger doesnt exist");
            else
                TagToTagTable.insertRecord(new Record(RecordString));
    }
    
    // Produce a sub table of Images that are tagged with the TagID
    IndexedTable getImagesFromTagID(String TagID)
    {
        IndexedTable Result;
        IndexedTable ImageIDs;
        // Produce a table of ImageIDs that are tagged with the TagID
        ImageIDs = ImageToTagTable.findMultiple(TagID, 1);
        // Result is an indexed table in the format of ImageTable
        Result = new IndexedTable("Result Table", ImageTable.getHeader());
        // Check there are ImageIDs with the Tag
        if (ImageIDs != null)
        {
            // For all the ImageIDs find the complete image record and add it to the result table
            for (int i = 0; i < ImageIDs.getNumRecords(); i++)
            {
                Result.insertRecord(new Record(ImageTable.findSingle(ImageIDs.getRecord(i).getField(0), 0)));
            }
            return Result;
        }
        else
            return null;
    }
    
    // Produce a sub table of Images that are tagged with the TagTitle
    IndexedTable getImagesFromTagTitle(String TagTitle)
    {
        IndexedTable Result;
        IndexedTable TagIDs;
        IndexedTable TempTable;
        //  Find all TagIDs that have the required title
        TagIDs = TagTable.findMultiple(TagTitle, 1);
        //  Result is an indexed table in the format of ImageTable
        Result = new IndexedTable("Result Table", ImageTable.getHeader());
        // If there are tags with the title
        if (TagIDs != null)
        {
            // For all the TagIDs find all images with the TagID and add them to the result table
            for (int i = 0; i < TagIDs.getNumRecords(); i++)
            {
                // Get all images with the tag
                TempTable = getImagesFromTagID(TagIDs.getRecord(i).getField(0));
                // Add all images with the tag to the results table
                for (int j = 0; j < TempTable.getNumRecords(); j++)
                {
                    Result.insertRecord(new Record(TempTable.getRecord(j)));
                    Result.print();
                }
            }
            if (Result.getNumRecords() == 0)
                return null;
            else
                return Result;
        }
        else
            return null;
    }
    
    String getTagIDFromTagTitle(String TagTitle)
    {
        Record TempRecord = new Record(TagTable.findSingle(TagTitle, 1));
        if (Record == null)
            return null;
        else
            return TempRecord.getField(0);
    }  
    
    String[] getFilenamesFromTagTitle(String TagTitle)
    {
        IndexedTable TempTable;
        TempTable = getImagesFromTagTitle(TagTitle);
        if (TempTable == null)
            return null;
        else
            return TempTable.getCol(2);
    }
    
    String[] getAllImageIDs()
    {
        return ImageTable.getCol(0);
    }
    
    String[] getAllTagTitles()
    {
        return TagTable.getCol(1);
    }
    
    String getImageFilename(String ImageID)
    {
        Record FoundRecord;
        FoundRecord = ImageTable.findSingle(ImageID, 0);
        if (FoundRecord == null)
            throw new Error("ImageID not found");
        else
            return FoundRecord.getField(2);
    }
    
    String[] getPossibleIDs(String Title)
    {
        return ImageTable.findMultiple(Title, 1).getCol(0);
    }
    
    Record getImageRecord(String ImageID)
    {
        Record FoundRecord;
        FoundRecord = ImageTable.findSingle(ImageID, 0);
        if (FoundRecord == null)
            throw new Error("ImageID not found");
        else
            return FoundRecord;
    }
    
    String[] getAllFilenames()
    {    
        return ImageTable.getCol(2);
    }

}