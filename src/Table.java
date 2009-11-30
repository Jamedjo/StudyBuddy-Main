import java.util.ArrayList;
import java.io.*;
class Table
{
    private String Name;
    private Record Header;
    private Record[] Records;
    private int NumRecords;
    
    Record getHeader() { return Header; }
    
    String getName() { return Name; }
    
    int getNumRecords() { return NumRecords; }
    
    int getNumFields() { return Header.getNumFields(); }
    
    // Creates new table from scratch
    Table(String name, Record newHeader)
    {
        Name = name;
        Header = new Record(newHeader);
        Records = new Record[10];
        NumRecords = 0;
    }
    
     // Creates new table from a file
    Table(String tablename, String filename)
    {
        int Stop = 0;
        Boolean IsHeader = true;
        Boolean Escaped = false;
        BufferedReader FileInput;
        FileReader TheStream;
        File TheFile;
        char TempChar;
        int TempInt;
        String FieldVal = "";
        ArrayList<String> FieldList = new ArrayList<String>();
        String[] FieldArray;
        TheFile = new File(filename);
        Records = new Record[10];
        try
        {
            // Try to open the file and read it
            TheStream = new FileReader(TheFile);
            FileInput = new BufferedReader(TheStream);
            // Start  to read the file
            while(Stop == 0)
            {
                // Read one character at a time
                TempInt = FileInput.read();
                // Check if end of file
                if (TempInt == -1)
                {
                    // Is end of file so stop reading
                    Stop = 1;
                }
                else
                {
                    // Not the end of the file so read the input as a character
                    TempChar = (char) TempInt;
                    // If its a \ then make the next character escaped
                    if (!Escaped && TempChar == '\\')
                        Escaped = true;
                    else
                    {
                        // If its an escape character convert ba<String> ck to real format
                        if (Escaped)
                        {
                            if (TempChar == 'c')
                                FieldVal = new String(FieldVal.concat(","));
                            if (TempChar == 'n')
                                FieldVal = new String(FieldVal.concat("\n"));
                            if (TempChar == '\\')
                                FieldVal = new String(FieldVal.concat("\\"));
                            Escaped = !Escaped;
                        }
                        else
                        {   
                            // Not an escaped character so check if its the end of a field or record
                            if (TempChar == ',')
                            {
                                // End of a field (,) so add the field to the list of fields
                                FieldList.add(FieldVal);
                                FieldVal = new String("");
                            }
                            else
                            {
                                if (TempChar == '\n')
                                {
                                    // End of a line so end of a record, combine the fields into a list, then array, then record.
                                    FieldList.add(FieldVal);
                                    FieldVal = new String("");
                                    FieldArray = new String[FieldList.size()];
                                    FieldList.toArray(FieldArray);
                                    FieldList = new ArrayList<String>();
                                    // Check if the record is the first record (i.e. the header) and add accordingly
                                    if (IsHeader)
                                    {
                                        IsHeader = false;
                                        Header = new Record(FieldArray);
                                    }
                                    else
                                    {
                                        insertRecord(new Record(FieldArray));
                                    }                        
                                }
                                else
                                    // Character is just a normal character so add it to the string for current field.
                                    FieldVal = new String(FieldVal.concat(Character.toString(TempChar)));
                            }
                        }
                    }
                }
            }
        }
        // Catch exceptions from file handling and throw an error
        catch (Exception TheError)
        {
            throw new Error(TheError);    
        }   
    }
    
    Table getMatches(Record r)
    {
        Table ResultTable = new Table(new String(Name.concat("Results")), r);
        for (int i = 0; i < NumRecords; i++)
        {
            if (Records[i].matches(r))
                ResultTable.insertRecord(Records[i]);
        }
        return ResultTable;
    }
    
