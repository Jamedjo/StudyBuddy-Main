import java.util.ArrayList;
import java.util.Hashtable;
import java.io.*;
import java.util.Enumeration;

class Index
{
  private Hashtable<String,ArrayList<String>> Records;
  private String Name;
  
  // Create a new index
  Index(String NewName)
  {
    Records = new Hashtable<String,ArrayList<String>>();
    Name = NewName;
  }
  
  // Create an index from a file
  Index(String Filename, String NewName) throws Exception
  {
    Records = new Hashtable<String,ArrayList<String>>();
    ArrayList<String> KeyList;
    BufferedReader FileInput;
    String IndexKey;
    String[] KeyArray;
    try
    {
      // Try to open the file and read it
      FileInput = new BufferedReader(new FileReader(Filename));
      // Read the name of the table
      Name = FileUtils.unEscape(FileInput.readLine());
      // Read the index key
      IndexKey = FileInput.readLine();
      if (IndexKey != null)
        IndexKey = FileUtils.unEscape(IndexKey);
      while (IndexKey != null)
      {
        KeyArray = FileInput.readLine().split(",");
        KeyList = new ArrayList<String>();
        for (int i=0; i<KeyArray.length; i++)
          KeyList.add(FileUtils.unEscape(KeyArray[i]));
        Records.put(IndexKey, KeyList);
        IndexKey = FileInput.readLine();
        if (IndexKey != null)
          IndexKey = FileUtils.unEscape(IndexKey);
      } 
    }
    catch(Exception TheError)
    {
      throw new Exception();    
    }
  }
  
  // Save the index to a file
  public void save(String Filename)
  {
    Enumeration AllKeys;
    PrintStream FileOutput;
    String TempKey;
    ArrayList<String> TempList;
    boolean First = true;
    // Try incase file input fails
    try
    {
      FileOutput = new PrintStream(new FileOutputStream(Filename));
      FileOutput.print(FileUtils.escape(Name));
      // Go through all keys and write arraylist to file
      AllKeys = Records.keys();
      while (AllKeys.hasMoreElements())
      {
        TempKey = (String)AllKeys.nextElement();
        TempList = Records.get(TempKey);
        if (TempList.size() > 0)
        {
          FileOutput.println();
          FileOutput.println(FileUtils.escape(TempKey));
          for (int k=0; k<TempList.size(); k++)
          {
            FileOutput.print(FileUtils.escape(TempList.get(k)));
            if (k != TempList.size() -1)
              FileOutput.print(",");
          }
        }
      }
    }
    // Catch any errors from the writing to files
    catch (Exception TheError)
    {
      throw new Error(TheError);    
    }
  }
  
  // Add a record into the index
  public void addIndex(Record RecordToAdd, int FieldToIndex, boolean[] KeyFields)
  {
    ArrayList<String> TempList;
    String IndexKey = RecordToAdd.getField(FieldToIndex);
    String MainTableKey = RecordToAdd.getKey(KeyFields);
    if (Records.containsKey(IndexKey))
    {
      TempList = Records.get(IndexKey);
      TempList.add(MainTableKey);
      Records.put(IndexKey, TempList);
    }
    else
    {
      TempList = new ArrayList<String>();
      TempList.add(MainTableKey);
      Records.put(IndexKey, TempList);
    }
  }
 
  // Remove a record from the index
  public int deleteIndex(Record RecordToDel, int FieldToDel, boolean[] KeyFields)
  {
    int ListIndex;
    ArrayList<String> TempList;
    String IndexKey = RecordToDel.getField(FieldToDel);
    String MainTableKey = RecordToDel.getKey(KeyFields);
    if (Records.containsKey(IndexKey) == false)
      return -1;
    else
    {
      TempList = Records.get(IndexKey);
      ListIndex = TempList.indexOf(MainTableKey);
      if (ListIndex == -1)
        return -1;
      else
      {
        TempList.remove(ListIndex);
        Records.put(IndexKey, TempList);
      }
    }
    return 1;
  }
  
  // Find all indexes matching a certain String
  public ArrayList getMatches(String SearchFor)
  {
    return (ArrayList)Records.get(SearchFor);
  }

}