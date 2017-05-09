package PL105_10220127;

public class ErrorMessageException extends Exception {
  private String merrorCode;
  private String mAtom;
  private int mLine;
  private int mColumn;
  
  public ErrorMessageException( String errorCode ) {
    this.merrorCode = errorCode;
  } // ErrorMessageException()
  
  public ErrorMessageException( String errorCode, int Line, int Column ) {
    this.merrorCode = errorCode;
    this.mLine = Line;
    this.mColumn = Column;
  } // ErrorMessageException()
  
  public ErrorMessageException( String errorCode, String atom, int Line, int Column ) {
    this.merrorCode = errorCode;
    this.mAtom = atom;
    this.mLine = Line;
    this.mColumn = Column;
  } // ErrorMessageException()
  
  public String GetErrorCode() {
    return this.merrorCode;
  } // GetErrorCode()
  
  public int GetLine() {
    return this.mLine;
  } // GetLine()
  
  public int GetColumn() {
    return this.mColumn;
  } // GetColumn()

  public String GetAtom() {
    return mAtom;
  } // GetAtom()
} // class ErrorMessageException
