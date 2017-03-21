package PL_10220127;

public class Token {
  private int mcolumn;
  private String mdata;
  public Token( String dataT, int columnT ) {
    mdata = dataT;
    mcolumn = columnT;
  } // Token()
  
  public Token( Token TokenT ) {
    mdata = TokenT.GetData();
    mcolumn = TokenT.GetColumn();
  } // Token()
  
  public void SetData( String DataT ) {
    mdata = DataT;
  } // SetData()
  
  public String GetData() {
    return mdata;
  } // GetData()
  
  public int GetColumn() {
    return mcolumn;
  } // GetColumn()
} // class Token
