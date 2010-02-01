class FileUtils
{
  // Make a string suitible for storing in a file
    static String escape(String EscapeString)
    {
      String ResultString = "";
      for (int i=0; i<EscapeString.length(); i++)
      {
        switch (EscapeString.charAt(i))
        {
          case '\n':
            ResultString = ResultString.concat("\\n");
          break;
          case '\\':
           ResultString = ResultString.concat("\\\\");
          break;
          case '\t':
            ResultString = ResultString.concat("\\t");
          break;
          case ',':
            ResultString = ResultString.concat("\\c");
          break;
          default:
            ResultString = ResultString + EscapeString.charAt(i);
          break;
        }
      }
      return ResultString;
    }
    
    // Return a string stored in a file back to normal
    static String unEscape(String EscapeString)
    {
      String ResultString = "";
      int i=0;
      while (i < EscapeString.length())
      {
        if (EscapeString.charAt(i) == '\\')
        {
          i++;
          switch (EscapeString.charAt(i))
          {
            case 'n':
              ResultString = ResultString.concat("\n");
            break;
            case '\\':
             ResultString = ResultString.concat("\\");
            break;
            case 't':
              ResultString = ResultString.concat("\t");
            break;
            case 'c':
              ResultString = ResultString.concat(",");
            break;
            default:
            break;
          }
          i++;
        }
        else
        {
          ResultString = ResultString + EscapeString.charAt(i);
          i++;
        }
      }
      return ResultString;
    }
}