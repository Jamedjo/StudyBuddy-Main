class ImageDatabase
{
    private IndexedTable ImageTable;
    private IndexedTable TagTable;
    private IndexedTable ImageToTagTable;
    private IndexedTable TagToTagTable;
    private String Name;
    
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
    
    void addImage(String ImageID, String Title, String Filename)
    {
        String[] RecordString = { ImageID, Title, Filename }; 
        ImageTable.insertRecord(new Record(RecordString));
    }
    
    void deleteImage(String ImageID, String Title, String Filename)
    {
        String[] RecordString = { ImageID, Title, Filename }; 
        ImageTable.deleteRecord(new Record(RecordString));
    }
    
    void deleteImage(Record r)
    {
        ImageTable.deleteRecord(r);
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