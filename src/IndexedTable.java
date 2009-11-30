class IndexedTable
{
    private Table MainTable;
    private Table[] Indexes;
    
    IndexedTable(String name, Record Header)
    {
        String[] IndexHeader = new String[2];
        MainTable = new Table(name, Header);
        Indexes = new Table[MainTable.getNumFields() - 1];
        for (int i = 0; i < MainTable.getNumFields() - 1; i ++)
        {
            IndexHeader[0] = MainTable.getFieldName(i + 1);
            IndexHeader[1] = MainTable.getFieldName(0);
            Indexes[i] = new Table(name + "_index_" + i, new Record(IndexHeader));
        }
    }
    
    IndexedTable(String name, String filename)
    {
        MainTable = new Table(name, filename);
        Indexes = new Table[MainTable.getNumFields() - 1];
        for (int i = 0; i < MainTable.getNumFields() - 1; i ++)
        {
            Indexes[i] = new Table(name + "_index_" + i, filename + "_index_" + i);
        }
    }
    
    int getNumRecords() { return MainTable.getNumRecords(); }
    int getNumFields() { return MainTable.getNumFields(); }
    Record getHeader() { return MainTable.getHeader(); }
    Record getRecord(int row) { return MainTable.getRecord(row); }
    
    String[] getCol(int col)
    {
        String[] Result = new String[MainTable.getNumRecords()];
        for (int i = 0; i < MainTable.getNumRecords(); i++)
        {
            Result[i] = new String(MainTable.getRecord(i).getField(col));
        }
        return Result;
    }
    
    void store(String filename)
    {
        MainTable.store(filename);
        for (int i = 0; i < MainTable.getNumFields() - 1; i ++)
        {
            Indexes[i].store(filename + "_index_" + i);
        }
    }
    
    void print() 
    {
        System.out.print("--------------------\n"); 
        System.out.print(MainTable.getName() + "\n\n");
        MainTable.store("print");
        System.out.print("--------------------\n");
        for (int i = 0; i < MainTable.getNumFields() - 1; i ++)
        {
            System.out.print(Indexes[i].getName() + "\n\n");
            Indexes[i].store("print");
            System.out.print("--------------------\n");
        } 
    }
    
    void insertRecord(Record r)
    {
        String[] IndexRecord = new String[2];
        MainTable.insertRecord(r);
        for (int i = 0; i < MainTable.getNumFields() - 1; i++)
        {
            IndexRecord[0] = r.getField(i + 1);
            IndexRecord[1] = r.getField(0);
            Indexes[i].insertRecord(new Record(IndexRecord));
        }
    }
    
    void deleteRecord(Record r)
    {
        String[] IndexRecord = new String[2];
        MainTable.deleteRecord(r);
        for (int i = 1; i < MainTable.getNumFields() - 1; i++)
        {
            IndexRecord[0] = r.getField(i + 1);
            IndexRecord[1] = r.getField(0);
            Indexes[i].deleteRecord(new Record(IndexRecord));
        }
    }
    
    IndexedTable findMultiple(String SearchFor, int Field)
    {
        int Index;
        IndexedTable PartWayTable;
        IndexedTable ResultTable = new IndexedTable("Result_Table", MainTable.getHeader());
        if (Field > MainTable.getNumFields() - 1 || Field < 0)
            throw new Error("Field not in table");
        else
        {
            if (Field == 0)
            {
                Index = MainTable.find(SearchFor);
                if (Index == -1)
                    return null;
                while (Index < MainTable.getNumRecords() && MainTable.getRecord(Index).getField(0).compareTo(SearchFor) == 0)
                {
                    ResultTable.insertRecord(new Record(MainTable.getRecord(Index)));
                    Index++;
                }
            }
            else
            {
                Index = Indexes[Field - 1].find(SearchFor);
                if (Index == -1)
                    return null;
                PartWayTable = new IndexedTable("Temp_Table", Indexes[Field - 1].getHeader());
                while (Index < Indexes[Field - 1].getNumRecords() && Indexes[Field - 1].getRecord(Index).getField(0).compareTo(SearchFor) == 0)
                {
                    PartWayTable.insertRecord(new Record(Indexes[Field - 1].getRecord(Index)));
                    Index++;
                }
                for (int i = 0; i < PartWayTable.getNumRecords(); i++)
                {
                    Index = MainTable.find(PartWayTable.getRecord(i).getField(1));
                    ResultTable.insertRecord(MainTable.getRecord(Index));
                }
            
            }
        }
        return ResultTable;
    }
    
    Record findSingle(String SearchFor, int Field)
    {
        int Index;
        Record ResultRecord;
        Record PartWayRecord;
        if (Field > MainTable.getNumFields() - 1 || Field < 0)
            throw new Error("Field not in table");
        else
        {
            if (Field == 0)
            {
                System.out.print("Searching for " + SearchFor + "\n");
                Index = MainTable.find(SearchFor);   
                    if (Index == -1)
                        return null;
                    else
                        ResultRecord = new Record(MainTable.getRecord(Index));
            }
            else
            {
                Index = Indexes[Field - 1].find(SearchFor);
                if (Index == -1)
                    return null;
                else
                {   
                    PartWayRecord = new Record(Indexes[Field - 1].getRecord(Index));
                    Index = MainTable.find(PartWayRecord.getField(1));
                    ResultRecord = MainTable.getRecord(Index);
                }
            }
        }
        return ResultRecord;
    }
    
    
    
}