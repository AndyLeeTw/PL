package PL105_10220127;

public class ErrorMessageException extends Exception {
  private String merrorCode;
  
  public ErrorMessageException( String errorCode ) {
    this.merrorCode = errorCode;
  } // ErrorMessageException()
  
  public String GetErrorCode() {
    return this.merrorCode;
  } // GetErrorCode()
} // class ErrorMessageException
