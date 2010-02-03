import java.io.*;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.*;

class ImageDatabase
{
  private IndexedTable ImageTable;
  private IndexedTable TagTable;
  private IndexedTable ImageToTagTable;
  private IndexedTable TagToTagTable;
  private String Name;
  private int NextImageID;
  private int NextTagID;
  
  // Create an image database with the supplies name
  ImageDatabase(String NewName)
  {
      Name = NewName;
      NextTagID = 0;
      NextImageID = 0;
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
      }
      catch (Exception TheError)
      {
        throw new Error(TheError);    
      } 
      ImageTable = new IndexedTable(Filename + "_ImageTable");
      TagTable = new IndexedTable(Filename + "_TagTable");
      ImageToTagTable = new IndexedTable(Filename + "_ImageToTagTable");
      TagToTagTable = new IndexedTable(Filename + "_TagToTagTable");
  }
  
  String getName() { return Name; }
  IndexedTable getImageTable() { return ImageTable; }
  IndexedTable getTagTable() { return TagTable; }
  IndexedTable getImageToTagTable() { return ImageToTagTable; }
  IndexedTable getTagToTagTable() { return TagToTagTable; }
  
  // Prints out a representation of the different tables
  void print()
  {
      System.out.println(ImageTable.toString());
      System.out.println(TagTable.toString());
      System.out.println(ImageToTagTable.toString());
      System.out.println(TagToTagTable.toString());
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
    }
    catch (Exception TheError)
    {
      throw new Error(TheError);    
    } 
    ImageTable.save(Filename + "_ImageTable");
    TagTable.save(Filename + "_TagTable");
    ImageToTagTable.save(Filename + "_ImageToTagTable");
    TagToTagTable.save(Filename + "_TagToTagTable");
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
    IndexedTable TagMatches;
    Enumeration TagRecords;
    if (ImageTable.deleteRecord(r) == -1)
      return -1;
    TagMatches = ImageToTagTable.getRecords(r.getField(0), 0);
    TagRecords = TagMatches.elements();
    while (TagRecords.hasMoreElements())
    {
      if (ImageToTagTable.deleteRecord((Record)TagRecords.nextElement()) == -1)
        return -1;
    }
    return 1;
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
  
  // Get an array of IDTitles of all tags
  String[] getAllTagIDs()
  {
    return TagTable.getColArray(0);
  }
  
  // Get an array of all the tag Titles
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
  
  // Adds all tags tagged by a node to that node (in a tree)
  private DefaultMutableTreeNode addTreeTags(DefaultMutableTreeNode NodeAddTo, Hashtable<String,IDTitle> PathTags)
  {
    String[] TagIDs;
    DefaultMutableTreeNode TempTreeNode;
    IDTitle TempIDTitle;
    // Gets a list of tags tagged with this tag (or all if root node)
    TempIDTitle = (IDTitle) NodeAddTo.getUserObject();
    if (TempIDTitle.getID().equals("-1"))
      TagIDs = getAllTagIDs();
    else
      TagIDs = getTagIDsFromTagID(TempIDTitle.getID());
    // For all of those tags, add them to the node as branches and recurse
    if (TagIDs != null)
      for (int i=0; i<TagIDs.length; i++)
      {
        if (PathTags.containsKey(TagIDs[i]) == false)
        {
          TempIDTitle = new IDTitle(TagIDs[i], getTagTitleFromTagID(TagIDs[i]));
          TempTreeNode = new DefaultMutableTreeNode(TempIDTitle);
          PathTags.put(TagIDs[i], TempIDTitle);
          TempTreeNode = addTreeTags(TempTreeNode, PathTags);
          PathTags.remove(TagIDs[i]);
          NodeAddTo.add(TempTreeNode);
        }
      }
    return NodeAddTo;
  }

  // Add a tag to the database from a selection on a tag tree
  public JTree addTagFromTree(JTree TreeAddTo, JFrame Window)
  {
    String NewTag = (String)JOptionPane.showInputDialog(Window, "Name of new Tag", "Create Tag", JOptionPane.PLAIN_MESSAGE, null, null, "");
    String AddResult;
    DefaultMutableTreeNode NodeAddTo = null;
    DefaultMutableTreeNode NodeToAdd = null;
    IDTitle NodeAddToObject = null;
    boolean AddToRoot = false;
    DefaultTreeModel Model;
    // Check user inputted tag name is valid
    if ((NewTag != null) && (NewTag.length() > 0))
    {
      // Add the new tag into the tag table
      AddResult = addTag(NewTag);
      if (AddResult != null)
      {
        // Find the currently selected node in the tree
        if (TreeAddTo.getSelectionPath() == null)
          AddToRoot = true;
        else
        {
          NodeAddTo = (DefaultMutableTreeNode)TreeAddTo.getLastSelectedPathComponent();
          // If root node selected then add to root node
          NodeAddToObject = (IDTitle)NodeAddTo.getUserObject();
          if (NodeAddToObject.getID().equals(-1))
            AddToRoot = true;
          if (AddToRoot == false)
          tagTag(AddResult, NodeAddToObject.getID());
        }
        if (AddToRoot == true)
          NodeAddTo = (DefaultMutableTreeNode) TreeAddTo.getModel().getRoot();
        NodeToAdd = new DefaultMutableTreeNode(new IDTitle(AddResult, NewTag));
        Model =(DefaultTreeModel)TreeAddTo.getModel();
        Model.insertNodeInto(NodeToAdd, NodeAddTo, NodeAddTo.getChildCount());
      }
    }
    return TreeAddTo;
  }
  
  // Delete a tag from the database from a selection on a tag tree
  public JTree deleteTagFromTree(JTree TreeDelFrom)
  {
    DefaultMutableTreeNode NodeToDel;
    IDTitle NodeToDelObject = null;
    boolean IsRoot = false;
    // Find the currently selected node in the tree
    if (TreeDelFrom.getSelectionPath() == null)
      IsRoot = true;
    else
    {
      NodeToDel = (DefaultMutableTreeNode)TreeDelFrom.getLastSelectedPathComponent();
      NodeToDelObject = (IDTitle)NodeToDel.getUserObject();
      if (NodeToDelObject.getID().equals("-1"))
        IsRoot = true;
    }
    if (IsRoot == false)
      deleteTag(NodeToDelObject.getID());
    return toTree();
  }
  
  // Converts the imagedatabase to a tree and returns the tree
  public JTree toTree()
  {
    JTree Result;
    DefaultMutableTreeNode RootNode = new DefaultMutableTreeNode(new IDTitle("-1", "All Tags"));
    RootNode = addTreeTags(RootNode, new Hashtable<String,IDTitle>());
    Result = new JTree(RootNode);
    Result.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    return Result;
  }

}