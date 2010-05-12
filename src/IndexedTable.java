import java.util.ArrayList;
import java.io.*;
import java.util.Enumeration;

class IndexedTable
{
  private Table MainTable;
  private Index[] Indexes;
  
  // Create a new empty indexed table
  IndexedTable(String Name, Record Header, boolean[] NewKeyFields)
  {
    MainTable = new Table(Name, Header, NewKeyFields);
    Indexes = new Index[Header.getNumFields()];
    for (int i = 0; i < MainTable.getNumFields(); i++)
      Indexes[i] = new Index(Name + "_index_" + i);
  }
  
  // Create a new indexed table from file
  IndexedTable(String Filename) throws Exception
  {
      try
	  {
		  MainTable = new Table(Filename);
		  Indexes = new Index[MainTable.getNumFields()];
		  for (int i = 0; i < MainTable.getNumFields(); i ++)
			Indexes[i] = new Index(Filename + "_index_" + i, Filename + "_index_" + i);
	  }
	  catch(Exception TheError)
	  {
		throw new Exception("Error Creating IndexedTable",TheError.getCause());
	  }
  }
  
  int getNumRecords() { return MainTable.getNumRecords(); }
  int getNumFields() { return MainTable.getNumFields(); }
  boolean[] getKeyFields() { return MainTable.getKeyFields(); }
  Record getHeader() { return MainTable.getHeader(); }
  Enumeration elements() { return MainTable.elements(); }
  Enumeration keys() { return MainTable.elements(); }
  
  // Returns a whole column of the table as an arraylist
  ArrayList getColList(int col)
  {
    return MainTable.getColList(col);
  }
  
  // Returns a whole column of the table as an arraylist
  String[] getColArray(int col, boolean sort)
  {
    return MainTable.getColArray(col, sort);
  }
  
  // Store table and indexes in files
  void save(String Filename)
  {
    MainTable.save(Filename);
    for (int i = 0; i < MainTable.getNumFields(); i++)
    {
      Indexes[i].save(Filename + "_index_" + i);
    }
  }
  
  // Returns a string diagram representing the table
  public String toString() 
  {
    return MainTable.toString();
  }
  
  // Adds the record into the table and indexes
  int addRecord(Record RecordToAdd)
  {
    if (MainTable.addRecord(RecordToAdd) == -1)
      return -1;
    else
      for (int i = 0; i < MainTable.getNumFields(); i++)
        Indexes[i].addIndex(RecordToAdd, i, MainTable.getKeyFields());
    return 1;
  }
  
  // Deletes the record from the table and indexes
  int deleteRecord(Record RecordToDel)
  {
    if (MainTable.deleteRecord(RecordToDel) == -1)
      return -1;
    for (int i = 0; i < MainTable.getNumFields(); i++)
      if (Indexes[i].deleteIndex(RecordToDel, i, MainTable.getKeyFields()) == -1)
        return -1;
    return 1;
  }
  
  // Returns a sub table of all records matching the supplied field
  IndexedTable getRecords(String SearchFor, int Field)
  {
    int Index;
    ArrayList IndexResult;
    IndexedTable Result = new IndexedTable("Result_Table", MainTable.getHeader(), MainTable.getKeyFields());
    if ((Field < MainTable.getNumFields()) && (Field >= 0) && (SearchFor != null))
    {
      IndexResult = Indexes[Field].getMatches(SearchFor);
      if (IndexResult != null)
        for (int i=0; i<IndexResult.size(); i++)
          Result.addRecord(MainTable.getRecord((String)IndexResult.get(i)));
    }
    return Result;
  }
  
  // Returns a sub table of all records matching the supplied record, null fields ignored
  IndexedTable getRecords(Record SearchFor)
  {
    IndexedTable Result = new IndexedTable("Result_Table", MainTable.getHeader(), MainTable.getKeyFields());
    boolean First = true;
    if (SearchFor != null)
      for (int i=0; i<this.getNumFields(); i++)
      {
        if (SearchFor.getField(i) != null)
          if (First)
          {
            Result = this.getRecords(SearchFor.getField(i), i);
            First = false;
          }
          else
            Result = Result.getRecords(SearchFor.getField(i), i);
      }
    return Result;
  }
  
  // Returns the first record matching the supplied field
  Record getRecord(String SearchFor, int Field)
  {
    Enumeration Results = this.getRecords(SearchFor, Field).elements();
    if (Results.hasMoreElements())
      return (Record) Results.nextElement();
    else
      return null;
  }
  
  // Returns the first record matching the supplied record, null fields ignored
  Record getRecord(Record SearchFor)
  {
    Enumeration Results = this.getRecords(SearchFor).elements();
    if (Results.hasMoreElements())
      return (Record) Results.nextElement();
    else
      return null;
  }
        
}