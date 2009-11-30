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
        Name = name;
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
        ImageTable.store(filename + "_ImageTable");
        TagTable.store(filename + "_TagTable");
        ImageToTagTable.store(filename + "_ImageToTagTable");
        TagToTagTable.store(filename + "_TagToTagTable");
    }
    
    void addImage(String ImageID, String Title, String Filename)
    {
        String[] RecordString = { ImageID, Title, Filename };
        ImageTable.insertRecord(new Record(RecordString));
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
    
    void addTag(String TagID, String Title)
    {
        String[] RecordString = { TagID, Title}; 
        TagTable.insertRecord(new Record(RecordString));
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
    
    IndexedTable getImagesFromTag(String TagID)
    {
        IndexedTable Result;
        IndexedTable ImageIDs;
        ImageIDs = ImageToTagTable.findMultiple(TagID, 1);
        Result = new IndexedTable("Result Table", ImageTable.getHeader());
        if (ImageIDs != null)
        {
            for (int i = 0; i < ImageIDs.getNumRecords(); i++)
            {
                Result.insertRecord(new Record(ImageTable.findSingle(ImageIDs.getRecord(i).getField(0), 0)));
            }
            return Result;
        }
        else
            return null;
    }
    
    String[] getAllImageIDs()
    {
        return ImageTable.getCol(0);
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
    
}