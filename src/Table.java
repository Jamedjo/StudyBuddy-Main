import java.util.ArrayList;
import java.util.Hashtable;
import java.io.*;
import java.util.Enumeration;

class Table
{
    private String Name;
    private Record Header;
    private int NumRecords;
    private Hashtable<String,Record> Records;
    private boolean[] KeyFields;
    
    // Return the Header record
    Record getHeader() { return Header; }
    
    // Return the Name of the table
    String getName() { return Name; }
    
    // Return the number of records
    int getNumRecords() { return NumRecords; }
    
    // Return the number of fields in each record
    int getNumFields() { return Header.getNumFields(); }
    
    // Return the key fields as an array of integers
    boolean[] getKeyFields() { return KeyFields; }
    
    // Return the elements as an Enumeration
    Enumeration elements() { return Records.elements(); }
    
    // Creates new table from scratch
    Table(String TableName, Record NewHeader, boolean[] NewKeyFields)
    {
        Name = TableName;
        Header = new Record(NewHeader);
        Records = new Hashtable<String,Record>();
        NumRecords = 0;
        KeyFields = new boolean[NewKeyFields.length];
        System.arraycopy(NewKeyFields, 0, KeyFields, 0, NewKeyFields.length);
    }
    
    // Creates new table from a file
    Table(String Filename) throws Exception
    {
      String[] TempArray;
      BufferedReader FileInput;
      String RecordString;
      Records = new Hashtable<String,Record>();
      try
      {
        // Try to open the file and read it
        FileInput = new BufferedReader(new FileReader(Filename));
        // Read the name of the table
        Name = FileInput.readLine();
        // Read the key fields
        TempArray = FileInput.readLine().split(",");
        KeyFields = new boolean[TempArray.length];
        for (int i=0; i< TempArray.length; i++)
        {
          if (Integer.parseInt(TempArray[i]) == 1)
            KeyFields[i] = true;
          else
            KeyFields[i] = false;
        }
        // Read the header record
        TempArray = FileInput.readLine().split(",");
        for (int i=0; i< TempArray.length; i++)
          TempArray[i] = FileUtils.unEscape(TempArray[i]);
        Header = new Record(TempArray);
        // Read the stored records
        RecordString = FileInput.readLine();
        while (RecordString != null)
        {
          TempArray = RecordString.split(",");
          for (int i=0; i< TempArray.length; i++)
            TempArray[i] = FileUtils.unEscape(TempArray[i]);
          this.addRecord(new Record(TempArray));
          RecordString = FileInput.readLine();
        }
      }
      // Catch exceptions from file handling and throw an error
//       catch(FileNotFoundError TheError){
//          throw TheError;
//      }
      catch (IOException TheError){
        throw TheError;
      }
//      catch
//      {
//        throw new Exception();
//      } 
    }
    
    // Convert the table to a formatted string
    public String toString()
    {
      String Result = null;
      int[] MaxLength = new int[this.getNumFields()];
      int r = 0;
      int j = 0;
      int TotalWidth = 0;
      Enumeration AllRecords;
      Record TempRecord;
      String TempField;
      // Find the longest item in each column for width
      AllRecords = Records.elements();
      while (AllRecords.hasMoreElements())
      {
        TempRecord = (Record)AllRecords.nextElement();
        for (int f=0; f<TempRecord.getNumFields(); f++)
        {
          if (MaxLength[f] < FileUtils.escape(TempRecord.getField(f)).length())
            MaxLength[f] = FileUtils.escape(TempRecord.getField(f)).length();
        }
      }
      // Check header isnt longest item for width
      for (int f=0; f<Header.getNumFields(); f++)
      {
        if (MaxLength[f] < FileUtils.escape(Header.getField(f)).length())
          MaxLength[f] = FileUtils.escape(Header.getField(f)).length();
      }
      // Get total line width
      for (int t=0; t<MaxLength.length; t++)
        TotalWidth += MaxLength[t] + 3;
      TotalWidth += 1;
      Result = Name + "\n";
      // Add dashes to separate header
      for (int d=0; d<TotalWidth; d++)
        Result = Result + "-";
      Result = Result + "\n|";
      // Add the header fields
      for (int f=0; f<Header.getNumFields(); f++)
      {
        if (KeyFields[f])
          Result = Result + "*";
        else
          Result = Result + " ";
        TempField = FileUtils.escape(Header.getField(f));
        Result = Result + TempField;
        j = TempField.length();
        while(j < MaxLength[f])
        {
          Result = Result + ' ';
          j++;
        }
        Result = Result + " |";
      }
      Result = Result + '\n';
      // Add dashes to separate header
      for (int d=0; d<TotalWidth; d++)
        Result = Result + "-";
      Result = Result + "\n";
      // Add the fields for each record
      AllRecords = Records.elements();
      while (AllRecords.hasMoreElements())
      {
        TempRecord = (Record)AllRecords.nextElement();
        Result = Result + "| ";
        for (int f=0; f<TempRecord.getNumFields(); f++)
        {
          TempField = FileUtils.escape(TempRecord.getField(f));
          Result = Result + TempField;
          j = TempField.length();
          while(j < MaxLength[f])
          {
            Result = Result + ' ';
            j++;
          }
          Result = Result + " |";
          if (f != TempRecord.getNumFields() -1)
            Result = Result + " ";
        }
        Result = Result + '\n';
      }
      return Result;
    }
    
