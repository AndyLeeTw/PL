package PL105_10220127;

public class SystemMessageException extends Exception {
  private String SystemMessage;
  public SystemMessageException( String SystemMessage ) {
    this.SetSystemMessage( SystemMessage );
  } // SystemMessageException()
  
  public String GetSystemMessage() {
    return this.SystemMessage;
  } // GetSystemMessage()
  
  public void SetSystemMessage( String SystemMessage ) {
    this.SystemMessage = SystemMessage;
  } // SetSystemMessage()
}
