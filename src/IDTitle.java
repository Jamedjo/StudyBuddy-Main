class IDTitle
{
  private String ID;
  private String Title;

  public String getID() {  return ID; }
  public String toString() {  return Title; }
  
  IDTitle(String NewID, String NewTitle)
  {
    ID = NewID;
    Title = NewTitle;
  }
  
  public boolean equals(Object CompareTo)
  {
    IDTitle TempIDTitle = (IDTitle)CompareTo;
    if (ID.equals(TempIDTitle.getID()) && (Title.equals(TempIDTitle.toString())))
      return true;
    else
      return false;
  }
}