    // Store the database in a file
    void save(String Filename)
    {
      Enumeration AllRecords;
      PrintStream FileOutput;
      Record TempRecord;
      // Try incase file input fails
      try
      {
        FileOutput = new PrintStream(new FileOutputStream(Filename));
        // Write the table name to the file
        FileOutput.println(Name);
        // Write the key fields to the file
        for (int k=0; k < KeyFields.length; k++)
        {
          if(KeyFields[k])
            FileOutput.print(1);
          else
            FileOutput.print(0);
          // Place a comma between fields (but not at end of line)
          if (k != KeyFields.length - 1)
            FileOutput.print(',');
        }
        FileOutput.println();
        // Go through header fields and write them to the file
        for (int i=0; i < Header.getNumFields(); i++)
        {
          // Escape the field and write to file
          FileOutput.print(FileUtils.escape(Header.getField(i)));
          // Place a comma between fields (but not at end of line)
          if (i != Header.getNumFields() - 1)
            FileOutput.print(',');
        }
        // Insert newline after header
        FileOutput.println();
        // Go through all records and write them to file
        AllRecords = Records.elements();
        while (AllRecords.hasMoreElements())
        {
          TempRecord = (Record)AllRecords.nextElement();
          // Go through all fields for each record
          for (int j=0; j < TempRecord.getNumFields(); j++)
          {
            // Escape the field and write to file
            FileOutput.print(FileUtils.escape(TempRecord.getField(j)));
            // Add comma after each field (but not at end of line)
            if (j != TempRecord.getNumFields() - 1)
              FileOutput.print(',');
          }
          // Add newline after each record
          FileOutput.println();
        }    
      }
      // Catch any errors from the writing to files
      catch (Exception TheError)
      {
        throw new Error(TheError);    
      }
    }
    
    // Returns a whole column of the table as an arraylist
    ArrayList<String> getColList(int col)
    {
      Record TempRecord;
      ArrayList<String> Result = new ArrayList<String>();
      Enumeration AllRecords = Records.elements();
      while (AllRecords.hasMoreElements())
      {
        TempRecord = (Record) AllRecords.nextElement();
        Result.add( TempRecord.getField(col));
      }
      return Result;
    }
      
    // Returns a whole column of the table as an array
    String[] getColArray(int col)
    {
      int i=0;
      Record TempRecord;
      String[] Result = new String[this.getNumRecords()];
      Enumeration AllRecords = Records.elements();
      while (AllRecords.hasMoreElements())
      {
        TempRecord = (Record) AllRecords.nextElement();
        Result[i] = TempRecord.getField(col);
        i++;
      }
      return Result;
    }
    // Retreive the fieldname of col from the header
    String getFieldName(int col)
    {
      try
      {
        return Header.getField(col);
      }
      catch (Error TheError)
      {
        throw new Error(TheError);
      }
    }
    
    // Retrieve the col of a fieldname in the header
    int getFieldIndex(String FieldName)
    {
      for (int f=0; f < Header.getNumFields(); f++)
        if (FieldName.equals(Header.getField(f)))
          return f;
      return -1;
    }
    
    // Retrieve the record with the given key
    Record getRecord(String Key)
    {
      if (Records.containsKey(Key))
        return (Record) Records.get(Key);
      else
        return null;
    }

    // Add the record into the table
    public int addRecord(Record RecordToAdd)
    {
      if (Records.containsKey(RecordToAdd.getKey(KeyFields)))
        return -1;
      else
      {
        Records.put(RecordToAdd.getKey(KeyFields), RecordToAdd);
        NumRecords++;
        return 1;
      }
    }

    // Deletes the record r from the table
    public int deleteRecord(Record RecordToDel)
    {
      if (Records.containsKey(RecordToDel.getKey(KeyFields)))
      {
        Records.remove(RecordToDel.getKey(KeyFields));
        NumRecords--;
        return 1;
      }
      else
        return -1;
    }
}