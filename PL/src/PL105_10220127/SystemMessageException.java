package PL105_10220127;

public class SystemMessageException extends Exception {
  private String mSystemCode;
  private String mAtom;
  private int mLine;
  private int mColumn;
  private ConsNode mHead = null;
  
  public SystemMessageException( String systemCode ) {
    this.mSystemCode = systemCode;
  } // SystemMessageException()
  
  public SystemMessageException( String systemCode, String unboundSymbol ) {
    this.mAtom = unboundSymbol;
    this.mSystemCode = systemCode;
  } // SystemMessageException()
  
  public SystemMessageException( String systemCode, int Line, int Column ) {
    this.mSystemCode = systemCode;
    this.mLine = Line;
    this.mColumn = Column;
  } // SystemMessageException()
  
  public SystemMessageException( String systemCode, String atom, int Line, int Column ) {
    this.mSystemCode = systemCode;
    this.mAtom = atom;
    this.mLine = Line;
    this.mColumn = Column;
  } // SystemMessageException()
  
  public SystemMessageException( String systemCode, String functionName, ConsNode head ) {
    this.mSystemCode = systemCode;
    this.mHead = head;
    this.mAtom = functionName;
  } // SystemMessageException()
  
  public String GetSystemCode() {
    return this.mSystemCode;
  } // GetSystemCode()
  
  public int GetLine() {
    return this.mLine;
  } // GetLine()
  
  public int GetColumn() {
    return this.mColumn;
  } // GetColumn()

  public String GetAtom() {
    return this.mAtom;
  } // GetAtom()
  
  public ConsNode GetHead() {
    return this.mHead;
  } // GetHead()
} // class SystemMessageException
