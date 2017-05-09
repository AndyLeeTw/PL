package PL105_10220127;

public class Token {
  private int mColumn;
  private int mLine;
  private String mData;
  
  public Token( String data, int line, int column ) {
    this.mData = data;
    this.mLine = line;
    this.mColumn = column;
  } // Token()
  
  public Token( Token TokenT ) {
    this.mData = TokenT.GetData();
    this.mColumn = TokenT.GetColumn();
  } // Token()
  
  public void SetData( String DataT ) {
    this.mData = DataT;
  } // SetData()
  
  public String GetData() {
    return this.mData;
  } // GetData()
  
  public void SetLine( int line ) {
    this.mLine = line;
  } // SetLine()
  
  public int GetLine() {
    return this.mLine;
  } // GetLine()

  public void SetColumn( int column ) {
    this.mColumn = column;
  } // SetColumn()
  
  public int GetColumn() {
    return this.mColumn;
  } // GetColumn()
} // class Token
