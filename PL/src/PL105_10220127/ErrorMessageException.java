package PL105_10220127;

public class ErrorMessageException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String merrorCode;
  
  public ErrorMessageException( String errorCode ) {
    this.merrorCode = errorCode;
  }
  public String GetErrorCode() {
    return this.merrorCode;
  }
}
