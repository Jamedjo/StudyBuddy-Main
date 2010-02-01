class Record
{
    private String[] Fields;
    
    // Creates a new record from an array of the fields
    Record(String[] NewFields)
    {
      if (NewFields.length == 0)
        throw new Error("Error no fields");
      Fields = new String[NewFields.length];
      System.arraycopy(NewFields, 0, Fields, 0, NewFields.length);
    }
    
    // Creates a new record from a current record
    Record(Record r)
    {
      Fields = new String[r.getNumFields()];
      for (int i = 0; i < r.getNumFields(); i++)
        Fields[i] = new String(r.getField(i));
    }
    
    // Checks if two records match i.e. fields that arent null are equal
    boolean matches(Record r)
    {
      for (int i = 0; i < Fields.length; i++)
      {
        if (r.getField(i) != null)
          if (r.getField(i).equals(Fields[i]) == false)
            return false;
      }
      return true;
    }
     
    // Returns the field at the specified cilumn
    String getField(int col)
    {
      String Result = null;
      if (col < 0)
          throw new Error("Error column less than 0");
      else
        if (col >= Fields.length)
          throw new Error("Error column value too large");
        else
          Result = Fields[col];
      return Result;
    }
    
    // Set the field at col to value
    void setField(int col, String value)
    {
      if (col < 0)
        throw new Error("Error column less than 0");
      else
        if (col >= Fields.length)
          throw new Error("Error column value too large");
        else
          Fields[col] = value; 
    }
    
    // Returns the number of fields in the record
    int getNumFields()
    {
      return Fields.length;
    }
    
    // Constructs a string of the key fields to act as a key
    String getKey(boolean[] KeyFields)
    {
      String Result = new String("");
      for (int f=0; f<KeyFields.length; f++)
        if (KeyFields[f])
          Result = Result.concat(this.getField(f));
      return Result;
    }
}