class TagNode
{
  private String TagID;
  private String TagTitle;

  public String getTagID() {  return TagID; }
  public String toString() {  return TagTitle; }
  
  TagNode(String NewID, String NewTitle)
  {
    TagID = NewID;
    TagTitle = NewTitle;
  }
  
  public boolean equals(Object CompareTo)
  {
    TagNode TempTagNode = (TagNode)CompareTo;
    if (TagID.equals(TempTagNode.getTagID()) && (TagTitle.equals(TempTagNode.toString())))
      return true;
    else
      return false;
  }
}