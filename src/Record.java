class Record
{
    private String[] Fields;

    Record(String[] NewFields)
    {
        if (NewFields.length == 0)
            throw new Error("Error no fields");
        Fields = new String[NewFields.length];
        for (int i = 0; i < NewFields.length; i++)
        {
            Fields[i] = NewFields[i];   
        }
    }
    
    Record(Record r)
    {
        Fields = new String[r.getNumFields()];
        for (int i = 0; i < r.getNumFields(); i++)
        {
            Fields[i] = new String(r.getField(i));
        }
    }
    
    boolean matches(Record r)
    {
        boolean result = true;
        for (int i = 0; i < Fields.length; i++)
        {
            if (r.getField(i) != null)
                if (r.getField(i).compareTo(Fields[i]) != 0)
                    result = false;
        }
        return result;
    }
         
                
    
    String getField(int col)
    {
        String Result = "Error";
        if (col < 0)
            throw new Error("Error column less than 0");
        else
            if (col >= Fields.length)
                throw new Error("Error column value too large");
            else
                Result = Fields[col];
        return Result;
    }
    
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
        
    int getNumFields()
    {
        return Fields.length;
    }
}