    // Store the database in a file or if the filename is "print" then print the table instead
    void store(String filename)
    {
        int Stop = 0;
        Boolean IsHeader = true;
        PrintStream FileOutput;
        FileOutputStream TheStream;
        File TheFile;
        char TempChar;
        int TempInt;
        int i;
        int j;
        String TempString = null;
        Record TempRecord;
        // Turn the file into a file variable (wont be used if "print" is being used)
        TheFile = new File(filename);
        // Try incase file input fails
        try
        {
            if (filename.compareTo("print") != 0)
            {
                // If filename isn't print then print to file
                TheStream = new FileOutputStream(TheFile);
                FileOutput = new PrintStream(TheStream);
            }
            else
            {
                // If filename is "print" then output to System.out
                FileOutput = new PrintStream(System.out);
            }
            // Go through header fields and write them to the file
            for (i=0; i < Header.getNumFields(); i++)
            {
                // Turn field into a string
                TempString = new String(Header.getField(i));
                // Run through string replacing escaped characters with "fake" escaped characters
                for (j=0; j < TempString.length(); j++)
                {
                    if (TempString.charAt(j) == '\\')
                        FileOutput.print("\\\\");
                    else
                        if (TempString.charAt(j) == ',')
                            FileOutput.print("\\c");
                        else
                            if (TempString.charAt(j) == '\n')
                                FileOutput.print("\\n");
                            else
                                FileOutput.print(TempString.charAt(j));
                }
                // Place a comma between fields (but not at end of line)
                if (i != Header.getNumFields() - 1)
                    FileOutput.print(',');
            }
            // Insert newline after header
            FileOutput.print('\n');
            // Go through all records and write them to file
            for (i=0; i < NumRecords; i++)
            {
                // Go through all fields for each record
                for (j=0; j < Records[i].getNumFields(); j++)
                {
                    // Turn field into a string
                    TempString = new String(Records[i].getField(j));
                    // Run through string replacing escape characters with "fake" escape characters
                    for ( int k=0; k < TempString.length(); k++)
                    {
                        TempChar = TempString.charAt(k);
                        if (TempChar == '\\')
                            FileOutput.print("\\\\");
                        else
                            if (TempChar == ',')
                                FileOutput.print("\\c");
                            else
                                if (TempChar == '\n')
                                    FileOutput.print("\\n");
                                else
                                    FileOutput.print(TempChar);
                    }
                    // Add comma after each field (but not at end of line)
                    if (j != Header.getNumFields() - 1)
                        FileOutput.print(',');
                }
                // Add newline after each record
                FileOutput.print('\n');
            }    
        }
        // Catch any errors from the writing to files
        catch (Exception TheError)
        {
            throw new Error(TheError);    
        }
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
    int getFieldIndex(String fieldName)
    {
        int Result = -1;
        for (int i=0; i < Header.getNumFields(); i++)
        {
            if (fieldName.compareToIgnoreCase(Header.getField(i)) == 0)
                Result = i;
        }
        return Result;
    }
    
    // Returns the record at the given row
    Record getRecord(int row)
    {
        if (row < 0)
            throw new Error("Error row less than 0");
        else
            if (row >= getNumRecords())
                throw new Error("Error row too large");
            else
                return Records[row];
    }
    
    // Performs a binary search for "SearchFor" on the sorted column 0, returns the index of the first occurance or -1 if it isnt found.
    int find(String SearchFor)
    {
        int low = 0;
        int high = NumRecords;
        int mid;
        int Stop = 0;
        do
        {
            mid = (low + high)/2;
            if (SearchFor == null)
                System.out.print("SearchFor is null\n");
            System.out.print("mid is " + mid + "\n");
            if (SearchFor.compareTo(Records[mid].getField(0)) < 0)
                high = mid;
            else
                low = mid + 1;
        } while (low < high && SearchFor.compareTo(Records[mid].getField(0)) != 0);
        // Check if "SearchFor" has been found at Records[mid]
        if (SearchFor.compareTo(Records[mid].getField(0)) == 0)
        {   
            // If Searchfor has been found then back track through the records to find its first occurance
            while (Stop == 0)
            {
                // Check if there is a previous record (otherwise stop backtracking)
                if (mid > 0)
                {
                    // Check if the previous record also matched SearchFor, if so backtrack
                    if (SearchFor.compareTo(Records[mid-1].getField(0)) == 0)
                        mid--;
                    else
                        Stop = 1;
                }
                else
                    Stop = 1;
            }
            return mid;
        }
        else
            // If SearchFor is not found, return -1
            return -1;
    }
    
    // Performs a binary insert (on column 0) to add r in its correct position
    void insertRecord(Record r)
    {
        int low = 0;
        int high = NumRecords;
        int mid;
        int i;
        // If this is the first record then just insert it
        if (NumRecords == 0)
            Records[0] = new Record(r);
        else
        {
            // Otherwise perform a binary search for the record
            do
            {
                mid = (low + high)/2;
                if (r.getField(0).compareTo(Records[mid].getField(0)) < 0)
                    high = mid;
                else
                    low = mid + 1;
            } while (low < high && r.getField(0).compareTo(Records[mid].getField(0)) != 0);
            // This outputs the best slot for insertion of the record
            if (low == high)
                mid = low;
            // Records below it are then moved down to produce a space
            i = NumRecords - 1;
            while (i >= mid)
            {
                Records[i+1] = new Record(Records[i]);
                i--;
            }
            // The record is then inserted into the space
            Records[mid] = new Record(r);
        }
        NumRecords++;
        // If the table is too small it is resized
        if (NumRecords == Records.length)
            resize(Records.length * 2);
    }
    
    // Resizes the table (for when it is full, for example)
    void resize(int NewSize)
    {
        Record[] TempArray;
        if (NewSize < NumRecords)
            throw new Error("Tables cannot be made smaller than the number of records");
        else
        {
            // Create a bigger array
            TempArray = new Record[NewSize];
            // Copy records accross
            System.arraycopy(Records, 0 , TempArray, 0, NumRecords);
            // Make the Records array point to the bigger new array
            Records = TempArray;
        }    
    }
    
    // Deletes the record r from the table
    int deleteRecord(Record r)
    {
        int Index;
        // Perform a binary search for the record
        Index = find(r.getField(0));
        // If its not found then return -1
        if (Index == -1)
        {
            // Record not present
            return -1;
        }
        else
        {
            // Since find works only on column 0, move down table until all columns match
            while (Records[Index].matches(r) == false && Index < NumRecords)    
                Index++;
            // First column found but not matching record
            if (Index == NumRecords)
                return -1;
            else
            {
                // Move all records below the one to delete up one (overwriting the record)
                System.out.print("Removing from " + Index + "\n");
                while (Index < NumRecords - 1)
                {
                    System.out.print("rippling\n");
                    Records[Index] = new Record(Records[Index+1]);
                    Index++;
                }
                // Reduce record count
                NumRecords--;
                System.out.print("Removed");
                store("print");
                return 1;
            }
        }
    }

}